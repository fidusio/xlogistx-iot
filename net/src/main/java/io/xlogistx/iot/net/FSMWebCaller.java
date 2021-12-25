package io.xlogistx.iot.net;


import io.xlogistx.common.fsm.*;

import io.xlogistx.iot.net.data.TaskConfig;
import org.zoxweb.server.http.HTTPCall;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.task.TaskSchedulerProcessor;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.http.HTTPMessageConfigInterface;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.GetName;
import org.zoxweb.shared.util.NVBoolean;
import org.zoxweb.shared.util.NVLong;


import java.util.function.Function;
import java.util.logging.Logger;



public final class FSMWebCaller {

    public enum SMWebCaller
        implements GetName
    {
        APP_START("app-start"),
        // Wait state
        WAIT("wait"),
        // Web execution
        WEB_EXEC("web-exec"),
        SUCCESS("success"),
        //RESET("reset"),
        // Retry state
        RETRY("retry"),
        // Reset the retries
        //RESET_RETRY("reset-retry"),
        // failed retry
        FAIL_RETRY("fail-retry"),
        REPEAT("repeat"),
        RETRY_COUNTER("retry-counter"),
        REPEAT_COUNTER("repeat-counter"),
        SHUTDOWN("shutdown"),

        ;

        private String name;
        SMWebCaller(String name)
        {
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
        }
    }

    private static Logger log = Logger.getLogger(FSMWebCaller.class.getName());

    private FSMWebCaller(){}
    public static StateMachineInt<TaskConfig> create(String fsmName, TaskSchedulerProcessor tsp, TaskConfig config)
    {
        StateMachineInt<TaskConfig> fsm = new StateMachine<TaskConfig>(fsmName, tsp);
        TriggerConsumer<Void> init = new TriggerConsumer<Void>(StateInt.States.INIT) {
            @Override
            public void accept(Void o) {
                TaskConfig tc = (TaskConfig) getState().getStateMachine().getConfig();
                getState().getStateMachine().publish(new Trigger<Long>(getState(), SMWebCaller.WAIT, tc.getInitDelay()));
            }
        };

        TriggerConsumerInt<Long> wait = new TriggerConsumer<Long>(SMWebCaller.WAIT) {
            private long delta;
            private Runnable run = new Runnable() {
                private TriggerConsumer<Long> outer;
                Runnable init(TriggerConsumer<Long> outer)
                {
                    this.outer = outer;
                    return this;
                }
                @Override
                public void run() {
                    delta = System.currentTimeMillis() - delta;
                    TaskConfig tc = (TaskConfig) getState().getStateMachine().getConfig();
                    getState().getStateMachine().publish(new Trigger<Void>(getState(), SMWebCaller.WEB_EXEC, null));
                    log.info(outer + " waited for " + Const.TimeInMillis.toString(delta));
                };
            }.init(this);

            @Override
            public void accept(Long aLong) {
                    delta = System.currentTimeMillis();
                    getState().getStateMachine().getScheduler().queue(aLong, run);
                    log.info(this + " created for " + Const.TimeInMillis.toString(aLong));
            }
        };

        Function<HTTPMessageConfigInterface[], Boolean> functionExec = new Function<HTTPMessageConfigInterface[], Boolean>() {
            @Override
            public Boolean apply(HTTPMessageConfigInterface[] hmcis){
                try {
                    for (HTTPMessageConfigInterface hmci : hmcis)
                    {
                        if(hmci != null) {
                            log.info("URL:" + hmci.getURL());
                            HTTPCall hc = new HTTPCall(hmci);
                            log.info(this + ":" + hc.sendRequest());
                        }
                    }
                    return true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return false;
                }

            }
        };

        TriggerConsumerInt<HTTPMessageConfigInterface[]> webExec = new TriggerConsumer<HTTPMessageConfigInterface[]>(SMWebCaller.WEB_EXEC) {
            @Override
            public void accept(HTTPMessageConfigInterface[] hmcis) {
                // send http request
                if(hmcis == null)
                    hmcis = ((TaskConfig) getState().getStateMachine().getConfig()).getHTTPConfigs();

                SMWebCaller triggerID = SMWebCaller.FAIL_RETRY;
                if((boolean)getFunction().apply(hmcis))
                    triggerID = SMWebCaller.SUCCESS;

                getState().getStateMachine().publish(new Trigger<Void>(getState(), triggerID,null));
            }
        }.setFunction(functionExec);

        TriggerConsumerInt<Void> failRetry = new TriggerConsumer<Void>(SMWebCaller.FAIL_RETRY) {
            @Override
            public void accept(Void aVoid) {

                NVLong retryCounter = (NVLong) getState().getProperties().get((GetName)SMWebCaller.RETRY_COUNTER);
                long retries = retryCounter.getValue();
                retryCounter.setValue(++retries);
                TaskConfig tc = (TaskConfig) getState().getStateMachine().getConfig();
                if (tc.getRetries() > 0 && retryCounter.getValue() == tc.getRetries())
                {
                    retryCounter.setValue(0L);
                    getState().getStateMachine().publish(new Trigger<Void>(getState(), SMWebCaller.SUCCESS, null));
                }
                else
                {
                    // wait state
                    getState().getStateMachine().publish(new Trigger<Long>(getState(), SMWebCaller.WAIT, tc.getRetryDelay()));
                }
            }
        };

        TriggerConsumer<Void> resetRetry = new TriggerConsumer<Void>(SMWebCaller.SUCCESS) {
            @Override
            public void accept(Void aVoid) {
                NVLong retryCounter = (NVLong) getState().getProperties().get((GetName)SMWebCaller.RETRY_COUNTER);
                retryCounter.setValue(0L);
            }
        };

        TriggerConsumer<Void> repeat = new TriggerConsumer<Void>(SMWebCaller.SUCCESS) {
            @Override
            public void accept(Void aVoid) {
                NVLong repeatCounter = (NVLong) getState().getProperties().get((GetName)SMWebCaller.REPEAT_COUNTER);
                long repeats = repeatCounter.getValue();
                repeatCounter.setValue(++repeats);
                log.info("repeat counter " + repeatCounter.getValue());

                TaskConfig tc = (TaskConfig) getState().getStateMachine().getConfig();
                if (tc.getRepeats() >= 0 && repeatCounter.getValue() >= tc.getRepeats())
                {
                    // go to repeat
                    getState().getStateMachine().publish(new Trigger<Void>(getState(), StateInt.States.FINAL, null));
                }
                else
                {
                    // wait state
                    getState().getStateMachine().publish(new Trigger<Long>(getState(), SMWebCaller.WAIT, tc.getRepeatDelay()));
                }

            }
        };

        TriggerConsumer<Void> end = new TriggerConsumer<Void>(StateInt.States.FINAL) {
            @Override
            public void accept(Void aVoid) {
                Boolean shutdown = getState().getProperties().getValue(SMWebCaller.SHUTDOWN);
                if(shutdown != null && shutdown)
                {
                    // we will shutdown
                    TaskUtil.close();
                }
                log.info("Final end state");
            }
        };

        Function<Void, Void> functionAppStart = new Function<Void, Void>() {
            @Override
            public Void apply(Void v){
               log.info("######################## APP START NOOP. *********************************");
               return null;
            }
        };

        TriggerConsumerInt<Void> appStart = new TriggerConsumer<Void>(SMWebCaller.SUCCESS) {
            @Override
            public void accept(Void aVoid) {

                NVBoolean appStart = (NVBoolean) getState().getProperties().get(SMWebCaller.APP_START);

                if (!appStart.getValue())
                {
                    appStart.setValue(true);
                    log.info("######################## APP START will called only once. *********************************");
                    if(getFunction() != null)
                        getFunction().apply(aVoid);
                }

            }
        }.setFunction(functionAppStart);


        fsm.setConfig(config)
                .register(new State(StateInt.States.INIT).register(init))
                .register(new State(SMWebCaller.WAIT).register(wait))
                .register(new State(SMWebCaller.WEB_EXEC).register(webExec))
                .register(new State(SMWebCaller.RETRY, new NVLong(SMWebCaller.RETRY_COUNTER, 0)).register(failRetry).register(resetRetry))
                .register(new State(SMWebCaller.REPEAT, new NVLong(SMWebCaller.REPEAT_COUNTER, 0)).register(repeat))
                .register(new State(SMWebCaller.APP_START, new NVBoolean(SMWebCaller.APP_START, false)).register(appStart))
                .register(new State(StateInt.States.FINAL).register(end));

        return fsm;
    }

    public static void main(String... args) {
        try {

            int index = 0;
            String name = args[index++];
            String json = IOUtil.inputStreamToString(args[index++]);
            TaskConfig tc = GSONUtil.fromJSON(json, TaskConfig.class);
            StateMachineInt<TaskConfig> fsm = create(name, TaskUtil.getDefaultTaskScheduler(), tc);

            fsm.lookupState(StateInt.States.FINAL).getProperties().add(new NVBoolean(FSMWebCaller.SMWebCaller.SHUTDOWN.getName(), true));


            fsm.lookupState(SMWebCaller.WEB_EXEC).lookupTriggerConsumer(SMWebCaller.WEB_EXEC).setFunction(new Function<HTTPMessageConfigInterface[], Boolean>() {

                int count = 0;
                @Override
                public Boolean apply(HTTPMessageConfigInterface[] o) {
                    count++;
                    boolean status = count % 1 == 0;
                    log.info("^^^^^^^^^^^^^^^^^^Exec override " + count + " status:" + status);
                    return status;
                }
            });


            fsm.start(false);

        } catch (Exception e) {
         e.printStackTrace();
        }
    }
}

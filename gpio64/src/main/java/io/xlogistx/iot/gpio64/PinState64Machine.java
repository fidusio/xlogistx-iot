package io.xlogistx.iot.gpio64;

import com.pi4j.io.gpio.digital.*;
import io.xlogistx.common.data.DataTriggerAfterWait;
import org.zoxweb.server.fsm.*;
import org.zoxweb.server.http.OkHTTPCall;
import org.zoxweb.server.task.TaskSchedulerProcessor;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.http.HTTPMessageConfig;
import org.zoxweb.shared.http.HTTPMessageConfigInterface;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.http.HTTPResponseData;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.ParamUtil;
import org.zoxweb.shared.util.SharedStringUtil;
import org.zoxweb.shared.util.SharedUtil;

import java.util.List;
import java.util.function.Function;

/**
 * Pin State Machine for Pi4J v3.
 * Implements DigitalStateChangeListener instead of GpioPinListenerDigital from v1.x.
 */
public class PinState64Machine
        extends StateMachine<NVGenericMap>
        implements DigitalStateChangeListener {

    private final TriggerConsumerInt<Void> init = new TriggerConsumer<Void>(StateInt.States.INIT) {
        @Override
        public void accept(Void o) {
            log.getLogger().info(getState().getStateMachine().getName() + " CREATED");
            publish(new Trigger(getState(), PinStatus.WAITING, null));
        }
    };


    public enum PinStatus {
        PIN_STATE_CHANGED,
        WAITING_STATE,
        PIN_CHANGED,
        WAITING,
        PIN_HIGH,
        PIN_LOW,
    }


    public class WaitTrigger extends TriggerConsumer<DataTriggerAfterWait> {
        public WaitTrigger() {
            super(PinStatus.WAITING);
        }

        @Override
        public void accept(DataTriggerAfterWait waitTime) {
            if (waitTime != null) {
                if (waitTime.getWaitTime() > 0) {
                    getStateMachine().getScheduler().queue(waitTime.getWaitTime(), () -> {
                        publish(waitTime.getName(), waitTime.getData());
                    });
                }
            }
        }
    }

    public class StateChangeTrigger extends TriggerConsumer<DigitalStateChangeEvent> {
        DigitalGPIO64Stats digitalGPIOStats;

        public StateChangeTrigger() {
            super(PinStatus.PIN_CHANGED);
        }

        @Override
        public void accept(DigitalStateChangeEvent event) {
            if (digitalGPIOStats == null) {
                synchronized (this) {
                    if (digitalGPIOStats == null)
                        digitalGPIOStats = new DigitalGPIO64Stats(GPIO64Pin.lookup(event.source().address()));
                }
            }

            digitalGPIOStats.updateStats(event.state().isHigh());
            if (event.state() == DigitalState.HIGH)
                log.getLogger().info("[" + digitalGPIOStats.getLowCounter() + "] " + digitalGPIOStats + " " + event.state());
            publishSync(event.state(), event);
        }
    }

    public class ChangedState extends State {
        public ChangedState() {
            super(PinStatus.PIN_STATE_CHANGED);
            register(new StateChangeTrigger());
        }
    }

    public class PinHighTrigger extends TriggerConsumer<DigitalStateChangeEvent> {

        public PinHighTrigger() {
            super(DigitalState.HIGH);
        }

        @Override
        public void accept(DigitalStateChangeEvent event) {
            GPIO64Pin.GPIONameMap gpnm = GPIO64Pin.lookupGPIONameMap(event.source().address());
            log.getLogger().info("high trigger state: " + event.state() + (gpnm != null ? " " + gpnm : ""));
            Function<Void, HTTPResponseData> webCaller = getFunction();
            if (webCaller != null) {
                HTTPResponseData hrd = webCaller.apply(null);
                if (hrd != null)
                    log.getLogger().info("" + hrd);
            }

            publishSync(PinStatus.WAITING, null);
        }
    }

    public class PinHighState extends State {
        public PinHighState() {
            super(DigitalState.HIGH);
            register(new PinHighTrigger());
        }
    }


    public class PinLowTrigger extends TriggerConsumer<DigitalStateChangeEvent> {

        public PinLowTrigger() {
            super(DigitalState.LOW);
        }

        @Override
        public void accept(DigitalStateChangeEvent event) {
            GPIO64Pin.GPIONameMap gpnm = GPIO64Pin.lookupGPIONameMap(event.source().address());
            log.getLogger().info("low trigger state: " + event.state() + (gpnm != null ? " " + gpnm : ""));
            Function<Void, HTTPResponseData> webCaller = getFunction();
            if (webCaller != null) {
                HTTPResponseData hrd = webCaller.apply(null);
                if (hrd != null)
                    log.getLogger().info("" + hrd);
            }
            publishSync(PinStatus.WAITING, null);
        }
    }

    public class PinLowState extends State {
        public PinLowState() {
            super(DigitalState.LOW);
            register(new PinLowTrigger());
        }
    }


    public class WaitingState extends State {
        public WaitingState() {
            super(PinStatus.WAITING_STATE);
            register(new WaitTrigger());
        }

    }


    public PinState64Machine(TaskSchedulerProcessor taskSchedulerProcessor)
            throws NullPointerException {
        super("PIN_STATE_MONITOR", taskSchedulerProcessor);
        register(new State(StateInt.States.INIT)
                .register(init))
                .register(new ChangedState())
                .register(new WaitingState())
                .register(new PinHighState())
                .register(new PinLowState());
    }


    @Override
    public void onDigitalStateChange(DigitalStateChangeEvent event) {
        // callback when we have a new pin state change
        publish(new Trigger(getCurrentState(), PinStatus.PIN_CHANGED, event));
    }

    public void monitorDigitalPin(PullResistance pullState, String pin, String userDefinedName) {
        GPIO64Pin.GPIONameMap gpioNameMap = GPIO64Pin.toGPIONameMap(pin);
        if (gpioNameMap != null) {
            pin = gpioNameMap.gpioPin.getName();
            GPIO64Pin.mapGPIOName(gpioNameMap);

        }
        GPIO64Pin gpioPin = GPIO64Pin.lookupGPIO(pin);
        if (pullState == null)
            pullState = PullResistance.OFF;
        if (gpioPin != null) {
            log.getLogger().info(gpioPin + " " + userDefinedName);
            GPIO64Pin.mapGPIOName(userDefinedName, gpioPin);
            DigitalInput input = GPIO64Tools.SINGLETON.setInputPin(pullState, gpioPin);
            input.addListener(this);
        }
    }


    public static void main(String... args) {
        try {
            ParamUtil.ParamMap params = ParamUtil.parse("=", args);

            List<String> gpios = params.lookup("gpio");

            String pullState = params.stringValue("pull", "PULL_DOWN");

            for (String gpioConfig : gpios) {
                System.out.println(gpioConfig);
                String[] parsedGPIO = SharedStringUtil.parseToken(gpioConfig, 2, false, ":");
                String gpio = parsedGPIO[0];
                String name = parsedGPIO[1];
                String url = parsedGPIO[2];
                GPIO64Pin.GPIONameMap gpioNameMap = GPIO64Pin.toGPIONameMap(gpio + ":" + name);


                GPIO64Pin gpioPin = null;
                if (gpioNameMap != null) {
                    gpioPin = gpioNameMap.gpioPin;
                } else {
                    gpioPin = GPIO64Pin.lookupGPIO(gpio);
                }

                if (gpioPin == null) {
                    throw new IllegalArgumentException("Invalid pinID " + gpio);
                }
                PinState64Machine pinStateMachine = new PinState64Machine(TaskUtil.defaultTaskScheduler());


                // set up HTTP callback for pin high state
                if (url != null) {
                    HTTPMessageConfigInterface pinHMCI = HTTPMessageConfig.createAndInit(url, null, HTTPMethod.GET, false);
                    pinStateMachine.lookupState(DigitalState.HIGH)
                            .lookupTriggerConsumer(DigitalState.HIGH).setFunction((p) -> {
                                HTTPResponseData ret = null;
                                try {
                                    ret = OkHTTPCall.send(pinHMCI);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return ret;
                            });
                }
                PullResistance ppr = SharedUtil.lookupEnum(pullState, PullResistance.values());
                pinStateMachine.start(true);
                pinStateMachine.monitorDigitalPin(ppr, gpioPin.getName(), gpioNameMap != null ? gpioNameMap.nameMap : "state-monitor");

            }


        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("usage: [gpio=GPIO_02:Name2:url] [gpio=GPIO_03:Name3:url]  [pull=PULL_DOWN|PULL_UP ");
            System.exit(-1);
        }
    }
}

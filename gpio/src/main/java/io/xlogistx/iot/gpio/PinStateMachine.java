package io.xlogistx.iot.gpio;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import io.xlogistx.common.data.DataTriggerAfterWait;
import io.xlogistx.iot.gpio.data.GPIOUtil;
import org.zoxweb.server.fsm.*;
import org.zoxweb.server.http.HTTPCall;
import org.zoxweb.server.task.TaskSchedulerProcessor;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.http.HTTPMessageConfig;
import org.zoxweb.shared.http.HTTPMessageConfigInterface;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.http.HTTPResponseData;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.ParamUtil;
import org.zoxweb.shared.util.SharedUtil;

import java.util.function.Function;

public class PinStateMachine
        extends StateMachine<NVGenericMap>
        implements GpioPinListenerDigital
{

    private final TriggerConsumerInt<Void> init = new TriggerConsumer<Void>(StateInt.States.INIT) {
        @Override
        public void accept(Void o) {
            log.info(getState().getStateMachine().getName() + " CREATED");
            //SSLSessionConfig config = (SSLSessionConfig) getStateMachine().getConfig();
            publish(new Trigger(getState(), PinStatus.WAITING, null));
        }
    };



    public enum PinStatus
    {
        PIN_STATE_CHANGED,
        WAITING_STATE,
        PIN_CHANGED,
        WAITING,

    }



    public class WaitTrigger extends TriggerConsumer<DataTriggerAfterWait>
    {
        public WaitTrigger()
        {
            super(PinStatus.WAITING);
        }

        @Override
        public void accept(DataTriggerAfterWait waitTime) {
            if(waitTime != null)
            {
                if (waitTime.getWaitTime() > 0)
                {
                    // use scheduler
                    getStateMachine().getScheduler().queue(waitTime.getWaitTime(), ()->{
                        publish(waitTime.getName(), waitTime.getData());
                    });
                }
            }
        }
    }
    public class StateChangeTrigger extends TriggerConsumer<GpioPinDigitalStateChangeEvent>
    {
        DigitalGPIOStats digitalGPIOStats;
        public StateChangeTrigger() {
            super(PinStatus.PIN_CHANGED);
        }

        @Override
        public void accept(GpioPinDigitalStateChangeEvent event)
        {
            if(digitalGPIOStats == null)
            {
                synchronized (this)
                {
                    if(digitalGPIOStats == null)
                        digitalGPIOStats = new DigitalGPIOStats(GPIOPin.lookup(event.getPin().getPin()));
                }
            }

            digitalGPIOStats.updateStats(GPIOUtil.state(event.getState()));
            if(event.getState() == PinState.HIGH)
                log.getLogger().info( "[" + digitalGPIOStats.getLowCounter() + "] " + digitalGPIOStats + " " + event.getState());
            publishSync(event.getState(), event);
        }
    }

    public class ChangedState extends State
    {
        public ChangedState()
        {
            super(PinStatus.PIN_STATE_CHANGED);
            register(new StateChangeTrigger());
        }
    }

    public class PinHighTrigger extends TriggerConsumer<GpioPinDigitalStateChangeEvent>
    {

        public PinHighTrigger()
        {
            super(PinState.HIGH);
        }
        /**
         * Performs this operation on the given argument.
         *
         * @param event the input argument
         */
        @Override
        public void accept(GpioPinDigitalStateChangeEvent event)
        {
            GPIOPin.GPIONameMap gpnm = GPIOPin.lookupGPIONameMap(event.getPin().getPin());
            log.getLogger().info( "high trigger state: " + event.getState() + (gpnm != null ? " " + gpnm : ""));
            Function<Void, HTTPResponseData> webCaller = getFunction();
            if (webCaller != null)
            {
                HTTPResponseData hrd = webCaller.apply(null);
                if (hrd != null)
                    log.getLogger().info("" + hrd);
            }


            publishSync(PinStatus.WAITING, null);
        }
    }

    public  class PinHighState extends State
    {
        public PinHighState()
        {
            super(PinState.HIGH);
            register( new PinHighTrigger());
        }
    }


    public class PinLowTrigger extends TriggerConsumer<GpioPinDigitalStateChangeEvent>
    {

        public PinLowTrigger()
        {
            super(PinState.LOW);
        }
        /**
         * Performs this operation on the given argument.
         *
         * @param event the input argument
         */
        @Override
        public void accept(GpioPinDigitalStateChangeEvent event)
        {
            GPIOPin.GPIONameMap gpnm = GPIOPin.lookupGPIONameMap(event.getPin().getPin());
            log.getLogger().info( "high trigger state: " + event.getState() + (gpnm != null ? " " + gpnm : ""));

            publishSync(PinStatus.WAITING, null);
        }
    }

    public class PinLowState extends State
    {
        public PinLowState()
        {
            super(PinState.LOW);
            register( new PinLowTrigger());
        }
    }




    public class WaitingState extends State
    {
        public WaitingState()
        {
            super(PinStatus.WAITING_STATE);
            register(new WaitTrigger());
        }

    }


    public PinStateMachine(TaskSchedulerProcessor taskSchedulerProcessor)
            throws NullPointerException
    {
        super("PIN_STATE_MONITOR", taskSchedulerProcessor);
        register(new State(StateInt.States.INIT)
                .register(init))
                .register(new ChangedState())
                .register(new WaitingState())
                .register(new PinHighState())
                .register(new PinLowState());
    }


    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent gpioPinDigitalStateChangeEvent)
    {
        // call back when we have a new pin state change
        //log.info("State event " + gpioPinDigitalStateChangeEvent);
        publish(new Trigger(getCurrentState(), PinStatus.PIN_CHANGED, gpioPinDigitalStateChangeEvent));
    }

    public void monitorDigitalPin(PinPullResistance pullSate, String pin, String userDefinedName)
    {
        GPIOPin.GPIONameMap gpioNameMap = GPIOPin.toGPIONameMap(pin);
        if (gpioNameMap != null)
        {
            pin = gpioNameMap.gpioPin.getName();
            GPIOPin.mapGPIOName(gpioNameMap);

        }
        GPIOPin gpioPin = GPIOPin.lookupGPIO(pin);
        if(pullSate == null)
            pullSate = PinPullResistance.OFF;
        if(gpioPin != null)
        {
            log.getLogger().info( gpioPin + " " + userDefinedName);
            GPIOPin.mapGPIOName(userDefinedName, gpioPin);
            GpioPinDigitalInput input = GPIOTools.SINGLETON.getGpioController()
                    .provisionDigitalInputPin(gpioPin.getValue(), pullSate);
            input.addListener(this);
        }
    }



    public static void main(String ...args)
    {
        try
        {
            ParamUtil.ParamMap params = ParamUtil.parse("=", args);
            String gpioID = params.stringValue("gpio");
            String pullState = params.stringValue("pull", "PULL_DOWN");
            GPIOPin.GPIONameMap gpioNameMap = GPIOPin.toGPIONameMap(gpioID);
            String url = params.stringValue("url", true);

            GPIOPin gpioPin = null;
            if (gpioNameMap != null)
            {
                gpioPin = gpioNameMap.gpioPin;
            }
            else
            {
                gpioPin = GPIOPin.lookupGPIO(gpioID);
            }

            if(gpioPin == null)
            {
                throw  new IllegalArgumentException("Invalid pinID " + gpioID);
            }



            PinStateMachine pinStateMachine = new PinStateMachine(TaskUtil.defaultTaskScheduler());


            // very bad
            if (url != null)
            {
                HTTPMessageConfigInterface hmci = HTTPMessageConfig.createAndInit(url, null, HTTPMethod.GET, false);;
                pinStateMachine.lookupState(PinState.HIGH)
                        .lookupTriggerConsumer(PinState.HIGH).setFunction(new Function<Void, HTTPResponseData>() {

                            /**
                             * Applies this function to the given argument.
                             *
                             * @param unused the function argument
                             * @return the function result
                             */
                            @Override
                            public HTTPResponseData apply(Void unused) {
                                HTTPResponseData ret = null;
                                try {
                                    ret = HTTPCall.send(hmci);
                                } catch (Exception e) {
                                   e.printStackTrace();
                                }
                                return ret;
                            }
                        });
            }
            PinPullResistance ppr = SharedUtil.lookupEnum(pullState, PinPullResistance.values());
            pinStateMachine.start(true);
            pinStateMachine.monitorDigitalPin(ppr, gpioPin.getName(), gpioNameMap != null ? gpioNameMap.nameMap : "state-monitor");







        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("usage: url=https://web.com gpio=GPIO_02:Name [pull=PULL_DOWN|PULL_UP]");
        }
    }
}

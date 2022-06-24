package io.xlogistx.iot.gpio;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import io.xlogistx.common.fsm.*;

import org.zoxweb.server.task.TaskCallback;
import org.zoxweb.server.task.TaskSchedulerProcessor;

import org.zoxweb.shared.util.NVGenericMap;

public class PinStateMachine
        extends StateMachine<NVGenericMap>
        implements GpioPinListenerDigital
{


    private TriggerConsumerInt<Void> init = new TriggerConsumer<Void>(StateInt.States.INIT) {
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
        WAITING
    }



    public class WaitTrigger extends TriggerConsumer<TaskCallback<Long, Long>>
    {
        public WaitTrigger()
        {
            super(PinStatus.WAITING);
        }

        @Override
        public void accept(TaskCallback<Long, Long> waitTime) {
            if(waitTime != null)
            {
                if (waitTime.get() > 0)
                {
                    // use scheduler
                }
            }
        }
    }
    public class StateChangeTrigger extends TriggerConsumer<GpioPinDigitalStateChangeEvent>
    {

        public StateChangeTrigger() {
            super(PinStatus.PIN_CHANGED);
        }

        @Override
        public void accept(GpioPinDigitalStateChangeEvent event)
        {
            log.info( event.getPin() + " state :" + event.getState());
            publishSync(PinStatus.WAITING, null);
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
        register(new State(StateInt.States.INIT).register(init)).register(new ChangedState()).register(new WaitingState());
    }


    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent gpioPinDigitalStateChangeEvent)
    {
        // call back when we have a new pin state change
        //log.info("State event " + gpioPinDigitalStateChangeEvent);
        publish(new Trigger(getCurrentState(), PinStatus.PIN_CHANGED, gpioPinDigitalStateChangeEvent));
    }

    public void monitorDigitalPin(String pin, String userDefinedName)
    {

        GPIOPin gpioPins[] = GPIOPin.lookup(pin);
        for(GPIOPin gpioPin : gpioPins)
        {
            log.info("" + gpioPin + " " + userDefinedName);
            GPIOPin.mapGIOName(userDefinedName, gpioPin);
            GpioPinDigitalInput input = GPIOTools.SINGLETON.getGpioController()
                    .provisionDigitalInputPin(gpioPin.getValue());
            input.addListener(this);
        }
    }
}

package io.xlogistx.iot.gpio;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import io.xlogistx.common.task.RunnableProperties;
import org.zoxweb.shared.util.*;


import java.util.logging.Logger;

public class GPIOController
        extends RunnableProperties {

    private static final transient Logger log = Logger.getLogger(GPIOController.class.getName());

    public enum Param
            implements GetName {
        PIN("pin"),
        DURATION("duration"),
        STATE("state"),
        ;
        private final String name;

        Param(String name) {
            this.name = name;
        }


        @Override
        public String getName() {
            return name;
        }
    }

    @Override
    public void run() {
        try {
            Pin pin = GPIOPin.lookupPin(getProperties().getValue(Param.PIN.getName()));
            long duration = 0;

            GetNameValue<?> gnvDuration = getProperties().get(Param.DURATION.getName());
            if (gnvDuration != null) {
                if (gnvDuration instanceof NVInt) {
                    duration = ((NVInt) gnvDuration).getValue();
                } else if (gnvDuration instanceof NVLong) {
                    duration = ((NVLong) gnvDuration).getValue();
                }
            }
            boolean state = getProperties().getValue(Param.STATE);
            log.info("Set pin: " + pin + " to: " + state + " for: " + Const.TimeInMillis.toString(duration));
            GPIOTools.SINGLETON.setOutputPin(pin, PinState.getState(state), duration);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

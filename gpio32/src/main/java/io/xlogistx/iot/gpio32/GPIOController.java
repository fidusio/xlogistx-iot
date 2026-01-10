package io.xlogistx.iot.gpio32;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import io.xlogistx.common.task.RunnableProperties;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.shared.util.*;

public class GPIOController
        extends RunnableProperties {

    public static final LogWrapper log = new LogWrapper(GPIOController.class).setEnabled(true);

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
            log.getLogger().info("Set pin: " + pin + " to: " + state + " for: " + Const.TimeInMillis.toString(duration));
            GPIOTools.SINGLETON.setOutputPin(pin, PinState.getState(state), duration);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

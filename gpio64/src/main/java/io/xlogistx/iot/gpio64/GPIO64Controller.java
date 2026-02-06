package io.xlogistx.iot.gpio64;

import com.pi4j.io.gpio.digital.DigitalState;
import org.zoxweb.shared.task.RunnableProperties;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.shared.util.*;

public class GPIO64Controller
        extends RunnableProperties {

    private static final LogWrapper log = new LogWrapper(GPIO64Controller.class);

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
            Integer address = io.xlogistx.iot.data.GPIOBCMPin.lookupAddress(getProperties().getValue(Param.PIN.getName()));
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
            log.getLogger().info("Set pin: " + address + " to: " + state + " for: " + Const.TimeInMillis.toString(duration));
            GPIO64Tools.SINGLETON.setOutputPin(address, state ? DigitalState.HIGH : DigitalState.LOW, duration);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

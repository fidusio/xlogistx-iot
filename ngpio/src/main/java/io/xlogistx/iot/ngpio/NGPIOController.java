package io.xlogistx.iot.ngpio;

import com.pi4j.io.gpio.digital.DigitalState;
import io.xlogistx.common.task.RunnableProperties;
import org.zoxweb.shared.util.*;


import java.util.logging.Logger;

public class NGPIOController
        extends RunnableProperties {

    private static final transient Logger log = Logger.getLogger(NGPIOController.class.getName());

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
            Integer address = NGPIOPin.lookupAddress(getProperties().getValue(Param.PIN.getName()));
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
            log.info("Set pin: " + address + " to: " + state + " for: " + Const.TimeInMillis.toString(duration));
            NGPIOTools.SINGLETON.setOutputPin(address, state ? DigitalState.HIGH : DigitalState.LOW, duration);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

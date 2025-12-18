package io.xlogistx.iot.gpio.data;

import com.pi4j.io.gpio.PinState;

public final class GPIOUtil {
    private GPIOUtil() {
    }



    public static boolean state(PinState state) {
        return state == PinState.HIGH;
    }


}

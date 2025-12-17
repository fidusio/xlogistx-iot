package io.xlogistx.iot.ngpio.data;

import com.pi4j.io.gpio.digital.DigitalState;
import org.zoxweb.shared.util.GetName;
import org.zoxweb.shared.util.SharedUtil;

public final class NGPIOUtil {
    private NGPIOUtil() {
    }

    public enum PortType
            implements GetName {
        ANALOG("A"),
        DIGITAL("D"),
        PROVISIONING("P");

        private final String name;

        PortType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        public static PortType lookup(String str) {
            return SharedUtil.lookupEnum(str, PortType.values());
        }
    }

    public static boolean state(DigitalState state) {
        return state == DigitalState.HIGH;
    }


}

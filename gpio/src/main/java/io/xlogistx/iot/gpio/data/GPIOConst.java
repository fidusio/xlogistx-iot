package io.xlogistx.iot.gpio.data;

import org.zoxweb.shared.util.GetName;
import org.zoxweb.shared.util.SharedUtil;

public final class GPIOConst {
    private GPIOConst(){}

    public enum PortType
        implements GetName
    {
        ANALOG("A"),
        DIGITAL("D"),
        PROVISIONING("P")
        ;

        private final String name;
        PortType(String name)
        {
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
        }

        public static PortType lookup(String str)
        {
            return SharedUtil.lookupEnum(str, PortType.values());
        }
    }


}

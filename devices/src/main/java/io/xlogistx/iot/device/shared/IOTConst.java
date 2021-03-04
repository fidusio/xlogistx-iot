package io.xlogistx.iot.device.shared;

import org.zoxweb.shared.util.GetName;

public final class IOTConst {
    private IOTConst(){}

    public enum DeviceType
    {
        CLOUD_BASED,
        CONTAINER,
        CONTROLLER,
        GATEWAY,
        LAPTOP,
        MODEM,
        PHONE,
        POWER_SWITCH,
        ROUTER,
        SENSOR,
        SERVER,
        SWITCH,
        TABLET,
        EMBEDDED
    }

    public enum Protocol
    {
        I2C,
        I2S,
        SPI,
        WIFI,
        ETHER,
        USB
    }


    public enum PortType
    {
        DIGITAL,
        ANALOG,
        ADC,
        DAC,
        PWM
    }

    public enum PortState
        implements GetName
    {
        INPUT("I"),
        OUTPUT("O"),
        IO("IO")
        ;

        private final String name;

        PortState(String name)
        {
            this.name = name;
        }
        /**
         * @return the name of the object
         */
        @Override
        public String getName() {
            return name;
        }
    }

    public enum FrequencyUnit
    implements GetName
    {
        HZ("H"),
        KHZ("K"),
        MHZ("M"),
        GHZ("G");


        private final String name;

        FrequencyUnit(String name)
        {
            this.name = name;
        }

        /**
         * @return the name of the object
         */
        @Override
        public String getName() {
            return name;
        }
    }
}

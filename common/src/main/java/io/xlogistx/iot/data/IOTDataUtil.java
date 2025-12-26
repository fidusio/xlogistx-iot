package io.xlogistx.iot.data;

import io.xlogistx.iot.data.i2c.*;
import org.zoxweb.shared.filters.DataFilter;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.GetName;
import org.zoxweb.shared.util.NamedDescription;
import org.zoxweb.shared.util.RegistrarMapDefault;
import org.zoxweb.shared.util.SharedUtil;

public class IOTDataUtil {
    private IOTDataUtil() {}
    public static final RegistrarMapDefault<String, DataFilter> DATA_FILTER = new RegistrarMapDefault<>(null, DataFilter::getID);

    public static final RegistrarMapDefault<String, I2CCodecBase> I2C_CODEC_MANAGER = new RegistrarMapDefault<>(TokenFilter.UPPER_COLON, I2CCodecBase::getName).setNamedDescription(new NamedDescription("I2CCodecManager", "I2CProtocol"))
            .registerValue(new I2CCodec("ping", "Ping the device return the ping value as java int, usage: PING"))
            .registerValue(new I2CCodec("messages", "The number i2c messages processed by the device return the count value as java int, usage: MESSAGES"))
            .registerValue(new I2CCodec("cpu-speed", "Get the device cpu frequency in hz, value as java int, usage: CPU-SPEED"))
            .registerValue(new I2CAref())
            .registerValue(new I2CCodec("reset", "Reboot the device, no return value bus will throw exception, usage: RESET"))
            .registerValue(I2CUptime.SINGLETON)
            .registerValue(I2CVersion.SINGLETON)
            .registerValue(I2CEcho.SINGLETON)
            .registerValue(I2CAddress.SINGLETON)
            .registerValue(new I2CIO());

    public enum PinMode
            implements GetName
    {
        INPUT("I"),
        OUTPUT("O"),
        INPUT_PULL_UP("P");
        ;

        private final String name;
        PinMode(String name)
        {
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
        }
    }

    public enum IOAction
    {
        G,
        S,
        D,
        U,
        P
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
    /**
     * Supported pin functions/alternate modes
     */
    public enum PinFunction {
        GPIO,       // General Purpose I/O (digital input/output)
        PWM,        // Pulse Width Modulation
        I2C,        // I2C bus (SDA/SCL)
        SPI,        // SPI bus (MOSI/MISO/SCLK/CE)
        UART,       // Serial UART (TX/RX)
        PCM,        // PCM audio (CLK/FS/DIN/DOUT)
        GPCLK,      // General Purpose Clock
        ONE_WIRE    // 1-Wire protocol
    }

}

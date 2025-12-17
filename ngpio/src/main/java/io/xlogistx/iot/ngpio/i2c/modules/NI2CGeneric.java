package io.xlogistx.iot.ngpio.i2c.modules;

import com.pi4j.context.Context;
import io.xlogistx.iot.ngpio.i2c.NI2CBaseDevice;

/**
 * Generic I2C device for Pi4J v3.
 */
public class NI2CGeneric extends NI2CBaseDevice {

    public NI2CGeneric(String name, Context pi4j, int bus, int address) {
        super(name, pi4j, bus, address);
    }

    @Override
    public void close() throws Exception {
        if (getI2C() != null) {
            getI2C().close();
        }
    }
}

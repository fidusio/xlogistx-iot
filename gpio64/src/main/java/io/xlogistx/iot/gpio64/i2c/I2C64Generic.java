package io.xlogistx.iot.gpio64.i2c;

import org.zoxweb.shared.io.SharedIOUtil;

/**
 * Generic I2C device for Pi4J v3.
 */
public class I2C64Generic extends I2C64BaseDevice {

    public I2C64Generic(String name, int bus, int address) {
        super(name, bus, address);
    }

    @Override
    public void close() throws Exception {
        SharedIOUtil.close(getI2C());
    }
}

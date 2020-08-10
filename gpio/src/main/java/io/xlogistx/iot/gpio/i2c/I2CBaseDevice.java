package io.xlogistx.iot.gpio.i2c;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

public abstract class I2CBaseDevice
    implements AutoCloseable
{
    protected final I2CBus i2cBus;
    protected final I2CDevice i2cDevice;
    protected I2CBaseDevice(int bus, int address)
            throws IOException,
            I2CFactory.UnsupportedBusNumberException
    {
        i2cBus = I2CFactory.getInstance(bus);
        i2cDevice = i2cBus.getDevice(address);
    }

    protected I2CBaseDevice(I2CBus i2cBus, I2CDevice i2cDevice)
            throws IOException,
            I2CFactory.UnsupportedBusNumberException
    {
        this.i2cBus = i2cBus;
        this.i2cDevice = i2cDevice;
    }


    public I2CDevice getI2CDevice()
    {
        return i2cDevice;
    }

    public I2CBus getI2CBus()
    {
        return i2cBus;
    }

}

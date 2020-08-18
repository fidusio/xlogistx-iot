package io.xlogistx.iot.gpio.i2c;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import org.zoxweb.shared.util.CanonicalID;

import org.zoxweb.shared.util.SetName;
import org.zoxweb.shared.util.SharedUtil;

import java.io.IOException;

public abstract class I2CBaseDevice
    implements AutoCloseable, SetName, CanonicalID
{
    private final I2CBus i2cBus;
    private final I2CDevice i2cDevice;
    private String name;
    protected I2CBaseDevice(String name, int bus, int address)
            throws IOException,
            I2CFactory.UnsupportedBusNumberException
    {
        i2cBus = I2CFactory.getInstance(bus);
        i2cDevice = i2cBus.getDevice(address);
        this.name = name;
    }

    protected I2CBaseDevice(String name, I2CBus i2cBus, I2CDevice i2cDevice)
            throws IOException,
            I2CFactory.UnsupportedBusNumberException
    {
        this.i2cBus = i2cBus;
        this.i2cDevice = i2cDevice;
        this.name = name;
    }


    public I2CDevice getI2CDevice()
    {
        return i2cDevice;
    }

    public I2CBus getI2CBus()
    {
        return i2cBus;
    }


    public String toCanonicalID()
    {
        return SharedUtil.toCanonicalID('-', getName(), getI2CBus().getBusNumber(), Integer.toHexString(getI2CDevice().getAddress()));
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }




}

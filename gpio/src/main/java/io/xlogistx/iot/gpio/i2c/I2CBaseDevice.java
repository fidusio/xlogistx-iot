package io.xlogistx.iot.gpio.i2c;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.impl.I2CDeviceImpl;

import org.zoxweb.shared.util.CanonicalID;

import org.zoxweb.shared.util.NamedDescription;
import org.zoxweb.shared.util.SharedUtil;

import java.io.IOException;

public abstract class I2CBaseDevice
    extends NamedDescription
    implements AutoCloseable, CanonicalID
{
    private final I2CBus i2cBus;
    private final I2CDevice i2cDevice;
    protected I2CBaseDevice(String name, int bus, int address)
            throws IOException,
            I2CFactory.UnsupportedBusNumberException
    {
        super(name);
        i2cBus = I2CFactory.getInstance(bus);
        i2cDevice = i2cBus.getDevice(address);
    }

    protected I2CBaseDevice(String name, I2CBus bus, int address)
            throws IOException,
            I2CFactory.UnsupportedBusNumberException
    {
        this(name, bus, bus.getDevice(address));
    }

    protected I2CBaseDevice(String name, I2CBus i2cBus, I2CDevice i2cDevice)
    {
        super(name);
        SharedUtil.checkIfNulls("bus or device can't be null", i2cBus, i2cDevice);
        this.i2cBus = i2cBus;
        this.i2cDevice = i2cDevice;
        I2CDeviceImpl j;

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

    public int hashCode()
    {
        return i2cDeviceID(getI2CBus().getBusNumber(), getI2CDevice().getAddress()).hashCode();
    }

    public boolean equals(Object o)
    {
        if (o != null)
        {
            if(o instanceof I2CBaseDevice)
            {
                return (((I2CBaseDevice)o).getI2CBus().getBusNumber() == getI2CBus().getBusNumber() &&
                        ((I2CBaseDevice)o).getI2CDevice().getAddress() == getI2CDevice().getAddress());
            }
        }
        return false;
    }


    public static String i2cDeviceID(int bus, int address){
        return SharedUtil.toCanonicalID('-', "I2CDevice", bus, Integer.toHexString(address));
    }



}

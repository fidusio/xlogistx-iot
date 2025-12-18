package io.xlogistx.iot.gpio64.i2c;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import io.xlogistx.iot.gpio64.GPIO64Tools;
import org.zoxweb.shared.util.CanonicalID;
import org.zoxweb.shared.util.NamedDescription;
import org.zoxweb.shared.util.SUS;
import org.zoxweb.shared.util.SharedUtil;

/**
 * I2C Base Device for Pi4J v3.
 * Uses Context-based I2C configuration instead of I2CFactory from v1.x.
 */
public abstract class I2C64BaseDevice
        extends NamedDescription
        implements AutoCloseable, CanonicalID {

    private final I2C i2c;
    private final int bus;
    private final int address;
    private final Context pi4j;

    protected I2C64BaseDevice(String name, int bus, int address) {
        super(name);
        SUS.checkIfNulls("name can't be null", name);
        this.pi4j = GPIO64Tools.SINGLETON.getContext();
        this.bus = bus;
        this.address = address;
        this.i2c = I2C64Util.SINGLETON.createI2C(name, bus, address);
    }

    protected I2C64BaseDevice(String name, I2C i2c, int bus, int address) {
        super(name);
        SharedUtil.checkIfNulls("I2C can't be null", i2c);
        this.i2c = i2c;
        this.bus = bus;
        this.address = address;
        this.pi4j = GPIO64Tools.SINGLETON.getContext();
    }

    public I2C getI2C() {
        return i2c;
    }

    public int getBus() {
        return bus;
    }

    public int getAddress() {
        return address;
    }

    public Context getContext() {
        return pi4j;
    }

    @Override
    public String toCanonicalID() {
        return SharedUtil.toCanonicalID('-', getName(), bus, Integer.toHexString(address));
    }

    @Override
    public int hashCode() {
        return i2cDeviceID(bus, address).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            if (o instanceof I2C64BaseDevice) {
                I2C64BaseDevice other = (I2C64BaseDevice) o;
                return (other.getBus() == bus && other.getAddress() == address);
            }
        }
        return false;
    }

    public static String i2cDeviceID(int bus, int address) {
        return SharedUtil.toCanonicalID('-', "I2CDevice", bus, Integer.toHexString(address));
    }
}

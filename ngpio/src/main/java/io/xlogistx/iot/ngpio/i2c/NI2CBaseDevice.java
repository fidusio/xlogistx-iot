package io.xlogistx.iot.ngpio.i2c;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import org.zoxweb.shared.util.CanonicalID;
import org.zoxweb.shared.util.NamedDescription;
import org.zoxweb.shared.util.SharedUtil;

/**
 * I2C Base Device for Pi4J v3.
 * Uses Context-based I2C configuration instead of I2CFactory from v1.x.
 */
public abstract class NI2CBaseDevice
        extends NamedDescription
        implements AutoCloseable, CanonicalID {

    private final I2C i2c;
    private final int bus;
    private final int address;
    private final Context pi4j;

    protected NI2CBaseDevice(String name, Context pi4j, int bus, int address) {
        super(name);
        SharedUtil.checkIfNulls("Context can't be null", pi4j);
        this.pi4j = pi4j;
        this.bus = bus;
        this.address = address;

        I2CConfig config = I2C.newConfigBuilder(pi4j)
                .id("i2c-" + bus + "-" + Integer.toHexString(address))
                .name(name)
                .bus(bus)
                .device(address)
                .build();

        this.i2c = pi4j.create(config);
    }

    protected NI2CBaseDevice(String name, I2C i2c, int bus, int address) {
        super(name);
        SharedUtil.checkIfNulls("I2C can't be null", i2c);
        this.i2c = i2c;
        this.bus = bus;
        this.address = address;
        this.pi4j = null;
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
            if (o instanceof NI2CBaseDevice) {
                NI2CBaseDevice other = (NI2CBaseDevice) o;
                return (other.getBus() == bus && other.getAddress() == address);
            }
        }
        return false;
    }

    public static String i2cDeviceID(int bus, int address) {
        return SharedUtil.toCanonicalID('-', "I2CDevice", bus, Integer.toHexString(address));
    }
}

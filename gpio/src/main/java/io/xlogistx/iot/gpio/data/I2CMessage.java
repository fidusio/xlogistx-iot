package io.xlogistx.iot.gpio.data;


import org.zoxweb.shared.data.PropertyDAO;
import org.zoxweb.shared.data.SimpleMessage;

import org.zoxweb.shared.util.*;

public class I2CMessage extends SimpleMessage {
    public enum Param
            implements GetNVConfig {
        I2C_BUS(NVConfigManager.createNVConfig("i2c-bus", "I2C Bus", "I2CBus", false, true, int.class)),
        I2C_ADDRESS(NVConfigManager.createNVConfig("i2c-address", "I2C device address", "I2CAddress", false, true, int.class)),
        ;

        private final NVConfig nvc;

        Param(NVConfig nvc) {
            this.nvc = nvc;
        }

        public NVConfig getNVConfig() {
            return nvc;
        }
    }

    public static final NVConfigEntity NVC_I2C_MESSAGE = new NVConfigEntityPortable(
            "i2c_message",
            "I2C message",
            "I2CMessage",
            true,
            false,
            false,
            false,
            SimpleMessage.class,
            SharedUtil.extractNVConfigs(Param.I2C_BUS, Param.I2C_ADDRESS, PropertyDAO.Param.PROPERTIES),
            null,
            false,
            SimpleMessage.NVC_SIMPLE_MESSAGE
    );


    public I2CMessage() {
        super(NVC_I2C_MESSAGE);
    }

    /**
     * Returns i2c bus id.
     * @return
     */
    public int getBus() {
        return lookupValue(Param.I2C_BUS);
    }

    /**
     * Sets bus id
     * @param bus
     */
    public void setBus(int bus) {
        setValue(Param.I2C_BUS, bus);
    }


    /**
     * Returns i2c address.
     * @return
     */
    public int getAddress() {
        return lookupValue(Param.I2C_ADDRESS);
    }

    /**
     * Sets i2c address
     * @param bus
     */
    public void setAddress(int bus) {
        setValue(Param.I2C_ADDRESS, bus);
    }
}

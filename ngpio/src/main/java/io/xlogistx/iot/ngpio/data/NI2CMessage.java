package io.xlogistx.iot.ngpio.data;


import org.zoxweb.shared.data.PropertyDAO;
import org.zoxweb.shared.data.SimpleMessage;

import org.zoxweb.shared.util.*;

public class NI2CMessage extends SimpleMessage {
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

    public static final NVConfigEntity NVC_NI2C_MESSAGE = new NVConfigEntityPortable(
            "ni2c_message",
            "NI2C message",
            "NI2CMessage",
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


    public NI2CMessage() {
        super(NVC_NI2C_MESSAGE);
    }

    /**
     * Returns i2c bus id.
     * @return bus id
     */
    public int getBus() {
        return lookupValue(Param.I2C_BUS);
    }

    /**
     * Sets bus id
     * @param bus the bus id
     */
    public void setBus(int bus) {
        setValue(Param.I2C_BUS, bus);
    }


    /**
     * Returns i2c address.
     * @return address
     */
    public int getAddress() {
        return lookupValue(Param.I2C_ADDRESS);
    }

    /**
     * Sets i2c address
     * @param address the i2c address
     */
    public void setAddress(int address) {
        setValue(Param.I2C_ADDRESS, address);
    }
}

package io.xlogistx.iot.device.shared;

import org.zoxweb.shared.data.PropertyDAO;
import org.zoxweb.shared.util.*;

public class Port extends PropertyDAO {
    public enum Param
            implements GetNVConfig {
        ID(NVConfigManager.createNVConfig("id", "ID", "id", false, true, String.class)),
        PIN(NVConfigManager.createNVConfig("pin", "Pin on the chip", "Pin", false, true, String.class)),
        PIN_TYPE(NVConfigManager.createNVConfig("type", "Pin Type", "PinType", false, true, IOTConst.PinType.class)),
        PIN_STATE(NVConfigManager.createNVConfig("state", "Pin State", "PinState", false, true, IOTConst.PinState.class)),
        SENSOR(NVConfigManager.createNVConfig("sensor", "SensorType", "SensorType", false, true, IOTConst.SensorType.class)),
        ;
        private final NVConfig nvc;

        Param(NVConfig nvc) {
            this.nvc = nvc;
        }

        public NVConfig getNVConfig() {
            return nvc;
        }
    }

    public static final NVConfigEntity NVC_PORT = new NVConfigEntityPortable("port",
            null,
            "Port",
            true,
            false,
            false,
            false,
            Port.class,
            SharedUtil.extractNVConfigs(Param.values()),
            null,
            false,
            PropertyDAO.NVC_PROPERTY_DAO);

    public Port() {
        super(NVC_PORT);
    }

    public Port(String id, IOTConst.PinState state, IOTConst.PinType type) {
        this();
        setID(id);
        setPinState(state);
        setPinType(type);
    }


    public IOTConst.PinType getPinType() {
        return lookupValue(Param.PIN_TYPE);
    }

    public void setPinType(IOTConst.PinType portType) {
        setValue(Param.PIN_TYPE, portType);
    }

    public IOTConst.PinState getPinState() {
        return lookupValue(Param.PIN_STATE);
    }

    public void setPinState(IOTConst.PinState portState) {
        setValue(Param.PIN_STATE, portState);
    }

    public String getID() {
        return lookupValue(Param.ID);
    }

    public void setName(String ID) {
        setID(ID);
    }

    public void setID(String ID) {
        setValue(Param.ID, ID);
    }

    public String getName() {
        return getID();
    }


    public IOTConst.SensorType getSensorType() {
        return lookupValue(Param.SENSOR);
    }

    public void setSensorType(IOTConst.SensorType sensorType) {
        setValue(Param.SENSOR, sensorType);
    }

    public String getPin() {
        return lookupValue(Param.PIN);
    }

    public void setPin(String pin) {
        setValue(Param.PIN, pin);
    }


}

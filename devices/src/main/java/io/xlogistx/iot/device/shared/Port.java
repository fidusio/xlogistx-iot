package io.xlogistx.iot.device.shared;

import org.zoxweb.shared.data.PropertyDAO;
import org.zoxweb.shared.util.*;

public class Port extends PropertyDAO
{
    public enum Param
            implements GetNVConfig
    {
        ID(NVConfigManager.createNVConfig("id", "ID", "id", false, true, String.class)),
        PORT_TYPE(NVConfigManager.createNVConfig("type", "Port Type", "PortType", false, true, IOTConst.PortType.class)),
        PORT_STATE(NVConfigManager.createNVConfig("state", "Port State", "PortState", false, true, IOTConst.PortState.class)),
        SENSOR(NVConfigManager.createNVConfig("sensor", "SensorType", "SensorType", false, true, IOTConst.SensorType.class)),
        ;
        private final NVConfig nvc;

        Param(NVConfig nvc)
        {
            this.nvc = nvc;
        }

        public NVConfig getNVConfig()
        {
            return nvc;
        }
    }

    public static final NVConfigEntity NVC_PORT = new NVConfigEntityLocal("port",
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

    public Port()
    {
        super(NVC_PORT);
    }

    public Port(String id, IOTConst.PortState state, IOTConst.PortType type)
    {
        this();
        setID(id);
        setPortState(state);
        setPortType(type);
    }


    public IOTConst.PortType getPortType()
    {
        return lookupValue(Param.PORT_TYPE);
    }

    public void setPortType(IOTConst.PortType portType)
    {
        setValue(Param.PORT_TYPE, portType);
    }

    public IOTConst.PortState getPortState()
    {
        return lookupValue(Param.PORT_STATE);
    }

    public void setPortState(IOTConst.PortState portState)
    {
        setValue(Param.PORT_STATE, portState);
    }

    public String getID()
    {
        return lookupValue(Param.ID);
    }

    public void setName(String ID)
    {
        setID(ID);
    }
    public void setID(String ID)
    {
        setValue(Param.ID, ID);
    }

    public String getName()
    {
        return getID();
    }



    public IOTConst.SensorType getSensorType()
    {
        return lookupValue(Param.SENSOR);
    }

    public void setSensorType(IOTConst.SensorType sensorType)
    {
        setValue(Param.SENSOR, sensorType);
    }


}

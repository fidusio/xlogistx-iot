package io.xlogistx.iot.device.shared;


import org.zoxweb.shared.data.PropertyDAO;
import org.zoxweb.shared.util.*;


public class IOTDeviceInfo
    extends PropertyDAO

{
    public enum Param
            implements GetNVConfig
    {
        DEVICE_ID(NVConfigManager.createNVConfig("id", "Device ID", "DeviceID", false, true, String.class)),
        DEVICE_TYPE(NVConfigManager.createNVConfig("device_type", "Device Type", "DeviceType", false, true, IOTConst.DeviceType.class)),
        CPU_ID(NVConfigManager.createNVConfig("cpu_id", "CPU ID", "CPUID", false, true, String.class)),
        CPU_SPEED(NVConfigManager.createNVConfig("cpu_speed", "Protocol speed", "Speed", false, true, double.class)),
        FREQ_UNIT(NVConfigManager.createNVConfig("freq_unit", "Frequency unit", "Frequency", false, true, IOTConst.FrequencyUnit.class)),
        COMM(NVConfigManager.createNVConfigEntity("comm", "Comm", "COMM",false, true, COMM.class, NVConfigEntity.ArrayType.NOT_ARRAY)),
        PORTS(NVConfigManager.createNVConfigEntity("ports", "Ports", "Ports",false, true, Port.class, NVConfigEntity.ArrayType.GET_NAME_MAP)),
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



    /**
     * This NVConfigEntity type constant is set to an instantiation of a NVConfigEntityLocal object based on DataContentDAO.
     */
    public static final NVConfigEntity NVC_IOT_DEVICE_INFO= new NVConfigEntityLocal("iot_device_info",
            null,
            "IOTDeviceInfo",
            true,
            false,
            false,
            false,
            IOTDeviceInfo.class,
            SharedUtil.extractNVConfigs(Param.values()),
            null,
            false,
            PropertyDAO.NVC_PROPERTY_DAO);

    public IOTDeviceInfo()
    {
        super(NVC_IOT_DEVICE_INFO);
    }



    public IOTConst.DeviceType getDeviceType() {
        return lookupValue(Param.DEVICE_TYPE);
    }


    public String getDeviceID() {
        return lookupValue(Param.DEVICE_ID);
    }
    public void setDeviceID(String id) {
        setValue(Param.DEVICE_ID, id);
    }


    public void setCPUID(String id) {
        setValue(Param.CPU_ID, id);
    }

    public String getCPUID() {
        return lookupValue(Param.CPU_ID);
    }



    public double getCPUSpeed()
    {
        return lookupValue(Param.CPU_SPEED);
    }

    public void setCPUSpeed(double speed)
    {
        setValue(Param.CPU_SPEED, speed);
    }

    public IOTConst.FrequencyUnit getFrequencyUnit()
    {
        return lookupValue(Param.FREQ_UNIT);
    }

    public void setFrequencyUnit(IOTConst.FrequencyUnit freqUnit)
    {
        setValue(Param.FREQ_UNIT, freqUnit);
    }

    public COMM getCOMM()
    {
        return lookupValue(Param.COMM);
    }

    public void setCOMM(COMM comm)
    {
        setValue(Param.COMM, comm);
    }

    public void setPorts(Port ...ports)
    {
        ((ArrayValues) lookup(Param.PORTS)).add(ports, true);
    }

    public Port[] getPorts()
    {
        return ((ArrayValues<NVEntity>) lookup(Param.PORTS)).valuesAs( new Port[0]);
    }



}

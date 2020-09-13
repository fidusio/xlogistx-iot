package io.xlogistx.iot.device.shared;


import org.zoxweb.shared.data.PropertyDAO;
import org.zoxweb.shared.util.*;


public class IOTDeviceConfig
    extends PropertyDAO
    implements  DeviceConfigInfo
{
    public enum Param
            implements GetNVConfig
    {
        DEVICE_ID(NVConfigManager.createNVConfig("device_id", "Device ID", "DeviceID", false, true, String.class)),
        DEVICE_TYPE(NVConfigManager.createNVConfig("device_type", "Device Type", "DeviceType", false, true, IOTDeviceType.class)),
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
    public static final NVConfigEntity NVC_IOT_DEVICE_CONFIG= new NVConfigEntityLocal("iot_device_config",
            null,
            "IOTDeviceConfig",
            true,
            false,
            false,
            false,
            IOTDeviceConfig.class,
            SharedUtil.extractNVConfigs(Param.values()),
            null,
            false,
            PropertyDAO.NVC_PROPERTY_DAO);

    public IOTDeviceConfig()
    {
        super(NVC_IOT_DEVICE_CONFIG);
    }


    @Override
    public IOTDeviceType getDeviceType() {
        return lookupValue(Param.DEVICE_TYPE);
    }

    @Override
    public String getDeviceID() {
        return lookupValue(Param.DEVICE_ID);
    }

    @Override
    public void setDeviceID(String id) {
        setValue(Param.DEVICE_ID, id);
    }


}

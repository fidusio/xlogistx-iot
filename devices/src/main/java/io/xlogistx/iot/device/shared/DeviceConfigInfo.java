package io.xlogistx.iot.device.shared;

import org.zoxweb.shared.util.*;

public interface DeviceConfigInfo
    extends SetDescription, SetName, GetNVProperties, DeviceID<String>
{
  IOTDeviceType getDeviceType();
  NVGenericMap getConfig();
}


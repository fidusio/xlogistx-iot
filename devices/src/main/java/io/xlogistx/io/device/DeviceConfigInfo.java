package io.xlogistx.io.device;

import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.SetDescription;
import org.zoxweb.shared.util.SetName;

public interface DeviceConfigInfo
    extends SetDescription, SetName
{

  public NVGenericMap getConfig();

  public NVGenericMap getCapabilities();

}


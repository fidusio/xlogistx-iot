package io.xlogistx.iot.gpio.data;

import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.util.*;



public class I2CVersion extends I2CCodec {
    public static final I2CVersion SINGLETON = new I2CVersion();
    private I2CVersion()
    {
        super("version", "Read the version model of the I2C device, return as java String, usage: VERSION");
    }

    @Override
    public SimpleMessage decode(I2CResp i2cResp)
    {
        SimpleMessage ret = createDecoderResponse(i2cResp.bus, i2cResp.address);
        int length = i2cResp.data[0];
        String version = SharedStringUtil.toString(i2cResp.data, 1, length);

        ret.getProperties().add("version", version);
        return ret;
    }
}

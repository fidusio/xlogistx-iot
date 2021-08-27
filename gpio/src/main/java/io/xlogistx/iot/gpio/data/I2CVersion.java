package io.xlogistx.iot.gpio.data;

import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.util.*;



public class I2CVersion extends I2CMessageCodec {
    public static final I2CVersion SINGLETON = new I2CVersion();
    private I2CVersion()
    {
        super("version", "Read the version model of the I2C device, return as java String, usage: VERSION");
    }

    @Override
    public SimpleMessage decode(byte[] input)
    {
        SimpleMessage ret = createDecoderResponse();
        int length = input[0];
        String version = SharedStringUtil.toString(input, 1, length);

        ret.getProperties().add("version", version);
        return ret;
    }
}

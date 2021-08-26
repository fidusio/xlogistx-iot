package io.xlogistx.iot.gpio.data;

import org.zoxweb.server.util.DateUtil;
import org.zoxweb.shared.util.*;

import java.util.Date;

public class I2CVersion extends I2CMessageCodec {
    public static final I2CVersion SINGLETON = new I2CVersion();
    private I2CVersion()
    {
        super("version", "Read the version model of the I2C device");
    }

    @Override
    public NVGenericMap decode(byte[] input)
    {
        NVGenericMap ret = new NVGenericMap();
        int length = input[0];
        String version = SharedStringUtil.toString(input, 1, length);
        System.out.println("version:" + version);
        ret.add(Token.TIMESTAMP, DateUtil.DEFAULT_GMT_MILLIS.format(new Date()));
        ret.add("version", version);
        return ret;
    }
}

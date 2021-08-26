package io.xlogistx.iot.gpio.data;

import org.zoxweb.server.util.DateUtil;
import org.zoxweb.shared.util.*;

import java.util.Date;

public class I2CUptime extends I2CMessageCodec {
    public static final I2CUptime SINGLETON = new I2CUptime();

    private I2CUptime() {
        super("uptime", "read the i2c device uptime in millis");
    }

    @Override
    public NVGenericMap decode(byte[] input) {
        NVGenericMap ret = new NVGenericMap();
        int offset = 0;
        ret.add(new NVPair(Token.TIMESTAMP, DateUtil.DEFAULT_GMT_MILLIS.format(new Date())));
        ret.add(new NVInt(Token.STATUS, BytesValue.SHORT.toValue(input, offset)));
        long millis = BytesValue.INT.toValue(input, 3);
        ret.add("uptime", Const.TimeInMillis.toString(millis));
        return ret;
    }
}
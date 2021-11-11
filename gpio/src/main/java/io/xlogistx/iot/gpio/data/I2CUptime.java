package io.xlogistx.iot.gpio.data;


import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.util.*;



public class I2CUptime extends I2CMessageCodec {
    public static final I2CUptime SINGLETON = new I2CUptime();

    private I2CUptime() {
        super("uptime", "read the i2c device uptime in millis, value java int, usage: UPTIME");
    }

    @Override
    public SimpleMessage decode(byte[] input) {
        SimpleMessage ret = createDecoderResponse();
        ret.setStatus(BytesValue.SHORT.toValue(input, 0));
        long millis = BytesValue.INT.toValue(input, 3);
        ret.getProperties().add("uptime", Const.TimeInMillis.toString(millis));
        return ret;
    }
}
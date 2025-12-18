package io.xlogistx.iot.data.i2c;


import io.xlogistx.iot.data.i2c.I2CResp;
import io.xlogistx.iot.data.i2c.I2CCodec;
import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.util.*;


public class I2CUptime extends I2CCodec {
    public static final I2CUptime SINGLETON = new I2CUptime();

    private I2CUptime() {
        super("uptime", "read the i2c device uptime in millis, value java int, usage: UPTIME");
    }

    @Override
    public SimpleMessage decode(I2CResp input) {
        SimpleMessage ret = createDecoderResponse(input.bus, input.address);
        ret.setStatus(BytesValue.SHORT.toValue(input.data, 0));
        long millis = BytesValue.INT.toValue(input.data, 3);
        ret.getProperties().add("uptime", Const.TimeInMillis.toString(millis));
        ret.getProperties().add(new NVLong("millis", millis));
        return ret;
    }
}
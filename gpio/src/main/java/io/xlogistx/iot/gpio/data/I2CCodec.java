package io.xlogistx.iot.gpio.data;


import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.*;


public class I2CCodec extends I2CCodecBase {


    public I2CCodec(String name, String description) {
        super(TokenFilter.UPPER_COLON.validate(name), description);
    }


    @Override
    public SimpleMessage decode(I2CResp i2cResp) {
        SimpleMessage ret = createDecoderResponse(i2cResp.bus, i2cResp.address);
        ret.setStatus(BytesValue.SHORT.toValue(i2cResp.data, 0));
        ret.getProperties().add(new NVInt(Token.RESULT, BytesValue.INT.toValue(i2cResp.data, i2cResp.data[2] == ':' ? 3 : 2)));
        return ret;
    }

    @Override
    public CommandToBytes encode(String input) {
        // input is ignored
        return new CommandToBytes(16, ':').command(TokenFilter.UPPER_COLON.validate(input));
    }

}

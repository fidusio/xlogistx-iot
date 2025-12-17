package io.xlogistx.iot.ngpio.data;


import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.*;


public class NI2CCodec extends NI2CCodecBase {


    public NI2CCodec(String name, String description) {
        super(TokenFilter.UPPER_COLON.validate(name), description);
    }


    @Override
    public SimpleMessage decode(NI2CResp i2cResp) {
        SimpleMessage ret = createDecoderResponse(i2cResp.bus, i2cResp.address);
        ret.setStatus(BytesValue.SHORT.toValue(i2cResp.data, 0));
        ret.getProperties().add(new NVInt(Token.RESULT, BytesValue.INT.toValue(i2cResp.data, i2cResp.data[2] == ':' ? 3 : 2)));
        return ret;
    }

    @Override
    public NCommandToBytes encode(String input) {
        // input is ignored
        return new NCommandToBytes(16, ':').command(TokenFilter.UPPER_COLON.validate(input));
    }

}

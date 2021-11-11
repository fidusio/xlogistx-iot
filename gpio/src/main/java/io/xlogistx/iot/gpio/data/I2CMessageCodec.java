package io.xlogistx.iot.gpio.data;


import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.*;




public class I2CMessageCodec extends I2CMessageBase
{


    public I2CMessageCodec(String name, String description)
    {
        super(TokenFilter.UPPER_COLON.validate(name), description);
    }


    @Override
    public SimpleMessage decode(byte[] input)
    {
        SimpleMessage ret = createDecoderResponse();
        ret.setStatus(BytesValue.SHORT.toValue(input, 0));
        ret.getProperties().add(new NVInt(Token.RESULT, BytesValue.INT.toValue(input, input[2] == ':' ?  3 : 2) ));
        return ret;
    }

    @Override
    public CommandToBytes encode(String input) {
        // input is ignored
        return new CommandToBytes(16, ':').command(TokenFilter.UPPER_COLON.validate(input));
    }

}

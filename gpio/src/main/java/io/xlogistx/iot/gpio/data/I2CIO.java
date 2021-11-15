package io.xlogistx.iot.gpio.data;

import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.BytesValue;
import org.zoxweb.shared.util.NVInt;
import org.zoxweb.shared.util.SharedStringUtil;
import org.zoxweb.shared.util.SharedUtil;

public class I2CIO
        extends I2CMessageCodec {

    public I2CIO() {
        super("IO", "Control the GPIO on the device usage: IO:[G,S,U,D]:[A,D,P]:pin:value");
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
        String[] tokens = SharedStringUtil.parseString(input, ":", true);
        int index = 0;
        CommandToBytes ret = new CommandToBytes(16, ':').command(TokenFilter.UPPER_COLON.validate(tokens[index++]));
        String action = tokens[index++];
        String pinType = tokens[index++];
        ret.toBytes(action).toBytes(pinType);
        int  pin = SharedUtil.parseInt(tokens[index++]);
        ret.toBytes((byte)pin);
        if(index < tokens.length)
        {
            int value = SharedUtil.parseInt(tokens[index++]);
            ret.toBytes((short)value);
        }


        return ret;
    }
}

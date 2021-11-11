package io.xlogistx.iot.gpio.data;


import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.*;



public class I2CEcho extends I2CMessageCodec {
    public static final I2CEcho SINGLETON = new I2CEcho();
    private I2CEcho()
    {
        super("ECHO", "Echo the value back, usage: ECHO:S:[I,L]:[short or int value], return:[status]:[I,L]:value");
    }

    @Override
    public SimpleMessage decode(byte[] input)
    {
        // OK:I,L:VALUE
        SimpleMessage ret = createDecoderResponse();
        int offset = 0;

        ret.setStatus(BytesValue.SHORT.toValue(input, offset));
        offset += 3;

        switch(input[offset++])
        {
            case 'I':
                ret.getProperties().add(new NVInt("short", BytesValue.SHORT.toValue(input, ++offset)));
                break;
            case 'L':
                ret.getProperties().add(new NVInt("int", BytesValue.INT.toValue(input, ++offset)));
                break;
        }

        return ret;
    }

    @Override
    public CommandToBytes encode(String input) {
        String[] tokens = input.split(":");
        int index = 0;

        CommandToBytes ret = new CommandToBytes(16, ':').command(TokenFilter.UPPER_COLON.validate(tokens[index++])).toBytes(TokenFilter.UPPER_COLON.validate(tokens[index++]));


        switch(TokenFilter.UPPER_COLON.validate(tokens[index++]))
        {
            case "I":
                ret.toBytes((byte)'I');
                ret.toBytes(SharedUtil.parseShort(tokens[index++]));
                break;
            case "L":
                ret.toBytes((byte)'L');
                ret.toBytes(SharedUtil.parseInt(tokens[index++]));
                break;
            default:
                throw new IllegalArgumentException("Invalid input" + input);

        }

        return ret;
    }
}

package io.xlogistx.iot.data.i2c;


import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.*;



public class I2CEcho extends I2CCodec {
    public static final I2CEcho SINGLETON = new I2CEcho();
    private I2CEcho()
    {
        super("ECHO", "Echo the value back, usage: ECHO:S:[I,L]:[short or int value], return:[status]:[I,L]:value");
    }

    @Override
    public SimpleMessage decode(I2CResp input)
    {
        // OK:I,L:VALUE
        SimpleMessage ret = createDecoderResponse(input.bus, input.address);
        int offset = 0;

        ret.setStatus(BytesValue.SHORT.toValue(input.data, offset));
        offset += 3;

        switch(input.data[offset++])
        {
            case 'I':
                ret.getProperties().add(new NVInt("short", BytesValue.SHORT.toValue(input.data, ++offset)));
                break;
            case 'L':
                ret.getProperties().add(new NVInt("int", BytesValue.INT.toValue(input.data, ++offset)));
                break;
        }

        return ret;
    }

    @Override
    public io.xlogistx.iot.data.CommandToBytes encode(String input) {
        String[] tokens = input.split(":");
        int index = 0;

        io.xlogistx.iot.data.CommandToBytes ret = new io.xlogistx.iot.data.CommandToBytes(16, ':').command(TokenFilter.UPPER_COLON.validate(tokens[index++])).toBytes(TokenFilter.UPPER_COLON.validate(tokens[index++]));


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

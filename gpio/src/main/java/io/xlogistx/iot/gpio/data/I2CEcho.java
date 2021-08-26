package io.xlogistx.iot.gpio.data;

import org.zoxweb.server.util.DateUtil;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.*;

import java.util.Date;

public class I2CEcho extends I2CMessageCodec {
    public static final I2CEcho SINGLETON = new I2CEcho();
    private I2CEcho()
    {
        super("ECHO", "Echo the message back");
    }

    @Override
    public NVGenericMap decode(byte[] input)
    {
        // OK:I,L:VALUE
        NVGenericMap ret = new NVGenericMap();
        int offset = 0;
        ret.add(new NVPair(Token.TIMESTAMP, DateUtil.DEFAULT_GMT_MILLIS.format(new Date())));
        ret.add(new NVInt(Token.STATUS, BytesValue.SHORT.toValue(input, offset)));
        offset += 3;

        switch(input[offset++])
        {
            case 'I':
                ret.add(new NVInt("short", BytesValue.SHORT.toValue(input, ++offset)));
                break;
            case 'L':
                ret.add(new NVInt("int", BytesValue.INT.toValue(input, ++offset)));
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

package io.xlogistx.iot.gpio.data;

import io.xlogistx.common.data.MessageCodec;
import org.zoxweb.server.util.DateUtil;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.*;

import java.util.Date;


public class I2CMessageCodec extends MessageCodec<String, CommandToBytes, byte[]>
{
    public enum Token
        implements GetName
    {
        STATUS("status"),
        RESULT("result"),
        TIMESTAMP("timestamp"),
        ;

        private final String name;
        Token(String name)
        {
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
        }
    }

    public I2CMessageCodec(String name, String description)
    {
        super(TokenFilter.UPPER_COLON.validate(name), description);
    }


    @Override
    public NVGenericMap decode(byte[] input)
    {
        NVGenericMap ret = new NVGenericMap();
        int offset = 0;
        ret.add(new NVPair(Token.TIMESTAMP, DateUtil.DEFAULT_GMT_MILLIS.format(new Date())));
        ret.add(new NVInt(Token.STATUS, BytesValue.SHORT.toValue(input, offset)));
        ret.add(new NVInt(Token.RESULT, BytesValue.INT.toValue(input, input[offset+2] == ':' ? offset + 3 : offset+2) ));
        return ret;
    }

    @Override
    public CommandToBytes encode(String input) {
        // input is ignored
        return new CommandToBytes(16, ':').command(TokenFilter.UPPER_COLON.validate(input));
    }
}

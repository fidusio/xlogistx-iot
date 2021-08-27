package io.xlogistx.iot.gpio.data;

import io.xlogistx.common.data.MessageCodec;
import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;

import org.zoxweb.shared.util.GetName;



public abstract  class I2CMessageBase extends MessageCodec<String, CommandToBytes, byte[], SimpleMessage>
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

    protected I2CMessageBase(String name, String description)
    {
        super(TokenFilter.UPPER_COLON.validate(name), description);
    }




    protected SimpleMessage createDecoderResponse()
    {
        SimpleMessage ret = new SimpleMessage();
        ret.setName(getName());
        ret.setCreationTime(System.currentTimeMillis());
        return ret;
    }
}

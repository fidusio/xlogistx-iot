package io.xlogistx.iot.gpio.data;

import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.*;


public abstract  class I2CCodecBase extends NamedDescription
        //MessageCodec<String, CommandToBytes, I2CResp, SimpleMessage>
    implements DataEncoder<String, CommandToBytes>, DataDecoder<I2CResp, SimpleMessage>
{

    private long timeStamp = System.nanoTime();
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

    public int responseLength()
    {
        return 16;
    }

    public synchronized void resetTimeStamp()
    {
        timeStamp = System.nanoTime();
    }

    /**
     * Return the delta in nanos second NOT millis
     * @return the time difference in nanos
     */
    public synchronized long delta()
    {
        return System.nanoTime() - timeStamp;
    }


    protected I2CCodecBase(String name, String description)
    {
        super(TokenFilter.UPPER_COLON.validate(name), description);
    }


    protected SimpleMessage createDecoderResponse(int bus, int address)
    {
        long delta = delta();
        I2CMessage ret = new I2CMessage();
        ret.setName(getName());
        ret.setBus(bus);
        ret.setAddress(address);
        ret.getProperties().add("exec_time", Const.TimeInMillis.nanosToString(delta));
        return ret;
    }
}

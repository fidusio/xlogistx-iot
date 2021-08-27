package io.xlogistx.iot.gpio.data;


import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.*;



public class I2C extends I2CMessageCodec {
    public static final I2C SINGLETON = new I2C();
    private I2C()
    {
        super("I2C", "Set the device I2C address, usage: I2C:S:[7-127], return: [status]:old-address:set-address");
    }

    @Override
    public SimpleMessage decode(byte[] input)
    {
        // OK:I,L:VALUE
        SimpleMessage ret = createDecoderResponse();
        int offset = 0;

        ret.setStatus(BytesValue.SHORT.toValue(input, offset));
        offset += 3;
        int oldAddress = input[offset++];
        int newAddress = input[++offset];
        ret.getProperties().add(new NVInt("old-i2c-address", oldAddress));
        ret.getProperties().add(new NVInt("new-i2c-address", newAddress));
        return ret;
    }

    /**
     * command: I2C:S:address byte
     * @param input
     * @return
     */
    @Override
    public CommandToBytes encode(String input) {
        String[] tokens = input.split(":");
        int index = 0;

        CommandToBytes ret = new CommandToBytes(16, ':').command(TokenFilter.UPPER_COLON.validate(tokens[index++])).toBytes(TokenFilter.UPPER_COLON.validate(tokens[index++]));
        int address = SharedUtil.parseInt(tokens[index++]);
        ret.toBytes((byte)address);

        return ret;
    }
}

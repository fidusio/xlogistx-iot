package io.xlogistx.iot.gpio.data;


import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.*;


public class I2CAddress extends I2CCodec {
    public static final I2CAddress SINGLETON = new I2CAddress();

    private I2CAddress() {
        super("I2C-ADDRESS", "Set the device I2C address, usage: I2C-ADDRESS:S:[7-127], return: [status]:old-address:set-address");
    }

    @Override
    public SimpleMessage decode(I2CResp i2cResp) {
        // OK:I,L:VALUE
        SimpleMessage ret = createDecoderResponse(i2cResp.bus, i2cResp.address);
        int offset = 0;

        ret.setStatus(BytesValue.SHORT.toValue(i2cResp.data, offset));
        offset += 3;
        int oldAddress = i2cResp.data[offset++];
        int newAddress = i2cResp.data[++offset];
        ret.getProperties().add(new NVInt("old-i2c-address", oldAddress));
        ret.getProperties().add(new NVInt("new-i2c-address", newAddress));
        return ret;
    }

    /**
     * command: I2C-ADDRESS:S:address byte
     * @param input message to be converted
     * @return the command to byte object
     */
    @Override
    public CommandToBytes encode(String input) {
        String[] tokens = input.split(":");
        int index = 0;

        CommandToBytes ret = new CommandToBytes(16, ':').command(TokenFilter.UPPER_COLON.validate(tokens[index++])).toBytes(TokenFilter.UPPER_COLON.validate(tokens[index++]));
        int address = SharedUtil.parseInt(tokens[index++]);
        ret.toBytes((byte) address);

        return ret;
    }
}

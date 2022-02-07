package io.xlogistx.iot.gpio.data;

import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.*;

public class I2CIO
        extends I2CCodec {

    public I2CIO() {
        super("IO", "Control the GPIO on the device usage: IO:[G,S,U,D]:[A,D,P]:pin:value");
    }

    private String lastAction;



    @Override
    public synchronized SimpleMessage decode(I2CResp input)
    {
        SimpleMessage ret = createDecoderResponse(input.bus, input.address);
        ret.setStatus(BytesValue.SHORT.toValue(input.data, 0));
        int index = 2;


            index = input.data[index] == ':' ? ++index : index;
            String portType = SharedStringUtil.toString(input.data, index, 1);
            GPIOConst.PortType pt = GPIOConst.PortType.lookup(portType);
            //System.out.println("pt: " + portType + " " + SharedStringUtil.bytesToHex(input));
            ret.getProperties().add(new NVEnum("port_type", pt));
            index++;

            index = input.data[index] == ':' ? ++index : index;
            ret.getProperties().add(new NVInt("pin", BytesValue.INT.toValue(input.data, index, 1)));
            index++;
            // int value
            index = input.data[index] == ':' ? ++index : index;
            int result = BytesValue.INT.toValue(input.data, index);
            if (pt == GPIOConst.PortType.DIGITAL)
                ret.getProperties().add(new NVBoolean(Token.RESULT, result != 0));
            else
                ret.getProperties().add(new NVInt(Token.RESULT, result));

            index += 2;

        return ret;
    }

    @Override
    public synchronized  CommandToBytes encode(String input) {
        // input is ignored
        String[] tokens = SharedStringUtil.parseString(input, ":", true);
        int index = 0;
        CommandToBytes ret = new CommandToBytes(16, ':').command(TokenFilter.UPPER_COLON.validate(tokens[index++]));
        lastAction = tokens[index++];
        String pinType = tokens[index++];
        ret.toBytes(lastAction).toBytes(pinType);

        int  pin = SharedUtil.parseInt(tokens[index++]);
        ret.toBytes((byte)pin);
        if(index < tokens.length)
        {
            int value = SharedUtil.parseInt(tokens[index++]);
            ret.toBytes((short) value);
        }

        return ret;
    }
}

package io.xlogistx.iot.gpio.data;

import io.xlogistx.iot.gpio.MultiplierDataFilter;
import io.xlogistx.iot.gpio.i2c.I2CUtil;
import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.*;

public class I2CIO
        extends I2CCodec {
    public enum PinMode
        implements GetName
    {
        INPUT("I"),
        OUTPUT("O"),
        INPUT_PULL_UP("P");
        ;

        private final String name;
        PinMode(String name)
        {
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
        }
    }

    public enum IOAction
    {
        G,
        S,
        D,
        U,
        P
    }

    public I2CIO() {
        super("IO", "Control the GPIO on the device usage: IO:[G,S,U,D]:[A,D]:pin:value or IO:P:pin:[I,O,P]");
    }





    @Override
    public synchronized SimpleMessage decode(I2CResp input)
    {
        SimpleMessage ret = createDecoderResponse(input.bus, input.address);
        ret.setStatus(BytesValue.SHORT.toValue(input.data, 0));
        int index = 2;


        index = input.data[index] == ':' ? ++index : index;
        String portType = SharedStringUtil.toString(input.data, index, 1);
        GPIOUtil.PortType pt = GPIOUtil.PortType.lookup(portType);
        //System.out.println(ret.getStatus() +" pt: " + portType + " " + SharedStringUtil.bytesToHex(input.data));
        ret.getProperties().add(new NVEnum("port_type", pt));
        index++;

        index = input.data[index] == ':' ? ++index : index;
        ret.getProperties().add(new NVInt("pin", BytesValue.INT.toValue(input.data, index, 1)));
        index++;
        // int value
        index = input.data[index] == ':' ? ++index : index;
        int result;
        switch(pt)
        {

            case ANALOG:
                result = BytesValue.INT.toValue(input.data, index);
                MultiplierDataFilter df = I2CUtil.DATA_FILTER.lookup(input.command);
                if(df != null)
                    ret.getProperties().add(df.decode(result));
                else
                    ret.getProperties().add(new NVInt(Token.RESULT, result));

                break;
            case DIGITAL:
                result = BytesValue.INT.toValue(input.data, index);
                ret.getProperties().add(new NVBoolean(Token.RESULT, result != 0));
                break;
            case PROVISIONING:
                result = BytesValue.INT.toValue(input.data, index);
                ret.getProperties().add(new NVEnum(Token.RESULT, SharedUtil.lookupEnum(result, PinMode.values())));
                break;
        }
//        int result = BytesValue.INT.toValue(input.data, index);
//        if (pt == GPIOConst.PortType.DIGITAL)
//            ret.getProperties().add(new NVBoolean(Token.RESULT, result != 0));
//        else
//            ret.getProperties().add(new NVInt(Token.RESULT, result));

        //index += 2;

        return ret;
    }



//    @Override
//    public synchronized  CommandToBytes encode(String input) {
//        // input is ignored
//        String[] tokens = SharedStringUtil.parseString(input, ":", true);
//        int index = 0;
//        CommandToBytes ret = new CommandToBytes(16, ':').command(TokenFilter.UPPER_COLON.validate(tokens[index++]));
//        action = tokens[index++];
//        String pinType = tokens[index++];
//        ret.toBytes(action).toBytes(pinType);
//
//        int  pin = SharedUtil.parseInt(tokens[index++]);
//        ret.toBytes((byte)pin);
//        if(index < tokens.length)
//        {
//            if (action.equals("P"))
//            {
//                String provisioning = tokens[index++];
//                switch (provisioning)
//                {
//                    case "P":
//                    case "I":
//                    case "O":
//                        ret.toBytes(provisioning);
//                        break;
//                    default:
//                        throw new IllegalArgumentException("Invalid Provisioning value [P,I,O]:" + provisioning);
//                }
//            }
//            else
//            {
//                int value = SharedUtil.parseInt(tokens[index++]);
//                ret.toBytes((short) value);
//            }
//        }
//
//        return ret;
//    }

    @Override
    public synchronized  CommandToBytes encode(String input) {
        // input is ignored
        String[] tokens = SharedStringUtil.parseString(input, ":", true);
        int index = 0;
        CommandToBytes ret = new CommandToBytes(16, ':').command(TokenFilter.UPPER_COLON.validate(tokens[index++]));
        IOAction action = SharedUtil.lookupEnum(tokens[index++], IOAction.values());
        ret.toBytes(action.name());

        switch(action)
        {

            case G:
            case S:
            case U:
                String pinType = tokens[index++];
                ret.toBytes(pinType);
                int pin = SharedUtil.parseInt(tokens[index++]);
                ret.toBytes((byte)pin);
                if(index < tokens.length)
                {
                    int value = SharedUtil.parseInt(tokens[index++]);
                    ret.toBytes((short) value);
                }
                break;
            case P:
                pin = SharedUtil.parseInt(tokens[index++]);
                ret.toBytes((byte)pin);
                PinMode pinMode = SharedUtil.lookupEnum(tokens[index], PinMode.values());

                if(pinMode != null)
                    ret.toBytes(pinMode.getName());
                else
                    throw new IllegalArgumentException("Invalid Provisioning value [P,I,O]: " + tokens[index]);

                break;
            case D:
                throw new IllegalArgumentException("delete no supported");


        }

        return ret;
    }

}

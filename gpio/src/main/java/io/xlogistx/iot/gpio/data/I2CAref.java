package io.xlogistx.iot.gpio.data;

import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.BytesValue;
import org.zoxweb.shared.util.NVEnum;
import org.zoxweb.shared.util.SharedStringUtil;
import org.zoxweb.shared.util.SharedUtil;

public class I2CAref
        extends I2CCodec {
    public enum ArefType {
        DEFAULT,
        EXTERNAL,
        INTERNAL
    }

    public I2CAref() {
        super("AREF", "Get/Set The analog reference : AREF:[G,S]:[D,E,I]");
    }


    @Override
    public synchronized SimpleMessage decode(I2CResp input) {
        SimpleMessage ret = createDecoderResponse(input.bus, input.address);
        ret.setStatus(BytesValue.SHORT.toValue(input.data, 0));
        int index = 2;


        index = input.data[index] == ':' ? ++index : index;
        int arefVal = BytesValue.INT.toValue(input.data, index);
        ret.getProperties().add(new NVEnum("aref", SharedUtil.lookupEnum(arefVal, ArefType.values())));
        index++;

        return ret;
    }


    public synchronized CommandToBytes encode(String input) {
        // input is ignored
        String[] tokens = SharedStringUtil.parseString(input, ":", true);
        int index = 0;
        CommandToBytes ret = new CommandToBytes(16, ':').command(TokenFilter.UPPER_COLON.validate(tokens[index++]));
        for (; index < tokens.length; index++) {
            ret.toBytes(TokenFilter.UPPER_COLON.validate(tokens[index]));
        }

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
}

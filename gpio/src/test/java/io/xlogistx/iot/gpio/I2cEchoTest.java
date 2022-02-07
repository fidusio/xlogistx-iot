package io.xlogistx.iot.gpio;

import io.xlogistx.iot.gpio.data.CommandToBytes;
import io.xlogistx.iot.gpio.data.I2CEcho;
import io.xlogistx.iot.gpio.data.I2CResp;
import org.junit.jupiter.api.Test;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.data.SimpleMessage;


import java.util.Arrays;

public class I2cEchoTest {
    @Test
    public void echoCommand()
    {
        String[] messages={
                "Echo:S:I:4500",
                "Echo:s:l:2000"
        };

        for(String message : messages) {
            CommandToBytes ctb = I2CEcho.SINGLETON.encode(message);
            System.out.println(Arrays.toString(ctb.data()) + " " + new String(ctb.data()));
        }
    }

    @Test
    public void echoResp()
    {
        byte[][] messages={
                {0, (byte)200, ':', 'I', ':', (byte) 127, (byte) 0xFF},

        };

        for(byte[] message : messages) {
            SimpleMessage resp = I2CEcho.SINGLETON.decode(I2CResp.build(1, 10, message));
            System.out.println(GSONUtil.DEFAULT_GSON.toJson(resp));
        }
    }
}

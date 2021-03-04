package iot.xlogistx.iot.device;

import io.xlogistx.iot.device.shared.COMM;
import io.xlogistx.iot.device.shared.IOTConst;
import io.xlogistx.iot.device.shared.IOTDeviceInfo;
import io.xlogistx.iot.device.shared.Port;
import org.junit.jupiter.api.Test;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.util.SharedStringUtil;

import java.io.IOException;

public class DeviceInfoTest {

    @Test
    public void createDeviceInfo() throws IOException {
        IOTDeviceInfo dev = new IOTDeviceInfo();
        dev.setDeviceID("asas");
        dev.setCPUSpeed(8);
        dev.setFrequencyUnit(IOTConst.FrequencyUnit.MHZ);
        dev.setCPUID("attiny84");
        dev.setPorts(new Port("1", IOTConst.PortState.INPUT, IOTConst.PortType.DIGITAL), new Port("2", IOTConst.PortState.OUTPUT, IOTConst.PortType.DIGITAL));
        dev.setCOMM(new COMM(IOTConst.Protocol.I2C, 100, IOTConst.FrequencyUnit.KHZ));
        String json = GSONUtil.toJSON(dev, false, false, false);
        dev = GSONUtil.fromJSON(json,IOTDeviceInfo.class);
        String json1 = GSONUtil.toJSON(dev, false, false, false);
        System.out.println(json);
        System.out.println(json1);
        System.out.println(json.equals(json1) + " " + SharedStringUtil.getBytes(json).length);

    }

    @Test
    public void readDeviceInfo() throws IOException {
        String json = "{\"id\":\"asas\",\"cpu_id\":\"attiny84\",\"cpu_speed\":8.0,\"freq_unit\":\"M\",\"ports\":[{\"id\":\"1\",\"type\":\"D\",\"state\":\"O\"}]}";
        IOTDeviceInfo dev = GSONUtil.fromJSON(json,IOTDeviceInfo.class);
        String json1 = GSONUtil.toJSON(dev, false, false, false);
        System.out.println(json1);
        System.out.println(json.equals(json1) + " " + SharedStringUtil.getBytes(json).length + " " + SharedStringUtil.getBytes(json1).length);
    }




}

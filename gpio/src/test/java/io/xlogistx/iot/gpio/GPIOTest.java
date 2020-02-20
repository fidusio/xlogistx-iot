package io.xlogistx.iot.gpio;

import org.junit.Test;
import org.zoxweb.server.util.GSONUtil;

public class GPIOTest {

    @Test
    public void gpioConfigTest()
    {

        GPIOConfig gpioConfig = new GPIOConfig().monitorSetter(GPIOPin.GPIO_02).nameSetter("WaterContainerLow");
        System.out.println(GSONUtil.DEFAULT_GSON.toJson(gpioConfig));

    }


    @Test
    public void pinStateMonitorTest()
    {
        GPIOConfig gpioConfig = new GPIOConfig().monitorSetter(GPIOPin.GPIO_04).nameSetter("WaterContainerLow").followersSetter(GPIOPin.GPIO_02, GPIOPin.GPIO_03);
        PinStateMonitor psm = new PinStateMonitor().monitorConfigSetter(gpioConfig);
        String json = GSONUtil.DEFAULT_GSON.toJson(psm);
        psm = GSONUtil.DEFAULT_GSON.fromJson(json, PinStateMonitor.class);
        String json1 = GSONUtil.DEFAULT_GSON.toJson(psm);
        assert (json.equals(json1));
        System.out.println(json1);
    }

    @Test
    public void pinStateConfigTest()
    {
        GPIOConfig master = new GPIOConfig().monitorSetter(GPIOPin.GPIO_04).nameSetter("LowWater").masterSetter(true);
        GPIOConfig slave = new GPIOConfig().monitorSetter(GPIOPin.GPIO_05).nameSetter("NespressoHigh").followersSetter(GPIOPin.GPIO_02, GPIOPin.GPIO_03).followersHighDelaySetter("8sec");
        PinStateMonitor psmMaster = new PinStateMonitor().monitorConfigSetter(master);
        PinStateMonitor psmSlave = new PinStateMonitor().monitorConfigSetter(slave);
        PinStateMonitorConfig psc = new PinStateMonitorConfig();
        psc.setMaster(psmMaster);
        psc.setSlaves(psmSlave);
        String json = GSONUtil.DEFAULT_GSON.toJson(psc);
        psc = GSONUtil.DEFAULT_GSON.fromJson(json, PinStateMonitorConfig.class);
        String json1 = GSONUtil.DEFAULT_GSON.toJson(psc);
        assert (json.equals(json1));
        System.out.println(json1);

    }
}

package io.xlogistx.iot.net;

import io.xlogistx.iot.net.data.VLanConfig;
import org.junit.jupiter.api.Test;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.SharedUtil;

public class VLanConfigTest
{
    @Test
    public void all()
    {
        VLanConfig vLanConfig = new VLanConfig(new NVGenericMap());
        vLanConfig.setEtherName("eth1").setVlanID(200).setIPNetmask("192.168.1.1/24");

        System.out.println(SharedUtil.toCanonicalID(',', vLanConfig.getEtherName(),
                vLanConfig.getVlanID(),
                vLanConfig.getIPNetmask(),
                vLanConfig.getIP(),
                vLanConfig.getNetmask(),
                vLanConfig.getVlanName()));
    }

    @Test
    public void json()
    {
        VLanConfig vLanConfig = new VLanConfig(new NVGenericMap());
        vLanConfig.setEtherName("eth1").setVlanID(200).setIPNetmask("10.1.100.1/24");
        String json = GSONUtil.toJSONDefault(vLanConfig.getProperties(), true);
        NVGenericMap nvgm = GSONUtil.fromJSONDefault(json, NVGenericMap.class);
        vLanConfig = new VLanConfig(nvgm);
        System.out.println(SharedUtil.toCanonicalID(',', vLanConfig.getEtherName(),
                vLanConfig.getVlanID(),
                vLanConfig.getIPNetmask(),
                vLanConfig.getIP(),
                vLanConfig.getNetmask(),
                vLanConfig.getVlanName()));

        System.out.println(json);

    }
}

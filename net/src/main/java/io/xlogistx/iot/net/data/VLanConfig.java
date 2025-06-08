package io.xlogistx.iot.net.data;

import io.xlogistx.common.data.PropertyHolder;
import org.zoxweb.shared.util.GetName;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.NVInt;
import org.zoxweb.shared.util.SharedUtil;

public class VLanConfig
        extends PropertyHolder {
    enum Params
            implements GetName {
        ETHER("ether"),
        VLAN_ID("vlan_id"),
        IP_MASK("ip_mask"),

        ;

        private final String name;

        Params(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public VLanConfig(NVGenericMap ngvm) {
        SharedUtil.checkIfNulls("NVGenericMap can't be null", ngvm);
        setProperties(ngvm);
    }


    public String getEtherName() {
        return getProperties().getValue(Params.ETHER);
    }

    public VLanConfig setEtherName(String name) {
        getProperties().add(Params.ETHER, name);
        return this;
    }

    public int getVlanID() {
        return getProperties().getValue(Params.VLAN_ID);
    }

    public VLanConfig setVlanID(int vlanID) {
        getProperties().add(new NVInt(Params.VLAN_ID, vlanID));
        return this;
    }

    public String getVlanName() {
        return getEtherName() + "." + getVlanID();
    }

    public String getIPNetmask() {
        return getProperties().getValue(Params.IP_MASK);
    }

    public VLanConfig setIPNetmask(String ipNetMask) {
        getProperties().add(Params.IP_MASK, ipNetMask);
        return this;
    }

    public String getIP() {
        String ipNetMask = getIPNetmask();
        return ipNetMask != null ? ipNetMask.split("/")[0] : null;
    }

    public String getNetmask() {
        String ipNetMask = getIPNetmask();
        if (ipNetMask != null) {
            String[] parsed = ipNetMask.split("/");
            if (parsed.length == 2)
                return parsed[1];

        }
        return null;
    }


    @Override
    protected void refreshProperties() {

    }
}

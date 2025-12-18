package io.xlogistx.iot.gpio64;


public class PinState64MonitorConfig {
    private PinState64Monitor master;
    private PinState64Monitor[] slaves;

    public PinState64Monitor getMaster() {
        return master;
    }

    public void setMaster(PinState64Monitor master) {
        this.master = master;
    }

    public PinState64Monitor[] getSlaves() {
        return slaves;
    }

    public void setSlaves(PinState64Monitor... slaves) {
        this.slaves = slaves;
    }

}

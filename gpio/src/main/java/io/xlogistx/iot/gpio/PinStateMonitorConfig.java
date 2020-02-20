package io.xlogistx.iot.gpio;


public class PinStateMonitorConfig
{
    private PinStateMonitor master;
    private PinStateMonitor[] slaves;

    public PinStateMonitor getMaster() {
        return master;
    }

    public void setMaster(PinStateMonitor master) {
        this.master = master;
    }

    public PinStateMonitor[] getSlaves() {
        return slaves;
    }

    public void setSlaves(PinStateMonitor... slaves) {
        this.slaves = slaves;
    }

}

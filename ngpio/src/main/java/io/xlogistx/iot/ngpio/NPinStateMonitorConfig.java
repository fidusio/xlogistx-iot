package io.xlogistx.iot.ngpio;


public class NPinStateMonitorConfig {
    private NPinStateMonitor master;
    private NPinStateMonitor[] slaves;

    public NPinStateMonitor getMaster() {
        return master;
    }

    public void setMaster(NPinStateMonitor master) {
        this.master = master;
    }

    public NPinStateMonitor[] getSlaves() {
        return slaves;
    }

    public void setSlaves(NPinStateMonitor... slaves) {
        this.slaves = slaves;
    }

}

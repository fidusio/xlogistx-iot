package io.xlogistx.iot.gpio32;


public class PinStateMonitorConfig {
    private PinStateMonitor master;
    private PinStateMonitor[] slaves;
//    private StateMachineInt stateMachine;
//    private String triggerID;

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

//    public void setStateTrigger(StateMachineInt state, String triggerID)
//    {
//        this.stateMachine = state;
//        this.triggerID = triggerID;
//
//    }


}

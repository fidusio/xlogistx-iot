package io.xlogistx.iot.gpio;


import com.google.gson.annotations.SerializedName;
import com.pi4j.io.gpio.PinState;
import org.zoxweb.shared.util.Const.TimeInMillis;

import java.util.concurrent.atomic.AtomicLong;

public class GPIOConfig {

    @SerializedName("to_monitor")
    private GPIOPin toMonitor;
    private GPIOPin[] followers;
    @SerializedName("high_delay")
    private long highDelay = 0;
    @SerializedName("low_delay")
    private long lowDelay = 0;
    private String name;
    @SerializedName("is_master")
    private boolean isMaster;


    @SerializedName("is_flipped")
    private boolean isInverse;

    public final transient AtomicLong timestamp = new AtomicLong(System.currentTimeMillis());


    public GPIOConfig() {
    }

    public GPIOConfig(GPIOPin toMonitor) {
        setToMonitor(toMonitor);
    }

    public void setToMonitor(GPIOPin toMonitor) {
        this.toMonitor = toMonitor;
    }

    public GPIOPin getToMonitor() {
        return toMonitor;
    }

    public void setFollowers(GPIOPin... toFollow) {
        this.followers = toFollow;
    }

    public GPIOPin[] getFollowers() {
        return followers;
    }

    public void setHighDelay(long followDelay) {
        this.highDelay = followDelay;
    }

    public long getHighDelay() {
        return highDelay;
    }

    public void setLowDelay(long followDelay) {
        this.lowDelay = followDelay;
    }

    public long getLowDelay() {
        return lowDelay;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public boolean isInverse() {
        return isInverse;
    }

    public void setInverse(boolean inverse) {
        isInverse = inverse;
    }


    // Actions

    public void setFollowersState(PinState state) {
        setFollowersState(state, false);
    }

    public synchronized void setFollowersState(PinState state, boolean checkMonitorState) {
        GPIOPin followers[] = getFollowers();
        if (toMonitor != null && followers != null && state != null) {
            if (checkMonitorState) {
                // if set to high always get the current sensor value
                state = GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(getToMonitor().getValue()));
            }
            if (isInverse()) {
                state = PinState.getInverseState(state);
            }
            for (GPIOPin toSet : followers) {
                GPIOTools.SINGLETON.setOutputPinState(toSet.getValue(), state, false, 0, false);
            }
        }
    }

    // Builder

    public GPIOConfig monitorSetter(GPIOPin toMonitor) {
        setToMonitor(toMonitor);
        return this;
    }

    public GPIOConfig followersSetter(GPIOPin... toFollow) {
        setFollowers(toFollow);
        return this;
    }

    public GPIOConfig followersHighDelaySetter(String time) {
        setHighDelay(TimeInMillis.toMillis(time));
        return this;
    }

    public GPIOConfig followersLowDelaySetter(String time) {
        setLowDelay(TimeInMillis.toMillis(time));
        return this;
    }

    public GPIOConfig nameSetter(String name) {
        setName(name);
        return this;
    }

    public GPIOConfig masterSetter(boolean master) {
        setMaster(master);
        return this;
    }

    public GPIOConfig inverseSetter(boolean flipped) {
        setInverse(flipped);
        return this;
    }

}


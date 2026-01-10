package io.xlogistx.iot.gpio64;


import com.google.gson.annotations.SerializedName;
import com.pi4j.io.gpio.digital.DigitalState;
import org.zoxweb.shared.util.Const.TimeInMillis;

import java.util.concurrent.atomic.AtomicLong;

public class GPIO64Config {

    @SerializedName("to_monitor")
    private io.xlogistx.iot.data.GPIOBCMPin toMonitor;
    private io.xlogistx.iot.data.GPIOBCMPin[] followers;
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


    public GPIO64Config() {
    }

    public GPIO64Config(io.xlogistx.iot.data.GPIOBCMPin toMonitor) {
        setToMonitor(toMonitor);
    }

    public void setToMonitor(io.xlogistx.iot.data.GPIOBCMPin toMonitor) {
        this.toMonitor = toMonitor;
    }

    public io.xlogistx.iot.data.GPIOBCMPin getToMonitor() {
        return toMonitor;
    }

    public void setFollowers(io.xlogistx.iot.data.GPIOBCMPin... toFollow) {
        this.followers = toFollow;
    }

    public io.xlogistx.iot.data.GPIOBCMPin[] getFollowers() {
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

    public void setFollowersState(DigitalState state) {
        setFollowersState(state, false);
    }

    public synchronized void setFollowersState(DigitalState state, boolean checkMonitorState) {
        io.xlogistx.iot.data.GPIOBCMPin[] followers = getFollowers();
        if (toMonitor != null && followers != null && state != null) {
            if (checkMonitorState) {
                // if set to high always get the current sensor value
                state = GPIO64Tools.SINGLETON.getPinState(getToMonitor());
            }
            if (isInverse()) {
                state = state == DigitalState.HIGH ? DigitalState.LOW : DigitalState.HIGH;
            }
            for (io.xlogistx.iot.data.GPIOBCMPin toSet : followers) {
                GPIO64Tools.SINGLETON.setOutputPinState(toSet, state, false, 0, false);
            }
        }
    }

    // Builder

    public GPIO64Config monitorSetter(io.xlogistx.iot.data.GPIOBCMPin toMonitor) {
        setToMonitor(toMonitor);
        return this;
    }

    public GPIO64Config followersSetter(io.xlogistx.iot.data.GPIOBCMPin... toFollow) {
        setFollowers(toFollow);
        return this;
    }

    public GPIO64Config followersHighDelaySetter(String time) {
        setHighDelay(TimeInMillis.toMillis(time));
        return this;
    }

    public GPIO64Config followersLowDelaySetter(String time) {
        setLowDelay(TimeInMillis.toMillis(time));
        return this;
    }

    public GPIO64Config nameSetter(String name) {
        setName(name);
        return this;
    }

    public GPIO64Config masterSetter(boolean master) {
        setMaster(master);
        return this;
    }

    public GPIO64Config inverseSetter(boolean flipped) {
        setInverse(flipped);
        return this;
    }

}

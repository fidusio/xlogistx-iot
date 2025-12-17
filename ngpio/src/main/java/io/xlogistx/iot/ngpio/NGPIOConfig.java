package io.xlogistx.iot.ngpio;


import com.google.gson.annotations.SerializedName;
import com.pi4j.io.gpio.digital.DigitalState;
import org.zoxweb.shared.util.Const.TimeInMillis;

import java.util.concurrent.atomic.AtomicLong;

public class NGPIOConfig {

    @SerializedName("to_monitor")
    private NGPIOPin toMonitor;
    private NGPIOPin[] followers;
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


    public NGPIOConfig() {
    }

    public NGPIOConfig(NGPIOPin toMonitor) {
        setToMonitor(toMonitor);
    }

    public void setToMonitor(NGPIOPin toMonitor) {
        this.toMonitor = toMonitor;
    }

    public NGPIOPin getToMonitor() {
        return toMonitor;
    }

    public void setFollowers(NGPIOPin... toFollow) {
        this.followers = toFollow;
    }

    public NGPIOPin[] getFollowers() {
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
        NGPIOPin[] followers = getFollowers();
        if (toMonitor != null && followers != null && state != null) {
            if (checkMonitorState) {
                // if set to high always get the current sensor value
                state = NGPIOTools.SINGLETON.getPinState(getToMonitor());
            }
            if (isInverse()) {
                state = state == DigitalState.HIGH ? DigitalState.LOW : DigitalState.HIGH;
            }
            for (NGPIOPin toSet : followers) {
                NGPIOTools.SINGLETON.setOutputPinState(toSet, state, false, 0, false);
            }
        }
    }

    // Builder

    public NGPIOConfig monitorSetter(NGPIOPin toMonitor) {
        setToMonitor(toMonitor);
        return this;
    }

    public NGPIOConfig followersSetter(NGPIOPin... toFollow) {
        setFollowers(toFollow);
        return this;
    }

    public NGPIOConfig followersHighDelaySetter(String time) {
        setHighDelay(TimeInMillis.toMillis(time));
        return this;
    }

    public NGPIOConfig followersLowDelaySetter(String time) {
        setLowDelay(TimeInMillis.toMillis(time));
        return this;
    }

    public NGPIOConfig nameSetter(String name) {
        setName(name);
        return this;
    }

    public NGPIOConfig masterSetter(boolean master) {
        setMaster(master);
        return this;
    }

    public NGPIOConfig inverseSetter(boolean flipped) {
        setInverse(flipped);
        return this;
    }

}

package io.xlogistx.iot.gpio64;


import com.google.gson.annotations.SerializedName;
import com.pi4j.io.gpio.digital.DigitalState;
import org.zoxweb.shared.util.Const.TimeInMillis;

import java.util.concurrent.atomic.AtomicLong;

public class GPIO64Config {

    @SerializedName("to_monitor")
    private GPIO64Pin toMonitor;
    private GPIO64Pin[] followers;
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

    public GPIO64Config(GPIO64Pin toMonitor) {
        setToMonitor(toMonitor);
    }

    public void setToMonitor(GPIO64Pin toMonitor) {
        this.toMonitor = toMonitor;
    }

    public GPIO64Pin getToMonitor() {
        return toMonitor;
    }

    public void setFollowers(GPIO64Pin... toFollow) {
        this.followers = toFollow;
    }

    public GPIO64Pin[] getFollowers() {
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
        GPIO64Pin[] followers = getFollowers();
        if (toMonitor != null && followers != null && state != null) {
            if (checkMonitorState) {
                // if set to high always get the current sensor value
                state = GPIO64Tools.SINGLETON.getPinState(getToMonitor());
            }
            if (isInverse()) {
                state = state == DigitalState.HIGH ? DigitalState.LOW : DigitalState.HIGH;
            }
            for (GPIO64Pin toSet : followers) {
                GPIO64Tools.SINGLETON.setOutputPinState(toSet, state, false, 0, false);
            }
        }
    }

    // Builder

    public GPIO64Config monitorSetter(GPIO64Pin toMonitor) {
        setToMonitor(toMonitor);
        return this;
    }

    public GPIO64Config followersSetter(GPIO64Pin... toFollow) {
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

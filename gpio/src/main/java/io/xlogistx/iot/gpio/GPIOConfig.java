package io.xlogistx.iot.gpio;


import com.pi4j.io.gpio.PinState;
import org.zoxweb.shared.util.Const.TimeInMillis;

import java.util.concurrent.atomic.AtomicLong;

public class GPIOConfig {


  private GPIOPin toMonitor;
  private GPIOPin[] followers;
  private long highDelay = 0;
  private long lowDelay = 0;
  private String name;
  public final transient AtomicLong timestamp = new AtomicLong(System.currentTimeMillis());
  //private long delay = 0


  public GPIOConfig() { }

  public GPIOConfig(GPIOPin toMonitor) {
    setToMonitor(toMonitor);
  }

  public void setToMonitor(GPIOPin toMonitor) {
    this.toMonitor = toMonitor;
  }

  public GPIOPin getToMonitor() { return toMonitor; }

  public void setFollowers(GPIOPin... toFollow) {
    this.followers = toFollow;
  }

  public GPIOPin[] getFollowers() { return followers; }

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

  public void setName(String name){ this.name = name; }

  public String getName() { return name; }



  // Actions

  public void setFollowersState(PinState state)
  {
    setFollowersState(state, false);
  }

  public synchronized void setFollowersState(PinState state, boolean checkMonitorState)
  {
    GPIOPin followers[] =  getFollowers();
    if (toMonitor != null && followers != null && state != null)
    {
      if (checkMonitorState)
      {
        // if set to high always get the current sensor value
        state = GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(getToMonitor().getValue()));
      }

      for (GPIOPin toSet : followers)
      {
       GPIOTools.SINGLETON.setOutputPinState(toSet.getValue(), state, false, 0, false);
      }
    }
  }

  // Builder

  public GPIOConfig monitor(GPIOPin toMonitor) {
    setToMonitor(toMonitor);
    return this;
  }

  public GPIOConfig followers(GPIOPin... toFollow) {
    setFollowers(toFollow);
    return this;
  }

  public GPIOConfig followersHighDelay(String time) {
    setHighDelay(TimeInMillis.toMillis(time));
    return this;
  }

  public GPIOConfig followersLowDelay(String time) {
    setLowDelay(TimeInMillis.toMillis(time));
    return this;
  }

  public GPIOConfig name(String name) {
    setName(name);
    return this;
  }
}


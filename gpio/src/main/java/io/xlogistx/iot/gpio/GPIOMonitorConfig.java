package io.xlogistx.iot.gpio;


import org.zoxweb.shared.util.Const.TimeInMillis;

import java.util.concurrent.atomic.AtomicLong;

public class GPIOMonitorConfig {


  private GPIOPin toMonitor;
  private GPIOPin[] followers;
  private long highDelay = 0;
  private long lowDelay = 0;
  private String name;
  public final transient AtomicLong timestamp = new AtomicLong(System.currentTimeMillis());
  //private long delay = 0;


  public GPIOMonitorConfig()
  {
  }

  public GPIOMonitorConfig(GPIOPin toMonitor)
  {
    setToMonitor(toMonitor);
  }

  public void setToMonitor(GPIOPin toMonitor)
  {
    this.toMonitor = toMonitor;
  }

  public GPIOPin getToMonitor(){
    return toMonitor;
  }

  public void setFollowers(GPIOPin ...toFollow)
  {
    this.followers = toFollow;
  }

  public GPIOPin[] getFollowers(){
    return followers;
  }

  public GPIOMonitorConfig monitor(GPIOPin toMonitor){
    setToMonitor(toMonitor);
    return this;
  }

  public GPIOMonitorConfig followers(GPIOPin ...toFollow){
    setFollowers(toFollow);
    return this;
  }

  public GPIOMonitorConfig followersHighDelay(String time){
    setHighDelay(TimeInMillis.toMillis(time));
    return this;
  }
  public GPIOMonitorConfig followersLowDelay(String time){
    setLowDelay(TimeInMillis.toMillis(time));
    return this;
  }

  public GPIOMonitorConfig name(String name) {
    setName(name);
    return this;
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

  public void setName(String name){ this.name = name; }

  public String getName() { return name; }
}
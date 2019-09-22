package io.xlogistx.iot.gpio;


import org.zoxweb.shared.util.Const.TimeInMillis;

public class GPIOMonitor {


  private GPIOPin toMonitor;
  private GPIOPin[] toFollow;
  private long highDelay =0;
  private long lowDelay = 0;
  //private long delay = 0;


  public GPIOMonitor()
  {
  }

  public GPIOMonitor(GPIOPin toMonitor)
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

  public void setToFollow(GPIOPin ...toFollow)
  {
    this.toFollow = toFollow;
  }

  public GPIOPin[] getToFollow(){
    return toFollow;
  }

  public GPIOMonitor setMonitor(GPIOPin toMonitor){
    setToMonitor(toMonitor);
    return this;
  }

  public GPIOMonitor setFollowers(GPIOPin ...toFollow){
    setToFollow(toFollow);
    return this;
  }

  public GPIOMonitor setFollowersHighDelay(String time){
    setHighDelay(TimeInMillis.toMillis(time));
    return this;
  }
  public GPIOMonitor setFollowersLowDelay(String time){
    setLowDelay(TimeInMillis.toMillis(time));
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


}

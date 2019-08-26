package io.xlogistx.iot.gpio;

import org.zoxweb.shared.util.Const.TimeInMillis;

public class GPIOMonitor {

  private GPIOPin toMonitor;
  private GPIOPin[] toFollow;
  private long delay =0;


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

  public GPIOMonitor setFollowersDelay(String time){
    setDelay(TimeInMillis.toMillis(time));
    return this;
  }

  public void setDelay(long followDelay) {
    this.delay = followDelay;
  } public long getDelay() {
    return delay;
  }


}

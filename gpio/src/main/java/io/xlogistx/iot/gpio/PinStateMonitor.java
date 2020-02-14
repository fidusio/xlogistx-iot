package io.xlogistx.iot.gpio;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.logging.Logger;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.SharedUtil;

public class PinStateMonitor
    implements GpioPinListenerDigital, AutoCloseable{
  private static final transient Logger log = Logger.getLogger(PinStateMonitor.class.getName());
  private GPIOConfig gpiom;

  private boolean enabled = false;

  private long lastEventTS = 0;
  public PinStateMonitor(GPIOConfig gpiom)
  {
    SharedUtil.checkIfNulls("GPIOM can't be null.", gpiom);
    this.gpiom = gpiom;
  }

  @Override
  public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event)
  {
    lastEventTS = System.currentTimeMillis();
    log.info(Thread.currentThread() + " --> GPIO PIN STATE : " + event.getPin() + " = "
        + GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(event.getPin().getPin())));
    setFollowersState(GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(event.getPin().getPin())), true);
  }

  public  void setFollowersState(final PinState state, boolean scheduled)
  {
    if (gpiom != null && gpiom.getFollowers() != null && state != null)
    {

        if(scheduled) {
          TaskUtil.getDefaultTaskScheduler()
              .queue(state.isHigh() ? gpiom.getHighDelay() : gpiom.getLowDelay(), (Runnable) () -> {
                if (state == PinState.HIGH)
                  gpiom.timestamp.set(System.currentTimeMillis());
                gpiom.setFollowersState(state, true);
                if (state == PinState.LOW) {
                  gpiom.timestamp.set(System.currentTimeMillis() - gpiom.timestamp.get());
                  log.info("It took: " + Const.TimeInMillis.toString(gpiom.timestamp.get()) + " " + gpiom.getName());
                }
              });
        }
        else{
          gpiom.setFollowersState(state, true);
        }
    }
  }

//  public synchronized void setFollowersState(PinState state)
//  {
//    if (gpiom != null && gpiom.getFollowers() != null && state != null) {
//      if (state == PinState.HIGH) {
//        // if set to high always get the current sensor value
//        state = GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(gpiom.getToMonitor().getValue()));
//      }
//
//      GPIOPin followers[] =  gpiom.getFollowers();
//      if (followers != null) {
//        for (GPIOPin toSet : followers) {
//          GPIOTools.SINGLETON.setOutputPinState(toSet.getValue(), state, false, 0, false);
//        }
//      }
//    }
//  }


  public boolean isEnabled() {
    return enabled;
  }

  public synchronized void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if(enabled)
      gpiom.setFollowersState(GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(gpiom.getToMonitor().getValue())), true);
    else
      gpiom.setFollowersState(PinState.LOW);
  }


  public GPIOConfig getMonitorConfig()
  {
    return gpiom;
  }

  public long getLastEventTS()
  {
    return lastEventTS;
  }

  @Override
  public void close()
  {
    GPIOTools.SINGLETON.getGpioController().removeListener(this);
    gpiom.setFollowersState(PinState.LOW);
  }
}

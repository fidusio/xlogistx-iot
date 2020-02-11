package io.xlogistx.iot.gpio;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.logging.Logger;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.util.Const;

public class PinStateMonitor
    implements GpioPinListenerDigital, AutoCloseable{
  private static final transient Logger log = Logger.getLogger(PinStateMonitor.class.getName());
  private GPIOMonitorConfig gpiom;
  private GPIOMonitorConfig gpiomOverride;


  private long lastEventTS = 0;
  public PinStateMonitor(GPIOMonitorConfig gpiom, GPIOMonitorConfig override, boolean checkOnStart)
  {
    this.gpiom = gpiom;
    this.gpiomOverride = override;
    if(checkOnStart)
      setFollowersState(GPIOTools.SINGLETON.getPinState(gpiom.getToMonitor()));
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
    if (gpiom != null && gpiom.getToFollow() != null && state != null)
    {

        if(scheduled) {
          TaskUtil.getDefaultTaskScheduler()
              .queue(state.isHigh() ? gpiom.getHighDelay() : gpiom.getLowDelay(), (Runnable) () -> {
                if (state == PinState.HIGH)
                  gpiom.timestamp.set(System.currentTimeMillis());
                setFollowersState(state);
                if (state == PinState.LOW) {
                  gpiom.timestamp.set(System.currentTimeMillis() - gpiom.timestamp.get());
                  log.info("It took: " + Const.TimeInMillis.toString(gpiom.timestamp.get()) + " " + gpiom.getName());
                }
              });
        }
        else{
          setFollowersState(state);
        }
    }
  }

  public synchronized void setFollowersState(PinState state)
  {
    if (gpiom != null && gpiom.getToFollow() != null && state != null)
    {
      if (state == PinState.HIGH)
      {
        // if set to high always get the current sensor value
        state = GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(gpiom.getToMonitor().getValue()));
        if (state == PinState.HIGH && gpiomOverride != null && GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(gpiomOverride.getToMonitor().getValue())) == PinState.HIGH)
        {
          // let the override state take control
          // just return
          return;
        }
      }

      for (GPIOPin toSet : gpiom.getToFollow()) {
        GPIOTools.SINGLETON.setOutputPinState(toSet.getValue(), state, false, 0, false);
      }
    }

  }

  public long getLastEventTS()
  {
    return lastEventTS;
  }

  @Override
  public void close()
  {
    GPIOTools.SINGLETON.getGpioController().removeListener(this);
    setFollowersState(PinState.LOW);
  }
}

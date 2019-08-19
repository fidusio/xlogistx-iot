package io.xlogistx.iot.gpio;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.util.logging.Logger;

public class PinStateMonitor
    implements GpioPinListenerDigital
{
  private static final transient Logger log = Logger.getLogger(PinStateMonitor.class.getName());
  private GPIOMonitor gpiom;

  private long lastEventTS = 0;
  public PinStateMonitor(GPIOMonitor gpiom, boolean checkOnStart)
  {
    this.gpiom = gpiom;
    if(checkOnStart)
      setFollowersState(GPIOTools.SINGLETON.getPinState(gpiom.getToMonitor()));
  }

  @Override
  public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event)
  {
    lastEventTS = System.currentTimeMillis();
    System.out.println(" --> GPIO PIN STATE : " + event.getPin() + " = "
        + GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(event.getPin().getPin())));
    setFollowersState(GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(event.getPin().getPin())));
  }

  public synchronized void setFollowersState(PinState state)
  {
    if (gpiom != null && gpiom.getToFollow() != null && state != null) {
      for (GPIOPin toSet : gpiom.getToFollow()) {
        GPIOTools.SINGLETON.setOutputPinState(toSet.getValue(), state, false, 0, false);
      }
    }
  }

  public long getLastEventTS()
  {
    return lastEventTS;
  }
}

package io.xlogistx.iot.gpio;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.util.Date;

public class PinStateMonitor
    implements GpioPinListenerDigital
{

  private GPIOMonitor gpiom;
  public PinStateMonitor(GPIOMonitor gpiom, boolean checkOnStart)
  {
    this.gpiom = gpiom;
    if(checkOnStart)
      setFollowersState(GPIOTools.SINGLETON.getPinState(gpiom.getToMonitor()));
  }

  @Override
  public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event)
  {
    System.out.println(new Date() + " --> GPIO PIN STATE : " + event.getPin() + " = "
        + GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(event.getPin().getPin())));
    setFollowersState(GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(event.getPin().getPin())));
  }

  public void setFollowersState(PinState state)
  {
    if (gpiom != null && gpiom.getToFollow() != null && state != null) {
      for (GPIOPin toSet : gpiom.getToFollow()) {
        GPIOTools.SINGLETON.setOutputPinState(toSet.getValue(), state, false, 0, false);
      }
    }
  }
}

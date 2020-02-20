package io.xlogistx.iot.gpio;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.logging.Logger;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.SharedUtil;
import org.zoxweb.server.flow.FlowProcessor;
import org.zoxweb.server.flow.FlowEvent;

public class PinStateMonitor
    implements GpioPinListenerDigital, AutoCloseable{
  private static final transient Logger log = Logger.getLogger(PinStateMonitor.class.getName());
  private GPIOConfig gpiom;

  private transient boolean enabled = false;
  private transient FlowProcessor fp;

  private transient long lastEventTS = 0;

  public PinStateMonitor()
  {
  }

  public PinStateMonitor(GPIOConfig gpiom, FlowProcessor fp)
  {
    SharedUtil.checkIfNulls("GPIOM can't be null.", gpiom);
    this.gpiom = gpiom;
    this.fp = fp;
  }

  @Override
  public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event)
  {
    lastEventTS = System.currentTimeMillis();
    log.info(Thread.currentThread() + " --> GPIO PIN STATE : " + event.getPin() + " = "
        + GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(event.getPin().getPin())));
    if(fp != null)
      fp.publish(new FlowEvent<GpioPinDigitalStateChangeEvent>(this, event));
    else
    {
      PinState state = event.getState();
      TaskUtil.getDefaultTaskScheduler()
              .queue(state.isHigh() ? gpiom.getHighDelay() : gpiom.getLowDelay(), (Runnable) () -> {
                if (state == PinState.HIGH)
                  gpiom.timestamp.set(System.currentTimeMillis());
                setPinState(state);
                if (state == PinState.LOW) {
                  gpiom.timestamp.set(System.currentTimeMillis() - gpiom.timestamp.get());
                  log.info("It took: " + Const.TimeInMillis.toString(gpiom.timestamp.get()) + " " + gpiom.getName());
                }
              });
    }
  }


  public synchronized void setPinState(PinState state)
  {
    if(enabled)
      gpiom.setFollowersState(state, true);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public synchronized void setEnabled(boolean enabled) {
    this.enabled = enabled;
    if(enabled)
      setPinState(GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(gpiom.getToMonitor().getValue())));
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

  public PinStateMonitor monitorConfigSetter(GPIOConfig gpiom)
  {
    this.gpiom = gpiom;
    return this;
  }

  public PinStateMonitor flowProcessorSetter(FlowProcessor fp)
  {
    this.fp = fp;
    return this;
  }
}

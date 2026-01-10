package io.xlogistx.iot.gpio32;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.zoxweb.server.flow.FlowEvent;
import org.zoxweb.server.flow.FlowProcessor;
import org.zoxweb.server.task.TaskSchedulerProcessor;
import org.zoxweb.shared.util.SharedUtil;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class PinStateMonitor
        implements GpioPinListenerDigital, AutoCloseable {
    private static final transient Logger log = Logger.getLogger(PinStateMonitor.class.getName());
    private GPIOConfig gpiom;

    private transient boolean enabled = false;
    private transient FlowProcessor fp;

    private transient long lastEventTS = 0;
    private transient TaskSchedulerProcessor tsp;
    private transient AtomicLong counter = new AtomicLong();

    public PinStateMonitor() {
    }

    public PinStateMonitor(GPIOConfig gpiom, FlowProcessor fp, TaskSchedulerProcessor tsp) {
        SharedUtil.checkIfNulls("GPIOM can't be null.", gpiom);
        this.gpiom = gpiom;
        this.tsp = tsp;
        this.fp = fp;
    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        lastEventTS = System.currentTimeMillis();
        log.info("[" + counter.incrementAndGet() + "] " + event.getPin() + " = "
                + GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(event.getPin().getPin())));
        if (fp != null)
            fp.publish(new FlowEvent<GpioPinDigitalStateChangeEvent>(this, event));
        else {
            PinState state = event.getState();
//      log.info("[" + counter.incrementAndGet() + "] " + event.getPin().getName() + ":" + state );
            if (tsp != null) {
                tsp.queue(state.isHigh() ? gpiom.getHighDelay() : gpiom.getLowDelay(), (Runnable) () -> {
                    if (state == PinState.HIGH)
                        gpiom.timestamp.set(System.currentTimeMillis());
                    setPinState(state);
                    if (state == PinState.LOW) {
                        gpiom.timestamp.set(System.currentTimeMillis() - gpiom.timestamp.get());
//            try {
//              log.info("It took: " + Const.TimeInMillis.toString(gpiom.timestamp.get()) + " " + gpiom.getName());
//            }
//            catch(Exception e)
//            {
//              //e.printStackTrace();
//
//            }
                    }
                });
            }

        }
    }


    public synchronized void setPinState(PinState state) {
        if (enabled)
            gpiom.setFollowersState(state, true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled)
            setPinState(GPIOTools.SINGLETON.getPinState(GPIOPin.lookup(gpiom.getToMonitor().getValue())));
        else
            gpiom.setFollowersState(PinState.LOW);
    }


    public GPIOConfig getMonitorConfig() {
        return gpiom;
    }

    public long getLastEventTS() {
        return lastEventTS;
    }

    @Override
    public void close() {
        GPIOTools.SINGLETON.getGpioController().removeListener(this);
        gpiom.setFollowersState(PinState.LOW);
    }

    public PinStateMonitor monitorConfigSetter(GPIOConfig gpiom) {
        this.gpiom = gpiom;
        return this;
    }

    public PinStateMonitor flowProcessorSetter(FlowProcessor fp) {
        this.fp = fp;
        return this;
    }
}

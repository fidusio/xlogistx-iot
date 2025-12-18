package io.xlogistx.iot.gpio64;

import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.DigitalStateChangeEvent;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;
import org.zoxweb.server.flow.FlowEvent;
import org.zoxweb.server.flow.FlowProcessor;
import org.zoxweb.server.task.TaskSchedulerProcessor;
import org.zoxweb.shared.util.SharedUtil;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Pin state monitor for Pi4J v3.
 * Implements DigitalStateChangeListener instead of GpioPinListenerDigital from v1.x.
 */
public class PinState64Monitor
        implements DigitalStateChangeListener, AutoCloseable {
    private static final transient Logger log = Logger.getLogger(PinState64Monitor.class.getName());
    private GPIO64Config gpiom;

    private transient boolean enabled = false;
    private transient FlowProcessor fp;

    private transient long lastEventTS = 0;
    private transient TaskSchedulerProcessor tsp;
    private transient AtomicLong counter = new AtomicLong();

    public PinState64Monitor() {
    }

    public PinState64Monitor(GPIO64Config gpiom, FlowProcessor fp, TaskSchedulerProcessor tsp) {
        SharedUtil.checkIfNulls("GPIOM can't be null.", gpiom);
        this.gpiom = gpiom;
        this.tsp = tsp;
        this.fp = fp;
    }

    @Override
    public void onDigitalStateChange(DigitalStateChangeEvent event) {
        lastEventTS = System.currentTimeMillis();
        GPIO64Pin gpioPin = GPIO64Pin.lookup(event.source().address());
        log.info("[" + counter.incrementAndGet() + "] GPIO " + event.source().address() + " = "
                + GPIO64Tools.SINGLETON.getPinState(gpioPin));
        if (fp != null)
            fp.publish(new FlowEvent<DigitalStateChangeEvent>(this, event));
        else {
            DigitalState state = event.state();
            if (tsp != null) {
                tsp.queue(state == DigitalState.HIGH ? gpiom.getHighDelay() : gpiom.getLowDelay(), (Runnable) () -> {
                    if (state == DigitalState.HIGH)
                        gpiom.timestamp.set(System.currentTimeMillis());
                    setPinState(state);
                    if (state == DigitalState.LOW) {
                        gpiom.timestamp.set(System.currentTimeMillis() - gpiom.timestamp.get());
                    }
                });
            }

        }
    }


    public synchronized void setPinState(DigitalState state) {
        if (enabled)
            gpiom.setFollowersState(state, true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled)
            setPinState(GPIO64Tools.SINGLETON.getPinState(gpiom.getToMonitor()));
        else
            gpiom.setFollowersState(DigitalState.LOW);
    }


    public GPIO64Config getMonitorConfig() {
        return gpiom;
    }

    public long getLastEventTS() {
        return lastEventTS;
    }

    @Override
    public void close() {
        // In Pi4J v3, listeners are removed differently
        gpiom.setFollowersState(DigitalState.LOW);
    }

    public PinState64Monitor monitorConfigSetter(GPIO64Config gpiom) {
        this.gpiom = gpiom;
        return this;
    }

    public PinState64Monitor flowProcessorSetter(FlowProcessor fp) {
        this.fp = fp;
        return this;
    }
}

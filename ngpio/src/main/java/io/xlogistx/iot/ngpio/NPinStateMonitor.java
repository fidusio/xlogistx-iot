package io.xlogistx.iot.ngpio;

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
public class NPinStateMonitor
        implements DigitalStateChangeListener, AutoCloseable {
    private static final transient Logger log = Logger.getLogger(NPinStateMonitor.class.getName());
    private NGPIOConfig gpiom;

    private transient boolean enabled = false;
    private transient FlowProcessor fp;

    private transient long lastEventTS = 0;
    private transient TaskSchedulerProcessor tsp;
    private transient AtomicLong counter = new AtomicLong();

    public NPinStateMonitor() {
    }

    public NPinStateMonitor(NGPIOConfig gpiom, FlowProcessor fp, TaskSchedulerProcessor tsp) {
        SharedUtil.checkIfNulls("GPIOM can't be null.", gpiom);
        this.gpiom = gpiom;
        this.tsp = tsp;
        this.fp = fp;
    }

    @Override
    public void onDigitalStateChange(DigitalStateChangeEvent event) {
        lastEventTS = System.currentTimeMillis();
        NGPIOPin gpioPin = NGPIOPin.lookup(event.source().address());
        log.info("[" + counter.incrementAndGet() + "] GPIO " + event.source().address() + " = "
                + NGPIOTools.SINGLETON.getPinState(gpioPin));
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
            setPinState(NGPIOTools.SINGLETON.getPinState(gpiom.getToMonitor()));
        else
            gpiom.setFollowersState(DigitalState.LOW);
    }


    public NGPIOConfig getMonitorConfig() {
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

    public NPinStateMonitor monitorConfigSetter(NGPIOConfig gpiom) {
        this.gpiom = gpiom;
        return this;
    }

    public NPinStateMonitor flowProcessorSetter(FlowProcessor fp) {
        this.fp = fp;
        return this;
    }
}

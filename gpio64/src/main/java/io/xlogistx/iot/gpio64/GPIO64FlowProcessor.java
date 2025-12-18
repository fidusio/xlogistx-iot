package io.xlogistx.iot.gpio64;

import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.io.gpio.digital.DigitalStateChangeEvent;
import org.zoxweb.server.flow.DefaultFlowProcessor;
import org.zoxweb.server.flow.FlowEvent;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.task.TaskSchedulerProcessor;
import org.zoxweb.shared.util.Const;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * GPIO Flow Processor for Pi4J v3.
 * Handles master/slave pin relationships and event flow processing.
 */
public class GPIO64FlowProcessor
        extends DefaultFlowProcessor<DigitalStateChangeEvent> {

    private static final transient Logger log = Logger.getLogger(GPIO64FlowProcessor.class.getName());
    private PinState64MonitorConfig pinStateMonitorConfig;

    public GPIO64FlowProcessor(PinState64MonitorConfig pinStateMonitorConfig, TaskSchedulerProcessor tsp) {
        super(tsp);
        this.pinStateMonitorConfig = pinStateMonitorConfig;
    }


    @Override
    public synchronized void accept(FlowEvent<DigitalStateChangeEvent> event) {
        PinState64Monitor source = (PinState64Monitor) event.getSource();
        log.info(Thread.currentThread() + " Event:" + event.getSource() + " " + event.getFlowType());
        if (source == pinStateMonitorConfig.getMaster()) {
            log.info("Master pin: " + event.getFlowType());
            // disable or enable all the slaves
            DigitalState state = event.getFlowType().state();
            for (PinState64Monitor psm : pinStateMonitorConfig.getSlaves()) {
                psm.setEnabled(state == DigitalState.LOW);
            }
        } else {
            if (source.isEnabled()) {
                DigitalState state = event.getFlowType().state();
                GPIO64Config gpiom = source.getMonitorConfig();
                tsp.queue(state == DigitalState.HIGH ? gpiom.getHighDelay() : gpiom.getLowDelay(), (Runnable) () -> {
                    if (state == DigitalState.HIGH)
                        gpiom.timestamp.set(System.currentTimeMillis());
                    source.setPinState(state);
                    if (state == DigitalState.LOW) {
                        gpiom.timestamp.set(System.currentTimeMillis() - gpiom.timestamp.get());
                        log.info("It took: " + Const.TimeInMillis.toString(gpiom.timestamp.get()) + " " + gpiom.getName());
                    }
                });
            } else {
                log.info("Source not enabled " + source);
            }
        }
    }

    @Override
    public synchronized void close() throws IOException {
        IOUtil.close(tsp);
    }

    public synchronized void init() {
        DigitalState masterState = GPIO64Tools.SINGLETON.getPinState(pinStateMonitorConfig.getMaster().getMonitorConfig().getToMonitor());
        for (PinState64Monitor psm : pinStateMonitorConfig.getSlaves()) {
            psm.flowProcessorSetter(this);
            psm.setEnabled(masterState == DigitalState.LOW);
            DigitalInput input = GPIO64Tools.SINGLETON.setInputPin(psm.getMonitorConfig().getToMonitor());
            input.addListener(psm);
        }

        pinStateMonitorConfig.getMaster().flowProcessorSetter(this);
        DigitalInput input = GPIO64Tools.SINGLETON.setInputPin(pinStateMonitorConfig.getMaster().getMonitorConfig().getToMonitor());
        input.addListener(pinStateMonitorConfig.getMaster());
        log.info("Init done");
    }
}

package io.xlogistx.iot.gpio;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import org.zoxweb.server.flow.DefaultFlowProcessor;
import org.zoxweb.server.flow.FlowEvent;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.task.TaskSchedulerProcessor;
import org.zoxweb.shared.util.Const;

import java.io.IOException;
import java.util.logging.Logger;

public class GPIOFlowProcessor
        extends DefaultFlowProcessor<GpioPinDigitalStateChangeEvent> {

    private static final transient Logger log = Logger.getLogger(GPIOFlowProcessor.class.getName());
    private PinStateMonitorConfig pinStateMonitorConfig;

    public GPIOFlowProcessor(PinStateMonitorConfig pinStateMonitorConfig, TaskSchedulerProcessor tsp) {
        super(tsp);
        this.pinStateMonitorConfig = pinStateMonitorConfig;
    }


    @Override
    public synchronized void accept(FlowEvent<GpioPinDigitalStateChangeEvent> event) {
        PinStateMonitor source = (PinStateMonitor) event.getSource();
        log.info(Thread.currentThread() + " Event:" + event.getSource() + " " + event.getFlowType());
        if (source == pinStateMonitorConfig.getMaster()) {
            log.info("Master pin: " + event.getFlowType());
            // disable or enable all the slaves
            PinState state = event.getFlowType().getState();
            for (PinStateMonitor psm : pinStateMonitorConfig.getSlaves()) {
                psm.setEnabled(state == PinState.LOW);
            }
        } else {
            if (source.isEnabled()) {
                PinState state = event.getFlowType().getState();
                GPIOConfig gpiom = source.getMonitorConfig();
                tsp.queue(state.isHigh() ? gpiom.getHighDelay() : gpiom.getLowDelay(), (Runnable) () -> {
                    if (state == PinState.HIGH)
                        gpiom.timestamp.set(System.currentTimeMillis());
                    source.setPinState(state);
                    if (state == PinState.LOW) {
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
        PinState masterState = GPIOTools.SINGLETON.getPinState(pinStateMonitorConfig.getMaster().getMonitorConfig().getToMonitor());
        for (PinStateMonitor psm : pinStateMonitorConfig.getSlaves()) {
            psm.flowProcessorSetter(this);
            psm.setEnabled(masterState == PinState.LOW);
            GpioPinDigitalInput input = GPIOTools.SINGLETON.getGpioController()
                    .provisionDigitalInputPin(psm.getMonitorConfig().getToMonitor().getValue());
            input.addListener(psm);
        }

        pinStateMonitorConfig.getMaster().flowProcessorSetter(this);
        GpioPinDigitalInput input = GPIOTools.SINGLETON.getGpioController()
                .provisionDigitalInputPin(pinStateMonitorConfig.getMaster().getMonitorConfig().getToMonitor().getValue());
        input.addListener(pinStateMonitorConfig.getMaster());
        log.info("Init done");
    }
}

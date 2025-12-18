package io.xlogistx.iot.gpio64;


import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.IOType;
import com.pi4j.io.gpio.digital.*;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmConfig;
import com.pi4j.io.pwm.PwmType;
import io.xlogistx.iot.data.GPIOAction;
import io.xlogistx.iot.gpio64.data.PWM64Config;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.server.task.TaskSchedulerProcessor;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.data.Range;
import org.zoxweb.shared.task.SupplierTask;
import org.zoxweb.shared.util.Const.Bool;
import org.zoxweb.shared.util.Const.TimeInMillis;
import org.zoxweb.shared.util.NVCollection;
import org.zoxweb.shared.util.NVCollectionStringDecoder;
import org.zoxweb.shared.util.SharedUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


/**
 * GPIO Tools singleton for Pi4J v3.
 * Uses Context-based architecture instead of GpioController from v1.x.
 */
public class GPIO64Tools {
    public static final Range<Integer> PWM_RANGE = new Range<Integer>(2, 4095);
    public static final LogWrapper log = new LogWrapper(GPIO64Tools.class);

    // This statement must be the last static code
    public static final GPIO64Tools SINGLETON = new GPIO64Tools();

    private volatile Context pi4j = null;
    private int pwmRangeValue;

    // Track provisioned I/O instances by their pin address
//    private final Map<Integer, DigitalOutput> provisionedOutputs = new ConcurrentHashMap<>();
//    private final Map<Integer, DigitalInput> provisionedInputs = new ConcurrentHashMap<>();
//    private final Map<Integer, Pwm> provisionedPwm = new ConcurrentHashMap<>();


    private GPIO64Tools() {
        try {
            pi4j = Pi4J.newAutoContext();
            log.getLogger().info("Pi4J v3 Context initialized");
            setPWMRangeMod(4095, 0);

            log.getLogger().info("DIGITAL_OUTPUT: " + pi4j.registry().allByIoType(IOType.DIGITAL_OUTPUT));

            log.getLogger().info("DIGITAL_INPUT: " +pi4j.registry().allByIoType(IOType.DIGITAL_INPUT));
            log.getLogger().info("PWM: " +pi4j.registry().allByIoType(IOType.PWM));
            log.getLogger().info("I2C: " +pi4j.registry().allByIoType(IOType.I2C));
        } catch (Exception e) {
            log.getLogger().warning("Failed to initialize Pi4J context: " + e.getMessage());
        }
    }

    public Context getContext() {
        return pi4j;
    }

    public synchronized void resetPin(int bcmAddress) {
        // Reset output if exists
        DigitalOutput output = lookupDigitalOutput(bcmAddress);
        if (output != null) {
            try {
                pi4j.shutdown(output.getId());
            } catch (Exception e) {
                e.printStackTrace();
                log.getLogger().fine("Error shutting down output pin " + bcmAddress + ": " + e.getMessage());
            }
        }

        // Reset input if exists
        DigitalInput input = lookupDigitalInput(bcmAddress);
        if (input != null) {
            try {
                pi4j.shutdown(input.getId());
            } catch (Exception e) {
                e.printStackTrace();
                log.getLogger().fine("Error shutting down input pin " + bcmAddress + ": " + e.getMessage());
            }
        }

        // Reset PWM if exists
        Pwm pwm = lookupPWM(bcmAddress);
        if (pwm != null) {
            try {
                pi4j.shutdown(pwm.getId());
            } catch (Exception e) {
                e.printStackTrace();
                log.getLogger().fine("Error shutting down PWM pin " + bcmAddress + ": " + e.getMessage());
            }
        }
    }

    public synchronized void resetPin(GPIO64Pin pin) {
        resetPin(pin.getBCMAddress());
    }

    public synchronized DigitalOutput setOutputPin(GPIO64Pin pin, DigitalState state, long durationInMillis) {
        return setOutputPin(pin.getBCMAddress(), state, durationInMillis);
    }


    public synchronized DigitalOutput createDigitalOutput(int bcmAddress, DigitalState state) {
        DigitalOutput output = lookupDigitalOutput(bcmAddress);
        if (output == null) {
            DigitalOutputConfig config = DigitalOutput.newConfigBuilder(pi4j)
                    .id("gpio-out-" + bcmAddress)
                    .name("GPIO Output " + bcmAddress)
                    .address(bcmAddress)
                    .initial(state)
                    .shutdown(DigitalState.LOW)
                    .build();
            output = pi4j.create(config);
        }

        return output;
    }

    public synchronized DigitalOutput lookupDigitalOutput(int bcmAddress) {
        String id = "gpio-out-" + bcmAddress;
        if (pi4j.registry().exists(id))
            return pi4j.registry().get(id);
        return null;
    }

    public synchronized DigitalInput lookupDigitalInput(int bcmAddress) {
        String id = "gpio-in-" + bcmAddress;
        if (pi4j.registry().exists(id))
            return pi4j.registry().get(id);
        return null;
    }

    public synchronized Pwm lookupPWM(int bcmAddress) {
        String id = "gpio-pwm-" + bcmAddress;
        if (pi4j.registry().exists(id))
            return pi4j.registry().get(id);
        return null;
    }


    public synchronized DigitalInput createDigitalInput(int bcmAddress) {

        DigitalInput input = lookupDigitalInput(bcmAddress);

        if (input == null) {
            DigitalInputConfig config = DigitalInput.newConfigBuilder(pi4j)
                    .id("gpio-in-" + bcmAddress)
                    .name("GPIO Input " + bcmAddress)
                    .address(bcmAddress).build();
            input = pi4j.create(config);
        }

        return input;
    }

    public synchronized DigitalOutput setOutputPin(int bcmAddress, DigitalState state, long durationInMillis) {
        log.getLogger().info(SharedUtil.toCanonicalID(',', Thread.currentThread(), bcmAddress, state, durationInMillis));
        resetPin(bcmAddress);
        DigitalOutput output = createDigitalOutput(bcmAddress, state);

        output.state(state);

        if (durationInMillis > 0) {
            TaskUtil.defaultTaskScheduler().queue(durationInMillis, new SupplierTask<DigitalOutput>(output) {
                @Override
                public void run() {
                    DigitalOutput gpdo = get();
                    DigitalState toBeSet = gpdo.state() == DigitalState.HIGH ? DigitalState.LOW : DigitalState.HIGH;
                    gpdo.state(toBeSet);
                    log.getLogger().info(gpdo.name() + " set to " + toBeSet);
                }
            });
        }

        return output;
    }

    public synchronized DigitalOutput setOutputPinState(GPIO64Pin pin, DigitalState state, boolean persist, long durationInMillis, boolean delay) {
        return setOutputPinState(pin.getBCMAddress(), state, persist, durationInMillis, delay);
    }

    public synchronized DigitalOutput setOutputPinState(int bcmAddress, DigitalState state, boolean persist, long durationInMillis, boolean delay) {
        log.getLogger().info(SharedUtil.toCanonicalID(',', Thread.currentThread(), bcmAddress, state, persist, durationInMillis));

        resetPin(bcmAddress);

        if (delay && durationInMillis > 0) {
            try {
                Thread.sleep(durationInMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        DigitalOutput output = lookupDigitalOutput(bcmAddress);
        output.state(state);

        if (durationInMillis > 0 && !delay) {
            try {
                Thread.sleep(durationInMillis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // revert to the back state
            state = state == DigitalState.HIGH ? DigitalState.LOW : DigitalState.HIGH;
            output.state(state);
        }

        return output;
    }

    public synchronized DigitalInput setInputPin(GPIO64Pin... gpios) {
        return setInputPin(null, gpios);
    }

    public synchronized DigitalInput setInputPin(PullResistance pullResistance, GPIO64Pin... gpios) {
        DigitalInput lastInput = null;
        for (GPIO64Pin gpio : gpios) {
            resetPin(gpio.getBCMAddress());

            DigitalInputConfigBuilder configBuilder = DigitalInput.newConfigBuilder(pi4j)
                    .id("gpio-in-" + gpio.getBCMAddress())
                    .name("GPIO Input " + gpio.getBCMAddress())
                    .address(gpio.getBCMAddress());

            if (pullResistance != null) {
                configBuilder.pull(pullResistance);
            }

            DigitalInputConfig config = configBuilder.build();
            lastInput = pi4j.create(config);
            if (lastInput != null && pullResistance != null)
                lastInput.pull();

        }
        return lastInput;
    }

    public synchronized void setPWMRangeMod(int range, int mod) {
        if (!PWM_RANGE.within(range))
            throw new IllegalArgumentException(range + " value out of range [2-4095]");
        this.pwmRangeValue = range;
    }

    public int getPWMRange() {
        return pwmRangeValue;
    }


    public synchronized Pwm setPWM(GPIO64Pin pin, float frequency, Range<Float> dutyCycle, long cycleDelay, int repeat) {
        log.getLogger().info(SharedUtil.toCanonicalID(',', pin, frequency, dutyCycle, cycleDelay, repeat));

        if (dutyCycle.getStart() < 0 || dutyCycle.getEnd() > 100) {
            throw new IllegalArgumentException(dutyCycle + " duty cycle out of range [0-100]");
        }
        resetPin(pin.getBCMAddress());

        PwmConfig config = Pwm.newConfigBuilder(pi4j)
                .id("pwm-" + pin.getBCMAddress())
                .name("PWM " + pin.getBCMAddress())
                .address(pin.getBCMAddress())
                .pwmType(PwmType.HARDWARE)
                .initial(0)
                .shutdown(0)
                .build();

        Pwm pwmOutput = pi4j.create(config);

        // Start PWM sweep task
        TaskUtil.defaultTaskScheduler().queue(0, new PWM64RangeExec(pwmOutput,
                new Range<Integer>(dutyCycleToPWM(dutyCycle.getStart()), dutyCycleToPWM(dutyCycle.getEnd())),
                (int) frequency, cycleDelay, repeat));

        return pwmOutput;
    }

    public int dutyCycleToPWM(float dutyCycle) {
        float pwm = ((float) getPWMRange() * dutyCycle) / 100;
        return (int) pwm;
    }

    public synchronized Pwm setPWM(GPIO64Pin pin, float frequency, float dutyCycle, long duration) {
        log.getLogger().info(SharedUtil.toCanonicalID(',', pin, frequency, dutyCycle, TimeInMillis.toString(duration)));

        if (dutyCycle < 0 || dutyCycle > 100) {
            throw new IllegalArgumentException(dutyCycle + " duty cycle out of range [0-100]");
        }
        resetPin(pin.getBCMAddress());

        PwmConfig config = Pwm.newConfigBuilder(pi4j)
                .id("pwm-" + pin.getBCMAddress())
                .name("PWM " + pin.getBCMAddress())
                .address(pin.getBCMAddress())
                .pwmType(PwmType.HARDWARE)
                .initial(0)
                .shutdown(0)
                .build();

        Pwm pwmOutput = pi4j.create(config);


        // Set PWM with frequency and duty cycle
        pwmOutput.on((int) dutyCycle, (int) frequency);

        log.getLogger().info("PWM Freq:" + frequency + ", duty-cycle:" + dutyCycle + "%");

        if (duration > 0) {
            TaskUtil.defaultTaskScheduler().queue(duration, () -> {
                try {
                    pwmOutput.off();
                    log.getLogger().info("PWM:" + pwmOutput.name() + " set to 0");
                } catch (Exception e) {
                    log.getLogger().info("Error setting pwm pin " + pwmOutput.name() + " to 0.");
                }
            });
        }
        return pwmOutput;
    }

    public synchronized long setPWM(PWM64Config pwmConfig) {
        GPIO64Pin[] all = pwmConfig.getGPIOPins();

        List<Pwm> outputs = new ArrayList<Pwm>();
        for (GPIO64Pin pin : all) {
            resetPin(pin.getBCMAddress());

            PwmConfig config = Pwm.newConfigBuilder(pi4j)
                    .id("pwm-" + pin.getBCMAddress())
                    .name("PWM " + pin.getBCMAddress())
                    .address(pin.getBCMAddress())
                    .pwmType(PwmType.HARDWARE)
                    .initial(0)
                    .shutdown(0)
                    .build();

            Pwm pwmOutput = pi4j.create(config);

            outputs.add(pwmOutput);
        }

        log.getLogger().info("Freq:" + pwmConfig.getFrequency() + ", duty-cycle:" + pwmConfig.getDutyCycle() +
                "%, duration:" + TimeInMillis.toString(pwmConfig.getDuration()));

        long delta = System.currentTimeMillis();
        outputs.forEach((pwmPin) -> {
            pwmPin.on((int) pwmConfig.getDutyCycle(), (int) pwmConfig.getFrequency());
        });

        outputs.forEach((pwmPin) -> log.getLogger().info(pwmPin.name() + " pwm started"));

        if (pwmConfig.getDuration() > 0) {
            TaskUtil.defaultTaskScheduler().queue(pwmConfig.getDuration(), () -> {
                outputs.forEach((pwm) -> {
                    try {
                        pwm.off();
                        log.getLogger().info("PWM:" + pwm.name() + " turned off");
                    } catch (Exception e) {
                        log.getLogger().info("Error turning off pwm pin " + pwm.name());
                    }
                });
            });
        }
        delta = System.currentTimeMillis() - delta;
        log.getLogger().info("It took " + TimeInMillis.toString(delta));

        return pwmConfig.getDuration();
    }


    public synchronized DigitalState getPinState(GPIO64Pin pin) {
        DigitalInput input = lookupDigitalInput(pin.getBCMAddress());
        if (input != null) {
            return input.state();
        }

        DigitalOutput output = lookupDigitalOutput(pin.getBCMAddress());
        if (output != null) {
            return output.state();
        }


        return DigitalState.UNKNOWN;
    }

//    public DigitalOutput getProvisionedOutput(int bcmAddress) {
//        return provisionedOutputs.get(bcmAddress);
//    }
//
//    public DigitalInput getProvisionedInput(int bcmAddress) {
//        return provisionedInputs.get(bcmAddress);
//    }
//
//    public Pwm getProvisionedPwm(int bcmAddress) {
//        return provisionedPwm.get(bcmAddress);
//    }

    public void shutdown() {
        if (pi4j != null) {
            pi4j.shutdown();
        }
    }


    public static void main(String... args) {
        try {

            int index = 0;
            long durationBeforeExit = -1;
            long delta = System.currentTimeMillis();
            NVCollectionStringDecoder decoder = new NVCollectionStringDecoder("=", ",", true);
            GPIOAction action = null;
            for (; index < args.length; index++) {
                if (index == 0) {
                    action = io.xlogistx.iot.data.GPIOAction.lookup(args[index]);
                    System.out.println("Action:" + action);
                    if (action != null)
                        continue;
                    else
                        action = GPIOAction.SET;
                }
                GPIO64Pin gpioPin = null;
                switch (action) {
                    case READ:
                        GPIO64Pin[] gpioPins = GPIO64Pin.lookup(args[index]);
                        for (GPIO64Pin gpioP : gpioPins) {
                            System.out.println(gpioP + ", " + SINGLETON.getPinState(gpioP));
                        }
                        break;
                    case READ_AS_INPUT:
                        gpioPins = GPIO64Pin.lookup(args[index]);
                        for (GPIO64Pin gpioP : gpioPins) {
                            SINGLETON.setInputPin(gpioP);
                            System.out.println(gpioP + ", " + SINGLETON.getPinState(gpioP));
                        }
                        break;
                    case SET_PULL_DOWN:
                        gpioPins = GPIO64Pin.lookup(args[index]);
                        for (GPIO64Pin gpioP : gpioPins) {
                            SINGLETON.setInputPin(PullResistance.PULL_DOWN, gpioP);
                            System.out.println(gpioP + ", " + SINGLETON.getPinState(gpioP));
                        }
                        break;
                    case SET_PULL_UP:
                        gpioPins = GPIO64Pin.lookup(args[index]);
                        for (GPIO64Pin gpioP : gpioPins) {
                            SINGLETON.setInputPin(PullResistance.PULL_UP, gpioP);
                            System.out.println(gpioP + ", " + SINGLETON.getPinState(gpioP));
                        }
                        break;
                    case MONITOR:
                        TaskUtil.defaultTaskScheduler();
                        boolean inverse = false;

                        String pins[] = args[index].split(Pattern.quote(","));
                        if ("-i".equalsIgnoreCase(pins[0])) {
                            inverse = true;
                            pins = Arrays.copyOfRange(pins, 1, pins.length);
                        }

                        gpioPins = GPIO64Pin.lookup(pins);
                        log.getLogger().info("to monitor:" + pins[0]);
                        gpioPin = gpioPins[0];
                        ArrayList<GPIO64Pin> toSet = new ArrayList<GPIO64Pin>();

                        for (int p = 1; p < gpioPins.length; p++) {
                            toSet.add(gpioPins[p]);
                        }

                        DigitalInput input = SINGLETON.setInputPin(gpioPin);

                        GPIO64Config gm = new GPIO64Config().monitorSetter(gpioPin)
                                .followersSetter(toSet.toArray(new GPIO64Pin[0]))
                                .followersHighDelaySetter("10sec")
                                .followersLowDelaySetter("0sec")
                                .nameSetter(gpioPin.toString())
                                .inverseSetter(inverse);

                        System.out.println(GSONUtil.toJSONDefault(gm));
                        PinState64Monitor psm = new PinState64Monitor(gm, null, TaskUtil.defaultTaskScheduler());

                        input.addListener(psm);

                        psm.setEnabled(true);
                        break;
                    case STATE_MONITOR:
                        log.getLogger().info("State monitor");

                        PinState64Machine pinStateMachine = new PinState64Machine(TaskUtil.defaultTaskScheduler());
                        pinStateMachine.start(true);

                        GPIO64Pin.GPIONameMap gpioNameMap = GPIO64Pin.toGPIONameMap(args[index]);

                        pinStateMachine.monitorDigitalPin(PullResistance.PULL_DOWN, args[index],
                                gpioNameMap != null ? gpioNameMap.nameMap : "pod-counter");

                        break;
                    case SET:
                        NVCollection<String> param = decoder.decode(args[index]);

                        Integer address = GPIO64Pin.lookupAddress(param.getName());

                        List<String> values = param.asList();
                        int valuesIndex = 0;
                        DigitalState state = Bool.lookupValue(values.get(valuesIndex++)) ? DigitalState.HIGH : DigitalState.LOW;

                        boolean persist = values.size() > valuesIndex ? Bool.lookupValue(values.get(valuesIndex++)) : false;

                        String waiting = values.size() > valuesIndex ? values.get(valuesIndex++) : null;
                        boolean delayFlag = false;
                        if (waiting != null) {
                            if (waiting.startsWith("d")) {
                                delayFlag = true;
                                waiting = waiting.substring(1);
                            }
                        }

                        long millis = waiting != null ? TimeInMillis.toMillis(waiting) : 0;
                        log.getLogger().info("GPIO " + address + " set to " + values + " for " + millis + " millis");
                        SINGLETON.setOutputPinState(address, state, persist, millis, delayFlag);
                        break;
                    case PWM:
                        String jsonCmd = args[index];
                        PWM64Config pwmConfig = GSONUtil.fromJSONDefault(IOUtil.inputStreamToString(jsonCmd), PWM64Config.class);
                        durationBeforeExit = SINGLETON.setPWM(pwmConfig);
                        break;
                    case FLOW:
                        jsonCmd = args[index++];
                        String jsonConfig = IOUtil.inputStreamToString(jsonCmd);
                        log.getLogger().info("Flow Config: " + jsonConfig);
                        TaskSchedulerProcessor tsp = null;
                        if (args.length > index) {
                            if (args[index++].equalsIgnoreCase("-m")) {
                                tsp = TaskUtil.defaultTaskScheduler();
                                log.getLogger().info("Parallel scheduler");
                            }
                        }
                        if (tsp == null) {
                            tsp = TaskUtil.simpleTaskScheduler();
                            log.getLogger().info("Single threaded scheduler");
                        }
                        PinState64MonitorConfig pinStateMonitorConfig = GSONUtil.fromJSONDefault(jsonConfig, PinState64MonitorConfig.class);
                        new GPIO64FlowProcessor(pinStateMonitorConfig, tsp).init();

                        break;
                }
            }


            delta = System.currentTimeMillis() - delta;
            log.getLogger().info("It took : " + TimeInMillis.toString(delta));
            if (durationBeforeExit >= 0) {
                TaskUtil.sleep(durationBeforeExit);
                TaskUtil.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("usage: [GPIO=Low/High,true/false,[duration]...");
        }
    }


}

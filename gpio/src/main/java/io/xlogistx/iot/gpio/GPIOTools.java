package io.xlogistx.iot.gpio;


import com.pi4j.io.gpio.*;
import com.pi4j.wiringpi.Gpio;
import io.xlogistx.iot.gpio.data.PWMConfig;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.logging.LoggerUtil;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.regex.Pattern;


public class GPIOTools
{
	public static final  Range<Integer> PWM_RANGE = new Range<Integer>(2, 4095);
	private static final transient Logger log = Logger.getLogger(GPIOTools.class.getName());

	// This statement must be the last static code
	public static final GPIOTools SINGLETON = new GPIOTools();
	private Lock lock = new ReentrantLock();
	private volatile GpioController gpioController = null;
	private  int pwmRangeValue;



	
	private GPIOTools() {
	  log.info("Wiring Pi setup:" + Gpio.wiringPiSetup());
	  gpioController = GpioFactory.getInstance();
	  setPWMRangeMod(4095, 0);
	}
	
	public GpioController getGpioController()
	{
		return gpioController;
	}

	public synchronized void resetPin(Pin pin)
	{
		GpioPin gpioPin = getGpioController().getProvisionedPin(pin);
		if(gpioPin != null)
		{
			getGpioController().unprovisionPin(gpioPin);
		}
	}

	public synchronized void setOutputPin(Pin pin, PinState state, long durationInMillis)
	{
		log.info(SharedUtil.toCanonicalID(',', Thread.currentThread(), pin, state,  durationInMillis));
		resetPin(pin);
		GpioPinDigitalOutput output = getGpioController().provisionDigitalOutputPin(pin, state);
		output.setShutdownOptions(false);
		output.setState(state);
		if(durationInMillis > 0)
		{
			TaskUtil.defaultTaskScheduler().queue(durationInMillis, new SupplierTask<GpioPinDigitalOutput>(output)
		    {
				@Override
				public void run() {
					GpioPinDigitalOutput gpdo = get();
					PinState toBeSet = PinState.getInverseState(gpdo.getState());
					gpdo.setState(toBeSet);
					log.info(gpdo.getName() + " set to " + toBeSet);
				}
			});
		}
	}
	
	public synchronized void setOutputPinState(Pin pin, PinState state, boolean persist, long durationInMillis, boolean delay)
	{
		log.info(SharedUtil.toCanonicalID(',', Thread.currentThread(), pin, state, persist, durationInMillis));
		//synchronized(pin)
		{

			resetPin(pin);
			
			if (delay && durationInMillis > 0 )
			{
			  try {
                Thread.sleep(durationInMillis);
              } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
			}
			
			GpioPinDigitalOutput output = getGpioController().provisionDigitalOutputPin(pin, state);
	
			if (persist)
				output.setShutdownOptions(false, state);
			output.setState(state);
			
			if (durationInMillis > 0 && !delay)
			{
				try {
					Thread.sleep(durationInMillis);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// revert to the back state
				
				state = PinState.getInverseState(state);
				if (persist)
					output.setShutdownOptions(false, state);
				output.setState(state);
			}
		}
		
	}

	public synchronized void setInputPin(GPIOPin ...gpios)
	{
		for(GPIOPin gpio : gpios)
		{
			getGpioController().provisionDigitalInputPin(gpio.getValue());
		}
	}

	public synchronized void setInputPin(PinPullResistance ppr, GPIOPin ...gpios)
	{
		for(GPIOPin gpio : gpios)
		{
			getGpioController().provisionDigitalInputPin(gpio.getValue(), ppr);
		}
	}

	public synchronized void setPWMRangeMod(int range, int mod)
	{
		if(!PWM_RANGE.within(range))
			throw new IllegalArgumentException(range + " value out of range [2-4095]");
		this.pwmRangeValue = range;
		Gpio.pwmSetMode(Gpio.PWM_MODE_MS);
	}

	public int getPWMRange()
	{
		return pwmRangeValue;
	}


	public synchronized GpioPinPwmOutput setPWM(Pin pin, float frequency, Range<Float> dutyCycle, long cycleDelay, int repeat)
	{
		log.info(SharedUtil.toCanonicalID(',', pin, frequency, dutyCycle, cycleDelay, repeat));

		if(dutyCycle.getStart() <0 || dutyCycle.getEnd() > 100)
		{
			throw new IllegalArgumentException(dutyCycle + " duty cycle out of range [0-100]");
		}
		resetPin(pin);
		GpioPinPwmOutput pwmOutputPin = getGpioController().provisionPwmOutputPin(pin);


		float clock = 19200000/(frequency * (float)getPWMRange());
		float calculatedFreq = 19200000/(clock * (float)getPWMRange());

		log.info("Clock:" +  clock + ", pwm-range:" + getPWMRange());
		log.info("Freq:" + frequency +", calculated-freq:" + calculatedFreq);


		// setup the clock divider
		Gpio.pwmSetClock((int)clock);
		pwmOutputPin.setPwmRange(getPWMRange());

		TaskUtil.defaultTaskScheduler().queue(0, new PWMRangeExec(pwmOutputPin, new Range<Integer>(dutyCycleToPWM(dutyCycle.getStart()), dutyCycleToPWM(dutyCycle.getEnd())), cycleDelay, repeat) );


		return pwmOutputPin;
	}

	public int dutyCycleToPWM(float dutyCycle)
	{
		float pwm = ((float)getPWMRange() *dutyCycle)/100;
		return (int)pwm;
	}

	public synchronized GpioPinPwmOutput setPWM(Pin pin, float frequency, float dutyCycle, long duration)
	{
		log.info(SharedUtil.toCanonicalID(',', pin, frequency, dutyCycle, TimeInMillis.toString(duration)));

		if(dutyCycle <0 || dutyCycle > 100)
		{
			throw new IllegalArgumentException(dutyCycle + " duty cycle out of range [0-100]");
		}
		resetPin(pin);
		GpioPinPwmOutput pwmOutputPin = getGpioController().provisionPwmOutputPin(pin);


		float clock = 19200000/(frequency * (float)getPWMRange());
		float calculatedFreq = 19200000/(clock * (float)getPWMRange());
		int pwm = dutyCycleToPWM(dutyCycle);
		log.info("Clock:" +  clock + ", pwm-range:" + getPWMRange() + ", pwm:" + pwm);
		log.info("Freq:" + frequency +", calculated-freq:" + calculatedFreq);


		// setup the clock divider
		Gpio.pwmSetClock((int)clock);
		pwmOutputPin.setPwmRange(getPWMRange());
		pwmOutputPin.setPwm(pwm);

		if(duration > 0) {
			TaskUtil.defaultTaskScheduler().queue(duration, () -> {
				try{
					pwmOutputPin.setPwm(0);
					pwmOutputPin.removeAllListeners();
					log.info("PWM:" + pwmOutputPin.getName() + " set to 0" );
				}
				catch (Exception e)
				{
					log.info("Error setting pwm pin " + pwmOutputPin.getName() + " to 0.");
				}

			});
		}
		return pwmOutputPin;
	}

	public synchronized long setPWM(PWMConfig pwmConfig)
	{
		 GPIOPin[] all = pwmConfig.getGPIOPins();

		 List<GpioPinPwmOutput> outputs = new ArrayList<GpioPinPwmOutput>();
		 for(GPIOPin pin : all)
		 {
		 	resetPin(pin.getValue());
		 	outputs.add(getGpioController().provisionPwmOutputPin(pin.getValue()));
		 }

		float pwm = (getPWMRange() *pwmConfig.getDutyCycle())/100;
		float clock = 19200000/(pwmConfig.getFrequency() * getPWMRange());
		log.info("Clock:" +  clock + ", pwm:" + pwm + ", duration:" + TimeInMillis.toString(pwmConfig.getDuration()));


		Gpio.pwmSetClock((int)clock);


		// dutycycle is the in percentage of the frequency
		// in RPI is set via a int range 2-4085
		// configDutyCycle is percentile 0-100 %
		// conversion formula = (range*configDutyCycle)/100

		long delta = System.currentTimeMillis();
		outputs.forEach((pwmPin) -> {
			pwmPin.setPwmRange(getPWMRange());
			pwmPin.setPwm((int) pwm);
		});


		outputs.forEach((pwmPin)-> log.info(pwmPin.getName()+ " pwm set to :"+pwmPin.getPwm()));

		 if(pwmConfig.getDuration() > 0) {
			 TaskUtil.defaultTaskScheduler().queue(pwmConfig.getDuration(), () -> {
					outputs.forEach((gppo)->{
						try{
							gppo.setPwm(0);
							log.info("PWM:" + gppo.getName() + " set to 0" );
						}
						catch (Exception e)
						{
							log.info("Error setting pwm pin " + gppo.getName() + " to 0.");
						}
					});
			 });
		 }
		 delta = System.currentTimeMillis() - delta;
		 log.info("It took " + TimeInMillis.toString(delta));


		return pwmConfig.getDuration();
	}
	
	
	public synchronized PinState getPinState(GPIOPin pin)
	{
	  return Gpio.digitalRead(pin.getBCMID()) == 1 ? PinState.HIGH : PinState.LOW;
	}



	
	public static void main(String ...args)
	{
		try
		{
			LoggerUtil.enableDefaultLogger("io.xlogistx");
			int index = 0;
			long durationBeforeExit = -1;
			long delta = System.currentTimeMillis();
			NVCollectionStringDecoder decoder = new NVCollectionStringDecoder("=", "," ,true);
			IOAction action = null;
			for (; index < args.length; index++) {
				if (index == 0) {
					action = IOAction.lookup(args[index]);
					System.out.println("Action:" + action);
					if (action != null)
						continue;
					else
						action = IOAction.SET;
				}
				GPIOPin gpioPin = null;
				switch (action) {
					case READ:
						GPIOPin[] gpioPins = GPIOPin.lookup(args[index]);
						for(GPIOPin gpioP: gpioPins) {
							System.out.println(gpioP + ", " + SINGLETON.getPinState(gpioP));
						}
						break;
					case READ_AS_INPUT:
						gpioPins = GPIOPin.lookup(args[index]);
						for(GPIOPin gpioP: gpioPins) {
							SINGLETON.setInputPin(gpioP);
							System.out.println(gpioP + ", " + SINGLETON.getPinState(gpioP));
						}
						break;
					case SET_PULL_DOWN:
						gpioPins = GPIOPin.lookup(args[index]);
						for(GPIOPin gpioP: gpioPins) {
							SINGLETON.setInputPin(PinPullResistance.PULL_DOWN, gpioP);
							System.out.println(gpioP + ", " + SINGLETON.getPinState(gpioP));
						}
						break;
					case SET_PULL_UP:
						gpioPins = GPIOPin.lookup(args[index]);
						for(GPIOPin gpioP: gpioPins) {
							SINGLETON.setInputPin(PinPullResistance.PULL_UP, gpioP);
							System.out.println(gpioP + ", " + SINGLETON.getPinState(gpioP));
						}
						break;
					case MONITOR:
						TaskUtil.defaultTaskScheduler();
						boolean inverse = false;

						String pins[] = args[index].split(Pattern.quote(","));
						if("-i".equalsIgnoreCase(pins[0]))
						{
							inverse = true;
							pins = Arrays.copyOfRange(pins, 1, pins.length);
						}


						gpioPins = GPIOPin.lookup(pins);
						log.info("to monitor:" + pins[0]);
						gpioPin = gpioPins[0];
						ArrayList<GPIOPin> toSet = new ArrayList<GPIOPin>();

						for (int p = 1; p < gpioPins.length; p++)
						{
							toSet.add(gpioPins[p]);
						}
						GpioPinDigitalInput input = SINGLETON.getGpioController()
								.provisionDigitalInputPin(gpioPin.getValue());
						GPIOConfig gm = new GPIOConfig().monitorSetter(gpioPin).followersSetter(toSet.toArray(new GPIOPin[0])).followersHighDelaySetter("10sec").followersLowDelaySetter("0sec").nameSetter(gpioPin.toString()).inverseSetter(inverse);

						System.out.println(GSONUtil.toJSONDefault(gm));
						PinStateMonitor psm = new PinStateMonitor(gm, null, TaskUtil.defaultTaskScheduler());

						input.addListener(psm);

						psm.setEnabled(true);
						//input.addListener(new PinStateListener(toSet.toArray(new GPIOPin[0])));
						break;
					case STATE_MONITOR:
						log.info("State monitor");

						PinStateMachine pinStateMachine = new PinStateMachine(TaskUtil.defaultTaskScheduler());
						pinStateMachine.start(true);

						GPIOPin.GPIONameMap gpioNameMap = GPIOPin.toGPIONameMap(args[index]);

						pinStateMachine.monitorDigitalPin(PinPullResistance.PULL_DOWN, args[index], gpioNameMap != null ? gpioNameMap.nameMap : "pod-counter");

						break;
					case SET:
						NVCollection<String> param = decoder.decode(args[index]);

						Pin pin = GPIOPin.lookupPin(param.getName());

						List<String> values = param.asList();
						int valuesIndex = 0;
						PinState state = PinState.getState(Bool.lookupValue(values.get(valuesIndex++)));
						//	SharedUtil.lookupEnum(values.get(valuesIndex++), PinState.values());

						boolean persist = values.size() > valuesIndex ? Bool.lookupValue(values.get(valuesIndex++)) : false;

						String waiting = values.size() > valuesIndex ? values.get(valuesIndex++) : null;
						boolean delay = false;
						if (waiting != null) {
							if (waiting.startsWith("d")) {
								delay = true;
								waiting = waiting.substring(1);
							}
						}

						long millis = waiting != null ? TimeInMillis.toMillis(waiting) : 0;
						log.info(pin.getName() + " set to " + values + " for " + millis + " millis");
						SINGLETON.setOutputPinState(pin, state, persist, millis, delay);
						break;
					case PWM:
						String jsonCmd = args[index];
						PWMConfig pwmConfig = GSONUtil.fromJSONDefault(IOUtil.inputStreamToString(jsonCmd), PWMConfig.class);
						durationBeforeExit = SINGLETON.setPWM(pwmConfig);
						break;
					case FLOW:
						jsonCmd = args[index++];
						String jsonConfig = IOUtil.inputStreamToString(jsonCmd);
						log.info("Flow Config: " + jsonConfig);
						TaskSchedulerProcessor tsp = null;
						if (args.length > index)
						{
							if (args[index++].equalsIgnoreCase("-m"))
							{
								tsp = TaskUtil.defaultTaskScheduler();
								log.info("Parallel scheduler");
							}
						}
						if ( tsp == null )
						{
							tsp = TaskUtil.simpleTaskScheduler();
							log.info("Single threaded scheduler");
						}
						PinStateMonitorConfig pinStateMonitorConfig = GSONUtil.fromJSONDefault(jsonConfig, PinStateMonitorConfig.class);
						new GPIOFlowProcessor(pinStateMonitorConfig, tsp).init();

						break;
				}
			}


			delta = System.currentTimeMillis() - delta;
			log.info("It took : " + TimeInMillis.toString(delta));
			if(durationBeforeExit >= 0) {
				TaskUtil.sleep(durationBeforeExit);
				TaskUtil.close();
			}

		}
		catch(Exception e)
		{
		    e.printStackTrace();
		    System.err.println("usage: [GPIO=Low/High,true/false,[duration]...");
		}
	}



	
}

package io.xlogistx.iot.gpio;



import com.pi4j.io.gpio.*;

import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import java.util.regex.Pattern;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.logging.LoggerUtil;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.util.Const.Bool;
import org.zoxweb.shared.util.Const.TimeInMillis;
import org.zoxweb.shared.util.NVCollection;
import org.zoxweb.shared.util.NVCollectionStringDecoder;
import org.zoxweb.shared.util.SharedUtil;

import com.pi4j.wiringpi.Gpio;



public class GPIOTools 
{
	static {
		LoggerUtil.enableDefaultLogger("io.xlogistx");
	}
	private static final transient Logger log = Logger.getLogger(GPIOTools.class.getName());
	public static final GPIOTools SINGLETON = new GPIOTools();
	private Lock lock = new ReentrantLock();
	private volatile GpioController gpioController = null;
	private static final Logger Log = Logger.getLogger(GPIOTools.class.getName());

	
	private GPIOTools() {
	  log.info("Wiring Pi setup:" + Gpio.wiringPiSetup());
	}
	
	public GpioController getGpioController()
	{
		if (gpioController == null)
		{
			try
			{
				lock.lock();
				if(gpioController == null)
				{
					gpioController = GpioFactory.getInstance();
				}
			}
			finally
			{
				lock.unlock();
			}
		}
		
		return gpioController;
	}
	
	public synchronized void setOutputPinState(Pin pin, PinState state, boolean persist, long durationInMillis, boolean delay)
	{		
		log.info(SharedUtil.toCanonicalID(',', Thread.currentThread(), pin, state, persist, durationInMillis));
		//synchronized(pin)
		{
			GpioPin gpioPin = SINGLETON.getGpioController().getProvisionedPin(pin);
			if(gpioPin != null)
			{
				SINGLETON.getGpioController().unprovisionPin(gpioPin);
			}
			
			if (delay && durationInMillis > 0 )
			{
			  try {
                Thread.sleep(durationInMillis);
              } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
			}
			
			GpioPinDigitalOutput output = SINGLETON.getGpioController().provisionDigitalOutputPin(pin, state);
	
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

	public void setInputPin(GPIOPin ...gpios)
	{
		for(GPIOPin gpio : gpios)
		{
			getGpioController().provisionDigitalInputPin(gpio.getValue());
		}
	}


	public long runPWD(PWMConfig pwmConfig)
	{
		 float cycleDuration = 1/pwmConfig.getFrequency();
		 float dutyCycleDuration = (cycleDuration*pwmConfig.getDutyCycle())/100;
		 float lowDuration = cycleDuration - dutyCycleDuration;
		 log.info(SharedUtil.toCanonicalID(',', cycleDuration,dutyCycleDuration));
		 GPIOPin[] all = pwmConfig.getGPIOPins();

		 List<GpioPinDigitalOutput> outputs = new ArrayList<GpioPinDigitalOutput>();
		 for(GPIOPin pin : all)
		 {
		 	outputs.add(SINGLETON.getGpioController().provisionDigitalOutputPin(pin.getValue()));
		 }

		 long delta = System.currentTimeMillis();
		 for(int i=0; i < pwmConfig.getCount(); i++)
		 {
			 outputs.forEach((n)->n.setState(PinState.LOW));
			 TaskUtil.sleep((long)(lowDuration*1000));
			 outputs.forEach((n)->n.setState(PinState.HIGH));
			 TaskUtil.sleep((long)(dutyCycleDuration*1000));
		 }
		 delta = System.currentTimeMillis() - delta;
		 if(pwmConfig.getLastState() != null)
			 for(GpioPinDigitalOutput gpdo : outputs)
				 gpdo.setState(pwmConfig.getLastState());

		return delta;
	}
	
	
	public synchronized PinState getPinState(GPIOPin pin)
	{
	  return Gpio.digitalRead(pin.getBCMID()) == 1 ? PinState.HIGH : PinState.LOW;
	}
	

	
	public static void main(String ...args)
	{
		try
		{
			int index = 0;
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
					case MONITOR:
						TaskUtil.getDefaultTaskScheduler();
						String pins[] = args[index].split(Pattern.quote(","));
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
						GPIOMonitor gm = new GPIOMonitor().setMonitor(gpioPin).setFollowers(toSet.toArray(new GPIOPin[0])).setFollowersHighDelay("6sec").setFollowersLowDelay("0sec");
						System.out.println(GSONUtil.DEFAULT_GSON.toJson(gm));
						input.addListener(new PinStateMonitor(gm, true));
						//input.addListener(new PinStateListener(toSet.toArray(new GPIOPin[0])));
						break;
					case SET:
						NVCollection<String> param = decoder.decode(args[index]);

						Pin pin = GPIOPin.lookupPin(param.getName());

						List<String> values = param.asList();
						int valuesIndex = 0;
						PinState state = PinState.getState(Bool.lookupValue(values.get(valuesIndex++)));
						//	SharedUtil.lookupEnum(values.get(valuesIndex++), PinState.values());

						boolean persist =
								values.size() > valuesIndex ? Bool.lookupValue(values.get(valuesIndex++)) : false;

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
						PWMConfig pwmConfig = GSONUtil.DEFAULT_GSON.fromJson(IOUtil.inputStreamToString(jsonCmd), PWMConfig.class);
						long duration = SINGLETON.runPWD(pwmConfig);
						log.info("It took " + TimeInMillis.toString(duration));
						break;
				}
			}


			delta = System.currentTimeMillis() - delta;
			log.info("It took : " + TimeInMillis.toString(delta));
		}
		catch(Exception e)
		{
		    e.printStackTrace();
		    System.err.println("usage: [GPIO=Low/High,true/false,[duration]...");
		}
	}
	
}

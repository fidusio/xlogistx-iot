package io.xlogistx.iot.gpio;


import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.zoxweb.shared.util.Const.Bool;
import org.zoxweb.shared.util.Const.TimeInMillis;
import org.zoxweb.shared.util.NVCollection;
import org.zoxweb.shared.util.NVCollectionStringDecoder;
import org.zoxweb.shared.util.SharedUtil;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.wiringpi.Gpio;


public class GPIOTools 
{
	private static final transient Logger log = Logger.getLogger(GPIOTools.class.getName());
	public static final GPIOTools SINGLETON = new GPIOTools();
	private Lock lock = new ReentrantLock();
	private volatile GpioController gpioController = null;
	
	
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
		log.info(SharedUtil.toCanonicalID(',', pin, state, persist, durationInMillis));
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
	
	
	public synchronized boolean getPinState(GPIOPin pin)
	{
	  return Gpio.digitalRead(pin.getBCMID()) == 1;
	}
	

	
	public static void main(String ...args)
	{
		try
		{
			int index = 0;
			
			NVCollectionStringDecoder decoder = new NVCollectionStringDecoder("=", "," ,true);
			boolean read = false;
			for (; index < args.length; index++)
			{
			    if (index == 0)
			      if (args[index].equalsIgnoreCase("-r"))
			      {
			        read = true;
			        continue;
			      }
				

				if(!read)
				{
					NVCollection<String> param = decoder.decode(args[index]);

					Pin pin = GPIOPin.lookupPin(param.getName());

    				List<String> values = param.asList();
    				int valuesIndex = 0;
    				PinState state = SharedUtil.lookupEnum(values.get(valuesIndex++), PinState.values());
    
    				
    				
    				boolean persist = values.size() > valuesIndex ? Bool.lookupValue(values.get(valuesIndex++)) : false;
    				
    				
    				String waiting = values.size() > valuesIndex ? values.get(valuesIndex++) : null;
    				boolean delay = false;
    				if (waiting != null)
    				{
    				  if (waiting.startsWith("d"))
    				  {
    				    delay = true;
    				    waiting = waiting.substring(1);
    				  }
    				}
				
  				  long millis = waiting != null ? TimeInMillis.toMillis(waiting) : 0;
  				  System.out.println(pin.getName() + " set to " + values + " for " + millis + " millis");
  				  SINGLETON.setOutputPinState(pin, state, persist, millis, delay);
				}
				else
				{

					GPIOPin gpioPin = GPIOPin.lookup(args[index]);
				  System.out.println(gpioPin +", " + SINGLETON.getPinState(gpioPin));
				}
			
			}
			
//			Collection<GpioPin> allPins = SINGLETON.getGpioController().getProvisionedPins();
//			for (GpioPin pin : allPins)
//			{
//			  System.out.println(pin.getClass().getName() + ", " + pin);
//			}
			
			
		}
		catch(Exception e)
		{
		    e.printStackTrace();
		    System.out.println("usage: [GPIO=Low/High,true/false,[duration]...");
		}
	}
	
}

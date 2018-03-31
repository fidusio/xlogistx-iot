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

public class GPIOTools 
{
	private static final transient Logger log = Logger.getLogger(GPIOTools.class.getName());
	public static final GPIOTools SINGLETON = new GPIOTools();
	private Lock lock = new ReentrantLock();
	private volatile GpioController gpioController = null;
	
	
	private GPIOTools() {}
	
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
	
	public void setOutputPinState(Pin pin, PinState state, boolean persist, long durationInMillis)
	{
		
		log.info(SharedUtil.toCanonicalID(',', pin, state, persist, durationInMillis));
		
		GpioPin gpioPin = SINGLETON.getGpioController().getProvisionedPin(pin);
		if(gpioPin != null)
		{
			SINGLETON.getGpioController().unprovisionPin(gpioPin);
		}
		
		GpioPinDigitalOutput output = SINGLETON.getGpioController().provisionDigitalOutputPin(pin, state);

		if (persist)
			output.setShutdownOptions(false, state);
		output.setState(state);
		
		if (durationInMillis > 0)
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
	
	public static void main(String ...args)
	{
		try
		{
			int index = 0;
			
			NVCollectionStringDecoder decoder = new NVCollectionStringDecoder("=", "," ,true);
			for (; index < args.length; index++)
			{
				
				NVCollection<String> param = decoder.decode(args[index]);
				
				Pin pin = GPIOPin.lookupPin(param.getName());
				List<String> values = param.asList();
				int valuesIndex = 0;
				PinState state = SharedUtil.lookupEnum(PinState.values(), values.get(valuesIndex++));
				
				boolean persist = values.size() > valuesIndex ? Bool.lookupValue(values.get(valuesIndex++)) : false;
				long millis = values.size() > valuesIndex ? TimeInMillis.toMillis(values.get(valuesIndex++)) : 0;
				System.out.println(pin.getName() + " set to " + values + " for " + millis + " millis");
				SINGLETON.setOutputPinState(pin, state, persist, millis);
			
			}
			
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}

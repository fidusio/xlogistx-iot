package io.xlogistx.iot.gpio;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.zoxweb.shared.util.NVCollection;
import org.zoxweb.shared.util.NVCollectionStringDecoder;
import org.zoxweb.shared.util.SharedUtil;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

public class GPIOTools 
{
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
	
	public void setOutputPinState(Pin pin, PinState state, boolean permanent)
	{
		GpioPinDigitalOutput output = SINGLETON.getGpioController().provisionDigitalOutputPin(pin, state);
		if (permanent)
			output.setShutdownOptions(false, state);
		
		output.setState(state);
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
				List<String> values = (List<String>) param.getValue();
				PinState state = SharedUtil.lookupEnum(PinState.values(), values.get(0));
				
				boolean persist = values.size() > 1 ? Boolean.getBoolean(values.get(1)) : false;
				SINGLETON.setOutputPinState(pin, state, persist);
				System.out.println(pin.getName() + " set to " + values);
			}
			
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}

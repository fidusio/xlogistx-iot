package io.xlogistx.iot.gpio;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	
	public void setOutputPinState(Pin pin, PinState state)
	{
		GpioPinDigitalOutput output = SINGLETON.getGpioController().provisionDigitalOutputPin(pin, state);
		output.setShutdownOptions(false, state);
		output.setState(state);
	}
	
	public static void main(String ...args)
	{
		try
		{
			int index = 0;
			PinState state = SharedUtil.lookupEnum(PinState.values(), args[index++]);
			for (; index< args.length; index++)
			{
				Pin pin = GPIOPin.lookupPin(args[index]);
				SINGLETON.setOutputPinState(pin, state);
				System.out.println(pin.getName() + " set to " + state);
			}
			
			
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}

package io.xlogistx.iot.gpio;

import static org.junit.Assert.*;

import org.junit.Test;

import com.pi4j.io.gpio.Pin;

public class PinTest {

	@Test
	public void lookupTest() {
		String[] pinIDs = {
			"GPIO-1",
			"2",
			"GPIO_03",
		};
		
		for(String pinID : pinIDs)
		{
			Pin p = GPIOPin.lookupPin(pinID);
			
			assertNotNull(p);
		}
	}

}

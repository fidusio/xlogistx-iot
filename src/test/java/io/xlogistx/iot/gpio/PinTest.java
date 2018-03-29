package io.xlogistx.iot.gpio;

import static org.junit.Assert.*;

import org.junit.Test;
import org.zoxweb.shared.util.Const.TimeInMillis;
import org.zoxweb.shared.util.NVCollection;
import org.zoxweb.shared.util.NVCollectionStringDecoder;

import com.pi4j.io.gpio.Pin;

public class PinTest {

	@Test
	public void lookupTest() {
		String[] pinIDs = {
			"GPIO-1=low,true,100millis",
			"2=low,true",
			"GPIO_03=low,true,5seconds",
		};
		NVCollectionStringDecoder decoder = new NVCollectionStringDecoder("=", "," ,true);
		for(String pinID : pinIDs)
		{
			
			NVCollection<String> param = decoder.decode(pinID);
			System.out.println(param);
			Pin p = GPIOPin.lookupPin(param.getName());
			if (param.getValue().size() == 3)
			{
				
				System.out.println(TimeInMillis.toMillis(param.asList().get(2)));
			}
			
			assertNotNull(p);
		}
	}

}

/*
 * Copyright (c) 2012-2014 ZoxWeb.com LLC.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.xlogistx.iot.gpio;


import org.zoxweb.shared.util.GetName;
import org.zoxweb.shared.util.GetValue;
import org.zoxweb.shared.util.SharedStringUtil;
import org.zoxweb.shared.util.SharedUtil;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

public enum GPIOPin
implements GetValue<Pin>, GetName

{
	GPIO_00(RaspiPin.GPIO_00, 0),
	GPIO_01(RaspiPin.GPIO_01, 1),
	GPIO_02(RaspiPin.GPIO_02, 2),
	GPIO_03(RaspiPin.GPIO_03, 3),
	GPIO_04(RaspiPin.GPIO_04, 4),
	GPIO_05(RaspiPin.GPIO_05, 5),
	GPIO_06(RaspiPin.GPIO_06, 6),
	GPIO_07(RaspiPin.GPIO_07, 7),
	GPIO_08(RaspiPin.GPIO_08, 8),
	GPIO_09(RaspiPin.GPIO_09, 9),
	GPIO_10(RaspiPin.GPIO_10, 10),
	GPIO_11(RaspiPin.GPIO_11, 11),
	GPIO_12(RaspiPin.GPIO_12, 12),
	GPIO_13(RaspiPin.GPIO_13, 13),
	GPIO_14(RaspiPin.GPIO_14, 14),
	GPIO_15(RaspiPin.GPIO_15, 15),
	GPIO_16(RaspiPin.GPIO_16, 16),
	GPIO_17(RaspiPin.GPIO_17, 17),
	GPIO_18(RaspiPin.GPIO_18, 18),
	GPIO_19(RaspiPin.GPIO_19, 19),
	GPIO_20(RaspiPin.GPIO_20, 20),
	GPIO_21(RaspiPin.GPIO_21, 21),
	GPIO_22(RaspiPin.GPIO_22, 22),
	GPIO_23(RaspiPin.GPIO_23, 23),
	GPIO_24(RaspiPin.GPIO_24, 24),
	GPIO_25(RaspiPin.GPIO_25, 25),
	GPIO_26(RaspiPin.GPIO_26, 26),
	GPIO_27(RaspiPin.GPIO_27, 27),
	GPIO_28(RaspiPin.GPIO_28, 28),
	GPIO_29(RaspiPin.GPIO_29, 29),
	GPIO_30(RaspiPin.GPIO_30),
	GPIO_31(RaspiPin.GPIO_31)
	;
	private final Pin PIN;
	private final int bcmPinID;
	GPIOPin(Pin p)
	{
		this(p, -1);
	}
	GPIOPin(Pin p, int bcmId)
    {
        PIN=p;
        bcmPinID = bcmId;
    }
	
	
	public int getBCMID()
	{
	  return bcmPinID;
	}
	@Override
	public Pin getValue() {
		// TODO Auto-generated method stub
		return PIN;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return PIN.getName();
	}
	
	
	public static GPIOPin lookup(String pinID)
	{
		pinID = SharedStringUtil.trimOrNull(pinID);
		GPIOPin ret = null;
		if (pinID != null)
		{
			pinID = pinID.replace('-', '_');
			String split[] = pinID.split("_");
			if (split.length == 2)
			{
				try
				{
					int n = Integer.parseInt(split[1]);
					split[1] = String.format("%02d", n);
					pinID = split[0] + "_" + split[1];
				}
				catch(Exception e)
				{
					
				}
			}
			ret = SharedUtil.lookupEnum(pinID, values());
		}
		if (ret == null)
		{
			try
			{
				int index = Integer.parseInt(pinID);
				if (index>-1 && index < values().length)
				{
					ret = values()[index];
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return ret;
	}
	
	public static Pin lookupPin(String pinID)
	{
		GPIOPin ret = lookup(pinID);	
		return ret != null ? ret.getValue() : null;
	}
	
	public String toString()
	{
	  return name() + "-" + bcmPinID;
	  
	}
	
}

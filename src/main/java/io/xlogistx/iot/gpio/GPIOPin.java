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

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

public enum GPIOPin
implements GetValue<Pin>, GetName

{
	GPIO_00(RaspiPin.GPIO_00),
	GPIO_01(RaspiPin.GPIO_01),
	GPIO_02(RaspiPin.GPIO_02),
	GPIO_03(RaspiPin.GPIO_03),
	GPIO_04(RaspiPin.GPIO_04),
	GPIO_05(RaspiPin.GPIO_05),
	GPIO_06(RaspiPin.GPIO_06),
	GPIO_07(RaspiPin.GPIO_07),
	GPIO_08(RaspiPin.GPIO_08),
	GPIO_09(RaspiPin.GPIO_09),
	GPIO_10(RaspiPin.GPIO_10),
	GPIO_11(RaspiPin.GPIO_11),
	GPIO_12(RaspiPin.GPIO_12),
	GPIO_13(RaspiPin.GPIO_13),
	GPIO_14(RaspiPin.GPIO_14),
	GPIO_15(RaspiPin.GPIO_15),
	GPIO_16(RaspiPin.GPIO_16),
	GPIO_17(RaspiPin.GPIO_17),
	GPIO_18(RaspiPin.GPIO_18),
	GPIO_19(RaspiPin.GPIO_19),
	GPIO_20(RaspiPin.GPIO_20)	
	;
	private final Pin PIN;
	GPIOPin( Pin p)
	{
		PIN=p;
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
	
	
	
}

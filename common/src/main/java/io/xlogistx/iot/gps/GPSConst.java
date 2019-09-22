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

package io.xlogistx.iot.gps;

import org.zoxweb.shared.util.GetValue;

/**
 * This class is declared final because it contains constant values.
 * @author mzebib
 *
 */
public final class GPSConst 
{

	/**
	 * This class is declared private to prevent instantiation of this class.
	 */
	private GPSConst()
	{
		
	}
	
	/**
	 * This enum contains the Position Fix Indicator values.
	 * @author mzebib
	 *
	 */
	public enum PositionFixIndicator implements GetValue<String>
	{
		
		FIX_OR_INVALID("0"),
		GPS_SPS_MODE("1"),
		DIFFERENTIAL_GPS_SPS_MODE("2"),
		GPS_PPS_MODE("3");
		
		
		private final String value;
		
		PositionFixIndicator(String value)
		{
			this.value = value;
		}
		
		@Override
		public String getValue() 
		{
			
			return value;
		}
		
		
	}
	
	/**
	 * This enum contains the mode type used by the 
	 * message ID: GSA (GNSS DOP and Active Satellites).
	 * @author mzebib
	 *
	 */
	public enum ModeOne implements GetValue<String>
	{
		AUTOMATIC("A"),
		MANUAL("M");

		private final String value;
		
		ModeOne(String value)
		{
			this.value = value;
		}
		
		@Override
		public String getValue() 
		{
			
			return value;
		}
				
		
	}
	
	/**
	 * This enum contains the mode type used by the
	 * message ID: GSA (GNSS DOP and Active Satellites).
	 * @author mzebib
	 *
	 */
	public enum ModeTwo implements GetValue<String>
	{
		FIX_NOT_AVAILABLE("1"),
		GREATER_THAN_OR_EQUAL_TO_4SVs_USED("3"),
		LESS_THAN_4SVs_USED("2");
		
		private final String value;
		
		ModeTwo(String value)
		{
			this.value = value;
		}
		
		@Override
		public String getValue() 
		{
			
			return value;
		}
				
		
		
	}
	
	/**
	 * This enum contains the cardinal direction values:
	 * North, East, South, and West.
	 * @author mzebib
	 *
	 */
	public enum CardinalDirection implements GetValue<String>
	{
		EAST("E"),
		NORTH("N"),
		SOUTH("S"),
		WEST("W");
		
		private final String value;
		
		CardinalDirection(String value)
		{
			this.value = value;
		}
		
		@Override
		public String getValue() 
		{
			
			return value;
		}
				
		
		
	}
		
	/**
	 * This enum contains the status type used by the message ID: 
	 * RMC (Recommended Minimum Navigation Information).
	 * @author mzebib
	 *
	 */
	public enum NavigationStatus implements GetValue<String>
	{

		DATA_VALID("A"),
		DATA_NOT_VALID("V");
		
		private final String value;
		
		NavigationStatus(String value)
		{
			this.value = value;
		}
		
		@Override
		public String getValue() 
		{
			
			return value;
		}
				
		
	}
	
	/**
	 * This enum contains the mode type used by the message ID: 
	 * RMC (Recommended Minimum Navigation Information).
	 * @author mzebib
	 *
	 */
	public enum NavigationMode implements GetValue<String>
	{
		AUTONOMOUS_MODE("A"),
		DIFFERENTIAL_MODE("D"),
		ESTIMATED_MODE("E");
		
		private final String value;
		
		NavigationMode(String value)
		{
			this.value = value;
		}
		
		@Override
		public String getValue()
		{
			
			return value;
		}
				
		
	}
	
	/**
	 * This enum contains the values magnetic north
	 * and true north.
	 * @author mzebib
	 *
	 */
	public enum NorthType implements GetValue<String>
	{
		MAGNETIC("M"),
		TRUE("T");
		
		private final String value;
		
		NorthType(String value)
		{
			this.value = value;
		}
		
		@Override
		public String getValue()
		{
			return value;
		}
				
		
	}
	
	/**
	 * This enum contains two units of speed: knots
	 * and kilometers per hour.
	 * @author mzebib
	 *
	 */
	public enum GroundSpeedUnits implements GetValue<String>
	{
		KNOTS("N"),
		KM_PER_HOUR("K");
		
		private final String value;
		
		GroundSpeedUnits(String value)
		{
			this.value = value;
		}
		
		@Override
		public String getValue()
		{
			
			return value;
		}
				
		
	}
	
}

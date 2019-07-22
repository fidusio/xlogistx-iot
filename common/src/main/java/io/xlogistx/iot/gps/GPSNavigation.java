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

import static io.xlogistx.iot.gps.GPSConst.*;

import org.zoxweb.shared.util.GetNVConfig;
import org.zoxweb.shared.util.NVConfig;
import org.zoxweb.shared.util.NVConfigEntity;
import org.zoxweb.shared.util.NVConfigEntityLocal;
import org.zoxweb.shared.util.NVConfigManager;
import org.zoxweb.shared.util.SharedUtil;

/**
 * This class is used to define the message parameters for the message 
 * protocol header $GPRMC. RMC (Recommended Minimum Navigation Information)
 * describes time, date, position, course and speed data.
 * @author mzebib
 *
 */
@SuppressWarnings("serial")
public class GPSNavigation extends GPSMessage
{
	/**
	 * This enum contains the messages parameters and creates an NVConfig 
	 * object for each parameter which specifies the paramater's name, 
	 * description, display name, and type.
	 * @author mzebib
	 *
	 */
	private enum Params
	implements GetNVConfig
	{
		GPS_TIME( NVConfigManager.createNVConfig("gps_time", "Date and time","GPSTime",false, true, Long.class)),
		STATUS( NVConfigManager.createNVConfig("status", "A=data valid or V=data not valid","Status",false, true, NavigationStatus.class)),
		LATITUDE( NVConfigManager.createNVConfig("latitude", "Latitude in ddmm.mmmm format","Latitude",false, true, Float.class)),
		NS_INDICATOR( NVConfigManager.createNVConfig("ns_indicator", "North/South indicator (N=north or S=south)","NSIndicator",false, true, CardinalDirection.class)),
		LONGITUDE( NVConfigManager.createNVConfig("longitude", "Longitude in ddmm.mmmm format","Longitude",false, true, Float.class)),
		EW_INDICATOR( NVConfigManager.createNVConfig("ew_indicator", "East/West indicator (E=east or W=west)","EWIndicator",false, true, CardinalDirection.class)),
		SPEED_OVER_GROUND( NVConfigManager.createNVConfig("speed_over_ground", "Speed over ground measured in knots","SpeedOverGround",false, true, Float.class)),
		COURSE_OVER_GROUND( NVConfigManager.createNVConfig("course_over_ground", "Course over ground measured in degrees","CourseOverGround",false, true, float.class)),
		MAGNETIC_VARIATION_VALUE( NVConfigManager.createNVConfig("magnetic_variation_value", "Magnteic variation in degrees","MagneticVariationValue",false, true, Float.class)),
		MAGNETIC_VARIATION_DIRECTION( NVConfigManager.createNVConfig("magnetic_variation_direction", "Magnetic variation direction","MagneticVariationDirection",false, true, CardinalDirection.class)),
		MODE( NVConfigManager.createNVConfig("mode", "Mode Type: A=Autonomous mode, D=Differential mode, and E=Estimated mode","Mode",false, true, NavigationMode.class)),
	
	;
	
		private final NVConfig cType;
		Params( NVConfig c)
		{
			cType = c;
		}
		
		public NVConfig getNVConfig() 
		{
			// TODO Auto-generated method stub
			return cType;
		}
	}
	

	private static final NVConfigEntity  NVC_GPS_NAVIGATION = new NVConfigEntityLocal("gps_navigation", null , "GPSNavigation", true, false, false, false,
			GPSNavigation.class, SharedUtil.extractNVConfigs(Params.values()), SharedUtil.toNVConfigList(GPSMessage.NVC_MESSAGE_ID, 
																										Params.GPS_TIME.getNVConfig(),
																										Params.STATUS.getNVConfig(),
																										Params.LATITUDE.getNVConfig(),
																										Params.NS_INDICATOR.getNVConfig(),
																										Params.LONGITUDE.getNVConfig(),
																										Params.EW_INDICATOR.getNVConfig(),
																										Params.SPEED_OVER_GROUND.getNVConfig(),
																										Params.COURSE_OVER_GROUND.getNVConfig(),
																										Params.MAGNETIC_VARIATION_VALUE.getNVConfig(),
																										Params.MAGNETIC_VARIATION_DIRECTION.getNVConfig(),
																										Params.MODE.getNVConfig()																																																
																										),
																										false,
																										GPSMessage.NVC_GPS_MESSAGE);
	


	/**
	 * This constructor creates a GPSNavigation object
	 * with the message parameters list defined and the GPS 
	 * message type set as output.
	 */
	public GPSNavigation()
	{
		super(NVC_GPS_NAVIGATION);
		setMessageType(GPSMessageType.OUTPUT);
	}
	
	
	/**
	 * This method returns the GPS time parameter in the message.
	 * @return
	 */
    public long getGPSTime() 
    {
		return lookupValue(Params.GPS_TIME);
	}

    /**
     * This method sets the GPS time parameter in the message.
     * @param timeStamp
     */
	public void setGPSTime(long timeStamp) 
	{
		setValue(Params.GPS_TIME, timeStamp);
	}
	
	/**
	 * This method returns the status parameter in the message.
	 * @return
	 */
	public NavigationStatus getStatus() 
	{
		return lookupValue(Params.STATUS);
	}
	
	/**
	 * This method sets the status parameter in the message.
	 * @param status
	 */
	public void setStatus(NavigationStatus status) 
	{
		setValue(Params.STATUS, status);
	}
	
	/**
	 * This method returns the latitude parameter in the message.
	 * @return
	 */
	public float getLatitude() 
	{
		return lookupValue(Params.LATITUDE);
	}
	
	/**
	 * This method sets the latitude parameter in the message.
	 * @param latitude
	 */
	public void setLatitude(float latitude) 
	{
		setValue(Params.LATITUDE, latitude);
	}
	
	/**
	 * This method returns the north/south indicator parameter 
	 * in the message.
	 * @return
	 */
	public CardinalDirection getNSIndicator() 
	{
		return lookupValue(Params.NS_INDICATOR);
	}
	
	/**
	 * This method sets the north/south indicator parameter in
	 * the message.
	 * @param nsIndicator
	 */
	public void setNSIndicator(CardinalDirection nsIndicator) 
	{
		setValue(Params.NS_INDICATOR, nsIndicator);
	}
	
	/**
	 * This method returns the longitude parameter in the message.
	 * @return
	 */
	public float getLongitude() 
	{
		return lookupValue(Params.LONGITUDE);
	}
	
	/**
	 * This method sets the longitude parameter in the message.
	 * @param longitude
	 */
	public void setLongitude(float longitude) 
	{
		setValue(Params.LONGITUDE, longitude);
	}
	
	/**
	 * This method returns the east/west indicator parameter 
	 * in the message.
	 * @return
	 */
	public CardinalDirection getEWIndicator() 
	{
		return lookupValue(Params.EW_INDICATOR);
	}
	
	/**
	 * This method sets the east/west indicator parameter
	 * in the message.
	 * @param ewIndicator
	 */
	public void setEWIndicator(CardinalDirection ewIndicator) 
	{
		setValue(Params.EW_INDICATOR, ewIndicator);
	}
	
	/**
	 * This message returns the speed over ground (in knots)
	 * parameter in the message.
	 * @return
	 */
	public float getSpeedOverGround() 
	{
		return lookupValue(Params.SPEED_OVER_GROUND);
	}
	
	/**
	 * This method sets the speed over ground (in knots)
	 * parameter in the message.
	 * @param speedOverGround
	 */
	public void setSpeedOverGround(float speedOverGround) 
	{
		setValue(Params.SPEED_OVER_GROUND, speedOverGround);
	}
	
	/**
	 * This method returns the course over ground (in degrees)
	 * parameter in the message.
	 * @return
	 */
	public float getCourseOverGround() 
	{
		return lookupValue(Params.COURSE_OVER_GROUND);
	}
	
	/**
	 * This method sets the course over ground (in degrees) 
	 * parameter in the message.
	 * @param courseOverGround
	 */
	public void setCourseOverGround(float courseOverGround) 
	{
		setValue(Params.COURSE_OVER_GROUND, courseOverGround);
	}
	
	/**
	 * This method returns the magnetic variation (in degrees) 
	 * parameter in the message.
	 * @return
	 */
	public float getMagneticVariationValue() 
	{
		return lookupValue(Params.MAGNETIC_VARIATION_VALUE);
	}
	
	/**
	 * This method sets the magnetic variation (in degrees) 
	 * parameter in the message.
	 * @param magneticVariationValue
	 */
	public void setMagneticVariationValue(float magneticVariationValue) 
	{
		setValue(Params.MAGNETIC_VARIATION_VALUE, magneticVariationValue);
	}
	
	/**
	 * This method returns the magnetic variation direction parameter
	 * in the message.
	 * @return
	 */
	public CardinalDirection getMagneticVariationDirection() 
	{
		return lookupValue(Params.MAGNETIC_VARIATION_DIRECTION);
	}
	
	/**
	 * This method sets the magnetic variation direction parameter 
	 * in the message.
	 * @param magneticVariationDirection
	 */
	public void setMagneticVariationDirection(CardinalDirection magneticVariationDirection) 
	{
		setValue(Params.MAGNETIC_VARIATION_DIRECTION, magneticVariationDirection);
	}
	
	/**
	 * This method returns the mode parameter in the message.
	 * @return
	 */
	public NavigationMode getMode()
	{
		return lookupValue(Params.MODE);
	}
	
	/**
	 * This method sets the mode parameter in the message.
	 * @param mode
	 */
	public void setMode(NavigationMode mode) 
	{
		setValue(Params.MODE, mode);
	}
	
	

}

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
 * 
 * This class is used to define the message parameters for the message 
 * protocol header $GPVTG. VTG (Course Over Ground and Ground Speed)
 * describes the course and speed information relative to the ground.
 * @author mzebib
 *
 */
@SuppressWarnings("serial")
public class GPSCourseInfo extends GPSMessage
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
		
		COURSE_HEADING_1( NVConfigManager.createNVConfig("course_heading_1", "Measured heading in degrees","CourseHeading1",false, true, Float.class)),
		REFERENCE_1( NVConfigManager.createNVConfig("true_north_reference", "True north reference","TrueReference",false, true, NorthType.class)),
		COURSE_HEADING_2( NVConfigManager.createNVConfig("course_heading_2", "Measured heading in degrees","CourseHeading2",false, true, Float.class)),
		REFERENCE_2( NVConfigManager.createNVConfig("magnetic_north_reference", "Magnetic north reference","MagneticReference",false, true, NorthType.class)),
		SPEED_KNOTS( NVConfigManager.createNVConfig("speed_1", "Measured horizontal speed", "Speed1",false, true, Float.class)),
		SPEED_KNOTS_UNITS( NVConfigManager.createNVConfig("speed_knots", "Speed units in knots","SpeedKnots",false, true, GroundSpeedUnits.class)),
		SPEED_KM_PER_HOUR( NVConfigManager.createNVConfig("speed_2", "Measured horizontal speed", "Speed2",false, true, Float.class)),
		SPEED_KM_PER_HOUR_UNITS( NVConfigManager.createNVConfig("speed_km_per_hour", "Speed units in kilometer per hour","SpeedKMperHour",false, true, GroundSpeedUnits.class)),
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
	
	private static final NVConfigEntity  NVC_GPS_COURSE_INFO = new NVConfigEntityLocal("gps_course_info", null , "GPSCourseInfo", true, false, false, false,
			GPSCourseInfo.class, SharedUtil.extractNVConfigs(Params.values()), SharedUtil.toNVConfigList(GPSMessage.NVC_MESSAGE_ID, 
																										Params.COURSE_HEADING_1.getNVConfig(),
																										Params.REFERENCE_1.getNVConfig(),
																										Params.COURSE_HEADING_2.getNVConfig(),
																										Params.REFERENCE_2.getNVConfig(),
																										Params.SPEED_KNOTS.getNVConfig(),
																										Params.SPEED_KNOTS_UNITS.getNVConfig(),
																										Params.SPEED_KM_PER_HOUR.getNVConfig(),
																										Params.SPEED_KM_PER_HOUR_UNITS.getNVConfig(),
																										Params.MODE.getNVConfig()																																													
																										),
																										false,
																										GPSMessage.NVC_GPS_MESSAGE);
	
	/**
	 * This constructor creates a GPSCourseInfo object
	 * with the message parameters list defined and the GPS 
	 * message type set as output.
	 */
	public GPSCourseInfo()
	{
		super(NVC_GPS_COURSE_INFO);
		setMessageType(GPSMessageType.OUTPUT);
	}
	
	
	/**
	 * This method returns the course heading parameter in the message.
	 * @return
	 */
	public float getCourseHeadingOne() 
	{
		return lookupValue(Params.COURSE_HEADING_1);
	}
	
	/**
	 * This method sets the course heading parameter in the message.
	 * @param courseHeading
	 */
	public void setCourseHeadingOne(float courseHeading) 
	{
		setValue(Params.COURSE_HEADING_1, courseHeading);
	}
	
	/**
	 * This method returns the reference based on the course heading 
	 * parameter in the message.
	 * @return
	 */
	public NorthType getReferenceOne() 
	{
		return lookupValue(Params.REFERENCE_1);
	}
	
	/**
	 * This method sets the reference based on the course heading 
	 * parameter in the message.
	 * @param type
	 */
	public void setReferenceOne(NorthType type) 
	{
		setValue(Params.REFERENCE_1, type);
	}
	
	/**
	 * This method returns the course heading parameter in the message.
	 * @return
	 */
	public float getCourseHeadingTwo() 
	{
		return lookupValue(Params.COURSE_HEADING_2);
	}
	
	/**
	 * This method sets the course heading parameter in the message.
	 * @param courseHeading
	 */
	public void setCourseHeadingTwo(float courseHeading) 
	{
		setValue(Params.COURSE_HEADING_2, courseHeading);
	}
	
	/**
	 * This method returns the reference based on the course heading 
	 * parameter in the message.
	 * @return
	 */
	public NorthType getReferenceTwo() 
	{
		return lookupValue(Params.REFERENCE_2);
	}
	
	/**
	 * This method sets the reference based on the course heading 
	 * parameter in the message.
	 * @param type
	 */
	public void setReferenceTwo(NorthType type) 
	{
		setValue(Params.REFERENCE_2, type);
	}
	
	/**
	 * This method returns the speed (in knots) parameter in the message.
	 * @return
	 */
	public float getSpeedInKnots() 
	{
		return lookupValue(Params.SPEED_KNOTS);
	}
	
	/**
	 * This method sets the speed (in knots) parameter in the message.
	 * @param speed
	 */
	public void setSpeedInKnots(float speed) 
	{
		setValue(Params.SPEED_KNOTS, speed);
	}
	
	/**
	 * This method returns the units of the speed (in knots) parameter
	 * in the message.
	 * @return
	 */
	public GroundSpeedUnits getSpeedKnotsUnits() 
	{
		return lookupValue(Params.SPEED_KNOTS_UNITS);
	}
	
	/**
	 * This method sets the units of the speed (in knots) parameter
	 * in the message.
	 * @param units
	 */
	public void setSpeedKnotsUnits(GroundSpeedUnits units) 
	{
		setValue(Params.SPEED_KNOTS_UNITS, units);
	}
	
	/**
	 * This method returns the speed (in kilometers per hour) parameter
	 * in the message.
	 * @return
	 */
	public float getSpeedInKMperHour() 
	{
		return lookupValue(Params.SPEED_KM_PER_HOUR);
	}
	
	/**
	 * This method sets the speed (in kilometers per hour) parameter
	 * in the message.
	 * @param speed
	 */
	public void setSpeedInKMperHour(float speed) 
	{
		setValue(Params.SPEED_KM_PER_HOUR, speed);
	}
	
	/**
	 * This method returns the units of the speed (in kilometers per hour)
	 * parameter in the message.
	 * @return
	 */
	public GroundSpeedUnits getSpeedKMUnits() 
	{
		return lookupValue(Params.SPEED_KM_PER_HOUR_UNITS);
	}
	
	/**
	 * This method sets the units of the speed (in kilometers per hour) 
	 * parameter in the message.
	 * @param units
	 */
	public void setSpeedKMUnits(GroundSpeedUnits units) 
	{
		setValue(Params.SPEED_KM_PER_HOUR_UNITS, units);
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

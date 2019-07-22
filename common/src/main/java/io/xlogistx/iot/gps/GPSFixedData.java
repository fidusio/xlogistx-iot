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
 * protocol header $GPGGA. GGA (Global Positioning System Fixed Data) 
 * describes the time, position, and fix related data.
 * @author mzebib
 *
 */
@SuppressWarnings("serial")
public class GPSFixedData extends GPSMessage
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
		
		UTC_TIME( NVConfigManager.createNVConfig("utc_time", "UTC time in hhmmss.SSS format","UTCTime",false, true, Long.class)),
		LATITUDE( NVConfigManager.createNVConfig("latitude", "Latitude in ddmm.mmmm format","Latitude",false, true, Float.class)),
		NS_INDICATOR( NVConfigManager.createNVConfig("ns_indicator", "North/South indicator (N=north or S=south)","NSIndicator",false, true, CardinalDirection.class)),
		LONGITUDE( NVConfigManager.createNVConfig("longitude", "Longitude in ddmm.mmmm format","Longitude",false, true, Float.class)),
		EW_INDICATOR( NVConfigManager.createNVConfig("ew_indicator", "East/West indicator (E=east or W=west)","EWIndicator",false, true, CardinalDirection.class)),
		POSITION_FIX_INDICATOR( NVConfigManager.createNVConfig("position_fix_indicator", "Position fix indicator (0=Fix not available, 1=GPS SPS Mode, 2=Differential GPS, or 3=GPS PPS Mode)","PositionFixIndicator",false, true, PositionFixIndicator.class)),
		SATELLITES_USED( NVConfigManager.createNVConfig("satellites_used", "The number (0 to 14) of satellites used","SatellitesUsed",false, true, Integer.class)),
		HDOP( NVConfigManager.createNVConfig("hdop", "Horizontal dilution of precision","HDOP",false, true, Float.class)),
		MSL_ALTITUDE( NVConfigManager.createNVConfig("msl_altitude", "Antenna altitude above/below mean-sea-level in meters","MSLAltitude",false, true, Float.class)),
		MSL_ALTITUDE_UNITS( NVConfigManager.createNVConfig("msl_altitude_units", "Units of antenna altitude (M=meters)","MSLAltitudeUnits",false, true, String.class)),
		GEOIDAL_SEPARATION( NVConfigManager.createNVConfig("geoidal_separation", "Geoids seperation value in meters","GeoidalSeparation",false, true, Float.class)),
		GEOIDAL_SEPARATION_UNITS( NVConfigManager.createNVConfig("geoidal_separation_units", "Units of geoids separation (M=meters)","GeoidalSeparationUnits",false, true, String.class)),
		AGE_OF_DIFFERIANTAL_CORRECTION( NVConfigManager.createNVConfig("age_of_diff_corr", "Null fields when DGPS is not used (value in seconds)","AgeOfDifferentialCorrection",false, true, Integer.class)),
		DIFFERNTIAL_REFERENCE_STATION_ID( NVConfigManager.createNVConfig("diff_ref_station_id", "Reference station identifier","DiffRefStationID",false, true, String.class)),
	
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
	
	private static final NVConfigEntity  NVC_GPS_FIXED_DATA = new NVConfigEntityLocal("gps_fixed_data", null , "GPSFixedData", true, false, false, false,
			GPSFixedData.class, SharedUtil.extractNVConfigs(Params.values()), SharedUtil.toNVConfigList(GPSMessage.NVC_MESSAGE_ID, 
																										Params.UTC_TIME.getNVConfig(),
																										Params.LATITUDE.getNVConfig(),
																										Params.NS_INDICATOR.getNVConfig(),
																										Params.LONGITUDE.getNVConfig(),
																										Params.EW_INDICATOR.getNVConfig(),
																										Params.POSITION_FIX_INDICATOR.getNVConfig(),
																										Params.SATELLITES_USED.getNVConfig(),
																										Params.HDOP.getNVConfig(),
																										Params.MSL_ALTITUDE.getNVConfig(),
																										Params.MSL_ALTITUDE_UNITS.getNVConfig(),
																										Params.GEOIDAL_SEPARATION.getNVConfig(),
																										Params.GEOIDAL_SEPARATION_UNITS.getNVConfig(),
																										Params.AGE_OF_DIFFERIANTAL_CORRECTION.getNVConfig(),
																										Params.DIFFERNTIAL_REFERENCE_STATION_ID.getNVConfig()																																																
																										),
																										false,
																										GPSMessage.NVC_GPS_MESSAGE);
	
	/**
	 * This constructor creates a GPSFixedData object
	 * with the message parameters list defined and the GPS 
	 * message type set as output.
	 */
	public GPSFixedData()
	{
		super(NVC_GPS_FIXED_DATA);
		setMessageType(GPSMessageType.OUTPUT);
	}
	
	/**
	 * 	This method returns the UTC time parameter in the message.
	 * @return
	 */
	public long getUtcTime() 
	{
		return lookupValue(Params.UTC_TIME);
	}
	
	/**
	 * This method sets the UTC time parameter in the message.
	 * @param utcTime
	 */
	public void setUtcTime(long utcTime) 
	{
		setValue(Params.UTC_TIME, utcTime);
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
	 * This method sets the north/south indicator parameter 
	 * in the message.
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
	 * This method returns the position fix indicator parameter 
	 * in the message.
	 * @return
	 */
	public PositionFixIndicator getPositionFixIndicator() 
	{
		return lookupValue(Params.POSITION_FIX_INDICATOR);
	}
	
	/**
	 * This method sets the position fix indicator parameter
	 * in the message.
	 * @param positionFixIndicator
	 */
	public void setPositionFixIndicator(PositionFixIndicator positionFixIndicator) 
	{
		setValue(Params.POSITION_FIX_INDICATOR, positionFixIndicator);
	}
	
	/**
	 * This method returns the satellites used parameter in
	 * the message.
	 * @return
	 */
	public int getSatellitesUsed() 
	{
		return lookupValue(Params.SATELLITES_USED);
	}
	
	/**
	 * This method sets the satellites used parameter in 
	 * the message.
	 * @param satellitesUsed
	 */
	public void setSatellitesUsed(int satellitesUsed) 
	{
		setValue(Params.SATELLITES_USED, satellitesUsed);
	}
	
	/**
	 * This method returns the HDOP parameter in the message.
	 * @return
	 */
	public float getHDOP() 
	{
		return lookupValue(Params.HDOP);
	}
	
	/**
	 * This method sets the HDOP parameter in the message.
	 * @param hdop
	 */
	public void setHDOP(float hdop) 
	{
		setValue(Params.HDOP, hdop);
	}
	
	/**
	 * This method returns the MSL altitude parameter
	 * in the message.
	 * @return
	 */
	public float getMSLAltitude() 
	{
		return lookupValue(Params.MSL_ALTITUDE);
	}
	
	/**
	 * This method sets the MSL altitude parameter 
	 * in the message.
	 * @param mslaltitude
	 */
	public void setMSLAltitude(float mslaltitude) 
	{
		setValue(Params.MSL_ALTITUDE, mslaltitude);
	}
	
	/**
	 * This method returns the MSL altitude units parameter
	 * in the message.
	 * @return
	 */
	public String getMSLAltitudeUnits() 
	{
		return lookupValue(Params.MSL_ALTITUDE_UNITS);
	}
	
	/**
	 * This method sets the MSL altitude units parameter
	 * in the message.
	 * @param mslUnits
	 */
	public void setMSLAltitudeUnits(String mslUnits) 
	{
		setValue(Params.MSL_ALTITUDE_UNITS, mslUnits);
	}
	
	/**
	 * This method returns the geoidal separation parameter
	 * in the message.
	 * @return
	 */
	public float getGeoSep() 
	{
		return lookupValue(Params.GEOIDAL_SEPARATION);
	}
	
	/**
	 * This method sets the geoidal separation parameter
	 * in the message.
	 * @param geoSep
	 */
	public void setGeoSep(float geoSep) 
	{
		setValue(Params.GEOIDAL_SEPARATION, geoSep);
	}
	
	/**
	 * This method returns the geoidal separation units 
	 * parameter in the message.
	 * @return
	 */
	public String getGeoSepUnits() 
	{
		return lookupValue(Params.GEOIDAL_SEPARATION_UNITS);
	}
	
	/**
	 * This method sets the geoidal separation units parameter
	 * in the message.
	 * @param geoSepUnits
	 */
	public void setGeoSepUnits(String geoSepUnits) 
	{
		setValue(Params.GEOIDAL_SEPARATION_UNITS, geoSepUnits);
	}
	
	/**
	 * This method returns the age of differential correction
	 * parameter in the message.
	 * @return
	 */
	public int getAge() 
	{
		return lookupValue(Params.AGE_OF_DIFFERIANTAL_CORRECTION);
	}
	
	/**
	 * This method sets the age of differential correction parameter
	 * in the message.
	 * @param age
	 */
	public void setAge(int age) 
	{
		setValue(Params.AGE_OF_DIFFERIANTAL_CORRECTION, age);
	}
	
	/**
	 * This method returns the differential reference station ID
	 * parameter in the message.
	 * @return
	 */
	public String getDiffRefStationID() 
	{
		return lookupValue(Params.DIFFERNTIAL_REFERENCE_STATION_ID);
	}

	/**
	 * This method sets the differential reference station ID
	 * parameter in the message.
	 * @param diffRefStationID
	 */
	public void setDiffRefStationID(String diffRefStationID) 
	{
		setValue(Params.DIFFERNTIAL_REFERENCE_STATION_ID, diffRefStationID);
	}
	
	
}

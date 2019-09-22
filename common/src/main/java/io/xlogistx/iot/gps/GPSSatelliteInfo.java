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


import org.zoxweb.shared.data.SetNameDAO;
import org.zoxweb.shared.util.GetNVConfig;
import org.zoxweb.shared.util.NVConfig;
import org.zoxweb.shared.util.NVConfigEntity;
import org.zoxweb.shared.util.NVConfigEntityLocal;
import org.zoxweb.shared.util.NVConfigManager;
import org.zoxweb.shared.util.SharedUtil;

/**
 * This class is used by the GPSSatellitesInView class to define the
 * dynamic message parameters of the message protocol header $GPGSV. 
 * GSV (Satellites in View) describes the number of GPS satellites 
 * in view, satellite ID numbers, elevation, azimuth, and SNR values.
 * @author mzebib
 *
 */
@SuppressWarnings("serial")
public class GPSSatelliteInfo extends SetNameDAO
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
				
		SATELLITE_ID( NVConfigManager.createNVConfig("satellite_id", "Channel number range from 1 to 32","SatelliteID",false, true, Integer.class)),
		ELEVATION( NVConfigManager.createNVConfig("elevation", "Elevation in degrees up to 90","Elevation",false, true, Integer.class)),
		AZIMUTH( NVConfigManager.createNVConfig("azimuth", "True elevation in degrees range from 0 to 359", "Azimuth",false, true, Integer.class)),
		SNR( NVConfigManager.createNVConfig("snr", "Signal-to-noise ratio in ranges from 0 to 99 dBHz (null when not tracking)" , "SNR" ,false, true, Integer.class));
		
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
	
	
	private static final NVConfigEntity  NVC_GPS_SATELLITE_INFO = new NVConfigEntityLocal("gps_navigation", null , "GPSNavigation", true, false, false, false,
			GPSNavigation.class, SharedUtil.extractNVConfigs(Params.values()), SharedUtil.toNVConfigList(
																										Params.SATELLITE_ID.getNVConfig(),
																										Params.ELEVATION.getNVConfig(),
																										Params.AZIMUTH.getNVConfig(),
																										Params.SNR.getNVConfig()																
																																															
																										), false, SetNameDAO.NVC_NAME_DAO);
	


	/**
	 * This constructor creates a GPSSatelliteInfo object
	 * with the message parameters list defined.
	 */
	public GPSSatelliteInfo()
	{
		super( NVC_GPS_SATELLITE_INFO);
		
	}
	
	/**
	 * This method returns the satellite ID parameter.
	 * @return
	 */
	public int getSatelliteID() 
	{
		return lookupValue(Params.SATELLITE_ID);
	}
	
	/**
	 * This method sets the satellite ID parameter.
	 * @param id
	 */
	public void setSatelliteID(int id) 
	{
		setValue(Params.SATELLITE_ID, id);
	}
	
	/**
	 * This method returns the elevation parameter.
	 * @return
	 */
	public int getElevation() 
	{
		return lookupValue(Params.ELEVATION);
	}
	
	/**
	 * This method sets the elevation parameter.
	 * @param elevation
	 */
	public void setElevation(int elevation) 
	{
		setValue(Params.ELEVATION, elevation);
	}
	
	/**
	 * This method returns the azimuth parameter.
	 * @return
	 */
	public int getAzimuth() 
	{
		return lookupValue(Params.AZIMUTH);
	}
	
	/**
	 * This method sets the azimuth parameter.
	 * @param azimuth
	 */
	public void setAzimuth(int azimuth) 
	{
		setValue(Params.AZIMUTH, azimuth);
	}
	
	/**
	 * This method returns the SNR parameter.
	 * @return
	 */
	public int getSNR() 
	{
		return lookupValue(Params.SNR);
	}
	
	/**
	 * This method sets the SNR parameter.
	 * @param snr
	 */
	public void setSNR(int snr) 
	{
		setValue(Params.SNR, snr);
	}
	
	
	
	
}

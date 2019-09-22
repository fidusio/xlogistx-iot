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

import java.util.ArrayList;
import org.zoxweb.shared.util.GetNVConfig;
import org.zoxweb.shared.util.NVConfig;
import org.zoxweb.shared.util.NVConfigEntity;
import org.zoxweb.shared.util.NVConfigEntity.ArrayType;
import org.zoxweb.shared.util.NVConfigEntityLocal;
import org.zoxweb.shared.util.NVConfigManager;
import org.zoxweb.shared.util.SharedUtil;

/**
 * This class is used to define the message parameters for the message 
 * protocol header $GPGSV. GSV (Satellites in View) describes the 
 * number of GPS satellites in view, satellite ID numbers, elevation,
 * azimuth, and SNR values.
 * @author mzebib
 *
 */
@SuppressWarnings("serial")
public class GPSSatellitesInView extends GPSMessage
{
	/**
	 * This enum contains the messages parameters and creates an NVConfig 
	 * object for three parameters which specifies the paramater's name, 
	 * description, display name, and type. The last parameter contains an
	 * NVConfigEntity object.
	 * @author mzebib
	 *
	 */
	private enum Params
	implements GetNVConfig
	{
				
		NUMBER_OF_MESSAGES( NVConfigManager.createNVConfig("number_of_messages", "Number of messages range from 1 to 3 (Depending on the number of satellites tracked, multiple messages of GSV data may be required.)","NumberOfMessages",false, true, Integer.class)),
		MESSAGE_SEQUENCE_ID( NVConfigManager.createNVConfig("message_sequence_id", "Message sequence ID range from 1 to 3","MessageSequenceID",false, true, Integer.class)),
		SATELLITES_IN_VIEW( NVConfigManager.createNVConfig("satellites_in_view", "Number of satellites in view", "SatellitesInView",false, true, Integer.class)),
		SATELLITE_INFO_LIST( NVConfigManager.createNVConfigEntity("satellite_info_list", "List contains satellite ID, elevation, azimuth, and SNR values" , "SatelliteInfoList" , false, true, GPSSatelliteInfo[].class, ArrayType.LIST));
		
		
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
	
	private static final NVConfigEntity  NVC_GPS_SATELLITES_IN_VIEW = new NVConfigEntityLocal("gps_satellites_in_view", null , "GPSSatellitesInView", true, false, false, false,
			GPSSatellitesInView.class, SharedUtil.extractNVConfigs(Params.values()), SharedUtil.toNVConfigList(GPSMessage.NVC_MESSAGE_ID, 
																													Params.NUMBER_OF_MESSAGES.getNVConfig(),
																													Params.MESSAGE_SEQUENCE_ID.getNVConfig(),
																													Params.SATELLITES_IN_VIEW.getNVConfig(),
																													Params.SATELLITE_INFO_LIST.getNVConfig()																					
																													),
																													false,
																													GPSMessage.NVC_GPS_MESSAGE);
																								
	/**
	 * This constructor creates a GPSSatellitesInView object
	 * with the message parameters list defined and the GPS 
	 * message type set as output.
	 */
	public GPSSatellitesInView()
	{
		super(NVC_GPS_SATELLITES_IN_VIEW);
		setMessageType(GPSMessageType.OUTPUT);
	}
	
	
	/**
	 * This method returns the number of messages parameter 
	 * in the message.
	 * @return
	 */
	public int getNumberofMessages()
	{
		return lookupValue(Params.NUMBER_OF_MESSAGES);
	}
	
	/**
	 * This method sets the number of messages parameter 
	 * in the message.
	 * @param num
	 */
	public void setNumberOfMessages(int num)
	{
		setValue(Params.NUMBER_OF_MESSAGES, num);
	}
	
	/**
	 * This method returns the message sequence ID number 
	 * parameter in the message.
	 * @return
	 */
	public int getMessageSequenceID()
	{
		return lookupValue(Params.MESSAGE_SEQUENCE_ID);
	}
	
	/**
	 * This method sets the message sequence ID number
	 * parameter in the message.
	 * @param id
	 */
	public void setMessageSequenceID(int id)
	{
		setValue(Params.MESSAGE_SEQUENCE_ID, id);
	}
	
	/**
	 * This method returns the satellites in view parameter
	 * in the message.
	 */
	public int getSatellitesInView()
	{
		return lookupValue(Params.SATELLITES_IN_VIEW);
	}
	
	/**
	 * This method sets the satellites in view parameter 
	 * in the message.
	 * @param view
	 */
	public void setSatellitesInView(int view)
	{
		setValue(Params.SATELLITES_IN_VIEW, view);
	}
	
	/**
	 * This method returns the satellite info list which 
	 * contains satellite ID, elevation, azimuth, and SNR 
	 * parameters.
	 * @return
	 */
	public ArrayList<GPSSatelliteInfo> getSatelliteInfo()
	{
		return lookupValue(Params.SATELLITE_INFO_LIST);
	}
	
	/**
	 * This method sets the satellite info list which contains
	 * satellite ID, elevation, azimuth, and SNR parameters.
	 * @param list
	 */
	public void setSatelliteInfo(ArrayList<GPSSatelliteInfo> list)
	{
		setValue(Params.SATELLITE_INFO_LIST, list);
	}
	



}

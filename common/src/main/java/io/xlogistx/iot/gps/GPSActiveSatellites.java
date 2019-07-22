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
import org.zoxweb.shared.util.NVConfigEntityLocal;
import org.zoxweb.shared.util.NVConfigManager;
import org.zoxweb.shared.util.NVPair;
import org.zoxweb.shared.util.SharedUtil;

/**
 * This class is used to define the message parameters for the message 
 * protocol header $GPGSA. GSA (GNSS DOP and Active Satellites) describes 
 * the GPS receiver operating mode, active satellites used in the 
 * position solution and DOP values.
 * @author mnael
 *
 */
@SuppressWarnings("serial")
public class GPSActiveSatellites extends GPSMessage
{
	/**
	 * This enum contains the messages parameters and creates an NVConfig 
	 * object for each parameter which specifies the paramater's name, 
	 * description, display name, and type.
	 * @author mzebib
	 *
	 */
	public enum Params
	implements GetNVConfig
	{
		
		MODE_1( NVConfigManager.createNVConfig("mode_1", "Specifies mode type: manual (forced to operate in 2D or 3D mode) or automatic (allowed to automatically switch 2D/3D)","Mode1",false, true, GPSConst.ModeOne.class)),
		MODE_2( NVConfigManager.createNVConfig("mode_2", "Specifies mode type: 1 (fix not available), 2 (2D; < 4 SVs used), or 3 (3D; >= 4 SVs used)","Mode2",false, true, GPSConst.ModeTwo.class)),
//		SATELLITE_USED_1( NVConfigManager.createNVConfig("satellite_used_channel_1", "SV on Channel 1","SatelliteUsedChannel1",false, true, String.class)),
//		SATELLITE_USED_2( NVConfigManager.createNVConfig("satellite_used_channel_2", "SV on Channel 2","SatelliteUsedChannel2",false, true, String.class)),
//		SATELLITE_USED_3( NVConfigManager.createNVConfig("satellite_used_channel_3", "SV on Channel 3","SatelliteUsedChannel3",false, true, String.class)),
//		SATELLITE_USED_4( NVConfigManager.createNVConfig("satellite_used_channel_4", "SV on Channel 4","SatelliteUsedChannel4",false, true, String.class)),
//		SATELLITE_USED_5( NVConfigManager.createNVConfig("satellite_used_channel_5", "SV on Channel 5","SatelliteUsedChannel5",false, true, String.class)),
//		SATELLITE_USED_6( NVConfigManager.createNVConfig("satellite_used_channel_6", "SV on Channel 6","SatelliteUsedChannel6",false, true, String.class)),
//		SATELLITE_USED_7( NVConfigManager.createNVConfig("satellite_used_channel_7", "SV on Channel 7","SatelliteUsedChannel7",false, true, String.class)),
//		SATELLITE_USED_8( NVConfigManager.createNVConfig("satellite_used_channel_8", "SV on Channel 8","SatelliteUsedChannel8",false, true, String.class)),
//		SATELLITE_USED_9( NVConfigManager.createNVConfig("satellite_used_channel_9", "SV on Channel 9","SatelliteUsedChannel9",false, true, String.class)),
//		SATELLITE_USED_10( NVConfigManager.createNVConfig("satellite_used_channel_10", "SV on Channel 10","SatelliteUsedChannel10",false, true, String.class)),
//		SATELLITE_USED_11( NVConfigManager.createNVConfig("satellite_used_channel_11", "SV on Channel 11","SatelliteUsedChannel11",false, true, String.class)),
//		SATELLITE_USED_12( NVConfigManager.createNVConfig("satellite_used_channel_12", "SV on Channel 12","SatelliteUsedChannel12",false, true, String.class)),
		//We need to clarify this list further (ie. String to Integer).
		SATELLITES_USED( NVConfigManager.createNVConfig("satellites_used", "List of satellites used which gives the channel used","SatellitesUsed",false, true, String[].class)),
		PDOP( NVConfigManager.createNVConfig("pdop", "Position dilution of precision","PDOP",false, true, Float.class)),
		HDOP( NVConfigManager.createNVConfig("hdop", "Horizontal dilution of precision","HDOP",false, true, Float.class)),
		VDOP( NVConfigManager.createNVConfig("vdop", "Vertical dilution of precision","VDOP",false, true, Float.class))
		
	
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
	
	
	private static final NVConfigEntity  NVC_GPS_ACTIVE_SATELLITES = new NVConfigEntityLocal("gps_active_satellites", null , "GPSActiveSatellites", true, false, false, false,
			GPSActiveSatellites.class, SharedUtil.extractNVConfigs(Params.values()), SharedUtil.toNVConfigList(GPSMessage.NVC_MESSAGE_ID, 
																										Params.MODE_1.getNVConfig(),
																										Params.MODE_2.getNVConfig(),
//																										Params.SATELLITE_USED_1.getNVConfig(),
//																										Params.SATELLITE_USED_2.getNVConfig(),
//																										Params.SATELLITE_USED_3.getNVConfig(),
//																										Params.SATELLITE_USED_4.getNVConfig(),
//																										Params.SATELLITE_USED_5.getNVConfig(),
//																										Params.SATELLITE_USED_6.getNVConfig(),
//																										Params.SATELLITE_USED_7.getNVConfig(),
//																										Params.SATELLITE_USED_8.getNVConfig(),
//																										Params.SATELLITE_USED_9.getNVConfig(),
//																										Params.SATELLITE_USED_10.getNVConfig(),
//																										Params.SATELLITE_USED_11.getNVConfig(),
//																										Params.SATELLITE_USED_12.getNVConfig(),
																										Params.SATELLITES_USED.getNVConfig(),
																										Params.PDOP.getNVConfig(),
																										Params.HDOP.getNVConfig(),
																										Params.VDOP.getNVConfig()																										
																										), 
																										false,
																										GPSMessage.NVC_GPS_MESSAGE);
	

	/**
	 * This constructor creates a GPSActiveSatellites object
	 * with the message parameters list defined and the GPS 
	 * message type set as output.
	 */
	public GPSActiveSatellites()
	{
		super(NVC_GPS_ACTIVE_SATELLITES);
		setMessageID(GPSMessageID.GPGSA);
		setMessageType(GPSMessageType.OUTPUT);
	}
	

	/**
	 * This method returns the Mode 1 parameter in the message.
	 * @return
	 */
	public GPSConst.ModeOne getMode1() 
	{
		return lookupValue(Params.MODE_1);
	}
	
	/**
	 * This method sets the Mode 1 parameter in the message.
	 * @param mode1
	 */
	public void setMode1(GPSConst.ModeOne mode1) 
	{
		setValue(Params.MODE_1, mode1);
	}
	
	/**
	 * This method returns the Mode 2 parameter in the message.
	 * @return
	 */
	public GPSConst.ModeTwo getMode2() 
	{
		return lookupValue(Params.MODE_2);
	}
	
	/**
	 * This method sets the Mode 2 parameter in the message.
	 * @param mode2
	 */
	public void setMode2(GPSConst.ModeTwo mode2) 
	{
		setValue(Params.MODE_2, mode2);
	}
	
//	/**
//	 * This method returns the first Satellite Used parameter in the message.
//	 * @return
//	 */
//	public String getSatelliteUsed1()
//	{
//		return lookupValue(Params.SATELLITE_USED_1);
//	}
//	
//	/**
//	 * This method sets the first Satellite Used parameter in the message.
//	 * @param satelliteUsed1
//	 */
//	public void setSatelliteUsed1(String satelliteUsed1) 
//	{
//		setValue(Params.SATELLITE_USED_1, satelliteUsed1);
//	}
//	
//	/**
//	 * This method returns the second Satellite Used parameter in the message.
//	 * @return
//	 */
//	public String getSatelliteUsed2() 
//	{
//		return lookupValue(Params.SATELLITE_USED_2);
//	}
//	
//	/**
//	 * This method sets the second Satellite Used parameter in the message.
//	 * @param satelliteUsed2
//	 */
//	public void setSatelliteUsed2(String satelliteUsed2) 
//	{
//		setValue(Params.SATELLITE_USED_2, satelliteUsed2);
//	}
//	
//	/**
//	 * This method returns the third Satellite Used parameter in the message.
//	 * @return
//	 */
//	public String getSatelliteUsed3() 
//	{
//		return lookupValue(Params.SATELLITE_USED_3);
//	}
//	
//	/**
//	 * This method sets the third Satellite Used parameter in the message.
//	 * @param satelliteUsed3
//	 */
//	public void setSatelliteUsed3(String satelliteUsed3) 
//	{
//		setValue(Params.SATELLITE_USED_3, satelliteUsed3);
//	}
//	
//	/**
//	 * This method returns the forth Satellite Used parameter in the message.
//	 * @return
//	 */
//	public String getSatelliteUsed4() 
//	{
//		return lookupValue(Params.SATELLITE_USED_4);
//	}
//	
//	/**
//	 * This method sets the forth Satellite Used parameter in the message.
//	 * @param satelliteUsed4
//	 */
//	public void setSatelliteUsed4(String satelliteUsed4) 
//	{
//		setValue(Params.SATELLITE_USED_4, satelliteUsed4);
//	}
//	
//	/**
//	 * This method returns the fifth Satellite Used parameter in the message.
//	 * @return
//	 */
//	public String getSatelliteUsed5() 
//	{
//		return lookupValue(Params.SATELLITE_USED_5);
//	}
//	
//	/**
//	 * This method sets the fifth Satellite Used parameter in the message.
//	 * @param satelliteUsed5
//	 */
//	public void setSatelliteUsed5(String satelliteUsed5) 
//	{
//		setValue(Params.SATELLITE_USED_5, satelliteUsed5);
//	}
//	
//	/**
//	 * This method returns the sixth Satellite Used parameter in the message.
//	 * @return
//	 */
//	public String getSatelliteUsed6() 
//	{
//		return lookupValue(Params.SATELLITE_USED_6);
//	}
//	
//	/**
//	 * This method sets the sixth Satellite Used parameter in the message.
//	 * @param satelliteUsed6
//	 */
//	public void setSatelliteUsed6(String satelliteUsed6) 
//	{
//		setValue(Params.SATELLITE_USED_6, satelliteUsed6);
//	}
//	
//	/**
//	 * This method returns the seventh Satellite Used parameter in the message.
//	 * @return
//	 */
//	public String getSatelliteUsed7() 
//	{
//		return lookupValue(Params.SATELLITE_USED_7);
//	}
//	
//	/**
//	 * This method sets the seventh Satellite Used parameter in the message.
//	 * @param satelliteUsed7
//	 */
//	public void setSatelliteUsed7(String satelliteUsed7)
//	{
//		setValue(Params.SATELLITE_USED_7, satelliteUsed7);
//	}
//	
//	/**
//	 * This method returns the eighth Satellite Used parameter in the message.
//	 * @return
//	 */
//	public String getSatelliteUsed8() 
//	{
//		return lookupValue(Params.SATELLITE_USED_8);
//	}
//	
//	/**
//	 * This method sets the eighth Satellite Used parameter in the message.
//	 * @param satelliteUsed8
//	 */
//	public void setSatelliteUsed8(String satelliteUsed8) 
//	{
//		setValue(Params.SATELLITE_USED_8, satelliteUsed8);
//	}
//	
//	/**
//	 * This method returns the ninth Satellite Used parameter in the message.
//	 * @return
//	 */
//	public String getSatelliteUsed9() 
//	{
//		return lookupValue(Params.SATELLITE_USED_9);
//	}
//	
//	/**
//	 * This method sets the ninth Satellite Used parameter in the message.
//	 * @param satelliteUsed9
//	 */
//	public void setSatelliteUsed9(String satelliteUsed9) 
//	{
//		setValue(Params.SATELLITE_USED_9, satelliteUsed9);
//	}
//	
//	/**
//	 * This method returns the tenth Satellite Used parameter in the message.
//	 * @return
//	 */
//	public String getSatelliteUsed10() 
//	{
//		return lookupValue(Params.SATELLITE_USED_10);
//	}
//	
//	/**
//	 * This method sets the tenth Satellite Used parameter in the message.
//	 * @param satelliteUsed10
//	 */
//	public void setSatelliteUsed10(String satelliteUsed10) 
//	{
//		setValue(Params.SATELLITE_USED_10, satelliteUsed10);
//	}
//	
//	/**
//	 * This method returns the eleventh Satellite Used parameter in the message.
//	 * @return
//	 */
//	public String getSatelliteUsed11() 
//	{
//		return lookupValue(Params.SATELLITE_USED_11);
//	}
//	
//	/**
//	 * This method sets the eleventh Satellite Used parameter in the message.
//	 * @param satelliteUsed11
//	 */
//	public void setSatelliteUsed11(String satelliteUsed11) 
//	{
//		setValue(Params.SATELLITE_USED_11, satelliteUsed11);
//	}
//	
//	/**
//	 * This method returns the twelfth Satellite Used parameter in the message.
//	 * @return
//	 */
//	public String getSatelliteUsed12() 
//	{
//		return lookupValue(Params.SATELLITE_USED_12);
//	}
//	
//	/**
//	 * This method sets the twelfth Satellite Used parameter in the message.
//	 * @param satelliteUsed12
//	 */
//	public void setSatelliteUsed12(String satelliteUsed12) 
//	{
//		setValue(Params.SATELLITE_USED_12, satelliteUsed12);
//	}
	
	
	public ArrayList<NVPair> getSatellitesUsed()
	{
		return lookupValue(Params.SATELLITES_USED);		
	}
	
	
	public void setSatellitesUsed(ArrayList<NVPair> satellitesList)
	{
		setValue(Params.SATELLITES_USED, satellitesList);
	}
	
	
	/**
	 * This method returns the PDOP parameter in the message.
	 * @return
	 */
	public float getPDOP() 
	{
		return lookupValue(Params.PDOP);
	}
	
	/**
	 * This method sets the PDOP parameter in the message.
	 * @param pdop
	 */
	public void setPDOP(float pdop) 
	{
		setValue(Params.PDOP, pdop);
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
	 * This method returns the VDOP parameter in the message.
	 * @return
	 */
	public float getVDOP() 
	{
		return lookupValue(Params.VDOP);
	}
	
	/**
	 * This method sets the VDOP parameter in the message. 
	 * @param pdop
	 */
	public void setVDOP(float pdop)
	{
		setValue(Params.VDOP, pdop);
	}
	
	
}

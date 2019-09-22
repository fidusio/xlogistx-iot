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
import org.zoxweb.shared.util.NVConfig;
import org.zoxweb.shared.util.NVConfigEntity;
import org.zoxweb.shared.util.NVConfigEntityLocal;
import org.zoxweb.shared.util.NVConfigManager;
import org.zoxweb.shared.util.NVPair;
import org.zoxweb.shared.util.SharedStringUtil;
import org.zoxweb.shared.util.SharedUtil;

/**
 * This class is used to generate the GPS input commands 
 * of type PMTK. The message format is as follows:
 * Message ID: PMTK
 * PktType: "000" to "999"
 * DataField: Length depends on the packet type.
 * $ + Message ID + PktType + DataField + * + CheckSum + End Sequence
 * e.g. $PMTK605*32<CR><LF>
 * @author mzebib
 *
 */
@SuppressWarnings("serial")
public class GPSPMTKCommand extends GPSInput
{
	
	private static final NVConfig PKT_TYPE =  NVConfigManager.createNVConfig("pkt_type", "PKT Type","PKTType",true, false, Integer.class);
	
	private static final NVConfig DATA_FIELD =  NVConfigManager.createNVConfig("data_field", "Data field","DataField",true, false, String[].class);

	
	
	private static final NVConfigEntity  NVC_GPS_PMTK_COMMAND = new NVConfigEntityLocal("gps_pmtk_command", null , "GPSPMTKCommand", true, false, false, false,
		GPSPMTKCommand.class, SharedUtil.toNVConfigList(PKT_TYPE, DATA_FIELD), null, false,
			NVC_GPS_INPUT);

	
	/**
	 * This constructor creates a GPSPMTKCommand object
	 * with the message parameters defined, message ID 
	 * set to PMTK and the message type set as input.
	 */
	public GPSPMTKCommand()
	{
		super(SharedUtil.merge(null, NVC_GPS_PMTK_COMMAND));
		setMessageID(GPSMessageID.PMTK);
		setMessageType(GPSMessageType.INPUT);
	}
	
	
	/**
	 * This method returns the PktType.
	 * @return
	 */
	public int getPktType()
	{
		return lookupValue(PKT_TYPE);
	}
	
	
	/**
	 * This method sets the Pkt type.
	 * @param type
	 */
	public void setPktType(int type)
	{
		if(type >= 0 && type <= 999)
		{
			setValue(PKT_TYPE, type);
		}
		
		else
		{
			throw new IllegalArgumentException("Invalid: Pkt Type value out of range!");
		}
		
	}
	
	/**
	 * This method returns the data field.
	 * @return
	 */
	public ArrayList<NVPair> getDataFields()
	{
		return lookupValue(DATA_FIELD);
	}
	
	/**
	 * This method sets the data field.
	 * @param data
	 */
	public void setDataFields(ArrayList<NVPair> data)
	{
		setValue(DATA_FIELD, data);
	}



//	public String oldToCanonicalID() 
//	{
//		String command = getMessageID().name() + String.format("%03d", getPktType());
//		//NVPair[] data =  getDataFields().toArray( new NVPair[0]);
//		
//		for(NVPair tmp : getDataFields())
//		{
//			if(!SharedStringUtil.isEmpty(tmp.getValue()))
//			{
//				command = command + "," + tmp.getValue();
//			}
//			
//		}
//		
//		return "$" + command + "*" + SharedStringUtil.byteToHex(null, (byte) checkSum(command));
//		
//	}
	
	
	/**
	 * This method returns a string of the GPS input command message
	 * after generating the checksum of the combined message containing
	 * the message ID, Pkt type, and data field.
	 */
	public String toCanonicalID() 
	{
		StringBuilder sb = new StringBuilder(getMessageID().getName());
		
		sb.append(String.format("%03d", getPktType()));
		
		
		for(NVPair tmp : getDataFields())
		{	
			sb.append(',');
			if(!SharedStringUtil.isEmpty(tmp.getValue()))
			{
				sb.append(tmp.getValue());
			}
			
		}
		
		String command = sb.substring(1);
		sb.append('*');
		
		return SharedStringUtil.byteToHex(sb, null, (byte) checkSum(command)).toString();
		
	}

}

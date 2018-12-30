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


import java.text.ParseException;
import java.util.List;
import static io.xlogistx.iot.gps.GPSConst.*;


import org.zoxweb.shared.util.NVConfig;
import org.zoxweb.shared.util.NVConfigEntity;
import org.zoxweb.shared.util.NVPair;
import org.zoxweb.shared.util.SharedStringUtil;
import org.zoxweb.shared.util.SharedUtil;

import io.xlogistx.iot.util.DateFormatter;


/**
 * This class parses GPS messages based on the message type and uses the
 * appropriate parser to interpret GPS messages.
 * @author mzebib
 *
 */
public class GPSParser
{
	/**
	 * This method takes the GPSMessage and the delimiter as a parameter. The
	 * message is parsed based on the delimiter and stored in a string array.
	 * The message protocol header is looked up in an enum in order 
	 * to call the appropriate parse method within the class.
	 * @param rawGPSMessage
	 * @param delim
	 * @return
	 * @throws ParseException
	 */
	public static GPSMessage parse(String rawGPSMessage, String delim) throws ParseException
	{
		String[] rawMessageAndCheckSum = parseParamWithCheckSum(rawGPSMessage);
		String[] tokens = rawMessageAndCheckSum[0].split(delim);
	
		GPSMessageID messageType = (GPSMessageID) SharedUtil.lookupEnum(tokens[0], GPSMessageID.values());
		GPSMessage ret = null;
		
		switch(messageType)
		{
		case GPGGA: 
			ret = ParseGPGGA(tokens, rawMessageAndCheckSum[1]);
			break;
		case GPGSA: 
			ret = parseGPGSA(tokens, rawMessageAndCheckSum[1]);
			break;
		case GPGSV: 
			ret = parseGPGSV(tokens, rawMessageAndCheckSum[1]);
			break;
		case GPRMC: 
			ret = parseGPRMC(tokens, rawMessageAndCheckSum[1]);
			break;
		case GPVTG: 
			ret = parseGeneric( new GPSCourseInfo(), tokens, rawMessageAndCheckSum[1], true);
			break;
		case GPGLL: 
			ret = parseGPGLL(tokens, rawMessageAndCheckSum[1]);
			break;
		case GPZDA: 
			ret = parseGPZDA(tokens, rawMessageAndCheckSum[1]);
			break;
		case GPMSS:
			ret = parseGeneric( new GPSReceiverSignal(), tokens, rawMessageAndCheckSum[1], true);
			break;
		default:
			return null;
		}
        
		if(ret != null)
		{
			ret.setData(rawMessageAndCheckSum[0].getBytes());
			String tmpCheckSum = SharedStringUtil.byteToHex(null, null, (byte) ret.computeCheckSum()).toString();
			
			if(!tmpCheckSum.equals(rawMessageAndCheckSum[1]))
			{
				throw new IllegalArgumentException("Checksum mismatch! Expected " + tmpCheckSum + " Sent Value:" + rawMessageAndCheckSum[1] + " for" + rawMessageAndCheckSum[0]);
			}
			
		}
		
		return ret;
    }
	
	
	/**
	 * This method parses the the last element in the token 
	 * and returns the message parameter found before '*' in
	 * the element and the checksum value found after '*'.
	 * after the star. 
	 * @param str
	 * @return
	 */
	private static String[] parseParamWithCheckSum(String str)
	{
		String[] ret = new String[2];
		
		int indexOf = str.indexOf('*');
		ret[0] = str.substring(0, indexOf);
		ret[1] = str.substring(indexOf + "*".length());
		
		return ret;
		
	}
	
	
	/**
	 * This method parses the message ID: GGA which represents
	 * Global Positioning System Fixed Data.
	 * @param tokens
	 * @param checkSum
	 * @return
	 * @throws ParseException
	 */
	public static GPSFixedData ParseGPGGA(String[] tokens, String checkSum) throws ParseException
	{
		SharedUtil.checkIfNulls("Tokens or message type are null.", tokens, checkSum);
		if(tokens.length != 15)
		{
			throw new IllegalArgumentException("Invalid: Cannot parse because the length of the token is not equal to 15.");
		}
		
		GPSFixedData fixed = new GPSFixedData();
		NVConfigEntity nvconfig = (NVConfigEntity) fixed.getNVConfig();
		List<NVConfig> nvconfigList = nvconfig.getDisplayAttributes();
		
		
		int i = 0;
		for(i = 0; i < tokens.length; i++)
		{
			NVConfig nvc = nvconfigList.get(i);
						
			switch(i)
			{
			case 1: 
				if(tokens[i] != null)
				{
					fixed.setUtcTime(DateFormatter.parseUTCTime(tokens[i]));
				}
				break;
			default:
				fixed.setValue(nvc, SharedUtil.stringToValue(nvc, tokens[i]));
				break;
			}
		}
		
		return fixed;

	}
	
	
	/**
	 * This method parses the message ID: GSA which represents 
	 * GNSS DOP and Active Satellites.
	 * @param tokens
	 * @param checkSum
	 * @return
	 * @throws ParseException
	 */
	public static GPSActiveSatellites parseGPGSA(String[] tokens, String checkSum) throws ParseException
	{
		SharedUtil.checkIfNulls("Tokens or message type are null.", tokens, checkSum);
		if(tokens.length != 18)
		{
			throw new IllegalArgumentException("Invalid: Cannot parse because the length of the token is not equal to 18.");
		}

		GPSActiveSatellites active = new GPSActiveSatellites();
		
		
		int i = 0;
		for(i = 0; i < tokens.length; i++)
		{
			if(i > 2 && i < 15)
			{
				if(!SharedStringUtil.isEmpty(tokens[i]))
				{
					active.getSatellitesUsed().add(new NVPair("sat[" + (i-2) + "]" ,tokens[i]));					
				}
			}
			
			else
			{
				switch(i)
				{
				
				case 1:
					active.setMode1((ModeOne) SharedUtil.lookupEnum(tokens[i], GPSConst.ModeOne.values()));
					break;
				case 2:
					active.setMode2((ModeTwo) SharedUtil.lookupEnum(tokens[i], GPSConst.ModeTwo.values()));
					break;
				case 15:
					active.setPDOP(Float.valueOf(tokens[i]));
					break;
				case 16:
					active.setHDOP(Float.valueOf(tokens[i]));
					break;
				case 17:
					active.setVDOP(Float.valueOf(tokens[i]));
					break;
				default:
					break;
				
				
				}
				
			}

		}
		
		return active;

	}
		
	
	/**
	 * This method parses the message ID: RMC which represents
	 * Recommended Minimum Navigation Information.
	 * @param tokens
	 * @param checkSum
	 * @return
	 * @throws ParseException
	 */
	public static GPSNavigation parseGPRMC(String[] tokens, String checkSum) throws ParseException
	{
		SharedUtil.checkIfNulls("Tokens or message type are null.", tokens, checkSum);
		if(tokens.length != 12 && tokens.length != 13)
		{
			throw new IllegalArgumentException("Invalid: Cannot parse because the length of the token is not equal to 12 or 13." + tokens.length);
		}

		GPSNavigation navig = new GPSNavigation();
		NVConfigEntity nvconfig = (NVConfigEntity) navig.getNVConfig();
		List<NVConfig> nvconfigList = nvconfig.getDisplayAttributes();
		
		
		
		for(int i = 0; i < tokens.length; i++)
		{
			NVConfig nvc = null;

			if(i > 9)
			{
				nvc = nvconfigList.get(i-1);
			}
			
			else
			{
				nvc = nvconfigList.get(i);
				
			}
			
			if(tokens.length == 12 ) 
			{
				switch(i)
				{
				case 1:
					break;
				case 9:
					if(tokens[i] != null)
					{
						navig.setGPSTime(DateFormatter.GPS_DATE_FORMAT.parse(tokens[9] + tokens[1]).getTime());
					}
					break;
				case 10:
					break;
				default:
					navig.setValue(nvc, SharedUtil.stringToValue(nvc, tokens[i]));
					break;
				}
			}
		
			else 
			{
				switch(i)
				{
				case 1:
					break;
				case 9:
					if(tokens[i] != null)
					{
						navig.setGPSTime(DateFormatter.GPS_DATE_FORMAT.parse(tokens[9] + tokens[1]).getTime());
					}
					break;
				case 10:
					navig.setMagneticVariationValue((float) SharedUtil.stringToValue(nvc, tokens[i]));
					break;
				case 11:
					navig.setMagneticVariationDirection((CardinalDirection) SharedUtil.stringToValue(nvc, tokens[i]));
					break;				
				default:
					navig.setValue(nvc, SharedUtil.stringToValue(nvc, tokens[i]));
					break;
				}
			}
		}
		
		return navig;
				
	}
	
	
	/**
	 * This method parses the message ID: VTG which represents
	 * Course and Speed Information Relative to the Ground.
	 * @param tokens
	 * @param checkSum
	 * @return
	 */
	public static GPSCourseInfo parseGPVTG(String[] tokens, String checkSum)
	{
		SharedUtil.checkIfNulls("Tokens or message type are null.", tokens, checkSum);
		if(tokens.length != 9)
		{
			throw new IllegalArgumentException("Invalid: Cannot parse because the length of the token is not equal to 9.");
		}

		GPSCourseInfo course = new GPSCourseInfo();
		NVConfigEntity nvconfig = (NVConfigEntity) course.getNVConfig();
		List<NVConfig> nvconfigList = nvconfig.getDisplayAttributes();
		

		for(int i = 0; i < tokens.length; i++)
		{
			NVConfig nvc = nvconfigList.get(i);
			course.setValue(nvc, SharedUtil.stringToValue(nvc, tokens[i]));			
		}
	
		return course;
		
	}
	
	/**
	 * This method can be used to parse generic messages 
	 * that do not have specific conditions.
	 * @param message
	 * @param tokens
	 * @param checkSum
	 * @return
	 */
	public static GPSMessage parseGeneric(GPSMessage message, String[] tokens, String checkSum, boolean fullMatch)
	{
		SharedUtil.checkIfNulls("Tokens or message type are null.", message, tokens, checkSum);
		NVConfigEntity nvconfig = (NVConfigEntity) message.getNVConfig();
		List<NVConfig> nvconfigList = nvconfig.getDisplayAttributes();
		if(fullMatch && tokens.length != nvconfigList.size())
		{
			throw new IllegalArgumentException("Invalid: Cannot parse because the length of the token " + tokens.length + " is not equal to " + nvconfigList.size());
		}
		
		
		
		for(int i = 0; i < tokens.length; i++)
		{
			NVConfig nvc = nvconfigList.get(i);
			message.setValue(nvc, SharedUtil.stringToValue(nvc, tokens[i]));			
		}
	
		return message;
		
	}
	
	
	
	/**
	 * This method parses the message ID: GLL which represents
	 * Geographic Position - Latitude/Longitude.
	 * @param tokens
	 * @param checkSum
	 * @return
	 * @throws ParseException
	 */
	public static GPSGeographicPosition parseGPGLL(String[] tokens, String checkSum) throws ParseException
	{
		SharedUtil.checkIfNulls("Tokens or message type are null.", tokens, checkSum);
		if(tokens.length != 7)
		{
			throw new IllegalArgumentException("Invalid: Cannot parse because the length of the token is not equal to 7.");
		}
		
		GPSGeographicPosition geopos = new GPSGeographicPosition();
		NVConfigEntity nvconfig = (NVConfigEntity) geopos.getNVConfig();
		List<NVConfig> nvconfigList = nvconfig.getDisplayAttributes();
		
		
		int i = 0;
		for(i = 0; i < tokens.length; i++)
		{
			NVConfig nvc = nvconfigList.get(i);
						
			switch(i)
			{
			case 5: 
				if(tokens[i] != null)
				{
					geopos.setUtcTime(DateFormatter.parseUTCTime(tokens[i]));
				}
				break;
			default:
				geopos.setValue(nvc, SharedUtil.stringToValue(nvc, tokens[i]));
				break;
			}
		}
		
		return geopos;
		
	}
	
	
	/**
	 * This method parses the message ID: ZDA which represents
	 * Date & Time.
	 * @param tokens
	 * @param checkSum
	 * @return
	 * @throws ParseException
	 */
	public static GPSDateTime parseGPZDA(String[] tokens, String checkSum) throws ParseException
	{
		
		GPSDateTime date = new GPSDateTime();
		
		int index = 0;
		String toParse = tokens[++index] + "," + tokens[++index] + "," + tokens[++index] + "," + tokens[++index];
		String timeZone = (tokens[++index].charAt(0) == '-' ? tokens[index] :  "+" + tokens[index]) + tokens[++index] ;
		date.setGPSTime(DateFormatter.GPS_ZDA_FORMAT.parse(toParse + timeZone).getTime());
		date.setZZTimeZone(timeZone);
				
		
		return date;
	} 
	
	
	/**
	 * This method parses the message ID: GSV which represents
	 * GNSS Satellites in View.
	 * @param tokens
	 * @param checkSum
	 * @return
	 */
	public static GPSSatellitesInView parseGPGSV(String[] tokens, String checkSum)
	{
		SharedUtil.checkIfNulls("Tokens or message type are null.", tokens, checkSum);
		
		GPSSatellitesInView satellite = new GPSSatellitesInView();
		NVConfigEntity nvconfig = (NVConfigEntity) satellite.getNVConfig();
		List<NVConfig> nvconfigList = nvconfig.getDisplayAttributes();

	
		for(int i = 0; i < tokens.length; i++)
		{
			
			if(i < 4)
			{
				satellite.setValue(nvconfigList.get(i), 
						SharedUtil.stringToValue(nvconfigList.get(i), 
								tokens[i]));
			}
			
			else
			{
				GPSSatelliteInfo info = new GPSSatelliteInfo();
				NVConfigEntity nvcSatInfo = (NVConfigEntity) info.getNVConfig();
				List<NVConfig> nvcSatInfoList = nvcSatInfo.getDisplayAttributes();
				
				for(int j = 0; j < nvcSatInfoList.size() && i < tokens.length; j++, i++)
				{
					info.setValue(nvcSatInfoList.get(j), SharedUtil.stringToValue(nvcSatInfoList.get(j), tokens[i]));
					
				}
				
				i--;
				satellite.getSatelliteInfo().add(info);	
			}
			
		}
				
		return satellite;
	
	}
}

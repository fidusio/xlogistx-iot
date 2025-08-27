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


import org.zoxweb.shared.data.DataDAO;
import org.zoxweb.shared.util.*;

/**
 * This class includes GPS message parameters common between all GPS messages
 * which include the message identifier, checksum, and message type.
 * @author mzebib
 *
 */
@SuppressWarnings("serial")
public abstract class GPSMessage extends DataDAO {
    /**
     * This variable creates an NVConfig object for the
     * parameter which specifies the paramater's name,
     * description, display name, and type.
     */
    protected static final NVConfig NVC_MESSAGE_ID = NVConfigManager.createNVConfig("message_id", "This is the message type identifer.", "MessageID", true, false, GPSMessageID.class);

    /**
     * This variable creates an NVConfig object for the
     * parameter which specifies the paramater's name,
     * description, display name, and type.
     */
    //protected static final NVConfig NVC_CHECKSUM =  NVConfigManager.createNVConfig("checksum", "The checksum is used for message verification check.","CheckSum",true, false, String.class);


    /**
     * This variable creates an NVConfig object for the
     * parameter which specifies the paramater's name,
     * description, display name, and type.
     */
    protected static final NVConfig NVC_MESSAGE_TYPE = NVConfigManager.createNVConfig("message_type", "This configures the type of message.", "MessageType", true, false, GPSMessageType.class);


    public static final NVConfigEntity NVC_GPS_MESSAGE = new NVConfigEntityPortable("gps_message", null, "GPSMessage", true, false, false, false, GPSMessage.class, SharedUtil.toNVConfigList(NVC_MESSAGE_ID, NVC_MESSAGE_TYPE), null, false, GPSMessage.NVC_DATA_DAO);//,SharedUtil.extractNVConfigs( new Params[]{Params.REFERENCE_ID, Params.NAME, Params.LENGTH}));


    /**
     * This constructor creates a GPSMessage object
     * with the message parameters list defined.
     * @param nvce
     */
    protected GPSMessage(NVConfigEntity nvce) {
        super(nvce);
        // TODO Auto-generated constructor stub
    }


    /**
     * This method returns the message type.
     * @return
     */
    public GPSMessageType getMessageType() {
        return lookupValue(NVC_MESSAGE_TYPE);
    }

    /**
     * This method sets the message type.
     * @param messageType
     */
    public void setMessageType(GPSMessageType messageType) {
        setValue(NVC_MESSAGE_TYPE, messageType);
    }

    /**
     * This method returns the message ID.
     * @return
     */
    public GPSMessageID getMessageID() {
        return lookupValue(NVC_MESSAGE_ID);
    }

    /**
     * This method sets the message ID.
     * @param messageID
     */
    public void setMessageID(GPSMessageID messageID) {
        setValue(NVC_MESSAGE_ID, messageID);
    }


    public int computeCheckSum() {
        return checkSum(new String(getData()));
    }

//	/**
//	 * This method returns the checksum.
//	 * @return
//	 */
//	public String getCheckSum() 
//	{
//		return lookupValue(NVC_CHECKSUM);
//	}
//
//	/**
//	 * This method sets the checksum.
//	 * @param checkSum
//	 */
//	public void setCheckSum(String checkSum) 
//	{
//		setValue(NVC_CHECKSUM, checkSum);
//	}


    public static String filterGPSMessage(String message) {
        int dollarIndex = message.indexOf("$");
        int starIndex = message.indexOf("*");

        if (dollarIndex != -1 && starIndex != -1) {
            message = message.substring(dollarIndex + 1, starIndex);
        } else if (dollarIndex != -1 && starIndex == -1) {
            message = message.substring(dollarIndex + 1);
        } else if (dollarIndex == -1 && starIndex != -1) {
            message = message.substring(0, starIndex);
        }

        return message;

    }


    public static int checkSum(String message) {
        return checkSum(filterGPSMessage(message).getBytes());
    }


    public static int checkSum(byte[] message) {
        int check = 0;

        for (int i = 0; i < message.length; i++) {
            check = (int) ((check) ^ message[i]);
        }

        return check;
    }


}

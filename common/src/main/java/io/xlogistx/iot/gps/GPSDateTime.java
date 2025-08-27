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


import org.zoxweb.shared.util.*;

/**
 * This class is used to define the message parameters for the message 
 * protocol header $GPZDA. ZDA (Date & Time) describes the UTC time,
 * day, month, year, and local time zone.
 * @author mzebib
 *
 */
@SuppressWarnings("serial")
public class GPSDateTime extends GPSMessage {
    /**
     * This variable creates an NVConfig object for the
     * parameter which specifies the paramater's name,
     * description, display name, and type.
     */
    private static final NVConfig NVC_GPS_TIME = NVConfigManager.createNVConfig("gps_time", "Date and time in local zone.", "GPSTime", true, false, Long.class);

    /**
     * This variable creates an NVConfig object for the
     * parameter which specifies the paramater's name,
     * description, display name, and type.
     */
    private static final NVConfig NVC_TIME_ZONE = NVConfigManager.createNVConfig("time_zone", "Time zone in ZZ format (TwoLetterISO8601TimeZone) represents the sign (+ or -), hour, and minutes.", "TimeZone", true, false, String.class);


    private static final NVConfigEntity NVC_GPS_DATE_TIME = new NVConfigEntityPortable("gps_date_time", null, "GPSDateTime", true, false, false, false,
            GPSDateTime.class, SharedUtil.toNVConfigList(NVC_GPS_TIME, NVC_TIME_ZONE), null, false,
            NVC_GPS_MESSAGE);
    /**
     * This constructor creates a GPSDateTime object
     * with message parameters list defined and the GPS
     * message type set as output.
     */
    public GPSDateTime() {
        super(NVC_GPS_DATE_TIME);
        setMessageType(GPSMessageType.OUTPUT);
    }

    /**
     * This method returns the GPS time.
     * @return
     */
    public long getGPSTime() {
        return lookupValue(NVC_GPS_TIME);
    }

    /**
     * This method sets the GPS Time.
     * @param gpsTime
     */
    public void setGPSTime(long gpsTime) {
        setValue(NVC_GPS_TIME, gpsTime);
    }

    /**
     * This method returns the local time zone.
     * @return
     */
    public String getZZTimeZone() {
        return lookupValue(NVC_TIME_ZONE);
    }

    /**
     * This method sets the local time zone.
     * @param timeZone
     */
    public void setZZTimeZone(String timeZone) {
        setValue(NVC_TIME_ZONE, timeZone);
    }


}

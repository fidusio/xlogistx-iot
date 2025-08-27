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

import static io.xlogistx.iot.gps.GPSConst.CardinalDirection;
import static io.xlogistx.iot.gps.GPSConst.NavigationStatus;

/**
 * This class is used to define the message parameters for the message 
 * protocol header $GPGLL. GLL (Geographic Position - Latitude/Longitude)
 * describes geographic position, latitude/longitude, and time.
 * @author mzebib
 *
 */
@SuppressWarnings("serial")
public class GPSGeographicPosition extends GPSMessage {
    /**
     * This enum contains the messages parameters and creates an NVConfig
     * object for each parameter which specifies the paramater's name,
     * description, display name, and type.
     * @author mzebib
     *
     */
    private enum Params
            implements GetNVConfig {

        LATITUDE(NVConfigManager.createNVConfig("latitude", "Latitude in ddmm.mmmm format", "Latitude", false, true, Float.class)),
        NS_INDICATOR(NVConfigManager.createNVConfig("ns_indicator", "North/South indicator (N=north or S=south)", "NSIndicator", false, true, CardinalDirection.class)),
        LONGITUDE(NVConfigManager.createNVConfig("longitude", "Longitude in ddmm.mmmm format", "Longitude", false, true, Float.class)),
        EW_INDICATOR(NVConfigManager.createNVConfig("ew_indicator", "East/West indicator (E=east or W=west)", "EWIndicator", false, true, CardinalDirection.class)),
        UTC_TIME(NVConfigManager.createNVConfig("utc_time", "UTC time in hhmmss.SSS format", "UTCTime", false, true, Long.class)),
        STATUS(NVConfigManager.createNVConfig("status", "A=data valid or V=data not valid", "Status", false, true, NavigationStatus.class)),

        ;

        private final NVConfig cType;

        Params(NVConfig c) {
            cType = c;
        }

        public NVConfig getNVConfig() {
            // TODO Auto-generated method stub
            return cType;
        }
    }

    private static final NVConfigEntity NVC_GPS_GEOGRAPHIC_POSITION = new NVConfigEntityPortable("gps_geographic_position", null, "GPSGeographicPosition", true, false, false, false,
            GPSGeographicPosition.class,
            SharedUtil.extractNVConfigs(Params.values()),
            SharedUtil.toNVConfigList(
                    NVC_MESSAGE_ID,
                    Params.LATITUDE.getNVConfig(),
                    Params.NS_INDICATOR.getNVConfig(),
                    Params.LONGITUDE.getNVConfig(),
                    Params.EW_INDICATOR.getNVConfig(),
                    Params.UTC_TIME.getNVConfig(),
                    Params.STATUS.getNVConfig()),
            false,
            NVC_GPS_MESSAGE);

    /**
     * This constructor creates a GPSGeographicPosition object
     * with the message parameters list defined and the GPS
     * message type set as output.
     */
    public GPSGeographicPosition() {
        super(NVC_GPS_GEOGRAPHIC_POSITION);
        setMessageType(GPSMessageType.OUTPUT);
    }

    /**
     * This method returns the latitude parameter in the message.
     * @return
     */
    public float getLatitude() {
        return lookupValue(Params.LATITUDE);
    }

    /**
     * This method sets the latitude parameter in the message.
     * @param latitude
     */
    public void setLatitude(float latitude) {
        setValue(Params.LATITUDE, latitude);
    }

    /**
     * This method returns the north/south indicator parameter
     * in the message.
     * @return
     */
    public CardinalDirection getNSIndicator() {
        return lookupValue(Params.NS_INDICATOR);
    }

    /**
     * This method sets the north/south indicator parameter
     * in the message.
     * @param nsIndicator
     */
    public void setNSIndicator(CardinalDirection nsIndicator) {
        setValue(Params.NS_INDICATOR, nsIndicator);
    }

    /**
     * This method returns the longitude parameter in the message.
     * @return
     */
    public float getLongitude() {
        return lookupValue(Params.LONGITUDE);
    }

    /**
     * This method sets the longitude parameter in the message.
     * @param longitude
     */
    public void setLongitude(float longitude) {
        setValue(Params.LONGITUDE, longitude);
    }

    /**
     * This method returns the east/west indicator parameter
     * in the message.
     * @return
     */
    public CardinalDirection getEWIndicator() {
        return lookupValue(Params.EW_INDICATOR);
    }

    /**
     * This method sets the east/west indicator parameter
     * in the message.
     * @param ewIndicator
     */
    public void setEWIndicator(CardinalDirection ewIndicator) {
        setValue(Params.EW_INDICATOR, ewIndicator);
    }

    /**
     * This method returns the UTC time parameter in the message.
     * @return
     */
    public long getUtcTime() {
        return lookupValue(Params.UTC_TIME);
    }

    /**
     * This method sets the UTC time parameter in the message.
     * @param utcTime
     */
    public void setUtcTime(long utcTime) {
        setValue(Params.UTC_TIME, utcTime);
    }

    /**
     * This method returns the status parameter in the message.
     * @return
     */
    public NavigationStatus getStatus() {
        return lookupValue(Params.STATUS);
    }

    /**
     * This method sets the status parameter in the message.
     * @param status
     */
    public void setStatus(NavigationStatus status) {
        setValue(Params.STATUS, status);
    }


}

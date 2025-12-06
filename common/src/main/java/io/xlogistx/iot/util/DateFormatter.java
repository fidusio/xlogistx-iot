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

package io.xlogistx.iot.util;

import org.zoxweb.shared.data.DataDAO;
import org.zoxweb.shared.util.SharedUtil;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

public class DateFormatter {

    // Thread-safe DateTimeFormatter instances
    public final static DateTimeFormatter DEFAULT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public final static DateTimeFormatter GPS_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyhhmmss.SSS");
    public final static DateTimeFormatter GPS_UTC_TIME_FORMAT = DateTimeFormatter.ofPattern("HHmmss.SSS");
    public final static DateTimeFormatter GPS_ZDA_FORMAT = DateTimeFormatter.ofPattern("HHmmss.SSS,dd,MM,yyyyZZ");


    private DateFormatter() {
    }

    public static String toString(DataDAO mb) {
        Instant instant = Instant.ofEpochMilli(mb.getCreationTime());
        String formattedDate = DEFAULT_DATE_FORMAT.format(instant.atZone(ZoneOffset.UTC));
        return SharedUtil.toCanonicalID(':', mb.getSourceID(), formattedDate, new String(mb.getData()));
    }

    public static long parseUTCTime(String utcTime) throws ParseException {
        LocalTime time = LocalTime.parse(utcTime, GPS_UTC_TIME_FORMAT);
        return time.getLong(ChronoField.MILLI_OF_DAY);
    }

}

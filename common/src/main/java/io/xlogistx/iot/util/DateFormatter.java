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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DateFormatter {


    public final static SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public final static SimpleDateFormat GPS_DATE_FORMAT = new SimpleDateFormat("ddMMyyhhmmss.SSS");
    public final static SimpleDateFormat GPS_UTC_TIME_FORMAT = new SimpleDateFormat("hhmmss.SSS");
    public final static SimpleDateFormat GPS_ZDA_FORMAT = new SimpleDateFormat("hhmmss.SSS,dd,MM,yyyyZZ");
    private static Date TIME_ZERO = null;
    private final static Lock LOCK = new ReentrantLock();


    private DateFormatter() {

    }

    public static String toString(DataDAO mb) {
        return SharedUtil.toCanonicalID(':', mb.getSourceID(), DEFAULT_DATE_FORMAT.format(new Date(mb.getCreationTime())), new String(mb.getData()));
    }


    public static long parseUTCTime(String utcTime) throws ParseException {
        if (TIME_ZERO == null) {
            try {
                LOCK.lock();

                if (TIME_ZERO == null) {
                    TIME_ZERO = GPS_UTC_TIME_FORMAT.parse("000000.000");
                }
            } finally {
                LOCK.unlock();
            }
        }

        Date temp = GPS_UTC_TIME_FORMAT.parse(utcTime);

        return temp.getTime() - TIME_ZERO.getTime();
    }

}

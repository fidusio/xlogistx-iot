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


import org.zoxweb.shared.util.CanonicalID;
import org.zoxweb.shared.util.NVConfig;
import org.zoxweb.shared.util.NVConfigEntity;
import org.zoxweb.shared.util.NVConfigEntityPortable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used as a template class for all
 * input GPS messages.
 * @author mzebib
 *
 */
@SuppressWarnings("serial")
abstract public class GPSInput extends GPSMessage implements CanonicalID {

    protected static final NVConfigEntity NVC_GPS_INPUT = new NVConfigEntityPortable("gps_input", null, "GPSInput", true, false, false, false,
            GPSInput.class, new ArrayList<NVConfig>(), null, false, GPSMessage.NVC_GPS_MESSAGE);


    /**
     * This constructor creates a GPSInput object
     * with the message parameters list defined and the GPS
     * message type set as output.
     */
    protected GPSInput(List<NVConfigEntity> list) {
        super(NVC_GPS_INPUT);
        setMessageType(GPSMessageType.INPUT);
    }


}

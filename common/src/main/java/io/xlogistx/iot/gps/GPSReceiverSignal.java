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
 * protocol header $GPMSS. MSS (MSK Receiver Signal) describes signal-to-noise 
 * ratio, signal strength, frequency, and bit rate from a radio-beacon receiver.
 * @author mzebib
 *
 */
@SuppressWarnings("serial")
public class GPSReceiverSignal extends GPSMessage {

    private enum Params
            implements GetNVConfig {
        /**
         * This enum contains the messages parameters and creates an NVConfig
         * object for each parameter which specifies the paramater's name,
         * description, display name, and type.
         * @author mzebib
         *
         */
        SIGNAL_STRENGTH(NVConfigManager.createNVConfig("signal_strength", "Signal strength of tracked frequency in dB", "SignalStrength", false, true, Integer.class)),
        SIGNAL_TO_NOISE_RATIO(NVConfigManager.createNVConfig("signal_to_noise_ratio", "Signal-to-Noise of tracked frequency in dB", "SignaltoNoiseRatio", false, true, Integer.class)),
        BEACON_FREQUENCY(NVConfigManager.createNVConfig("beacon_frequency", "Currently tracked frequency in KHz", "BeaconFrequency", false, true, Float.class)),
        BEACON_BIT_RATE(NVConfigManager.createNVConfig("beacon_bit_rate", "Beacon bit rate in bits per second", "BeaconBitRate", false, true, Integer.class)),


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


    private static final NVConfigEntity NVC_GPS_RECEIVER_SIGNAL = new NVConfigEntityPortable("gps_receiver_signal", null, "GPSReceiverSignal", true, false, false, false,
            GPSReceiverSignal.class, SharedUtil.extractNVConfigs(Params.values()), SharedUtil.toNVConfigList(
            NVC_MESSAGE_ID,
            Params.SIGNAL_STRENGTH.getNVConfig(),
            Params.SIGNAL_TO_NOISE_RATIO.getNVConfig(),
            Params.BEACON_FREQUENCY.getNVConfig(),
            Params.BEACON_BIT_RATE.getNVConfig()
    ),
            false,
            NVC_GPS_MESSAGE);


    /**
     * This constructor creates a GPSReceiverSignal object
     * with the message parameters list defined and the GPS
     * message type set as output.
     */
    public GPSReceiverSignal() {
        super(NVC_GPS_RECEIVER_SIGNAL);
        setMessageType(GPSMessageType.OUTPUT);
    }


    /**
     * This method returns the signal strength parameter
     * in the message.
     * @return
     */
    public int getSignalStrength() {
        return lookupValue(Params.SIGNAL_STRENGTH);
    }


    /**
     * This method sets the signal strength parameter 
     * in the message.
     * @param signal
     */
    public void setSignalStrength(int signal) {
        setValue(Params.SIGNAL_STRENGTH, signal);
    }


    /**
     * This method returns the signal-to-noise ratio
     * parameter in the message.
     * @return
     */
    public int getSignalToNoiseRatio() {
        return lookupValue(Params.SIGNAL_TO_NOISE_RATIO);
    }


    /**
     * This method sets the signal-to-noise ratio
     * parameter in the message.
     * @param ratio
     */
    public void setSignalToNoiseRatio(int ratio) {
        setValue(Params.SIGNAL_TO_NOISE_RATIO, ratio);
    }


    /**
     * This method returns the beacon frequency parameter
     * in the message.
     * @return
     */
    public float getBeaconFrequency() {
        return lookupValue(Params.BEACON_FREQUENCY);
    }


    /**
     * This method sets the beacon frequency parameter
     * in the message.
     * @param frequency
     */
    public void setBeaconFrequency(float frequency) {
        setValue(Params.BEACON_FREQUENCY, frequency);
    }


    /**
     * This method returns the beacon bit rate parameter
     * in the message.
     * @return
     */
    public int getBeaconBitRate() {
        return lookupValue(Params.BEACON_BIT_RATE);
    }


    /**
     * This method sets the beacon bit rate parameter
     * in the message.
     * @param rate
     */
    public void setBeaconBitRate(int rate) {
        setValue(Params.BEACON_BIT_RATE, rate);
    }


}

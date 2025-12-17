/*
 * Copyright (c) 2012-2024 ZoxWeb.com LLC.
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

package io.xlogistx.iot.ngpio;


import org.zoxweb.shared.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * GPIO Pin enumeration for Pi4J v3 using BCM pin addresses directly.
 * This replaces the RaspiPin-based approach from Pi4J v1.x.
 */
public enum NGPIOPin
        implements GetValue<Integer>, GetName {

    // BCM GPIO pins - address is the BCM pin number
    GPIO_00(0),
    GPIO_01(1),
    GPIO_02(2),
    GPIO_03(3),
    GPIO_04(4),
    GPIO_05(5),
    GPIO_06(6),
    GPIO_07(7),
    GPIO_08(8),
    GPIO_09(9),
    GPIO_10(10),
    GPIO_11(11),
    GPIO_12(12),
    GPIO_13(13),
    GPIO_14(14),
    GPIO_15(15),
    GPIO_16(16),
    GPIO_17(17),
    GPIO_18(18),
    GPIO_19(19),
    GPIO_20(20),
    GPIO_21(21),
    GPIO_22(22),
    GPIO_23(23),
    GPIO_24(24),
    GPIO_25(25),
    GPIO_26(26),
    GPIO_27(27),
    GPIO_28(28),
    GPIO_29(29),
    GPIO_30(30),
    GPIO_31(31);


    private static final Lock lock = new ReentrantLock();

    public static class GPIONameMap {

        public final NGPIOPin gpioPin;
        public final String nameMap;

        GPIONameMap(NGPIOPin pin, String nameMap) {
            this.gpioPin = pin;
            this.nameMap = nameMap;
        }

        @Override
        public String toString() {
            return gpioPin.name() + ":" + nameMap;
        }
    }

    private static final Map<String, GPIONameMap> mappedGPIOs = new HashMap<String, GPIONameMap>();
    private final int bcmAddress;


    NGPIOPin(int bcmAddress) {
        this.bcmAddress = bcmAddress;
    }


    /**
     * Get the BCM address for this GPIO pin.
     * In Pi4J v3, this is used directly as the pin address.
     * @return BCM pin address
     */
    public int getBCMAddress() {
        return bcmAddress;
    }

    /**
     * Alias for getBCMAddress() to maintain compatibility.
     * @return BCM pin address
     */
    public int getBCMID() {
        return bcmAddress;
    }

    @Override
    public Integer getValue() {
        return bcmAddress;
    }

    @Override
    public String getName() {
        return name();
    }

    public static NGPIOPin lookup(int bcmAddress) {
        for (NGPIOPin p : values()) {
            if (bcmAddress == p.bcmAddress)
                return p;
        }
        return null;
    }

    public static NGPIOPin[] lookup(String... pinIDs) {
        if (pinIDs != null && pinIDs.length == 1 && "all".equalsIgnoreCase(pinIDs[0])) {
            return NGPIOPin.values();
        }

        List<NGPIOPin> ret = new ArrayList<NGPIOPin>();
        for (String pinID : pinIDs) {
            pinID = SharedStringUtil.trimOrNull(pinID);
            if (pinID != null) {
                NGPIOPin toAdd = null;
                // try to get the mapped

                GPIONameMap toFind = mappedGPIOs.get(pinID);
                // maybe mapped
                if (toFind != null)
                    toAdd = toFind.gpioPin;


                // maybe a string
                if (toAdd == null) {
                    pinID = pinID.replace('-', '_');
                    String[] split = pinID.split("_");
                    if (split.length == 2) {
                        try {
                            int n = Integer.parseInt(split[1]);
                            split[1] = String.format("%02d", n);
                            pinID = split[0] + "_" + split[1];
                        } catch (Exception e) {
                            // exception OK
                        }
                    }
                    toAdd = SharedUtil.lookupEnum(pinID, values());
                }

                // maybe by number
                if (toAdd == null) {
                    try {
                        int index = Integer.parseInt(pinID);
                        if (index > -1 && index < values().length) {
                            toAdd = values()[index];
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }

                // add if we have a matching GPIO
                if (toAdd != null) {
                    ret.add(toAdd);
                }
            }
        }

        return ret.toArray(new NGPIOPin[ret.size()]);
    }


    public static NGPIOPin mapGPIOName(String gpioNameUserDefinedName) {
        gpioNameUserDefinedName = SharedStringUtil.trimOrNull(gpioNameUserDefinedName);
        SUS.checkIfNulls("GPIO_ID:UserDefinedName can't be null", gpioNameUserDefinedName);

        String[] tokens = gpioNameUserDefinedName.split("_");
        if (tokens.length != 2)
            throw new IllegalArgumentException("invalid gpioNameUserDefinedName " + gpioNameUserDefinedName);

        return mapGPIOName(tokens[0], tokens[1]);
    }

    public static GPIONameMap toGPIONameMap(String gpioNameUserDefinedName) {
        gpioNameUserDefinedName = SharedStringUtil.trimOrNull(gpioNameUserDefinedName);
        SUS.checkIfNulls("GPIO_ID:UserDefinedName can't be null", gpioNameUserDefinedName);

        String[] tokens = gpioNameUserDefinedName.split(":");
        if (tokens.length != 2)
            return null;

        GPIONameMap ret = mappedGPIOs.get(tokens[1]);

        if (ret == null) {
            NGPIOPin pin = lookupGPIO(tokens[0]);
            if (pin == null) {
                throw new IllegalArgumentException("Invalid NGPIOPin name  " + tokens[0]);
            }
            ret = new GPIONameMap(pin, tokens[1]);
        }

        return ret;
    }


    public static NGPIOPin mapGPIOName(GPIONameMap gpio) {
        GPIONameMap ret = mappedGPIOs.get(gpio.nameMap);
        if (ret == null)
            return mapGPIOName(gpio.nameMap, gpio.gpioPin);

        return ret.gpioPin;
    }

    public static NGPIOPin mapGPIOName(String userDefinedName, NGPIOPin gpio) {
        return mapGPIOName(userDefinedName, gpio.getName());
    }

    public static NGPIOPin mapGPIOName(String userDefinedName, String gpioName) {
        userDefinedName = SharedStringUtil.trimOrNull(userDefinedName);
        SUS.checkIfNulls("GPIO name or GPIO can't be null", userDefinedName, gpioName);
        NGPIOPin gpio = lookupGPIO(gpioName);
        if (gpio == null)
            throw new IllegalArgumentException(gpioName + " not found");

        GPIONameMap data = new GPIONameMap(gpio, userDefinedName);
        lock.lock();
        try {
            mappedGPIOs.put(userDefinedName, data);
            mappedGPIOs.put(data.gpioPin.name(), data);
        } finally {
            lock.unlock();
        }
        return gpio;
    }

    public static NGPIOPin unmapGPIOName(String userDefinedName) {
        GPIONameMap toFind = mappedGPIOs.get(userDefinedName);
        if (toFind != null) {

            lock.lock();
            try {
                mappedGPIOs.remove(toFind.gpioPin.name());
                mappedGPIOs.remove(toFind.nameMap);
            } finally {
                lock.unlock();
            }

        }
        return toFind != null ? toFind.gpioPin : null;
    }


    public static Integer lookupAddress(String pinID) {
        NGPIOPin[] ret = lookup(pinID);
        return ret.length != 0 ? ret[0].getValue() : null;
    }

    public static GPIONameMap lookupGPIONameMap(int bcmAddress) {
        NGPIOPin gpioPin = NGPIOPin.lookup(bcmAddress);
        return gpioPin != null ? mappedGPIOs.get(gpioPin.name()) : null;
    }

    public static GPIONameMap lookupGPIONameMap(String pin) {
        return mappedGPIOs.get(pin);
    }

    public static NGPIOPin lookupGPIO(String pinID) {
        NGPIOPin[] ret = lookup(pinID);
        return ret.length != 0 ? ret[0] : null;
    }

    public String toString() {
        return name() + "-" + bcmAddress;
    }

}

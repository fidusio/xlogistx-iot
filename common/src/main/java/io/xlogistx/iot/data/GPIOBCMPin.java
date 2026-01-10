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

package io.xlogistx.iot.data;


import org.zoxweb.shared.util.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import static io.xlogistx.iot.data.IOTDataUtil.PinFunction;

/**
 * GPIO Pin enumeration for Pi4J v3 using BCM pin addresses directly.
 * This replaces the RaspiPin-based approach from Pi4J v1.x.
 */
public enum GPIOBCMPin
        implements GetValue<Integer>, GetName {

    // BCM GPIO pins - (bcmAddress, physicalPin, functions...)
    // Physical pin -1 means not available on standard 40-pin header
    GPIO_00(0, 27, PinFunction.GPIO, PinFunction.I2C),      // I2C0 SDA (EEPROM)
    GPIO_01(1, 28, PinFunction.GPIO, PinFunction.I2C),      // I2C0 SCL (EEPROM)
    GPIO_02(2, 3, PinFunction.GPIO, PinFunction.I2C),       // I2C1 SDA
    GPIO_03(3, 5, PinFunction.GPIO, PinFunction.I2C),       // I2C1 SCL
    GPIO_04(4, 7, PinFunction.GPIO, PinFunction.GPCLK, PinFunction.ONE_WIRE),  // GPCLK0, 1-Wire default
    GPIO_05(5, 29, PinFunction.GPIO),
    GPIO_06(6, 31, PinFunction.GPIO),
    GPIO_07(7, 26, PinFunction.GPIO, PinFunction.SPI),      // SPI0 CE1
    GPIO_08(8, 24, PinFunction.GPIO, PinFunction.SPI),      // SPI0 CE0
    GPIO_09(9, 21, PinFunction.GPIO, PinFunction.SPI),      // SPI0 MISO
    GPIO_10(10, 19, PinFunction.GPIO, PinFunction.SPI),     // SPI0 MOSI
    GPIO_11(11, 23, PinFunction.GPIO, PinFunction.SPI),     // SPI0 SCLK
    GPIO_12(12, 32, PinFunction.GPIO, PinFunction.PWM),     // PWM0
    GPIO_13(13, 33, PinFunction.GPIO, PinFunction.PWM),     // PWM1
    GPIO_14(14, 8, PinFunction.GPIO, PinFunction.UART),     // UART TXD
    GPIO_15(15, 10, PinFunction.GPIO, PinFunction.UART),    // UART RXD
    GPIO_16(16, 36, PinFunction.GPIO),
    GPIO_17(17, 11, PinFunction.GPIO),
    GPIO_18(18, 12, PinFunction.GPIO, PinFunction.PWM, PinFunction.PCM),  // PWM0 / PCM CLK
    GPIO_19(19, 35, PinFunction.GPIO, PinFunction.PWM, PinFunction.PCM),  // PWM1 / PCM FS
    GPIO_20(20, 38, PinFunction.GPIO, PinFunction.PCM),     // PCM DIN
    GPIO_21(21, 40, PinFunction.GPIO, PinFunction.PCM),     // PCM DOUT
    GPIO_22(22, 15, PinFunction.GPIO),
    GPIO_23(23, 16, PinFunction.GPIO),
    GPIO_24(24, 18, PinFunction.GPIO),
    GPIO_25(25, 22, PinFunction.GPIO),
    GPIO_26(26, 37, PinFunction.GPIO),
    GPIO_27(27, 13, PinFunction.GPIO),
    GPIO_28(28, -1, PinFunction.GPIO),  // Not on 40-pin header
    GPIO_29(29, -1, PinFunction.GPIO),  // Not on 40-pin header
    GPIO_30(30, -1, PinFunction.GPIO),  // Not on 40-pin header
    GPIO_31(31, -1, PinFunction.GPIO);  // Not on 40-pin header



    private static final Lock lock = new ReentrantLock();

    public static class GPIONameMap {

        public final GPIOBCMPin gpioPin;
        public final String nameMap;

        GPIONameMap(GPIOBCMPin pin, String nameMap) {
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
    private final int physicalPin;
    private final Set<PinFunction> functions;


    GPIOBCMPin(int bcmAddress, int physicalPin, PinFunction... functions) {
        this.bcmAddress = bcmAddress;
        this.physicalPin = physicalPin;
        this.functions = functions.length > 0
            ? EnumSet.copyOf(java.util.Arrays.asList(functions))
            : EnumSet.of(PinFunction.GPIO);
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

    /**
     * Get the physical pin number on the 40-pin header.
     * @return physical pin number, or -1 if not available on 40-pin header
     */
    public int getPhysicalPin() {
        return physicalPin;
    }

    /**
     * Check if this pin is available on the standard 40-pin header.
     * @return true if available on 40-pin header
     */
    public boolean isOnHeader() {
        return physicalPin > 0;
    }

    /**
     * Get all supported functions for this pin.
     * @return unmodifiable set of supported functions
     */
    public Set<PinFunction> getFunctions() {
        return java.util.Collections.unmodifiableSet(functions);
    }

    /**
     * Check if this pin supports a specific function.
     * @param function the function to check
     * @return true if the pin supports the function
     */
    public boolean supports(PinFunction function) {
        return functions.contains(function);
    }

    /**
     * Check if this pin supports PWM.
     * @return true if PWM is supported
     */
    public boolean supportsPWM() {
        return functions.contains(PinFunction.PWM);
    }

    /**
     * Check if this pin supports I2C.
     * @return true if I2C is supported
     */
    public boolean supportsI2C() {
        return functions.contains(PinFunction.I2C);
    }

    /**
     * Check if this pin supports SPI.
     * @return true if SPI is supported
     */
    public boolean supportsSPI() {
        return functions.contains(PinFunction.SPI);
    }

    /**
     * Check if this pin supports UART.
     * @return true if UART is supported
     */
    public boolean supportsUART() {
        return functions.contains(PinFunction.UART);
    }

    /**
     * Find all pins that support a specific function.
     * @param function the function to filter by
     * @return array of pins supporting the function
     */
    public static GPIOBCMPin[] findByFunction(PinFunction function) {
        List<GPIOBCMPin> result = new ArrayList<>();
        for (GPIOBCMPin pin : values()) {
            if (pin.supports(function)) {
                result.add(pin);
            }
        }
        return result.toArray(new GPIOBCMPin[0]);
    }

    @Override
    public Integer getValue() {
        return bcmAddress;
    }

    @Override
    public String getName() {
        return name();
    }

    public static GPIOBCMPin lookup(int bcmAddress) {
        for (GPIOBCMPin p : values()) {
            if (bcmAddress == p.bcmAddress)
                return p;
        }
        return null;
    }

    /**
     * Lookup a GPIO pin by its physical pin number on the 40-pin header.
     * @param physicalPin the physical pin number (1-40)
     * @return the GPIO64Pin or null if not found
     */
    public static GPIOBCMPin lookupByPhysicalPin(int physicalPin) {
        for (GPIOBCMPin p : values()) {
            if (physicalPin == p.physicalPin)
                return p;
        }
        return null;
    }

    public static GPIOBCMPin[] lookup(String... pinIDs) {
        if (pinIDs != null && pinIDs.length == 1 && "all".equalsIgnoreCase(pinIDs[0])) {
            return GPIOBCMPin.values();
        }

        List<GPIOBCMPin> ret = new ArrayList<GPIOBCMPin>();
        for (String pinID : pinIDs) {
            pinID = SharedStringUtil.trimOrNull(pinID);
            if (pinID != null) {
                GPIOBCMPin toAdd = null;
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

        return ret.toArray(new GPIOBCMPin[ret.size()]);
    }


    public static GPIOBCMPin mapGPIOName(String gpioNameUserDefinedName) {
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
            GPIOBCMPin pin = lookupGPIO(tokens[0]);
            if (pin == null) {
                throw new IllegalArgumentException("Invalid NGPIOPin name  " + tokens[0]);
            }
            ret = new GPIONameMap(pin, tokens[1]);
        }

        return ret;
    }


    public static GPIOBCMPin mapGPIOName(GPIONameMap gpio) {
        GPIONameMap ret = mappedGPIOs.get(gpio.nameMap);
        if (ret == null)
            return mapGPIOName(gpio.nameMap, gpio.gpioPin);

        return ret.gpioPin;
    }

    public static GPIOBCMPin mapGPIOName(String userDefinedName, GPIOBCMPin gpio) {
        return mapGPIOName(userDefinedName, gpio.getName());
    }

    public static GPIOBCMPin mapGPIOName(String userDefinedName, String gpioName) {
        userDefinedName = SharedStringUtil.trimOrNull(userDefinedName);
        SUS.checkIfNulls("GPIO name or GPIO can't be null", userDefinedName, gpioName);
        GPIOBCMPin gpio = lookupGPIO(gpioName);
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

    public static GPIOBCMPin unmapGPIOName(String userDefinedName) {
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
        GPIOBCMPin[] ret = lookup(pinID);
        return ret.length != 0 ? ret[0].getValue() : null;
    }

    public static GPIONameMap lookupGPIONameMap(int bcmAddress) {
        GPIOBCMPin gpioPin = GPIOBCMPin.lookup(bcmAddress);
        return gpioPin != null ? mappedGPIOs.get(gpioPin.name()) : null;
    }

    public static GPIONameMap lookupGPIONameMap(String pin) {
        return mappedGPIOs.get(pin);
    }

    public static GPIOBCMPin lookupGPIO(String pinID) {
        GPIOBCMPin[] ret = lookup(pinID);
        return ret.length != 0 ? ret[0] : null;
    }

    public String toString() {
        return name() + "[bcm=" + bcmAddress + ",pin=" + (physicalPin > 0 ? physicalPin : "N/A") + ",functions=" + functions + "]";
    }

}

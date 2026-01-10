package io.xlogistx.iot.gpio;

import io.xlogistx.iot.data.i2c.I2CCodecBase;

import org.zoxweb.server.util.LockHolder;
import org.zoxweb.shared.data.SimpleMessage;

import java.io.IOException;

/**
 * Common interface for I2C utility operations.
 * Implemented by I2CUtil (Pi4J v1) and I2C64Util (Pi4J v3).
 */
public interface I2CHandler {


    /**
     *
     * @return the lock holder associated with IC implementation
     */
    LockHolder getLockHolder();
    /**
     * Send an I2C command to a device.
     * @param bus the I2C bus number
     * @param address the device address
     * @param command the command to send
     * @param filterID optional filter ID for the response
     * @param repeat number of times to repeat the command
     * @return SimpleMessage containing the response
     * @throws IOException if an error occurs during communication
     */
    SimpleMessage sendI2CCommand(int bus, int address, String command, String filterID, int repeat) throws IOException;

    /**
     * Write raw data to an I2C device.
     * @param lockStat if true, acquire lock during write
     * @param bus the I2C bus number
     * @param address the device address
     * @param data the data to write
     * @throws Exception if an error occurs during write
     */
    void writeToI2C(boolean lockStat, int bus, int address, byte[] data) throws IOException;

    /**
     * Get all available I2C codecs.
     * @return array of I2CCodecBase instances
     */
    I2CCodecBase[] getI2CCodecs();


    int[] getI2CDeviceIDs(int bus, int startAddress, int endAddress) throws IOException;

    I2CIO getI2CIO(int bus, int address) throws IOException;

}

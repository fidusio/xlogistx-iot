package io.xlogistx.iot.gpio.i2c;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.util.Console;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.SharedUtil;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class I2CUtil
{
    private I2CUtil(){}

    public static I2CDevice[] scanI2CDevices(int bus, int startAddress, int endAddress) throws IOException,
            I2CFactory.UnsupportedBusNumberException
    {
        I2CBus i2CBus = I2CFactory.getInstance(bus);
        List<I2CDevice> ret = new ArrayList<I2CDevice>();
        for(int i = startAddress; i <= endAddress; i++)
        {
            I2CDevice i2CDevice = i2CBus.getDevice(i);
            if(i2CDevice != null)
            {
                try {
                    i2CDevice.read();
                    ret.add(i2CDevice);
                }
                catch (IOException e)
                {

                }
            }
        }

        try
        {
            i2CBus.close();
        }
        catch(Exception e){}
        return ret.toArray(new I2CDevice[ret.size()]);
    }


    public static void main(String[] args) throws InterruptedException, IOException, I2CFactory.UnsupportedBusNumberException {


        try {
            // create Pi4J console wrapper/helper
            // (This is a utility class to abstract some of the boilerplate code)
            final Console console = new Console();

            // print program title/header
            console.title("<-- The Pi4J Project -->", "I2C Example");

            // allow for user to exit program using CTRL-C
            console.promptForExit();

            int index = 0;
            int busID = SharedUtil.parseInt(args[index++]);
            int startAddress = SharedUtil.parseInt(args[index++]);
            int endAddress = SharedUtil.parseInt(args[index++]);

            // fetch all available busses
            try {
                int[] ids = I2CFactory.getBusIds();
                console.println("Found follow I2C busses: " + Arrays.toString(ids));
            } catch (IOException exception) {
                console.println("I/O error during fetch of I2C busses occurred");
            }

            // find available busses
//        for (int number = I2CBus.BUS_0; number <= I2CBus.BUS_17; ++number) {
//            try {
//                @SuppressWarnings("unused")
//                I2CBus bus = I2CFactory.getInstance(number);
//                console.println("Supported I2C bus " + number + " found");
//            } catch (IOException exception) {
//                console.println("I/O error on I2C bus " + number + " occurred");
//            } catch (UnsupportedBusNumberException exception) {
//                console.println("Unsupported I2C bus " + number + " required");
//            }
//        }

            // get the I2C bus to communicate on
            long ts = System.currentTimeMillis();
            //I2CBus i2c = I2CFactory.getInstance(I2CBus.BUS_1);
            I2CDevice[] devs = I2CUtil.scanI2CDevices(busID, startAddress, endAddress);
            ts = System.currentTimeMillis() - ts;
            console.println("it took " + Const.TimeInMillis.toString(ts) + " i2cDevices : " + devs.length);

            for (I2CDevice dev : devs) {
                console.print(String.format("%x", dev.getAddress()) + " ");
            }


            //int devAddress = Integer.parseInt(args[index++], 16);
//
//        console.println("device address:" + Integer.toHexString(devAddress));
//        i2c.getDevice(devAddress);

            // create an I2C device for an individual device on the bus that you want to communicate with
            // in this example we will use the default address for the TSL2561 chip which is 0x39.


            // next, lets perform am I2C READ operation to the TSL2561 chip
            // we will read the 'ID' register from the chip to get its part number and silicon revision number
//        console.println("... reading ID register from ADS1115");

//        byte[][] configs={
//
//        };
//
//        byte[] buffer = new byte[2];
//        for(int i=0; i < 4; i++)
//        {
//            int read = device.read(i, buffer, 0,2);
//
//            console.println("register:" + i + " bytes read:" + read + " data:" + SharedStringUtil.bytesToHex(buffer));
//        }

            //readADC1115(ads1115.getI2CDevice(), Float.parseFloat(args[index++]));


//        int response = device.read(TSL2561_REG_ID);
//        console.println("TSL2561 ID = " + String.format("0x%02x", response) + " (should be 0x50)");
//
//        // next we want to start taking light measurements, so we need to power up the sensor
//        console.println("... powering up TSL2561");
//        device.write(TSL2561_REG_CONTROL, TSL2561_POWER_UP);
//
//        // wait while the chip collects data
//        Thread.sleep(500);
//
//        // now we will perform our first I2C READ operation to retrieve raw integration
//        // results from DATA_0 and DATA_1 registers
//        console.println("... reading DATA registers from TSL2561");
//        int data0 = device.read(TSL2561_REG_DATA_0);
//        int data1 = device.read(TSL2561_REG_DATA_1);
//
//        // print raw integration results from DATA_0 and DATA_1 registers
//        console.println("TSL2561 DATA 0 = " + String.format("0x%02x", data0));
//        console.println("TSL2561 DATA 1 = " + String.format("0x%02x", data1));
//
//        // before we exit, lets not forget to power down light sensor
//        console.println("... powering down TSL2561");
//        device.write(TSL2561_REG_CONTROL, TSL2561_POWER_DOWN);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.err.println("Usage: [bus id] [start address] [end address inclusive]");
        }
    }

}

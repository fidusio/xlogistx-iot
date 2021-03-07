package io.xlogistx.iot.gpio.i2c;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.util.Console;
import io.xlogistx.iot.gpio.i2c.modules.I2CGeneric;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.io.UByteArrayOutputStream;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.SharedStringUtil;
import org.zoxweb.shared.util.SharedUtil;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
                    System.out.println(String.format("%x", i) + ":" + i2CDevice.read());
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
            String command = args[index++].toLowerCase();



            switch(command)
            {
                case "scan":
                {
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


                    // get the I2C bus to communicate on
                    long ts = System.currentTimeMillis();
                    //I2CBus i2c = I2CFactory.getInstance(I2CBus.BUS_1);
                    I2CDevice[] devs = I2CUtil.scanI2CDevices(busID, startAddress, endAddress);
                    ts = System.currentTimeMillis() - ts;
                    console.println("it took " + Const.TimeInMillis.toString(ts) + " i2cDevices : " + devs.length);

                    for (I2CDevice dev : devs) {
                        console.print(String.format("%x", dev.getAddress()) + " ");
                    }
                }
                break;
                case "id":
                {

                    String register = args[index++];
                    int busID = SharedUtil.parseInt(args[index++]);
                    int address = SharedUtil.parseInt(args[index++]);
                    int size = SharedUtil.parseInt(args[index++]);
                    I2CGeneric i2cDev = new I2CGeneric("generic", busID, address);




                    int counter = 0;
                    //i2cDev.getI2CDevice().write((byte)'I');
                    byte buffer[] = new byte[512];
                    byte[] i2cCommand = new byte[2];
                    i2cCommand[0] = (byte)register.charAt(0);
                    i2cCommand[1] = (byte)size;
                    for (int r = 0; r < 4; r++)
                    {
                        console.println("Attempt["+r+"] start:************************************"  );
                        UByteArrayOutputStream baos = new UByteArrayOutputStream();
                        console.println("command: " + SharedStringUtil.bytesToHex(i2cCommand));
                        i2cDev.getI2CDevice().write(i2cCommand);

                        int totalRead = 0;
                        do {
                            int toRead = buffer.length;
                            if (totalRead + buffer.length > size) {
                                toRead = size - totalRead;
                            }
                            console.println("toRead:" + toRead);
                            int read = i2cDev.getI2CDevice().read(buffer, 0, toRead);
                            totalRead += read;
                            baos.write(buffer, 0, read);
                        } while (totalRead < size);

                        for (int i = 0; i < baos.size(); i++) {
                            console.print(baos.byteAt(i) + " ");
                        }

//                    while((read = i2cDev.getI2CDevice().read()) != -1)
//                    {
//                        baos.write(read);
//                        console.print(baos.size() + ":" + read +", ");
//                        counter++;
//                        if(counter == 100)
//                            break;
//                    }

                        console.println("\n Buffer: " + baos.toString());
                        console.println("read " + baos.size());

                        console.println("Attempt["+r+"] end:***************************************"  );
                    }
                }
                break;
            }




        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.err.println("Usage: [bus id] [start address] [end address inclusive]");
        }
    }

}

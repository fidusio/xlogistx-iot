package io.xlogistx.iot.gpio.i2c;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.util.Console;
import io.xlogistx.iot.gpio.data.CommandToBytes;
import io.xlogistx.iot.gpio.i2c.modules.I2CGeneric;

import org.zoxweb.server.io.UByteArrayOutputStream;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.shared.util.BytesValue;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.SharedStringUtil;
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

    public static void write(I2CBaseDevice dev, CommandToBytes command) throws IOException
    {
        write(dev.getI2CDevice(), command);
    }

    public static void write(I2CDevice dev, CommandToBytes command) throws IOException
    {
        dev.write(command.data(), 0, command.size());
    }

    public static void main(String[] args) throws InterruptedException, IOException, I2CFactory.UnsupportedBusNumberException {

        long ts = System.currentTimeMillis();
        final Console console = new Console();
        try {
            // create Pi4J console wrapper/helper
            // (This is a utility class to abstract some of the boilerplate code)


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

                    //I2CBus i2c = I2CFactory.getInstance(I2CBus.BUS_1);
                    I2CDevice[] devs = I2CUtil.scanI2CDevices(busID, startAddress, endAddress);

                    //console.println("it took " + Const.TimeInMillis.toString(ts) + " i2cDevices : " + devs.length);

                    for (I2CDevice dev : devs) {
                        console.print(String.format("%x", dev.getAddress()) + " ");
                    }
                }
                break;
                case "command":
                {

                    String action = args[index++];
                    action = action.toUpperCase();
                    int busID = SharedUtil.parseInt(args[index++]);
                    int address = SharedUtil.parseInt(args[index++]);

                    I2CGeneric i2cDev = new I2CGeneric("generic", busID, address);
                    int counter = 0;
                    //i2cDev.getI2CDevice().write((byte)'I');
                    byte buffer[] = new byte[512];
                    CommandToBytes i2cCommand = new CommandToBytes(16, (byte)0);
                    //int i2cIndex = 0;
                    i2cCommand.command(action);
                    //i2cCommand[i2cIndex++] = (byte)action.charAt(0);
                    switch(action)
                    {
                        case "I":
                            {
                                int size = SharedUtil.parseInt(args[index++]);
                                i2cCommand.addByte((byte)size);
                                //i2cCommand[i2cIndex++] = (byte)size;
                                for (int r = 0; r < 4; r++)
                                {

                                    console.println("Attempt["+r+"] start:************************************"  );
                                    UByteArrayOutputStream baos = new UByteArrayOutputStream();
                                    console.println("command: " + SharedStringUtil.bytesToHex(i2cCommand.data(), 0, i2cCommand.size()));
                                    write(i2cDev, i2cCommand);

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

                                    console.println("\n Buffer: " + baos.toString());
                                    console.println("read " + baos.size());

                                    console.println("Attempt["+r+"] end:***************************************"  );
                                }
                            }
                            break;
                        case "S":
                            {
                                int i2cNewAddress = SharedUtil.parseInt(args[index++]);
                                i2cCommand.toBytes((byte)i2cNewAddress);
                                //i2cCommand[i2cIndex++] = (byte)i2cAddress;
                                console.println("I2C address change command: " + SharedStringUtil.bytesToHex(i2cCommand.data(), 0, i2cCommand.size()));
                                write(i2cDev, i2cCommand);
                                console.println("I2C response old: " + i2cDev.getI2CDevice().read() + " new "  +i2cDev.getI2CDevice().read());
                            }

                            break;
                        case "P":
                            {
                                // bad hack
                                int repeat = address;

                                List<I2CGeneric> i2cDevs = new ArrayList<I2CGeneric>();
                                for (;index < args.length;)
                                {
                                    address = SharedUtil.parseInt(args[index++]);
                                    i2cDevs.add(new I2CGeneric("generic", busID, address));
                                }

//                                        new I2CGeneric("generic", busID, address);
//                                int repeat = index < args.length ?  SharedUtil.parseInt(args[index++]) : 1;
                                console.println("ping command: " + SharedStringUtil.bytesToHex(i2cCommand.data(), 0, i2cCommand.size()));
                                long localTS = System.currentTimeMillis();
                                byte[] pingData = new byte[Integer.BYTES];
                                for(int i=0; i < repeat; i++)
                                {
                                    for (I2CGeneric i2c : i2cDevs) {
                                        write(i2c, i2cCommand);
//                                    TaskUtil.sleep(100);
                                        i2c.getI2CDevice().read(pingData, 0, pingData.length);
//                                    TaskUtil.sleep(50);
                                        console.println(i2c.getI2CDevice().getAddress() + ":ping: " + BytesValue.INT.toValue(pingData));
                                    }
                                }
                                localTS = System.currentTimeMillis() - localTS;
                                float rate = (float)localTS/(float)repeat;
                                console.println("ping count: " + repeat + " : " + Const.TimeInMillis.toString(localTS) + " millis per command: " + rate);
                            }
                            break;
                        case "F":
                        {

                            console.println("flip command: " + SharedStringUtil.bytesToHex(i2cCommand.data(), 0, i2cCommand.size()));
                            write(i2cDev, i2cCommand);
                            console.println("light status:" + i2cDev.getI2CDevice().read());

                        }
                            break;
                        case "R":
                        {

                            console.println("Reset command: " + SharedStringUtil.bytesToHex(i2cCommand.data(), 0, i2cCommand.size()));
                            write(i2cDev, i2cCommand);
                            console.println("Reset status:" + i2cDev.getI2CDevice().read());

                        }

                        break;
                        case "C":
                        case "L":
                        {
                            console.println(command + ": " + SharedStringUtil.bytesToHex(i2cCommand.data(), 0, i2cCommand.size()));
                            write(i2cDev, i2cCommand);
                            //console.println("set command sent");
                            byte[] pingData = new byte[4];
                            i2cDev.getI2CDevice().read(pingData, 0, pingData.length);
                            console.println(command + " resp: " + BytesValue.INT.toValue(pingData));
                        }
                        break;
                        case "A":
                        {
                            int repeat = index < args.length ?  SharedUtil.parseInt(args[index++]) : 1;
                            console.println("ADC read: " + SharedStringUtil.bytesToHex(i2cCommand.data(), 0, i2cCommand.size()));
                            byte[] pingData = new byte[4];
                            long localTS = System.currentTimeMillis();
                            float add = 0;
                            for( int i = 0; i < repeat ; i++) {
                                write(i2cDev, i2cCommand);
                                i2cDev.getI2CDevice().read(pingData, 0, pingData.length);
                                float value = BytesValue.FLOAT.toValue(pingData);
                                console.println("voltage value: " + value);
                                add += value;
                                if (repeat > 1)
                                    TaskUtil.sleep(10); // delay added to allow the ADC on the iot device to recover
                            }
                            localTS = System.currentTimeMillis() - localTS;
                            float rate = (float)localTS/(float)repeat;
                            console.println("voltage read count: " + repeat + " : " + Const.TimeInMillis.toString(localTS) + " millis per command: " + rate);
                            console.println("average:" + add/(float)repeat);
                        }
                        break;
                        case "W":
                        {
//                            i2cCommand[i2cIndex++] = (byte)SharedUtil.parseInt(args[index++]);// port
//                            i2cCommand[i2cIndex++] = (byte)SharedUtil.parseInt(args[index++]);// pwm 0-255
                            i2cCommand.toBytes((byte)SharedUtil.parseInt(args[index++]));// port
                            i2cCommand.toBytes((byte)SharedUtil.parseInt(args[index++]));// pwm 0-255

                            console.println("pwm set: " + SharedStringUtil.bytesToHex(i2cCommand.data(), 0, i2cCommand.size()));
                            write(i2cDev, i2cCommand);
                            int pwm = i2cDev.getI2CDevice().read();
                            console.println("pwm read set value:"  + pwm);
                        }
                        break;
                        case "V":
                        {
                            int pin = SharedUtil.parseInt(args[index++]);
                            int angle = SharedUtil.parseInt(args[index++]);
                            i2cCommand.toBytes((byte) pin);
                            i2cCommand.toBytes((byte) angle);


                            console.println("servo set: " +SharedStringUtil.bytesToHex(i2cCommand.data(), 0, i2cCommand.size()));
                            write(i2cDev, i2cCommand);
                            byte data[] = new byte[3];
//                            i2cDev.getI2CDevice().read(data, 0, data.length);
//                            short sNew = BytesValue.SHORT.toValue(data, 0, 2);
//                            short sOld = BytesValue.SHORT.toValue(data, 2, 2);
                            //i2cDev.getI2CDevice().read(data, 0, data.length);
                            int sNew = i2cDev.getI2CDevice().read(); //BytesValue.SHORT.toValue(data, 0, 2);
                            //int sOld = i2cDev.getI2CDevice().read(); //data[2]&0xFF;
                            console.println("servo new: "  + sNew + " old: ");
                        }
                        break;
                    }
                }
                break;
            }





        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.err.println("Ver 1.0-Usage: [bus id] [start address] [end address inclusive]");
        }

        ts = System.currentTimeMillis() - ts;
        console.println("It took: " + Const.TimeInMillis.toString(ts));


    }

}

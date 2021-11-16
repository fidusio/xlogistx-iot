package io.xlogistx.iot.gpio.i2c;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import io.xlogistx.common.data.CodecManager;
import io.xlogistx.common.data.MessageCodec;
import io.xlogistx.iot.gpio.data.*;
import io.xlogistx.iot.gpio.i2c.modules.I2CGeneric;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.Const;

import org.zoxweb.shared.util.SharedUtil;


import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;


public class I2CUtil
{
    public static final String VERSION = "I2C-UTIL-1.03.42";
    private static final Logger log = Logger.getLogger(I2CUtil.class.getName());
    private static final CodecManager<I2CMessageBase> I2C_CODEC_MANAGER = new CodecManager<I2CMessageBase>("I2CCodecManager", TokenFilter.UPPER_COLON, "I2CProtocol")
            .add(new I2CMessageCodec("ping", "Ping the device return the ping value as java int, usage: PING"))
            .add(new I2CMessageCodec("messages", "The number i2c messages processed by the device return the count value as java int, usage: MESSAGES"))
            .add(new I2CMessageCodec("cpu-speed", "Get the device cpu frequency in hz, value as java int, usage: CPU-SPEED"))
            .add(new I2CMessageCodec("aref", "Get the device aref , value as java short, usage: AREF"))
            .add(new I2CMessageCodec("reset", "Reboot the device, no return value bus will throw exception, usage: RESET"))
            //.add(I2CUptime.SINGLETON)
            .add(I2CVersion.SINGLETON)
            .add(I2CEcho.SINGLETON)
            .add(I2CAddress.SINGLETON)
            .add(new I2CIO());

    public static final I2CUtil SINGLETON = new I2CUtil();



    private Map<String, I2CBaseDevice> i2cDevices = new LinkedHashMap<String, I2CBaseDevice>();

    private I2CUtil(){}



    public SimpleMessage sendI2CCommand(int bus, int address, String command) throws IOException, I2CFactory.UnsupportedBusNumberException {
        SharedUtil.checkIfNulls("null command.", command);
        I2CBaseDevice i2cDevice = createI2CDevice("generic", bus, address);
        String rawCommand = command.toUpperCase();

        MessageCodec mc = I2C_CODEC_MANAGER.lookup(rawCommand);
        if(mc == null){
            throw new IllegalArgumentException("Command not supported: " + command);
        }
        CommandToBytes i2cCommand = (CommandToBytes) I2C_CODEC_MANAGER.lookup(rawCommand).encode(rawCommand);
        log.info("sending: " + rawCommand + " " + i2cCommand);
        byte[] respData = new byte[16];
        // we can only send and read one message at time
        // from the bus in the i2c implementation there is a lock
        // the current lock is just precautionary is case of implementation changes
        synchronized (this) {
                i2cDevice.getI2CDevice().read(i2cCommand.data(), 0, i2cCommand.size(), respData, 0, respData.length);
        }
        return (SimpleMessage) mc.decode(respData);
    }

    public I2CBaseDevice createI2CDevice(String name, int bus, int address) throws IOException, I2CFactory.UnsupportedBusNumberException {
        String i2cID  = I2CBaseDevice.i2cDeviceID(bus, address);
        I2CBaseDevice ret = i2cDevices.get(i2cID);
        if(ret == null)
        {
            // avoid double penetration
            ret = i2cDevices.get(i2cID);
            if(ret == null)
            {
                ret = new I2CGeneric(name, bus, address);
            }
        }

        return ret;
    }

    public CodecManager<I2CMessageBase> getI2cCodecManager()
    {
        return I2C_CODEC_MANAGER;
    }

    public I2CDevice[] scanI2CDevices(int bus, int startAddress, int endAddress) throws IOException,
            I2CFactory.UnsupportedBusNumberException
    {
        I2CBus i2CBus = I2CFactory.getInstance(bus);
        List<I2CDevice> ret = new ArrayList<I2CDevice>();
        for(int i = startAddress; i <= endAddress; i++)
        {
            I2CDevice i2CDevice = i2CBus.getDevice(i);
            if(i2CDevice != null)
            {
                try
                {
                    i2CDevice.read();
                    ret.add(i2CDevice);
                }
                catch (IOException e)
                {
                }
            }
        }

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




    public static void error(String token)
    {
        if (token != null){
            token = " " + token + " ";
        }
        else{
            token = "";
        }
        System.err.println();
        System.err.println("I2CUtil parameters");
        System.err.println(VERSION + " usage :" + token + " scan    bus-id start-address end-address");

        System.err.println(VERSION + " usage :" + token + " command bus-id i2c-device-address [i2cCommand]... \n\n");

        MessageCodec[] all = I2C_CODEC_MANAGER.all();
        for(MessageCodec mc : all)
        {
            System.err.println(VERSION + ":I2C command: " + mc);
        }
        System.exit(-1);
    }



    public static void main(String[] args) {

        long ts = System.currentTimeMillis();

        int commandCount = 0;
        try {
            // create Pi4J console wrapper/helper
            // (This is a utility class to abstract some of the boilerplate code)


            // print program title/header
            log.info("I2CUtil:" + VERSION);


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
                        log.info("Found follow I2C busses: " + Arrays.toString(ids));
                    } catch (IOException exception) {
                        log.info("I/O error during fetch of I2C busses occurred");
                    }
                    I2CDevice[] devs = I2CUtil.SINGLETON.scanI2CDevices(busID, startAddress, endAddress);

                    for (I2CDevice dev : devs) {
                        System.out.println("I2C Device Address: " + String.format("%x", dev.getAddress()));
                    }
                }
                break;
                case "command":
                    int busID = SharedUtil.parseInt(args[index++]);
                    int address = SharedUtil.parseInt(args[index++]);
                    for (; index < args.length;) {
                        commandCount++;
                        String rawCommand = args[index++];

                        SimpleMessage resp = I2CUtil.SINGLETON.sendI2CCommand(busID, address, rawCommand);


                        MessageCodec mc = I2C_CODEC_MANAGER.lookup(rawCommand);
                        System.out.println("Sending [" + rawCommand + "]");
                        StringBuilder sb = new StringBuilder();
                        sb.append("Request [" + rawCommand + "] response: " + GSONUtil.DEFAULT_GSON.toJson(resp));
                        System.out.println(commandCount + " " + sb.toString());
                    }
                break;
                default:
                    error(null);
            }





        }
        catch(Exception e)
        {
            e.printStackTrace();
            error(null);

        }

        ts = System.currentTimeMillis() - ts;
        log.info("It took: " + Const.TimeInMillis.toString(ts) + " to process " + commandCount + " commands.");


    }

}

package io.xlogistx.iot.gpio.i2c;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import io.xlogistx.common.data.CodecManager;
import io.xlogistx.common.data.MessageCodec;
import io.xlogistx.iot.gpio.data.*;
import io.xlogistx.iot.gpio.i2c.modules.I2CGeneric;
import org.zoxweb.server.http.OkHTTPCall;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.http.HTTPMessageConfig;
import org.zoxweb.shared.http.HTTPMessageConfigInterface;
import org.zoxweb.shared.http.HTTPResponseData;
import org.zoxweb.shared.http.HTTPStatusCode;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.ParamUtil;
import org.zoxweb.shared.util.SUS;
import org.zoxweb.shared.util.SharedStringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class I2CUtil
{

    public static final String VERSION = "I2C-UTIL-1.04.12";
    public static final LogWrapper log = new LogWrapper(I2CUtil.class);

    //private Map<String, I2CBaseDevice> i2cDevices = new HashMap<>();
    private static final CodecManager<I2CCodecBase> I2C_CODEC_MANAGER = new CodecManager<I2CCodecBase>("I2CCodecManager", TokenFilter.UPPER_COLON, "I2CProtocol")
            .add(new I2CCodec("ping", "Ping the device return the ping value as java int, usage: PING"))
            .add(new I2CCodec("messages", "The number i2c messages processed by the device return the count value as java int, usage: MESSAGES"))
            .add(new I2CCodec("cpu-speed", "Get the device cpu frequency in hz, value as java int, usage: CPU-SPEED"))
            .add(new I2CAref())
            .add(new I2CCodec("reset", "Reboot the device, no return value bus will throw exception, usage: RESET"))
            .add(I2CUptime.SINGLETON)
            .add(I2CVersion.SINGLETON)
            .add(I2CEcho.SINGLETON)
            .add(I2CAddress.SINGLETON)
            .add(new I2CIO());

    public static final I2CUtil SINGLETON = new I2CUtil();

    private final Lock lock = new ReentrantLock();



    //private final Map<String, I2CBaseDevice> i2cDevices = new LinkedHashMap<String, I2CBaseDevice>();

    private I2CUtil(){}

    public void acquireLock(boolean lockStat)
    {
        if(lockStat)
            lock.lock();
    }

    public void releaseLock(boolean lockStat)
    {
        if(lockStat)
            lock.unlock();
    }

    public SimpleMessage sendI2CCommand(int bus, int address, String command, String filterID, int repeat) throws IOException, I2CFactory.UnsupportedBusNumberException {
        SUS.checkIfNulls("null command.", command);
        if (repeat < 1)
            repeat = 1;
        I2CBaseDevice i2cDevice = createI2CDevice("generic", bus, address);
        String rawCommand = command.toUpperCase();

        I2CCodecBase mc = I2C_CODEC_MANAGER.lookup(rawCommand);
        if(mc == null)
        {
            throw new IllegalArgumentException("Command not supported: " + command);
        }



        I2CDevice i2cDev = i2cDevice.getI2CDevice();

        CommandToBytes i2cCommand = mc.encode(rawCommand);
        if  (log.isEnabled()) log.getLogger().info("sending: " + rawCommand + " " + i2cCommand);
        byte[] respData = new byte[mc.responseLength()];
        // we can only send and read one message at time
        // from the bus in the i2c implementation there is a lock
        // the current lock is just precautionary is case of implementation changes

        acquireLock(true);
        try
        {
            mc.resetTimeStamp();
            for(int i = 0; i < repeat; i++)
                i2cDev.read(i2cCommand.data(), 0, i2cCommand.size(), respData, 0, respData.length);

        // close the bus it is a must
        // to avoid bus read issues specially with io set commands
        }
        finally
        {
            IOUtil.close(i2cDevice);
            releaseLock(true);
        }

        SimpleMessage ret = mc.decode(I2CResp.build(bus, address, respData, filterID));

        return ret;
    }


    public void writeToI2C(boolean lockStat, int bus, int address, byte[] data)
            throws IOException, I2CFactory.UnsupportedBusNumberException
    {
        I2CBaseDevice i2cDevice = createI2CDevice("generic", bus, address);
        acquireLock(lockStat);
        try
        {
            I2CDevice i2cDev = i2cDevice.getI2CDevice();
            i2cDev.write(data);
        }
        finally
        {
            IOUtil.close(i2cDevice);
            releaseLock(lockStat);
        }
    }

    public I2CBaseDevice createI2CDevice(String name, int bus, int address) throws IOException, I2CFactory.UnsupportedBusNumberException {


        return new I2CGeneric(name, bus, address);


//        String i2cID  = I2CBaseDevice.i2cDeviceID(bus, address);
//        I2CBaseDevice ret = i2cDevices.get(i2cID);
//        if(ret == null)
//        {
//            synchronized (this)
//            {
//                ret = i2cDevices.get(i2cID);
//                if (ret == null) {
//                    ret = new I2CGeneric(name, bus, address);
//                    //i2cDevices.put(i2cID, ret);
//                }
//            }
//        }
//
//        return ret;
    }

    public CodecManager<I2CCodecBase> getI2cCodecManager()
    {
        return I2C_CODEC_MANAGER;
    }

    public synchronized I2CDevice[]  scanI2CDevices(int bus, int startAddress, int endAddress) throws IOException,
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
        try
        {
            if (i2CBus != null)
                i2CBus.close();
        }
        catch (Exception e)
        {

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
        System.err.println(VERSION + " usage :" + token + " i2c=scan bus=bus-id");

        System.err.println(VERSION + " usage :" + token + " [i2c=cmd] bus=bus-id address=i2c-address [uri=i2cCommand1 uri=2icCommand2]... \n\n");

        MessageCodec[] all = I2C_CODEC_MANAGER.all();
        for(MessageCodec mc : all)
        {
            System.err.println(VERSION + ":I2C command: " + mc);
        }
        System.exit(-1);
    }


    public static int exec(ParamUtil.ParamMap params) throws Exception
    {
        String i2cCommand = params.stringValue("i2c", "cmd");
        String user = params.stringValue("user", null);
        String password = params.stringValue("password", null);
        String url = params.stringValue("url", true);
        List<String> uris = params.lookup("uri");
        String httpMethod = params.stringValue("method", "GET");
        int busID = params.smartIntValue("bus", 0);
        int address = params.smartIntValue("address", 0);
        int repeat = params.intValue("repeat", 1);
        long delay = Const.TimeInMillis.toMillisNullZero(params.stringValue("delay", null));

        if  (log.isEnabled()) log.getLogger().info(""+params);
        int commandCount = 0;
        long ts = System.currentTimeMillis();
        switch(i2cCommand)
        {
            case "cmd":

                if (url != null)
                {
                    do {
                        for (String uri : uris) {
                            commandCount++;
                            try {
                                if (uri != null) {
                                    if (busID > 0)
                                        uri = SharedStringUtil.embedText(uri, "{bus}", "" + busID);

                                    if (address > 0)
                                        uri = SharedStringUtil.embedText(uri, "{address}", "" + address);
                                    HTTPMessageConfigInterface hmci = HTTPMessageConfig.createAndInit(url, uri, httpMethod, false);
                                    hmci.setBasicAuthorization(user, password);
                                    HTTPResponseData hrd = OkHTTPCall.send(hmci);
                                    if (hrd.getStatus() == HTTPStatusCode.OK.CODE) {
                                        if (log.isEnabled())
                                            log.getLogger().info(SharedStringUtil.toString(hrd.getData()));
                                    } else if (log.isEnabled()) log.getLogger().info("" + hrd);
                                }
                                if (delay > 0)
                                    TaskUtil.sleep(delay);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        repeat--;
                    } while (repeat > 0);
                }
                else
                {
                    for (String uri : uris) {
                        SimpleMessage resp = I2CUtil.SINGLETON.sendI2CCommand(busID, address, uri, null, repeat);


                        //MessageCodec mc2 = I2C_CODEC_MANAGER.lookup(rawCommand);
                        if (log.isEnabled()) log.getLogger().info("Sending [" + uri + "]");
                        StringBuilder sb = new StringBuilder();
                        sb.append("Request [" + uri + "] response: " + GSONUtil.toJSONDefault(resp));
                        if (log.isEnabled()) log.getLogger().info(commandCount + " " + sb.toString());
                    }

                }
                break;
            case "scan":
                try
                {
                    int[] ids = I2CFactory.getBusIds();
                    if  (log.isEnabled()) log.getLogger().info("Found follow I2C busses: " + Arrays.toString(ids));
                } catch (IOException exception) {
                    if  (log.isEnabled()) log.getLogger().info("I/O error during fetch of I2C busses occurred");
                }
                I2CDevice[] devs = I2CUtil.SINGLETON.scanI2CDevices(busID, 4, 127);

                for (I2CDevice dev : devs) {
                    if  (log.isEnabled()) log.getLogger().info("I2C Device Address: " + String.format("%x", dev.getAddress()));
                }
                break;
            default:
                error(null);
        }
        ts = System.currentTimeMillis() - ts;
        if  (log.isEnabled()) log.getLogger().info("It took: " + Const.TimeInMillis.toString(ts) + " to process " + commandCount + " commands.");
        return commandCount;

    }

    public static void main(String[] args) {
        try
        {
            // print program title/header
            if  (log.isEnabled()) log.getLogger().info("I2CUtil:" + VERSION);
            ParamUtil.ParamMap params = ParamUtil.parse("=", args);
            if  (log.isEnabled()) log.getLogger().info("" + params);

           exec(params);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            error(null);
        }
    }

}

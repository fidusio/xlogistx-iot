package io.xlogistx.iot.ngpio.i2c;

import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import io.xlogistx.iot.data.CommandToBytes;
import io.xlogistx.iot.data.IOTDataUtil;
import io.xlogistx.iot.data.i2c.I2CCodecBase;
import io.xlogistx.iot.data.i2c.I2CResp;
import io.xlogistx.iot.ngpio.NGPIOTools;
import io.xlogistx.iot.ngpio.i2c.modules.NI2CGeneric;
import org.zoxweb.server.http.OkHTTPCall;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.data.SimpleMessage;
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * I2C Utility class for Pi4J v3.
 * Uses Context-based I2C instead of I2CFactory from v1.x.
 */
public class NI2CUtil {

    public static final String VERSION = "NI2C-UTIL-1.00.00";
    public static final LogWrapper log = new LogWrapper(NI2CUtil.class);
//    public static final RegistrarMapDefault<String, DataFilter> DATA_FILTER = new RegistrarMapDefault<>(null, DataFilter::getID);
//
//    private static final RegistrarMapDefault<String, I2CCodecBase> I2C_CODEC_MANAGER = new RegistrarMapDefault<>(TokenFilter.UPPER_COLON, I2CCodecBase::getName)
//            .setNamedDescription(new NamedDescription("NI2CCodecManager", "NI2CProtocol"))
//            .registerValue(new I2CCodec("ping", "Ping the device return the ping value as java int, usage: PING"))
//            .registerValue(new I2CCodec("messages", "The number i2c messages processed by the device return the count value as java int, usage: MESSAGES"))
//            .registerValue(new I2CCodec("cpu-speed", "Get the device cpu frequency in hz, value as java int, usage: CPU-SPEED"))
//            .registerValue(new I2CCodec("reset", "Reboot the device, no return value bus will throw exception, usage: RESET"));

    public static final NI2CUtil SINGLETON = new NI2CUtil();

    private final Lock lock = new ReentrantLock();


    private NI2CUtil() {
    }

    public void acquireLock(boolean lockStat) {
        if (lockStat)
            lock.lock();
    }

    public void releaseLock(boolean lockStat) {
        if (lockStat)
            lock.unlock();
    }

    public SimpleMessage sendI2CCommand(int bus, int address, String command, String filterID, int repeat) throws IOException {
        SUS.checkIfNulls("null command.", command);
        if (repeat < 1)
            repeat = 1;
        NI2CBaseDevice i2cDevice = createI2CDevice("generic", bus, address);
        String rawCommand = command.toUpperCase();

        I2CCodecBase mc = IOTDataUtil.I2C_CODEC_MANAGER.lookup(rawCommand);
        if (mc == null) {
            throw new IllegalArgumentException("Command not supported: " + command);
        }


        I2C i2c = i2cDevice.getI2C();

        CommandToBytes i2cCommand = mc.encode(rawCommand);
        if (log.isEnabled()) log.getLogger().info("sending: " + rawCommand + " " + i2cCommand);
        byte[] respData = new byte[mc.responseLength()];

        acquireLock(true);
        try {
            mc.resetTimeStamp();
            for (int i = 0; i < repeat; i++) {
                // In Pi4J v3, we use write then read
                i2c.write(i2cCommand.data(), 0, i2cCommand.size());
                i2c.read(respData, 0, respData.length);
            }
        } finally {
            IOUtil.close(i2cDevice);
            releaseLock(true);
        }

        SimpleMessage ret = mc.decode(I2CResp.build(bus, address, respData, filterID));

        return ret;
    }


    public void writeToI2C(boolean lockStat, int bus, int address, byte[] data) throws IOException {
        NI2CBaseDevice i2cDevice = createI2CDevice("generic", bus, address);
        acquireLock(lockStat);
        try {
            I2C i2c = i2cDevice.getI2C();
            i2c.write(data);
        } finally {
            IOUtil.close(i2cDevice);
            releaseLock(lockStat);
        }
    }

    public NI2CBaseDevice createI2CDevice(String name, int bus, int address) throws IOException {
        return new NI2CGeneric(name, NGPIOTools.SINGLETON.getContext(), bus, address);
    }

    public I2CCodecBase[] getI2cCodecs() {
        List<I2CCodecBase> ret = new ArrayList<>();

        Iterator<I2CCodecBase> iter = IOTDataUtil.I2C_CODEC_MANAGER.values();
        while (iter.hasNext()) {
            ret.add(iter.next());
        }
        return ret.toArray(new I2CCodecBase[0]);
    }

    public synchronized I2C[] scanI2CDevices(int bus, int startAddress, int endAddress) throws IOException {
        List<I2C> ret = new ArrayList<I2C>();
        for (int i = startAddress; i <= endAddress; i++) {
            try {
                I2CConfig config = I2C.newConfigBuilder(NGPIOTools.SINGLETON.getContext())
                        .id("scan-" + bus + "-" + i)
                        .bus(bus)
                        .device(i)
                        .build();
                I2C i2c = NGPIOTools.SINGLETON.getContext().create(config);
                // Try to read from the device to see if it exists
                i2c.read();
                ret.add(i2c);
            } catch (Exception e) {
                // Device doesn't exist at this address
            }
        }

        return ret.toArray(new I2C[ret.size()]);
    }

    public static void write(NI2CBaseDevice dev, CommandToBytes command) throws IOException {
        write(dev.getI2C(), command);
    }

    public static void write(I2C dev, CommandToBytes command) throws IOException {
        dev.write(command.data(), 0, command.size());
    }


    public static int exec(ParamUtil.ParamMap params) throws Exception {
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

        if (log.isEnabled()) log.getLogger().info("" + params);
        int commandCount = 0;
        long ts = System.currentTimeMillis();
        switch (i2cCommand) {
            case "cmd":

                if (url != null) {
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
                } else {
                    for (String uri : uris) {
                        SimpleMessage resp = NI2CUtil.SINGLETON.sendI2CCommand(busID, address, uri, null, repeat);


                        //MessageCodec mc2 = I2C_CODEC_MANAGER.lookup(rawCommand);
                        if (log.isEnabled()) log.getLogger().info("Sending [" + uri + "]");
                        StringBuilder sb = new StringBuilder();
                        sb.append("Request [" + uri + "] response: " + GSONUtil.toJSONDefault(resp));
                        if (log.isEnabled()) log.getLogger().info(commandCount + " " + sb.toString());
                    }

                }
                break;
            case "scan":
//                try {
//                    int[] ids = NI2CFactory.getBusIds();
//                    if (log.isEnabled()) log.getLogger().info("Found follow I2C busses: " + Arrays.toString(ids));
//                } catch (IOException exception) {
//                    if (log.isEnabled()) log.getLogger().info("I/O error during fetch of I2C busses occurred");
//                }
                I2C[] devs = SINGLETON.scanI2CDevices(busID, 4, 127);

                for (I2C dev : devs) {
                    if (log.isEnabled())
                        log.getLogger().info("I2C Device Address: " + String.format("%x", dev.getDevice()));
                }
                break;
            default:
                error(null);
        }
        ts = System.currentTimeMillis() - ts;
        if (log.isEnabled())
            log.getLogger().info("It took: " + Const.TimeInMillis.toString(ts) + " to process " + commandCount + " commands.");
        return commandCount;

    }

    public static void error(String token) {
        if (token != null) {
            token = " " + token + " ";
        } else {
            token = "";
        }
        System.err.println();
        System.err.println("NI2CUtil parameters");
        System.err.println(VERSION + " usage :" + token + " i2c=scan bus=bus-id");

        System.err.println(VERSION + " usage :" + token + " [i2c=cmd] bus=bus-id address=i2c-address [uri=i2cCommand1 uri=2icCommand2]... \n\n");

        Iterator<I2CCodecBase> all = IOTDataUtil.I2C_CODEC_MANAGER.values();
        while (all.hasNext()) {
            System.err.println(VERSION + ":I2C command: " + all.next());
        }
        System.exit(-1);
    }


    public static void main(String[] args) {
        try {
            // print program title/header
            if (log.isEnabled()) log.getLogger().info("I2CUtil:" + VERSION);
            ParamUtil.ParamMap params = ParamUtil.parse("=", args);
            if (log.isEnabled()) log.getLogger().info("" + params);

            exec(params);
        } catch (Exception e) {
            e.printStackTrace();
            error(null);
        }
    }

}

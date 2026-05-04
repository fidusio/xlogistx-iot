package io.xlogistx.iot.gpio64.i2c;

import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import io.xlogistx.iot.data.CommandToBytes;
import io.xlogistx.iot.data.IOTDataUtil;
import io.xlogistx.iot.data.i2c.I2CCodecBase;
import io.xlogistx.iot.data.i2c.I2CResp;
import io.xlogistx.iot.gpio.I2CHandler;
import io.xlogistx.iot.gpio.I2CIO;
import io.xlogistx.iot.gpio64.GPIO64Tools;
import org.zoxweb.server.http.OkHTTPCall;
import org.zoxweb.shared.io.SharedIOUtil;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.server.util.LockHolder;
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


/**
 * I2C Utility class for Pi4J v3.
 * Uses Context-based I2C instead of I2CFactory from v1.x.
 */
public class I2C64Util implements I2CHandler {

    public static final String VERSION = "I2C-64-UTIL-PI4J-4.0.1-1.00.10";
    public static final LogWrapper log = new LogWrapper(I2C64Util.class).setEnabled(false);
//    private volatile Map<String, I2C> i2cMap = new ConcurrentHashMap<>();
//    public static final RegistrarMapDefault<String, DataFilter> DATA_FILTER = new RegistrarMapDefault<>(null, DataFilter::getID);
//
//    private static final RegistrarMapDefault<String, I2CCodecBase> I2C_CODEC_MANAGER = new RegistrarMapDefault<>(TokenFilter.UPPER_COLON, I2CCodecBase::getName)
//            .setNamedDescription(new NamedDescription("NI2CCodecManager", "NI2CProtocol"))
//            .registerValue(new I2CCodec("ping", "Ping the device return the ping value as java int, usage: PING"))
//            .registerValue(new I2CCodec("messages", "The number i2c messages processed by the device return the count value as java int, usage: MESSAGES"))
//            .registerValue(new I2CCodec("cpu-speed", "Get the device cpu frequency in hz, value as java int, usage: CPU-SPEED"))
//            .registerValue(new I2CCodec("reset", "Reboot the device, no return value bus will throw exception, usage: RESET"));

    public static final I2C64Util SINGLETON = new I2C64Util();

    private final LockHolder lockHolder = new LockHolder();


    private I2C64Util() {
    }

    public String version() {
        return VERSION;
    }

    @Override
    public LockHolder getLockHolder() {
        return lockHolder;
    }

    @Override
    public SimpleMessage sendI2CCommand(int bus, int address, String command, String filterID, int repeat) throws IOException {
        SUS.checkIfNulls("null command.", command);
        if (repeat < 1)
            repeat = 1;
        I2C64BaseDevice i2cDevice = createI2CDevice("generic", bus, address);
        String rawCommand = command.toUpperCase();

        I2CCodecBase mc = IOTDataUtil.I2C_CODEC_MANAGER.lookup(rawCommand);
        if (mc == null) {
            throw new IllegalArgumentException("Command not supported: " + command);
        }


        I2C i2c = i2cDevice.getI2C();

        CommandToBytes i2cCommand = mc.encode(rawCommand);
        if (log.isEnabled()) log.getLogger().info("sending: " + rawCommand + " " + i2cCommand);
        byte[] respData = new byte[mc.responseLength()];

        getLockHolder().lock(true);
        try {
            mc.resetTimeStamp();
            for (int i = 0; i < repeat; i++) {
                // In Pi4J v3, we use write then read
                i2c.write(i2cCommand.data(), 0, i2cCommand.size());
                i2c.read(respData, 0, respData.length);
            }
        } finally {
            getLockHolder().unlock(true);
        }

        SimpleMessage ret = mc.decode(I2CResp.build(bus, address, respData, filterID));

        return ret;
    }


    @Override
    public void writeToI2C(boolean lockStat, int bus, int address, byte[] data) throws IOException {
        I2C64BaseDevice i2cDevice = createI2CDevice("generic", bus, address);
        getLockHolder().lock(lockStat);
        try {
            I2C i2c = i2cDevice.getI2C();
            i2c.write(data);
        } finally {
            getLockHolder().unlock(lockStat);
        }
    }

    public I2C64BaseDevice createI2CDevice(String name, int bus, int address) throws IOException {
        return new I2C64Generic(name, bus, address);
    }


    public I2C createI2C(String name, int bus, int address) {
        String id = "I2C-" + (SUS.isNotEmpty(name) ? name + "-" : "") + bus + "-" + Integer.toHexString(address);
        if (log.isEnabled()) log.getLogger().info("Creating I2C device " + id);
        getLockHolder().lock(true);
        try {
            if (GPIO64Tools.SINGLETON.getContext().registry().exists(id)) {
                if (log.isEnabled()) log.getLogger().info(id + " already exists");
                return GPIO64Tools.SINGLETON.getContext().registry().get(id);
            }

            if (SUS.isEmpty(name))
                name = bus + "-" + address;

            I2CConfig config = I2C.newConfigBuilder(GPIO64Tools.SINGLETON.getContext())
                    .id(id)
                    .name(name)
                    .bus(bus)
                    .device(address)
                    .build();

            I2C i2c = GPIO64Tools.SINGLETON.getContext().create(config);
            try {
                int n = i2c.write(new byte[1], 0, 0);
                log.getLogger().info(id + " I2C exists wrote " + n);
//                if (n < 0) {
//                    if(log.isEnabled()) log.getLogger().info("I2C device " + id + " not found wrote < 0");
//                    SharedIOUtil.close(i2c);
//                    return null;
//                }
            } catch (Exception e) {
                if (log.isEnabled())
                    e.printStackTrace();

                if (log.isEnabled()) log.getLogger().info("probe failed for " + id + ": " + e);
                GPIO64Tools.SINGLETON.getContext().registry().remove(id);
                SharedIOUtil.close(i2c);

                return null;
            }
            return i2c;
        } finally {
            getLockHolder().unlock(true);
        }
    }

    public void releaseI2C(String name, int bus, int address) {
        String id = "I2C-" + (SUS.isNotEmpty(name) ? name + "-" : "") + bus + "-" + Integer.toHexString(address);
        getLockHolder().lock(true);
        try {
            if (GPIO64Tools.SINGLETON.getContext().registry().exists(id)) {

                com.pi4j.io.IO ioDev = GPIO64Tools.SINGLETON.getContext().registry().get(id);
                GPIO64Tools.SINGLETON.getContext().registry().remove(id);
                if (ioDev instanceof AutoCloseable)
                    SharedIOUtil.close((AutoCloseable) ioDev);
            }
        } finally {
            getLockHolder().unlock(true);
        }
    }

    @Override
    public I2CCodecBase[] getI2CCodecs() {
        List<I2CCodecBase> ret = new ArrayList<>();

        Iterator<I2CCodecBase> iter = IOTDataUtil.I2C_CODEC_MANAGER.values();
        while (iter.hasNext()) {
            ret.add(iter.next());
        }
        return ret.toArray(new I2CCodecBase[0]);
    }


    public int[] getI2CDeviceIDs(int bus, int startAddress, int endAddress) throws IOException {
        return scanI2CDevices(bus, startAddress, endAddress);
    }

    @Override
    public void close(int bus, int address) {
        releaseI2C("generic", bus, address);
    }

    @Override
    public I2CIO getI2CIO(int bus, int address) throws IOException {
        I2C i2c = createI2C(null, bus, address);
        if (i2c == null)
            throw new IOException("No I2C device at bus " + bus + " address 0x" + Integer.toHexString(address));
        return new I2CIO(address, getLockHolder(), i2c.getInputStream(), i2c.getOutputStream());
    }


    public int[] scanI2CDevices(int bus, int startAddress, int endAddress) {

        List<Integer> found = new ArrayList<>();
        getLockHolder().lock(true);
        try {
            for (int i = startAddress; i <= endAddress; i++) {
                try {
                    if (createI2C("scan", bus, i) != null)
                        found.add(i);
                } finally {
                    releaseI2C("scan", bus, i);
                }
            }
        } finally {
            getLockHolder().unlock(true);
        }

        int[] ret = new int[found.size()];
        for (int j = 0; j < found.size(); j++)
            ret[j] = found.get(j);
        return ret;
    }

    public static void write(I2C64BaseDevice dev, CommandToBytes command) throws IOException {
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
                        SimpleMessage resp = I2C64Util.SINGLETON.sendI2CCommand(busID, address, uri, null, repeat);


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
                int[] devs = SINGLETON.scanI2CDevices(busID, 4, 127);

                for (int dev : devs) {
                    if (log.isEnabled())
                        log.getLogger().info("I2C Device Address: " + String.format("%x", dev));
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

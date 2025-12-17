package io.xlogistx.iot.ngpio.i2c;

import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import io.xlogistx.iot.ngpio.NGPIOTools;
import io.xlogistx.iot.ngpio.data.*;
import io.xlogistx.iot.ngpio.i2c.modules.NI2CGeneric;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.filters.DataFilter;
import org.zoxweb.shared.filters.TokenFilter;
import org.zoxweb.shared.util.*;

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
    public static final RegistrarMapDefault<String, DataFilter> DATA_FILTER = new RegistrarMapDefault<>(null, DataFilter::getID);

    private static final RegistrarMapDefault<String, NI2CCodecBase> I2C_CODEC_MANAGER = new RegistrarMapDefault<>(TokenFilter.UPPER_COLON, NI2CCodecBase::getName)
            .setNamedDescription(new NamedDescription("NI2CCodecManager", "NI2CProtocol"))
            .registerValue(new NI2CCodec("ping", "Ping the device return the ping value as java int, usage: PING"))
            .registerValue(new NI2CCodec("messages", "The number i2c messages processed by the device return the count value as java int, usage: MESSAGES"))
            .registerValue(new NI2CCodec("cpu-speed", "Get the device cpu frequency in hz, value as java int, usage: CPU-SPEED"))
            .registerValue(new NI2CCodec("reset", "Reboot the device, no return value bus will throw exception, usage: RESET"));

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

        NI2CCodecBase mc = I2C_CODEC_MANAGER.lookup(rawCommand);
        if (mc == null) {
            throw new IllegalArgumentException("Command not supported: " + command);
        }


        I2C i2c = i2cDevice.getI2C();

        NCommandToBytes i2cCommand = mc.encode(rawCommand);
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

        SimpleMessage ret = mc.decode(NI2CResp.build(bus, address, respData, filterID));

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

    public NI2CCodecBase[] getI2cCodecs() {
        List<NI2CCodecBase> ret = new ArrayList<>();

        Iterator<NI2CCodecBase> iter = I2C_CODEC_MANAGER.values();
        while (iter.hasNext()) {
            ret.add(iter.next());
        }
        return ret.toArray(new NI2CCodecBase[0]);
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

    public static void write(NI2CBaseDevice dev, NCommandToBytes command) throws IOException {
        write(dev.getI2C(), command);
    }

    public static void write(I2C dev, NCommandToBytes command) throws IOException {
        dev.write(command.data(), 0, command.size());
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

        Iterator<NI2CCodecBase> all = I2C_CODEC_MANAGER.values();
        while (all.hasNext()) {
            System.err.println(VERSION + ":I2C command: " + all.next());
        }
        System.exit(-1);
    }


    public static void main(String[] args) {
        try {
            if (log.isEnabled()) log.getLogger().info("NI2CUtil:" + VERSION);
            ParamUtil.ParamMap params = ParamUtil.parse("=", args);
            if (log.isEnabled()) log.getLogger().info("" + params);

            String i2cCommand = params.stringValue("i2c", "cmd");
            int busID = params.smartIntValue("bus", 0);
            int address = params.smartIntValue("address", 0);
            int repeat = params.intValue("repeat", 1);

            switch (i2cCommand) {
                case "cmd":
                    List<String> uris = params.lookup("uri");
                    for (String uri : uris) {
                        SimpleMessage resp = NI2CUtil.SINGLETON.sendI2CCommand(busID, address, uri, null, repeat);
                        if (log.isEnabled()) log.getLogger().info("Request [" + uri + "] response: " + resp);
                    }
                    break;
                case "scan":
                    I2C[] devs = NI2CUtil.SINGLETON.scanI2CDevices(busID, 4, 127);
                    for (I2C dev : devs) {
                        if (log.isEnabled())
                            log.getLogger().info("I2C Device found at address: " + String.format("0x%02x", dev.device()));
                    }
                    break;
                default:
                    error(null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            error(null);
        }
    }

}

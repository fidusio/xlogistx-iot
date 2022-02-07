package io.xlogistx.iot.gpio.i2c;

import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import io.xlogistx.common.data.MessageCodec;

import io.xlogistx.common.data.PropertyHolder;
import io.xlogistx.iot.gpio.i2c.modules.ADS1115;
import org.zoxweb.shared.annotation.EndPointProp;
import org.zoxweb.shared.annotation.ParamProp;
import org.zoxweb.shared.annotation.SecurityProp;
import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.http.HTTPStatusCode;
import org.zoxweb.shared.security.SecurityConsts;
import org.zoxweb.shared.util.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

@SecurityProp(authentications = {SecurityConsts.AuthenticationType.BASIC,
        SecurityConsts.AuthenticationType.BEARER,
        SecurityConsts.AuthenticationType.JWT},
        roles = "local-admin,remote-admin")
public class I2CEndPoints
    extends PropertyHolder
{
    private static final Logger log = Logger.getLogger(I2CEndPoints.class.getName());


    @EndPointProp(methods = {HTTPMethod.GET}, name="i2c-command", uris="/i2c/command/{i2c-bus}/{i2c-address}/{command}")
    public SimpleMessage i2cCommand(@ParamProp(name="i2c-bus") int bus,
                                    @ParamProp(name="i2c-address") String addressID,
                                    @ParamProp(name="command") String command)
            throws IOException, I2CFactory.UnsupportedBusNumberException
    {
        int address = SharedUtil.parseInt(addressID);
        SimpleMessage response = I2CUtil.SINGLETON.sendI2CCommand(bus, address, command, 0);
        return response;
    }

    @EndPointProp(methods = {HTTPMethod.GET}, name="i2c-all-commands", uris="/i2c/commands")
    public SimpleMessage i2cSupportedCommands()
    {

        MessageCodec[] allMessages = I2CUtil.SINGLETON.getI2cCodecManager().all();
        SimpleMessage response = new SimpleMessage();
        response.setDescription("All supported messages");
        for(MessageCodec icmb : allMessages)
        {
            response.getProperties().add(icmb.getName(), icmb.getDescription());
        }
        return response;
    }

    @EndPointProp(methods = {HTTPMethod.GET}, name="i2c-scan-bus", uris="/i2c/scan/{i2c-bus}")
    public SimpleMessage i2cScanBus(@ParamProp(name="i2c-bus") int bus) throws IOException, I2CFactory.UnsupportedBusNumberException {
        I2CDevice[] actives = I2CUtil.SINGLETON.scanI2CDevices(bus, 1, 127);

        SimpleMessage response = new SimpleMessage();
        response.setDescription("List of i2c devices");
        response.getProperties().add(new NVInt("bus", bus));

        NVIntList list = new NVIntList("active-i2c-devices");

        for(I2CDevice icd : actives)
        {
            list.getValue().add(icd.getAddress());
        }
        response.getProperties().add(list);
        return response;
    }


    @EndPointProp(methods = {HTTPMethod.GET}, name="i2c-ads1115", uris="/i2c/ads1115/{bus}/{address_id}/{volt_ref}/{port}/{delay}")
    public SimpleMessage i2cADS1115(@ParamProp(name="bus") int bus,
                                    @ParamProp(name="address_id") String addressID,
                                    @ParamProp(name="volt_ref") float voltRef,
                                    @ParamProp(name="port", optional = true) ADS1115.Port[] ports)
            throws IOException, I2CFactory.UnsupportedBusNumberException {

        int address = Integer.parseInt(addressID, 16);
        String id = SharedUtil.toCanonicalID('-', "ADS1115", bus, Integer.toHexString(address));
        ADS1115 device = ResourceManager.SINGLETON.lookup(id);
        ADS1115.PGA pga = ADS1115.PGA.match(voltRef);

        if (pga == null)
            throw new IllegalArgumentException("Invalid volt reference " + voltRef);

        if(device == null)
        {
            synchronized (ResourceManager.SINGLETON)
            {
                if(ResourceManager.SINGLETON.lookup(id) == null)
                {
                    device = new ADS1115(bus, address);
                    ResourceManager.SINGLETON.map(device.toCanonicalID(), device);
                }
            }
        }


        log.info("bus: " + bus + " address: " + addressID + " volt-ref: " + pga + " ports: " + Arrays.toString(ports) + " delay:" + device.getDelay());
        SimpleMessage response = new SimpleMessage();

        if(ports == null)
            ports = ADS1115.Port.values();



        for( ADS1115.Port p: ports)
        {
            float volt = device.readPortInVolts(p,pga);
            response.getProperties().add(new NVFloat(p.name(), volt));
            log.info("bus: " + bus + " address: " + addressID + " volt-ref: " + pga + " port: " + p + " delay:" + device.getDelay() + " volt: " + volt);
        }

        response.setStatus(HTTPStatusCode.OK.CODE);
        response.setMessage("I2C ADS1115 ports.");
        return response;
    }

    @EndPointProp(methods = {HTTPMethod.GET}, name="i2c-ads1115-delay", uris="/i2c/ads1115/delay/{bus}/{address_id}/{delay}")
    public SimpleMessage i2cADS1115Delay(@ParamProp(name="bus") int bus,
                                         @ParamProp(name="address_id") String addressID,
                                         @ParamProp(name="delay", optional = true) String delay) throws IOException, I2CFactory.UnsupportedBusNumberException {

        int address = Integer.parseInt(addressID, 16);
        String id = SharedUtil.toCanonicalID('-', "ADS1115", bus, Integer.toHexString(address));
        ADS1115 device = ResourceManager.SINGLETON.lookup(id);



        if(device == null)
        {
            synchronized (ResourceManager.SINGLETON)
            {
                if(ResourceManager.SINGLETON.lookup(id) == null)
                {
                    device = new ADS1115(bus, address);
                    ResourceManager.SINGLETON.map(device.toCanonicalID(), device);
                }
            }
        }


        log.info("bus: " + bus + " address: " + addressID + " delay:" + delay);
        if(delay != null)
        {
            long delayInMillis = Const.TimeInMillis.toMillis(delay);
            device.setDelay(delayInMillis);
        }

        SimpleMessage response = new SimpleMessage();
        response.setStatus(HTTPStatusCode.OK.CODE);
        response.setMessage("I2C ADS1115 delay");
        response.getProperties().add("delay", Const.TimeInMillis.nanosToString(device.getDelay()));

        return response;
    }

    @Override
    protected void propertiesUpdated() {

    }
}

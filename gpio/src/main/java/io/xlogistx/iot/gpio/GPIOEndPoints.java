package io.xlogistx.iot.gpio;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.i2c.I2CFactory;
import io.xlogistx.common.data.PropertyHolder;
import io.xlogistx.iot.gpio.i2c.modules.ADS1115;
import org.zoxweb.shared.annotation.EndPointProp;
import org.zoxweb.shared.annotation.ParamProp;
import org.zoxweb.shared.annotation.SecurityProp;
import org.zoxweb.shared.data.Range;
import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.http.HTTPStatusCode;
import org.zoxweb.shared.http.URIScheme;
import org.zoxweb.shared.security.SecurityConsts;
import org.zoxweb.shared.util.*;

import java.io.IOException;

import java.util.Arrays;
import java.util.logging.Logger;


@SecurityProp(authentications = {SecurityConsts.AuthenticationType.BASIC,
                                 SecurityConsts.AuthenticationType.BEARER,
                                 SecurityConsts.AuthenticationType.JWT},
              roles = "local-admin,remote-admin")
public class GPIOEndPoints
extends PropertyHolder
{



    private static final Logger log = Logger.getLogger(GPIOEndPoints.class.getName());





    @EndPointProp(methods = {HTTPMethod.GET, HTTPMethod.POST}, name="output-pin", uris="/output/pin/{gpio}/{state}/{duration}")
    public SimpleMessage outputPin(@ParamProp(name="gpio") String gpio,
                                   @ParamProp(name="state") boolean state,
                                   @ParamProp(name="duration", optional = true) String durationParam)
    {
        Pin pin = GPIOPin.lookupPin(gpio);
        long duration = 0 ;
        if (durationParam != null)
        {
            duration = Const.TimeInMillis.toMillis(durationParam);
        }

        GPIOTools.SINGLETON.setOutputPin(pin, PinState.getState(state), duration);
        SimpleMessage response = new SimpleMessage(pin + " set successfully: " + (state ? Const.Bool.ON : Const.Bool.OFF),
                                                   HTTPStatusCode.OK.CODE);
        return response;
    }

    @EndPointProp(methods = {HTTPMethod.GET, HTTPMethod.POST}, name="output-pwm", uris="/output/pwm/{gpio}/{frequency}/{duty-cycle}/{duration}")
    public SimpleMessage outputPWM(@ParamProp(name="gpio") String gpio,
                                   @ParamProp(name="frequency") float freq,
                                   @ParamProp(name="duty-cycle") float ductyCycle,
                                   @ParamProp(name="duration", optional = true) String duration,
                                   @ParamProp(name="monitor", optional = true) boolean monitor)
    {

        GPIOPin pin = GPIOPin.lookupGPIO(gpio);

        if (duration != null) {

            duration = "" + Const.TimeInMillis.toMillis(duration);

        } else {
            duration = "" + 0;
        }
        GPIOTools.SINGLETON.setPWM(pin.getValue(), freq, ductyCycle, Const.TimeInMillis.toMillis(duration));

        SimpleMessage response = new SimpleMessage(pin + " pwm set successfully.",
                HTTPStatusCode.OK.CODE);
        return response;
    }


    @EndPointProp(methods = {HTTPMethod.GET, HTTPMethod.POST}, name="output-pwm-scan", uris="/output/pwm/scan/{gpio}/{frequency}/{lower-level}/{upper-level}/{delay}/{count}")
    public SimpleMessage outputPWM(@ParamProp(name="gpio") String gpio,
                                   @ParamProp(name="frequency") float freq,
                                   @ParamProp(name="lower-level") float lower,
                                   @ParamProp(name="upper-level") float upper,
                                   @ParamProp(name="delay") String delayParam,
                                   @ParamProp(name="count") int count)
    {

        GPIOPin pin = GPIOPin.lookupGPIO(gpio);
        Range<Float> dutyCycle = new Range<Float>(lower, upper);
        long delay = Const.TimeInMillis.toMillis(delayParam);


        GPIOTools.SINGLETON.setPWM(pin.getValue(), freq, dutyCycle, delay, count);

        SimpleMessage response = new SimpleMessage(pin + " pwm set successfully.",
                HTTPStatusCode.OK.CODE);
        return response;
    }


    @EndPointProp(methods = {HTTPMethod.GET, HTTPMethod.POST}, name="config-pwm", uris="/config/pwm/{range}")
    public void configPWM(@ParamProp(name="range") int range)
    {
        GPIOTools.SINGLETON.setPWMRangeMod(range, -1);
    }


    @EndPointProp(methods = {HTTPMethod.GET, HTTPMethod.POST}, name="read-pwm-config", uris="/lookup/pwm")
    @SecurityProp(authentications = {SecurityConsts.AuthenticationType.BASIC,
            SecurityConsts.AuthenticationType.BEARER,
            SecurityConsts.AuthenticationType.JWT},
            roles = "local-admin,remote-admin",
            protocols = {URIScheme.HTTPS})
    public NVGenericMap pwmConfig()
    {
        NVGenericMap ret = new NVGenericMap();
        ret.add(new NVInt("pwm_max_steps", GPIOTools.SINGLETON.getPWMRange()));
        ret.add(new NVPair("pwm_range", GPIOTools.PWM_RANGE.toString()));

        return ret;
    }

    @EndPointProp(methods = {HTTPMethod.GET, HTTPMethod.POST}, name="map-gpio", uris="/gpio/map/{gpio}/{name}")
    public SimpleMessage mapGPIO(@ParamProp(name="gpio") String gpio,
                                  @ParamProp(name="name") String name)
    {
        GPIOPin gpioPin = GPIOPin.lookupGPIO(gpio);
        GPIOPin.mapGIOName(name, gpioPin);

        SimpleMessage response = new SimpleMessage(gpioPin.getName() + " successfully mapped to " + name,
                HTTPStatusCode.OK.CODE);
        return response;
    }

    @EndPointProp(methods = {HTTPMethod.GET, HTTPMethod.DELETE}, name="map-gpio", uris="/gpio/unmap/{name}")
    public SimpleMessage unmapGPIO(@ParamProp(name="name") String name)
    {
        GPIOPin gpioPin = GPIOPin.unmapGIOName(name);
        if(gpioPin == null)
            throw  new IllegalArgumentException("gpio was never mapped to " + name);

        SimpleMessage response = new SimpleMessage(gpioPin.getName() + " name mapping removed from " + name,
                HTTPStatusCode.OK.CODE);
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

    protected void propertiesUpdated()
    {
        log.info("WE MUST UPDATE");
        if(getProperties() != null)
        {
            NVGenericMap gpiosMap = (NVGenericMap) getProperties().get("gpios-map");
            if (gpiosMap != null) {
                for (GetNameValue<?> pinInfo : gpiosMap.values()) {
                    try
                    {
                        GPIOPin gpio = GPIOPin.mapGIOName(pinInfo.getName(), ""+pinInfo.getValue());
                        log.info(gpio.getName() +" --> " + pinInfo.getName());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                log.info("GPIO-MAPS NOT FOUND");
            }

            NVGenericMap gpiosInit = (NVGenericMap) getProperties().get("gpios-init");
            if(gpiosInit != null)
            {
                for (GetNameValue<?> gnv : gpiosInit.values()) {
                    try
                    {
                        NVGenericMap pinConfig = (NVGenericMap) gnv;

                        GPIOPin pin = GPIOPin.lookupGPIO(pinConfig.getName());
                        log.info(getID()+" *********************************************Pin lookup:" + pinConfig + " pin:" +pin);

                        if(pin != null)
                        {

                            boolean state = Const.Bool.lookupValue("" + pinConfig.getValue("state"));
                            long duration = pinConfig.get("duration") != null ? Const.TimeInMillis.toMillis("" + pinConfig.getValue("duration")) : 0;

                            GPIOTools.SINGLETON.setOutputPin(pin.getValue(), PinState.getState(state), duration);
                            log.info(pin + " set " + state + " duration " + duration);

                        }
                        else
                        {
                            log.info("Pin not found:" + pinConfig.getName());
                        }

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        else
        {
            log.info("Properties is null");
        }
    }
}

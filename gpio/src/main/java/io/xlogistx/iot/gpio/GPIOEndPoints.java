package io.xlogistx.iot.gpio;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import io.xlogistx.common.data.PropertyHolder;
import org.zoxweb.shared.annotation.EndPointProp;
import org.zoxweb.shared.annotation.ParamProp;
import org.zoxweb.shared.annotation.SecurityProp;
import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.http.HTTPStatusCode;
import org.zoxweb.shared.security.SecurityConsts;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.GetNameValue;
import org.zoxweb.shared.util.NVGenericMap;

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
                                   @ParamProp(name="duty-cycle") float ductycycle,
                                   @ParamProp(name="duration", optional = true) String duration)
    {

        GPIOPin pin = GPIOPin.lookupGPIO(gpio);

        if (duration != null) {

            duration = "" + Const.TimeInMillis.toMillis(duration);

        } else {
            duration = "" + 0;
        }

        PWMConfig pwmConfig = new PWMConfig().gpioPinSetter(pin).frequencySetter(freq).dutyCycleSetter(ductycycle).durationSetter(duration);
        GPIOTools.SINGLETON.setPWM(pwmConfig);
        SimpleMessage response = new SimpleMessage(pin + " pwm set successfully.",
                HTTPStatusCode.OK.CODE);
        return response;
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

    protected void propertiesUpdated()
    {
        log.info("WE MUST UPDATE");
        if(getProperties() != null)
        {
            NVGenericMap gpioMaps = (NVGenericMap) getProperties().get("gpio-maps");
            if (gpioMaps != null) {
                for (GetNameValue<?> gnv : gpioMaps.values()) {
                    try
                    {
                        GPIOPin gpio = GPIOPin.mapGIOName(gnv.getName(), ""+gnv.getValue());
                        log.info(gpio.getName() +" --> " + gnv.getName());
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
        }
        else
        {
            log.info("Properties is null");
        }
    }
}

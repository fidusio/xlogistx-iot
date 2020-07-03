package io.xlogistx.iot.gpio;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import org.zoxweb.shared.annotation.EndPointProp;
import org.zoxweb.shared.annotation.ParamProp;
import org.zoxweb.shared.annotation.SecurityProp;
import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.http.HTTPStatusCode;
import org.zoxweb.shared.security.SecurityConsts;
import org.zoxweb.shared.util.Const;

public class GPIOEndPoints {

    @EndPointProp(methods = {HTTPMethod.GET}, name="gpio", uris="/output-pin/{gpio}/{state}/{duration}")
    @SecurityProp(authentications = {SecurityConsts.AuthenticationType.ALL})
    public SimpleMessage outputPin(@ParamProp(name="gpio") String gpio,
                                   @ParamProp(name="state") boolean state,
                                   @ParamProp(name="duration", optional = true) String duration)
    {
        Pin pin = GPIOPin.lookupPin(gpio);
        GPIOTools.SINGLETON.setOutputPinState(pin, PinState.getState(state), true, 0 , false);
        SimpleMessage response = new SimpleMessage(pin + " set successfully: " + (state ? Const.Bool.ON : Const.Bool.OFF),
                                                   HTTPStatusCode.OK.CODE);
        return response;
    }
}

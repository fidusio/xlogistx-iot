package io.xlogistx.iot.device.controller.impl;

import org.zoxweb.shared.annotation.EndPointProp;
import org.zoxweb.shared.annotation.ParamProp;
import org.zoxweb.shared.annotation.SecurityProp;
import org.zoxweb.shared.crypto.CryptoConst;
import org.zoxweb.shared.data.SimpleMessage;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.http.HTTPStatusCode;
import org.zoxweb.shared.http.URIScheme;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.NVPair;

import java.io.IOException;

@SecurityProp(authentications = {CryptoConst.AuthenticationType.ALL}, protocols = {URIScheme.HTTPS})
public class UBNTEndPoints {

    @EndPointProp(methods = {HTTPMethod.GET}, name = "ubnt-power", uris = "/ubnt/power/{ip}/{user}/{password}/{port}/{state}")

    public SimpleMessage ubntPortController(@ParamProp(name = "ip") String ip,
                                            @ParamProp(name = "user") String user,
                                            @ParamProp(name = "password") String password,
                                            @ParamProp(name = "port") int port,
                                            @ParamProp(name = "state") String state) throws IOException {


        String url = "https://" + ip;
        NVPair sessionCookie = UBNTEqpt.loginCookie(url, user, password, null);
        UBNTEqpt.controlPort(url, sessionCookie, port, Const.Bool.lookupValue(state), null);
        UBNTEqpt.getSensorsStatus(url, sessionCookie, null);

        return new SimpleMessage("port: " + port + "@" + ip + " set successfully to: " + state, HTTPStatusCode.OK.CODE);
    }


    @EndPointProp(methods = {HTTPMethod.GET}, name = "ubnt-reboot", uris = "/ubnt/reboot/{ip}/{user}/{password}")
    public SimpleMessage ubntSwitchReboot(@ParamProp(name = "ip") String ip,
                                          @ParamProp(name = "user") String user,
                                          @ParamProp(name = "password") String password) throws IOException {


        String url = "https://" + ip;

        UBNTEqpt.reboot(url, user, password, null);
        SimpleMessage ret = new SimpleMessage("device: " + ip + " successfully rebooted.", HTTPStatusCode.OK.CODE);
        return ret;
    }

}

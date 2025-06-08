/*
 * Copyright (c) 2012-2017 ZoxWeb.com LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.xlogistx.iot.device.controller.impl;

import org.zoxweb.server.http.HTTPCall;
import org.zoxweb.server.http.HTTPUtil;
import org.zoxweb.server.net.ssl.SSLCheckDisabler;
import org.zoxweb.shared.http.HTTPMessageConfig;
import org.zoxweb.shared.http.HTTPMessageConfigInterface;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.http.HTTPResponseData;
import org.zoxweb.shared.net.IPAddress;
import org.zoxweb.shared.util.NVPair;
import org.zoxweb.shared.util.SetNameValue;

import java.util.List;

public class UBNTSwitch {

    public static void main(String[] args) {
        try {
            int i = 0;
            String url = args[i++];
            String uri = args[i++];
            String user = args[i++];
            String passwd = args[i++];
            IPAddress proxy = new IPAddress("10.0.0.114", 8080);

            HTTPMessageConfigInterface hcc = new HTTPMessageConfig();
            hcc.setURL(url);
            hcc.setURI(uri);
            hcc.setProxyAddress(proxy);
            hcc.setMethod(HTTPMethod.GET);
            HTTPCall hc = new HTTPCall(hcc, SSLCheckDisabler.SINGLETON);
            HTTPResponseData rd = hc.sendRequest();
            System.out.println("" + rd);
            List<HTTPMessageConfigInterface> forms = HTTPUtil.extractFormsContent(rd, 0);
            System.out.println("" + forms);
            hcc = forms.get(0);
            hcc.setURL(url);
            hcc.setURI("login.cgi?uri=//");
            hcc.setRedirectEnabled(false);
            hcc.setProxyAddress(proxy);
            SetNameValue<String> username = (SetNameValue<String>) hcc.getParameters().get("username");//.setValue( user);
            username.setValue(user);
            SetNameValue<String> password = (SetNameValue<String>) hcc.getParameters().get("password");
            password.setValue(passwd);

            //NVPair cookie = SharedUtil.lookup(hcc.getHeaderParameters(), "Cookie");
            String cookie = HTTPUtil.extractRequestCookie(rd);

            hc = new HTTPCall(hcc, SSLCheckDisabler.SINGLETON);
            System.out.println("" + hcc);
            rd = hc.sendRequest();
            //System.out.println( ""+ rd);
            System.out.println("cookie" + cookie);
            HTTPMessageConfigInterface setSensor = HTTPMessageConfig.createAndInit(url, "sensors/1/", HTTPMethod.PUT);
            setSensor.setCookie(cookie);
            setSensor.getHeaders().add(new NVPair("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"));
            //setSensor.getHeaderParameters().add( new NVPair("X-Requested-With", "XMLHttpRequest"));
            //setSensor.getHeaderParameters().add( new NVPair("Referer", "http://10.0.1.51/power"));
            setSensor.getParameters().add(new NVPair("output", "1"));
            System.out.println("" + setSensor);
            hc = new HTTPCall(setSensor, SSLCheckDisabler.SINGLETON);

            System.out.println(hc.sendRequest().getStatus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

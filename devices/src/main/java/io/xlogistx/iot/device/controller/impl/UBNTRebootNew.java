package io.xlogistx.iot.device.controller.impl;

import org.zoxweb.server.http.HTTPCall;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.http.HTTPMessageConfig;
import org.zoxweb.shared.http.HTTPMessageConfigInterface;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.http.HTTPResponseData;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.NVPair;
import org.zoxweb.shared.util.SharedStringUtil;

import java.util.Locale;

public class UBNTRebootNew {
    public static void main(String ...args)
    {
        try
        {
            int index = 0;
            String username = args[index++];
            String password = args[index++];
            for (;index < args.length;) {
                String url = args[index++].toLowerCase();
                if(!url.startsWith("https://"))
                    url = "https://" + url;

                HTTPMessageConfigInterface hmci = HTTPMessageConfig.createAndInit(url, "/api/v1.0/user/login", HTTPMethod.POST, false);
                hmci.setContentType("application/json");
                NVGenericMap nvgm = new NVGenericMap();
                nvgm.add("username", username);
                nvgm.add("password", password);
                hmci.setContent(GSONUtil.toJSONGenericMap(nvgm, false, false, false));
                HTTPCall hc = new HTTPCall(hmci);
                HTTPResponseData rd = hc.sendRequest();


                String authToken = rd.getResponseHeaders().get("x-auth-token").get(0);
                hmci = HTTPMessageConfig.createAndInit(url, "/api/v1.0/system/reboot", HTTPMethod.POST, false);
                hmci.setContentType("application/json");
                hmci.getHeaderParameters().add(new NVPair("x-auth-token", authToken));

                hc = new HTTPCall(hmci);
                rd = hc.sendRequest();

                System.out.println("Device: " + url + " " + SharedStringUtil.toString(rd.getData()));
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}

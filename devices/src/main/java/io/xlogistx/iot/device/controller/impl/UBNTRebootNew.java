package io.xlogistx.iot.device.controller.impl;

import io.xlogistx.common.task.RunnableProperties;
import org.zoxweb.server.http.HTTPCall;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.http.HTTPMessageConfig;
import org.zoxweb.shared.http.HTTPMessageConfigInterface;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.http.HTTPResponseData;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.NVPair;
import org.zoxweb.shared.util.SharedStringUtil;


import java.util.logging.Logger;


public class UBNTRebootNew
        extends RunnableProperties {
    private static final Logger log = Logger.getLogger(UBNTRebootNew.class.getName());

    public UBNTRebootNew() {
    }

    public UBNTRebootNew(String host, String username, String password) {
        host = host.toLowerCase();
        if (!host.startsWith("https://"))
            host = "https://" + host;

        setProperties(new NVGenericMap());
        getProperties().add("host", host);
        getProperties().add("username", username);
        getProperties().add("password", password);

    }

    public void run() {
        try {
            HTTPMessageConfigInterface hmci = HTTPMessageConfig.createAndInit(getProperties().getValue("host"), "/api/v1.0/user/login", HTTPMethod.POST, false);
            hmci.setContentType("application/json");
            NVGenericMap nvgm = new NVGenericMap();
            nvgm.add(getProperties().get("username"));
            nvgm.add(getProperties().get("password"));

            hmci.setContent(GSONUtil.toJSONGenericMap(nvgm, false, false, false));
            //HTTPCall hc = new HTTPCall(hmci);
            HTTPResponseData rd = HTTPCall.send(hmci);//hc.sendRequest();


            String authToken = rd.getHeaders().get("x-auth-token").get(0);
            hmci = HTTPMessageConfig.createAndInit(getProperties().getValue("host"), "/api/v1.0/system/reboot", HTTPMethod.POST, false);
            hmci.setContentType("application/json");
            hmci.getHeaders().add(new NVPair("x-auth-token", authToken));

            //hc = new HTTPCall(hmci);
            rd = HTTPCall.send(hmci);
            log.info("Device: " + getProperties().getValue("host") + ", " + SharedStringUtil.toString(rd.getData()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String... args) {
        try {
            int index = 0;
            String username = args[index++];
            String password = args[index++];
            for (; index < args.length; ) {
                UBNTRebootNew ubntSwitch = new UBNTRebootNew(args[index++], username, password);
                ubntSwitch.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("usage: username password [hosts...]");
        }
    }
}

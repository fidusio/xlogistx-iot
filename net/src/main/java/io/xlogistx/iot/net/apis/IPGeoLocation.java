package io.xlogistx.iot.net.apis;

import org.zoxweb.server.http.OkHTTPCall;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.http.*;
import org.zoxweb.shared.util.GetNameValue;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.NVPair;
import org.zoxweb.shared.util.SharedStringUtil;

import java.io.IOException;

public class IPGeoLocation {


    public enum Params
            implements GetNameValue<String> {
        URL("url", "http://ip-api.com/json"),
        LATITUDE("lat", null),
        LONGITUDE("lon", null),
        IP("query", null);;
        private final String name;
        private final String value;

        Params(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getValue() {
            return value;
        }
    }


    private String endpoint;

    public IPGeoLocation() {
        this(Params.URL.getValue());
    }

    public IPGeoLocation(String endpoint) {
        this.endpoint = endpoint;
    }


    public NVGenericMap lookup()
            throws IOException {
        return lookup(null);
    }

    public NVGenericMap lookup(String ip)
            throws IOException {
        ip = SharedStringUtil.trimOrNull(ip);
        HTTPMessageConfigInterface hmci = HTTPMessageConfig.createAndInit(endpoint, null, HTTPMethod.GET);
        hmci.setHTTPParameterFormatter(HTTPEncoder.URI_REST_ENCODED);
        if (ip != null) {
            hmci.getParameters().add(new NVPair(Params.IP, ip));
        }
        //HTTPCall hc = new HTTPCall(hmci);
        HTTPResponseData hrd = OkHTTPCall.send(hmci);
        return GSONUtil.fromJSONGenericMap(hrd.getData());
    }

}

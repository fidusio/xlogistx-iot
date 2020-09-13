package io.xlogistx.iot.net.apis;

import org.zoxweb.server.http.HTTPCall;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.http.*;
import org.zoxweb.shared.util.GetNameValue;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.NVPair;

import java.io.IOException;

public class SunriseSunset {
    public enum Params
            implements GetNameValue<String>
    {
        URL("url", "https://api.sunrise-sunset.org/json"),
        LATITUDE("lat", null),
        LONGITUDE("lng", null),
        FORMATTED("formatted", "0"),
        DATE("date", null),
        SUNRISE("sunrise", null),
        SUNSET("sunset", null),

        ;
        private final String name;
        private final String value;
        Params(String name, String value)
        {
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
    public SunriseSunset()
    {
        this(Params.URL.getValue());
    }
    public SunriseSunset(String endpoint)
    {
        this.endpoint = endpoint;
    }


    public NVGenericMap lookup(float latitude, float longitude, String date)
            throws IOException
    {
        return lookup(""+latitude, "" + longitude, date);
    }

    public NVGenericMap lookup(double latitude, double longitude, String date)
            throws IOException
    {
        return lookup(""+latitude, "" + longitude, date);
    }

    public NVGenericMap lookup(String latitude, String longitude, String date)
            throws IOException
    {
        HTTPMessageConfigInterface hmci = HTTPMessageConfig.createAndInit(endpoint, null, HTTPMethod.GET);
        hmci.setHTTPParameterFormatter(HTTPEncoder.URL_ENCODED);
        hmci.getParameters().add(new NVPair(Params.LATITUDE, latitude));
        hmci.getParameters().add(new NVPair(Params.LONGITUDE, longitude));
        if(date!=null)
            hmci.getParameters().add(new NVPair(Params.DATE, date));
        hmci.getParameters().add(new NVPair(Params.FORMATTED));
        HTTPCall hc = new HTTPCall(hmci);
        HTTPResponseData hrd = hc.sendRequest();
        NVGenericMap result = GSONUtil.fromJSONGenericMap(hrd.getData());
        return (NVGenericMap) result.get("results");
    }
}

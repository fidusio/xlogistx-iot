package io.xlogistx.iot.net.apis;


import org.zoxweb.server.http.OkHTTPCall;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.http.*;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.GetNameValue;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.NVPair;

import java.io.IOException;
import java.util.Date;

public class SunriseSunset {
    public enum Params
            implements GetNameValue<String>
    {
        URL("url", "http://api.sunrise-sunset.org/json"),
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
        HTTPMessageConfigInterface hmci = HTTPMessageConfig.createAndInit(endpoint, null, HTTPMethod.GET, true);
        hmci.setRedirectEnabled(true);
        hmci.setHTTPParameterFormatter(HTTPEncoder.URL_ENCODED);
        hmci.getParameters().add(new NVPair(Params.LATITUDE, latitude));
        hmci.getParameters().add(new NVPair(Params.LONGITUDE, longitude));
        if(date!=null)
            hmci.getParameters().add(new NVPair(Params.DATE, date));
        hmci.getParameters().add(new NVPair(Params.FORMATTED));
        HTTPResponseData hrd = OkHTTPCall.send(hmci);
        NVGenericMap result = GSONUtil.fromJSONGenericMap(hrd.getData());
        return (NVGenericMap) result.get("results");
    }

    public static void main(String... args)
    {
        try
        {
            int index = 0;
            String ip = args.length > index ? args[index++] : null;
            IPGeoLocation ipGeoLocation = new IPGeoLocation();
            NVGenericMap resultGeoLoc = ipGeoLocation.lookup(ip);//"iot.xlogistx.io");
            SunriseSunset sunsetSunrise = new SunriseSunset();
            NVGenericMap resultSS = sunsetSunrise.lookup( (float)resultGeoLoc.getValue(IPGeoLocation.Params.LATITUDE), resultGeoLoc.getValue(IPGeoLocation.Params.LONGITUDE), null);


            System.out.println("sunrise: "+ new Date((long)resultSS.getValue(SunriseSunset.Params.SUNRISE.getName())) +" sunset: " +
                    new Date((long)resultSS.getValue(SunriseSunset.Params.SUNSET.getName())));


            long sunrise = resultSS.getValue(SunriseSunset.Params.SUNRISE.getName());
            long sunset = resultSS.getValue(SunriseSunset.Params.SUNSET.getName());

            long current = System.currentTimeMillis();
            long deltaSunrise = sunrise - current;
            long deltaSunset = sunset - current;

            System.out.println("To sunrise: " + deltaSunrise +  " " + Const.TimeInMillis.toString(deltaSunrise));
            System.out.println("To sunset: " + deltaSunrise +  " " + Const.TimeInMillis.toString(deltaSunset));
        }
        catch (Exception e)
        {
            e.printStackTrace();

        }
    }
}

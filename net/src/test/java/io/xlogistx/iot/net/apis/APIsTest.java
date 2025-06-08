package io.xlogistx.iot.net.apis;

import org.junit.jupiter.api.Test;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.NVGenericMap;

import java.io.IOException;
import java.util.Date;

public class APIsTest {
    @Test
    public void ipGeoLocationTest() throws IOException {
        IPGeoLocation ipGeoLocation = new IPGeoLocation();
        NVGenericMap result = ipGeoLocation.lookup();
        System.out.println("ip: " + result.getValue(IPGeoLocation.Params.IP) + " latitude: " +
                result.getValue(IPGeoLocation.Params.LATITUDE) + " longitude: " + result.getValue(IPGeoLocation.Params.LONGITUDE));
    }


    @Test
    public void sunsetSunriseTest() throws IOException {
        IPGeoLocation ipGeoLocation = new IPGeoLocation();
        NVGenericMap resultGeoLoc = ipGeoLocation.lookup();//"iot.xlogistx.io");
        System.out.println(resultGeoLoc);
        SunriseSunset sunsetSunrise = new SunriseSunset();
        NVGenericMap resultSS = sunsetSunrise.lookup((float) resultGeoLoc.getValue(IPGeoLocation.Params.LATITUDE), resultGeoLoc.getValue(IPGeoLocation.Params.LONGITUDE), null);


        System.out.println("sunrise: " + new Date((long) resultSS.getValue(SunriseSunset.Params.SUNRISE.getName())) + " sunset: " +
                new Date((long) resultSS.getValue(SunriseSunset.Params.SUNSET.getName())));


        long sunrise = resultSS.getValue(SunriseSunset.Params.SUNRISE.getName());
        long sunset = resultSS.getValue(SunriseSunset.Params.SUNSET.getName());

        long current = System.currentTimeMillis();
        long deltaSunrise = sunrise - current;
        long deltaSunset = sunset - current;

        System.out.println("To sunrise: " + deltaSunrise + " " + Const.TimeInMillis.toString(deltaSunrise));
        System.out.println("To sunset: " + deltaSunrise + " " + Const.TimeInMillis.toString(deltaSunset));

        // day sunset is always > sunrise
        // if deltaSunrise and deltaSunset negative, we are past sunset must look for next day
        // if deltaSunrise and deltaSunset positive, wait till sunrise
        // if deltaSunrise - and deltaSunset +, wait till sunset

    }


}

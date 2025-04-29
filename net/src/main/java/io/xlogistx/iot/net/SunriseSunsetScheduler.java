package io.xlogistx.iot.net;

import io.xlogistx.common.cron.CronScheduler;
import io.xlogistx.common.cron.CronTool.Type;


import io.xlogistx.iot.net.apis.IPGeoLocation;
import io.xlogistx.iot.net.apis.SunriseSunset;

import org.jetbrains.annotations.NotNull;
import org.zoxweb.server.http.HTTPCall;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.http.*;
import org.zoxweb.shared.util.*;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SunriseSunsetScheduler
    implements WaitTime<SunriseSunsetScheduler>, Runnable, CronScheduler
{

    private static final Logger log = Logger.getLogger(SunriseSunsetScheduler.class.getName());
    private final String ip;
    private final Set<Runnable> duringDay = new LinkedHashSet<Runnable>();
    private final Set<Runnable> duringNight = new LinkedHashSet<Runnable>();

    private final float longitude;
    private final float latitude;
    private long currentSunrise;
    private long currentSunset;
    private volatile long currentWait;
    private final Executor executor;
    private Type currentType;
    private volatile long lastUpdate = System.currentTimeMillis();


    public String getAPIService() {
        return apiService;
    }

    public void setAPIService(String apiService) {
        this.apiService = apiService;
    }

    private String apiService;

    public SunriseSunsetScheduler(Executor executor, String ip)
            throws IOException
    {
        this.ip = ip;
        this.executor = executor;

        IPGeoLocation ipGeoLocation = new IPGeoLocation();
        NVGenericMap resultGeoLoc = ipGeoLocation.lookup(ip);
        latitude = (float)resultGeoLoc.getValue(IPGeoLocation.Params.LATITUDE);
        longitude = (float)resultGeoLoc.getValue(IPGeoLocation.Params.LONGITUDE);
    }

    public SunriseSunsetScheduler(Executor executor, String ip, float latitude, float longitude)
            throws IOException
    {
        this.ip = ip;
        this.executor = executor;
        this.latitude = latitude;
        this.longitude = longitude;

    }


    @Override
    public long nextWait()
    {
        return currentWait;
    }






    @Override
    public SunriseSunsetScheduler getType() {
        return this;
    }

    public void run()
    {
        try
        {
            long deltaSunrise;
            long deltaSunset;
            if (getAPIService() == null) {
                SunriseSunset sunsetSunrise = new SunriseSunset();
                log.info("lat: " + latitude + " lon: " + longitude);
                NVGenericMap resultSS = sunsetSunrise.lookup(latitude, longitude, ip);


                log.info("SunRise: " + new Date((long) resultSS.getValue(SunriseSunset.Params.SUNRISE.getName())) + " SunSet: " +
                        new Date((long) resultSS.getValue(SunriseSunset.Params.SUNSET.getName())));


                currentSunrise = resultSS.getValue(SunriseSunset.Params.SUNRISE.getName());
                currentSunset = resultSS.getValue(SunriseSunset.Params.SUNSET.getName());

                lastUpdate = System.currentTimeMillis();
                deltaSunrise = currentSunrise - lastUpdate;
                deltaSunset = currentSunset - lastUpdate;
            }
            else
            {
                HTTPResponseData hrd = HTTPCall.send(HTTPMessageConfig.createAndInit(apiService, null, HTTPMethod.GET));
                NVGenericMap resp = GSONUtil.fromJSONGenericMap(hrd.getData());
                deltaSunrise = (int)resp.getValue("sunrise-millis");
                deltaSunset = (int)resp.getValue("sunset-millis");
            }

            log.info("To sunrise: " + deltaSunrise +  " " + Const.TimeInMillis.toString(deltaSunrise));
            log.info("To sunset: " + deltaSunset +  " " + Const.TimeInMillis.toString(deltaSunset));
            // day sunset is always > sunrise
            // if deltaSunrise and deltaSunset negative, we are past sunset must look for next day
            // if deltaSunrise and deltaSunset positive, wait till sunrise
            // if deltaSunrise - and deltaSunset +, wait till sunset

            if (deltaSunrise > 0 && deltaSunset > 0)
            {
                // if deltaSunrise and deltaSunset positive, wait till sunrise
                // NIGHT time

                currentType = Type.NIGHT;
                currentWait = deltaSunrise;
                synchronized (this)
                {
                    for (Runnable night : duringNight)
                        executeRunnable(night);
                }
            }
            if (deltaSunrise < 0 && deltaSunset > 0)
            {
                // DAY time
                currentType = Type.DAY;
                currentWait = deltaSunset;
                synchronized (this)
                {
                    for (Runnable day : duringDay)
                        executeRunnable(day);
                }


            }

            log.info("Now: " + currentType + ", will wait till: " + Const.TimeInMillis.toString(currentWait));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            currentWait = Const.TimeInMillis.SECOND.MILLIS*30;
        }

    }

    private void executeRunnable(Runnable rp)
    {
        if (executor != null)
            executor.execute(rp);
        else
            rp.run();
    }

    public synchronized boolean schedule(String cron, Runnable rp)
    {
        Type ct = Type.lookup(cron);
        SUS.checkIfNulls("Value not null", ct, rp);
        switch(ct)
        {
            case DAY:
                duringDay.add(rp);
                if(currentType == Type.DAY)
                    executeRunnable(rp);
                log.info("scheduled: " + ct);
                return true;
            case NIGHT:
                duringNight.add(rp);
                if(currentType == Type.NIGHT)
                    executeRunnable(rp);
                log.info("scheduled: " + ct);
                return true;
        }
        return false;
    }

    /**
     * Returns the remaining delay associated with this object, in the
     * given time unit.
     *
     * @param unit the time unit
     * @return the remaining delay; zero or negative values indicate
     * that the delay has already elapsed
     */
    @Override
    public long getDelay(@NotNull TimeUnit unit) {
        return currentWait - (System.currentTimeMillis() - lastUpdate);
    }
}

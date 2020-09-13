package io.xlogistx.iot.app;

import io.xlogistx.common.cron.CronConfig;
import io.xlogistx.common.cron.CronSchedulerConfig;
import io.xlogistx.common.cron.CronTask;
import io.xlogistx.common.cron.CronTool;

import io.xlogistx.iot.net.SunriseSunsetScheduler;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.logging.LoggerUtil;
import org.zoxweb.server.task.TaskUtil;

import org.zoxweb.server.util.GSONUtil;




import java.util.logging.Logger;

public class CronDayNight {
    private static final transient Logger log = Logger.getLogger(CronDayNight.class.getName());
    public static void main(String ...args)
    {
        try
        {
            LoggerUtil.enableDefaultLogger("io.xlogistx");
            int index = 0;


            String filename = args[index++];

            CronTool ct = new CronTool(TaskUtil.getDefaultTaskScheduler());
            SunriseSunsetScheduler ssc = new SunriseSunsetScheduler(TaskUtil.getDefaultTaskProcessor(), null);
            CronTask cronTask = ct.registerCronTask("day", ssc, ssc);
            ct.registerCronTask("night", cronTask);

            CronConfig cc = GSONUtil.fromJSON(IOUtil.inputStreamToString(filename), CronConfig.class);
            for (CronSchedulerConfig scs : cc.getConfigs())
            {
                ct.cron(scs);
            }

//            for(int i = index; i < args.length; i++)
//            {
//                String url = args[i];
//                IOTDeviceConfig day = new IOTDeviceConfig();
//                IOTDeviceConfig night = new IOTDeviceConfig();
//                day.getProperties().add("url", url);
//                day.getProperties().add("user", user);
//                day.getProperties().add("password", password);
//                day.getProperties().add(new NVInt("port", port));
//                day.getProperties().add(new NVBoolean("state", false));
//                night.getProperties().add("url", url);
//                night.getProperties().add("user", user);
//                night.getProperties().add("password", password);
//                night.getProperties().add(new NVInt("port", port));
//                night.getProperties().add(new NVBoolean("state", true));
//                ct.cron("day", new UBNTPowerController(day.getProperties()));
//                ct.cron("night", new UBNTPowerController(night.getProperties()));
//            }


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

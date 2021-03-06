package io.xlogistx.iot.app;

import io.xlogistx.common.cron.CronConfig;
import io.xlogistx.common.cron.CronSchedulerConfig;
import io.xlogistx.common.cron.CronTask;
import io.xlogistx.common.cron.CronTool;
import io.xlogistx.http.HTTPServerCreator;
import io.xlogistx.iot.gpio.GPIOFlowProcessor;
import io.xlogistx.iot.gpio.PinStateMonitorConfig;
import io.xlogistx.iot.net.SunriseSunsetScheduler;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.task.TaskUtil;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.http.HTTPServerConfig;
import org.zoxweb.shared.util.ParamUtil;

import java.io.File;
import java.util.logging.Logger;

public class Main
{
    private final static Logger log = Logger.getLogger(Main.class.getName());
    public static void main(String ...args)
    {
        try
        {
            ParamUtil.ParamMap params = ParamUtil.parse("-", args);
            String wsConfig = params.stringValue("-wsc", true);
            String flowConfig = params.stringValue("-fc", true);
            String cronConfig = params.stringValue("-cc", true);



            if(wsConfig != null)
            {
                File file = IOUtil.locateFile(wsConfig);
                HTTPServerConfig hsc = GSONUtil.fromJSON(IOUtil.inputStreamToString(file), HTTPServerConfig.class);
                log.info("HTTPServerConfig:" + hsc);
                log.info("WebServer json:" + hsc.getConnectionConfigs());
                HTTPServerCreator httpServerCreator = new HTTPServerCreator();
                httpServerCreator.setAppConfig(hsc);
                httpServerCreator.createApp();
            }
            if(flowConfig != null)
            {
                File file = IOUtil.locateFile(flowConfig);
                String jsonFC = IOUtil.inputStreamToString(file);
                log.info("" + jsonFC);
                PinStateMonitorConfig pinStateMonitorConfig = GSONUtil.DEFAULT_GSON.fromJson(jsonFC, PinStateMonitorConfig.class);
                new GPIOFlowProcessor(pinStateMonitorConfig, TaskUtil.getDefaultTaskScheduler()).init();
            }
            if(cronConfig != null)
            {

                CronConfig cc = GSONUtil.fromJSON(IOUtil.inputStreamToString(cronConfig), CronConfig.class);

                CronTool ct = new CronTool(TaskUtil.getDefaultTaskScheduler());
                TaskUtil.getDefaultTaskScheduler().queue(cc.getSetupDelay(), ()->
                {
                    try
                    {
                        SunriseSunsetScheduler ssc = new SunriseSunsetScheduler(TaskUtil.getDefaultTaskProcessor(), null);
                        CronTask cronTask = ct.registerCronTask("day", ssc, ssc);
                        ct.registerCronTask("night", cronTask);
                        for (CronSchedulerConfig scs : cc.getConfigs())
                        {
                            ct.cron(scs);
                        }
                    }
                    catch (Exception e)
                    {
                        error(e);
                    }
                });

            }

            if(wsConfig == null && flowConfig == null && cronConfig == null)
                throw new IllegalArgumentException("No config found");

        }
        catch(Exception e)
        {
           error(e);
        }
    }


    private static void error(Exception e)
    {
        e.printStackTrace();
        System.err.println("command: [-wsc web-server-config.json] [-fc flow-config.json] [-cc cron_config.json]");
        System.exit(-1);
    }
}

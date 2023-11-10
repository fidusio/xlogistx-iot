package io.xlogistx.iot.app;

import io.xlogistx.common.cron.CronConfig;
import io.xlogistx.common.cron.CronSchedulerConfig;
import io.xlogistx.common.cron.CronTask;
import io.xlogistx.common.cron.CronTool;
import io.xlogistx.http.NIOHTTPServerCreator;
import io.xlogistx.iot.gpio.GPIOFlowProcessor;
import io.xlogistx.iot.gpio.PinStateMonitorConfig;
import io.xlogistx.iot.gpio.i2c.I2CUtil;
import io.xlogistx.iot.net.SunriseSunsetScheduler;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.logging.LoggerUtil;
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
            LoggerUtil.enableDefaultLogger("io.xlogistx");
            ParamUtil.ParamMap params = ParamUtil.parse("-", args);
            String wsConfig = params.stringValue("-wsc", true);
            String flowConfig = params.stringValue("-fc", true);
            String cronConfig = params.stringValue("-cc", true);
            String i2cCommand = params.stringValue("-i2c", true);



            if(wsConfig != null)
            {
                File file = IOUtil.locateFile(wsConfig);
                HTTPServerConfig hsc = GSONUtil.fromJSON(IOUtil.inputStreamToString(file), HTTPServerConfig.class);
                log.info("HTTPServerConfig:" + hsc);
                log.info("WebServer json:" + hsc.getConnectionConfigs());
                NIOHTTPServerCreator httpServerCreator = new NIOHTTPServerCreator();
                httpServerCreator.setAppConfig(hsc);
                httpServerCreator.createApp();
            }
            if(flowConfig != null)
            {
                File file = IOUtil.locateFile(flowConfig);
                String jsonFC = IOUtil.inputStreamToString(file);
                log.info("" + jsonFC);
                PinStateMonitorConfig pinStateMonitorConfig = GSONUtil.fromJSONDefault(jsonFC, PinStateMonitorConfig.class);
                new GPIOFlowProcessor(pinStateMonitorConfig, TaskUtil.defaultTaskScheduler()).init();
            }
            if(cronConfig != null)
            {

                CronConfig cc = GSONUtil.fromJSON(IOUtil.inputStreamToString(cronConfig), CronConfig.class);

                CronTool ct = new CronTool(TaskUtil.defaultTaskScheduler());
                TaskUtil.defaultTaskScheduler().queue(cc.getSetupDelay(), ()->
                {
                    try
                    {
                        SunriseSunsetScheduler ssc = new SunriseSunsetScheduler(TaskUtil.defaultTaskProcessor(), null);
                        ssc.setAPIService(cc.getAPIEndpoint());
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
            if(i2cCommand != null)
            {
                I2CUtil.exec(params);
            }

            if(wsConfig == null && flowConfig == null && cronConfig == null && i2cCommand==null)
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
        System.err.println("command: [-wsc web-server-config.json] [-fc flow-config.json] [-cc cron_config.json] [-i2c [command] [command-params....]");
        I2CUtil.error("-i2c");
        System.exit(-1);
    }
}

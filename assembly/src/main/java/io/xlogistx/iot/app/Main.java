package io.xlogistx.iot.app;

import io.xlogistx.http.HTTPServerCreator;
import io.xlogistx.iot.gpio.GPIOFlowProcessor;
import io.xlogistx.iot.gpio.PinStateMonitorConfig;
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

            if(wsConfig == null && flowConfig == null)
                throw new IllegalArgumentException("No config found");

        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.err.println("command: [-wsc web-server-config.json] [-fc flow-config.json]");
        }
    }
}

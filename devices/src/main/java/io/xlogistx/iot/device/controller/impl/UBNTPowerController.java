package io.xlogistx.iot.device.controller.impl;

import io.xlogistx.common.task.RunnableProperties;
import org.zoxweb.shared.util.GetName;
import org.zoxweb.shared.util.NVGenericMap;

import java.io.IOException;
import java.util.logging.Logger;

public class UBNTPowerController
    extends RunnableProperties
{

    private static final transient Logger log = Logger.getLogger(UBNTPowerController.class.getName());
    public enum Param
        implements GetName
    {
        URL("url"),
        USER("user"),
        PASSWORD("password"),
        PORT("port"),
        STATE("state"),
        ;
        private final String name;
        Param(String name)
        {
            this.name = name;
        }


        @Override
        public String getName() {
            return name;
        }
    }


    public UBNTPowerController()
    {

    }

    public UBNTPowerController(NVGenericMap nvgm)
    {
        setProperties(nvgm);
    }

    @Override
    public void run() {

        try
        {
           exec();
        }
        catch (Exception e)
        {
            try
            {
                exec();
            }
            catch (Exception ee)
            {
                ee.printStackTrace();
                log.info("FAILED: " +Thread.currentThread() +  ", " + getProperties().getValue(Param.URL) + ", " + getProperties().getValue(Param.STATE));
            }
        }

    }

    private void exec() throws IOException
    {
        String url = getProperties().getValue(Param.URL);

        UBNTEqpt.controlPort(
                url,
                getProperties().getValue(Param.USER),
                getProperties().getValue(Param.PASSWORD),
                getProperties().getValue(Param.PORT),
                getProperties().getValue(Param.STATE),
                null);
        log.info(Thread.currentThread() +  ", " + url + ", " + getProperties().getValue(Param.STATE));
    }
}

package io.xlogistx.iot.device.controller.impl;

import io.xlogistx.common.task.RunnableProperties;
import org.zoxweb.shared.util.GetName;
import org.zoxweb.shared.util.NVGenericMap;

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
        String url = getProperties().getValue(Param.URL);
        try {



            log.info(Thread.currentThread() +  ", " + url + ", " + getProperties().getValue(Param.STATE));
            UBNTEqpt.controlPort(url,
                    getProperties().getValue(Param.USER),
                    getProperties().getValue(Param.PASSWORD),
                    getProperties().getValue(Param.PORT),
                    getProperties().getValue(Param.STATE), null);

        } catch (Exception e) {
            e.printStackTrace();
            log.info("Error" + Thread.currentThread() +  " " + url + " : DAY");
        }

    }
}

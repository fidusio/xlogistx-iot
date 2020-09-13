package io.xlogistx.iot.device.controller.impl;

import io.xlogistx.common.task.RunnableProperties;
import org.zoxweb.shared.util.GetName;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.NVGenericMapList;

import java.util.logging.Logger;

public class UBNTRebootController
    extends RunnableProperties
{
    private static final transient Logger log = Logger.getLogger(UBNTPowerController.class.getName());
    public enum Param
            implements GetName
    {
        DEVICES("devices"),
        URL("url"),
        USER("user"),
        PASSWORD("password"),
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


    public UBNTRebootController()
    {

    }

    public UBNTRebootController(NVGenericMap nvgm)
    {
        setProperties(nvgm);
    }

    @Override
    public void run()
    {
        // get the devices
        NVGenericMapList nvgml = (NVGenericMapList)getProperties().get(Param.DEVICES);
        for(NVGenericMap nvgm : nvgml.getValue())
        {
            String url = nvgm.getValue(Param.URL);
            String user = nvgm.getValue(Param.USER);
            String password = nvgm.getValue(Param.PASSWORD);
            try
            {
                UBNTEqpt.reboot(url, user, password, null);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}

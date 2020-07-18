package io.xlogistx.iot.net.data;

import org.zoxweb.shared.data.PropertyDAO;

import org.zoxweb.shared.http.HTTPMessageConfig;
import org.zoxweb.shared.http.HTTPMessageConfigInterface;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.util.*;

import java.util.Date;
import java.util.List;

public class TaskConfig
    extends PropertyDAO
{


    public enum Param
        implements GetNVConfig
    {
        HTTP_CONFIG(NVConfigManager.createNVConfigEntity("http_config", "HTTPConfig", "HTTPConfig", false, true, HTTPMessageConfig.class, NVConfigEntity.ArrayType.LIST)),
        INIT_DELAY(NVConfigManager.createNVConfig("init_delay", "InitialDelay", "InitDelay", false, true, Date.class)),
        RETRIES(NVConfigManager.createNVConfig("retries", "Retries", "Retries", false, true, int.class)),
        RETRY_DELAY(NVConfigManager.createNVConfig("retry_delay", "Retry delay in case of failure", "RetryDelay", false, true, Date.class)),
        REPEATS(NVConfigManager.createNVConfig("repeats", "Repeats", "Repeats", false, true, int.class)),
        REPEAT_DELAY(NVConfigManager.createNVConfig("repeat_delay", "Repeat Delay", "RepeatDelay", false, true, Date.class))
        ;

        private final NVConfig nvc;

        Param(NVConfig nvc)
        {
            this.nvc = nvc;
        }

        public NVConfig getNVConfig()
        {
            return nvc;
        }
    }

    public static final NVConfigEntity NVC_TASK_CONFIG = new NVConfigEntityLocal("TASK_CONFIG",
            null,
            "TaskConfig",
            true,
            false,
            false,
            false,
            TaskConfig.class,
            SharedUtil.extractNVConfigs(Param.values()),
            null,
            false,
            PropertyDAO.NVC_PROPERTY_DAO);
    public TaskConfig()
    {
        super(NVC_TASK_CONFIG);
    }

    public HTTPMessageConfigInterface[] getHTTPConfigs()
    {
        List<HTTPMessageConfigInterface> ret =  lookupValue(Param.HTTP_CONFIG);

        if(ret != null)
        {
            for(HTTPMessageConfigInterface hmci : ret) {
                if (hmci.getMethod() == null) {
                    hmci.setMethod(HTTPMethod.GET);
                }
            }
        }
        return ret.toArray( new HTTPMessageConfigInterface[0]);
    }

    public long getInitDelay()
    {
        return lookupValue(Param.INIT_DELAY);
    }
    public int getRetries()
    {
        return lookupValue(Param.RETRIES);
    }

    public long getRetryDelay()
    {
        return lookupValue(Param.RETRY_DELAY);
    }

    public int getRepeats()
    {
        return lookupValue(Param.REPEATS);
    }

    public long getRepeatDelay()
    {
        return lookupValue(Param.REPEAT_DELAY);
    }


}

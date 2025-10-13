package io.xlogistx.iot.gpio;

import io.xlogistx.common.data.PropertyContainer;
import org.zoxweb.server.http.OkHTTPCall;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.shared.annotation.PostStartup;
import org.zoxweb.shared.http.HTTPMessageConfig;
import org.zoxweb.shared.http.HTTPMessageConfigInterface;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.http.HTTPResponseData;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.NVStringList;

public class GPIOPostStartup
        extends PropertyContainer<NVGenericMap> {
    public static final LogWrapper log = new LogWrapper(GPIOPostStartup.class).setEnabled(true);

    @PostStartup
    public void postStartup() {

        if (log.isEnabled()) log.getLogger().info(" Start ***********");
        boolean async = getProperties().getValue("async", false);
        NVStringList urls = getProperties().getNV("urls");
        if (log.isEnabled()) log.getLogger().info("URLS: " + urls);
        if (urls != null) {
            for (String url : urls.getValues()) {
                if (log.isEnabled()) log.getLogger().info("url: " + url);
                try {
                    HTTPMessageConfigInterface hmci = HTTPMessageConfig.createAndInit(url, null, HTTPMethod.GET, false);
                    HTTPResponseData hrd = OkHTTPCall.send(hmci);
                    if (log.isEnabled()) log.getLogger().info("" + hrd);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void refreshProperties() {
        log.getLogger().info("" + getProperties());

    }
}

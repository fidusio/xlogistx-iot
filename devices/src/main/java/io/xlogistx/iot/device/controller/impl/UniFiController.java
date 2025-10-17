package io.xlogistx.iot.device.controller.impl;

import io.xlogistx.common.task.RunnableProperties;
import org.zoxweb.server.http.HTTPUtil;
import org.zoxweb.server.http.OkHTTPCall;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.http.*;
import org.zoxweb.shared.util.GetNameValue;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.NVGenericMapList;
import org.zoxweb.shared.util.RateCounter;

import java.io.IOException;

public class UniFiController
        extends RunnableProperties {
    public static final LogWrapper log = new LogWrapper(UniFiController.class);

    private GetNameValue<String> securityCookie;
    private NVGenericMap sites = null;
    public final RateCounter success = new RateCounter("Success");

    public final RateCounter failed = new RateCounter("Failed");

    public UniFiController() {
    }


    public void run() {
        failed.reset();
        success.reset();
        try {
            if (getProperties().getValue("mac") != null) {
                log.getLogger().info("Restart device: " + getProperties().getValue("site") + "-" + getProperties().getValue("mac"));
                restart(getProperties().getValue("site"), getProperties().getValue("mac"), "hard");
            } else if (getProperties().getValue("site") != null) {
                log.getLogger().info("Restart site: " + getProperties().getValue("site"));
                restart(getProperties().getValue("site"));
            } else {
                log.getLogger().info("Restart all sites");
                restartAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("usage UniFiController url username password [site] [device mac address]");
        }

        System.out.println(success);
        System.out.println(failed);
    }

    public NVGenericMap getSiteDevices(String site) throws IOException {
        NVGenericMap localSite = (NVGenericMap) getAllSites().get(site);
        String uri = "/api/s/" + localSite.getValue("name") + "/stat/device-basic";
        HTTPMessageConfigInterface hmci = HTTPMessageConfig.createAndInit(getProperties().getValue("url"), uri, "get", false);
        hmci.getHeaders().add(getSecurityCookie());
        //HTTPResponseData hrd = new HTTPCall(hmci).sendRequest();
        HTTPAPIResult<NVGenericMap> hro = OkHTTPCall.send(hmci, NVGenericMap.class);

        return hro.getData();
    }

    public void restart(String site, String mac, String rebootType) throws IOException {
        long delta = System.currentTimeMillis();
        try {
            internalRestart(site, mac, rebootType);
            success.register(System.currentTimeMillis() - delta);
        } catch (IOException e) {
            failed.register(System.currentTimeMillis() - delta);
            throw e;
        }
    }

    private void internalRestart(String site, String mac, String rebootType) throws IOException {
        log.getLogger().info("Site: " + site);
        NVGenericMap localSite = (NVGenericMap) getAllSites().get(site);
        NVGenericMap nvgm = new NVGenericMap();
        nvgm.add("cmd", "restart");
        nvgm.add("reboot_type", rebootType);
        nvgm.add("mac", mac.toLowerCase());
        String uri = "/api/s/" + localSite.getValue("name") + "/cmd/devmgr";
        HTTPMessageConfigInterface hmci = HTTPMessageConfig.createAndInit(getProperties().getValue("url"), uri, "post", false);
        hmci.setContentType("application/json;charset=UTF-8");
        hmci.getHeaders().add(getSecurityCookie());
        hmci.setContent(GSONUtil.toJSONGenericMap(nvgm, false, false, false));
        OkHTTPCall.send(hmci);
    }

    public void restartAll() throws IOException {
        NVGenericMap localSites = getAllSites();
        for (GetNameValue<?> nvgm : localSites.values()) {
            try {

                restart(nvgm.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void restart(String site) throws IOException {
        NVGenericMap devices = getSiteDevices(site);
        for (GetNameValue<?> gnv : devices.values()) {
            if (gnv instanceof NVGenericMapList) {
                for (NVGenericMap device : ((NVGenericMapList) gnv).getValue()) {
                    try {
                        restart(site, device.getValue("mac"), "hard");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    public NVGenericMap getAllSites() throws IOException {
        if (sites == null) {
            synchronized (this) {
                if (sites == null) {
                    HTTPMessageConfigInterface hmci = HTTPMessageConfig.createAndInit(getProperties().getValue("url"), "api/self/sites", "get");
                    hmci.setSecureCheckEnabled(false);
                    hmci.setContentType("application/json;charset=UTF-8");
                    hmci.getHeaders().add(getSecurityCookie());
                    HTTPAPIResult<NVGenericMap> hro = OkHTTPCall.send(hmci, NVGenericMap.class);
                    if (hro.getStatus() != HTTPStatusCode.OK.CODE) {
                        throw new IOException("" + hro);
                    }

                    setSites(hro.getData());
                }
            }
        }
        return sites;
    }

    private synchronized void setSites(NVGenericMap nvgm) {
        sites = new NVGenericMap();
        for (GetNameValue<?> gnv : nvgm.values()) {
            if (gnv instanceof NVGenericMapList) {
                //log.info("Site List: " + gnv);
                for (NVGenericMap site : ((NVGenericMapList) gnv).getValue()) {
                    String desc = site.getValue("desc");
                    site.setName(desc);
                    sites.add(site);
                }
            }
        }
    }


    public GetNameValue<String> login()
            throws IOException {
        NVGenericMap nvgm = new NVGenericMap();
        nvgm.add(getProperties().get("username"));
        nvgm.add(getProperties().get("password"));
        final String uri = "/api/login";
        HTTPMessageConfigInterface hmci = HTTPMessageConfig.createAndInit(getProperties().getValue("url"), uri, "post");
        hmci.setSecureCheckEnabled(false);
        hmci.setContent(GSONUtil.toJSONGenericMap(nvgm, false, false, false));
        hmci.setContentType("application/json;charset=UTF-8");
        //HTTPCall hc = new HTTPCall(hmci);
        HTTPResponseData hrd = OkHTTPCall.send(hmci);
        if (hrd.getStatus() != HTTPStatusCode.OK.CODE) {
            throw new IOException("" + hrd);
        }
        return HTTPUtil.extractHeaderCookie(hrd);
    }

    public GetNameValue<String> getSecurityCookie() throws IOException {
        if (securityCookie == null) {
            synchronized (this) {
                if (securityCookie == null) {
                    securityCookie = login();
                }
            }
        }
        return securityCookie;
    }

    public static void main(String... args) {
        try {
            UniFiController unifi = new UniFiController();
            int index = 0;
            String host = args[index++];
            String user = args[index++];
            String password = args[index++];

            unifi.getProperties().add("url", host);
            unifi.getProperties().add("username", user);
            unifi.getProperties().add("password", password);
            String site = args.length > index ? args[index++] : null;
            String macAddress = args.length > index ? args[index++] : null;

            unifi.getProperties().add("site", site);
            unifi.getProperties().add("mac", macAddress);

            unifi.run();
        } catch (Exception e) {
            System.err.println("usage UniFiController url username password [site] [device mac address]");
            e.printStackTrace();
        }
    }
}

package io.xlogistx.iot.mqtt;

import org.apache.activemq.artemis.core.security.CheckType;
import org.apache.activemq.artemis.core.security.Role;
import org.apache.activemq.artemis.spi.core.protocol.RemotingConnection;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager;
import org.apache.activemq.artemis.spi.core.security.ActiveMQSecurityManager5;
import org.zoxweb.server.http.HTTPCall;
import org.zoxweb.server.security.JSPrincipal;
import org.zoxweb.shared.http.HTTPMessageConfig;
import org.zoxweb.shared.http.HTTPMessageConfigInterface;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.http.HTTPMediaType;
import org.zoxweb.shared.util.SharedUtil;

import javax.security.auth.Subject;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ArtemisSM
    implements ActiveMQSecurityManager5
{

    private String loginURL;
    private static final Logger log = Logger.getLogger(ArtemisSM.class.getName());
    @Override
    public Subject authenticate(String user, String password, RemotingConnection remotingConnection, String securityDomain)
    {
        Subject ret = null;
        try
        {
            log.info(Thread.currentThread() + ":" + SharedUtil.toCanonicalID(':', user,remotingConnection));
            log.info("RemoteConnection id: " + remotingConnection.getID());
            HTTPMessageConfigInterface hmci = HTTPMessageConfig.createAndInit(loginURL, null, HTTPMethod.GET);
            hmci.setUser(user);
            hmci.setPassword(password);
            hmci.setContentType(HTTPMediaType.APPLICATION_JSON);
            HTTPCall.send(hmci);
            ret = new Subject();
            ret.getPrincipals().add(new JSPrincipal(user));

            remotingConnection.setSubject(ret);;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;

        //true, new JSPrincipal(user).asSet(), null, null);
    }

    @Override
    public boolean authorize(Subject subject, Set<Role> roles, CheckType checkType, String address) {

        return true;
    }

    @Override
    public String getDomain() {
        return ActiveMQSecurityManager5.super.getDomain();
    }

    @Override
    public boolean validateUser(String user, String password) {
        log.info(user + " " + password);
        return true;
    }

    @Override
    public boolean validateUserAndRole(String s, String s1, Set<Role> set, CheckType checkType) {
        return true;
    }

    @Override
    public ActiveMQSecurityManager init(Map<String, String> properties) {
        log.info(""+properties);
        if(properties != null)
        {
            loginURL = properties.get("url");
            log.info("login url: " + loginURL);
        }
        return this;
        //return ActiveMQSecurityManager5.super.init(properties);
    }
}

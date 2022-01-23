package io.xlogistx.iot.device.controller.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;


import org.zoxweb.server.security.SSLCheckDisabler;
import org.zoxweb.server.http.HTTPCall;
import org.zoxweb.server.http.HTTPUtil;
import org.zoxweb.shared.http.HTTPCallException;
import org.zoxweb.shared.http.HTTPMessageConfig;
import org.zoxweb.shared.http.HTTPMessageConfigInterface;
import org.zoxweb.shared.http.HTTPMethod;
import org.zoxweb.shared.http.HTTPMimeType;
import org.zoxweb.shared.http.HTTPResponseData;
import org.zoxweb.shared.http.HTTPStatusCode;
import org.zoxweb.shared.net.InetSocketAddressDAO;
import org.zoxweb.shared.util.NVPair;
import org.zoxweb.shared.util.SetNameValue;

/**
 * This class communicates with Ubiqutiti equipments
 */
public class UBNTEqpt
{
  private static final Logger log = Logger.getLogger(UBNTEqpt.class.getName());

  public static NVPair loginCookie(String url, String user, String passwd, InetSocketAddressDAO proxy) throws IOException
  {
    HTTPCall hc;
    String uri = "login.cgi";
    // get the login form
    HTTPMessageConfigInterface hcc = new HTTPMessageConfig();
    hcc.setURL(url);
    hcc.setURI(uri);
    hcc.setMethod(HTTPMethod.POST);
    hcc.setProxyAddress(proxy);
    hcc.setRedirectEnabled(false);
    hcc.getParameters().add(new NVPair("uri", "/"));


    hc = new HTTPCall(hcc, SSLCheckDisabler.SINGLETON);
    HTTPResponseData rd = hc.sendRequest();



    List<HTTPMessageConfigInterface> forms = HTTPUtil.extractFormsContent(rd, 0);
    // perform login to get the session token
    hcc = forms.get(0);

    hcc.setURL(url);
    hcc.setURI(uri);
    hcc.setProxyAddress(proxy);
    hcc.setRedirectEnabled(false);

    NVPair cookie = (NVPair) hcc.getHeaders().get("Cookie");
    ((SetNameValue<String>) hcc.getParameters().get("username")).setValue(user);
    ((SetNameValue<String>)hcc.getParameters().get("password")).setValue( passwd);

    hc = new HTTPCall(hcc, SSLCheckDisabler.SINGLETON);
    rd = hc.sendRequest();
    if (rd.getStatus() != HTTPStatusCode.FOUND.CODE)
    {
      throw new HTTPCallException("login failed",rd);
    }
    return cookie;
  }

  public static HTTPResponseData controlPort(String url, String user, String passwd, int port, boolean on, InetSocketAddressDAO proxy) throws IOException
  {
    NVPair cookie = loginCookie(url, user, passwd, proxy);
    return controlPort(url, cookie, port, on, proxy);
  }


  public static HTTPResponseData controlPort(String url, NVPair sessionCookie, int port, boolean on, InetSocketAddressDAO proxy) throws IOException
  {

    // create the control message
    HTTPMessageConfigInterface setSensor = HTTPMessageConfig.createAndInit(url, "sensors/"+port, HTTPMethod.PUT, false);
    setSensor.setProxyAddress(proxy);
    setSensor.getHeaders().add(sessionCookie);
    setSensor.getHeaders().add( new NVPair("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"));
    //setSensor.getHeaderParameters().add( new NVPair("X-Requested-With", "XMLHttpRequest"));
    //setSensor.getHeaderParameters().add( new NVPair("Referer", "http://10.0.1.51/power"));
    setSensor.getParameters().add( new NVPair("output", (on ?  "1" : "0" )));
    //System.out.println( ""+setSensor);
    setSensor.setRedirectEnabled(false);
    HTTPCall hc = new HTTPCall(setSensor);
    return hc.sendRequest();
  }

  public static HTTPResponseData getSensorsStatus(String url, NVPair sessionCookie, InetSocketAddressDAO proxy) throws IOException
  {

    // create the control message
    HTTPMessageConfigInterface getSensorsStatus = HTTPMessageConfig.createAndInit(url, "mfi/sensors.cgi", HTTPMethod.GET);
    getSensorsStatus.setProxyAddress(proxy);
    getSensorsStatus.getHeaders().add( sessionCookie);
    //getSensorsStatus.getHeaderParameters().add( new NVPair("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"));
    getSensorsStatus.getHeaders().add( new NVPair("X-Requested-With", "XMLHttpRequest"));
    getSensorsStatus.getHeaders().add( new NVPair("Accept", "*/*"));
    //setSensor.getHeaderParameters().add( new NVPair("Referer", "http://10.0.1.51/power"));
    getSensorsStatus.getParameters().add( new NVPair("t", "0."+System.currentTimeMillis() ));
    System.out.println( ""+getSensorsStatus);
    HTTPCall hc = new HTTPCall(getSensorsStatus, SSLCheckDisabler.SINGLETON);
    return  hc.sendRequest();
  }


  public static HTTPResponseData reboot(String url, NVPair sessionCookie) throws IOException
  {
    HTTPMessageConfigInterface reboot = HTTPMessageConfig.createAndInit(url, "reboot.cgi", HTTPMethod.POST);
    reboot.getHeaders().add(sessionCookie);
    reboot.setContentType(HTTPMimeType.MULTIPART_FORM_DATA);
    //System.out.println( ""+reboot);
    HTTPCall hc = new HTTPCall(reboot, SSLCheckDisabler.SINGLETON);
    return hc.sendRequest();
  }


  public static HTTPResponseData reboot(String url, String user, String passwd, InetSocketAddressDAO proxy) throws IOException
  {
    NVPair cookie = loginCookie(url, user, passwd, proxy);
    HTTPMessageConfigInterface reboot = HTTPMessageConfig.createAndInit(url, "reboot.cgi", HTTPMethod.POST);
    reboot.getHeaders().add(cookie);
    reboot.setContentType(HTTPMimeType.MULTIPART_FORM_DATA);
    //reboot.setMultiPartEncoding(true);
    reboot.setProxyAddress(proxy);
    reboot.setRedirectEnabled(true);
    //System.out.println( "REBOOT:"+reboot);
    HTTPCall hc = new HTTPCall(reboot, SSLCheckDisabler.SINGLETON);
    HTTPResponseData rd =  hc.sendRequest();

    return rd;
  }



  public static void main(String ...args)
  {
    try
    {
      int index = 0;
      String command = args[index++].toLowerCase();

      String[] urls = args[index++].split(Pattern.quote(","));
      String user = args[index++];
      String password = args[index++];
      InetSocketAddressDAO proxy = null;

      if (args.length > index)
      {
        proxy = new InetSocketAddressDAO(args[index++]);
      }

      for (String url: urls) {
        try {

          switch (command) {
            case "-r":
              HTTPResponseData rd = reboot(url, user, password, proxy);
              //System.out.println("Rd:" + rd);
              System.out.println(new Date() + " reboot result for " + url + " " + HTTPStatusCode
                  .statusByCode(rd.getStatus()));
              //System.out.println("Rd:" + rd);
              break;
            case "-l":
              NVPair cookie = loginCookie(url, user, password, proxy);
              //System.out.println("Rd:" + rd);
              System.out.println(new Date() + " login session cookie " + url + " " + cookie);
              //System.out.println("Rd:" + rd);
              break;
            default:
              throw new IllegalArgumentException(command + " invalid command.");
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }





    }
    catch(Exception e)
    {
      e.printStackTrace();
      System.err.println();
      System.err.println("Usage  : [-r or -l] URL User Password [proxy address:port]");
      System.err.println("Command: -r reboot");
      System.err.println("Command: -l login and return the session cookie");
    }

  }

}

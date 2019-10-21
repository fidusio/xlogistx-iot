package io.xlogistx.iot.device.controller.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
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

public class UBNTEqpt
{

  public static NVPair loginCookie(String url, String user, String passwd, InetSocketAddressDAO proxy) throws IOException
  {
    HTTPCall hc = null;
    String uri = "login.cgi";
    // get the login form
    HTTPMessageConfigInterface hcc = new HTTPMessageConfig();
    hcc.setURL(url);
    hcc.setURI(uri);
    hcc.setMethod( HTTPMethod.POST);
    hcc.setProxyAddress(proxy);
    hcc.setRedirectEnabled(false);
    //hcc.setMultiPartEncoding(true);
    hcc.getParameters().add(new NVPair("uri", "/"));


    hc = new HTTPCall(hcc, SSLCheckDisabler.SINGLETON);
    //System.out.println( ""+ hcc);
    HTTPResponseData rd = hc.sendRequest();
    //System.out.println( ""+ rd);


    // parse the form content
    List<HTTPMessageConfigInterface> forms = HTTPUtil.extractFormsContent(rd, 0);
    //System.out.println( ""+ forms);
    // perform login to get the session token
    hcc = forms.get(0);

    hcc.setURL(url);
    hcc.setURI(uri);
    hcc.setProxyAddress(proxy);
    hcc.setRedirectEnabled(false);

    //System.out.println( ""+ forms);
    NVPair cookie = (NVPair) hcc.getHeaderParameters().get("Cookie");
    SetNameValue<String> username = (SetNameValue<String>) hcc.getParameters().get("username");
//		if (username == null)
//		{
//			hcc.getParameters().add(new NVPair("username", user));
//		}
//		else
    {
      username.setValue(user);
    }


    SetNameValue<String> password = (SetNameValue<String>) hcc.getParameters().get("password");
//		if (password == null)
//		{
//			hcc.getParameters().add(new NVPair("password", passwd));
//		}
//		else
    {
      password.setValue( passwd);
    }



    //hcc.getParameters().add(new NVPair("uri", " /"));
    //NVPair cookie = HTTPUtil.extractRequestCookie(rd);



    // make the login call
    //System.out.println(hcc);
    hc = new HTTPCall(hcc, SSLCheckDisabler.SINGLETON);
    rd = hc.sendRequest();
    if (rd.getStatus() != HTTPStatusCode.FOUND.CODE)
    {
      throw new HTTPCallException("login failed",rd);
    }



    return cookie;
  }

  public static String controlPort(String url, String user, String passwd, int port, boolean on, InetSocketAddressDAO proxy) throws IOException
  {
    NVPair cookie = loginCookie(url, user, passwd, proxy);
    return controlPort(url, cookie, port, on, proxy);
  }


  public static String controlPort(String url, NVPair sessionCookie, int port, boolean on, InetSocketAddressDAO proxy) throws IOException
  {

    // create the control message
    HTTPMessageConfigInterface setSensor = HTTPMessageConfig.createAndInit(url, "sensors/"+port, HTTPMethod.PUT, false);
    setSensor.setProxyAddress(proxy);
    setSensor.getHeaderParameters().add(sessionCookie);
    setSensor.getHeaderParameters().add( new NVPair("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"));
    //setSensor.getHeaderParameters().add( new NVPair("X-Requested-With", "XMLHttpRequest"));
    //setSensor.getHeaderParameters().add( new NVPair("Referer", "http://10.0.1.51/power"));
    setSensor.getParameters().add( new NVPair("output", (on ?  "1" : "0" )));
    //System.out.println( ""+setSensor);
    setSensor.setRedirectEnabled(false);
    HTTPCall hc = new HTTPCall(setSensor);

    HTTPResponseData rd =  hc.sendRequest();

    return new String(rd.getData());
  }

  public static String getSensorsStatus(String url, NVPair sessionCookie, InetSocketAddressDAO proxy) throws IOException
  {

    // create the control message
    HTTPMessageConfigInterface getSensorsStatus = HTTPMessageConfig.createAndInit(url, "mfi/sensors.cgi", HTTPMethod.GET);
    getSensorsStatus.setProxyAddress(proxy);
    getSensorsStatus.getHeaderParameters().add( sessionCookie);
    //getSensorsStatus.getHeaderParameters().add( new NVPair("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"));
    getSensorsStatus.getHeaderParameters().add( new NVPair("X-Requested-With", "XMLHttpRequest"));
    getSensorsStatus.getHeaderParameters().add( new NVPair("Accept", "*/*"));
    //setSensor.getHeaderParameters().add( new NVPair("Referer", "http://10.0.1.51/power"));
    getSensorsStatus.getParameters().add( new NVPair("t", "0."+System.currentTimeMillis() ));
    System.out.println( ""+getSensorsStatus);
    HTTPCall hc = new HTTPCall(getSensorsStatus, SSLCheckDisabler.SINGLETON);
    HTTPResponseData rd =  hc.sendRequest();

    return new String( rd.getData());

  }


  public static String reboot(String url, NVPair sessionCookie) throws IOException
  {
    HTTPMessageConfigInterface reboot = HTTPMessageConfig.createAndInit(url, "reboot.cgi", HTTPMethod.POST);
    reboot.getHeaderParameters().add(sessionCookie);
    reboot.setContentType(HTTPMimeType.MULTIPART_FORM_DATA);
    //System.out.println( ""+reboot);
    HTTPCall hc = new HTTPCall(reboot, SSLCheckDisabler.SINGLETON);
    HTTPResponseData rd =  hc.sendRequest();

    return new String( rd.getData());
  }


  public static HTTPResponseData reboot(String url, String user, String passwd,  InetSocketAddressDAO proxy) throws IOException
  {
    NVPair cookie = loginCookie(url, user, passwd, proxy);
    HTTPMessageConfigInterface reboot = HTTPMessageConfig.createAndInit(url, "reboot.cgi", HTTPMethod.POST);
    reboot.getHeaderParameters().add(cookie);
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

      String urls[] = args[index++].split(Pattern.quote(","));
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
      System.err.println("Usage  : -rl URL User Password [proxy address:port]");
      System.err.println("Command: -r reboot");
      System.err.println("Command: -l login and return the session cookie");
    }

  }

}

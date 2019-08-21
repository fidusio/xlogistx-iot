package io.xlogistx.iot.device.impl;

import java.util.regex.Pattern;
import org.zoxweb.shared.net.InetSocketAddressDAO;
import org.zoxweb.shared.util.Const.Bool;
import org.zoxweb.shared.util.NVPair;

public class MPowerController
{
  public static void main(String ...args)
  {
    try
    {
      int i = 0;
      String urls[] = args[i++].split(Pattern.quote(","));
      String user = args[i++];
      String passwd = args[i++];
      int port = Integer.parseInt(args[i++]);
      boolean on = Bool.lookupValue(args[i++]);//Boolean.parseBoolean(args[i++]);
      InetSocketAddressDAO proxy = null;
      if (args.length > i)
      {
        proxy = new InetSocketAddressDAO(args[i++]);
        proxy.setProxyType("http");
        System.out.println(proxy);
      }






      //System.out.println(""+ UBNTEqpt.reboot(url, user, passwd, null).getStatus());
      //System.exit(0);


      for (String url : urls) {
        try {
          long ts = System.currentTimeMillis();
          NVPair sessionCookie = UBNTEqpt.loginCookie(url, user, passwd, proxy);

          //System.out.println(UBNTMPower.reboot(url, sessionCookie));

          //System.exit(-1);

          String response = null;

          response = UBNTEqpt.controlPort(url, sessionCookie, port, on, proxy);
          System.out.println(response);
          response = UBNTEqpt.getSensorsStatus(url, sessionCookie, proxy);
          System.out.println(response);
          ts = System.currentTimeMillis() - ts;
          System.out.println(response + "\nin " + ts + " millis.");
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      //System.out.println( System.currentTimeMillis() + " millis.");
      //System.out.println( UBNTMPower.getSensorsStatus(url, cookie));



    }
    catch( Exception e)
    {
      e.printStackTrace();
    }
  }
}

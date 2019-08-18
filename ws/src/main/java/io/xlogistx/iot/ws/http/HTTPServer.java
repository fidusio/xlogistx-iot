package io.xlogistx.iot.ws.http;

import com.sun.net.httpserver.*;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.zoxweb.server.security.CryptoUtil;
import org.zoxweb.server.task.TaskUtil;

import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.http.HTTPHeaderName;
import org.zoxweb.shared.http.HTTPMimeType;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.SharedStringUtil;


import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.logging.Logger;


public class HTTPServer {


  private final static Logger log = Logger.getLogger(HTTPServer.class.getName());

  static class  ContextHandler implements HttpHandler
  {
    public void handle(HttpExchange he) throws IOException {
      he.getRequestMethod();
      InputStream is = he.getRequestBody();
      is.close();


      NVGenericMap nvgm = new NVGenericMap();
      nvgm.add("url", he.getHttpContext().getPath());
      nvgm.add("data", "" + new Date());

      String json  = GSONUtil.DEFAULT_GSON.toJson(nvgm);
      byte[] response = SharedStringUtil.getBytes(json);
      he.getResponseHeaders().add(HTTPHeaderName.CONTENT_TYPE.getName(), HTTPMimeType.APPLICATION_JSON.getValue());
      he.getResponseHeaders().add(HTTPHeaderName.CONTENT_TYPE.getName(), "charset=utf-8");
      he.sendResponseHeaders(200, response.length);
      OutputStream os = he.getResponseBody();
      os.write(response);
      os.close();
      //log.info(""+Thread.currentThread());
    }
  }

  public static void main(String ...args)
  {
    try
    {
      TaskUtil.setThreadMultiplier(8);
      System.setProperty("java.util.logging.SimpleFormatter.format","%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$-6s : %2$s %5$s%6$s%n");
      String keyfile = System.getenv("KEYFILE");
      String keyfilePassword = System.getenv("KEYFILE_PASSWORD");
      int index = 0;
      if (keyfile == null)
      {
        keyfile = args[index++];
      }

      if (keyfilePassword == null)
      {
        keyfilePassword = args[index++];
      }


      SSLContext sslContext = CryptoUtil.initSSLContext(keyfile, CryptoUtil.PKCS12,  keyfilePassword.toCharArray(), null, null, null);

      int port = Integer.parseInt(args[index++]);
      HttpsServer server = HttpsServer.create(new InetSocketAddress(port), 250);
      for (; index < args.length; index++) {
        server.createContext("/"+args[index], new ContextHandler());
      }
      server.setExecutor(TaskUtil.getDefaultTaskProcessor());
      //server.setExecutor(Executors.newFixedThreadPool(96));
      server.setHttpsConfigurator (new HttpsConfigurator(sslContext));
      /*server.setHttpsConfigurator (new HttpsConfigurator(sslContext) {
        SSLParameters sslParameters;
        public void configure (HttpsParameters params) {
          if (sslParameters == null)
          {
            sslParameters = getSSLContext().getDefaultSSLParameters();
          }
          // get the remote address if needed
          //InetSocketAddress remote = params.getClientAddress();

          //SSLParameters sslParameters = getSSLContext().getDefaultSSLParameters();
          try {
//            sslParameters.setCipherSuites(new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
//                "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
//                "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
//                "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_SHA256",
//                "TLS_ECDHE_ECDSA_WITH_AES_128_SHA256", "TLS_ECDHE_RSA_WITH_AES_128_SHA",
//                "TLS_ECDHE_ECDSA_WITH_AES_128_SHA",
//                "TLS_ECDHE_RSA_WITH_AES_256_SHA384", "TLS_ECDHE_ECDSA_WITH_AES_256_SHA384",
//                "TLS_ECDHE_RSA_WITH_AES_256_SHA", "TLS_ECDHE_ECDSA_WITH_AES_256_SHA",
//                "TLS_DHE_RSA_WITH_AES_128_SHA256", "TLS_DHE_RSA_WITH_AES_128_SHA",
//                "TLS_DHE_DSS_WITH_AES_128_SHA256", "TLS_DHE_RSA_WITH_AES_256_SHA256",
//                "TLS_DHE_DSS_WITH_AES_256_SHA", "TLS_DHE_RSA_WITH_AES_256_SHA"});
            params.setSSLParameters(sslParameters);
//            log.info(Arrays.toString(sslParameters.getCipherSuites()));
//            log.info(""+Thread.currentThread());
            // statement above could throw IAE if any params invalid.
            // eg. if app has a UI and parameters supplied by a user.
          }
          catch(Exception e)
          {
            e.printStackTrace();
          }
        }
      });
      */



      server.start();

      log.info("server started @ " + server.getAddress() + " executor thread count:" + TaskUtil.getDefaultTaskProcessor().availableExecutorThreads());

    }
    catch(Exception e)
    {
      e.printStackTrace();

      System.err.println("usage: <http port> uri-1 uri-2 ...");
    }
  }
}
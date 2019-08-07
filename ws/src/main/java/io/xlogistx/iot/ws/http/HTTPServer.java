package io.xlogistx.iot.ws.http;

import com.sun.net.httpserver.*;
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


  private final static Logger LOG = Logger.getLogger(HTTPServer.class.getName());

  static class  ContextHandler implements HttpHandler
  {
    public void handle(HttpExchange he) throws IOException {

      InputStream is = he.getRequestBody();
      is.close();


      NVGenericMap nvgm = new NVGenericMap();
      nvgm.add("url", he.getHttpContext().getPath());
      nvgm.add("data", "123456");

      String json  = GSONUtil.DEFAULT_GSON.toJson(nvgm);
      byte[] response = SharedStringUtil.getBytes(json);
      he.getResponseHeaders().add(HTTPHeaderName.CONTENT_TYPE.getName(), HTTPMimeType.APPLICATION_JSON.getValue());
      he.getResponseHeaders().add(HTTPHeaderName.CONTENT_TYPE.getName(), "charset=utf-8");
      he.sendResponseHeaders(200, response.length);
      OutputStream os = he.getResponseBody();
      os.write(response);
      os.close();
    }
  }

  public static void main(String ...args)
  {
    try
    {

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
      server.setHttpsConfigurator (new HttpsConfigurator(sslContext) {
        public void configure (HttpsParameters params) {

          // get the remote address if needed
          //InetSocketAddress remote = params.getClientAddress();


          params.setSSLParameters(getSSLContext().getDefaultSSLParameters());
          // statement above could throw IAE if any params invalid.
          // eg. if app has a UI and parameters supplied by a user.

        }
      });

      server.start();

      System.out.println("server started @ " + server.getAddress());

    }
    catch(Exception e)
    {
      e.printStackTrace();

      System.err.println("usage: <http port> uri-1 uri-2 ...");
    }
  }
}
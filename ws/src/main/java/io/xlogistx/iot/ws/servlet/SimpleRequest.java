package io.xlogistx.iot.ws.servlet;

import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.zoxweb.server.http.HTTPRequestAttributes;
import org.zoxweb.server.http.servlet.HTTPServletUtil;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.http.HTTPStatusCode;
import org.zoxweb.shared.util.NVGenericMap;


@WebServlet(name = "all-servlet",
    urlPatterns = "/*")
public class SimpleRequest
  extends HttpServlet
{
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException
  {
    HTTPRequestAttributes hra = HTTPServletUtil.extractRequestAttributes(req);
    NVGenericMap nvgm = new NVGenericMap();
    nvgm.add("url", hra.getPathInfo());
    nvgm.add("data", "" + new Date());

    String json  = GSONUtil.DEFAULT_GSON.toJson(nvgm);
    HTTPServletUtil.sendJSONObj(req, resp, HTTPStatusCode.OK, nvgm);

  }
}

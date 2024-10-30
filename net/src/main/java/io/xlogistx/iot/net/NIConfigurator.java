package io.xlogistx.iot.net;


import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.server.util.RuntimeUtil;
import org.zoxweb.shared.data.RuntimeResultDAO;
import org.zoxweb.shared.net.InetProp.InetProto;
import org.zoxweb.shared.net.NIConfigDAO;
import org.zoxweb.shared.net.NIConfigDAO.Param;
import org.zoxweb.shared.util.*;

import java.io.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class NIConfigurator 
{
  
  private NIConfigurator()
  {
    
  }
  
  
//  private static NVConfig NI_PARAMS[] = 
//    {   NIConfigDAO.Param.ADDRESS.getNVConfig(),
//        NIConfigDAO.Param.GATEWAY.getNVConfig(), 
//        NIConfigDAO.Param.NETMASK.getNVConfig(),
//        NIConfigDAO.Param.NETWORK.getNVConfig(),
//        NIConfigDAO.Param.DNS_SERVERS.getNVConfig()
//    }; 
  
  
  
  public static String embed(String str, NVConfig nvc, NVBase<?> nvb)
  {
    
    //if(nvb != null)
    {
      if (nvb != null && nvb.getValue() != null)
      {
        String formattedToken = SharedStringUtil.format(nvb, " ", false);
        str = SharedStringUtil.embedText(str, SharedStringUtil.tag(nvb.getName()), formattedToken);
      }
      else
      {
        str = SharedStringUtil.embedText(str, SharedStringUtil.tag(nvc.getName()), "");
      }
    }
    return str;
  }
  
  public static String generateNIConfig(NIConfigDAO nicd, String template)
  {
    String ret = template;
   

    // replace the ni_name
    ret = SharedStringUtil.embedText(ret, SharedStringUtil.tag(Param.NI_NAME.getNVConfig().getName()), nicd.getNIName());
    // replace the inet protocol
    ret = SharedStringUtil.embedText(ret, SharedStringUtil.tag(Param.INET_PROTO.getNVConfig().getName()), 
                                     nicd.getInetProtocol().getName());
    
    // address
    
    ret = embed(ret,  Param.ADDRESS.getNVConfig(), (NVBase<?>)nicd.getProperties().get(Param.ADDRESS));
   
    // netmask
    ret = embed(ret, Param.NETMASK.getNVConfig(), (NVBase<?>)nicd.getProperties().get(Param.NETMASK));
    
    // GATEWAY
    ret = embed(ret, Param.GATEWAY.getNVConfig(), (NVBase<?>)nicd.getProperties().get(Param.GATEWAY));
   
    // DNS-SERVERS
    ret = embed(ret, Param.DNS_SERVERS.getNVConfig(), (NVBase<?>)nicd.getProperties().get(Param.DNS_SERVERS));
    
    String lines[] = ret.split("\n");
    StringBuilder sb = new StringBuilder();
    for (String line: lines)
    {
      if (SUS.isNotEmpty(line))
      {
        if(sb.length() > 0)
          sb.append('\n');
        sb.append(line);
      }
    }
    
    sb.insert(0, "# Autogenerated file on " + new Date() + "\n");
    return sb.toString();
  }
  
  
  public static NIConfigDAO[] readConfig(String config) throws IOException
  {
    return readConfig(new StringReader(config));
  }
  
  public static NIConfigDAO[] readConfig(Reader reader) throws IOException
  {
    Map<String, NIConfigDAO> ret = new LinkedHashMap<String, NIConfigDAO>();
    try
    {
      BufferedReader br = new BufferedReader(reader);
      
      String line;
      NIConfigDAO match = null;
      while((line = br.readLine()) != null)
      {
        line = line.trim();
        if(!line.isEmpty() && !SharedStringUtil.isComment(line))
        {
          NIConfigDAO temp = niMarker(line);
          if (temp != null)
          {
            ret.put(temp.getNIName(), temp);
            match = temp;
          }
          else if(match != null)
          {
            // get the match parameters
            
//            for(NVConfig nvc : NI_PARAMS)
//            {
//              if (line.startsWith(nvc.getName()))
//              {
//                
//                match.setValue(nvc, parseValue(nvc, line));
//                break;
//              }         
//            }
            
            GetNameValueComment<String> nvpc = SharedUtil.parseGetNameStringComment(line, " ", "#", "//");
            if (nvpc != null)
            {
              if (match.lookup(nvpc.getGNV().getName()) != null)
                match.setValue(nvpc.getGNV().getName(), nvpc.getGNV().getValue());
              else
                match.getProperties().add(nvpc.getGNV().getName(), nvpc.getGNV().getValue());
            }
            
          }
        } 
      }
    }
    finally
    {
      IOUtil.close(reader);
    }
    return ret.values().toArray(new NIConfigDAO[ret.size()]); 
  }
  
  
  private static NIConfigDAO niMarker(String line)
  {
    if (line.startsWith("iface"))
    {
      String niInfos[] = line.split(" ");
      int index = 1;
      String niName = niInfos.length > index ? niInfos[index++] : null; 
      String inet = niInfos.length > index ? (niInfos[index++].equals("inet") ? "inet" : null) : null;
      String proto = niInfos.length > index ? niInfos[index++] : null;
      if (niName != null && inet != null && proto != null)
      {
        InetProto ip = SharedUtil.lookupEnum(proto, InetProto.values());
        if (ip != null)
        {
          NIConfigDAO ret = new NIConfigDAO();
          ret.setNIName(niName);
          ret.setInteProtocol(ip);
          return ret;
        }
      }
    }
    
    return null;
  }
  
  
  public static void writeNIFile(File destination, NIConfigDAO nicd, String template) throws IOException
  {
    SharedUtil.checkIfNulls("Destinatio or config null", destination, nicd, template);
    if(destination.exists() && destination.isDirectory())
    {
      throw new IllegalArgumentException("Desctination is a directory");
    }
    
    String configFile = generateNIConfig(nicd, template);
    
    
    FileOutputStream fis = null;
    
    try
    {
      fis = new FileOutputStream(destination);
      fis.write(SharedStringUtil.getBytes(configFile));
    }
    finally
    {
      IOUtil.close(fis);
    }
  }
  
  public static int upNI(String upCommand, String ni) throws InterruptedException, IOException
  {
    RuntimeResultDAO rrd = RuntimeUtil.runAndFinish(upCommand, ni); 
    return rrd.getExitCode();
  }
  
  
  public static int downNI(String downCommand, String ni) throws InterruptedException, IOException
  {
    RuntimeResultDAO rrd = RuntimeUtil.runAndFinish(downCommand, ni);
    return rrd.getExitCode();
  }


  public static void main(String ...args)
  {
    for(String filename : args)
    {
      try
      {
        NIConfigDAO ret[] = readConfig(new FileReader(filename));
        for(NIConfigDAO nicd : ret)
        {
          System.out.println(GSONUtil.toJSON(nicd, false, false, false));
        }
      }
      catch(Exception e)
      {
        e.printStackTrace();
      }
    }
  }
}

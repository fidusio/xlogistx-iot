package io.xlogistx.iot.net;

import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.logging.Logger;

import io.xlogistx.iot.net.data.NIRenameDAO;
import org.zoxweb.server.util.RuntimeUtil;

import org.zoxweb.shared.util.SharedUtil;
import io.xlogistx.iot.net.data.OSConfigDAO;

public class NIRename 
{
  
  private static final transient Logger log = Logger.getLogger(NIRename.class.getName());
  private NIRename() {}
  
  public static NIRenameDAO renameNI(OSConfigDAO oscd, NIRenameDAO nird, long upDelay) throws IOException, InterruptedException
  {
    SharedUtil.checkIfNulls("Null nird or parameters", oscd, nird, nird.getNIToName(), nird.getSysNetFolder());
    return renameNI(oscd.getIfUpCommand(), nird, upDelay);
  }
  
  
  public static NIRenameDAO renameNI(String niUpCommand, NIRenameDAO nird, long upDelay) throws IOException, InterruptedException
  {
    SharedUtil.checkIfNulls("Null nird or parameters", niUpCommand, nird, nird.getNIToName(), nird.getSysNetFolder());
    NetworkInterface niToLocate = NetworkInterface.getByName(nird.getNIToName());
    if (niToLocate == null)
    {
      File folder = new File(nird.getSysNetFolder());
      if (!folder.isDirectory())
        throw new IllegalArgumentException(nird.getSysNetFolder() + " is not a folder.");
      for (File f : folder.listFiles())
      {
        //log.info("file:" + f.getName() + " isFile " + f.isFile() + " isDirectory " + f.isDirectory());
        if (f.isDirectory())
        {
          if (!nird.getFilteredNIs().contains(f.getName()))
          {
            nird.setNIToRename(f.getName());
            log.info("to rename:" + f);
            break;
          }
        }
      }
      
      if (nird.getNIToRename() != null)
      {
        log.info("" + RuntimeUtil.runAndFinish(nird.getScript(), nird.getNIToRename(), nird.getNIToName()));
        if (upDelay > 0)
        {
          try
          {
            Thread.sleep(upDelay);
          }
          catch(InterruptedException e)
          {
           e.printStackTrace();
          }
        }
        log.info("" + RuntimeUtil.runAndFinish(niUpCommand, nird.getNIToName()));
        if (upDelay > 0)
        {
          try
          {
            Thread.sleep(upDelay);
          }
          catch(InterruptedException e)
          {
           e.printStackTrace();
          }
        }
      }
    }
    
    return nird;
  }
}

package io.xlogistx.iot.net;


import java.io.IOException;

import io.xlogistx.iot.net.data.NIRenameConfig;
import io.xlogistx.iot.net.data.OSConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.zoxweb.server.util.GSONUtil;

public class NetDataDAOTest {

  @BeforeAll
  public static void setUpBeforeClass() throws Exception {}

  @Test
  public void niRenameDAO() throws IOException {
   NIRenameConfig nir = new NIRenameConfig();
   nir.setName("NIRename");
   nir.setDescription("NI rename dao config");
   nir.setNIToName("eth1");
   nir.setFilteredNIs("lo", "eth0");
   nir.setScript("rename_script");
   nir.setSysNetFolder("/sys/class/net");
   
   String json = GSONUtil.toJSON(nir, true, false, false);
   System.out.println(json);
   
   nir = GSONUtil.fromJSON(json, NIRenameConfig.class);
   json = GSONUtil.toJSON(nir, true, true, true);
   System.out.println(json);
   
   System.out.println(nir.getFilteredNIs());
   System.out.println(nir.getFilteredNIs().contains("lo"));
   System.out.println(nir.getFilteredNIs().contains("12321432"));
  }
  
  
  
  
  @Test
  public void osConfigDAO() throws IOException {
   OSConfig oscd = new OSConfig();
   oscd.setName("ArmBian");
   oscd.setDescription("ArmBian linux os for arm SBCs");
   oscd.setIfDownCommand("ifdown");
   oscd.setIfUpCommand("ifup");
   oscd.setNetworkInterfacesFolder("/etc/network/interfaces.d");
   oscd.setIfConfigCommand("ifconfig");
   
   String json = GSONUtil.toJSON(oscd, true, false, false);
   System.out.println(json);
   
   oscd = GSONUtil.fromJSON(json, OSConfig.class);
   json = GSONUtil.toJSON(oscd, true, true, true);
   System.out.println(json);
   
  
  }

}

package io.xlogistx.iot.net;


import java.io.IOException;

import io.xlogistx.iot.net.data.NIRenameDAO;
import io.xlogistx.iot.net.data.OSConfigDAO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.zoxweb.server.util.GSONUtil;

public class NetDataDAOTest {

  @BeforeAll
  public static void setUpBeforeClass() throws Exception {}

  @Test
  public void niRenameDAO() throws IOException {
   NIRenameDAO nir = new NIRenameDAO();
   nir.setName("NIRename");
   nir.setDescription("NI rename dao config");
   nir.setNIToName("eth1");
   nir.setFilteredNIs("lo", "eth0");
   nir.setScript("rename_script");
   nir.setSysNetFolder("/sys/class/net");
   
   String json = GSONUtil.toJSON(nir, true, false, false);
   System.out.println(json);
   
   nir = GSONUtil.fromJSON(json, NIRenameDAO.class);
   json = GSONUtil.toJSON(nir, true, true, true);
   System.out.println(json);
   
   System.out.println(nir.getFilteredNIs());
   System.out.println(nir.getFilteredNIs().contains("lo"));
   System.out.println(nir.getFilteredNIs().contains("12321432"));
  }
  
  
  
  
  @Test
  public void osConfigDAO() throws IOException {
   OSConfigDAO oscd = new OSConfigDAO();
   oscd.setName("ArmBian");
   oscd.setDescription("ArmBian linux os for arm SBCs");
   oscd.setIfDownCommand("ifdown");
   oscd.setIfUpCommand("ifup");
   oscd.setNetworkInterfacesFolder("/etc/network/interfaces.d");
   oscd.setIfConfigCommand("ifconfig");
   
   String json = GSONUtil.toJSON(oscd, true, false, false);
   System.out.println(json);
   
   oscd = GSONUtil.fromJSON(json, OSConfigDAO.class);
   json = GSONUtil.toJSON(oscd, true, true, true);
   System.out.println(json);
   
  
  }

}

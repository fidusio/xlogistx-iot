package io.xlogistx.iot.net;


import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.net.InetProp.InetProto;
import org.zoxweb.shared.net.NIConfigDAO;


public class TestNIGenerator {

  @BeforeAll
  public static void setUpBeforeClass() throws Exception {}

  @Test
  public void testStatic() throws IOException {
    String niConfigTemplate = IOUtil.inputStreamToString(this.getClass().getClassLoader().getResource("ni-config/ni_file_template").getFile());
    NIConfigDAO nicd = new NIConfigDAO();
    nicd.setName("wan");
    nicd.setNIName("eth0");
    nicd.setInteProtocol(InetProto.STATIC);
    nicd.setAddress("192.168.1.100");
    nicd.setNetmask("255.255.255.0");
    nicd.setGateway("192.168.1.1");
    nicd.setDNSServers("127.0.0.1");
    
    System.out.println(NIConfigurator.generateNIConfig(nicd, niConfigTemplate));
  }
  
  
  @Test
  public void testStaticNoGateway() throws IOException {
    String niConfigTemplate = IOUtil.inputStreamToString(this.getClass().getClassLoader().getResource("ni-config/ni_file_template").getFile());
    NIConfigDAO nicd = new NIConfigDAO();
    nicd.setName("lan");
    nicd.setNIName("eth1");
    nicd.setInteProtocol(InetProto.STATIC);
    nicd.setAddress("192.168.1.1");
    nicd.setNetmask("255.255.255.0");
    nicd.setDNSServers("127.0.0.1");
    
    System.out.println(NIConfigurator.generateNIConfig(nicd, niConfigTemplate));
  }
  
  
  @Test
  public void testDHCP() throws IOException {
    String niConfigTemplate = IOUtil.inputStreamToString(this.getClass().getClassLoader().getResource("ni-config/ni_file_template").getFile());
    NIConfigDAO nicd = new NIConfigDAO();
    nicd.setName("wan");
    nicd.setNIName("eth0");
    nicd.setInteProtocol(InetProto.DHCP);
    nicd.setDNSServers("127.0.0.1");
  
    System.out.println(NIConfigurator.generateNIConfig(nicd, niConfigTemplate));
  }
  @Test
  public void testRead() throws IOException
  {
    String niConfigTemplate = IOUtil.inputStreamToString(this.getClass().getClassLoader().getResource("ni-config/ni_file_template").getFile());
    NIConfigDAO nicd = new NIConfigDAO();
    nicd.setName("lan");
    nicd.setNIName("eth1");
    nicd.setInteProtocol(InetProto.STATIC);
    nicd.setAddress("192.168.1.1");
    nicd.setNetmask("255.255.255.0");
    nicd.setDNSServers("127.0.0.1");
    
    String config = NIConfigurator.generateNIConfig(nicd, niConfigTemplate);
    
    NIConfigDAO nicds[] = NIConfigurator.readConfig(config);
    for(NIConfigDAO nic : nicds)
    {
      System.out.println(GSONUtil.toJSON(nic, false, false, false));
    }
    
  }
  
  
  @Test
  public void testInterfaces() throws IOException
  {
    String config = IOUtil.inputStreamToString(this.getClass().getClassLoader().getResource("ni-config/interfaces").getFile());
    
    
    NIConfigDAO nicds[] = NIConfigurator.readConfig(config);
    for(NIConfigDAO nic : nicds)
    {
      System.out.println("Interfaces:" + GSONUtil.toJSON(nic, false, false, false));
    }
    
  }
}

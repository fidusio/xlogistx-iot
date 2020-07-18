package io.xlogistx.iot.net.data;


import org.zoxweb.shared.data.PropertyDAO;


import org.zoxweb.shared.util.GetName;
import org.zoxweb.shared.util.NVConfigEntity;
import org.zoxweb.shared.util.NVConfigEntityLocal;
import org.zoxweb.shared.util.SharedUtil;


@SuppressWarnings("serial")
public class OSConfigDAO
  extends PropertyDAO
{

  public enum Param
  implements GetName
  {
    NI_FOLDER("ni_folder"),
    IFUP_CMD("ifup_cmd"),
    IFDOWN_CMD("ifdown_cmd"),
    IFCONFIG_CMD("ifconfig_cmd"),
    NI_TEMPLATE("ni_template");
    ;
    private String name;
  
    Param(String name)
    {
        this.name = name;
    }
  
    

    @Override
    public String getName() {
      // TODO Auto-generated method stub
      return name;
    }
  }
  
  
  
  /**
   * This NVConfigEntity type constant is set to an instantiation of a NVConfigEntityLocal object based on DataContentDAO.
   */
  public static final NVConfigEntity NVC_OS_CONFIG_PROPERTIES_DAO = new NVConfigEntityLocal("os_config_dao",
                                                                              null,
                                                                              "OSConfigDAO", 
                                                                              true,
                                                                              false,
                                                                              false,
                                                                              false,
                                                                              OSConfigDAO.class,
                                                                              SharedUtil.extractNVConfigs(),
                                                                              null,
                                                                              false,
                                                                              PropertyDAO.NVC_PROPERTY_DAO);
  
  
  public OSConfigDAO()
  {
    super(NVC_OS_CONFIG_PROPERTIES_DAO);
  }
  
  
  public String getNetworkInterfaceFolder()
  {
    return getProperties().getValue(Param.NI_FOLDER);
  }
  
  public void setNetworkInterfacesFolder(String path)
  {
    getProperties().add(Param.NI_FOLDER, path);
  }
  
  public String getIfUpCommand()
  {
    return getProperties().getValue(Param.IFUP_CMD);
  }
  
  public void setIfUpCommand(String command)
  {
    getProperties().add(Param.IFUP_CMD, command);
  }
  
  public String getIfDownCommand()
  {
    return getProperties().getValue(Param.IFDOWN_CMD);
  }
  
  public void setIfDownCommand(String command)
  {
    getProperties().add(Param.IFDOWN_CMD, command);
  }
  
  public String getIfConfigCommand()
  {
    return getProperties().getValue(Param.IFCONFIG_CMD);
  }
  
  public void setIfConfigCommand(String command)
  {
    getProperties().add(Param.IFCONFIG_CMD, command);
  }
  
  public String getNITemplate()
  {
    return getProperties().getValue(Param.NI_TEMPLATE);
  }
  
  public void setNITemplate(String templateFilename)
  {
    getProperties().add(Param.NI_TEMPLATE, templateFilename);
  }
}

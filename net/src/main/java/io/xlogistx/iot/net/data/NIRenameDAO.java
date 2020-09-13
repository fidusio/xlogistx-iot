package io.xlogistx.iot.net.data;

import java.util.List;
import org.zoxweb.shared.data.PropertyDAO;
import org.zoxweb.shared.util.GetName;
import org.zoxweb.shared.util.NVConfigEntity;
import org.zoxweb.shared.util.NVConfigEntityLocal;
import org.zoxweb.shared.util.NVStringList;
import org.zoxweb.shared.util.SharedUtil;

@SuppressWarnings("serial")
public class NIRenameDAO
  extends PropertyDAO
{

  public enum Param
  implements GetName
  {
    SYS_NET_FOLDER("sys_net_folder"),
    FILTERED_NIS("filtered_nis"),
    NI_TO_NAME("ni_to_name"),
    NI_TO_RENAME("ni_to_rename"),
    SCRIPT("script"),
    ;

    private final String name;
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
  
  
  public static final NVConfigEntity NVC_NI_RENAME_DAO = new NVConfigEntityLocal("ni_rename_dao",
      null,
      "NIRenameDAO", 
      true,
      false,
      false,
      false,
      PropertyDAO.class,
      SharedUtil.extractNVConfigs(),
      null,
      false,
      PropertyDAO.NVC_PROPERTY_DAO);
  
  public NIRenameDAO()
  {
    super(NVC_NI_RENAME_DAO);
    setFilteredNIs();
  }
  
  public String getSysNetFolder()
  {
    return getProperties().getValue((GetName)Param.SYS_NET_FOLDER);
  }
  
  public void setSysNetFolder(String path)
  {
    getProperties().add(Param.SYS_NET_FOLDER, path);
  }
  
  public List<String> getFilteredNIs()
  {
    return getProperties().getValue((GetName)Param.FILTERED_NIS);
  }
  
  public void setFilteredNIs(String ...nis)
  {
    NVStringList nvsl = new NVStringList(Param.FILTERED_NIS.getName());
    SharedUtil.addTo(nvsl.getValue(), nis);
    getProperties().add(nvsl);
  }
  
  
  public String getNIToName()
  {
    return getProperties().getValue((GetName)Param.NI_TO_NAME);
  }
  
  public void setNIToName(String niToName)
  {
    getProperties().add(Param.NI_TO_NAME, niToName);
  }
  
  public String getNIToRename()
  {
    return getProperties().getValue((GetName)Param.NI_TO_RENAME);
  }
  
  public void setNIToRename(String niToRename)
  {
    getProperties().add(Param.NI_TO_RENAME, niToRename);
  }
  
 
  
  
  public String getScript()
  {
    return getProperties().getValue((GetName)Param.SCRIPT);
  }
  
  public void setScript(String script)
  {
    getProperties().add(Param.SCRIPT, script);
  }
}

package com.webank.wedatasphere.dss.framework.workspace.dao;

import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateUserDO;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.PermissionUserCountDO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author arionliu
* @description 针对表【dss_ec_config_template_user(参数模板可见用户表)】的数据库操作Mapper
* @createDate 2023-06-16 11:36:08
* @Entity com.webank.wedatasphere.dss.framework.workspace.dao.entity.EcConfigTemplateUserDO
*/
public interface ECConfigTemplateUserMapper {






    int insertBatch(List<ECConfigTemplateUserDO> list);

    List<ECConfigTemplateUserDO> selectByTemplateId(String templateId);
    List<ECConfigTemplateUserDO> selectByUsers( @Param("workspaceId")Long workspaceId, @Param("userNames")List<String> userNames);
    int deleteByTemplateId(String templateId);

    @MapKey("groupId")
    Map<String, PermissionUserCountDO> getPermissionUserCount(List<String> list);

    int checkTemplateUse(@Param("templateId") String templateId);

}

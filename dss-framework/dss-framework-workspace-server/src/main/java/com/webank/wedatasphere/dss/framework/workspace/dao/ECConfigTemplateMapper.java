package com.webank.wedatasphere.dss.framework.workspace.dao;

import com.webank.wedatasphere.dss.framework.workspace.bean.request.ECConfTemplateGetRequest;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author arionliu
* @description 针对表【dss_ec_config_template(参数模板表)】的数据库操作Mapper
* @createDate 2023-06-14 17:11:01
* @Entity com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateDO
*/
public interface ECConfigTemplateMapper {

    int deleteByTemplateId(String templateId);

    int insert(ECConfigTemplateDO record);

    ECConfigTemplateDO getByTemplateId(String templateId);

    List<ECConfigTemplateDO> getTemplatesList(ECConfTemplateGetRequest request);
    List<ECConfigTemplateDO> getTemplatesByWorkspaceId(@Param("workspaceId") Long workspaceId);
    List<ECConfigTemplateDO> getTemplatesListByUids( @Param("templateIds")List<String> templateIds);

    ECConfigTemplateDO getByName(String name);

    int update(ECConfigTemplateDO record);


}

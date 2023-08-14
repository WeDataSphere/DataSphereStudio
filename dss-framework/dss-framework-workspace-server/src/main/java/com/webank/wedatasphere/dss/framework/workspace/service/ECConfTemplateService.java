package com.webank.wedatasphere.dss.framework.workspace.service;

import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECConfItem;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECConfTemplate;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.ECConfTemplateGetRequest;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.TemplatePermissionUsersGetRequest;

import java.util.List;

/**
 * 资源参数模板服务
 * Author: xlinliu
 * Date: 2023/6/14
 */
public interface ECConfTemplateService {
    void checkConfTemplateName(String name) ;
    List<String> getEngineTypeList(String applicationFilter, String operateUser);
    List<String> getEngineNameList(String applicationFilter, String operateUser);
    List<String> getApplicationList(String operateUser);
    /**
     * 获取单个参数模板配置详情
     * @param templateId 参数模板id
     */
    ECConfTemplate getTemplateParamDetail(String templateId, String operateUser);

    List<ECConfItem> getTemplateParamConf(String engineType, String operateUser);

    /**
     * 分页获取工作空间内的模板
     * @return
     */
    PageInfo <ECConfTemplate> getTemplateList(ECConfTemplateGetRequest request);
    ECConfTemplate getTemplate(String templateId);
    ECConfTemplate saveTemplate(ECConfTemplate ecConfTemplate,Long workspaceId,String operator);

    PageInfo<String> getPermissionUserList(TemplatePermissionUsersGetRequest request);
    void deleteTemplate(String templateId);

    boolean checkTemplateUse(String templateId);
}

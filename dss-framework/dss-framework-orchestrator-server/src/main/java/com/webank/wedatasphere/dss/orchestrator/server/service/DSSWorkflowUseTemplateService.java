package com.webank.wedatasphere.dss.orchestrator.server.service;

import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.framework.project.request.DSSWorkflowUseTemplateRequest;
import com.webank.wedatasphere.dss.orchestrator.server.bean.ECTemplateWorkflow;

import java.util.List;

public interface DSSWorkflowUseTemplateService {

    /**
     * 工作流模板引用列表分页查询
     * @param request
     * @return
     */
    PageInfo<ECTemplateWorkflow> getTemplateWorkflowList(DSSWorkflowUseTemplateRequest request);

    /**
     * 工作流引用模板搜索模块
     * 获取项目名称列表
     * @return
     */
    List<String> getTemplateProjectNames(String templateId);

    /**
     * 工作流引用模板搜索模块
     * 获取工作流名称列表
     * @return
     */
    List<String> getTemplateflowNames(String templateId);

}

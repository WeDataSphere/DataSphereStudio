package com.webank.wedatasphere.dss.orchestrator.server.service;

import com.webank.wedatasphere.dss.framework.project.request.WorkflowDefaultTemplateRequest;
import com.webank.wedatasphere.dss.orchestrator.server.bean.ECTemplateWorkflowDefault;

import java.util.List;


public interface DSSWorkflowDefaultTemplateService {

    /**
     * 创建/修改编排接口（保存工作流默认模板）
     * @param request
     */
    void saveTemplateRef(WorkflowDefaultTemplateRequest request);

    /**
     * 获取工作流默认模板（点击配置时回显）
     * @param orchestratorId
     * @return
     */
    List<ECTemplateWorkflowDefault> getWrokflowDefaultTemplates(Long orchestratorId);
}

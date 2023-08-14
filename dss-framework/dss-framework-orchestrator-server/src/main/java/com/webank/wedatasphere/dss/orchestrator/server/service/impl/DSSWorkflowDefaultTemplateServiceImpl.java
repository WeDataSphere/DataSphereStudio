package com.webank.wedatasphere.dss.orchestrator.server.service.impl;

import com.webank.wedatasphere.dss.framework.project.dao.ECTemplateWorkflowDefaultMapper;
import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkflowDefaultDO;
import com.webank.wedatasphere.dss.framework.project.request.WorkflowDefaultTemplateRequest;
import com.webank.wedatasphere.dss.orchestrator.server.bean.ECTemplateWorkflowDefault;
import com.webank.wedatasphere.dss.orchestrator.server.service.DSSWorkflowDefaultTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DSSWorkflowDefaultTemplateServiceImpl implements DSSWorkflowDefaultTemplateService {

    @Autowired
    ECTemplateWorkflowDefaultMapper templateWorkflowDefaultMapper;

    @Override
    public void saveTemplateRef(WorkflowDefaultTemplateRequest request) {
        //如果request.templates为空，则表示移除了所有的工作流默认模板
        if(CollectionUtils.isEmpty(request.getTemplateIds())){
            templateWorkflowDefaultMapper.deleteWorkflowTemplateRef(request.getOrchestratorId());
            return;
        }
        templateWorkflowDefaultMapper.deleteWorkflowTemplateRef(request.getOrchestratorId());
        List<ECTemplateWorkflowDefaultDO> list = new ArrayList<>();
        request.getTemplateIds().forEach(item ->{
            ECTemplateWorkflowDefaultDO defaultDO = new ECTemplateWorkflowDefaultDO();
            defaultDO.setTemplateId(item);
            defaultDO.setOrchestratorId(request.getOrchestratorId());
            defaultDO.setProjectId(request.getProjectId());
            defaultDO.setCreateUser(request.getCreateUser());
            defaultDO.setUpdateUser(request.getUpdateUser());
            list.add(defaultDO);
        });
        //新增默认
        templateWorkflowDefaultMapper.batchInsert(list);
    }

    @Override
    public List<ECTemplateWorkflowDefault> getWrokflowDefaultTemplates(Long orchestratorId) {
        return templateWorkflowDefaultMapper.getWorkflowDefaultTemplates(orchestratorId).stream().map(ECTemplateWorkflowDefault::convertToBean).collect(Collectors.toList());
    }
}

package com.webank.wedatasphere.dss.orchestrator.server.service.impl;

import com.github.pagehelper.PageHelper;
import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.framework.project.dao.ECTemplateWorkflowMapper;
import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkflowDO;
import com.webank.wedatasphere.dss.framework.project.request.DSSWorkflowUseTemplateRequest;
import com.webank.wedatasphere.dss.orchestrator.server.bean.ECTemplateWorkflow;
import com.webank.wedatasphere.dss.orchestrator.server.service.DSSWorkflowUseTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DSSWorkflowUseTemplateServiceImpl implements DSSWorkflowUseTemplateService {

    private final static Logger LOGGER = LoggerFactory.getLogger(DSSWorkflowUseTemplateServiceImpl.class);

    @Autowired
    private ECTemplateWorkflowMapper templateWorkflowMapper;

    @Override
    public PageInfo<ECTemplateWorkflow> getTemplateWorkflowList(DSSWorkflowUseTemplateRequest request) {
        PageHelper.startPage(request.getPageNow(), request.getPageSize());
        List<ECTemplateWorkflowDO> workflowUseTemplates = templateWorkflowMapper.getAllWorkflowUseTemplates(request);
        com.github.pagehelper.PageInfo<ECTemplateWorkflowDO> doPage = new com.github.pagehelper.PageInfo<>(workflowUseTemplates);
        return new PageInfo<>(workflowUseTemplates.stream().map(ECTemplateWorkflow::convertToBean).collect(Collectors.toList()), doPage.getTotal());
    }

    @Override
    public List<String> getTemplateProjectNames(String templateId) {
        return templateWorkflowMapper.getTemplateProjectNames(templateId);
    }

    @Override
    public List<String> getTemplateflowNames(String templateId) {
        return templateWorkflowMapper.getTemplateOrchestratorNames(templateId);
    }

}

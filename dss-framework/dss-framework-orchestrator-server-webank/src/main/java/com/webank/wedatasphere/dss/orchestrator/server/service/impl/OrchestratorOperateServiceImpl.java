package com.webank.wedatasphere.dss.orchestrator.server.service.impl;

import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.framework.project.dao.ECTemplateWorkflowDefaultMapper;
import com.webank.wedatasphere.dss.framework.project.dao.ECTemplateWorkflowMapper;
import com.webank.wedatasphere.dss.orchestrator.server.service.OrchestratorOperateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrchestratorOperateServiceImpl implements OrchestratorOperateService {

    private final static Logger LOGGER = LoggerFactory.getLogger(OrchestratorOperateServiceImpl.class);

    @Autowired
    private ECTemplateWorkflowDefaultMapper ecTemplateWorkflowDefaultMapper;

    @Autowired
    private ECTemplateWorkflowMapper ecTemplateWorkflowMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteTemplateOperate(Long orchestratorId) {
        LOGGER.info("begin to delete workflowtemplateref and worflowUse , orchestratorId:{}",orchestratorId);
        try {
            ecTemplateWorkflowDefaultMapper.deleteWorkflowTemplateRef(orchestratorId);
            ecTemplateWorkflowMapper.deleteWorkflowUse(orchestratorId, null);
        } catch (Exception e) {
            throw new DSSRuntimeException("删除工作流引用关系失败！");
        }
    }
}

package com.webank.wedatasphere.dss.workflow.service.impl;

import com.webank.wedatasphere.dss.common.entity.node.DSSNode;
import com.webank.wedatasphere.dss.framework.project.dao.ECTemplateWorkflowMapper;
import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkflowDO;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlow;
import com.webank.wedatasphere.dss.workflow.common.parser.WorkFlowParser;
import com.webank.wedatasphere.dss.workflow.dao.FlowMapper;
import com.webank.wedatasphere.dss.workflow.dao.DSSFlowMapper;
import com.webank.wedatasphere.dss.workflow.service.SaveFlowHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: xlinliu
 * Date: 2023/7/28
 */
@Service
public class WebankSaveFlowHookImpl implements SaveFlowHook {
    private static final Logger logger = LoggerFactory.getLogger(WebankSaveFlowHookImpl.class);
    @Autowired
    private WorkFlowParser workFlowParser;
    @Autowired
    private DSSFlowMapper DSSFlowMapper;
    @Autowired
    ECTemplateWorkflowMapper ecTemplateWorkflowMapper;
    @Autowired
    private FlowMapper flowMapper;
    @Override
    public void beforeSave(String jsonFlow, DSSFlow dssFlow, Long parentFlowID) {

    }

    @Override
    public void afterSave(String jsonFlow, DSSFlow dssFlow, Long parentFlowID) {
        Long projectId = dssFlow.getProjectId();
        Long flowId=dssFlow.getId();
        //和编排关联的工作流的id
        Long orcRefFlowId = flowId;
        while (parentFlowID != null) {
            orcRefFlowId = parentFlowID;
            parentFlowID = flowMapper.getParentFlowID(orcRefFlowId);
        }
        DSSOrchestratorVersion version = DSSFlowMapper.getLatestOrcVersionByAppId(orcRefFlowId);
        Long orcId = version.getOrchestratorId();
        List<DSSNode> nodes = workFlowParser.getWorkFlowNodes(jsonFlow);
        List<String> templateIds = workFlowParser.getParamConfTemplate(jsonFlow) ;

        ecTemplateWorkflowMapper.deleteWorkflowUse(orcId,flowId);
        if(!templateIds.isEmpty()){
            List<ECTemplateWorkflowDO> ecTemplateWorkflowDOs = templateIds.stream().map(e -> new ECTemplateWorkflowDO(e, projectId, orcId, flowId)).collect(Collectors.toList());
            logger.info("update workflow template ref. orchestratorId:{},rootFlowId:{},flowId:{}",orcId,orcRefFlowId,flowId);
            logger.info("new templateIds:{},",templateIds);
            ecTemplateWorkflowMapper.batchInsert(ecTemplateWorkflowDOs);
        }
    }
}

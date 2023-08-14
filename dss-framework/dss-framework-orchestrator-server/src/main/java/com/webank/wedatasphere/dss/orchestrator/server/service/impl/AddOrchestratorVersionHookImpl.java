package com.webank.wedatasphere.dss.orchestrator.server.service.impl;

import com.webank.wedatasphere.dss.framework.project.dao.ECTemplateWorkflowMapper;
import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkflowDO;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;
import com.webank.wedatasphere.dss.orchestrator.common.ref.OrchestratorRefConstant;
import com.webank.wedatasphere.dss.orchestrator.server.service.AddOrchestratorVersionHook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author: xlinliu
 * Date: 2023/8/1
 */
@Service
public class AddOrchestratorVersionHookImpl implements AddOrchestratorVersionHook {
    @Autowired
    ECTemplateWorkflowMapper ecTemplateWorkflowMapper;
    @Override
    public void beforeAdd(DSSOrchestratorVersion oldVersion, Map<String, Object> content) {
        ecTemplateWorkflowMapper.deleteAllWorkflowUseOfOrc(oldVersion.getOrchestratorId());
    }

    @Override
    public void afterAdd(DSSOrchestratorVersion newVersion, Map<String, Object> content) {
        Long projectId= newVersion.getProjectId();
        Long orcId= newVersion.getOrchestratorId();
        List<String[]> flowIdTemplateIdTuples = (List<String[]>) content.get(OrchestratorRefConstant.ORCHESTRATION_FLOWID_PARAMCONF_TEMPLATEID_TUPLES_KEY);
        if(flowIdTemplateIdTuples!=null){
            List<ECTemplateWorkflowDO> ecTemplateWorkflowDOS = flowIdTemplateIdTuples.stream()
                    .map(e -> new ECTemplateWorkflowDO(e[1], projectId, orcId, Long.valueOf(e[0])))
                    .collect(Collectors.toList());
            ecTemplateWorkflowMapper.batchInsert(ecTemplateWorkflowDOS);
        }
    }
}

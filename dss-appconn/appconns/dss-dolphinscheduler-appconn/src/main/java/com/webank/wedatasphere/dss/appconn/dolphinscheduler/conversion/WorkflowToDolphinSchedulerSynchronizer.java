/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.webank.wedatasphere.dss.appconn.dolphinscheduler.conversion;

import com.webank.wedatasphere.dss.appconn.dolphinscheduler.DolphinSchedulerAppConn;
import com.webank.wedatasphere.dss.appconn.dolphinscheduler.entity.DolphinSchedulerConvertedRel;
import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.OrchestrationUpdateOperation;
import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.ref.OrchestrationUpdateRequestRef;
import com.webank.wedatasphere.dss.common.entity.node.DSSNode;
import com.webank.wedatasphere.dss.orchestrator.converter.standard.operation.DSSToRelConversionOperation;
import com.webank.wedatasphere.dss.orchestrator.converter.standard.ref.OrchestrationToRelConversionRequestRef;
import com.webank.wedatasphere.dss.standard.common.exception.operation.ExternalOperationFailedException;
import com.webank.wedatasphere.dss.workflow.conversion.entity.ConvertedRel;
import com.webank.wedatasphere.dss.workflow.conversion.operation.WorkflowToRelSynchronizer;
import com.webank.wedatasphere.dss.workflow.core.entity.Workflow;
import com.webank.wedatasphere.dss.workflow.core.entity.WorkflowNode;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @author enjoyyin
 * @date 2022-03-17
 * @since 0.5.0
 */
public class WorkflowToDolphinSchedulerSynchronizer implements WorkflowToRelSynchronizer {

    private DSSToRelConversionOperation dssToRelConversionOperation;


    @Override
    public void setDSSToRelConversionOperation(DSSToRelConversionOperation dssToRelConversionOperation) {
        this.dssToRelConversionOperation = dssToRelConversionOperation;
    }

    @Override
    public void syncToRel(ConvertedRel convertedRel) {
        DolphinSchedulerConvertedRel dolphinSchedulerConvertedRel = (DolphinSchedulerConvertedRel)convertedRel;
        OrchestrationToRelConversionRequestRef requestRef = dolphinSchedulerConvertedRel.getDSSToRelConversionRequestRef();
        Workflow workflow = dolphinSchedulerConvertedRel.getWorkflow();
        checkSchedulerProject(workflow);
        Long dolphinSchedulerWorkflowId = requestRef.getRefOrchestrationId();
        DolphinSchedulerAppConn appConn = (DolphinSchedulerAppConn) dssToRelConversionOperation.getConversionService().getAppStandard().getAppConn();
        OrchestrationUpdateOperation updateOperation = appConn.getOrCreateStructureStandard().getOrchestrationService(dssToRelConversionOperation.getConversionService().getAppInstance())
            .getOrchestrationUpdateOperation();
        OrchestrationUpdateRequestRef.OrchestrationUpdateRequestRefImpl ref = new OrchestrationUpdateRequestRef.OrchestrationUpdateRequestRefImpl()
                .setRefOrchestrationId(dolphinSchedulerWorkflowId).setProjectName(requestRef.getDSSProject().getName())
                .setUserName(requestRef.getUserName()).setWorkspace(requestRef.getWorkspace()).setDSSOrchestration(workflow);
        updateOperation.updateOrchestration(ref);
    }

    private void checkSchedulerProject(Workflow flow) throws ExternalOperationFailedException {
        List<WorkflowNode> nodes = flow.getWorkflowNodes();
        for (WorkflowNode node : nodes) {
            DSSNode dssNode = node.getDSSNode();
            if (CollectionUtils.isEmpty(dssNode.getResources())) {
                throw new ExternalOperationFailedException(90021, dssNode.getName() + "节点内容不能为空");
            }
        }
    }

}

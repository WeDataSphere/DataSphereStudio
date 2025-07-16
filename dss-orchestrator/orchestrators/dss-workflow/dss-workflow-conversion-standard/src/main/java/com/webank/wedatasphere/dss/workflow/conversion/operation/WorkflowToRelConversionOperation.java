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
package com.webank.wedatasphere.dss.workflow.conversion.operation;

import com.webank.wedatasphere.dss.orchestrator.converter.standard.ref.DSSToRelConversionRequestRef;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlow;
import com.webank.wedatasphere.dss.workflow.conversion.entity.PreConversionRel;
import com.webank.wedatasphere.dss.workflow.conversion.entity.WorkflowPreConversionRelImpl;
import com.webank.wedatasphere.dss.workflow.core.WorkflowFactory;
import com.webank.wedatasphere.dss.workflow.core.entity.Workflow;

/**
 * @author enjoyyin
 * @date 2022-03-16
 * @since 0.5.0
 */
public class WorkflowToRelConversionOperation
        extends AbstractDSSToRelConversionOperation<DSSToRelConversionRequestRef.OrchestrationToRelConversionRequestRefImpl> {

    @Override
    protected PreConversionRel getPreConversionRel(DSSToRelConversionRequestRef.OrchestrationToRelConversionRequestRefImpl ref) {
        Workflow workflow = WorkflowFactory.INSTANCE.getJsonToFlowParser().parse((DSSFlow) ref.getDSSOrchestration());
        WorkflowPreConversionRelImpl rel = new WorkflowPreConversionRelImpl();
        rel.setDSSToRelConversionRequestRef(ref);
        rel.setWorkflow(workflow);
        return rel;
    }

}

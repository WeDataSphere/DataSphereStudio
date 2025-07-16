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
package com.webank.wedatasphere.dss.appconn.dolphinscheduler.entity;

import com.webank.wedatasphere.dss.orchestrator.converter.standard.ref.OrchestrationToRelConversionRequestRef;
import com.webank.wedatasphere.dss.workflow.conversion.entity.ConvertedRel;
import com.webank.wedatasphere.dss.workflow.conversion.entity.PreConversionRel;
import com.webank.wedatasphere.dss.workflow.conversion.entity.WorkflowPreConversionRel;
import com.webank.wedatasphere.dss.workflow.conversion.entity.WorkflowPreConversionRelImpl;

public class DolphinSchedulerConvertedRel extends WorkflowPreConversionRelImpl implements ConvertedRel {

    public DolphinSchedulerConvertedRel(PreConversionRel rel) {
        setWorkflow(((WorkflowPreConversionRel) rel).getWorkflow());
        setDSSToRelConversionRequestRef(rel.getDSSToRelConversionRequestRef());
    }

    @Override
    public OrchestrationToRelConversionRequestRef getDSSToRelConversionRequestRef() {
        return (OrchestrationToRelConversionRequestRef) super.getDSSToRelConversionRequestRef();
    }

}

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

import com.webank.wedatasphere.dss.appconn.dolphinscheduler.entity.DolphinSchedulerConvertedRel;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.workflow.conversion.entity.ConvertedRel;
import com.webank.wedatasphere.dss.workflow.conversion.entity.PreConversionRel;
import com.webank.wedatasphere.dss.workflow.conversion.entity.WorkflowPreConversionRel;
import com.webank.wedatasphere.dss.workflow.conversion.operation.WorkflowToRelConverter;
import com.webank.wedatasphere.dss.workflow.core.entity.Workflow;
import com.webank.wedatasphere.dss.workflow.core.entity.WorkflowNode;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DolphinSchedulerWorkflowToRelConverter implements WorkflowToRelConverter {

    @Override
    public ConvertedRel convertToRel(PreConversionRel rel) {
        Workflow workflow = ((WorkflowPreConversionRel) rel).getWorkflow();
        String repeatNodes = workflow.getWorkflowNodes().stream().map(WorkflowNode::getName)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream().filter(entry -> entry.getValue() > 1).map(Map.Entry::getKey)
                .collect(Collectors.joining(", "));
        if (StringUtils.isNotEmpty(repeatNodes)) {
            throw new DSSRuntimeException(80001, "重复的节点名称。项目中不同工作流（或子工作流）里存在重名节点，请修改节点名避免重名。重名节点：" + repeatNodes);
        }
        DolphinSchedulerConvertedRel dolphinSchedulerConvertedRel = new DolphinSchedulerConvertedRel(rel);
        return dolphinSchedulerConvertedRel;
    }

    @Override
    public int getOrder() {
        return 5;
    }
}

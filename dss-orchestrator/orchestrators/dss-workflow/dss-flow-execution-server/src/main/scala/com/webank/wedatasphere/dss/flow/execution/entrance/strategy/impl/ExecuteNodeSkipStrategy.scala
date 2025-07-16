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
package com.webank.wedatasphere.dss.flow.execution.entrance.strategy.impl

import java.util

import com.webank.wedatasphere.dss.flow.execution.entrance.conf.FlowExecutionEntranceConfiguration
import com.webank.wedatasphere.dss.flow.execution.entrance.strategy.NodeSkipStrategy
import com.webank.wedatasphere.dss.workflow.core.entity.WorkflowNode

class ExecuteNodeSkipStrategy extends NodeSkipStrategy {
  override def isSkippedNode(node: WorkflowNode, paramsMap: util.Map[String, Any], isReversedChoose: Boolean): Boolean = {
    FlowExecutionEntranceConfiguration.SKIP_NODES.getValue.split(",").exists(_.equalsIgnoreCase(node.getNodeType))
  }
}

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
package com.webank.wedatasphere.dss.flow.execution.entrance.strategy

import com.webank.wedatasphere.dss.flow.execution.entrance.conf.FlowExecutionEntranceConfiguration
import com.webank.wedatasphere.dss.workflow.core.entity.WorkflowNode

trait NodeSkipStrategy {
  /**
   * 判断节点是否需要跳过
   *
   * @param node             节点信息
   * @param paramsMap        执行入参
   * @param isReversedChoose 是否是反选
   * @return true/false
   *
   *
   */
  def isSkippedNode(node: WorkflowNode, paramsMap: java.util.Map[String, Any], isReversedChoose: Boolean): Boolean = {
    if (FlowExecutionEntranceConfiguration.SKIP_NODES.getValue.split(",").exists(_.equalsIgnoreCase(node.getNodeType))) {
      return true
    }
    val nodeIdObj = paramsMap.get("nodeID")
    if (nodeIdObj != null) {
      if (isReversedChoose) {
        return !nodeIdObj.toString.split(",").exists(_.equalsIgnoreCase(node.getId))
      } else {
        return nodeIdObj.toString.split(",").exists(_.equalsIgnoreCase(node.getId))
      }
    }
    false
  }
}
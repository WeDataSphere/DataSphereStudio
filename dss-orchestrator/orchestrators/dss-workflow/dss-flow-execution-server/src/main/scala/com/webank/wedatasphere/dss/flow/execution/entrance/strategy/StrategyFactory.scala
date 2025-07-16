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

import com.webank.wedatasphere.dss.flow.execution.entrance.enums.ExecuteStrategyEnum
import com.webank.wedatasphere.dss.flow.execution.entrance.strategy.impl.{ExecuteNodeSkipStrategy, ReExecuteNodeSkipStrategy, SelectedExecuteNodeSkipStrategy}

object StrategyFactory {

  def getNodeSkipStrategy(executeStrategy: String): NodeSkipStrategy = {
    val skipStrategy = ExecuteStrategyEnum.getEnum(executeStrategy) match {
      case ExecuteStrategyEnum.IS_RE_EXECUTE => new ReExecuteNodeSkipStrategy
      case ExecuteStrategyEnum.IS_SELECTED_EXECUTE => new SelectedExecuteNodeSkipStrategy
      case ExecuteStrategyEnum.IS_EXECUTE => new ExecuteNodeSkipStrategy
      case _ => new ExecuteNodeSkipStrategy
    }
    skipStrategy
  }
}

/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.dss.appjoint.sendemail

import com.webank.wedatasphere.dss.appjoint.execution.core.{AppJointNode, NodeContext}

/**
  * Created by enjoyyin on 2019/10/12.
  */
trait SendEmailNodeExecutionHook {

  def preGenerate(node: AppJointNode, nodeContext: NodeContext): Unit

  def preSend(node: AppJointNode, nodeContext: NodeContext, email: Email): Unit

}

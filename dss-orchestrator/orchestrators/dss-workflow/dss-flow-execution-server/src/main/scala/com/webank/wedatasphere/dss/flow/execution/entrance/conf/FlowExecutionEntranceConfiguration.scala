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

package com.webank.wedatasphere.dss.flow.execution.entrance.conf

import org.apache.linkis.common.conf.CommonVars


object FlowExecutionEntranceConfiguration {

  val NODE_PRINT_FLAG = CommonVars("wds.linkis.flow.node.log.flag", false)

  val NODE_STATUS_POLLER_THREAD_SIZE = CommonVars("wds.dds.flow.node.status.poller.thread.size", 20)

  val NODE_STATUS_POLLER_SCHEDULER_TIME = CommonVars("wds.dds.flow.node.status.poller.scheduler.time", 5)
  val FLOW_EXECUTION_SCHEDULER_POOL_SIZE = CommonVars("wds.linkis.flow.execution.pool.size", 30)

  val NODE_STATUS_INTERVAL = CommonVars("wds.dds.flow.node.status.poller.interval.time", 3000)

  val COMMAND = "command"

  val JOB_ID = "job.id"

  val FLOW_NAME = "dss.flow.flowid"


  val PROJECT_NAME = "dss.flow.projectname"

  val FLOW_EXEC_ID = "dss.flow.execid"

  val WORKSPACE_ID = "dss.flow.workspace.id"


  val PROXY_USER = "user.to.proxy"


  val FLOW_SUBMIT_USER = "flow.submituser"


  val SKIP_NODES =   CommonVars("wds.dss.flow.skip.nodes", "workflow.subflow")

  val SIGNAL_NODES = CommonVars("wds.dss.flow.signal.nodes", "linkis.appconn.eventchecker.eventreceiver")

  // ===== 以下为 REQ-DSS-1.26.0-FIX-001 新增 =====

  // P0：连续状态获取失败升级阈值（毫秒）。自最近一次成功获取到 entrance 状态起算，
  // 持续失败达到该时长后 kill Linkis 任务并将节点置为 Failed。
  // 默认 30 分钟：entrance Full GC 恢复通常为分钟级；readTimeout=300s 时约容忍 6 次连续失败，
  // readTimeout 调优为 60s 后约容忍 30 次，可区分"抖动"与"真不可用"。<=0 视为无限等待（不建议）。
  val NODE_STATE_FETCH_FAIL_TIMEOUT = CommonVars("wds.dss.flow.node.state.fetch.fail.timeout", 30 * 60 * 1000L)

  // P1：节点级最大运行时长兜底（毫秒），-1 表示关闭。
  // 从节点 startTime 起算，非终态且超过该时长时节点翻转为 Timeout 终态。
  val NODE_MAX_RUNNING_TIME = CommonVars("wds.dss.flow.node.max.running.time", -1L)

  // P1：信号节点（EventReceiver 等）最大运行时长兜底（毫秒），-1 表示关闭。
  // 未显式配置 NODE_MAX_RUNNING_TIME 的信号节点按本值兜底；默认 2 小时 = 2 × entrance 侧
  // max.receive.hours（默认 1 小时，EventChecker.java:68-70），留足余量避免与 entrance 侧
  // maxLoopTime 兜底竞态误杀仍在合法等待的节点。
  val SIGNAL_NODE_MAX_RUNNING_TIME = CommonVars("wds.dss.flow.node.max.running.time.signal", 2 * 60 * 60 * 1000L)

  val PROPS_MAP = "props_map"

  val WORKSPACE = "workspace"

  val CONTEXT_ID = "contextID"

  val NODE_NAME = "nodeName"

  val FLOW_VAR_MAP = "flow_var_map"


  val NODE_CONFIGURATION_KEY = "configuration"

  val PROJECT_RESOURCES = "project_resources"

  val FLOW_RESOURCES = "flow_resources"

  val START_UP = "startup"

  val YARN_QUEUE = "wds.linkis.rm.yarnqueue"

  val CONFIGURATION = "configuration"

  val USER_TO_PROXY = "user.to.proxy"

}

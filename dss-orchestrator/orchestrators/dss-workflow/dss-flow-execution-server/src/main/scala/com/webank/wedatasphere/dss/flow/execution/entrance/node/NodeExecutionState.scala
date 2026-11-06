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

package com.webank.wedatasphere.dss.flow.execution.entrance.node


import org.apache.linkis.common.utils.Logging


object NodeExecutionState extends Enumeration with Logging {

  type NodeExecutionState = Value

  val Inited, WaitForRetry, Scheduled, Running, Succeed, Failed, Cancelled, Skipped ,Timeout = Value

  def isRunning(jobState: NodeExecutionState) = Running == jobState

  def isScheduled(jobState: NodeExecutionState) = Scheduled == jobState

  def isInited(jobState: NodeExecutionState) = Inited == jobState

  def isCompleted(jobState: NodeExecutionState) = jobState match {
    case Inited | Scheduled | Running | WaitForRetry => false
    case _ => true
  }

  def isSucceed(jobState: NodeExecutionState) = Succeed == jobState

 def isFailed(jobState: NodeExecutionState):Boolean =  Failed == jobState

  /**
    * REQ-DSS-1.26.0-FIX-001: Linkis 任务状态名 → NodeExecutionState 安全映射。
    *
    * 三级策略：
    * 1. 枚举快路径：withName 直接命中（Running/Succeed/Failed/Cancelled/Scheduled 等，正常路径零开销零变更）；
    * 2. 别名映射：Linkis 侧与 DSS 枚举拼写不一致的已知状态（见 LINKIS_STATE_ALIASES）；
    * 3. 未知兜底：返回 Running（非终态）继续轮询并 warn，不抛 NoSuchElementException。
    *
    * 注意：null/空串同样兜底 Running——entrance 极端场景下可能返回空状态串。
    */
  def fromLinkisStateName(stateName: String): NodeExecutionState = {
    val trimmed = if (stateName == null) "" else stateName.trim
    if (trimmed.isEmpty) {
      return Running
    }
    // 1. 枚举快路径（保留原 withName 语义，正常状态串行为与修复前完全一致）
    values.find(_.toString.equalsIgnoreCase(trimmed)) match {
      case Some(state) => return state
      case _ =>
    }
    // 2. 已知 Linkis 状态别名
    LINKIS_STATE_ALIASES.get(trimmed.toLowerCase) match {
      case Some(state) => state
      case _ =>
        // 3. 未知状态兜底：视为运行中，不中断轮询
        logger.warn(s"Unknown Linkis job state [$trimmed], mapped to Running by default. " +
          s"Please check Linkis version compatibility.")
        Running
    }
  }

  /**
    * Linkis 侧与 DSS 枚举拼写不一致的已知状态别名表（key 全小写）。
    * Linkis 1.x JobStatus 常见值（org.apache.linkis.governance.common.entity.job.JobStatus / 旧 ExecutionNodeStatus）。
    * Linkis 升级新增任务状态时需同步检查并补充映射（见设计文档 8.3 维护约定）。
    */
  private val LINKIS_STATE_ALIASES: scala.collection.mutable.Map[String, NodeExecutionState] = {
    val m = scala.collection.mutable.Map[String, NodeExecutionState]()
    // ---- 提交/排队中间态 → 映射为 Running（DSS 无对应等待枚举，且非终态语义一致）
    m("submitting") = Running
    m("queued") = Running
    m("scheduled") = Scheduled        // Linkis "Sched" 类中间态
    m("sched") = Scheduled
    m("wait_for_retry") = WaitForRetry
    m("retry") = WaitForRetry
    m("init") = Inited
    m("idle") = Running
    // ---- 成功终态
    m("success") = Succeed
    m("succeeded") = Succeed
    m("succeed") = Succeed            // 冗余保护（大小写无关命中）
    // ---- 失败终态
    m("failed") = Failed
    m("failure") = Failed
    m("error") = Failed
    m("failed_for_resultsize") = Failed   // Linkis: 结果集超限失败
    // ---- 取消终态
    m("cancelled") = Cancelled
    m("canceled") = Cancelled
    m("cancel") = Cancelled
    m("killed") = Cancelled
    m("kill") = Cancelled
    m("shutdown") = Cancelled
    m("stopped") = Cancelled
    // ---- 超时终态
    m("timeout") = Timeout
    m("time_out") = Timeout
    m("scheduled_timeout") = Timeout     // Linkis: 排队超时（Sched+Timeout 组合态）
    m("queue_full_timeout") = Timeout    // Linkis: 队列满超时
    m
  }
}

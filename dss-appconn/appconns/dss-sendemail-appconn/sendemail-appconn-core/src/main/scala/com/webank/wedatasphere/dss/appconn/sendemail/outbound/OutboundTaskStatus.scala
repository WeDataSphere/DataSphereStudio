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

package com.webank.wedatasphere.dss.appconn.sendemail.outbound

/**
 * DataGo outbound task status enumeration (see 接口文档 2.4).
 *
 * Non-terminal: pending / detecting / detected_pass
 * Terminal success: exported
 * Terminal failure: detected_fail / detect_error / export_failed
 */
object OutboundTaskStatus {

  // non-terminal
  val PENDING = "pending"
  val DETECTING = "detecting"
  val DETECTED_PASS = "detected_pass"

  // terminal
  val EXPORTED = "exported"
  val DETECTED_FAIL = "detected_fail"
  val DETECT_ERROR = "detect_error"
  val EXPORT_FAILED = "export_failed"

  private val TERMINAL = Set(EXPORTED, DETECTED_FAIL, DETECT_ERROR, EXPORT_FAILED)
  private val FAILED_TERMINAL = Set(DETECTED_FAIL, DETECT_ERROR, EXPORT_FAILED)

  def normalize(status: String): String = if (status == null) "" else status.trim.toLowerCase

  def isTerminal(status: String): Boolean = TERMINAL.contains(normalize(status))

  def isSuccess(status: String): Boolean = EXPORTED == normalize(status)

  def isFailedTerminal(status: String): Boolean = FAILED_TERMINAL.contains(normalize(status))

}

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

import com.webank.wedatasphere.dss.appconn.sendemail.conf.SendEmailAppConnConfiguration
import org.apache.linkis.common.utils.Logging

/**
 * DataGo outbound configuration reader/validator.
 *
 * Reads datago.outbound.* config items from SendEmailAppConnConfiguration and validates them
 * before entering the outbound flow. Authentication uses a page-login session-token (config) plus
 * a dss_user_name cookie whose value is the workflow executeUser (fallback submitUser), passed in
 * from the runtime map by the caller (loginUser = HDFS upload owner on the DataGo side). The old
 * DSS fixed token + IP whitelist is removed.
 */
object DataGoOutboundConfig extends Logging {

  def getApiBaseUrl: String = SendEmailAppConnConfiguration.DATAGO_OUTBOUND_API_BASE_URL.getValue

  def getSessionToken: String = SendEmailAppConnConfiguration.DATAGO_OUTBOUND_SESSION_TOKEN.getValue

  def getSource: String = SendEmailAppConnConfiguration.DATAGO_OUTBOUND_SOURCE.getValue

  def getChannel: String = SendEmailAppConnConfiguration.DATAGO_OUTBOUND_CHANNEL.getValue

  def getSendPath: String = SendEmailAppConnConfiguration.DATAGO_OUTBOUND_SEND_PATH.getValue

  def getTaskPath: String = SendEmailAppConnConfiguration.DATAGO_OUTBOUND_TASK_PATH.getValue

  def getPollInterval: Int = SendEmailAppConnConfiguration.DATAGO_OUTBOUND_POLL_INTERVAL.getValue

  def getMaxWait: Int = SendEmailAppConnConfiguration.DATAGO_OUTBOUND_MAX_WAIT.getValue

  def getRetryMax: Int = SendEmailAppConnConfiguration.DATAGO_OUTBOUND_RETRY_MAX.getValue

  def getRetryInterval: Int = SendEmailAppConnConfiguration.DATAGO_OUTBOUND_RETRY_INTERVAL.getValue

  def getImageMaxSize: Int = SendEmailAppConnConfiguration.DATAGO_OUTBOUND_IMAGE_MAXSIZE.getValue

  def getImageBatchMaxCount: Int = SendEmailAppConnConfiguration.DATAGO_OUTBOUND_IMAGE_BATCH_MAXCOUNT.getValue

  def getConnectTimeout: Int = SendEmailAppConnConfiguration.DATAGO_OUTBOUND_HTTP_CONNECT_TIMEOUT.getValue

  def getReadTimeout: Int = SendEmailAppConnConfiguration.DATAGO_OUTBOUND_HTTP_READ_TIMEOUT.getValue

  def validate(): Unit = {
    if (getApiBaseUrl.isEmpty) {
      throw new IllegalArgumentException("DataGo outbound api.base.url is not configured. " +
        "Please set wds.dss.appconn.datago.outbound.api.base.url in appconn.properties.")
    }
    if (getSessionToken.isEmpty) {
      throw new IllegalArgumentException("DataGo outbound session.token is not configured. " +
        "Please set wds.dss.appconn.datago.outbound.session.token in appconn.properties.")
    }
    if (getSource.isEmpty) {
      throw new IllegalArgumentException("DataGo outbound source is not configured. " +
        "Please set wds.dss.appconn.datago.outbound.source in appconn.properties.")
    }
    if (getSendPath.isEmpty) {
      throw new IllegalArgumentException("DataGo outbound send.path is not configured. " +
        "Please set wds.dss.appconn.datago.outbound.send.path in appconn.properties.")
    }
    if (getTaskPath.isEmpty) {
      throw new IllegalArgumentException("DataGo outbound task.path is not configured. " +
        "Please set wds.dss.appconn.datago.outbound.task.path in appconn.properties.")
    }
    requirePositive(getPollInterval, "poll.interval")
    requirePositive(getMaxWait, "max.wait")
    requirePositive(getRetryMax, "retry.max")
    requirePositive(getRetryInterval, "retry.interval")
    logger.info(s"DataGo outbound config validated. API Base URL: ${getApiBaseUrl}, source: ${getSource}, channel: ${getChannel}")
  }

  private def requirePositive(value: Int, name: String): Unit = {
    if (value <= 0) {
      throw new IllegalArgumentException(s"DataGo outbound ${name} must be positive. " +
        s"Please set wds.dss.appconn.datago.outbound.${name} to a positive integer in appconn.properties.")
    }
  }

}

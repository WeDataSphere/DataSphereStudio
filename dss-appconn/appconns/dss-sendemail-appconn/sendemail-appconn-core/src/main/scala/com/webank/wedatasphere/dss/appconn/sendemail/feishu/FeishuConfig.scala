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

package com.webank.wedatasphere.dss.appconn.sendemail.feishu

import com.webank.wedatasphere.dss.appconn.sendemail.conf.SendEmailAppConnConfiguration
import org.apache.linkis.common.utils.Logging

/**
 * Feishu configuration data class.
 * Reads feishu-related configuration from SendEmailAppConnConfiguration.
 */
object FeishuConfig extends Logging {

  def getAppId: String = SendEmailAppConnConfiguration.FEISHU_APP_ID.getValue

  def getAppSecret: String = SendEmailAppConnConfiguration.FEISHU_APP_SECRET.getValue

  def getApiBaseUrl: String = SendEmailAppConnConfiguration.FEISHU_API_BASE_URL.getValue

  def validate(): Unit = {
    if (getAppId.isEmpty) {
      throw new IllegalArgumentException("Feishu app.id is not configured. " +
        "Please set wds.dss.appconn.feishu.app.id in appconn.properties.")
    }
    if (getAppSecret.isEmpty) {
      throw new IllegalArgumentException("Feishu app.secret is not configured. " +
        "Please set wds.dss.appconn.feishu.app.secret in appconn.properties.")
    }
    logger.info(s"Feishu integration config validated. App ID: ${getAppId}, API Base URL: ${getApiBaseUrl}")
  }

}

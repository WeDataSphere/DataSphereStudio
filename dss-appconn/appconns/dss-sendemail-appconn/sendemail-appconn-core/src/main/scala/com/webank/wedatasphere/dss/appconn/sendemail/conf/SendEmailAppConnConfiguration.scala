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

package com.webank.wedatasphere.dss.appconn.sendemail.conf

import org.apache.linkis.common.conf.CommonVars

object SendEmailAppConnConfiguration {

  val EMAIL_SENDER_CLASS = CommonVars("wds.dss.appconn.email.sender.class",
    "com.webank.wedatasphere.dss.appconn.sendemail.email.sender.EsbEmailSender")

  val EMAIL_HOOK_CLASSES = CommonVars("wds.dss.appconn.email.hook.classes",
    "com.webank.wedatasphere.dss.appconn.sendemail.hook.SendEmailItsmCheckHook," +
      "com.webank.wedatasphere.dss.appconn.sendemail.hook.SendEmailCheckHook," +
      "com.webank.wedatasphere.dss.appconn.sendemail.hook.SendEmailSendReportHook," +
      "com.webank.wedatasphere.dss.appconn.sendemail.hook.SendEmailVisualisContentLimitHook," +
      "com.webank.wedatasphere.dss.appconn.sendemail.hook.SendEmailTableauCheckHook," +
      "com.webank.wedatasphere.dss.appconn.sendemail.hook.SendEmailMetaBaseCheckHook," +
      "com.webank.wedatasphere.dss.appconn.sendemail.hook.SendEmailMlssv2CheckHook")

  val EMAIL_IMAGE_MAXSIZE = CommonVars("wds.dss.appconn.email.image.maxsize", 5000*30000)
  val CHECK_EMAIL_IMAGE_SWITCH = CommonVars("wds.dss.appconn.email.image.check", true)
  val EMAIL_IMAGE_HEIGHT = CommonVars("wds.dss.appconn.email.image.height", 500)
  val EMAIL_IMAGE_WIDTH = CommonVars("wds.dss.appconn.email.image.width", 1920)
  val DEFAULT_EMAIL_FROM = CommonVars("wds.dss.appconn.email.from.default", "")
  val DEFAULT_EMAIL_SUFFIX = CommonVars("wds.dss.appconn.email.suffix.default", "@webank.com")

  val EMAIL_HOST = CommonVars("wds.dss.appconn.email.host", "")
  val EMAIL_PORT: CommonVars[Integer] = CommonVars[Integer]("wds.dss.appconn.email.port", -1)
  val EMAIL_PROTOCOL = CommonVars("wds.dss.appconn.email.protocol", "smtp")
  val EMAIL_USERNAME = CommonVars("wds.dss.appconn.email.username", "")
  val EMAIL_PASSWORD = CommonVars("wds.dss.appconn.email.password", "")

  val EMAIL_SMTP_AUTH = CommonVars("wds.dss.appconn.email.smtp.auth", "true")
  val EMAIL_SMTP_STARTTLS_ENABLE = CommonVars("wds.dss.appconn.email.smtp.starttls.enable", "true")
  val EMAIL_SMTP_STARTTLS_REQUIRED = CommonVars("wds.dss.appconn.email.smtp.starttls.required", "true")
  val EMAIL_SMTP_SSL_ENABLED = CommonVars("wds.dss.appconn.email.smtp.ssl.enable", "true")
  val EMAIL_SMTP_TIMEOUT: CommonVars[Integer] = CommonVars("wds.dss.appconn.email.smtp.timeout", 25000)

  val EMAIL_ENTITY_CLASSES = CommonVars("wds.dss.appconn.email.hook.entity.classes",
    "com.webank.wedatasphere.dss.appconn.sendemail.hook.entity.newvisualis.NewVisualisEmailInfo," +
      "com.webank.wedatasphere.dss.appconn.sendemail.hook.entity.visualis.VisualisEmailInfo," +
      "com.webank.wedatasphere.dss.appconn.sendemail.hook.entity.visualis.MetaBaseEmailInfo," +
      "com.webank.wedatasphere.dss.appconn.sendemail.hook.entity.mlssv2.Mlssv2EmailInfo")

  // DataGo image outbound configuration (replaces the former fass-core direct Feishu integration).
  // Authentication uses a page-login session-token (config) + a dss_user_name cookie whose value is
  // the workflow executeUser (fallback submitUser) from the runtime map, not a config item; whether
  // to send is still controlled by the sendemail node parameter sendFeishu.
  val DATAGO_OUTBOUND_API_BASE_URL = CommonVars("wds.dss.appconn.datago.outbound.api.base.url", "")
  val DATAGO_OUTBOUND_SESSION_TOKEN = CommonVars("wds.dss.appconn.datago.outbound.session.token", "")
  val DATAGO_OUTBOUND_SOURCE = CommonVars("wds.dss.appconn.datago.outbound.source", "dss")
  val DATAGO_OUTBOUND_CHANNEL = CommonVars("wds.dss.appconn.datago.outbound.channel", "feishu")
  val DATAGO_OUTBOUND_SEND_PATH = CommonVars("wds.dss.appconn.datago.outbound.send.path", "/api/outbound/send")
  val DATAGO_OUTBOUND_TASK_PATH = CommonVars("wds.dss.appconn.datago.outbound.task.path", "/api/outbound/task")
  val DATAGO_OUTBOUND_POLL_INTERVAL: CommonVars[Integer] = CommonVars[Integer]("wds.dss.appconn.datago.outbound.poll.interval", 10)
  val DATAGO_OUTBOUND_MAX_WAIT: CommonVars[Integer] = CommonVars[Integer]("wds.dss.appconn.datago.outbound.max.wait", 120)
  val DATAGO_OUTBOUND_RETRY_MAX: CommonVars[Integer] = CommonVars[Integer]("wds.dss.appconn.datago.outbound.retry.max", 3)
  val DATAGO_OUTBOUND_RETRY_INTERVAL: CommonVars[Integer] = CommonVars[Integer]("wds.dss.appconn.datago.outbound.retry.interval", 30)
  val DATAGO_OUTBOUND_IMAGE_MAXSIZE: CommonVars[Integer] = CommonVars[Integer]("wds.dss.appconn.datago.outbound.image.maxsize", 10485760)
  val DATAGO_OUTBOUND_HTTP_CONNECT_TIMEOUT: CommonVars[Integer] = CommonVars[Integer]("wds.dss.appconn.datago.outbound.http.connect.timeout", 10000)
  val DATAGO_OUTBOUND_HTTP_READ_TIMEOUT: CommonVars[Integer] = CommonVars[Integer]("wds.dss.appconn.datago.outbound.http.read.timeout", 60000)
}

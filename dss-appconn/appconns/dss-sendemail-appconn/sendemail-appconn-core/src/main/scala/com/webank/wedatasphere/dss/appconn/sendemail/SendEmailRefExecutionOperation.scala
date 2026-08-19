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

package com.webank.wedatasphere.dss.appconn.sendemail

import java.util

import com.webank.wedatasphere.dss.appconn.sendemail.conf.SendEmailAppConnInstanceConfiguration
import com.webank.wedatasphere.dss.appconn.sendemail.outbound.DataGoImageSender
import com.webank.wedatasphere.dss.standard.app.development.listener.ref.ExecutionResponseRef.ExecutionResponseRefBuilder
import com.webank.wedatasphere.dss.standard.app.development.listener.ref.{ExecutionResponseRef, RefExecutionRequestRef}
import com.webank.wedatasphere.dss.standard.app.development.operation.{AbstractDevelopmentOperation, RefExecutionOperation}
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef
import org.apache.linkis.common.utils.Utils

import scala.collection.JavaConversions._

class SendEmailRefExecutionOperation
  extends AbstractDevelopmentOperation[RefExecutionRequestRef.RefExecutionRequestRefImpl, ResponseRef]
    with RefExecutionOperation[RefExecutionRequestRef.RefExecutionRequestRefImpl] {

  private val sendEmailAppConnHooks = SendEmailAppConnInstanceConfiguration.getSendEmailRefExecutionHooks
  private val emailContentParsers = SendEmailAppConnInstanceConfiguration.getEmailContentParsers
  private val emailContentGenerators = SendEmailAppConnInstanceConfiguration.getEmailContentGenerators
  private val emailGenerator = SendEmailAppConnInstanceConfiguration.getEmailGenerator
  private val emailSender = SendEmailAppConnInstanceConfiguration.getEmailSender


  override def init(): Unit = {
    super.init()
    val properties = new util.HashMap[String, String]
    service.getAppInstance.getConfig.foreach {
      case (key: String, value: Object) if value != null =>
        properties.put(key, value.toString)
      case _ =>
    }
    emailSender.init(properties)
  }

  override def execute(requestRef: RefExecutionRequestRef.RefExecutionRequestRefImpl): ExecutionResponseRef = {
    val email = Utils.tryCatch {
      sendEmailAppConnHooks.foreach(_.preGenerate(requestRef))
      val email = emailGenerator.generateEmail(requestRef)
      emailContentParsers.foreach(_.parse(email))
      emailContentGenerators.foreach{
        g => Utils.tryQuietly(g.generate(email))
      }
      sendEmailAppConnHooks.foreach(_.preSend(requestRef, email))
      email
    }{ t =>
      return putErrorMsg(t.getMessage, t)
    }
    Utils.tryCatch {
      // Step1: send email
      emailSender.send(email)
    } { t =>
      return putErrorMsg("发送邮件失败！", t)
    }

    // Step 2: Send to Feishu (optional, controlled by node runtime parameter and feishuTo field)
    val runtimeMap = requestRef.getExecutionRequestRefContext.getRuntimeMap
    val sendFeishu = Option(runtimeMap.get("sendFeishu")).exists(_.toString.equalsIgnoreCase("true"))
    if (sendFeishu && email.getFeishuTo != null && email.getFeishuTo.trim.nonEmpty) {
      // dss_user_name = workflow executeUser, fallback submitUser (loginUser = HDFS upload owner on DataGo)
      val executeUser = Option(runtimeMap.get("executeUser")).map(_.toString).filter(_.nonEmpty).getOrElse("")
      val submitUser = Option(runtimeMap.get("submitUser")).map(_.toString).filter(_.nonEmpty).getOrElse("")
      val loginUser = if (executeUser.nonEmpty) executeUser else submitUser
      logger.info(s"Feishu sending is selected and feishuTo is configured: ${email.getFeishuTo}, loginUser: ${loginUser}")
      Utils.tryCatch {
        appendExecutionLog(requestRef, "start Feishu sending")
        DataGoImageSender.send(email, loginUser)
        logger.info("Feishu sending completed successfully.")
        appendExecutionLog(requestRef, "Feishu sending completed successfully.")
      } { t =>
        appendExecutionLog(requestRef, s"Feishu sending completed failed. error reason: ${t.getMessage}")
        return putErrorMsg(s"飞书发送失败！原因：${t.getMessage}", t)
      }
    } else if (sendFeishu) {
      logger.warn("Feishu sending is selected but feishuTo is empty, skip Feishu sending.")
    }

    new ExecutionResponseRefBuilder().success()
  }

  protected def putErrorMsg(errorMsg: String, t: Throwable): ExecutionResponseRef = {
    logger.error(s"failed to send email, $errorMsg ", t)
    new ExecutionResponseRefBuilder().setException(t).setErrorMsg(errorMsg).error()
  }

  /** Append a line to the workflow node's execution log (visible in the DSS UI), mirroring dss-datago-feishu-appconn. */
  private def appendExecutionLog(requestRef: RefExecutionRequestRef.RefExecutionRequestRefImpl, message: String): Unit = {
    try {
      val ctx = requestRef.getExecutionRequestRefContext
      if (ctx != null) {
        val ts = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ctx.appendLog(s"$ts $message")
      }
    } catch {
      case e: Exception => logger.warn("appendExecutionLog failed: " + e.getMessage, e)
    }
  }

}

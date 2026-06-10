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

import com.webank.wedatasphere.dss.appconn.sendemail.email.Email
import com.webank.wedatasphere.dss.appconn.sendemail.email.domain.{Attachment, PngAttachment}
import com.webank.wedatasphere.dss.appconn.sendemail.exception.EmailSendFailedException
import org.apache.linkis.common.utils.Logging

/**
 * Feishu message sender.
 * Uploads image attachments, then sends one template message to each Feishu receiver.
 */
object FeishuMessageSender extends Logging {

  /**
   * Send email attachments and subject to Feishu receivers.
   *
   * @param email the email object containing subject, attachments, and feishuTo
   */
  def send(email: Email): Unit = {
    val feishuTo = email.getFeishuTo
    if (feishuTo == null || feishuTo.trim.isEmpty) {
      logger.warn("feishuTo is empty, skip Feishu sending.")
      return
    }

    FeishuConfig.validate()

    // Parse receivers (semicolon separated)
    val receivers = feishuTo.split(";").map(_.trim).filter(_.nonEmpty)
    if (receivers.isEmpty) {
      logger.warn("No valid Feishu receivers found, skip Feishu sending.")
      return
    }

    val subject = if (email.getSubject != null) email.getSubject else "DSS Email Notification"

    logger.info(s"Start sending to Feishu. Receivers: ${receivers.mkString(",")}, Subject: ${subject}")

    val imageKeys = uploadImageAttachments(email.getAttachments)
    sendMessages(receivers, subject, imageKeys)

    logger.info(s"Feishu sending completed. ${receivers.length} receivers, ${imageKeys.length} image attachments.")
  }

  private def sendMessages(receivers: Array[String], subject: String, imageKeys: Array[String]): Unit = {
    val templateCode = FeishuConfig.getTemplateCode
    FeishuConfig.requireTemplateCode(templateCode, "wds.dss.appconn.feishu.template.code")

    val paramsJson = buildParamsJson(subject, imageKeys)
    receivers.foreach { receiver =>
      try {
        FeishuClient.sendTemplateMessage(receiver, templateCode, paramsJson)
      } catch {
        case e: Exception =>
          logger.error(s"Failed to send Feishu message to receiver ${receiver}", e)
          throw new EmailSendFailedException(80006, s"Failed to send Feishu message to receiver ${receiver}: ${e.getMessage}")
      }
    }
  }

  private def uploadImageAttachments(attachments: Array[Attachment]): Array[String] = {
    if (attachments == null || attachments.isEmpty) {
      return Array.empty[String]
    }

    attachments.filter(isImageAttachment).map { attachment =>
      try {
        uploadAttachment(attachment)
      } catch {
        case e: Exception =>
          logger.error(s"Failed to upload image attachment ${attachment.getName} to Feishu", e)
          throw new EmailSendFailedException(80007, s"Failed to upload Feishu image attachment ${attachment.getName}: ${e.getMessage}")
      }
    }
  }

  /**
   * Upload an attachment to Feishu.
   * Uses the Attachment's File if available, otherwise writes base64 content to a temp file.
   */
  private def uploadAttachment(attachment: Attachment): String = {
    val fileName = attachment.getName

    val file = attachment.getFile
    if (file != null && file.exists()) {
      return FeishuClient.uploadFile(file, fileName, "message")
    }

    logger.info(s"Attachment ${fileName} has no File reference, writing base64 to temp file for upload.")
    val tempFile = java.io.File.createTempFile("feishu_upload_", s"_${fileName}")
    try {
      import java.util.Base64
      val bytes = Base64.getDecoder.decode(attachment.getBase64Str)
      java.nio.file.Files.write(tempFile.toPath, bytes)
      FeishuClient.uploadFile(tempFile, fileName, "message")
    } finally {
      if (tempFile.exists()) {
        tempFile.delete()
      }
    }
  }

  private def isImageAttachment(attachment: Attachment): Boolean = {
    attachment.isInstanceOf[PngAttachment] ||
      Option(attachment.getMediaType).exists(_.toLowerCase.startsWith("image/")) ||
      Option(attachment.getName).exists(_.toLowerCase.endsWith(".png"))
  }

  private def buildParamsJson(subject: String, imageKeys: Array[String]): String = {
    val content = FeishuClient.escapeJson(s"[DSS Email Notification] ${subject}")
    val imageKeyItems = imageKeys.map(key => s"""{"img_key":"${FeishuClient.escapeJson(key)}"}""").mkString("[", ",", "]")
    val firstImageKey = imageKeys.headOption.map { key =>
      s""","imgKey":{"img_key":"${FeishuClient.escapeJson(key)}"}"""
    }.getOrElse("")
    s"""{"content":"${content}","subject":"${FeishuClient.escapeJson(subject)}","imgKeys":${imageKeyItems}${firstImageKey}}"""
  }

}

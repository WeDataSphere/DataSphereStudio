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
import com.webank.wedatasphere.dss.appconn.sendemail.email.domain.Attachment
import com.webank.wedatasphere.dss.appconn.sendemail.exception.EmailSendFailedException
import org.apache.linkis.common.utils.Logging

/**
 * Feishu message sender.
 * Sends email subject as text message and attachments as file messages
 * to the specified Feishu users.
 */
object FeishuMessageSender extends Logging {

  /**
   * Send email attachments and subject to Feishu users.
   *
   * @param email the email object containing subject, attachments, and feishuTo
   */
  def send(email: Email): Unit = {
    val feishuTo = email.getFeishuTo
    if (feishuTo == null || feishuTo.trim.isEmpty) {
      logger.warn("feishuTo is empty, skip Feishu sending.")
      return
    }

    // Validate Feishu configuration
    FeishuConfig.validate()

    // Parse receiver IDs (comma separated)
    val receivers = feishuTo.split(",").map(_.trim).filter(_.nonEmpty)
    if (receivers.isEmpty) {
      logger.warn("No valid Feishu receiver IDs found, skip Feishu sending.")
      return
    }

    val subject = if (email.getSubject != null) email.getSubject else "DSS Email Notification"

    logger.info(s"Start sending to Feishu. Receivers: ${receivers.mkString(",")}, Subject: ${subject}")

    // Send subject as text message to all receivers
    for (receiver <- receivers) {
      try {
        FeishuClient.sendTextMessage(receiver, "open_id", s"[DSS邮件通知] ${subject}")
      } catch {
        case e: Exception =>
          logger.error(s"Failed to send subject text message to Feishu user ${receiver}", e)
          throw new EmailSendFailedException(80006, s"飞书发送失败: 向用户 ${receiver} 发送主题消息失败 - ${e.getMessage}")
      }
    }

    // Send each attachment as file message to all receivers
    val attachments = email.getAttachments
    if (attachments != null && attachments.nonEmpty) {
      for (attachment <- attachments) {
        val fileKey = try {
          uploadAttachment(attachment)
        } catch {
          case e: Exception =>
            logger.error(s"Failed to upload attachment ${attachment.getName} to Feishu", e)
            throw new EmailSendFailedException(80007, s"飞书发送失败: 上传附件 ${attachment.getName} 失败 - ${e.getMessage}")
        }

        for (receiver <- receivers) {
          try {
            FeishuClient.sendFileMessage(receiver, "open_id", fileKey)
          } catch {
            case e: Exception =>
              logger.error(s"Failed to send file message to Feishu user ${receiver}, file: ${attachment.getName}", e)
              throw new EmailSendFailedException(80008, s"飞书发送失败: 向用户 ${receiver} 发送附件 ${attachment.getName} 失败 - ${e.getMessage}")
          }
        }
      }
    }

    logger.info(s"Feishu sending completed. ${receivers.length} receivers, ${if (attachments != null) attachments.length else 0} attachments.")
  }

  /**
   * Upload an attachment to Feishu.
   * Uses the Attachment's File if available, otherwise writes base64 content to a temp file.
   */
  private def uploadAttachment(attachment: Attachment): String = {
    val fileName = attachment.getName

    // Prefer using File directly if available
    val file = attachment.getFile
    if (file != null && file.exists()) {
      return FeishuClient.uploadFile(file, fileName)
    }

    // Fallback: write base64 content to a temp file and upload
    logger.info(s"Attachment ${fileName} has no File reference, writing base64 to temp file for upload.")
    val tempFile = java.io.File.createTempFile("feishu_upload_", s"_${fileName}")
    try {
      import java.util.Base64
      val bytes = Base64.getDecoder.decode(attachment.getBase64Str)
      java.nio.file.Files.write(tempFile.toPath, bytes)
      FeishuClient.uploadFile(tempFile, fileName)
    } finally {
      if (tempFile.exists()) {
        tempFile.delete()
      }
    }
  }

}

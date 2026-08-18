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

import java.io.File
import java.nio.file.Files
import java.util.Base64
import com.webank.wedatasphere.dss.appconn.sendemail.email.Email
import com.webank.wedatasphere.dss.appconn.sendemail.email.domain.{Attachment, PngAttachment}
import com.webank.wedatasphere.dss.appconn.sendemail.exception.EmailSendFailedException
import org.apache.linkis.common.utils.Logging

import scala.collection.mutable.ArrayBuffer

/**
 * Orchestrates the DataGo image outbound flow for a sendemail node.
 *
 *   1. validate feishuTo + config
 *   2. text = subject (fallback "DSS Email Notification")
 *   3. recipients = feishuTo split/trim -> JSON array (DataGo filters v_ / system users)
 *   4. prepare image attachments (File or base64 temp file, each <= image.maxsize)
 *   5. ① submitImage -> taskId
 *   6. ② poll until terminal (exported = success; failed terminal = 81004; timeout = 81005)
 *
 * loginUser (the workflow executeUser, fallback submitUser, read from the runtime map by the
 * caller) is threaded into ①/② as the dss_user_name cookie value = HDFS upload owner on DataGo.
 */
object DataGoImageSender extends Logging {

  def send(email: Email, loginUser: String): Unit = {
    val feishuTo = email.getFeishuTo
    if (feishuTo == null || feishuTo.trim.isEmpty) {
      logger.warn("feishuTo is empty, skip DataGo image outbound.")
      return
    }

    DataGoOutboundConfig.validate()

    val receivers = feishuTo.split(";").map(_.trim).filter(_.nonEmpty)
    if (receivers.isEmpty) {
      logger.warn("No valid Feishu receivers found, skip DataGo image outbound.")
      return
    }

    val text = resolveText(email.getSubject)
    val recipientsJson = toJsonArray(receivers)
    val title = text

    val tempFiles = ArrayBuffer[File]()
    val imageFiles = prepareImageFiles(email.getAttachments, tempFiles)
    try {
      val batchMaxCount = DataGoOutboundConfig.getImageBatchMaxCount
      val batches: Array[Array[File]] =
        if (imageFiles.isEmpty) Array(Array.empty[File])
        else if (batchMaxCount <= 0) Array(imageFiles)
        else imageFiles.grouped(batchMaxCount).toArray
      logger.info(s"DataGo outbound: ${imageFiles.length} image(s) split into ${batches.length} batch(es) " +
        s"(batchMaxCount=${batchMaxCount}, perBatchMaxWait=${DataGoOutboundConfig.getMaxWait}s, loginUser=${Option(loginUser).getOrElse("")}).")
      var batchIndex = 0
      batches.foreach { batch =>
        batchIndex += 1
        val taskId = DataGoOutboundClient.submitImage(text, recipientsJson, title, batch, loginUser)
        logger.info(s"DataGo outbound batch ${batchIndex}/${batches.length} accepted, taskId=${taskId}, batchImages=${batch.length}. Start polling until terminal.")
        pollUntilTerminal(taskId, loginUser)
      }
      logger.info(s"DataGo outbound completed successfully, ${batches.length} batch(es), receivers=${receivers.mkString(",")}.")
    } finally {
      tempFiles.foreach(cleanupTempFile)
    }
  }

  private def resolveText(subject: String): String =
    if (subject != null && subject.trim.nonEmpty) subject else "DSS Email Notification"

  private def toJsonArray(receivers: Array[String]): String =
    receivers.map(r => "\"" + DataGoOutboundClient.escapeJson(r) + "\"").mkString("[", ",", "]")

  // ---------------------------------------------------------------- image attachments

  private def prepareImageFiles(attachments: Array[Attachment], tempFiles: ArrayBuffer[File]): Array[File] = {
    if (attachments == null || attachments.isEmpty) {
      return Array.empty[File]
    }
    attachments.filter(isImageAttachment).map(prepareImageFile(_, tempFiles))
  }

  private def isImageAttachment(attachment: Attachment): Boolean = {
    attachment.isInstanceOf[PngAttachment] ||
      Option(attachment.getMediaType).exists(_.toLowerCase.startsWith("image/")) ||
      Option(attachment.getName).exists(_.toLowerCase.endsWith(".png"))
  }

  private def prepareImageFile(attachment: Attachment, tempFiles: ArrayBuffer[File]): File = {
    val fileName = attachment.getName

    val file = attachment.getFile
    if (file != null && file.exists()) {
      checkFileSize(file.length(), fileName)
      return file
    }

    val base64 = attachment.getBase64Str
    if (base64 == null || base64.isEmpty) {
      throw new EmailSendFailedException(81006,
        s"Image attachment ${fileName} has neither File nor base64 content")
    }
    val bytes = decodeBase64Image(base64, fileName)
    checkFileSize(bytes.length, fileName)

    val tempFile = try {
      File.createTempFile("datago_image_", imageSuffix(fileName))
    } catch {
      case e: Exception =>
        throw new EmailSendFailedException(81006,
          s"Image attachment ${fileName} failed to create temp file: ${e.getMessage}")
    }
    try {
      Files.write(tempFile.toPath, bytes)
    } catch {
      case e: Exception =>
        if (tempFile.exists()) tempFile.delete()
        throw new EmailSendFailedException(81006,
          s"Image attachment ${fileName} failed to write temp file: ${e.getMessage}")
    }
    tempFiles += tempFile
    tempFile
  }

  private def checkFileSize(size: Long, fileName: String): Unit = {
    val max = DataGoOutboundConfig.getImageMaxSize
    if (size > max) {
      throw new EmailSendFailedException(81006,
        s"Image attachment ${fileName} size ${size} bytes exceeds the ${max} byte limit")
    }
  }

  /**
   * Decode an image attachment's base64 content robustly:
   *   - strip a `data:<mime>;base64,` data-URI prefix (HTML inline images carry a data URI);
   *   - strip whitespace / line breaks (commons-codec may chunk output every 76 chars);
   *   - decode with the standard decoder, falling back to the MIME decoder;
   *   - on failure surface a content preview so the actual format is diagnosable.
   */
  private def decodeBase64Image(raw: String, fileName: String): Array[Byte] = {
    var s = if (raw == null) "" else raw.trim
    if (s.startsWith("data:")) {
      val marker = ";base64,"
      val idx = s.indexOf(marker)
      if (idx >= 0) s = s.substring(idx + marker.length)
      else if (s.indexOf(',') >= 0) s = s.substring(s.indexOf(',') + 1)
    }
    val cleaned = s.filterNot(ch => ch == ' ' || ch == '\r' || ch == '\n' || ch == '\t')
    if (cleaned.isEmpty) {
      throw new EmailSendFailedException(81006,
        s"Image attachment ${fileName} base64 content is empty after cleaning")
    }
    try {
      Base64.getDecoder.decode(cleaned)
    } catch {
      case _: IllegalArgumentException =>
        try {
          Base64.getMimeDecoder.decode(cleaned)
        } catch {
          case e2: IllegalArgumentException =>
            throw new EmailSendFailedException(81006,
              s"Image attachment ${fileName} failed to decode base64: ${e2.getMessage}, contentPrefix=${cleaned.take(48)}")
        }
    }
  }

  /** Temp-file suffix restricted to a DataGo-accepted image extension (so the upload filename passes DataGo's check). */
  private def imageSuffix(fileName: String): String = {
    val dot = fileName.lastIndexOf('.')
    if (dot < 0 || dot == fileName.length - 1) return ".png"
    val ext = fileName.substring(dot).toLowerCase
    if (ext.matches("^\\.(png|jpg|jpeg|gif|webp|bmp)$")) ext else ".png"
  }

  private def cleanupTempFile(file: File): Unit = {
    try {
      if (file != null && file.exists()) file.delete()
    } catch {
      case e: Exception => logger.warn(s"Failed to delete temp file ${file}", e)
    }
  }

  // ---------------------------------------------------------------- polling

  private def pollUntilTerminal(taskId: Long, loginUser: String): Unit = {
    val deadline = System.currentTimeMillis() + DataGoOutboundConfig.getMaxWait * 1000L
    val interval = DataGoOutboundConfig.getPollInterval * 1000L
    while (true) {
      val result = DataGoOutboundClient.queryTask(taskId, loginUser)
      val status = result.status
      if (OutboundTaskStatus.isTerminal(status)) {
        logger.info(s"DataGo outbound task ${taskId} terminal: status=${status}, resultSummary=${result.resultSummary}")
        if (OutboundTaskStatus.isSuccess(status)) {
          return
        }
        if (OutboundTaskStatus.isFailedTerminal(status)) {
          throw new EmailSendFailedException(81004,
            s"DataGo outbound task ${taskId} ended in failed terminal status: ${status}, resultSummary: ${result.resultSummary} (DataGo has notified receivers)")
        }
        throw new EmailSendFailedException(81004,
          s"DataGo outbound task ${taskId} ended in unknown terminal status: ${status}, resultSummary: ${result.resultSummary}")
      }
      if (System.currentTimeMillis() > deadline) {
        throw new EmailSendFailedException(81005,
          s"DataGo outbound task ${taskId} polling timed out after ${DataGoOutboundConfig.getMaxWait}s")
      }
      Thread.sleep(interval)
    }
  }

}

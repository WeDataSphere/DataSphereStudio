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

import java.io.{BufferedReader, DataOutputStream, InputStreamReader, File}
import java.net.{HttpURLConnection, URL}
import java.security.MessageDigest
import java.nio.file.Files
import scala.util.Random
import org.apache.linkis.common.utils.Logging
import com.webank.wedatasphere.dss.appconn.sendemail.exception.EmailSendFailedException

/**
 * Feishu API client.
 * Handles authenticated file upload and template message sending via the unified Feishu access API.
 */
object FeishuClient extends Logging {

  /**
   * Upload a file to Feishu and return the key used by message templates.
   *
   * @param file the file to upload
   * @param fileName the display name for the file
   * @param fileType the Feishu material type. Use "message" for image message material.
   * @return key returned by Feishu
   */
  def uploadFile(file: File, fileName: String, fileType: String): String = {
    val url = s"${baseUrl}/feishu/external/access/file/upload"

    logger.info(s"Uploading file to Feishu: ${fileName}, fileType: ${fileType}")

    val boundary = "----WebKitFormBoundary" + System.currentTimeMillis()
    val connection = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary)
    addAuthHeaders(connection)

    val CRLF = "\r\n"
    val outputStream = new DataOutputStream(connection.getOutputStream)

    writeMultipartText(outputStream, boundary, "fileType", fileType)

    if (fileName != null && fileName.trim.nonEmpty) {
      writeMultipartText(outputStream, boundary, "fileName", fileName)
    }

    outputStream.writeBytes("--" + boundary + CRLF)
    outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + CRLF)
    outputStream.writeBytes("Content-Type: application/octet-stream" + CRLF + CRLF)

    val fileBytes = Files.readAllBytes(file.toPath)
    outputStream.write(fileBytes)

    outputStream.writeBytes(CRLF + "--" + boundary + "--" + CRLF)
    outputStream.flush()
    outputStream.close()

    val response = readResponse(connection)
    val respCode = getFieldFromJson(response, "respCode")
    if (!isSuccess(respCode)) {
      val respMsg = getFieldFromJson(response, "respMsg")
      throw new EmailSendFailedException(80003, s"Failed to upload file to Feishu: respCode=$respCode, respMsg=$respMsg, fileName=$fileName")
    }

    val fileKey = getFieldFromJson(response, "data")
    logger.info(s"File uploaded to Feishu successfully. key: ${fileKey}")
    fileKey
  }

  /**
   * Send a template message to a Feishu receiver.
   */
  def sendTemplateMessage(receiver: String, templateCode: String, paramsJson: String): Unit = {
    val url = s"${baseUrl}/feishu/external/access/sendMessage"
    val body = s"""{"receiver":"${escapeJson(receiver)}","templateCode":"${escapeJson(templateCode)}","params":${paramsJson},"appId":"${escapeJson(FeishuConfig.getAppId)}"}"""

    logger.info(s"Sending Feishu template message to receiver: ${receiver}, templateCode: ${templateCode}")
    val response = sendPostRequest(url, body, "application/json; charset=utf-8")

    val respCode = getFieldFromJson(response, "respCode")
    if (!isSuccess(respCode)) {
      val respMsg = getFieldFromJson(response, "respMsg")
      throw new EmailSendFailedException(80004, s"Failed to send Feishu message: respCode=$respCode, respMsg=$respMsg, receiver=$receiver")
    }

    logger.info(s"Feishu template message sent to receiver ${receiver} successfully. feishuMsgId: ${getFieldFromJson(response, "feishuMsgId")}")
  }

  /**
   * Send an HTTP POST request.
   */
  private def sendPostRequest(urlStr: String, body: String, contentType: String): String = {
    val connection = new URL(urlStr).openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", contentType)
    addAuthHeaders(connection)

    val outputStream = connection.getOutputStream
    outputStream.write(body.getBytes("UTF-8"))
    outputStream.flush()
    outputStream.close()

    readResponse(connection)
  }

  private def baseUrl: String = FeishuConfig.getApiBaseUrl.stripSuffix("/")

  private def addAuthHeaders(connection: HttpURLConnection): Unit = {
    val appId = FeishuConfig.getAppId
    val nonce = f"${Random.nextInt(100000)}%05d"
    val timestamp = (System.currentTimeMillis() / 1000).toString
    val firstHash = sha256(appId + nonce + timestamp)
    val signature = sha256(firstHash + FeishuConfig.getAppToken)

    connection.setRequestProperty("FS-AppId", appId)
    connection.setRequestProperty("FS-Nonce", nonce)
    connection.setRequestProperty("FS-Timestamp", timestamp)
    connection.setRequestProperty("FS-Signature", signature)
    connection.setRequestProperty("FS-Source", FeishuConfig.getSource)
  }

  private def writeMultipartText(outputStream: DataOutputStream, boundary: String, name: String, value: String): Unit = {
    val CRLF = "\r\n"
    outputStream.writeBytes("--" + boundary + CRLF)
    outputStream.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + CRLF + CRLF)
    outputStream.write(value.getBytes("UTF-8"))
    outputStream.writeBytes(CRLF)
  }

  /**
   * Read HTTP response from connection.
   */
  private def readResponse(connection: HttpURLConnection): String = {
    val responseCode = connection.getResponseCode
    val inputStream = if (responseCode >= 200 && responseCode < 300) {
      connection.getInputStream
    } else {
      connection.getErrorStream
    }

    if (inputStream == null) {
      throw new EmailSendFailedException(80005, s"Feishu API returned HTTP $responseCode with no response body")
    }

    val reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))
    val response = new StringBuilder
    var line: String = null
    while ({ line = reader.readLine(); line != null }) {
      response.append(line)
    }
    reader.close()
    response.toString
  }

  def escapeJson(value: String): String = {
    if (value == null) {
      ""
    } else {
      value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\r", "\\r")
        .replace("\n", "\\n")
        .replace("\t", "\\t")
    }
  }

  private def sha256(value: String): String = {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8"))
    digest.map(byte => "%02X".format(byte & 0xff)).mkString
  }

  private def isSuccess(respCode: String): Boolean = respCode != null && respCode.endsWith("0000")

  /**
   * Extract a field value from a simple JSON string.
   * This is a lightweight alternative to pulling in a full JSON library.
   */
  private def getFieldFromJson(json: String, field: String): String = {
    val patterns = List(
      s""""${field}"\\s*:\\s*"([^"]*)"""".r,
      s""""${field}"\\s*:\\s*(\\d+)""".r
    )
    patterns.foreach { pattern =>
      pattern.findFirstMatchIn(json) match {
        case Some(m) => return m.group(1)
        case None =>
      }
    }
    null
  }

}

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
import java.nio.file.Files
import scala.collection.mutable
import org.apache.linkis.common.utils.Logging
import com.webank.wedatasphere.dss.appconn.sendemail.exception.EmailSendFailedException

/**
 * Feishu API client.
 * Handles Tenant Token management (with caching and auto-refresh),
 * file upload, and message sending via Feishu Open Platform API.
 */
object FeishuClient extends Logging {

  private var tenantToken: String = _
  private var tokenExpireTime: Long = 0L
  private val TOKEN_REFRESH_MARGIN: Long = 300000L // 5 minutes before expiry

  /**
   * Get a valid Tenant Access Token, refreshing if necessary.
   */
  def getTenantAccessToken(): String = synchronized {
    if (tenantToken != null && System.currentTimeMillis() < tokenExpireTime) {
      return tenantToken
    }
    refreshTenantToken()
    tenantToken
  }

  /**
   * Refresh the Tenant Access Token via Feishu API.
   */
  private def refreshTenantToken(): Unit = {
    val appId = FeishuConfig.getAppId
    val appSecret = FeishuConfig.getAppSecret
    val baseUrl = FeishuConfig.getApiBaseUrl

    val url = s"${baseUrl}/auth/v3/tenant_access_token/internal"
    val body = s"""{"app_id":"${appId}","app_secret":"${appSecret}"}"""

    logger.info("Requesting Feishu Tenant Access Token...")
    val response = sendPostRequest(url, body, "application/json; charset=utf-8")
    val code = getFieldFromJson(response, "code")
    if (code != "0") {
      val msg = getFieldFromJson(response, "msg")
      throw new EmailSendFailedException(80002, s"Failed to get Feishu tenant token: code=$code, msg=$msg")
    }

    tenantToken = getFieldFromJson(response, "tenant_access_token")
    val expire = getFieldFromJson(response, "expire").toLong
    tokenExpireTime = System.currentTimeMillis() + expire * 1000 - TOKEN_REFRESH_MARGIN

    logger.info(s"Feishu Tenant Access Token refreshed. Expires in ${expire}s")
  }

  /**
   * Upload a file to Feishu and return the file_key.
   *
   * @param file the file to upload
   * @param fileName the display name for the file
   * @return file_key returned by Feishu
   */
  def uploadFile(file: File, fileName: String): String = {
    val baseUrl = FeishuConfig.getApiBaseUrl
    val token = getTenantAccessToken()
    val url = s"${baseUrl}/im/v1/files"

    logger.info(s"Uploading file to Feishu: ${fileName}")

    val boundary = "----WebKitFormBoundary" + System.currentTimeMillis()
    val connection = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    connection.setRequestProperty("Authorization", "Bearer " + token)
    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary)

    val CRLF = "\r\n"
    val outputStream = new DataOutputStream(connection.getOutputStream)

    // Add file_type field
    outputStream.writeBytes("--" + boundary + CRLF)
    outputStream.writeBytes("Content-Disposition: form-data; name=\"file_type\"" + CRLF + CRLF)
    outputStream.writeBytes("stream" + CRLF)

    // Add file_name field
    outputStream.writeBytes("--" + boundary + CRLF)
    outputStream.writeBytes("Content-Disposition: form-data; name=\"file_name\"" + CRLF + CRLF)
    outputStream.writeBytes(fileName + CRLF)

    // Add file content
    outputStream.writeBytes("--" + boundary + CRLF)
    outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"" + CRLF)
    outputStream.writeBytes("Content-Type: application/octet-stream" + CRLF + CRLF)

    val fileBytes = Files.readAllBytes(file.toPath)
    outputStream.write(fileBytes)

    outputStream.writeBytes(CRLF + "--" + boundary + "--" + CRLF)
    outputStream.flush()
    outputStream.close()

    val response = readResponse(connection)
    val code = getFieldFromJson(response, "code")
    if (code != "0") {
      val msg = getFieldFromJson(response, "msg")
      throw new EmailSendFailedException(80003, s"Failed to upload file to Feishu: code=$code, msg=$msg, fileName=$fileName")
    }

    val fileKey = getFieldFromJson(response, "file_key")
    logger.info(s"File uploaded to Feishu successfully. file_key: ${fileKey}")
    fileKey
  }

  /**
   * Send a file message to a Feishu user.
   *
   * @param receiveId the receiver's open_id or user_id
   * @param receiveIdType the type of receive_id: "open_id", "user_id", or "chat_id"
   * @param fileKey the file_key returned by uploadFile
   */
  def sendFileMessage(receiveId: String, receiveIdType: String, fileKey: String): Unit = {
    val baseUrl = FeishuConfig.getApiBaseUrl
    val token = getTenantAccessToken()
    val url = s"${baseUrl}/im/v1/messages?receive_id_type=${receiveIdType}"

    val body = s"""{"receive_id":"${receiveId}","msg_type":"file","content":"{\\"file_key\\":\\"${fileKey}\\"}"}"""

    logger.info(s"Sending file message to Feishu user: ${receiveId}")
    val response = sendPostRequest(url, body, "application/json; charset=utf-8", Some(s"Bearer ${token}"))

    val code = getFieldFromJson(response, "code")
    if (code != "0") {
      val msg = getFieldFromJson(response, "msg")
      throw new EmailSendFailedException(80004, s"Failed to send Feishu message: code=$code, msg=$msg, receiveId=$receiveId")
    }

    logger.info(s"File message sent to Feishu user ${receiveId} successfully.")
  }

  /**
   * Send a text message to a Feishu user (used for subject notification).
   */
  def sendTextMessage(receiveId: String, receiveIdType: String, text: String): Unit = {
    val baseUrl = FeishuConfig.getApiBaseUrl
    val token = getTenantAccessToken()
    val url = s"${baseUrl}/im/v1/messages?receive_id_type=${receiveIdType}"

    val escapedText = text.replace("\"", "\\\"").replace("\n", "\\n")
    val body = s"""{"receive_id":"${receiveId}","msg_type":"text","content":"{\\"text\\":\\"${escapedText}\\"}"}"""

    logger.info(s"Sending text message to Feishu user: ${receiveId}")
    val response = sendPostRequest(url, body, "application/json; charset=utf-8", Some(s"Bearer ${token}"))

    val code = getFieldFromJson(response, "code")
    if (code != "0") {
      val msg = getFieldFromJson(response, "msg")
      throw new EmailSendFailedException(80004, s"Failed to send Feishu text message: code=$code, msg=$msg, receiveId=$receiveId")
    }

    logger.info(s"Text message sent to Feishu user ${receiveId} successfully.")
  }

  /**
   * Send an HTTP POST request.
   */
  private def sendPostRequest(urlStr: String, body: String, contentType: String, authHeader: Option[String] = None): String = {
    val connection = new URL(urlStr).openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", contentType)
    authHeader.foreach(h => connection.setRequestProperty("Authorization", h))

    val outputStream = connection.getOutputStream
    outputStream.write(body.getBytes("UTF-8"))
    outputStream.flush()
    outputStream.close()

    readResponse(connection)
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

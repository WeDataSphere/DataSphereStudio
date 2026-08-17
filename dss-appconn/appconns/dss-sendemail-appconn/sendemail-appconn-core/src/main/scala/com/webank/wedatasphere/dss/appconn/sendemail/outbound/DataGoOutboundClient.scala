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

import java.io.{BufferedReader, InputStreamReader}
import java.net.{HttpURLConnection, URL}
import java.nio.file.Files
import com.webank.wedatasphere.dss.appconn.sendemail.exception.EmailSendFailedException
import org.apache.linkis.common.utils.Logging

/**
 * DataGo outbound HTTP client.
 *
 * ① submitImage  -> POST /api/outbound/send   (multipart/form-data, type=image) -> taskId
 * ② queryTask    -> POST /api/outbound/task    (application/json)                -> status
 *
 * Every request carries Authorization: Bearer <session-token> + Cookie: dss_user_name=<loginUser>.
 * HTTP 502/504 and network-level unreachable are retried with backoff; other failures surface as
 * EmailSendFailedException (81002 submit / 81003 query / 81007 parse-or-no-body).
 *
 * Uses JDK HttpURLConnection with hand-written multipart and lightweight regex JSON extraction,
 * mirroring the prior FeishuClient (no external JSON/HTTP dependency).
 */
object DataGoOutboundClient extends Logging {

  /** Retryable: HTTP 502/504 or a network-level unreachable condition. */
  private class UpstreamUnreachableException(val httpCode: Int, message: String) extends RuntimeException(message)

  // ---------------------------------------------------------------- ① submitImage

  def submitImage(text: String, recipientsJson: String, title: String, images: Array[java.io.File], loginUser: String): Long = {
    val url = s"${baseUrl}${DataGoOutboundConfig.getSendPath}"
    val boundary = "----DSSDataGoBoundary" + System.currentTimeMillis()
    val body = buildMultipartBody(boundary, text, recipientsJson, title, images)
    val response = sendWithRetry(url, body, multipartContentType(boundary), op = "submit", loginUser = loginUser)

    val success = getFieldFromJson(response, "success")
    if (!"true".equalsIgnoreCase(success)) {
      val code = getFieldFromJson(response, "code")
      throw new EmailSendFailedException(81002,
        s"DataGo submit failed: code=${code}, message=${getFieldFromJson(response, "message")}")
    }
    val taskIdStr = getFieldFromJson(response, "taskId")
    if (taskIdStr == null || taskIdStr.isEmpty) {
      throw new EmailSendFailedException(81007, s"DataGo submit response has no taskId: ${response}")
    }
    try taskIdStr.toLong
    catch {
      case _: NumberFormatException =>
        throw new EmailSendFailedException(81007, s"DataGo submit taskId is not a number: ${taskIdStr}")
    }
  }

  // ---------------------------------------------------------------- ② queryTask

  def queryTask(taskId: Long, loginUser: String): String = {
    val url = s"${baseUrl}${DataGoOutboundConfig.getTaskPath}"
    val body = s"""{"taskId":${taskId},"source":"${escapeJson(DataGoOutboundConfig.getSource)}"}""".getBytes("UTF-8")
    val response = sendWithRetry(url, body, "application/json; charset=utf-8", op = "query", loginUser = loginUser)

    if (response == null || response.trim.isEmpty) {
      throw new EmailSendFailedException(81007, "DataGo query response has no body")
    }
    val success = getFieldFromJson(response, "success")
    if (!"true".equalsIgnoreCase(success)) {
      val code = getFieldFromJson(response, "code")
      throw new EmailSendFailedException(81003,
        s"DataGo query failed: code=${code}, message=${getFieldFromJson(response, "message")}")
    }
    val status = getFieldFromJson(response, "status")
    if (status == null || status.isEmpty) {
      throw new EmailSendFailedException(81007, s"DataGo query response has no status: ${response}")
    }
    status
  }

  // ---------------------------------------------------------------- HTTP transport

  private def baseUrl: String = DataGoOutboundConfig.getApiBaseUrl.stripSuffix("/")

  private def multipartContentType(boundary: String): String = s"multipart/form-data; boundary=${boundary}"

  /**
   * Send with retry. Retries only on UpstreamUnreachableException (502/504 / network unreachable);
   * all other exceptions propagate immediately.
   */
  private def sendWithRetry(url: String, body: Array[Byte], contentType: String, op: String, loginUser: String): String = {
    val retryMax = DataGoOutboundConfig.getRetryMax
    val retryInterval = DataGoOutboundConfig.getRetryInterval
    var attempt = 0
    var lastError: UpstreamUnreachableException = null
    while (attempt <= retryMax) {
      try {
        return sendPost(url, body, contentType, loginUser)
      } catch {
        case e: UpstreamUnreachableException =>
          lastError = e
          if (attempt < retryMax) {
            logger.warn(s"DataGo ${op} attempt ${attempt + 1}/${retryMax + 1} upstream unreachable " +
              s"(HTTP ${e.httpCode}), retry in ${retryInterval}s: ${e.getMessage}")
            Thread.sleep(retryInterval * 1000L)
          }
      }
      attempt += 1
    }
    val code = if (op == "submit") 81002 else 81003
    throw new EmailSendFailedException(code,
      s"DataGo ${op} failed after ${retryMax} retries (upstream unreachable): " +
        s"${Option(lastError).map(_.getMessage).getOrElse("")}")
  }

  private def sendPost(urlStr: String, body: Array[Byte], contentType: String, loginUser: String): String = {
    val connection = new URL(urlStr).openConnection().asInstanceOf[HttpURLConnection]
    try {
      connection.setRequestMethod("POST")
      connection.setDoOutput(true)
      connection.setConnectTimeout(DataGoOutboundConfig.getConnectTimeout)
      connection.setReadTimeout(DataGoOutboundConfig.getReadTimeout)
      connection.setRequestProperty("Content-Type", contentType)
      connection.setRequestProperty("Authorization", "Bearer " + DataGoOutboundConfig.getSessionToken)
      connection.setRequestProperty("Cookie", "dss_user_name=" + Option(loginUser).getOrElse(""))

      val out = connection.getOutputStream
      out.write(body)
      out.flush()
      out.close()

      val responseCode = connection.getResponseCode
      if (responseCode == 502 || responseCode == 504) {
        throw new UpstreamUnreachableException(responseCode, s"DataGo upstream unreachable, HTTP ${responseCode}")
      }

      val inputStream =
        if (responseCode >= 200 && responseCode < 300) connection.getInputStream else connection.getErrorStream
      if (inputStream == null) {
        throw new EmailSendFailedException(81007, s"DataGo returned HTTP ${responseCode} with no response body")
      }

      val reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))
      val sb = new StringBuilder
      var line: String = null
      while ({ line = reader.readLine(); line != null }) {
        sb.append(line)
      }
      reader.close()
      sb.toString
    } catch {
      case e: UpstreamUnreachableException => throw e
      case e: java.net.ConnectException =>
        throw new UpstreamUnreachableException(0, "DataGo connect failed: " + e.getMessage)
      case e: java.net.SocketTimeoutException =>
        throw new UpstreamUnreachableException(0, "DataGo timeout: " + e.getMessage)
      case e: java.net.UnknownHostException =>
        throw new UpstreamUnreachableException(0, "DataGo host unknown: " + e.getMessage)
      case e: EmailSendFailedException => throw e
      case e: Exception =>
        throw new EmailSendFailedException(81007, "DataGo HTTP call failed: " + e.getMessage)
    } finally {
      try connection.disconnect() catch { case _: Exception => () }
    }
  }

  // ---------------------------------------------------------------- multipart body

  private def buildMultipartBody(boundary: String, text: String, recipientsJson: String,
                                 title: String, images: Array[java.io.File]): Array[Byte] = {
    val CRLF = "\r\n"
    val out = new java.io.ByteArrayOutputStream()

    def writeText(name: String, value: String): Unit = {
      out.write(s"--${boundary}${CRLF}".getBytes("UTF-8"))
      out.write(s"""Content-Disposition: form-data; name="${name}"${CRLF}${CRLF}""".getBytes("UTF-8"))
      out.write(s"${value}${CRLF}".getBytes("UTF-8"))
    }

    writeText("source", DataGoOutboundConfig.getSource)
    writeText("type", "image")
    writeText("channel", DataGoOutboundConfig.getChannel)
    writeText("text", text)
    writeText("recipients", recipientsJson)
    writeText("title", title)

    if (images != null) {
      images.foreach { file =>
        out.write(s"--${boundary}${CRLF}".getBytes("UTF-8"))
        out.write(s"""Content-Disposition: form-data; name="images"; filename="${file.getName}"${CRLF}""".getBytes("UTF-8"))
        out.write(s"Content-Type: application/octet-stream${CRLF}${CRLF}".getBytes("UTF-8"))
        out.write(Files.readAllBytes(file.toPath))
        out.write(CRLF.getBytes("UTF-8"))
      }
    }

    out.write(s"--${boundary}--${CRLF}".getBytes("UTF-8"))
    out.toByteArray
  }

  // ---------------------------------------------------------------- helpers

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

  /**
   * Extract a field value from a JSON string (string / boolean / number), in that order.
   * Lightweight regex alternative to pulling in a JSON library; values are matched flat
   * (nesting is irrelevant because field names are unique in the DataGo responses).
   */
  private def getFieldFromJson(json: String, field: String): String = {
    if (json == null) return null
    val patterns = List(
      s""""${field}"\\s*:\\s*"([^"]*)"""".r,
      s""""${field}"\\s*:\\s*(true|false)""".r,
      s""""${field}"\\s*:\\s*(-?\\d+)""".r
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

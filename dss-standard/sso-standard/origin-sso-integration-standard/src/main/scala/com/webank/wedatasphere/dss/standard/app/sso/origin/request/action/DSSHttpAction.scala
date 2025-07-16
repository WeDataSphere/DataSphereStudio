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
package com.webank.wedatasphere.dss.standard.app.sso.origin.request.action

import java.io.{Closeable, InputStream}
import java.util

import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils
import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.linkis.httpclient.request._

/**
 *
 * @date 2022-03-16
 * @author enjoyyin
 * @since 1.1.0
 */
trait DSSHttpAction extends HttpAction with UserAction {

  private var url: String = _
  private var user: String = _

  override def getURL: String = url

  def setUrl(url: String): Unit = {
    this.url = url
  }

  override def setUser(user: String): Unit = {
    this.user = user
  }

  override def getUser: String = user

}

class DSSGetAction extends GetAction with DSSHttpAction

class DSSPostAction extends POSTAction with DSSHttpAction {
  override def getRequestPayload: String = if (getRequestPayloads.isEmpty) "" else DSSCommonUtils.COMMON_GSON.toJson(getRequestPayloads)
}

class DSSDeleteAction extends DeleteAction with DSSHttpAction

class DSSPutAction extends PutAction with DSSHttpAction {
  override def getRequestPayload: String = if (getRequestPayloads.isEmpty) "" else DSSCommonUtils.COMMON_GSON.toJson(getRequestPayloads)
}

class DSSDownloadAction extends DSSGetAction with DownloadAction with DSSHttpAction with Closeable {

  private var inputStream: InputStream = _
  private var response: HttpResponse = _

  def getInputStream: InputStream = inputStream

  override def write(inputStream: InputStream): Unit = {
    this.inputStream = inputStream
  }

  override def getResponse: HttpResponse = response

  override def setResponse(response: HttpResponse): Unit = this.response = response

  override def close(): Unit = {
    if(inputStream != null) {
      IOUtils.closeQuietly(inputStream)
    }
    response match {
      case r: CloseableHttpResponse =>
        IOUtils.closeQuietly(r)
      case _ =>
    }
  }
}

class DSSUploadAction(override val files: util.Map[String, String])
  extends DSSPostAction with UploadAction {

  private var _binaryBodies: util.List[BinaryBody] = _
  private var _inputStreams: util.Map[String, java.io.InputStream] = _

  def this() = this(new util.HashMap[String, String])

  def this(binaryBodies: util.List[BinaryBody]) = {
    this(new util.HashMap[String, String])
    _binaryBodies = binaryBodies
  }

  def setBinaryBodies(binaryBodies: util.List[BinaryBody]): Unit = _binaryBodies = binaryBodies

  def setInputStreams(inputStreams: util.Map[String, java.io.InputStream]): Unit =
    _inputStreams = inputStreams

  override def inputStreams: util.Map[String, java.io.InputStream] = _inputStreams

  override def binaryBodies: util.List[BinaryBody] = _binaryBodies

}
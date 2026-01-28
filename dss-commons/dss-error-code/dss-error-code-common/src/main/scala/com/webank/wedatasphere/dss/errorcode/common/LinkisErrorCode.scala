/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.dss.errorcode.common

import scala.util.matching.Regex


class LinkisErrorCode {

  private var errorRegex: Regex = _

  private var matchContent: String = _
  private var matchType: Int = 0
  private var appName: String = _
  private var uri: String = _
  private var solution: String = _
  private var updateBy: String = _
  private var skipped: Boolean = _



  def this(matchContent: String, matchType: Int, appName: String, uri: String, solution: String, updateBy: String) = {
    this()
    this.matchType = matchType
    this.matchContent = matchContent
    this.appName = appName
    this.uri = uri
    this.solution = solution
    this.updateBy = updateBy

    this.errorRegex = matchContent.r.unanchored
  }


  def getErrorRegex: Regex = this.errorRegex

  def getSolution: String = this.solution

  def getAppName: String = this.appName

  def setAppName(appName: String): Unit = this.appName = appName

  def getSkipped: Boolean = this.skipped

  def setSkipped(skipped: Boolean): Unit = this.skipped = skipped

  def getUri: String = this.uri

  def setUri(uri: String): Unit = this.uri = uri

  def getUpdateBy: String = this.updateBy

  def setUpdateBy(updateBy: String): Unit = this.updateBy = updateBy

  def getMatchContent: String = this.matchContent

  def setSolution(solution: String): Unit = this.solution = solution

  def setMatchContent(matchContent: String): Unit = this.matchContent = matchContent

  def setErrorRegex(errorRegex: Regex): Unit = this.errorRegex = errorRegex

  def setMatchType(matchType: Integer): Unit = this.matchType = matchType

  def getMatchType: Int = this.matchType


  def setErrorRegexStr(errorRegexStr: String): Unit = {
    this.matchContent = errorRegexStr
    this.errorRegex = errorRegexStr.r.unanchored
  }


  def getErrorRegexStr: String = this.matchContent

  override def toString: String = {
    "匹配内容:" + this.matchContent + "," + "匹配类型:" + this.matchType
  }

  //  override def hashCode(): Int = if (errorCode != null) errorCode.hashCode else super.hashCode()

  //  override def equals(obj: Any): Boolean = {
  //    if (!obj.isInstanceOf[LinkisErrorCode]) return false
  //    obj.asInstanceOf[LinkisErrorCode].getErrorCode.equals(this.errorCode)
  //  }
}

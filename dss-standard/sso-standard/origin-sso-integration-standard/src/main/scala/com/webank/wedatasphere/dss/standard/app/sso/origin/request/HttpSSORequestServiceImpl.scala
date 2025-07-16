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
package com.webank.wedatasphere.dss.standard.app.sso.origin.request

import java.util

import com.webank.wedatasphere.dss.standard.app.sso.request.SSORequestService
import com.webank.wedatasphere.dss.standard.common.service.AppServiceImpl

/**
 * 用于通过使用类似 token 的方式，访问没有打通一级规范的第三方系统，不推荐使用。
 * 如第三方系统为 Python 系统，用户无法对接 DSS 一级规范，只能通过 token 的方式访问第三方系统，则推荐使用该实现类。
 * 如何使用该类？请实现一个 {@code SSOIntegrationStandardFactory} 的实现类即可，DSS 框架会自动找到这个实现类，
 * 并加载您的 {@code SSOIntegrationStandard}（推荐直接返回 {@code HttpSSOIntegrationStandard}）
 *
 * @author enjoyyin
 * @since 1.1.0
 */
class HttpSSORequestServiceImpl extends AppServiceImpl with SSORequestService {

  private val ssoRequestServices = new util.HashMap[String, HttpSSORequestOperation]

  protected def createHttpSSORequestOperation(appName: String): HttpSSORequestOperation =
    new HttpSSORequestOperation(appName)

  override def createSSORequestOperation(appName: String): HttpSSORequestOperation = {
    if(!ssoRequestServices.containsKey(appName)) synchronized {
      if(!ssoRequestServices.containsKey(appName)) ssoRequestServices.put(appName, createHttpSSORequestOperation(appName))
    }
    ssoRequestServices.get(appName)
  }

}

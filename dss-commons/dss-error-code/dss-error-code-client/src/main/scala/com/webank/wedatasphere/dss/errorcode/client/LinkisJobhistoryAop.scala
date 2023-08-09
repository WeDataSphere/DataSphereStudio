/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.dss.errorcode.client

import com.webank.wedatasphere.dss.errorcode.client.manager.LinkisErrorCodeManager
import com.webank.wedatasphere.dss.errorcode.common.LinkisErrorCode
import com.webank.wedatasphere.dss.errorcode.common.utils.GsonHelper
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.server.{BDPJettyServerHelper, Message, catchIt}
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect, Pointcut}
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.util.control.Breaks.{break, breakable}

@Order(100)
@Aspect
@Component
class LinkisJobhistoryAop extends Logging {

  private val linkisErrorCodeManager = LinkisErrorCodeManager.getInstance();

  @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(public org.apache.linkis.server.Message *(..))")
  def restfulMessageCatch(): Unit = {}

  @Around("restfulMessageCatch()")
  def dealMessageRestful(proceedingJoinPoint: ProceedingJoinPoint): Object = {
    val message = catchIt {
      proceedingJoinPoint.proceed().asInstanceOf[Message]
    }
    if (!proceedingJoinPoint.getSignature.getDeclaringTypeName.equals("org.apache.linkis.jobhistory.restful.api.QueryRestfulApi")) {
      return message;
    }

    val errorCodes: java.util.List[LinkisErrorCode] = linkisErrorCodeManager.getLinkisErrorCodes
    var solution: String = null
    // jobhistory/{id}/get
    if (proceedingJoinPoint.getSignature.getName.equalsIgnoreCase("getTaskByID")) {
      val task = message.getData.get("task")
      if (task == null) {
        return message
      }
      val taskJson = GsonHelper.toJson(task)
      val taskMap = GsonHelper.getMapFromJson(taskJson)
      val status: String = taskMap.get("status").asInstanceOf[String]
      if (!status.equalsIgnoreCase("Failed")) {
        return message
      }
      val errDesc: String = taskMap.get("errDesc").asInstanceOf[String]
      val errCode = String.valueOf(taskMap.get("errCode").asInstanceOf[Double])
      Utils.tryCatch {
        errorCodes.filter(code => code.getAppName.equalsIgnoreCase("linkis")).foreach(e =>
          breakable {
            e.getMatchType match {
              case 1 => if (e.getMatchContent.r.unanchored.findFirstIn(errDesc).isDefined) {
                solution = e.getSolution
                break
              }
              case 2 => if (e.getMatchContent.equals(errCode)) {
                solution = e.getSolution
                break
              }
            }
          })
      } {
        t: Throwable => error("failed to match error code", t)
      }
    } // jobhistory/list
    else if (proceedingJoinPoint.getSignature.getName.equals("list")) {

    }
    if (solution != null) {
      val map = GsonHelper.getMapFromJson(solution)
      message.data("solution", map)
    } else {
      message.data("solution", null)
    }
    message
  }


}


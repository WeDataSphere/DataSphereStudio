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

@Order(101)
@Aspect
@Component
class RestfulSolutionUrlAOP extends Logging {

  private val linkisErrorCodeManager = LinkisErrorCodeManager.getInstance();

  @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(public org.apache.linkis.server.Message *(..))")
  def restfulMessageCatch(): Unit = {}

  @Around("restfulMessageCatch()")
  def dealMessageRestful(proceedingJoinPoint: ProceedingJoinPoint): Object = {
    val message = catchIt {
      proceedingJoinPoint.proceed().asInstanceOf[Message]
    }
    if (message.getStatus != 0) {
      val errorCodes: java.util.List[LinkisErrorCode] = linkisErrorCodeManager.getLinkisErrorCodes
      errorCodes.filter(_.getSkipped) foreach { e =>
        e.getMatchType match {
          case 1 => if (e.getMatchContent.r.unanchored.findFirstIn(message.getMessage).isDefined) message
          case 2 => if (e.getMatchContent.r().findFirstIn(message.getMessage).isDefined) message
        }
      }
      var solution: String = null
      Utils.tryCatch {
        errorCodes.foreach(e =>
          breakable {
            e.getMatchType match {
              case 1 => if (e.getMatchContent.r.unanchored.findFirstIn(message.getMessage).isDefined) {
                solution = e.getSolution
                break
              }
              case 2 => if (e.getMatchContent.r().findFirstIn(message.getMessage).isDefined) {
                solution = e.getSolution
                break
              }
            }
          })
      } {
        t: Throwable => error("failed to match error code", t)
      }
      if (solution != null) {
        val map = GsonHelper.getMapFromJson(solution)
        message.data("solution", map)
      } else {
        message.data("solution", null)
      }
    }
    message
  }

  @Pointcut("@annotation(javax.ws.rs.Path) && execution(public javax.ws.rs.core.Response *(..)))")
  def restfulResponseCatch(): Unit = {}

  @Around("restfulResponseCatch()")
  def dealResponseRestful(proceedingJoinPoint: ProceedingJoinPoint): Object = {
    val resp: Message = catchIt {
      Message
      return proceedingJoinPoint.proceed()
    }
    resp
  }
}

package com.webank.wedatasphere.dss.detection.server

import com.webank.wedatasphere.dss.common.auditlog.{OperateTypeEnum, TargetTypeEnum}
import com.webank.wedatasphere.dss.common.utils.AuditLogUtils
import com.webank.wedatasphere.dss.detection.server.message.{DetectionMessage, InputStreamMessage, StreamMessage, TextMessage}
import com.webank.wedatasphere.dss.detection.server.method.{DetectionMethod, CustomDetectionMethod}
import com.webank.wedatasphere.dss.detection.server.result.{DetectionResult, DetectionResultFactory, ResultType}
import com.webank.wedatasphere.dss.detection.server.utils.ConstantUtils
import org.apache.commons.lang.StringUtils
import org.slf4j.{Logger, LoggerFactory}

import java.util

/**
 * @date 2023/3/15 11:18
 */
class DetectionService {

  val logger: Logger = LoggerFactory.getLogger(classOf[DetectionService])

  /**
   * 默认结果集类型ResultType.NORMAL，检测策略为webank-scanrules
   */
  var resultType: ResultType = ResultType.NORMAL
  var detectionMethod: DetectionMethod = new CustomDetectionMethod
  val resultFactory: DetectionResultFactory = new DetectionResultFactory

  def this(resultType: ResultType) = {
    this()
    this.resultType = resultType
  }

  def this(resultType: ResultType, detectionMethod: DetectionMethod) {
    this()
    this.resultType = resultType
    this.detectionMethod = detectionMethod
  }

  /**
   * text类型的数据只需要全部读取出来，然后一次性检测即可
   *
   */
  def detectTextMessage(msg: TextMessage): DetectionResult = {
    val result = resultFactory.getDetectionResult(resultType)
    val sensitiveMsg = new util.ArrayList[String]()
    val sensitiveMsgMask = new util.ArrayList[String]()
    val senseInfo = detectionMethod.detectLine(msg.getMessage)
    sensitiveMsg.add(senseInfo)
    sensitiveMsgMask.add(ConstantUtils.maskAll(senseInfo))
    result.setDetectionMethod(detectionMethod)
    result.setSensitiveMsg(sensitiveMsg)
    result.setSensitiveMsgMask(sensitiveMsgMask)
    result.setSubmitInformation(msg.getSubmitInformation)
    result
  }

  def dealResult(msg: DetectionMessage, result: DetectionResult): DetectionResult = {
    if (result.getSensitiveMsg.isEmpty || result.getSensitiveMsg == null || result.getSensitiveMsg.get(0) == null) {
       return result
    }
    val subitInfo = result.getSubmitInformation
    AuditLogUtils.printLog(subitInfo.getUser, subitInfo.getTags.get("workspaceId"), subitInfo.getTags.get("workspaceName"), TargetTypeEnum.WORKFLOW, subitInfo.getTags.get("job"),
      subitInfo.getTags.get("flowName"), OperateTypeEnum.SEND_EMAIL, result)
    result
  }

  def detectStreamMessage(msg: StreamMessage): DetectionResult =  {
    val result = resultFactory.getDetectionResult(resultType)
    result.setDetectionMethod(detectionMethod)
    result.setSubmitInformation(msg.getSubmitInformation)
    //对待检测输入流处理
    msg match {
      case msg: InputStreamMessage => {
        val sensitiveMsg = new util.ArrayList[String]()
        val sensitiveMsgMask = new util.ArrayList[String]()
        while(!msg.isDone) {
          val sensitiveLine = detectionMethod.detectLine(msg.getMessage)
          if(StringUtils.isNotBlank(sensitiveLine)) {
            sensitiveMsg.add(sensitiveLine)
            sensitiveMsgMask.add(ConstantUtils.maskAll(sensitiveLine))
          }
        }
        result.setSensitiveMsg(sensitiveMsg)
        result.setSensitiveMsgMask(sensitiveMsgMask)
        result
      }
      case _ => result
    }
  }

  /**
   * @return
   */
  def detection(msg: DetectionMessage) : DetectionResult = {
    // 加开关 start check sensitive
    if(msg == null) {
      return resultFactory.getDetectionResult
    }
    val result: DetectionResult = msg match {
      case msg: TextMessage => detectTextMessage(msg)
      case msg: StreamMessage => detectStreamMessage(msg)
      case _ => resultFactory.getDetectionResult
    }
    dealResult(msg, result)
    result
  }

}

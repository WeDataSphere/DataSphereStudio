package com.webank.wedatasphere.dss.datamap.exception

import org.apache.linkis.common.exception.ErrorException

case class ITSMException(errMsg: String) extends ErrorException(57899, errMsg){

  def this(errMsg: String, ITSMErrorCode: Int) {
    this(errMsg)
  }

}

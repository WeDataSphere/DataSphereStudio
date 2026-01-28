package com.webank.wedatasphere.dss.datamap.exception


import org.apache.linkis.common.exception.ErrorException

case class DataMapException(errMsg: String) extends ErrorException(57899, errMsg) {
  def this(errMsg: String, dmErrorCode: Int) {
    this(errMsg)
  }
}

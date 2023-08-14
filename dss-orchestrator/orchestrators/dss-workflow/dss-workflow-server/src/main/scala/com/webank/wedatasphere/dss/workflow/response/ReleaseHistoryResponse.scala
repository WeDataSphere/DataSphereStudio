package com.webank.wedatasphere.dss.workflow.response

trait HistoryBaseProtocol {
}

case class ReleaseHistoryResponse(historys: List[Object], totalPage: Int) extends HistoryBaseProtocol {
}

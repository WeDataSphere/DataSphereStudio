package com.webank.wedatasphere.dss.framework.project.rpc

import com.webank.wedatasphere.dss.framework.project.service.WebankDSSProjectOperateService
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestDeleteOrchestrator
import jakarta.annotation.PostConstruct
import org.apache.linkis.rpc.{RPCMessageEvent, Receiver, ReceiverChooser}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by enjoyyin on 2022/9/22.
  */
@Component
class WeBankProjectReceiverChooser extends ReceiverChooser {

  @Autowired
  private var webankDSSProjectOperateService: WebankDSSProjectOperateService = _
  private var receiver: Option[WeBankProjectReceiver] = _

  @PostConstruct
  def init(): Unit = receiver = Some(new WeBankProjectReceiver(webankDSSProjectOperateService))

  override def chooseReceiver(event: RPCMessageEvent): Option[Receiver] = event.message match {
    case _ : RequestDeleteOrchestrator => receiver
    case _ => None
  }
}

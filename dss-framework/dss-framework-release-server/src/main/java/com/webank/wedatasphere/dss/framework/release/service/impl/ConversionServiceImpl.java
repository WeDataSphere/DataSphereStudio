package com.webank.wedatasphere.dss.framework.release.service.impl;

import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.common.utils.RpcAskUtils;
import com.webank.wedatasphere.dss.framework.release.entity.orchestrator.OrchestratorReleaseInfo;
import com.webank.wedatasphere.dss.framework.release.service.ConversionService;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestFrameworkConvertOrchestration;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestFrameworkConvertOrchestrationStatus;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.ResponseConvertOrchestrator;
import com.webank.wedatasphere.dss.sender.service.DSSSenderServiceFactory;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.rpc.Sender;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Created by enjoyyin on 2021/7/4.
 */
@Component
public class ConversionServiceImpl implements ConversionService {

    @Override
    public void convert(RequestFrameworkConvertOrchestration newRequest,  List<DSSLabel> dssLabels) throws DSSErrorException {
        Sender sender = DSSSenderServiceFactory.getOrCreateServiceInstance().getOrcSender(dssLabels);
        newRequest.setOrcAppId(null);
        ResponseConvertOrchestrator response = RpcAskUtils.processAskException(sender.ask(newRequest),
                ResponseConvertOrchestrator.class, RequestFrameworkConvertOrchestration.class);
        if(!response.getResponse().isCompleted()) {
            RequestFrameworkConvertOrchestrationStatus req = new RequestFrameworkConvertOrchestrationStatus(response.getId());
            while(!response.getResponse().isCompleted()) {
                response = RpcAskUtils.processAskException(sender.ask(req), ResponseConvertOrchestrator.class,
                        RequestFrameworkConvertOrchestrationStatus.class);
                Utils.sleepQuietly(1000);
            }
        }
        if(response.getResponse().isFailed()) {
            String msg = response.getResponse().getMessage();
            if(msg!=null&&msg.contains("Duplicate job names found")){
                String regNodeName="'.+\\.job'";
                String nodeName="";
                Matcher matcher= Pattern.compile(regNodeName).matcher(msg);
                if(matcher.find()){
                    nodeName=matcher.group();
                    nodeName=nodeName.substring(0,nodeName.lastIndexOf('.'));
                }
                msg="重复的节点名称。项目中不同工作流（或子工作流）里存在重名节点，请修改节点名避免重名。重名节点:"+nodeName;
            }
            throw new DSSErrorException(50322,msg );
        }
    }
}

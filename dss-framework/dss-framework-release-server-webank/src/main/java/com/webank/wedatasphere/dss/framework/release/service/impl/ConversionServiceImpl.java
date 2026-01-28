package com.webank.wedatasphere.dss.framework.release.service.impl;

import com.webank.wedatasphere.dss.appconn.core.AppConn;
import com.webank.wedatasphere.dss.appconn.core.ext.OptionalAppConn;
import com.webank.wedatasphere.dss.appconn.manager.AppConnManager;
import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.common.utils.RpcAskUtils;
import com.webank.wedatasphere.dss.framework.project.service.DSSProjectService;
import com.webank.wedatasphere.dss.framework.release.service.ConversionService;
import com.webank.wedatasphere.dss.framework.release.utils.ReleaseConf;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ResponseAppAndSubFlowCompare;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.*;
import com.webank.wedatasphere.dss.sender.service.DSSSenderServiceFactory;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.common.desc.AppInstance;
import com.webank.wedatasphere.dss.workflow.core.operation.RefFlowCompareInfoUploadOperation;
import com.webank.wedatasphere.dss.workflow.core.operation.RefFlowCompareInfoUploadOperation.RefFlowCompareInfoUploadRequestRefImpl;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.rpc.Sender;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * Created by enjoyyin on 2021/7/4.
 */
@Component
public class ConversionServiceImpl implements ConversionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConversionServiceImpl.class);
    @Autowired
    private DSSProjectService dssProjectService;

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

    @Override
    public void uploadFlowVersionCompareInfo(String userName,Long projectId,Long orcId,Workspace workspace ) {
        Sender orcSender = DSSSenderServiceFactory.getOrCreateServiceInstance().getOrcSender();
        RequestOrchestratorVersion requestOrchestratorVersion =
                RequestOrchestratorVersion.newInstance(userName, projectId,orcId);
        ResponseOrchetratorVersion responseOrchetratorVersion =
                RpcAskUtils.processAskException(orcSender.ask(requestOrchestratorVersion),ResponseOrchetratorVersion.class, RequestOrchestratorVersion.class);
        List<DSSOrchestratorVersion> orchestratorVersions=responseOrchetratorVersion.getOrchestratorVersions();
        if (CollectionUtils.isEmpty(orchestratorVersions) || orchestratorVersions.size() < 2) {
            LOGGER.info("only one version,return directly.orcId:{},projectId{},username:{}",
                    responseOrchetratorVersion.getOrchestratorId(),projectId,userName);
            //如果只有一个版本，那就不用上传变化信息了
            return;
        }
        //实际项目里，就不要炫算法了，直接老老实实排序吧，性能影响也不大
        orchestratorVersions.sort(Comparator.comparing(DSSOrchestratorVersion::getId).reversed());
        DSSOrchestratorVersion newVersion=orchestratorVersions.get(0);
        DSSOrchestratorVersion oldLastVersion=orchestratorVersions.get(1);
        RequestAppWithSubFlowCompare requestAppWithSubFlowCompare = new RequestAppWithSubFlowCompare(oldLastVersion.getAppId(), newVersion.getAppId());
        Sender flowSender = DSSSenderServiceFactory.getOrCreateServiceInstance().getWorkflowSender();
        ResponseAppAndSubFlowCompare responseAppAndSubFlowCompare =
                RpcAskUtils.processAskException(flowSender.ask(requestAppWithSubFlowCompare), ResponseAppAndSubFlowCompare.class, RequestAppWithSubFlowCompare.class);
        LOGGER.info("get compare info successfully,now try to upload to schedulis,orcId:{},newVersionId:{},oldLastVersionId:{}"
        ,responseOrchetratorVersion.getOrchestratorId(),newVersion.getAppId(),oldLastVersion.getAppId());
        if(CollectionUtils.isEmpty(responseAppAndSubFlowCompare.getList())){
            LOGGER.info("two versions are not different,return directly.orcId:{}",responseOrchetratorVersion.getOrchestratorId());
            //如果没有差异，那就不用上传变化信息了
            return;
        }
        AppConn appConn = AppConnManager.getAppConnManager().getAppConn(ReleaseConf.PROXY_USER_SCHEDULER_APP_CONN_NAME.getValue());
        if(appConn == null) {
            throw new DSSRuntimeException("Not exists " + ReleaseConf.PROXY_USER_SCHEDULER_APP_CONN_NAME.getValue() + " AppConn.");
        }
        if(!(appConn instanceof OptionalAppConn)) {
            throw new DSSRuntimeException("AppConn " + ReleaseConf.PROXY_USER_SCHEDULER_APP_CONN_NAME.getValue() + " doesn't support to upload flow compare info.");
        }
        AppInstance appInstance = appConn.getAppDesc().getAppInstances().get(0);
        RefFlowCompareInfoUploadOperation operation = (RefFlowCompareInfoUploadOperation) ((OptionalAppConn) appConn).getOrCreateOptionalStandard().getOptionalService(appInstance)
                .getOptionalOperation(RefFlowCompareInfoUploadOperation.OPERATION_NAME);
        RefFlowCompareInfoUploadRequestRefImpl requestRef = new RefFlowCompareInfoUploadRequestRefImpl();
        requestRef.setUserName(userName);
        requestRef.setWorkspace(workspace);
        Long refProjectId = dssProjectService.getAppConnProjectId(appInstance.getId(), projectId);
        requestRef.setRefProjectId(refProjectId);
        requestRef.setFlowModifyInfos(responseAppAndSubFlowCompare.getList());
        operation.apply(requestRef);
    }
}

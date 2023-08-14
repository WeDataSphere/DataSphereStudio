package com.webank.wedatasphere.dss.framework.project.service.impl;


import com.webank.wedatasphere.dss.appconn.schedulis.SchedulisAppConn;
import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.common.utils.DSSExceptionUtils;
import com.webank.wedatasphere.dss.common.utils.RpcAskUtils;
import com.webank.wedatasphere.dss.framework.project.dao.DSSProjectMapper;
import com.webank.wedatasphere.dss.framework.project.entity.request.RemoveFlowRequest;
import com.webank.wedatasphere.dss.framework.project.exception.DSSProjectErrorException;
import com.webank.wedatasphere.dss.framework.project.request.ForbiddenFlowRequest;
import com.webank.wedatasphere.dss.framework.project.service.DSSOrchestratorService;
import com.webank.wedatasphere.dss.framework.project.service.ScheduleService;
import com.webank.wedatasphere.dss.framework.workspace.util.WorkspaceUtils;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;
import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorDetail;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.*;
import com.webank.wedatasphere.dss.sender.service.DSSSenderServiceFactory;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.common.exception.operation.ExternalOperationWarnException;
import com.webank.wedatasphere.dss.workflow.core.ref.RefOrchestrationExecutionInfoResponseRef;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.rpc.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: jinyangrao on 2021/7/1
 * @description:
 */
@Service
public class DSSOrchestratorServiceImpl implements DSSOrchestratorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSSOrchestratorServiceImpl.class);

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private DSSProjectMapper dssProjectMapper;

    private final Sender orcSender = DSSSenderServiceFactory.getOrCreateServiceInstance().getScheduleOrcSender();
    ThreadLocal<SimpleDateFormat> simpleDateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    @Override
    public List<OrchestratorDetail> getOrchestratorsByLabel(String username, String dssLabel, Long projectId,Workspace workspace,boolean withScheduleInfo) throws DSSErrorException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} asks {} orchestrators in projectId {}", username, dssLabel, projectId);
        }
        String projectName = dssProjectMapper.getProjectNameById(projectId);
        try {
            RequestOrcDetail requestOrcDetail = new RequestOrcDetail(projectId, username, dssLabel);
            ResponseOrcDetail responseOrcDetail = RpcAskUtils.processAskException(orcSender.ask(requestOrcDetail), ResponseOrcDetail.class, RequestOrcDetail.class);
            List<OrchestratorDetail> orchestratorDetails = responseOrcDetail.getOrchestratorDetails() != null ? responseOrcDetail.getOrchestratorDetails() : new ArrayList<>();
            if (CollectionUtils.isNotEmpty(orchestratorDetails)) {
                for (OrchestratorDetail orchestratorDetail : orchestratorDetails) {
                    //返回工程名称
                    orchestratorDetail.setProjectName(projectName);
                    if(withScheduleInfo) {
                        //将状态,调度信息等内容查出
                        String orcName = orchestratorDetail.getOrchestratorName();
                        //获取编排模式详情
                        List<RefOrchestrationExecutionInfoResponseRef.Execution> executionList = scheduleService.getSchedulerInfoContent(username, projectName, orcName, workspace);
                        setSchedulisInfo(orchestratorDetail, executionList);
                    }
                }
            } else {
                LOGGER.info("Production orchestration mode quantity is empty.");
            }
            return orchestratorDetails;
        } catch (final Throwable t) {
            DSSExceptionUtils.dealErrorException(63321, "Failed to get production orchestration mode: "+t.getMessage(), t, DSSErrorException.class);
        }
        return new ArrayList<OrchestratorDetail>();
    }

    /**
     * 设置编排详情的调度信息
     * @param orchestratorDetail
     * @param executionList
     * @throws IOException
     */
    private void setSchedulisInfo(OrchestratorDetail orchestratorDetail, List<RefOrchestrationExecutionInfoResponseRef.Execution> executionList) throws IOException {
        if (CollectionUtils.isEmpty(executionList)) {
            return;
        }
        for (RefOrchestrationExecutionInfoResponseRef.Execution execution : executionList) {
            orchestratorDetail.setStatus(execution.getStatus());
            orchestratorDetail.setScheduleTime(simpleDateFormat.get().format(new Date(execution.getSubmitTime())));
            orchestratorDetail.setReleaseUser(execution.getSubmitUser());
            orchestratorDetail.setLastUpdater(execution.getSubmitUser());
            orchestratorDetail.setLastUpdateTime(execution.getEndTime() != -1 ? simpleDateFormat.get().format(new Date(execution.getEndTime())) : null);
        }
    }

    @Override
    public void scheduleFlow(String username, String projectName, int orchestratorId, String scheduleTime,
                             String alarmEmails, String alarmLevel, Workspace workspace) throws Exception {
        LOGGER.info("user {}  wants to schedule projectName {} orchestratorId {}", username, projectName, orchestratorId);
        try {
            RequestOrcSchedule requestOrcSchedule =
                    new RequestOrcSchedule(username, projectName, orchestratorId, scheduleTime, alarmEmails, alarmLevel);
            ResponseOrcSchedule responseOrcSchedule = RpcAskUtils.processAskException(orcSender.ask(requestOrcSchedule), ResponseOrcSchedule.class, RequestOrcSchedule.class);
            String orchestratorName = responseOrcSchedule.getOrchestratorName();
            //todo 先在project模块记录下调度的信息
            scheduleService.scheduleFlow(username, projectName, orchestratorName, scheduleTime,
                    alarmEmails, alarmLevel, workspace);
        } catch (Exception e) {
            DSSExceptionUtils.dealErrorException(63322, "Failed to set scheduling："+e.getMessage(), e, DSSErrorException.class);
        }
    }

    @Override
    public void setOrchestratorPriv(String username, int workspaceId, long projectID, String projectName, int orchestratorId, List<String> accessUsers, int privModel,Workspace workspace) throws DSSErrorException {
        LOGGER.info("begin to set orchestrator priv for user {} , projectId {} orcId {} accessUsers {} projectName {}",
                username, projectID, orchestratorId, accessUsers, projectName);
        RequestOrcSchedulePriv requestOrcSchedulePriv =
                new RequestOrcSchedulePriv(username, workspaceId, projectID, projectName, orchestratorId, accessUsers, privModel);
        orcSender.ask(requestOrcSchedulePriv);
//        int priv = 2;
//        Date date = new Date(System.currentTimeMillis());
//        orchestratorMapper.deleteAllOrchestratorPriv(workspaceId, projectID, orchestratorId);
//        orchestratorMapper.setOrchestratorPriv(workspaceId, projectID, orchestratorId, accessUsers, priv, date);
        //wtss权限设置统一移到工程级别进行设置，避免多个编排设置出现不一致情况，这里目前只做生产中心编排可见性设置。
//        SchedulerAppConn schedulerAppConn = (SchedulerAppConn)appConnService.getAppConn("schedulis");
//        schedulerAppConn.setSchedulePriv(username, projectName, accessUsers);
    }

    @Override
    public boolean removeFlowSchedule(RemoveFlowRequest removeFlowRequest, String username,Workspace workspace) throws Exception {
        //todo 接口调通后，需要放到orchestrator模块
        Long schedulisProjectId = dssProjectMapper.getSchedulisProjectId(SchedulisAppConn.SCHEDULIS_APPCONN_NAME,removeFlowRequest.getProjectId());
        if (schedulisProjectId == null || schedulisProjectId == 0) {
            DSSExceptionUtils.dealErrorException(633239, "schedulis工程Id为空或者为0，WTSS没有对应的工程", DSSProjectErrorException.class);
        }
        //删除prod环境的调度信息
        RequestOrcSchedualisDelete requestOrcSchedualisDelete =
                new RequestOrcSchedualisDelete(removeFlowRequest.getOrchestratorId());
        int updateCount = (Integer) orcSender.ask(requestOrcSchedualisDelete);

        //调用WTSS的接口，删除调度
        boolean status = false;
        try {
            status = scheduleService.removeFlowSchedule(schedulisProjectId, removeFlowRequest.getOrchestratorName(), username, workspace);
        } catch (ExternalOperationWarnException e) {
            LOGGER.error("removeFlowScheduleError-{}", e.getMessage());
            //如果schedulisId为空，直接返回
            if (90089 == e.getErrCode() || 100333 == e.getErrCode()) {
                return true;
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("removeFlowScheduleError2-", e);
            throw e;
        }
        LOGGER.info("remove flow schedule success, updateCount-{}", updateCount);
        return status;
    }

    @Override
    public boolean setFlowSchedule(ForbiddenFlowRequest forbiddenFlowRequest, String username, Workspace workspace) throws Exception {
        //todo 接口调通后，需要放到orchestrator模块
        Long schedulisProjectId = dssProjectMapper.getSchedulisProjectId(SchedulisAppConn.SCHEDULIS_APPCONN_NAME,forbiddenFlowRequest.getProjectId());
        if (schedulisProjectId == null) {
            DSSExceptionUtils.dealErrorException(633240, "schedulis工程Id为空或者为0，WTSS没有对应的工程", DSSProjectErrorException.class);
        }
        //更新调用标示
        RequestOrcSchedualisUpdate requestOrcSchedualisUpdate =
                new RequestOrcSchedualisUpdate(forbiddenFlowRequest.getOrchestratorId(), forbiddenFlowRequest.getActiveFlag());
        int updateCount = (Integer) orcSender.ask(requestOrcSchedualisUpdate);

        //调用WTSS接口，更新调度标示
        boolean status = false;
        try {
            status = scheduleService.setWorkflowActiveFlag(forbiddenFlowRequest.getActiveFlag(), schedulisProjectId,
                forbiddenFlowRequest.getOrchestratorName(), username, workspace);
        } catch (ExternalOperationWarnException e) {
            LOGGER.error("removeFlowScheduleError, username is {}, projectId is {}, orchestratorName is {}.", username, forbiddenFlowRequest.getProjectId(),
                forbiddenFlowRequest.getOrchestratorName(), e);
            //如果schedulisId为空，直接返回
            if (90089 == e.getErrCode() || 100333 == e.getErrCode()) {
                return true;
            }
            throw e;
        } catch (Exception e) {
            LOGGER.error("removeFlowScheduleError2-", e);
            throw e;
        }
        LOGGER.info("setFlowSchedule update count (dss_orchestrator_schedule_info) = {}", updateCount);
        return status;
    }

    @Override
    public List<OrchestratorDetail> getAllOrchestratorDetailsOfWorkspace(String username, Long workspaceId, String dssLabel,Workspace workspace) throws DSSErrorException {
        //1：显示；0：隐藏
        int visible = 1;
        List<OrchestratorDetail> orchestratorDetails = dssProjectMapper.getProjectIdsByWorkspaceId(workspaceId, visible).stream()
                .map(tmpProjectId -> {
                try {
                    List<OrchestratorDetail> orchestratorDetail = getOrchestratorsByLabel(username, dssLabel, tmpProjectId,workspace,true);
                    return orchestratorDetail;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            })
                .filter(Objects::nonNull).
                flatMap(tempOrcList -> tempOrcList.stream())
                .collect(Collectors.toList());
        return orchestratorDetails;
    }

    @Override
    public List<DSSOrchestratorVersion> getOrchestratorVersions(String username, Long projectId, Long orchestratorId, String dssLabel) {
        //todo 按照dsslabel进行选择 默认搞一下生产的
        LOGGER.info("user {} wants to get versions in  projectId {}, orchestratorId {}, dssLabel {}", username,
                projectId, orchestratorId, dssLabel);
        //通过rpc的方式去获取的版本信息
        RequestOrchestratorVersion requestOrchestratorVersion = RequestOrchestratorVersion.newInstance(username, projectId, orchestratorId, dssLabel);
        ResponseOrchetratorVersion responseOrchetratorVersion = RpcAskUtils.processAskException(orcSender.ask(requestOrchestratorVersion),
                ResponseOrchetratorVersion.class, RequestOrchestratorVersion.class);
        LOGGER.info("user {} ends to getVersions : {} ", username, responseOrchetratorVersion.getOrchestratorVersions());
        return responseOrchetratorVersion.getOrchestratorVersions();
    }


    @Override
    public String getScheduleHistoryUrl(String schedulerName) throws Exception {
        String scheduleHistoryUrl = getHistoryUrl(SchedulisAppConn.SCHEDULIS_APPCONN_NAME);
        if (StringUtils.isNotBlank(scheduleHistoryUrl)) {
            scheduleHistoryUrl = !scheduleHistoryUrl.endsWith("/") ? scheduleHistoryUrl + "/" : scheduleHistoryUrl;
        }
        String redirectUrl = scheduleHistoryUrl + "api/v1/redirect";
        String historyUrl = scheduleHistoryUrl + "manager?project=${projectName}&flow=${flowName}#executions";
        scheduleHistoryUrl = WorkspaceUtils.redirectUrlFormat(redirectUrl, historyUrl);

        return scheduleHistoryUrl;
    }

    @Override
    public String getHistoryUrl(String schedulerName) {
        return dssProjectMapper.getSchedualisUrl(schedulerName);
    }
}

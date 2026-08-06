package com.webank.wedatasphere.dss.framework.project.service.impl;


import com.webank.wedatasphere.dss.appconn.schedulis.SchedulisAppConn;
import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.common.utils.DSSExceptionUtils;
import com.webank.wedatasphere.dss.common.utils.RpcAskUtils;
import com.webank.wedatasphere.dss.framework.project.dao.DSSProjectMapper;
import com.webank.wedatasphere.dss.framework.project.dao.WebankDSSProjectMapper;
import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectUser;
import com.webank.wedatasphere.dss.framework.project.entity.request.RemoveFlowRequest;
import com.webank.wedatasphere.dss.framework.project.exception.DSSProjectErrorException;
import com.webank.wedatasphere.dss.framework.project.request.ForbiddenFlowRequest;
import com.webank.wedatasphere.dss.framework.project.service.DSSProjectUserService;
import com.webank.wedatasphere.dss.framework.project.service.WebankDSSOrchestratorService;
import com.webank.wedatasphere.dss.framework.project.service.WebankScheduleService;
import com.webank.wedatasphere.dss.framework.workspace.util.WorkspaceUtils;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;
import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorDetail;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestOrcDetail;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestOrcSchedualisDelete;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestOrcSchedualisUpdate;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestOrcSchedule;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestOrcSchedulePriv;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestOrchestratorVersion;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.ResponseOrcDetail;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.ResponseOrcSchedule;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.ResponseOrchetratorVersion;
import com.webank.wedatasphere.dss.sender.service.DSSSenderServiceFactory;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.common.exception.operation.ExternalOperationWarnException;
import com.webank.wedatasphere.dss.workflow.core.ref.RefOrchestrationExecutionInfoResponseRef;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.rpc.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: jinyangrao on 2021/7/1
 * @description:
 */
@Service
public class WebankDSSOrchestratorServiceImpl implements WebankDSSOrchestratorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebankDSSOrchestratorServiceImpl.class);

    @Autowired
    private WebankScheduleService webankScheduleService;

    @Autowired
    private DSSProjectMapper dssProjectMapper;

    @Autowired
    private WebankDSSProjectMapper webankDSSProjectMapper;

    @Autowired
    private DSSProjectUserService projectUserService;


    private final Sender orcSender = DSSSenderServiceFactory.getOrCreateServiceInstance().getScheduleOrcSender();
    ThreadLocal<SimpleDateFormat> simpleDateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    /**
     * 生产中心编排/调度信息并发拉取线程池。
     * <p>
     * 用于并发执行 {@link #getAllOrchestratorDetailsOfWorkspace} 的按项目遍历，以及
     * {@link #getOrchestratorsByLabel} 内逐编排的 Schedulis HTTP 调用，消除原有 N×M 次顺序远程调用的耗时。
     * <p>
     * 设计要点：
     * <ul>
     *   <li>有界线程池（核心 8 / 最大 16），并发度上限按 Schedulis 承压能力设定，避免瞬时打爆下游；</li>
     *   <li>有界队列 + CallerRunsPolicy 背压：队列满时回退到调用线程同步执行，既不丢任务又对下游形成天然限流；</li>
     *   <li>daemon 线程，不阻塞 JVM 退出；进程级单例，随 {@code @Service} 生命周期共享。</li>
     * </ul>
     */
    private final ThreadFactory orcScheduleThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("dss-prod-orchestrator-fetch-thread-%d")
            .setDaemon(true)
            .build();
    private final ExecutorService orcScheduleExecutor = new ThreadPoolExecutor(
            8, 16, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(512),
            orcScheduleThreadFactory,
            new ThreadPoolExecutor.CallerRunsPolicy());

    @Override
    public List<OrchestratorDetail> getOrchestratorsByLabel(String username, String dssLabel, Long projectId,Workspace workspace,boolean withScheduleInfo) throws DSSErrorException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} asks {} orchestrators in projectId {}", username, dssLabel, projectId);
        }
        String projectName = dssProjectMapper.getProjectNameById(projectId);
        try {
            List<String> permissionUsers = projectUserService.getProjectPriv(projectId).stream()
                    .filter(projectUser -> projectUser.getPriv() == 3).map(DSSProjectUser::getUsername).collect(Collectors.toList());
            RequestOrcDetail requestOrcDetail = new RequestOrcDetail(projectId, username, dssLabel, permissionUsers);
            ResponseOrcDetail responseOrcDetail = RpcAskUtils.processAskException(orcSender.ask(requestOrcDetail), ResponseOrcDetail.class, RequestOrcDetail.class);
            List<OrchestratorDetail> orchestratorDetails = responseOrcDetail.getOrchestratorDetails() != null ? responseOrcDetail.getOrchestratorDetails() : new ArrayList<>();

            if (CollectionUtils.isNotEmpty(orchestratorDetails)) {
                if (withScheduleInfo) {
                    // 将状态、调度信息等内容查出：逐编排 Schedulis HTTP 改为并发拉取，消除 N×M 顺序远程调用耗时。
                    // 单个编排调度信息拉取失败时仅 warn 并留空状态字段，不阻断其它编排（容错增强）。
                    List<CompletableFuture<Void>> schedulisFutures = orchestratorDetails.stream()
                            .map(orchestratorDetail -> CompletableFuture.runAsync(() -> {
                                orchestratorDetail.setProjectName(projectName);
                                try {
                                    String orcName = orchestratorDetail.getOrchestratorName();
                                    // 获取编排模式详情
                                    List<RefOrchestrationExecutionInfoResponseRef.Execution> executionList =
                                            webankScheduleService.getSchedulerInfoContent(username, projectName, orcName, workspace);
                                    setSchedulisInfo(orchestratorDetail, executionList);
                                } catch (final Throwable t) {
                                    LOGGER.warn("getSchedulerInfoContent failed, orcName={}", orchestratorDetail.getOrchestratorName(), t);
                                }
                            }, orcScheduleExecutor))
                            .collect(Collectors.toList());
                    CompletableFuture.allOf(schedulisFutures.toArray(new CompletableFuture[0])).join();
                } else {
                    // withScheduleInfo=false 分支（如项目复制）不拉取调度信息，仅回填工程名称，行为与原顺序逻辑一致。
                    for (OrchestratorDetail orchestratorDetail : orchestratorDetails) {
                        orchestratorDetail.setProjectName(projectName);
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
            webankScheduleService.scheduleFlow(username, projectName, orchestratorName, scheduleTime,
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
        Long schedulisProjectId = webankDSSProjectMapper.getSchedulisProjectId(SchedulisAppConn.SCHEDULIS_APPCONN_NAME,removeFlowRequest.getProjectId());
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
            status = webankScheduleService.removeFlowSchedule(schedulisProjectId, removeFlowRequest.getOrchestratorName(), username, workspace);
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
        Long schedulisProjectId = webankDSSProjectMapper.getSchedulisProjectId(SchedulisAppConn.SCHEDULIS_APPCONN_NAME,forbiddenFlowRequest.getProjectId());
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
            status = webankScheduleService.setWorkflowActiveFlag(forbiddenFlowRequest.getActiveFlag(), schedulisProjectId,
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
        List<Long> projectIds = dssProjectMapper.getProjectIdsByWorkspaceId(workspaceId, visible);
        if (CollectionUtils.isEmpty(projectIds)) {
            return new ArrayList<>();
        }
        // 按项目并发拉取编排详情，消除原顺序 stream().map 的耗时；单个项目失败返回 null 被 filter 过滤，隔离语义不变。
        // 最终排序由外层 OrchestratorDetailsUtils.sortOrchestratorDetailList 统一完成，并发收集顺序不影响结果。
        List<CompletableFuture<List<OrchestratorDetail>>> futures = projectIds.stream()
                .map(tmpProjectId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return getOrchestratorsByLabel(username, dssLabel, tmpProjectId, workspace, true);
                    } catch (Exception e) {
                        LOGGER.error("getAllOrchestratorDetailsOfWorkspace failed, projectId={}", tmpProjectId, e);
                        return null;
                    }
                }, orcScheduleExecutor))
                .collect(Collectors.toList());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<DSSOrchestratorVersion> getOrchestratorVersions(String username, Long projectId, Long orchestratorId, String dssLabel) {
        //todo 按照dsslabel进行选择 默认搞一下生产的
        LOGGER.info("user {} wants to get versions in  projectId {}, orchestratorId {}, dssLabel {}", username,
                projectId, orchestratorId, dssLabel);
        //通过rpc的方式去获取的版本信息
        RequestOrchestratorVersion requestOrchestratorVersion = RequestOrchestratorVersion.newInstance(username, projectId, orchestratorId);
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
        return webankDSSProjectMapper.getSchedualisUrl(schedulerName);
    }
}

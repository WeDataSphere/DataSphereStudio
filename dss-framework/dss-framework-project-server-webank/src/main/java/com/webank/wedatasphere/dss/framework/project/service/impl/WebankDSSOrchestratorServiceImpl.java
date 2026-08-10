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
     * 生产中心编排详情并发拉取线程池（仅项目级）。
     * <p>
     * 用于 {@link #getAllOrchestratorDetailsOfWorkspace} 按项目并发拉取编排详情。每个项目任务内部对编排的
     * Schedulis HTTP 调用保持顺序执行（不再向本池提交子任务），因此不存在"外层任务占线程等待内层任务、
     * 内层任务又排队等线程"的嵌套依赖，结构上不会发生线程池自死锁。
     * <p>
     * 有界线程池 + 有界队列 + CallerRunsPolicy 背压（队列满时回退调用线程同步执行，天然限流，不丢任务）；
     * daemon 线程，不阻塞 JVM 退出。
     */
    private final ExecutorService projectFetchExecutor = new ThreadPoolExecutor(
            4, 8, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(256),
            new ThreadFactoryBuilder().setNameFormat("dss-prod-project-fetch-thread-%d").setDaemon(true).build(),
            new ThreadPoolExecutor.CallerRunsPolicy());

    /** 全量拉取的总超时（秒）：防止个别项目因 Schedulis HTTP hang 导致 get 无限等待、接口一直 pending；超时后抛异常让接口失败。 */
    private static final long WORKSPACE_FETCH_TIMEOUT_SECONDS = 120L;

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
                for (OrchestratorDetail orchestratorDetail : orchestratorDetails) {
                    //返回工程名称
                    orchestratorDetail.setProjectName(projectName);
                    if(withScheduleInfo) {
                        //将状态,调度信息等内容查出
                        String orcName = orchestratorDetail.getOrchestratorName();
                        //获取编排模式详情
                        List<RefOrchestrationExecutionInfoResponseRef.Execution> executionList = webankScheduleService.getSchedulerInfoContent(username, projectName, orcName, workspace);
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
        // 仅对项目级做并发：每个项目任务内部调用 getOrchestratorsByLabel（编排级 Schedulis HTTP 仍顺序执行），
        // 项目任务不再向 projectFetchExecutor 提交子任务，无嵌套并发依赖，结构上不会发生线程池自死锁。
        // 任一项目获取编排报错即抛异常（不再吞掉跳过），让整个 getProdOrchestrators 接口失败，问题立即暴露。
        // 最终排序由外层 OrchestratorDetailsUtils.sortOrchestratorDetailList 统一完成，并发收集顺序不影响结果。
        List<CompletableFuture<List<OrchestratorDetail>>> futures = projectIds.stream()
                .map(tmpProjectId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return getOrchestratorsByLabel(username, dssLabel, tmpProjectId, workspace, true);
                    } catch (DSSErrorException e) {
                        // 获取编排报错：不吞异常，直接抛出，使该 future 异常完成，进而让整个请求失败
                        LOGGER.error("getAllOrchestratorDetailsOfWorkspace failed, projectId={}", tmpProjectId, e);
                        throw new RuntimeException(e);
                    }
                }, projectFetchExecutor))
                .collect(Collectors.toList());
        CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            // 带超时 get 兜底：任一项目报错 -> allOf 异常完成 -> get 抛 ExecutionException（首个异常立即抛出，不等其它项目）；
            // 若个别项目因 Schedulis HTTP hang 卡住，超时后抛异常，保证接口不会一直 pending。
            all.get(WORKSPACE_FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            all.cancel(true);
            throw new DSSErrorException(63321, "获取工作空间编排详情超时（" + WORKSPACE_FETCH_TIMEOUT_SECONDS + "s），workspaceId=" + workspaceId);
        } catch (java.util.concurrent.ExecutionException e) {
            // 解包首个项目报错的原始异常并抛出
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            if (cause.getCause() instanceof DSSErrorException) {
                throw (DSSErrorException) cause.getCause();
            }
            throw new DSSErrorException(63321, "获取工作空间编排详情失败: " + cause.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DSSErrorException(63321, "获取工作空间编排详情被中断，workspaceId=" + workspaceId);
        }
        return futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        // 走到这里说明 allOf 已正常完成，理论上不会有异常；防御性返回 null 交由 filter 跳过
                        return null;
                    }
                })
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

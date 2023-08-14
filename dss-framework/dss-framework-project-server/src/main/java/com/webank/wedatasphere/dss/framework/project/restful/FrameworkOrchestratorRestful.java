package com.webank.wedatasphere.dss.framework.project.restful;

import com.webank.wedatasphere.dss.framework.project.entity.ProjectOperateRecordBO;
import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectQueryRequest;
import com.webank.wedatasphere.dss.framework.project.entity.request.RemoveFlowRequest;
import com.webank.wedatasphere.dss.framework.project.entity.response.ProjectResponse;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateTypeEnum;
import com.webank.wedatasphere.dss.framework.project.request.ForbiddenFlowRequest;
import com.webank.wedatasphere.dss.framework.project.request.ProductWorkflowPrivRequest;
import com.webank.wedatasphere.dss.framework.project.request.ScheduleFlowRequest;
import com.webank.wedatasphere.dss.framework.project.service.DSSProjectService;
import com.webank.wedatasphere.dss.framework.project.service.DSSOrchestratorService;
import com.webank.wedatasphere.dss.framework.project.service.DSSProjectOperateService;
import com.webank.wedatasphere.dss.framework.project.service.impl.ProxyUserProjectHttpRequestHook;
import com.webank.wedatasphere.dss.framework.project.utils.OrchestratorDetailsUtils;
import com.webank.wedatasphere.dss.framework.proxy.conf.ProxyUserConfiguration;
import com.webank.wedatasphere.dss.framework.proxy.exception.DSSProxyUserErrorException;
import com.webank.wedatasphere.dss.framework.proxy.service.DssProxyUserService;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;
import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorDetail;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.sso.utils.SSOHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/dss/framework/project", produces = {"application/json"})
public class FrameworkOrchestratorRestful {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkOrchestratorRestful.class);
    private static final Long ALL_PROJECT_FLAG = -1L;
    @Autowired
    private DSSOrchestratorService DSSOrchestratorService;
    @Autowired
    private DSSProjectService dssProjectService;
    @Autowired
    private DssProxyUserService dssProxyUserService;
    @Autowired
    private DSSProjectOperateService DSSProjectOperateService;

    private void saveProjectOperation(HttpServletRequest request, Long projectId, Function<String, String> createContent, ProjectOperateTypeEnum operateType) {
        ProjectOperateRecordBO record = ProxyUserProjectHttpRequestHook.createProjectOperateRecordBO(request, projectId, createContent, dssProxyUserService);
        record.setOperateType(operateType);
        DSSProjectOperateService.addOneRecord(record);
    }

    /**
     * 设置调度
     */
    @PostMapping(path = "/scheduleFlow")
    public Message scheduleProductFlow(HttpServletRequest request, @RequestBody ScheduleFlowRequest scheduleFlowRequest) throws ErrorException {
        if(ProxyUserConfiguration.isProxyUserEnable()) {
            return Message.error("该 DSS 环境不支持修改工作流调度信息，如需修改请到调度系统中修改,本次保存调度设置相关内容修改将被忽略");
        }
        String username = SecurityFilter.getLoginUsername(request);
        Workspace workspace = SSOHelper.getWorkspace(request);
        String projectName = scheduleFlowRequest.getProjectName();
        int orchestratorId = scheduleFlowRequest.getOrchestratorId();
        String scheduleTime = scheduleFlowRequest.getScheduleTime();
        String alarmEmails = scheduleFlowRequest.getAlarmUserEmails();
        String alarmLevel = scheduleFlowRequest.getAlarmLevel();
        try {
            LOGGER.info("user {} try to scheduleProductFlow, the request params is: {}.", username, scheduleFlowRequest);
            DSSOrchestratorService.scheduleFlow(username, projectName, orchestratorId, scheduleTime, alarmEmails, alarmLevel, workspace);
        }  catch (final Throwable t) {
            LOGGER.error("failed to schedule flow projectName {}, orchestratorId {}, username {}", projectName, orchestratorId, username, t);
            return Message.error("为工作流设置定时调度失败"+t.getMessage());
        }
        return Message.ok("配置调度成功");
    }


    /**
     * 删除调度
     */
    @PostMapping(path = "/removeFlowSchedule")
    public Message removeFlowSchedule(HttpServletRequest request, @RequestBody RemoveFlowRequest removeFlowRequest) throws Exception {
        String username = SecurityFilter.getLoginUsername(request);
        Workspace workspace = SSOHelper.getWorkspace(request);
        LOGGER.info("user {} try to removeFlowSchedule, the request params is: {}.", username, removeFlowRequest);
        boolean status = DSSOrchestratorService.removeFlowSchedule(removeFlowRequest, username, workspace);
        saveProjectOperation(request, removeFlowRequest.getProjectId(),
            user -> String.format("%s removed flow schedule %s.", user, removeFlowRequest.getOrchestratorName()), ProjectOperateTypeEnum.DELETE_SCHEDULE);
        return Message.ok().data("status", status);
    }


    /**
     * 禁用调度
     */
    @PostMapping(path = "/setFlowSchedule")
    public Message setFlowSchedule(HttpServletRequest request, @RequestBody ForbiddenFlowRequest forbiddenFlowRequest) throws Exception {
        String username = SecurityFilter.getLoginUsername(request);
        LOGGER.info("user {} try to setFlowSchedule, the request params is: {}.", username, forbiddenFlowRequest);
        Workspace workspace = SSOHelper.getWorkspace(request);
        String activeFlag = forbiddenFlowRequest.getActiveFlag();
        if (StringUtils.isBlank(activeFlag)) {
            Message.error("设置调度的标示[activeFlag]不能为空");
        }
        if (!"true".equals(activeFlag) && !"false".equals(activeFlag)) {
            Message.error("调度的标示[activeFlag]只能为true或者为false");
        }
        boolean status = DSSOrchestratorService.setFlowSchedule(forbiddenFlowRequest, username, workspace);
        saveProjectOperation(request, forbiddenFlowRequest.getProjectId(), user -> String.format("Workflow schedule %s was forbidden by %s.",
            forbiddenFlowRequest.getOrchestratorName(), user), ProjectOperateTypeEnum.DISABLE_SCHEDULE);
        return Message.ok().data("status", status);
    }

    /**
     * 设置生产中心权限
     */
    @PostMapping(path = "/setProductWorkflowPriv")
    public Message setProductWorkflowPriv(HttpServletRequest request, @RequestBody ProductWorkflowPrivRequest productWorkflowPrivRequest) throws Exception {
        if(ProxyUserConfiguration.isProxyUserEnable()) {
            return Message.error("该 DSS 环境不支持修改工作流的用户权限,本次保存权限设置相关内容修改将被忽略");
        }
        String username = SecurityFilter.getLoginUsername(request);
        LOGGER.info("user {} try to setProductWorkflowPriv, the request params is: {}.", username, productWorkflowPrivRequest);
        Workspace workspace = SSOHelper.getWorkspace(request);
        int privModel = productWorkflowPrivRequest.getPrivModel();
        List<String> accessUsers = productWorkflowPrivRequest.getAccessUsers();
        int orchestratorId = productWorkflowPrivRequest.getOrchestratorId();
        String projectName = productWorkflowPrivRequest.getProjectName();
        long projectID = productWorkflowPrivRequest.getProjectID();
        int workspaceId = productWorkflowPrivRequest.getWorkspaceId();
        if (accessUsers == null) {
            accessUsers = new ArrayList<>();
        }
        if (!accessUsers.contains(username)) {
            accessUsers.add(username);
        }
        DSSOrchestratorService.setOrchestratorPriv(username, workspaceId, projectID, projectName, orchestratorId, accessUsers, privModel, workspace);
        return Message.ok("更改工作流的用户成功");
    }

    /**
     * 根据标签获取的编排模式
     */
    @GetMapping(path = "/getProdOrchestrators")
    public Message getProjectOrchestratorByLabel(HttpServletRequest request, @RequestParam("dssLabel") String dssLabel,
                                                 @RequestParam("projectId") Long projectId,
                                                 @RequestParam("workspaceId") Long workspaceId) throws DSSProxyUserErrorException {
        String username = SecurityFilter.getLoginUsername(request);
        Workspace workspace = SSOHelper.getWorkspace(request);
        LOGGER.info("user {} begin to get project orchestrator by label, dssLabel: {},projectId: {},workspaceId: {}.",username,dssLabel,projectId,workspaceId);
        if(ProxyUserConfiguration.isProxyUserEnable()) {
            ProjectQueryRequest projectQueryRequest = new ProjectQueryRequest();
            String proxyUser;
            try {
                proxyUser = dssProxyUserService.getProxyUser(request);
            }catch (DSSProxyUserErrorException e){
                LOGGER.error("getProxyUser Failed,cookie is :{}", Arrays.stream(request.getCookies())
                        .map(cookie->String.format("%s=%s",cookie.getName(),cookie.getValue())).collect(Collectors.joining(",")));
                throw e;
            }
            projectQueryRequest.setUsername(proxyUser);
            projectQueryRequest.setId(projectId);
            projectQueryRequest.setWorkspaceId(workspace.getWorkspaceId());
            List<ProjectResponse> projects = dssProjectService.getListByParam(projectQueryRequest);
            if(CollectionUtils.isEmpty(projects)) {
                return Message.error("You have no permission to access this project.");
            }
            username = proxyUser;
        }
        List<OrchestratorDetail> orchestratorDetails;
        String scheduleHistoryUrl;
        try {
            // ALL_PROJECT_FLAG为-1则获取所有的编排
            orchestratorDetails = projectId.equals(ALL_PROJECT_FLAG)
                    ?
                    DSSOrchestratorService.getAllOrchestratorDetailsOfWorkspace(username,workspaceId, dssLabel, workspace)
                    :
                    DSSOrchestratorService.getOrchestratorsByLabel(username,dssLabel,projectId, workspace,true);
            OrchestratorDetailsUtils.sortOrchestratorDetailList(orchestratorDetails);
            scheduleHistoryUrl = DSSOrchestratorService.getScheduleHistoryUrl("Schedulis");
            return Message.ok("获取编排模式成功")
                    .data("orchestrators", orchestratorDetails)
                    .data("scheduleHistory", scheduleHistoryUrl);
        } catch (final Exception t) {
            LOGGER.error("failed to getOrchestratorByLabel for user {}, projectId {}, workspaceId: {}.", username, projectId, workspaceId, t);
            return Message.error("failed to get " + dssLabel + " orchestrator " + t.getMessage());
        }
    }


    @GetMapping("/listAllOrchestratorVersions")
    public Message listAllProductFlowVersions(HttpServletRequest request,
                                              @RequestParam("projectId") Long projectId,
                                              @RequestParam("orchestratorId") Long orchestratorId,
                                              @RequestParam("dssLabel") String dssLabel) {
        String username = SecurityFilter.getLoginUsername(request);
        try {
            List<DSSOrchestratorVersion> orchestratorVersions = DSSOrchestratorService.getOrchestratorVersions(username, projectId, orchestratorId, dssLabel);
            orchestratorVersions.sort(new Comparator<DSSOrchestratorVersion>() {
                @Override
                public int compare(DSSOrchestratorVersion o1, DSSOrchestratorVersion o2) {
                    return o2.getVersion().compareTo(o1.getVersion());
                }
            });
            return Message.ok("获取orchstrator版本信息成功").data("orchestratorVersions", orchestratorVersions);
        } catch (final Throwable t) {
            LOGGER.error("Failed to list all versions for projectId {}, orchestratorId {} , dssLabel {}.", projectId, orchestratorId, dssLabel, t);
            return Message.error("获取orchestrator版本信息失败");
        }
    }

}

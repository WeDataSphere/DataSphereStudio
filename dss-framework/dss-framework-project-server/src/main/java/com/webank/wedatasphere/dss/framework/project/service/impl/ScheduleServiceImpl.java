package com.webank.wedatasphere.dss.framework.project.service.impl;

import com.webank.wedatasphere.dss.appconn.manager.AppConnManager;
import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.ref.RefOrchestrationContentRequestRef;
import com.webank.wedatasphere.dss.framework.common.exception.DSSFrameworkWarnException;
import com.webank.wedatasphere.dss.framework.project.service.ScheduleService;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.app.structure.optional.OptionalOperation;
import com.webank.wedatasphere.dss.standard.app.structure.optional.OptionalService;
import com.webank.wedatasphere.dss.standard.common.desc.AppInstance;
import com.webank.wedatasphere.dss.standard.common.entity.ref.RequestRef;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;
import com.webank.wedatasphere.dss.standard.common.exception.operation.ExternalOperationWarnException;
import com.webank.wedatasphere.dss.standard.common.utils.RequestRefUtils;
import com.webank.wedatasphere.dss.workflow.core.InternalSchedulerAppConn;
import com.webank.wedatasphere.dss.workflow.core.operation.RefOrchestrationActiveFlagOperation;
import com.webank.wedatasphere.dss.workflow.core.operation.RefOrchestrationCronOperation;
import com.webank.wedatasphere.dss.workflow.core.operation.RefOrchestrationExecutionInfoOperation;
import com.webank.wedatasphere.dss.workflow.core.operation.RefOrchestrationUncronOperation;
import com.webank.wedatasphere.dss.workflow.core.ref.RefOrchestrationActiveFlagRequestRef;
import com.webank.wedatasphere.dss.workflow.core.ref.RefOrchestrationCronRequestRef;
import com.webank.wedatasphere.dss.workflow.core.ref.RefOrchestrationExecutionInfoResponseRef;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Override
    public List<RefOrchestrationExecutionInfoResponseRef.Execution> getSchedulerInfoContent(String username, String projectName, String orcName, Workspace workspace){
        Consumer<RefOrchestrationContentRequestRef> requestRefConsumer = requestRef -> {
            requestRef.setUserName(username);
            requestRef.setProjectName(projectName);
            requestRef.setOrchestrationName(orcName);
            requestRef.setWorkspace(workspace);
        };
        RefOrchestrationExecutionInfoResponseRef responseRef = tryOptionalOperation(RefOrchestrationExecutionInfoOperation.OPERATION_NAME, requestRefConsumer);
        return responseRef.getExecutions();
    }


    @Override
    public void scheduleFlow(String username, String projectName, String orchestratorName, String scheduleTime,
                             String alarmEmails, String alarmLevel, Workspace workspace) {
        //todo 先在project模块记录下调度的信息
        Consumer<RefOrchestrationCronRequestRef> requestRefConsumer = requestRef -> {
            requestRef.setProjectName(projectName);
            requestRef.setOrchestrationName(orchestratorName);
            requestRef.setScheduleTime(scheduleTime);
            requestRef.setAlarmEmails(alarmEmails);
            requestRef.setAlarmLevel(alarmLevel);
            requestRef.setUserName(username);
            requestRef.setWorkspace(workspace);
        };
        tryOptionalOperation(RefOrchestrationCronOperation.OPERATION_NAME, requestRefConsumer);
    }

    @Override
    public boolean removeFlowSchedule(Long refProjectId, String orchestratorName, String username, Workspace workspace) {
        Consumer<RefOrchestrationContentRequestRef> requestRefConsumer = requestRef -> {
            requestRef.setRefProjectId(refProjectId);
            requestRef.setOrchestrationName(orchestratorName);
            requestRef.setUserName(username);
            requestRef.setWorkspace(workspace);
        };
        tryOptionalOperation(RefOrchestrationUncronOperation.OPERATION_NAME, requestRefConsumer);
        return true;
    }

//    @Override
//    public void validReleaseUserExistWtss(List<String> releaseUsers,String createBy,String projectName, String username, Workspace workspace) throws Exception {
//        //如果发布权限，只有一个用户并且是创建用户，则不需要检查
//        if(releaseUsers.size() == 1 && releaseUsers.contains(createBy)){
//            return;
//        }
//        //创建人不需要检查是否存在WTSS系统
//        releaseUsers.remove(createBy);
//        //请求参数封装
//        RelProjectPrivilegeRequestRefImpl relProjectPrivilegeRequestRef = new RelProjectPrivilegeRequestRefImpl();
//        relProjectPrivilegeRequestRef.setProjectName(projectName);
//        relProjectPrivilegeRequestRef.setAccessUsers(releaseUsers);
//        relProjectPrivilegeRequestRef.setUserName(username);
//        relProjectPrivilegeRequestRef.setWorkspace(workspace);
//        //调用第三方appconn，设定工程的权限
//        InternalSchedulisAppConn schedulerAppConn = getInternalSchedulisAppConn();
//        if (schedulerAppConn == null || schedulerAppConn.getAppDesc() == null) {
//            LOGGER.error("validReleaseUserExistWtss-schedulerAppConn is null");
//            return;
//        }
//        List<AppInstance> appInstances = schedulerAppConn.getAppDesc().getAppInstances();
//        if (appInstances == null) {
//            LOGGER.error("validReleaseUserExistWtss-appInstances of schedulerAppConn is null");
//            return;
//        }
//        Map<String,String> wtssUserIdMap = schedulerAppConn.getOrCreateStructureStandard()
//                .getProjectService(appInstances.get(0))
//                .getRelProjectPrivilegeOperation()
//                .getWtssUserId(relProjectPrivilegeRequestRef);
//        if(!releaseUsers.contains(createBy)){
//            //创建人默认含有发布权限
//            releaseUsers.add(createBy);
//        }
//        LOGGER.info("wtssUserIdMap:",wtssUserIdMap);
//    }

    @Override
    public boolean setWorkflowActiveFlag(String activeFlag, Long refProjectId, String orchestratorName, String username, Workspace workspace) {
        Consumer<RefOrchestrationActiveFlagRequestRef> requestRefConsumer = requestRef -> {
            requestRef.setActiveFlag(activeFlag);
            requestRef.setRefProjectId(refProjectId);
            requestRef.setUserName(username);
            requestRef.setOrchestrationName(orchestratorName);
            requestRef.setWorkspace(workspace);
        };
        tryOptionalOperation(RefOrchestrationActiveFlagOperation.OPERATION_NAME, requestRefConsumer);
        return true;
    }

    private <R extends RequestRef, V extends ResponseRef> V tryOptionalOperation(String operationName,
                                                                                 Consumer<R> requestRefConsumer) {
        InternalSchedulerAppConn schedulerAppConn = AppConnManager.getAppConnManager().getAppConn(InternalSchedulerAppConn.class);
        List<AppInstance> appInstances = schedulerAppConn.getAppDesc().getAppInstances();
        if (CollectionUtils.isEmpty(appInstances)) {
            throw new DSSFrameworkWarnException(90222, schedulerAppConn.getClass().getSimpleName() + " has no appInstance.");
        }
        OptionalService optionalService = schedulerAppConn.getOrCreateOptionalStandard().getOptionalService(appInstances.get(0));
        OptionalOperation operation = optionalService.getOptionalOperation(operationName);
        R requestRef = RequestRefUtils.getRequestRef(operation);
        requestRefConsumer.accept(requestRef);
        V responseRef = (V) operation.apply(requestRef);
        if(responseRef.isFailed()) {
            throw new ExternalOperationWarnException(90337, responseRef.getErrorMsg());
        }
        return responseRef;
    }
}

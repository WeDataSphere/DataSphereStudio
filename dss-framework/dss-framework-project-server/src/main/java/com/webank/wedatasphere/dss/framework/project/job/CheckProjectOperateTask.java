package com.webank.wedatasphere.dss.framework.project.job;

import com.google.common.collect.Lists;
import com.webank.wedatasphere.dss.common.alter.ExecuteAlter;
import com.webank.wedatasphere.dss.common.conf.DSSCommonConf;
import com.webank.wedatasphere.dss.common.server.beans.ImsAlter;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.framework.project.dao.DssProjectOperateRecordMapper;
import com.webank.wedatasphere.dss.framework.project.dao.entity.ProjectOperateRecordDO;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateRecordStatusEnum;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateTypeEnum;
import com.webank.wedatasphere.dss.sender.service.conf.DSSSenderServiceConf;
import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.rpc.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
@EnableScheduling
public class CheckProjectOperateTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckProjectOperateTask.class);
    @Autowired
    private DssProjectOperateRecordMapper recordMapper;

    @Autowired
    private ExecuteAlter executeAlter;
    @PostConstruct
    public void checkSelfExecuteTasks() {
        LOGGER.info("CheckProjectOperateTask: Start checking for tasks that are still running after instance exceptions");
        String thisInstance = Sender.getThisInstance();
        List<ProjectOperateRecordDO> maybeFailedJobs = recordMapper.getRecordByStatus(
                Lists.newArrayList(ProjectOperateRecordStatusEnum.INIT.getCode(), ProjectOperateRecordStatusEnum.RUNNING.getCode()),
                Lists.newArrayList(ProjectOperateTypeEnum.EXPORT.getCode(), ProjectOperateTypeEnum.IMPORT.getCode(), ProjectOperateTypeEnum.PUBLISH.getCode()));
        List<ProjectOperateRecordDO> failedJobs = maybeFailedJobs.stream().filter(t -> Objects.equals(t.getInstanceName(), thisInstance))
                .peek(t -> {
                    t.setStatus(ProjectOperateRecordStatusEnum.FAILED.getCode());
                    t.setContent("系统打盹，请稍后重试！");
                    t.setCreateTime(new Date());
                }).collect(Collectors.toList());
        if (failedJobs.size() > 0) {
            LOGGER.warn("实例服务启动阶段，以下工作流因为执行实例异常导致失败！{}", DSSCommonUtils.COMMON_GSON.toJson(failedJobs));
            recordMapper.batchUpdateRecords(failedJobs);
        }
    }
    @Scheduled(cron = "#{@getCheckInstanceIsActiveCron}")
    public void checkProjectOperateTask() {

        ServiceInstance[] allActionInstances = Sender.getInstances(DSSSenderServiceConf.CURRENT_DSS_SERVER_NAME.getValue());
        List<ProjectOperateRecordDO> maybeFailedJobs = recordMapper.getRecordByStatus(
                Lists.newArrayList(ProjectOperateRecordStatusEnum.INIT.getCode(), ProjectOperateRecordStatusEnum.RUNNING.getCode()),
                Lists.newArrayList(ProjectOperateTypeEnum.EXPORT.getCode(), ProjectOperateTypeEnum.IMPORT.getCode(), ProjectOperateTypeEnum.PUBLISH.getCode()));
        LOGGER.info("These tasks are maybe failed. " + maybeFailedJobs.toString());
        List<String> activeInstance = Arrays.stream(allActionInstances).map(ServiceInstance::getInstance).collect(Collectors.toList());
        LOGGER.info("Active instances are " + activeInstance);
        List<ProjectOperateRecordDO> failedJobs = new ArrayList<>();
        if (maybeFailedJobs.size() > 0) {
            failedJobs = maybeFailedJobs.stream().filter(t -> !activeInstance.contains(t.getInstanceName()))
                    .peek(t -> {
                        t.setStatus(ProjectOperateRecordStatusEnum.FAILED.getCode());
                        t.setContent("系统打盹，请稍后重试！");
                        t.setCreateTime(new Date());
                    }).collect(Collectors.toList());
        }

        // update job status to failed
        if (failedJobs.size() > 0) {
            LOGGER.warn("以下工作流因为执行实例异常导致失败！{}", DSSCommonUtils.COMMON_GSON.toJson(failedJobs));
            recordMapper.batchUpdateRecords(failedJobs);
            List<String> exceptionInstances = failedJobs.stream().map(ProjectOperateRecordDO::getInstanceName).distinct().collect(Collectors.toList());
            List<Long> exceptionId = failedJobs.stream().map(ProjectOperateRecordDO::getId).collect(Collectors.toList());
            failedJobs.clear();
            // send alter
            ImsAlter imsAlter = new ImsAlter("DSS exception of instance: " + exceptionInstances,
                    "以下id的工作流操作失败，请到表dss_project_operate_record查看失败信息：" + exceptionId,
                    "1", DSSCommonConf.ALTER_RECEIVER.getValue());
            executeAlter.sendAlter(imsAlter);
        }
    }
}

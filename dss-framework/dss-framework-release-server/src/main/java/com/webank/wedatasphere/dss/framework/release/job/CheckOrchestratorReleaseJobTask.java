package com.webank.wedatasphere.dss.framework.release.job;

import com.webank.wedatasphere.dss.common.alter.ExecuteAlter;
import com.webank.wedatasphere.dss.common.conf.DSSCommonConf;
import com.webank.wedatasphere.dss.common.protocol.JobStatus;
import com.webank.wedatasphere.dss.common.server.beans.ImsAlter;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.framework.release.dao.ReleaseTaskMapper;
import com.webank.wedatasphere.dss.framework.release.entity.task.ReleaseTask;
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
public class CheckOrchestratorReleaseJobTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckOrchestratorReleaseJobTask.class);

    @Autowired
    private ReleaseTaskMapper releaseTaskMapper;

    @Autowired
    private ExecuteAlter executeAlter;
    @PostConstruct
    public void checkSelfExecuteTasks() {
        LOGGER.info("CheckOrchestratorReleaseJobTask: Start checking for tasks that are still running after instance exceptions");
        String thisInstance = Sender.getThisInstance();
        List<ReleaseTask> maybeFailedJobs = releaseTaskMapper.getReleaseTaskByStatus(Arrays.asList(JobStatus.Inited.getStatus(), JobStatus.Running.getStatus()));
        List<ReleaseTask> failedJobs = maybeFailedJobs.stream().filter(t -> Objects.equals(t.getInstanceName(), thisInstance))
                .peek(t -> {
                    t.setErrorMsg("执行发布的实例异常，请重新发布！");
                    t.setStatus(JobStatus.Failed.getStatus());
                    t.setUpdateTime(new Date());
                }).collect(Collectors.toList());
        if (failedJobs.size() > 0) {
            LOGGER.warn("实例服务启动阶段，以下工作流发布任务因为执行实例异常导致发布失败！{}", DSSCommonUtils.COMMON_GSON.toJson(failedJobs));
            releaseTaskMapper.batchUpdateReleaseJob(failedJobs);
        }

    }
    @Scheduled(cron = "#{@getCheckInstanceIsActiveCron}")
    public void checkOrchestratorReleaseJobTask() {

        ServiceInstance[] allActionInstances = Sender.getInstances(DSSSenderServiceConf.CURRENT_DSS_SERVER_NAME.getValue());
        List<ReleaseTask> maybeFailedJobs = releaseTaskMapper.getReleaseTaskByStatus(Arrays.asList(JobStatus.Inited.getStatus(), JobStatus.Running.getStatus()));
        LOGGER.info("These tasks are maybe failed. " + maybeFailedJobs.toString());
        List<String> activeInstance = Arrays.stream(allActionInstances).map(ServiceInstance::getInstance).collect(Collectors.toList());
        LOGGER.info("Active instances are " + activeInstance);
        List<ReleaseTask> failedJobs = new ArrayList<>();
        if (maybeFailedJobs.size() > 0) {
            for (ReleaseTask maybeFailedJob : maybeFailedJobs) {
                if (!activeInstance.contains(maybeFailedJob.getInstanceName())) {
                    maybeFailedJob.setStatus(JobStatus.Failed.getStatus());
                    maybeFailedJob.setUpdateTime(new Date());
                    maybeFailedJob.setErrorMsg("执行发布的实例异常，请重新发布！");
                    failedJobs.add(maybeFailedJob);
                }
            }
        }

        // update publish job status to failed
        if (failedJobs.size() > 0) {
            LOGGER.warn("以下工作流发布任务因为执行实例异常导致发布失败！{}", DSSCommonUtils.COMMON_GSON.toJson(failedJobs));
            releaseTaskMapper.batchUpdateReleaseJob(failedJobs);
            List<String> exceptionInstances = failedJobs.stream().map(ReleaseTask::getInstanceName).distinct().collect(Collectors.toList());
            List<Long> exceptionId = failedJobs.stream().map(ReleaseTask::getId).collect(Collectors.toList());
            failedJobs.clear();
            // send alter
            ImsAlter imsAlter = new ImsAlter("DSS exception of instance: " + exceptionInstances,
                    "以下id的工作流发布失败，请到表dss_release_task查看失败的工作流信息：" + exceptionId,
                    "1", DSSCommonConf.ALTER_RECEIVER.getValue());
            executeAlter.sendAlter(imsAlter);
        }
    }
}

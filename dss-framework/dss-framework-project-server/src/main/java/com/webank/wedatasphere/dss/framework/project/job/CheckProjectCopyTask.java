package com.webank.wedatasphere.dss.framework.project.job;

import com.webank.wedatasphere.dss.common.alter.ExecuteAlter;
import com.webank.wedatasphere.dss.common.conf.DSSCommonConf;
import com.webank.wedatasphere.dss.common.server.beans.ImsAlter;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.framework.project.dao.DSSProjectCopyTaskMapper;
import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectCopyTask;
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
public class CheckProjectCopyTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckProjectCopyTask.class);
    @Autowired
    private DSSProjectCopyTaskMapper dssProjectCopyTaskMapper;

    @Autowired
    private ExecuteAlter executeAlter;

    @PostConstruct
    public void checkSelfExecuteTasks() {
        LOGGER.info("CheckProjectCopyTask: Start checking for tasks that are still running after instance exceptions");
        String thisInstance = Sender.getThisInstance();
        LOGGER.info("实例服务启动阶段的实例名为：{}", thisInstance);
        List<DSSProjectCopyTask> maybeFailedJobs = dssProjectCopyTaskMapper.selectTaskByStatus();
        List<DSSProjectCopyTask> failedJobs = maybeFailedJobs.stream().filter(t -> Objects.equals(t.getInstanceName(), thisInstance))
                        .peek(t -> {
                            t.setErrorMsg("执行复制的实例异常，请稍后重试！");
                            t.setStatus(3);
                            t.setUpdateTime(new Date());
                        }).collect(Collectors.toList());
        if (failedJobs.size() > 0) {
            LOGGER.warn("实例服务启动阶段，以下项目复制任务因为执行实例异常导致失败！{}", DSSCommonUtils.COMMON_GSON.toJson(failedJobs));
            dssProjectCopyTaskMapper.batchUpdateCopyTask(failedJobs);
        }
    }

    @Scheduled(cron = "#{@getCheckInstanceIsActiveCron}")
    public void checkProjectCopyTask() {

        ServiceInstance[] allActionInstances = Sender.getInstances(DSSSenderServiceConf.CURRENT_DSS_SERVER_NAME.getValue());
        List<DSSProjectCopyTask> maybeFailedJobs = dssProjectCopyTaskMapper.selectTaskByStatus();
        LOGGER.info("These tasks are maybe failed. " + maybeFailedJobs.toString());
        List<String> activeInstance = Arrays.stream(allActionInstances).map(ServiceInstance::getInstance).collect(Collectors.toList());
        LOGGER.info("Active instances are " + activeInstance);
        List<DSSProjectCopyTask> failedJobs = new ArrayList<>();
        if (maybeFailedJobs.size() > 0) {
            failedJobs = maybeFailedJobs.stream().filter(t -> !activeInstance.contains(t.getInstanceName()))
                    .peek(t -> {
                        t.setErrorMsg("执行复制的实例异常，请稍后重试！");
                        t.setStatus(3);
                        t.setUpdateTime(new Date());
                    }).collect(Collectors.toList());
        }

        // update copy job status to failed
        if (failedJobs.size() > 0) {
            LOGGER.warn("以下项目复制任务因为执行实例异常导致失败！{}", DSSCommonUtils.COMMON_GSON.toJson(failedJobs));
            dssProjectCopyTaskMapper.batchUpdateCopyTask(failedJobs);
            List<String> exceptionInstances = failedJobs.stream().map(DSSProjectCopyTask::getInstanceName).distinct().collect(Collectors.toList());
            List<Long> exceptionId = failedJobs.stream().map(DSSProjectCopyTask::getId).collect(Collectors.toList());
            failedJobs.clear();
            // send alter
            ImsAlter imsAlter = new ImsAlter("DSS exception of instance: " + exceptionInstances,
                    "以下Id的项目复制失败，请到表dss_project_copy_task查看复制失败的项目信息：" + exceptionId,
                    "1", DSSCommonConf.ALTER_RECEIVER.getValue());
            executeAlter.sendAlter(imsAlter);
        }
    }
}

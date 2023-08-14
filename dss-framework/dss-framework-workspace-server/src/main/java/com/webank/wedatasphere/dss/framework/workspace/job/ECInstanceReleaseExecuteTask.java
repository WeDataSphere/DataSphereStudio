package com.webank.wedatasphere.dss.framework.workspace.job;

import com.webank.wedatasphere.dss.common.conf.DSSCommonConf;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.client.ResourceManageClient;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.ECInstanceKillRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.ECInstanceRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.vo.QueueInfo;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECKillHistoryRecord;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECReleaseStrategy;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECReleaseStrategyJob;
import com.webank.wedatasphere.dss.framework.workspace.dao.DSSWorkspaceECReleaseStrategyMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECReleaseStrategyDO;
import com.webank.wedatasphere.dss.framework.workspace.service.ECReleaseSummaryInfoNotificationService;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceECKillHistoryService;
import com.webank.wedatasphere.dss.framework.workspace.strategy.ECKillingPriorityQueue;
import com.webank.wedatasphere.dss.framework.workspace.strategy.ECKillingPriorityQueueFactory;
import org.apache.linkis.rpc.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.webank.wedatasphere.dss.framework.compute.resource.manager.client.ResourceManageClient.ENGINE_TYPE_IN_YARN_QUEUE;

/**
 * ec实例自动释放定时任务
 * Author: xlinliu
 * Date: 2023/4/23
 */
@Component
@EnableScheduling
public class ECInstanceReleaseExecuteTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ECInstanceReleaseExecuteTask.class);
    /**
     * 本机任务运行状态。
     */
    private static volatile boolean isRunning = false;
    /**
     * 规则轮训周期，单位秒
     */
    public static final Integer KILL_PERIOD=DSSCommonConf.DSS_EC_KILL_PERIOD.getValue();
    /**
     * 分布式锁最大超时时间的周期数
     */
    public static final Integer KILL_TIME_OUT_PERIOD_EPOCH=3;
    public static  final String KILLER_NAME="system";


    @Autowired
    DSSWorkspaceECReleaseStrategyMapper dssWorkspaceECReleaseStrategyMapper;
    @Autowired
    ResourceManageClient resourceManageClient;
    @Autowired
    DSSWorkspaceECKillHistoryService dssWorkspaceECKillHistoryService;

    @Autowired
    ECReleaseSummaryInfoNotificationService notificationService;

    @Scheduled(cron = "#{@getECInstanceReleaseCron}")
    public void executeStrategy(){
        if(isRunning){
            LOGGER.info("last period is in running,so skip this task");
            return;
        }else{
            // lock
            isRunning=true;
        }
        try {
            String thisInstanceName = Sender.getThisInstance();
            if (StringUtils.isEmpty(thisInstanceName)) {
                LOGGER.info("instance is not ready,ignore this task execute");
                return;
            }
            LOGGER.info("start to execute ec release task.execute instance name:{}", thisInstanceName);
            String operator = KILLER_NAME;
            List<ECReleaseStrategyJob> strategies = getNeedProcessingStrategyList();
            for (ECReleaseStrategyJob strategy : strategies) {
                String strategyId = strategy.getStrategyId();
                String expectExecuteInstance = strategy.getExecuteInstance();
                Date expectExecuteTime = strategy.getExecuteTime();
                LOGGER.info("try execute ec release strategy. strategyId:{},strategyName:{}", strategyId, strategy.getName());
                //更新成功则说明抢锁成功
                if (dssWorkspaceECReleaseStrategyMapper.casSwapExecuteInstance(strategyId, expectExecuteInstance
                        , expectExecuteTime, thisInstanceName) > 0) {
                    try {
                        LOGGER.info("start to execute ec release strategy.  {}", DSSCommonUtils.COMMON_GSON.toJson(strategy));
                        processStrategy(strategy, operator, thisInstanceName);
                        LOGGER.info("success to execute ec release strategy. strategyId:{},strategyName:{}", strategyId, strategy.getName());
                    } catch (Exception e) {
                        String msg = String.format("fail to execute ec release strategy. strategyId:%s,strategyName:%s", strategyId, strategy.getName());
                        LOGGER.error(msg, e);
                    } finally {
                        dssWorkspaceECReleaseStrategyMapper.cleanExecuteInfo(strategyId);
                    }
                }
            }
        }finally {
            //unlock
            isRunning=false;
        }
    }

    /**
     * 获取需要被处理的释放规则
     */
    private List<ECReleaseStrategyJob> getNeedProcessingStrategyList(){
        List<ECReleaseStrategyDO> dos= dssWorkspaceECReleaseStrategyMapper.getStrategiesNeedProcessing(KILL_PERIOD*KILL_TIME_OUT_PERIOD_EPOCH);
        return dos.stream().map(ECReleaseStrategyJob::fromDO).collect(Collectors.toList());
    }
    private void processStrategy(ECReleaseStrategy strategy, String operator,String executeInstance){
        //第一步，查询队列当前的状态
        String queueName = strategy.getQueue();
        QueueInfo queueInfo= resourceManageClient.getQueueInfo(queueName,operator);
        int maxCores = Optional.ofNullable(queueInfo)
                .map(QueueInfo::getMaxResources)
                .map(QueueInfo.Resource::getCores)
                .orElseThrow(() -> new DSSRuntimeException("fetch queue info from linkis failed."));
        long maxMemory=Optional.ofNullable(queueInfo)
                .map(QueueInfo::getMaxResources)
                .map(QueueInfo.Resource::getMemory)
                .orElseThrow(() -> new DSSRuntimeException("fetch queue info from linkis failed."));
        int usedCores=Optional.ofNullable(queueInfo)
                .map(QueueInfo::getUsedResources)
                .map(QueueInfo.Resource::getCores)
                .orElseThrow(() -> new DSSRuntimeException("fetch queue info from linkis failed."));
        long usedMemory=Optional.ofNullable(queueInfo)
                .map(QueueInfo::getUsedResources)
                .map(QueueInfo.Resource::getMemory)
                .orElseThrow(() -> new DSSRuntimeException("fetch queue info from linkis failed."));
        LOGGER.info("get queue info successfully. queue name:{}",queueInfo.getQueuename().getQueueName());
        //第二步，判断队列是否达到规则触发条件
        ECReleaseStrategy.Condition triggerCondition = strategy.getTriggerConditionConf();
        ECReleaseStrategy.Condition terminateCondition= strategy.getTerminateConditionConf();
        if (satisfy(maxCores, maxMemory, usedCores, usedMemory, triggerCondition, true)) {
            LOGGER.info("satisfy strategy trigger condition,begin to kill ec instances. queueName:{},maxCores:{},maxMemory:{},usedCores:{},usedMemory:{}",queueName,maxCores, maxMemory, usedCores, usedMemory);
            List<String> engineTypes= new ArrayList<>(ENGINE_TYPE_IN_YARN_QUEUE);
            List<String> ecStatus = Collections.singletonList("Unlock");
            ECInstanceRequest ecInstanceRequest = new ECInstanceRequest();
            ecInstanceRequest.setEngineType(engineTypes);
            ecInstanceRequest.setStatus(ecStatus);
            ecInstanceRequest.setYarnQueue(queueName);
            List<ECKillHistoryRecord> ecInstanceList = resourceManageClient.fetchECInstance(ecInstanceRequest, operator).stream()
                    .filter(e -> e.getUseResource() != null && e.getUseResource().getYarn() != null && queueName.equals(e.getUseResource().getYarn().getQueueName()))
                    .map(e -> ECKillHistoryRecord.convertECInstance2ECKillHistoryRecord(e, strategy.getWorkspaceId(), strategy.getStrategyId(), operator))
                    .collect(Collectors.toList());
            LOGGER.info("fetch ec instance list successfully. ec instance list size:{}", ecInstanceList.size());
            ECKillingPriorityQueue priorityQueue = ECKillingPriorityQueueFactory.getUserFairInstance(ecInstanceList);
            LOGGER.info("fetch ec instance list successfully. ec instance list size:{}", ecInstanceList.size());
            List<ECKillHistoryRecord> killHistoryRecordList = new ArrayList<>();
            List<ECInstanceKillRequest> ecInstanceKillRequestList = new ArrayList<>();
            //（1）未到达终止条件，（2）且还有需要被处理的ec，两个条件同时满足才继续循环。
            while( !satisfy(maxCores, maxMemory, usedCores, usedMemory,terminateCondition,false)
                    && priorityQueue.hasNext()){
                ECKillHistoryRecord ecInstance=priorityQueue.poll();
                LOGGER.info("add ec to killing list. ec name:{}", ecInstance.getInstance());
                ecInstanceKillRequestList.add(new ECInstanceKillRequest(ecInstance.getInstance(), ecInstance.getOwner()));
                ecInstance.setExecuteInstance(executeInstance);
                killHistoryRecordList.add(ecInstance);
                usedCores -= ecInstance.getYarn().getQueueCpu();
                usedMemory -= ecInstance.getYarn().getQueueMemory();
                LOGGER.info(" the yarn info after killing:usedCores:{},usedMemory:{} ec name:{}", usedCores,usedMemory,ecInstance.getInstance());
            }
            if(!killHistoryRecordList.isEmpty()){
                LOGGER.info("start to batch kill ec list.");
                resourceManageClient.batchKillECInstance(ecInstanceKillRequestList,operator);
                LOGGER.info("batch kill ec list successfully.");
                dssWorkspaceECKillHistoryService.batchAddEcKillRecord(killHistoryRecordList);
                LOGGER.info("batch record ec list kill history successfully.");
            }
        }
        //第三步，尝试发送本规则的清理归总信息到ims系统。
        notificationService.tryNotify(strategy);
    }

    /**
     * 类封装转化
     * @param ecInstance linkis中拉的ec实例
     * @return kill里的实例
     */


    /**
     * 判断是否满足条件
     * @param maxCores 队列核数容量
     * @param maxMemory 队列内存容量
     * @param usedCores 已使用核数
     * @param usedMemory 已使用内存
     * @param condition 规则条件
     * @param isTrigger true为触发条件，end为终止条件
     * @return 是否满足条件
     */
    private boolean satisfy(int maxCores, Long maxMemory,
                            int usedCores, long usedMemory,
                            ECReleaseStrategy.Condition condition,
                            boolean isTrigger){
        //第一步，解析条件值
        Boolean cpuSatisfy=null;
        Boolean memorySatisfy = null;
        for (ECReleaseStrategy.SimpleCondition simpleCondition : condition.getConditions()) {
            if("cpu".equalsIgnoreCase( simpleCondition.getField())){
                double  cpuPercentCondition= simpleCondition.getValue();
                cpuSatisfy= isTrigger ?
                                cpuPercentCondition / 100.0 * maxCores <= usedCores :
                                cpuPercentCondition / 100.0 * maxCores >= usedCores;
            }
            if("memory".equalsIgnoreCase(simpleCondition.getField())){
                double memoryPercentCondition = simpleCondition.getValue();
                memorySatisfy= isTrigger ?
                                memoryPercentCondition / 100.0 * maxMemory <= usedMemory :
                                memoryPercentCondition / 100.0 * maxMemory >= usedMemory;
            }
        }
        //只有cpu条件
        if(cpuSatisfy!=null&&memorySatisfy==null){
            return cpuSatisfy;
        }
        //只有内存条件
        else if(cpuSatisfy==null&&memorySatisfy!=null){
            return memorySatisfy;
        }
        //都有
        else if(cpuSatisfy!=null&&memorySatisfy!=null){
            if("and".equalsIgnoreCase( condition.getRelation())){
                return cpuSatisfy && memorySatisfy;
            } else if ("or".equalsIgnoreCase( condition.getRelation())) {
                return cpuSatisfy || memorySatisfy;
            }else{
                throw new DSSRuntimeException(" unsupported release strategy condition relation"+condition.getRelation());
            }
        }
        else{
            throw new DSSRuntimeException("release strategy condition can not be empty");
        }
    }
}

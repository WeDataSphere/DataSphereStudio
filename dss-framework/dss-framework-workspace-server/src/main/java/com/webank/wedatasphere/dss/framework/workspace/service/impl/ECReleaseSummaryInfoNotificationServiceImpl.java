package com.webank.wedatasphere.dss.framework.workspace.service.impl;

import com.webank.wedatasphere.dss.common.alter.ExecuteAlter;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.server.beans.ImsAlter;
import com.webank.wedatasphere.dss.common.server.enums.ImsWarnTypeEnum;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECReleaseStrategy;
import com.webank.wedatasphere.dss.framework.workspace.dao.DSSWorkspaceEcKillHistoryMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.DSSWorkspaceEcReleaseImsRecordMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECKillSumHistoryRecordDO;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECReleaseImsRecordDO;
import com.webank.wedatasphere.dss.framework.workspace.service.ECReleaseSummaryInfoNotificationService;
import org.apache.linkis.rpc.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

/**
 * Author: xlinliu
 * Date: 2023/4/21
 */
@Service
public class ECReleaseSummaryInfoNotificationServiceImpl implements ECReleaseSummaryInfoNotificationService {

    private final static Logger logger = LoggerFactory.getLogger(ECReleaseSummaryInfoNotificationServiceImpl.class);

    @Autowired
    private DSSWorkspaceEcReleaseImsRecordMapper imsRecordMapper;

    @Autowired
    private DSSWorkspaceEcKillHistoryMapper dssWorkspaceEcKillHistoryMapper;

    @Autowired
    private ExecuteAlter executeAlter;

    private final static int NOT_SEND_STATUS = 0;
    private final static int HAD_SEND_STATUS = 1;
    private final static int HAD_SEND_FAILD = 2;
    private final static int STRATEGY_DISABLED = 0;
    private final static ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = ThreadLocal.withInitial(()->new SimpleDateFormat("yyyy-MM-dd HH:mm:00"));

    /**
     * 查询dss_ec_release_ims_record表是否存在3min（以配置文件配置为准）以内的告警记录
     *    存在 =》 返回false
     *    不存在 => 1.根据ecReleaseStrategyId查询dss_ec_kill_history表，按照模板组装数据，
     *             2.插入到dss_ec_release_ims_record表中，发送告警
     *             3.更新dss_ec_release_ims_record表status字段状态为：已发送
     *
     * @param releaseStrategy
     * @return
     */
    @Override
    public boolean tryNotify(ECReleaseStrategy releaseStrategy) {
        logger.info("call tryNotify, releaseStrategy params: {}", DSSCommonUtils.COMMON_GSON.toJson(releaseStrategy));
        // 解析ims配置
        ECReleaseStrategy.IMSConf imsConf = releaseStrategy.getImsConf();
        if(null == imsConf){
            logger.error("ims rule could not be empty!");
            throw new DSSRuntimeException("ims rule could not be empty!");
        }
        if(!imsConf.getEnable()){
            return false;
        }
        //校验规则是否被禁用,禁用则不发送告警
        if(releaseStrategy.getStatus() == STRATEGY_DISABLED){
            logger.info("strategy status is disabled!");
            return false;
        }
        //查询间隔时间之内是否存在ims发送记录
        List<ECReleaseImsRecordDO> ecReleaseImsRecordDOS = imsRecordMapper.selectRecordIntervalTime(imsConf.getDuration(), releaseStrategy.getStrategyId());
        if (!CollectionUtils.isEmpty(ecReleaseImsRecordDOS)) {
            //特定时间以内存在告警记录，则不发送
            logger.info("规则：{}，存在 {} 分钟以内的ims发送记录，不予再次发送!", releaseStrategy.getStrategyId(), imsConf.getDuration());
            return false;
        }

        //统计指定间隔时间内的ec kill记录
        List<ECKillSumHistoryRecordDO> ecKillSumHistoryRecordDOS = dssWorkspaceEcKillHistoryMapper.sumECKillInfoByStrategyId(releaseStrategy.getStrategyId(), imsConf.getDuration());
        if(CollectionUtils.isEmpty(ecKillSumHistoryRecordDOS)){
            logger.info("规则：{}，在 {} 分钟内不存在kill记录!", releaseStrategy.getStrategyId(), imsConf.getDuration());
            return false;
        }

        //起止时间
        String endTime = SIMPLE_DATE_FORMAT.get().format(System.currentTimeMillis());
        String startTime = SIMPLE_DATE_FORMAT.get().format(System.currentTimeMillis() - imsConf.getDuration() * 1000 * 60L);
        //工作空间id
        Long workspaceId = ecKillSumHistoryRecordDOS.stream().map(ECKillSumHistoryRecordDO::getWorkspaceId).findFirst().get();
        //队列
        String queue = ecKillSumHistoryRecordDOS.stream().map(ECKillSumHistoryRecordDO::getQueue).findFirst().get();

        //插入到dss_ec_release_ims_record
        ECReleaseImsRecordDO imsRecordDO = new ECReleaseImsRecordDO();
        imsRecordDO.setRecordId(UUID.randomUUID().toString());
        imsRecordDO.setStrategyId(releaseStrategy.getStrategyId());
        imsRecordDO.setWorkspaceId(workspaceId);
        imsRecordDO.setContent(DSSCommonUtils.COMMON_GSON.toJson(ecKillSumHistoryRecordDOS));
        imsRecordDO.setExecuteInstance(Sender.getThisInstance());
        imsRecordDO.setStatus(NOT_SEND_STATUS);
        imsRecordMapper.insert(imsRecordDO);

        //发送发告警
        ImsAlter imsAlter = new ImsAlter(queue + "队列空闲引擎释放告警", buildImsContent(ecKillSumHistoryRecordDOS, queue, startTime, endTime), ImsWarnTypeEnum.getCodeByType(imsConf.getLevel()),String.join(",", imsConf.getReceiver()));
        try {
            executeAlter.sendAlter(imsAlter);
            //更新status字段状态状态
            imsRecordMapper.updateImsRecordStatus(HAD_SEND_STATUS, imsRecordDO.getRecordId());
        } catch (Exception e) {
            logger.error("send ims alert error!");
            //更新失败
            imsRecordMapper.updateImsRecordStatus(HAD_SEND_FAILD, imsRecordDO.getRecordId());
        }
        return true;
    }

    private String buildImsContent(List<ECKillSumHistoryRecordDO> ecKillSumHistoryRecordDOS, String queue, String startTime, String endTime){
        StringBuilder sb = new StringBuilder("");
        sb.append("队列名称:").append(queue).append("，\n");
        sb.append("告警周期内请求释放的引擎信息如下：\n");
        sb.append("周期起止时间：").append(startTime).append("~").append(endTime).append("\n");
        sb.append("|用户|kill引擎数|释放CPU数|释放内存数（GB）|引擎空闲时长总和（秒）|\n");
        ecKillSumHistoryRecordDOS.forEach(item ->{
            sb.append("|");
            sb.append(item.getOwner()).append("|");
            sb.append(item.getInstances()).append("|");
            sb.append(item.getYarnCores()).append("|");
            //byte转GB
            sb.append(item.getYarnMemories()/Math.pow(1024, 3)).append("|");
            sb.append(item.getUnlockDurations()).append("|");
            sb.append("\n");
        });
        return sb.toString();
    }

}

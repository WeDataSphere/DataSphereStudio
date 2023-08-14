package com.webank.wedatasphere.dss.framework.workspace.bean;


import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response.ECInstance;
import com.webank.wedatasphere.dss.framework.workspace.bean.vo.ECKillHistoryRecordVO;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECKillHistoryRecordDO;
import org.apache.linkis.common.utils.ByteTimeUtils;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.Locale;
import java.util.Optional;

/**
 * 引擎释放历史记录
 * Author: xlinliu
 * Date: 2023/4/20
 */
public class ECKillHistoryRecord {
    /**
     * 释放规则id
     */
    private String strategyId;

    /**
     * 工作空间id
     */
    private Long workspaceId;
    /**
     * 引擎实例名
     */
    private String instance;
    /**
     * 引擎类型
     */
    private String engineType;

    /**
     * yarn资源
     */
    private YarnInfo yarn;
    /**
     * 本地资源
     */
    private DriverInfo driver;

    /**
     * 创建者
     */
    private String owner;
    /**
     * 启动时间
     */
    private String createTime;

    private String killer;
    private Date killTime;
    /**
     * 空闲时长，单位毫秒
     */
    private Long  unlockDuration;

    /**
     * 负责发送的服务实例
     */
    private String executeInstance;


    /**
     * DO转domain

     */
    public static ECKillHistoryRecord fromDO(ECKillHistoryRecordDO recordDO){
        ECKillHistoryRecord record=new ECKillHistoryRecord();
        BeanUtils.copyProperties(recordDO,record);
        YarnInfo yarnInfo=new YarnInfo();
        yarnInfo.setQueueName(recordDO.getQueue());
        yarnInfo.setQueueCpu(recordDO.getYarnCore());
        yarnInfo.setQueueMemory(recordDO.getYarnMemory());
        yarnInfo.setInstance(1);
        record.setYarn(yarnInfo);

        DriverInfo driverInfo = new DriverInfo();
        driverInfo.setCpu(recordDO.getDriverCore());
        driverInfo.setMemory(recordDO.getDriverMemory());
        driverInfo.setInstance(1);
        record.setDriver(driverInfo);

        record.setCreateTime(recordDO.getEcStartTime());
        return record;
    }

    /**
     * 转DO对象
     */
    public ECKillHistoryRecordDO toDO(){
        ECKillHistoryRecordDO recordDO=new ECKillHistoryRecordDO();
        BeanUtils.copyProperties(this,recordDO);
        YarnInfo yarnInfo = this.getYarn();
        recordDO.setQueue(yarnInfo.getQueueName());
        recordDO.setYarnCore(yarnInfo.getQueueCpu());
        recordDO.setYarnMemory(yarnInfo.getQueueMemory());

        DriverInfo driverInfo = this.getDriver();
        recordDO.setDriverCore(driverInfo.getCpu());
        recordDO.setDriverMemory(driverInfo.getMemory());
        recordDO.setEcStartTime(this.getCreateTime());
        return recordDO;
    }

    public ECKillHistoryRecordVO toVO(){
        ECKillHistoryRecordVO recordVO = new ECKillHistoryRecordVO();
        BeanUtils.copyProperties(this, recordVO);
        //本地内存显示值
        ECKillHistoryRecordVO.DriverInfo voDriverInfo=new ECKillHistoryRecordVO.DriverInfo();
        BeanUtils.copyProperties(this.getDriver(),voDriverInfo);
        voDriverInfo.setMemory(bytesToString(this.getDriver().getMemory()));
        recordVO.setDriver(voDriverInfo);
        //yarn内存显示值
        ECKillHistoryRecordVO.YarnInfo voYarnInfo=new ECKillHistoryRecordVO.YarnInfo();
        BeanUtils.copyProperties(this.getYarn(), voYarnInfo);
        voYarnInfo.setQueueMemory(bytesToString(this.getYarn().getQueueMemory()));
        recordVO.setYarn(voYarnInfo);
        //空闲时长显示值
        String unLockDurationStr= ByteTimeUtils.msDurationToString(this.getUnlockDuration());
        recordVO.setUnlockDuration(unLockDurationStr);
        return recordVO;
    }

    /**
     * 把linkis的ec实例结果，转成kill记录结果。
     * @param ecInstance linkis  ec实例
     * @param workspaceId 工作空间名
     * @param strategyId 策略id
     * @param killer kill请求触发者
     */
    public static ECKillHistoryRecord convertECInstance2ECKillHistoryRecord(ECInstance ecInstance, Long workspaceId, String strategyId, String killer){
        ECKillHistoryRecord ecKillHistoryRecord=new ECKillHistoryRecord();
        BeanUtils.copyProperties(ecInstance,ecKillHistoryRecord);
        long unlockDuration = ecInstance.getLastUnlockTimestamp() != null && ecInstance.getLastUnlockTimestamp() > 0 ?
                System.currentTimeMillis() - ecInstance.getLastUnlockTimestamp() : 0;
        ecKillHistoryRecord.setUnlockDuration(unlockDuration);
        DriverInfo driverInfo = new DriverInfo();
        if(ecInstance.getUseResource()!=null&&ecInstance.getUseResource().getDriver()!=null) {
            BeanUtils.copyProperties(ecInstance.getUseResource().getDriver(), driverInfo);
            Long driverMemory = Optional.ofNullable(ecInstance.getUseResource())
                    .map(ECInstance.Resource::getDriver).map(ECInstance.DriverInfo::getMemory).map(ECKillHistoryRecord::parseByteString).orElse(0L);
            driverInfo.setMemory(driverMemory);
        }
        ecKillHistoryRecord.setDriver(driverInfo);
        YarnInfo yarnInfo = new YarnInfo();
        if(ecInstance.getUseResource()!=null&&ecInstance.getUseResource().getYarn()!=null) {
            BeanUtils.copyProperties(ecInstance.getUseResource().getYarn(), yarnInfo);
            Long yarnMemory = Optional.ofNullable(ecInstance.getUseResource())
                    .map(ECInstance.Resource::getYarn).map(ECInstance.YarnInfo::getQueueMemory).map(ECKillHistoryRecord::parseByteString).orElse(0L);
            yarnInfo.setQueueMemory(yarnMemory);
        }
        ecKillHistoryRecord.setYarn(yarnInfo);
        ecKillHistoryRecord.setWorkspaceId(workspaceId);
        ecKillHistoryRecord.setKiller(killer);
        ecKillHistoryRecord.setStrategyId(strategyId);
        return ecKillHistoryRecord;
    }



    private static long parseByteString(String str){
        str = str.trim();
        double numberValue = Double.parseDouble(str.substring(0, str.length() - 2).trim());
        if(str.endsWith("TB")||str.endsWith("tb")){
            return (long) numberValue *(1L << 40);
        }
        else if(str.endsWith("GB")||str.endsWith("gb")){
            return (long) numberValue *(1L << 30);
        }else if(str.endsWith("MB")||str.endsWith("mb")){
            return (long) numberValue *(1L << 20);
        }else if(str.endsWith("KB")||str.endsWith("kb")){
            return (long) numberValue *(1L << 10);
        }else{
            return (long) numberValue;
        }
    }

    private static String bytesToString(long size) {
        long TB = 1L << 40;
        long GB = 1L << 30;
        long MB = 1L << 20;
        long KB = 1L << 10;

        double value;
        String unit;
        if (size >= 2 * TB || -2 * TB >= size) {
            value = size * 1f / TB;
            unit = "TB";
        } else if (size >= 2 * GB || -2 * GB >= size) {
            value = size * 1f / GB;
            unit = "GB";
        } else if (size >= 2 * MB || -2 * MB >= size) {
            value = size * 1f / MB;
            unit = "MB";
        } else if (size >= 2 * KB || -2 * KB >= size) {
            value = size * 1f / KB;
            unit = "KB";
        } else {
            value = size * 1f;
            unit = "B";
        }
        return String.format(Locale.US, "%.1f %s", value, unit);
    }


    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public YarnInfo getYarn() {
        return yarn;
    }

    public void setYarn(YarnInfo yarn) {
        this.yarn = yarn;
    }

    public DriverInfo getDriver() {
        return driver;
    }

    public void setDriver(DriverInfo driver) {
        this.driver = driver;
    }

    public String getKiller() {
        return killer;
    }

    public void setKiller(String killer) {
        this.killer = killer;
    }

    public Date getKillTime() {
        return killTime;
    }

    public void setKillTime(Date killTime) {
        this.killTime = killTime;
    }

    public Long getUnlockDuration() {
        return unlockDuration;
    }

    public void setUnlockDuration(Long unlockDuration) {
        this.unlockDuration = unlockDuration;
    }

    public String getExecuteInstance() {
        return executeInstance;
    }

    public void setExecuteInstance(String executeInstance) {
        this.executeInstance = executeInstance;
    }

    public static class DriverInfo {
        private Integer cpu = 0;
        private Integer instance = 0;
        /**
         * 单位 Byte
         */
        private Long memory = 0L;

        public Integer getCpu() {
            return cpu;
        }

        public void setCpu(Integer cpu) {
            this.cpu = cpu;
        }

        public Integer getInstance() {
            return instance;
        }

        public void setInstance(Integer instance) {
            this.instance = instance;
        }

        public Long getMemory() {
            return memory;
        }

        public void setMemory(Long memory) {
            this.memory = memory;
        }
    }

    public static class YarnInfo {
        private String queueName = "";
        /**
         * 单位 Byte
         */
        private Long queueMemory = 0L;
        private Integer instance = 0;
        private Integer queueCpu = 0;

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public Long getQueueMemory() {
            return queueMemory;
        }

        public void setQueueMemory(Long queueMemory) {
            this.queueMemory = queueMemory;
        }

        public Integer getInstance() {
            return instance;
        }

        public void setInstance(Integer instance) {
            this.instance = instance;
        }

        public Integer getQueueCpu() {
            return queueCpu;
        }

        public void setQueueCpu(Integer queueCpu) {
            this.queueCpu = queueCpu;
        }
    }
}
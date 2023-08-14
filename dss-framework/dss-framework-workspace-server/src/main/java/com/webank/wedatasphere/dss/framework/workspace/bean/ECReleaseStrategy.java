package com.webank.wedatasphere.dss.framework.workspace.bean;

import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECReleaseStrategyDO;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

/**
 * 引擎释放规则配置
 * Author: xlinliu
 * Date: 2023/4/17
 */
public class ECReleaseStrategy {
    /**
     * 规则id，全局唯一
     */
    private String strategyId;

    private Long workspaceId;
    /**
     * 规则名
     */
    private String name;

    private String description;
    /**
     * 规则关联的队列
     */
    private String queue;
    /**
     * 触发条件
     */
    private Condition triggerConditionConf;
    /**
     * 终止条件
     */
    private Condition terminateConditionConf;
    /**
     * 告警信息
     */
    private IMSConf imsConf;
    private String creator;
    private String modifier;
    private Date createTime;
    private Date modifyTime;
    /**
     * 规则状态：0 禁用中，1 启用中
     */
    private int status;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public Condition getTriggerConditionConf() {
        return triggerConditionConf;
    }

    public void setTriggerConditionConf(Condition triggerConditionConf) {
        this.triggerConditionConf = triggerConditionConf;
    }

    public Condition getTerminateConditionConf() {
        return terminateConditionConf;
    }

    public void setTerminateConditionConf(Condition terminateConditionConf) {
        this.terminateConditionConf = terminateConditionConf;
    }

    public IMSConf getImsConf() {
        return imsConf;
    }

    public void setImsConf(IMSConf imsConf) {
        this.imsConf = imsConf;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    /**
     * 从Do持久化对象活化过来
     */
    public static ECReleaseStrategy fromDO(ECReleaseStrategyDO strategyDO){
        ECReleaseStrategy strategy=new ECReleaseStrategy();
        BeanUtils.copyProperties(strategyDO,strategy);
        strategy.setTriggerConditionConf(DSSCommonUtils.COMMON_GSON.fromJson(strategyDO.getTriggerConditionConf(), Condition.class));
        strategy.setTerminateConditionConf(DSSCommonUtils.COMMON_GSON.fromJson(strategyDO.getTerminateConditionConf(), Condition.class));
        strategy.setImsConf(DSSCommonUtils.COMMON_GSON.fromJson(strategyDO.getImsConf(), IMSConf.class));
        return strategy;
    }

    /**
     * 转为DO持久化对象
     */
    public ECReleaseStrategyDO toDO(){
        ECReleaseStrategyDO strategyDO=new ECReleaseStrategyDO();
        BeanUtils.copyProperties(this,strategyDO);
        strategyDO.setTriggerConditionConf(DSSCommonUtils.COMMON_GSON.toJson(this.getTriggerConditionConf()));
        strategyDO.setTerminateConditionConf(DSSCommonUtils.COMMON_GSON.toJson(this.getTerminateConditionConf()));
        strategyDO.setImsConf(DSSCommonUtils.COMMON_GSON.toJson(this.getImsConf()));
        return strategyDO;
    }






    public static class Condition{
       private String relation;
       private List<SimpleCondition> conditions;

        public String getRelation() {
            return relation;
        }

        public void setRelation(String relation) {
            this.relation = relation;
        }

        public List<SimpleCondition> getConditions() {
            return conditions;
        }

        public void setConditions(List<SimpleCondition> conditions) {
            this.conditions = conditions;
        }
    }
    public static class SimpleCondition{
        private String field;
        private String operation;
        private Double value;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getOperation() {
            return operation;
        }

        public void setOperation(String operation) {
            this.operation = operation;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }

    public static class IMSConf{
        private String level;
        private boolean enable = false;
        private int duration;
        private List<String> receiver;

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public boolean getEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public List<String> getReceiver() {
            return receiver;
        }

        public void setReceiver(List<String> receiver) {
            this.receiver = receiver;
        }
    }
}

package com.webank.wedatasphere.dss.framework.workspace.bean;

import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECReleaseStrategyDO;
import org.springframework.beans.BeanUtils;

import java.util.Date;

/**
 * 释放规则规则执行任务
 * Author: xlinliu
 * Date: 2023/4/23
 */
public class ECReleaseStrategyJob extends ECReleaseStrategy{
    /**
     * 执行实例
     */
    private String executeInstance;
    /**
     * 执行时间
     */
    private Date executeTime;

    public String getExecuteInstance() {
        return executeInstance;
    }

    public void setExecuteInstance(String executeInstance) {
        this.executeInstance = executeInstance;
    }

    public Date getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(Date executeTime) {
        this.executeTime = executeTime;
    }

    public static ECReleaseStrategyJob fromDO(ECReleaseStrategyDO strategyDO){
        ECReleaseStrategyJob strategyJob=new ECReleaseStrategyJob();
        BeanUtils.copyProperties(strategyDO,strategyJob);
        BeanUtils.copyProperties(ECReleaseStrategy.fromDO(strategyDO),strategyJob);
        return strategyJob;
    }

}

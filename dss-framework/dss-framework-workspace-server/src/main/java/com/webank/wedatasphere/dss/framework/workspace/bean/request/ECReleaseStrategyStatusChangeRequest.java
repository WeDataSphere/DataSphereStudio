package com.webank.wedatasphere.dss.framework.workspace.bean.request;

/**
 * 改变引擎释放规则启用状态的请求参数
 * Author: xlinliu
 * Date: 2023/4/18
 */
public class ECReleaseStrategyStatusChangeRequest {
    /**
     * 规则id
     */
    private String strategyId;
    /**
     * 改变的动作。
     * turnOn：开启
     * turnDown：关闭
     */
    private String action;

    public String getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(String strategyId) {
        this.strategyId = strategyId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}

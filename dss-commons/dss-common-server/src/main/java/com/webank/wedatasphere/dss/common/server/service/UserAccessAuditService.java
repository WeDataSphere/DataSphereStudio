package com.webank.wedatasphere.dss.common.server.service;

public interface UserAccessAuditService {
    /**
     * 增加登录次数
     * @param userName 用户名
     */
    void increaseLoginCount(String userName);
    /**
     * 获取登录次数。没登录过的话，返回0
     * @param userName 用户名
     */
    Long getLoginCount(String userName);

    /**
     * 获取之前访问次数，并把访问次数加1
     * @param userName 用户名
     * @return 之前的访问次数
     */
    Long getAndIncreaseLoginCount(String userName);
}

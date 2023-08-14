package com.webank.wedatasphere.dss.framework.workspace.service;

import com.webank.wedatasphere.dss.framework.workspace.bean.ECReleaseStrategy;

/**
 * ec释放请求摘要信息推送服务
 * Author: xlinliu
 * Date: 2023/4/21
 */
public interface ECReleaseSummaryInfoNotificationService {
    boolean tryNotify(ECReleaseStrategy releaseStrategy);
}

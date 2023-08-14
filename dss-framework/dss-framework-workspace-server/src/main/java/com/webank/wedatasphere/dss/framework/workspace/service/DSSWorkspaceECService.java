package com.webank.wedatasphere.dss.framework.workspace.service;

import com.webank.wedatasphere.dss.framework.workspace.bean.ECReleaseStrategy;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.ECReleaseStrategyStatusChangeRequest;

import java.util.List;

/**
 * 工作空间引擎信息相关服务
 * Author: xlinliu
 * Date: 2023/4/17
 */
public interface DSSWorkspaceECService {
    /**
     * 查询工作空间内的队列名
     * @param workspaceId 工作空间id
     * @return
     */
    List<String> getQueueList(Long workspaceId);

    /**
     * 查询空间内的引擎释放规则列表
     * @param workspaceId 工作空间id
     * @return 规则列表
     */
    List<ECReleaseStrategy> listEcReleaseStrategy(Long workspaceId);

    /**
     * 获取单个规则
     * @param strategyId 规则id
     */
    ECReleaseStrategy getStrategy(String strategyId);

    /**
     * 保存/新建引擎释放规则
     * @param strategy 要保存/新建的规则
     * @param workspaceId  要保存到的工作空间id
     * @param userName 操作人
     * @return 保存好/新建好的规则
     */
    ECReleaseStrategy saveEcReleaseStrategy(ECReleaseStrategy strategy,Long workspaceId,String userName);

    /**
     * 删除引擎释放规则
     * @param strategyId 要删除的规则id
     */
    ECReleaseStrategy deleteEcReleaseStrategy(String strategyId);

    /**
     * 改变规则启用状态
     */
    ECReleaseStrategy changeEcReleaseStrategyStatus(ECReleaseStrategyStatusChangeRequest changeRequest);

}

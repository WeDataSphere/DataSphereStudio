package com.webank.wedatasphere.dss.framework.workspace.dao;

import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECReleaseStrategyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * EC释放规则操作mapper
 * Author: xlinliu
 * Date: 2023/4/18
 */
@Mapper
public interface DSSWorkspaceECReleaseStrategyMapper {
    List<ECReleaseStrategyDO>  getStrategyListInWorkspace(@Param("workspaceId")Long workspaceId);

    ECReleaseStrategyDO getStrategyByStrategyId(@Param("strategyId")String strategyId);
    ECReleaseStrategyDO getStrategyByQueueName(@Param("queueName")String queueName);

    /**
     * 查询需要被处理的规则。
     * @param secondTimeOut 资源处理的有效期，单位秒。 超过有效期的资源，被视为未处理
     */
    List<ECReleaseStrategyDO> getStrategiesNeedProcessing(@Param("secondTimeOut") Integer secondTimeOut);

    void insertStrategy(ECReleaseStrategyDO strategy);
    void updateStrategy(ECReleaseStrategyDO strategy);

    void deleteStrategy(@Param("strategyId") String strategyId);

    /**
     * 改变规则的状态
     * @param strategyId 规则的id
     * @param status 目标状态
     */
    void changeStrategyStatus(@Param("strategyId")String strategyId,@Param("status") Integer status);

    /**
     * cas方式更新执行实例和执行开始时间。
     * 注意，本来还有一个updateExecuteTime需要更新，但是这个参数可以直接更新成now()函数的值.
     * @param expectInstance 预期的执行实例
     * @param expectTime 预期是开始执行时间
     * @param updateInstance 新的目标实例
     * @return
     */
    int casSwapExecuteInstance(@Param("strategyId")String strategyId,
                               @Param("expectInstance") String expectInstance, @Param("expectTime") Date expectTime,
                               @Param("updateInstance") String updateInstance);
    void cleanExecuteInfo(@Param("strategyId")String strategyId);
}

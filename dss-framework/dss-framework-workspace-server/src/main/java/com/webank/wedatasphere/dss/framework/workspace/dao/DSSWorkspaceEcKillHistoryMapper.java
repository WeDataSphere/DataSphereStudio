package com.webank.wedatasphere.dss.framework.workspace.dao;

import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECKillHistoryRecordDO;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECKillSumHistoryRecordDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author arionliu
* @description 针对表【dss_ec_kill_history(请求释放EC历史)】的数据库操作Mapper
* @createDate 2023-04-19 15:37:47
* @Entity com.webank.wedatasphere.dss.framework.workspace.dao.entity.EcKillHistoryRecordDO
*/
public interface DSSWorkspaceEcKillHistoryMapper {
    /**
     * 插入请求释放ec历史记录
     * @param record 插入的记录
     */
    void insert(ECKillHistoryRecordDO record);

    /**
     * 批量插入请求释放ec历史记录
     * @param recordList 插入的记录
     */
    void batchInsert(@Param("recordList") List<ECKillHistoryRecordDO> recordList);

    /**
     * 根据工作空间名查询空间内的请求释放ec历史
     * @param workspaceId 工作空间名
     * @return 历史记录
     */
    List<ECKillHistoryRecordDO> selectByWorkspaceId(@Param("workspaceId") Long workspaceId);

    /**
     * 根据strategyId统计告警信息
     * @param ecStrategyId
     * @return
     */
    List<ECKillSumHistoryRecordDO> sumECKillInfoByStrategyId(@Param("ecStrategyId") String ecStrategyId, @Param("intervalTime") int intervalTime);

}

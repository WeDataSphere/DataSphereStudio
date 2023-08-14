package com.webank.wedatasphere.dss.framework.workspace.service;

import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECKillHistoryRecord;

import java.util.List;

/**
 * ec kill 历史记录服务
 * Author: xlinliu
 * Date: 2023/4/23
 */
public interface DSSWorkspaceECKillHistoryService {
    /**
     * 查询工作空间内引擎释放历史记录。
     * @param workspaceId
     * @return
     */
    PageInfo<ECKillHistoryRecord> listEcKillHistory(Long workspaceId, Integer pageNow, Integer pageSize);

    /**
     * 保存释放记录
     */
    void addEcKillRecord(ECKillHistoryRecord ecKillRecord);

    /**
     * 批量保存释放记录
     */
    void batchAddEcKillRecord(List<ECKillHistoryRecord> ecKillRecord);

}

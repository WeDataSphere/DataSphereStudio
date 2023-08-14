package com.webank.wedatasphere.dss.framework.workspace.service.impl;

import com.github.pagehelper.PageHelper;
import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECKillHistoryRecord;
import com.webank.wedatasphere.dss.framework.workspace.dao.DSSWorkspaceEcKillHistoryMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECKillHistoryRecordDO;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceECKillHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: xlinliu
 * Date: 2023/4/23
 */
@Service
public class DSSWorkspaceECKillHistoryServiceImpl implements DSSWorkspaceECKillHistoryService {
    @Autowired
    DSSWorkspaceEcKillHistoryMapper dssWorkspaceEcKillHistoryMapper;

    @Override
    public PageInfo<ECKillHistoryRecord> listEcKillHistory(Long workspaceId, Integer pageNow, Integer pageSize) {
        PageHelper.startPage(pageNow, pageSize);
        List<ECKillHistoryRecordDO> historyDOList= dssWorkspaceEcKillHistoryMapper.selectByWorkspaceId(workspaceId);
        com.github.pagehelper.PageInfo<ECKillHistoryRecordDO> doPage = new com.github.pagehelper.PageInfo<>(historyDOList);

        List<ECKillHistoryRecord> recordList=
                doPage.getList().stream().map(ECKillHistoryRecord::fromDO).collect(Collectors.toList());
        return new PageInfo<>(recordList,doPage.getTotal());
    }

    @Override
    public void addEcKillRecord(ECKillHistoryRecord ecKillRecord) {
        dssWorkspaceEcKillHistoryMapper.insert(ecKillRecord.toDO());
    }

    @Override
    public void batchAddEcKillRecord(List<ECKillHistoryRecord> ecKillRecords) {
        List<ECKillHistoryRecordDO> dos=ecKillRecords.stream().map(ECKillHistoryRecord::toDO).collect(Collectors.toList());
        dssWorkspaceEcKillHistoryMapper.batchInsert(dos);
    }
}

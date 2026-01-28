package com.webank.wedatasphere.dss.framework.workspace.strategy.impl;

import com.webank.wedatasphere.dss.framework.workspace.bean.ECKillHistoryRecord;
import com.webank.wedatasphere.dss.framework.workspace.strategy.ECKillingPriorityQueue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * EC实例请求释放默认优先队列
 * Author: xlinliu
 * Date: 2023/4/23
 */
public class DefaultECKillingPriorityQueue implements ECKillingPriorityQueue {
    private List<ECKillHistoryRecord> ecKillHistoryRecords;

    public DefaultECKillingPriorityQueue(List<ECKillHistoryRecord> ecKillHistoryRecords) {

        this.ecKillHistoryRecords = new ArrayList<>(ecKillHistoryRecords);
        this.ecKillHistoryRecords.sort(
                Comparator.comparing((Function<ECKillHistoryRecord,Long>) e -> e.getYarn().getQueueMemory(),Comparator.reverseOrder())
                .thenComparing(e -> e.getYarn().getQueueCpu(),Comparator.reverseOrder()));
    }

    @Override
    public boolean hasNext() {
        return !this.ecKillHistoryRecords.isEmpty();
    }

    @Override
    public ECKillHistoryRecord poll() {
        return ecKillHistoryRecords.remove(0);
    }
}

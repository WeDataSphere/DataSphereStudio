package com.webank.wedatasphere.dss.framework.workspace.strategy;

import com.webank.wedatasphere.dss.framework.workspace.bean.ECKillHistoryRecord;

/**
 * 引擎释放优先队列抽象
 * Author: xlinliu
 * Date: 2023/4/23
 */
public interface ECKillingPriorityQueue {
    boolean hasNext();
    ECKillHistoryRecord poll();
}

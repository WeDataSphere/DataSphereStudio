package com.webank.wedatasphere.dss.framework.workspace.strategy;

import com.webank.wedatasphere.dss.framework.workspace.bean.ECKillHistoryRecord;
import com.webank.wedatasphere.dss.framework.workspace.strategy.impl.DefaultECKillingPriorityQueue;
import com.webank.wedatasphere.dss.framework.workspace.strategy.impl.UserFairECKillingPriorityQueue;

import java.util.List;

/**
 * ec请求释放优先队列简单工厂
 * Author: xlinliu
 * Date: 2023/4/23
 */
public class ECKillingPriorityQueueFactory {
    public static ECKillingPriorityQueue getInstance(List<ECKillHistoryRecord> records){
        return new DefaultECKillingPriorityQueue(records);
    }
    public static ECKillingPriorityQueue getUserFairInstance(List<ECKillHistoryRecord> records){
        return new UserFairECKillingPriorityQueue(records);
    }
}

package com.webank.wedatasphere.dss.framework.workspace.strategy.impl;

import com.webank.wedatasphere.dss.framework.workspace.bean.ECKillHistoryRecord;
import com.webank.wedatasphere.dss.framework.workspace.strategy.ECKillingPriorityQueue;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户公平性优先队列。优先kill空闲引擎数量多的用户。
 * Author: xlinliu
 * Date: 2023/4/25
 */
public class UserFairECKillingPriorityQueue implements ECKillingPriorityQueue {
    private static final Long LONG_TIME_THRESHOLD = 5L*60*1000;
    private Map<String, List<ECKillHistoryRecord>> groupByUser;
    private List<String> users;
    private List<ECKillHistoryRecord> longTimeList;
    private List<ECKillHistoryRecord> shortTimeList;
    /**
     * 为正数时，表示队列的三个阶段。
     * stage=1,第1阶段是用户分组阶段，此时存在多于一个空闲引擎的用户。
     * stage=2,第2阶段是空闲时间优先阶段，此时所有用户都只有1个空闲引擎，且存在空闲时间多于5分钟的引擎。
     * stage=3,第3阶段是资源占用量优先阶段，此时所有用户都只有1个空闲引擎，且引擎空闲时间均小于5分钟。
     * 为负数时，表示队列不可以再取出元素了，hasNext() 返回false。
     */
    private int stage;


    public UserFairECKillingPriorityQueue(List<ECKillHistoryRecord> ecList) {
        if(ecList==null||ecList.isEmpty()){
            stage=-1;
            return;
        }

        this.groupByUser=ecList.stream().collect(Collectors.groupingBy(ECKillHistoryRecord::getOwner));
        for (Map.Entry<String, List<ECKillHistoryRecord>> stringListEntry : this.groupByUser.entrySet()) {
            //对同一个用户的引擎现根据内存降序，再根据cpu做降序,再根据空闲时长降序。最有排driver
            stringListEntry.getValue().sort(
                    Comparator.comparing(
                            (Function<ECKillHistoryRecord,Long>) e -> e.getYarn().getQueueMemory(),Comparator.reverseOrder())
                            .thenComparing(e -> e.getYarn().getQueueCpu(),Comparator.reverseOrder())
                            .thenComparing(ECKillHistoryRecord::getUnlockDuration,Comparator.reverseOrder())
                            .thenComparing(e->e.getDriver().getMemory(),Comparator.reverseOrder())
                            .thenComparing(e->e.getDriver().getCpu(),Comparator.reverseOrder())
            );
        }
        this.users = new ArrayList<>(this.groupByUser.keySet());
        //初始化为阶段1,并且直接排序
        this.stage=1;
        reSort();
    }

    @Override
    public boolean hasNext() {
        return stage>0;
    }

    @Override
    public ECKillHistoryRecord poll() {

        ECKillHistoryRecord e;
        if(stage==1){
            String firstUser = this.users.get(0);
            e= this.groupByUser.get(firstUser).remove(0);
        }else if(stage==2){
            e=this.longTimeList.remove(0);
        }else if(stage==3){
            e = this.shortTimeList.remove(0);
        }else {
            return null;
        }
        reSort();
        return e;
    }

    private void reSort(){
        if (stage == 1) {
            //根据用户名下的实例数对用户做降序排序
            this.users.sort(Comparator.comparing(e -> this.groupByUser.get(e).size(), Comparator.reverseOrder()));
            String firstUser = this.users.get(0);
            //所有用户的实例数都只有1，这时候要变更队列阶段了。
            if (this.groupByUser.get(firstUser).size() == 1) {
                Map<Boolean, List<ECKillHistoryRecord>> partition = this.groupByUser.values().stream().flatMap(Collection::stream).collect(Collectors.partitioningBy(e -> e.getUnlockDuration() > LONG_TIME_THRESHOLD));
                //长空闲时长的实例按照空闲时长降序，再根据内存降序，再根据cpu降序
                this.longTimeList = new ArrayList<>(partition.get(true));
                this.longTimeList.sort(
                        Comparator.comparing(ECKillHistoryRecord::getUnlockDuration,Comparator.reverseOrder())
                        .thenComparing( e -> e.getYarn().getQueueMemory(),Comparator.reverseOrder())
                        .thenComparing(e -> e.getYarn().getQueueCpu(),Comparator.reverseOrder())
                        .thenComparing(e->e.getDriver().getMemory(),Comparator.reverseOrder())
                        .thenComparing(e->e.getDriver().getCpu(),Comparator.reverseOrder())
                );
                //短空闲时长的实例按照资源排序。资源先排内存，再排cpu，再拍空闲时长
                this.shortTimeList = new ArrayList<>(partition.get(false));
                this.shortTimeList.sort(
                        Comparator.comparing((Function<ECKillHistoryRecord,Long>) e -> e.getYarn().getQueueMemory(),Comparator.reverseOrder())
                                .thenComparing(e -> e.getYarn().getQueueCpu(),Comparator.reverseOrder())
                                .thenComparing(ECKillHistoryRecord::getUnlockDuration,Comparator.reverseOrder())
                                .thenComparing(e->e.getDriver().getMemory(),Comparator.reverseOrder())
                                .thenComparing(e->e.getDriver().getCpu(),Comparator.reverseOrder())
                );
                //如果longTimeList为空，说明所有ec都是短时空闲，直接进入阶段3。否则进入阶段2.
                if (this.longTimeList.isEmpty()) {
                    stage = 3;
                } else {
                    stage = 2;
                }
            }
        } else if (stage == 2) {
            //不存在长空闲时长的实例，说明要改变阶段了
            if (this.longTimeList.isEmpty()) {
                //如果也不存在短空闲时长的实例，说明队列空了。否则到阶段3
                if(this.shortTimeList.isEmpty()){
                    stage=-1;
                }else {
                    stage=3;
                }
            }
        }else if(stage==3){
            //没有任何实例了，说明队列为空了
            if(this.shortTimeList.isEmpty()){
                stage=-1;
            }
        }
    }


}

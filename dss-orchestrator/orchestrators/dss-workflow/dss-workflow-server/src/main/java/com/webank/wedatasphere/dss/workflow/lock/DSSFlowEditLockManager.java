/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.workflow.lock;

import com.webank.wedatasphere.dss.common.entity.BmlResource;
import com.webank.wedatasphere.dss.common.entity.project.DSSProject;
import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.common.protocol.project.ProjectInfoRequest;
import com.webank.wedatasphere.dss.common.utils.RpcAskUtils;
import com.webank.wedatasphere.dss.git.common.protocol.request.GitCommitRequest;
import com.webank.wedatasphere.dss.git.common.protocol.response.GitCommitResponse;
import com.webank.wedatasphere.dss.sender.service.DSSSenderServiceFactory;
import com.webank.wedatasphere.dss.workflow.common.entity.DSSFlow;
import com.webank.wedatasphere.dss.workflow.constant.DSSWorkFlowConstant;
import com.webank.wedatasphere.dss.workflow.dao.FlowMapper;
import com.webank.wedatasphere.dss.workflow.dao.LockMapper;
import com.webank.wedatasphere.dss.workflow.entity.DSSFlowEditLock;
import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.common.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.rpc.Sender;
import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import static com.webank.wedatasphere.dss.workflow.constant.DSSWorkFlowConstant.FLOW_STATUS_PUSH;
import static com.webank.wedatasphere.dss.workflow.constant.DSSWorkFlowConstant.FLOW_STATUS_SAVE;


/**
 * 工作流编辑分布式锁
 */
public class DSSFlowEditLockManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSSFlowEditLockManager.class);

    private volatile static boolean isInit;

    private static LockMapper lockMapper;

    private static FlowMapper flowMapper;


    private static final DelayQueue<UnLockEvent> unLockEvents = new DelayQueue<>();
    private static final ThreadLocal<SimpleDateFormat> sdf = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    protected DSSFlowEditLockManager() {
    }

    static {
        LOGGER.info("unLockEvents移除定时线程开启...");
        LOGGER.info("编辑锁超时时间为：{} ms", DSSWorkFlowConstant.DSS_FLOW_EDIT_LOCK_TIMEOUT.getValue());
        init();
        //程序重启时，删除所有编辑锁
        List<String> publishList = lockMapper.selectFlowIdByStatus(DSSWorkFlowConstant.FLOW_STATUS_PUBLISH);
        List<String> pushList = lockMapper.selectFlowIdByStatus(DSSWorkFlowConstant.FLOW_STATUS_PUSH);
        List<String> result = new ArrayList<>();
        result.addAll(publishList);
        result.addAll(pushList);
        if (!CollectionUtils.isEmpty(result)) {
            lockMapper.deleteExpectAfterSave(result);
        }else {
            lockMapper.deleteALL();
        }
        Utils.defaultScheduler().scheduleAtFixedRate(() -> {
            try {
                UnLockEvent pop = unLockEvents.poll();
                if (pop != null) {
                    DSSFlowEditLock flowEditLock = pop.getFlowEditLock();
                    long flowId = flowEditLock.getFlowID();
                    DSSFlowEditLock updateFlowEditLock = lockMapper.getFlowEditLockByID(flowId);
                    //队列对象如果过期，先去数据库查询锁，并判断锁是否过期
                    if (updateFlowEditLock != null && isLockExpire(updateFlowEditLock)) {
                        //锁过期，移除记录
                        String expireTime = sdf.get().format(new Date(System.currentTimeMillis() - DSSWorkFlowConstant.DSS_FLOW_EDIT_LOCK_TIMEOUT.getValue()));
                        LOGGER.info("删除用户{}的编辑锁,flowId:{},lockContent:{},owner:{},update_time:{},expireTime:{}",
                                updateFlowEditLock.getUsername(), flowId, updateFlowEditLock.getLockContent(),
                                updateFlowEditLock.getOwner(), updateFlowEditLock.getUpdateTime(), expireTime);
                        lockMapper.clearExpire(expireTime, flowId);
                    } else if (updateFlowEditLock == null) {
                        LOGGER.info("lock already clear");
                    } else {
                        //锁没有过期，延长队列时间
                        pop.setExpireTime(updateFlowEditLock.getUpdateTime().getTime() + DSSWorkFlowConstant.DSS_FLOW_EDIT_LOCK_TIMEOUT.getValue());
                        unLockEvents.offer(pop);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("定时清除工作流编辑锁错误", e);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * 获取工作流编辑锁
     *
     * @param dssFlow  工作流对象
     * @param username 登陆用户
     * @param owner    锁的拥有者(ticketId)
     * @return 锁id
     * @throws DSSErrorException ex
     */
    public static String tryAcquireLock(DSSFlow dssFlow, String username, String owner) throws DSSErrorException {
        if (StringUtils.isBlank(username)) {
            throw new DSSErrorException(60061, "tryAcquireLock failed , because username is null");
        }
        if (StringUtils.isBlank(owner)) {
            throw new DSSErrorException(60062, "tryAcquireLock failed , because owner is null");
        }
        Long flowID = dssFlow.getId();
        if (flowID == null) {
            throw new DSSErrorException(60063, "tryAcquireLock failed , because flowId is null");
        }
        String lock;
        init();
        DSSFlowEditLock flowEditLock = lockMapper.getPersonalFlowEditLock(flowID, owner);

        if (flowEditLock != null && isLockExpire(flowEditLock)) {
            // 1.另外的dss-server服务挂掉了（main）;2.记录已经过期，但是UnLockEvent 尚未(即将)去更新数据库;3.记录已经过期，但是updateTime 尚未（即将）更新数据库
            flowEditLock.setExpire(true);
            int i = lockMapper.compareAndSwap(flowEditLock);
            LOGGER.info("try to set lock to an expire state,row:{},lock:{}", i, flowEditLock);
            if (i == 1) {
                //更新成功，尝试获取新锁
                LOGGER.info("unlock success,lock:{}", flowEditLock);
                lockMapper.clearExpire(sdf.get().format(new Date(System.currentTimeMillis() - DSSWorkFlowConstant.DSS_FLOW_EDIT_LOCK_TIMEOUT.getValue())), flowID);
                lock = generateLock(flowID, username, owner);
            } else {
                // 失败的缘由: 1.另外的dss-server也走到了这步，优先更新了;2.UnLockEvent;3.用户刚好点了saveFlow延续
                throw new DSSErrorException(60055, "acquire lock failed");
            }
        } else if (flowEditLock != null) {
            lockMapper.clearExpire(sdf.get().format(new Date(System.currentTimeMillis() - DSSWorkFlowConstant.DSS_FLOW_EDIT_LOCK_TIMEOUT.getValue())), flowID);
            lock = generateLock(flowID, username, owner);
        } else {
            // 插入锁,获取到数据库自增id
            lock = generateLock(flowID, username, owner);
        }
        // 插入成功，返回lock，push一条记录到queue中，插入失败，返回null
        return lock;
    }

    private static String generateLock(Long flowID, String username, String owner) throws DSSErrorException {
        try {
            String lockContent = UUID.randomUUID().toString();
            DSSFlowEditLock newLock = new DSSFlowEditLock();
            newLock.setExpire(false);
            Date date = new Date();
            newLock.setCreateTime(date);
            newLock.setUpdateTime(date);
            newLock.setFlowID(flowID);
            newLock.setLockContent(lockContent);
            newLock.setOwner(owner);
            newLock.setUsername(username);
            lockMapper.insertLock(newLock);
            //推到queue中
            UnLockEvent unLockEvent = new UnLockEvent();
            unLockEvent.setCreateTime(date.getTime());
            unLockEvent.setExpireTime(date.getTime() + DSSWorkFlowConstant.DSS_FLOW_EDIT_LOCK_TIMEOUT.getValue());
            unLockEvent.setFlowEditLock(newLock);
            unLockEvents.offer(unLockEvent);
            return lockContent;
        } catch (DuplicateKeyException e) {
            LOGGER.warn("acquire lock failed", e.getMessage());
            DSSFlowEditLock personalFlowEditLock = lockMapper.getPersonalFlowEditLock(flowID, null);
            String userName = Optional.ofNullable(personalFlowEditLock).map(DSSFlowEditLock::getUsername).orElse(null);
            throw new DSSErrorException(DSSWorkFlowConstant.EDIT_LOCK_ERROR_CODE, "用户" + userName + "已锁定编辑");
        }
    }

    public static void deleteLock(String flowEditLock) throws DSSErrorException {
        try {
            if (StringUtils.isNotBlank(flowEditLock)) {
                DSSFlowEditLock dssFlowEditLock = lockMapper.getFlowEditLockByLockContent(flowEditLock);
                if (dssFlowEditLock != null) {
                    // 获取当前项目信息
                    DSSFlow dssFlow = flowMapper.selectFlowByID(dssFlowEditLock.getFlowID());
                    DSSProject projectInfo = getProjectInfo(dssFlow.getProjectId());
                    // 对于接入Git的项目，工作流解锁加入额外处理
                    if (projectInfo.getAssociateGit()) {
                        String status = lockMapper.selectStatusByFlowId(dssFlowEditLock.getFlowID());
                        if (!StringUtils.isEmpty(status) && DSSWorkFlowConstant.FLOW_STATUS_SAVE.equals(status)) {
//                        pushProject(projectInfo.getName(), projectInfo.getWorkspaceId(), "resurce", "version", "path", projectInfo.getUsername(), "comment");
                            lockMapper.insertFlowStatus(dssFlow.getId(), FLOW_STATUS_PUSH);
                        }
                    }
                    lockMapper.clearExpire(sdf.get().format(new Date(System.currentTimeMillis() - DSSWorkFlowConstant.DSS_FLOW_EDIT_LOCK_TIMEOUT.getValue())), dssFlowEditLock.getFlowID());
                }
            }
        } catch (Exception e) {
            LOGGER.error("flowEditLock delete failed，flowId：{}", flowEditLock, e);
            throw new DSSErrorException(60059, "工作流编辑锁主动释放失败，flowId:" + flowEditLock + "");
        }
    }

    public static DSSProject getProjectInfo(Long projectId) throws DSSErrorException {
        ProjectInfoRequest projectInfoRequest = new ProjectInfoRequest();
        projectInfoRequest.setProjectId(projectId);
        DSSProject dssProject = RpcAskUtils.processAskException(DSSSenderServiceFactory.getOrCreateServiceInstance().getProjectServerSender()
                .ask(projectInfoRequest), DSSProject.class, ProjectInfoRequest.class);
        if (dssProject == null) {
            throw new DSSErrorException(90001, "工程不存在");
        }
        return dssProject;
    }

    public static void pushProject(String projectName, Long workspaceId, String resource, String version, String path, String username, String comment) {
        LOGGER.info("-------=======================begin to add testGit1=======================-------");
        Sender gitSender = DSSSenderServiceFactory.getOrCreateServiceInstance().getGitSender();
        Map<String, BmlResource> file = new HashMap<>();
        // 测试数据 key表示项目名、value为项目BmlResource资源
        file.put(path, new BmlResource(resource, version));
        GitCommitRequest request1 = new GitCommitRequest(workspaceId, projectName, file, comment, username);
        GitCommitResponse responseWorkflowValidNode = RpcAskUtils.processAskException(gitSender.ask(request1), GitCommitResponse.class, GitCommitRequest.class);
        LOGGER.info("-------=======================End to add testGit1=======================-------: {}", responseWorkflowValidNode);
    }


    public static String updateLock(String lock) throws DSSErrorException {
        if (StringUtils.isBlank(lock)) {
            throw new DSSErrorException(60066, "update workflow failed because you do not have flowEditLock!");
        }
        //保存并刷新数据库更新时间
        DSSFlowEditLock dssFlowEditLock = new DSSFlowEditLock();
        dssFlowEditLock.setUpdateTime(new Date());
        dssFlowEditLock.setLockContent(lock);
        DSSFlowEditLock updateFlowEditLock = lockMapper.getFlowEditLockByLockContent(lock);
        if (updateFlowEditLock == null || updateFlowEditLock.getExpire()) {
            lockMapper.clearExpire(sdf.get().format(new Date(System.currentTimeMillis() - DSSWorkFlowConstant.DSS_FLOW_EDIT_LOCK_TIMEOUT.getValue())), 0L);
            throw new DSSErrorException(60057, "编辑锁已过期，请刷新页面");
        }
        try {
            lockMapper.compareAndSwap(dssFlowEditLock);
            return lock;
        } catch (Exception e) {
            LOGGER.error("unexpected error occurred when update dss flow edit lock,{}", dssFlowEditLock, e);
            lockMapper.clearExpire(sdf.get().format(new Date(System.currentTimeMillis() - DSSWorkFlowConstant.DSS_FLOW_EDIT_LOCK_TIMEOUT.getValue())), updateFlowEditLock.getFlowID());
            throw new DSSErrorException(60059, "工作流编辑锁更新出错，请刷新页面");
        }
    }

    public static boolean isLockExpire(DSSFlowEditLock flowEditLock) {
        String status = lockMapper.selectStatusByFlowId(flowEditLock.getFlowID());
        if (StringUtils.isEmpty(status) && DSSWorkFlowConstant.FLOW_STATUS_SAVE.equals(status)) {
            return false;
        }
        return System.currentTimeMillis() - flowEditLock.getUpdateTime().getTime() >= DSSWorkFlowConstant.DSS_FLOW_EDIT_LOCK_TIMEOUT.getValue();
    }

    public static void init() {
        if (!isInit) {
            synchronized (DSSFlowEditLockManager.class) {
                if (!isInit) {
                    lockMapper = DataWorkCloudApplication.getApplicationContext().getBean(LockMapper.class);
                    flowMapper = DataWorkCloudApplication.getApplicationContext().getBean(FlowMapper.class);
                    isInit = true;
                }
            }
        }
    }

    public static class UnLockEvent implements Delayed {
        private long createTime;
        private long expireTime;
        private DSSFlowEditLock flowEditLock;

        DSSFlowEditLock getFlowEditLock() {
            return flowEditLock;
        }

        void setFlowEditLock(DSSFlowEditLock flowEditLock) {
            this.flowEditLock = flowEditLock;
        }

        public boolean isExpire() {
            return System.currentTimeMillis() - expireTime > 0;
        }

        public long getCreateTime() {
            return createTime;
        }

        void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public long getExpireTime() {
            return this.expireTime;
        }

        void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expireTime - System.currentTimeMillis(), unit);
        }

        @Override
        public int compareTo(Delayed o) {
            return (int) (getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public String toString() {
            return "UnLockEvent{" +
                    "createTime=" + createTime +
                    ", expireTime=" + expireTime +
                    ", flowEditLock=" + flowEditLock +
                    '}';
        }
    }

}

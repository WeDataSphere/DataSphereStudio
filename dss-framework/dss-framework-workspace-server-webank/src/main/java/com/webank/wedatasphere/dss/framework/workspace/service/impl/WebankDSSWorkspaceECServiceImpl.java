package com.webank.wedatasphere.dss.framework.workspace.service.impl;

import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECReleaseStrategy;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.ECReleaseStrategyStatusChangeRequest;
import com.webank.wedatasphere.dss.framework.workspace.dao.WebankDSSWorkspaceECReleaseStrategyMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.WebankDSSWorkspaceQueueMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECReleaseStrategyDO;
import com.webank.wedatasphere.dss.framework.workspace.service.WebankDSSWorkspaceECService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: xlinliu
 * Date: 2023/4/17
 */
@Service
public class WebankDSSWorkspaceECServiceImpl implements WebankDSSWorkspaceECService {
    @Autowired
    WebankDSSWorkspaceQueueMapper webankDSSWorkspaceQueueMapper;
    @Autowired
    WebankDSSWorkspaceECReleaseStrategyMapper webankDSSWorkspaceECReleaseStrategyMapper;

    @Override
    public List<String> getQueueList(Long workspaceId,boolean isCrossCluster) {
        int crossClusterFlag = isCrossCluster ? 1 : 0;
        return webankDSSWorkspaceQueueMapper.getQueueList(workspaceId,crossClusterFlag);
    }

    @Override
    public List<ECReleaseStrategy> listEcReleaseStrategy(Long workspaceId) {
        List<ECReleaseStrategyDO> dos=webankDSSWorkspaceECReleaseStrategyMapper.getStrategyListInWorkspace(workspaceId);
        return dos.stream().map(ECReleaseStrategy::fromDO).collect(Collectors.toList());
    }

    @Override
    public ECReleaseStrategy getStrategy(String strategyId) {
        ECReleaseStrategyDO strategyDO = webankDSSWorkspaceECReleaseStrategyMapper.getStrategyByStrategyId(strategyId);
        return Optional.ofNullable(strategyDO).map(ECReleaseStrategy::fromDO).orElse(null);
    }

    @Override
    public ECReleaseStrategy saveEcReleaseStrategy(ECReleaseStrategy strategy,Long workspaceId,String userName) {
        //todo 规则保存前需要校验规则的合法性
        if(strategy.getImsConf()==null){
            strategy.setImsConf(new ECReleaseStrategy.IMSConf());
        }
        //说明是新增规则
        if(strategy.getStrategyId()==null){
            Set<String> strategyNameSet = listEcReleaseStrategy(workspaceId).stream().map(ECReleaseStrategy::getName).collect(Collectors.toSet());
            Set<String> queueInWorkspace = new HashSet<>(getQueueList(workspaceId,strategy.getCrossCluster()));
            String strategyName = strategy.getName();
            if(strategyNameSet.contains(strategyName)){
                String msg = String.format("strategy name %s already exist(规则名%s已经存在，不允许重名)", strategyName,strategyName);
                throw new DSSRuntimeException(msg);
            }
            String queueName= strategy.getQueue();
            if(queueName==null||!queueInWorkspace.contains(queueName)){
                String msg=String.format("queue %s is not in this workspace(此工作空间不存在队列%s，是否跨集群：%s",queueName,queueName,strategy.getCrossCluster());
                throw new DSSRuntimeException(msg);
            }
            if (webankDSSWorkspaceECReleaseStrategyMapper.getStrategyByQueueName(queueName, strategy.getCrossCluster() ? 1 : 0) != null) {
                String msg = String.format("already exist strategy with queue %s(队列%s已关联其他规则，不可重复配置", queueName, queueName);
                throw new DSSRuntimeException(msg);
            }
            String uuid= UUID.randomUUID().toString();
            strategy.setStrategyId(uuid);
            strategy.setCreator(userName);
            strategy.setModifier(userName);
            ECReleaseStrategyDO strategyDO= strategy.toDO();
            strategyDO.setWorkspaceId(workspaceId);

            webankDSSWorkspaceECReleaseStrategyMapper.insertStrategy(strategyDO);
        }
        //说明是修改规则
        else{
            strategy.setModifier(userName);
            ECReleaseStrategyDO strategyDO=strategy.toDO();
            webankDSSWorkspaceECReleaseStrategyMapper.updateStrategy(strategyDO);
        }
        return getStrategy(strategy.getStrategyId());
    }

    @Override
    public ECReleaseStrategy deleteEcReleaseStrategy(String strategyId) {
        ECReleaseStrategy strategy=getStrategy(strategyId);
        if(strategy==null){
            throw new DSSRuntimeException("strategy not exist(规则不存在，无法删除)");
        }else if(strategy.getStatus()==1){
            throw new DSSRuntimeException("can not delete strategy in effect(无法删除启用状态的规则，请先禁用规则)");
        }
        webankDSSWorkspaceECReleaseStrategyMapper.deleteStrategy(strategyId);
        return strategy;
    }

    @Override
    public ECReleaseStrategy changeEcReleaseStrategyStatus(ECReleaseStrategyStatusChangeRequest changeRequest) {
        ECReleaseStrategy strategy=getStrategy(changeRequest.getStrategyId());
        if(strategy==null){
            throw new RuntimeException("strategy not exist(规则不存在，请刷新重试）");
        }
        int status;
        if("turnOn".equals(changeRequest.getAction())){
            status=1;
        }else if("turnDown".equals(changeRequest.getAction())){
            status=0;
        }else {
            throw new DSSRuntimeException("unknown action " + changeRequest.getAction());
        }
        webankDSSWorkspaceECReleaseStrategyMapper.changeStrategyStatus(changeRequest.getStrategyId(), status);
        return strategy;
    }

}

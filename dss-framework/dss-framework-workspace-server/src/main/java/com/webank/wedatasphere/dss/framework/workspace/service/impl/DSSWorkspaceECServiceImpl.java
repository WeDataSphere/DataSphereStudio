package com.webank.wedatasphere.dss.framework.workspace.service.impl;

import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECReleaseStrategy;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.ECReleaseStrategyStatusChangeRequest;
import com.webank.wedatasphere.dss.framework.workspace.dao.DSSWorkspaceECReleaseStrategyMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.DSSWorkspaceQueueMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECReleaseStrategyDO;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceECService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: xlinliu
 * Date: 2023/4/17
 */
@Service
public class DSSWorkspaceECServiceImpl implements DSSWorkspaceECService {
    @Autowired
    DSSWorkspaceQueueMapper dssWorkspaceQueueMapper;
    @Autowired
    DSSWorkspaceECReleaseStrategyMapper dssWorkspaceECReleaseStrategyMapper;

    @Override
    public List<String> getQueueList(Long workspaceId) {
        return dssWorkspaceQueueMapper.getQueueList(workspaceId);
    }

    @Override
    public List<ECReleaseStrategy> listEcReleaseStrategy(Long workspaceId) {
        List<ECReleaseStrategyDO> dos= dssWorkspaceECReleaseStrategyMapper.getStrategyListInWorkspace(workspaceId);
        return dos.stream().map(ECReleaseStrategy::fromDO).collect(Collectors.toList());
    }

    @Override
    public ECReleaseStrategy getStrategy(String strategyId) {
        ECReleaseStrategyDO strategyDO = dssWorkspaceECReleaseStrategyMapper.getStrategyByStrategyId(strategyId);
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
            Set<String> queueInWorkspace = new HashSet<>(getQueueList(workspaceId));
            String strategyName = strategy.getName();
            if(strategyNameSet.contains(strategyName)){
                String msg = String.format("strategy name %s already exist(规则名%s已经存在，不允许重名)", strategyName,strategyName);
                throw new DSSRuntimeException(msg);
            }
            String queueName= strategy.getQueue();
            if(queueName==null||!queueInWorkspace.contains(queueName)){
                String msg=String.format("queue %s is not in this workspace(此工作空间不存在队列%s",queueName,queueName);
                throw new DSSRuntimeException(msg);
            }
            if(dssWorkspaceECReleaseStrategyMapper.getStrategyByQueueName(queueName)!=null){
                String msg=String.format("already exist strategy with queue %s(队列%s已关联其他规则，不可重复配置",queueName,queueName);
                throw new DSSRuntimeException(msg);
            }
            String uuid= UUID.randomUUID().toString();
            strategy.setStrategyId(uuid);
            strategy.setCreator(userName);
            strategy.setModifier(userName);
            ECReleaseStrategyDO strategyDO= strategy.toDO();
            strategyDO.setWorkspaceId(workspaceId);

            dssWorkspaceECReleaseStrategyMapper.insertStrategy(strategyDO);
        }
        //说明是修改规则
        else{
            strategy.setModifier(userName);
            ECReleaseStrategyDO strategyDO=strategy.toDO();
            dssWorkspaceECReleaseStrategyMapper.updateStrategy(strategyDO);
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
        dssWorkspaceECReleaseStrategyMapper.deleteStrategy(strategyId);
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
        dssWorkspaceECReleaseStrategyMapper.changeStrategyStatus(changeRequest.getStrategyId(), status);
        return strategy;
    }

}

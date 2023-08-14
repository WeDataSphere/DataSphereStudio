package com.webank.wedatasphere.dss.framework.workspace.service.impl;

import com.github.pagehelper.PageHelper;
import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.client.ResourceManageClient;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request.ApplyECConfTemplateRequest;
import com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response.ApplyECConfTemplateResponse;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECConfTemplate;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECConfTemplateApplyRule;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECTemplateApplyRecord;
import com.webank.wedatasphere.dss.framework.workspace.bean.PermissionUser;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.*;
import com.webank.wedatasphere.dss.framework.workspace.bean.vo.ECConfTemplateApplyInfoVO;
import com.webank.wedatasphere.dss.framework.workspace.bean.vo.ECConfTemplateApplyRuleVO;
import com.webank.wedatasphere.dss.framework.workspace.dao.ECConfigTemplateApplyRuleExecuteRecordMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.ECConfigTemplateApplyRuleMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.ECConfigTemplateApplyRuleUserMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateApplyRuleDO;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateApplyRuleExecuteRecordDO;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateApplyRuleUserDO;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.PermissionUserCountDO;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceUserService;
import com.webank.wedatasphere.dss.framework.workspace.service.ECConfTemplateApplyRuleService;
import com.webank.wedatasphere.dss.framework.workspace.service.ECConfTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Author: xlinliu
 * Date: 2023/6/26
 */
@Service
public class ECConfTemplateApplyRuleServiceImpl implements ECConfTemplateApplyRuleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ECConfTemplateApplyRuleServiceImpl.class);

    @Autowired
    private ECConfTemplateService ecConfTemplateService;
    @Autowired
    ResourceManageClient resourceManageClient;
    @Autowired
    ECConfigTemplateApplyRuleMapper ecConfigTemplateApplyRuleMapper;
    @Autowired
    ECConfigTemplateApplyRuleUserMapper ecConfigTemplateApplyRuleUserMapper;
    @Autowired
    private DSSWorkspaceUserService dssWorkspaceUserService;
    @Autowired
    private ECConfigTemplateApplyRuleExecuteRecordMapper ecConfigTemplateApplyRuleExecuteRecordMapper;

    @Override
    public ECConfTemplateApplyRule saveRule(ECConfTemplateApplyRuleSaveRequest saveRequest, String operator, Long workspaceId) {
        String application= saveRequest.getApplication();
        String engineName= saveRequest.getEngineName();
        ECConfTemplateRuleGetRequest ruleGetRequest=new ECConfTemplateRuleGetRequest();
        ruleGetRequest.setWorkspaceId(workspaceId);
        ruleGetRequest.setRuleType(ECConfTemplateApplyRule.RULE_TYPE_NEWUSER);
        ruleGetRequest.setApplication(application);
        ruleGetRequest.setPageNow(1);
        ruleGetRequest.setPageSize(10);
        boolean existSameNewUserTypeRule = saveRequest.getRuleType() == ECConfTemplateApplyRule.RULE_TYPE_NEWUSER
                && getRuleList(ruleGetRequest).getData().stream().anyMatch(e -> e.getEngineName().equals(engineName));
        if(existSameNewUserTypeRule){
            throw new DSSRuntimeException(application + "应用已经存在" + engineName + "引擎的工作空间新用户规则，不可重复配置");
        }
        ECConfTemplate template = ecConfTemplateService.getTemplate(saveRequest.getTemplateId());
        ECConfTemplateApplyRule rule = new ECConfTemplateApplyRule(template,
                saveRequest.getRuleType(),
                saveRequest.getEngineName(),
                saveRequest.getPermissionType(),
                saveRequest.getPermissionUsers(),
                saveRequest.getApplication(),
                operator);

        ECConfigTemplateApplyRuleDO ruleDO = rule.toDO(workspaceId);
        ecConfigTemplateApplyRuleMapper.insert(ruleDO);
        if (rule.getPermissionType() == ECConfTemplateApplyRule.SPECIFIED_USER_PERMISSION_TYPE) {
            List<ECConfigTemplateApplyRuleUserDO> userDos = rule.getUserDOList(workspaceId);
            ecConfigTemplateApplyRuleUserMapper.insertBatch(userDos);
        } else if (rule.getPermissionType() == ECConfTemplateApplyRule.ALL_USER_PERMISSION_TYPE) {
            List<PermissionUser> permissionUsers = dssWorkspaceUserService.getAllWorkspaceUsers(workspaceId).stream()
                    .map(PermissionUser::new).collect(Collectors.toList());
            rule.setPermissionUsers(permissionUsers);
        }
        return rule;
    }

    @Override
    public void executeRule(ECConfTemplateApplyRule rule,String operator,Long workspaceId) {
        String engineName=rule.getEngineName();
        String engineType = engineName.substring(0, engineName.indexOf("-"));
        String engineVersion = engineName.substring(engineName.indexOf("-")+1);
        List<String> userList=rule.getPermissionUsers().stream().map(PermissionUser::getName).collect(Collectors.toList());
        ApplyECConfTemplateRequest clientRequest = new ApplyECConfTemplateRequest(rule.getTemplate().getTemplateId()
                , rule.getApplication()
                , engineType
                , engineVersion
                , operator
                , userList);
        int status = 0;
        try {
            ApplyECConfTemplateResponse response = resourceManageClient.applyECConfTemplate(clientRequest);

            List<ECConfigTemplateApplyRuleExecuteRecordDO> successUsers = response.getSuccess().getInfoList().stream()
                    .map(ApplyECConfTemplateResponse.User::getUser).map(
                            e -> new ECConfigTemplateApplyRuleExecuteRecordDO(e, rule.getRuleId(), workspaceId, rule.getTemplate().getName()
                                    , engineType, rule.getApplication(), 1, operator)
                    )
                    .collect(Collectors.toList());
            List<ECConfigTemplateApplyRuleExecuteRecordDO> failedUsers = response.getError().getInfoList().stream()
                    .map(ApplyECConfTemplateResponse.User::getUser).map(
                            e -> new ECConfigTemplateApplyRuleExecuteRecordDO(e, rule.getRuleId(), workspaceId, rule.getTemplate().getName()
                                    , engineType, rule.getApplication(), 2, operator)
                    )
                    .collect(Collectors.toList());
            if (!successUsers.isEmpty()) {
                ecConfigTemplateApplyRuleExecuteRecordMapper.insertBatch(successUsers);
            }
            if (!failedUsers.isEmpty()) {
                ecConfigTemplateApplyRuleExecuteRecordMapper.insertBatch(failedUsers);
            }

            //有成功，没失败：全部成功
            if (!successUsers.isEmpty() && failedUsers.isEmpty()) {
                status = 1;
            }

            //没成功，有失败：全部失败
            else if (successUsers.isEmpty() && !failedUsers.isEmpty()) {
                status = 2;
            }
            //有失败也有成功：部分失败
            else if (!successUsers.isEmpty() && !failedUsers.isEmpty()) {
                status = 3;
            }
        }catch (Exception e){
            LOGGER.error("execute rule failed,ruleId:{},templateName:{}", rule.getRuleId(), rule.getTemplate().getName());
            LOGGER.error("execute rule failed",e);
            status = 2;
        }
        ecConfigTemplateApplyRuleMapper.updateStatus(rule.getRuleId(), status,operator);
    }

    @Override
    public PageInfo<ECConfTemplateApplyRuleVO> getRuleList(ECConfTemplateRuleGetRequest request) {
        PageHelper.startPage(request.getPageNow(), request.getPageSize());
        List<ECConfigTemplateApplyRuleDO> dos = ecConfigTemplateApplyRuleMapper.getRules(request);
        com.github.pagehelper.PageInfo<ECConfigTemplateApplyRuleDO> doPage = new com.github.pagehelper.PageInfo<>(dos);
        List<ECConfTemplateApplyRuleVO> recordList=
                doPage.getList().stream().map(ECConfTemplateApplyRuleVO::fromDO).collect(Collectors.toList());
        List<String> ruleIds=
                recordList.stream()
                        .filter(e -> e.getPermissionType() == ECConfTemplateApplyRule.SPECIFIED_USER_PERMISSION_TYPE)
                        .map(ECConfTemplateApplyRuleVO::getRuleId)
                        .collect(Collectors.toList());
        Map<String, PermissionUserCountDO> countMap;
        if(!ruleIds.isEmpty()) {
            countMap = ecConfigTemplateApplyRuleUserMapper.getPermissionUserCount(ruleIds);
        }else {
            countMap= Collections.EMPTY_MAP;
        }
        long workspaceUserCount= dssWorkspaceUserService.getUserCount(request.getWorkspaceId());
        for (ECConfTemplateApplyRuleVO rule : recordList) {
            long count;
            if(rule.getPermissionType()==ECConfTemplateApplyRule.SPECIFIED_USER_PERMISSION_TYPE){
                count = countMap.getOrDefault(rule.getRuleId(),new PermissionUserCountDO(){ {setGroupId("");setUserCount(0L);}}).getUserCount();
            }else if(rule.getPermissionType()==ECConfTemplateApplyRule.ONLY_NEW_USER_PERMISSION_TYPE){
                count=0L;
            } else{
                count = workspaceUserCount;
            }
            rule.setPermissionUserCount(count);
        }
        return new PageInfo<>(recordList,doPage.getTotal());
    }


    @Override
    public PageInfo<String> getPermissionUserList(RulePermissionUsersGetRequest request) {
        ECConfigTemplateApplyRuleDO ruleDO = ecConfigTemplateApplyRuleMapper.selectByRuleId(request.getRuleId());
        if(ruleDO==null){
            throw new DSSRuntimeException("rule does not exist,ruleId" + request.getRuleId());
        }
        if(ruleDO.getPermissionType()==ECConfTemplateApplyRule.SPECIFIED_USER_PERMISSION_TYPE){
            PageHelper.startPage(request.getPageNow(), request.getPageSize());
            List<ECConfigTemplateApplyRuleUserDO> dos= ecConfigTemplateApplyRuleUserMapper.selectByRuleId(request.getRuleId());
            com.github.pagehelper.PageInfo<ECConfigTemplateApplyRuleUserDO> doPage = new com.github.pagehelper.PageInfo<>(dos);
            List<String> userNames=doPage.getList().stream().map(ECConfigTemplateApplyRuleUserDO::getUserName).collect(Collectors.toList());
            return new PageInfo<>(userNames, doPage.getTotal());

        }else{
            return dssWorkspaceUserService.getAllWorkspaceUsersPage(ruleDO.getWorkspaceId(), request.getPageNow(), request.getPageSize());
        }
    }

    @Override
    public ECConfTemplateApplyRule getRule(String ruleId) {
        ECConfigTemplateApplyRuleDO ruleDO= ecConfigTemplateApplyRuleMapper.selectByRuleId(ruleId);
        if(ruleDO==null){
            return null;
        }else{
            ECConfTemplate template = ecConfTemplateService.getTemplate(ruleDO.getTemplateId());
            if(template==null){
                LOGGER.error("rule related template is not exist templateId:{},tempalteName:{}",ruleDO.getTemplateId(),ruleDO.getTemplateName());
                throw new DSSRuntimeException("规则关联的参数模板不存在，请检查是否已删除");
            }
            return ECConfTemplateApplyRule.fromDO(ruleDO, template);

        }
    }

    @Override
    public void deleteRule(String ruleId) {
        ecConfigTemplateApplyRuleUserMapper.deleteByRuleId(ruleId);
        ecConfigTemplateApplyRuleMapper.deleteByRuleId(ruleId);
    }

    @Override
    public PageInfo<ECTemplateApplyRecord> getExecuteRecords(ECConfTemplateRuleExecuteRecordGetRequest request) {
            PageHelper.startPage(request.getPageNow(), request.getPageSize());
            List<ECConfigTemplateApplyRuleExecuteRecordDO> dos= ecConfigTemplateApplyRuleExecuteRecordMapper.selectRecords(request);
            com.github.pagehelper.PageInfo<ECConfigTemplateApplyRuleExecuteRecordDO> doPage = new com.github.pagehelper.PageInfo<>(dos);
            List<ECTemplateApplyRecord> userNames=doPage.getList().stream().map(ECTemplateApplyRecord::fromDO).collect(Collectors.toList());
            return new PageInfo<>(userNames, doPage.getTotal());
    }

    @Override
    public PageInfo<ECConfTemplateApplyInfoVO> getTemplateApplyRecordGroupByApplication(ConfTemplateApplyInfoGetRequest request
    ,Long workspaceId) {
        ECConfTemplate template = ecConfTemplateService.getTemplate(request.getTemplateId());
        if(template==null){
            throw new DSSRuntimeException("template does not exit. templateId:" + request.getTemplateId());
        }
        Map<String,List<String>> groupUser=
        ecConfigTemplateApplyRuleExecuteRecordMapper.selectUserCurrentRecordsByTemplate(template.getName(), request.getUsername(),workspaceId)
                .stream()
                .collect(Collectors.groupingBy(ECConfigTemplateApplyRuleExecuteRecordDO::getApplication,
                        Collectors.mapping(ECConfigTemplateApplyRuleExecuteRecordDO::getUserName, Collectors.toList())));
        int offset=(request.getPageNow()-1)*request.getPageSize();
        int length=request.getPageSize();
        List<ECConfTemplateApplyInfoVO> records = new ArrayList<>(length);

        int i=0;
        for (Map.Entry<String, List<String>> entry : groupUser.entrySet()) {
            if(i<offset){
                continue;
            } else if (i==offset+length) {
               break;
            }
            records.add(new ECConfTemplateApplyInfoVO(entry.getKey(), entry.getValue()));
            i++;
        }
        return new PageInfo<>(records,groupUser.size());
    }
}

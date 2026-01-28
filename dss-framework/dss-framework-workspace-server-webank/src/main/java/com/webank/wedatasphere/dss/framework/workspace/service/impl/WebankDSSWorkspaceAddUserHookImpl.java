package com.webank.wedatasphere.dss.framework.workspace.service.impl;

import com.webank.wedatasphere.dss.framework.workspace.bean.ECConfTemplateApplyRule;
import com.webank.wedatasphere.dss.framework.workspace.bean.PermissionUser;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.ECConfTemplateRuleGetRequest;
import com.webank.wedatasphere.dss.framework.workspace.dao.ECConfigTemplateApplyRuleMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.EcConfigTemplateApplyRuleDepartmentMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateApplyRuleDO;
import com.webank.wedatasphere.dss.common.server.esb.http.HttpStaffInfoGetter;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceAddUserHook;
import com.webank.wedatasphere.dss.framework.workspace.service.ECConfTemplateApplyRuleService;
import com.webank.wedatasphere.dss.common.conf.WorkspaceServerConstant;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: xlinliu
 * Date: 2023/6/30
 */
@Service
public class WebankDSSWorkspaceAddUserHookImpl implements DSSWorkspaceAddUserHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebankDSSWorkspaceAddUserHookImpl.class);
    @Autowired
    ECConfigTemplateApplyRuleMapper ecConfigTemplateApplyRuleMapper;
    @Autowired
    ECConfTemplateApplyRuleService ecConfTemplateApplyRuleService;
    @Autowired
    EcConfigTemplateApplyRuleDepartmentMapper ecConfigTemplateApplyRuleDepartmentMapper;
    @Autowired
    HttpStaffInfoGetter httpStaffInfoGetter;
    @Override
    public void beforeAdd(String userName, long workspace) {
        // do nothing
    }

    @Override
    public void afterAdd(String userName, long workspaceId) {
        ECConfTemplateRuleGetRequest filter = new ECConfTemplateRuleGetRequest();
        filter.setWorkspaceId(workspaceId);
        LOGGER.info("start to execute after add user to workspace hook.username:{},workspaceId:{}",userName,workspaceId);
        filter.setRuleType(ECConfTemplateApplyRule.RULE_TYPE_NEWUSER);
        List<ECConfigTemplateApplyRuleDO> dos = ecConfigTemplateApplyRuleMapper.getRules(filter);
        String fullOrgNameByUsername = httpStaffInfoGetter.getFullOrgNameByUsername(userName);
        List<String> ruleIds = new ArrayList<>();
        if(StringUtil.isNotBlank(fullOrgNameByUsername)){
            String departmentName = fullOrgNameByUsername.split(WorkspaceServerConstant.DEFAULT_STAFF_SPLIT)[0];
            ruleIds = ecConfigTemplateApplyRuleDepartmentMapper.selectBydDepartment(departmentName, workspaceId);
        }
        List<String> ruleInfo=new ArrayList<>(dos.size());
        for (ECConfigTemplateApplyRuleDO ruleDO : dos) {
            String ruleId=ruleDO.getRuleId();
            if(ECConfTemplateApplyRule.ONLY_NEW_USER_PERMISSION_TYPE==ruleDO.getPermissionType()||ruleIds.contains(ruleId)) {
                ECConfTemplateApplyRule rule = ecConfTemplateApplyRuleService.getRule(ruleId);
                rule.setPermissionUsers(Collections.singletonList(new PermissionUser(userName)));
                ecConfTemplateApplyRuleService.executeRule(rule, "system", workspaceId);
                ruleInfo.add(ruleId + "#" + rule.getTemplate().getName());
            }
        }
        LOGGER.info("successfully execute after add user to workspace hook.username:{},workspaceId:{},rule count:{},ruleInfo:{}",
                userName,
                workspaceId,
                dos.size(),
                String.join(",", ruleInfo));
    }
}

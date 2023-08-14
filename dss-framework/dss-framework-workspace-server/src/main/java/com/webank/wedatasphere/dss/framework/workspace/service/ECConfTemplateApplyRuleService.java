package com.webank.wedatasphere.dss.framework.workspace.service;

import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECConfTemplateApplyRule;
import com.webank.wedatasphere.dss.framework.workspace.bean.ECTemplateApplyRecord;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.*;
import com.webank.wedatasphere.dss.framework.workspace.bean.vo.ECConfTemplateApplyInfoVO;
import com.webank.wedatasphere.dss.framework.workspace.bean.vo.ECConfTemplateApplyRuleVO;

/**
 * 参数模板规则服务
 * Author: xlinliu
 * Date: 2023/6/26
 */
public interface ECConfTemplateApplyRuleService {
    ECConfTemplateApplyRule saveRule(ECConfTemplateApplyRuleSaveRequest saveRequest, String operator, Long workspaceId);
    void executeRule(ECConfTemplateApplyRule rule,String operator,Long workspaceId);

    PageInfo<ECConfTemplateApplyRuleVO> getRuleList(ECConfTemplateRuleGetRequest request);

    PageInfo<String> getPermissionUserList(RulePermissionUsersGetRequest request);

    ECConfTemplateApplyRule getRule(String ruleId);

    void deleteRule(String ruleId);

    PageInfo<ECTemplateApplyRecord> getExecuteRecords(ECConfTemplateRuleExecuteRecordGetRequest request);

    PageInfo<ECConfTemplateApplyInfoVO> getTemplateApplyRecordGroupByApplication(ConfTemplateApplyInfoGetRequest request
    , Long workspaceId);

}

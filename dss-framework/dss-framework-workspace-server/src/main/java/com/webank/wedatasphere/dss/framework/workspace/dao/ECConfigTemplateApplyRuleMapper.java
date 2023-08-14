package com.webank.wedatasphere.dss.framework.workspace.dao;

import com.webank.wedatasphere.dss.framework.workspace.bean.request.ECConfTemplateRuleGetRequest;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateApplyRuleDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author arionliu
* @description 针对表【dss_ec_config_template_apply_rule(模板应用规则表)】的数据库操作Mapper
* @createDate 2023-06-26 11:29:59
* @Entity com.webank.wedatasphere.dss.framework.workspace.dao.entity.EcConfigTemplateApplyRuleDO
*/
public interface ECConfigTemplateApplyRuleMapper {

    int deleteByRuleId(String ruleId);

    int insert(ECConfigTemplateApplyRuleDO record);

    void updateStatus(@Param("ruleId") String ruleId,@Param("status")int status,@Param("executeUser")String executeUser);


    ECConfigTemplateApplyRuleDO selectByRuleId(String ruleId);
    List<ECConfigTemplateApplyRuleDO> getRules(ECConfTemplateRuleGetRequest filter);



}

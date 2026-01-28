package com.webank.wedatasphere.dss.framework.workspace.dao;

import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateApplyRuleDepartmentDO;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateApplyRuleUserDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author v_xuanzhou
* @description 针对表【dss_ec_config_template_apply_rule_department(模板应用规则覆盖部门表)】的数据库操作Mapper
* @createDate 2023-04-27 11:37:19
* @Entity com.webank.wedatasphere.dss.framework.workspace.dao.entity.EcConfigTemplateApplyRuleDepartmentMapper
*/
public interface EcConfigTemplateApplyRuleDepartmentMapper {

    int deleteByRuleId(String ruleId);
    int insertBatch(List<ECConfigTemplateApplyRuleDepartmentDO> list);
    List<ECConfigTemplateApplyRuleUserDO> selectByRuleId(String ruleId);
    List<String> selectBydDepartment(@Param("departmentName") String departmentName, @Param("workspaceId") Long workspaceId);

}

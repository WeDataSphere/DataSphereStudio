package com.webank.wedatasphere.dss.framework.workspace.dao;

import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateApplyRuleUserDO;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.PermissionUserCountDO;
import org.apache.ibatis.annotations.MapKey;

import java.util.List;
import java.util.Map;

/**
* @author arionliu
* @description 针对表【dss_ec_config_template_apply_rule_user(模板应用规则覆盖用户表)】的数据库操作Mapper
* @createDate 2023-06-26 11:37:19
* @Entity com.webank.wedatasphere.dss.framework.workspace.dao.entity.EcConfigTemplateApplyRuleUserDO
*/
public interface ECConfigTemplateApplyRuleUserMapper {

    int deleteByRuleId(String ruleId);

    int insert(ECConfigTemplateApplyRuleUserDO record);
    int insertBatch(List<ECConfigTemplateApplyRuleUserDO> list);
    List<ECConfigTemplateApplyRuleUserDO> selectByRuleId(String ruleId);

    int insertSelective(ECConfigTemplateApplyRuleUserDO record);

    ECConfigTemplateApplyRuleUserDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ECConfigTemplateApplyRuleUserDO record);

    int updateByPrimaryKey(ECConfigTemplateApplyRuleUserDO record);



    @MapKey("groupId")
    Map<String, PermissionUserCountDO> getPermissionUserCount(List<String> list);

}

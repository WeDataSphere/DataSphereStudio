package com.webank.wedatasphere.dss.framework.workspace.dao;

import com.webank.wedatasphere.dss.framework.workspace.bean.request.ConfTemplateApplyInfoGetRequest;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.ECConfTemplateRuleExecuteRecordGetRequest;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateApplyRuleExecuteRecordDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author arionliu
* @description 针对表【dss_ec_config_template_apply_rule_execute_record(模板应用规则执行记录表)】的数据库操作Mapper
* @createDate 2023-06-26 11:36:31
* @Entity com.webank.wedatasphere.dss.framework.workspace.dao.entity.EcConfigTemplateApplyRuleExecuteRecordDO
*/
public interface ECConfigTemplateApplyRuleExecuteRecordMapper {

    int deleteByPrimaryKey(Long id);

    int insert(ECConfigTemplateApplyRuleExecuteRecordDO record);
    int insertBatch(List<ECConfigTemplateApplyRuleExecuteRecordDO> list);

    int insertSelective(ECConfigTemplateApplyRuleExecuteRecordDO record);

    List<ECConfigTemplateApplyRuleExecuteRecordDO> selectRecords(ECConfTemplateRuleExecuteRecordGetRequest filter);

    /**
     * 获取用户当前生效的配置记录
     */
    List<ECConfigTemplateApplyRuleExecuteRecordDO> selectUserCurrentRecordsByTemplate(@Param("templateName")String templateName
            ,@Param("username") String username,@Param("workspaceId") Long workspaceId);

    int updateByPrimaryKeySelective(ConfTemplateApplyInfoGetRequest filter);

    int updateByPrimaryKey(ECConfigTemplateApplyRuleExecuteRecordDO record);

}

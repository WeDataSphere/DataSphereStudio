package com.webank.wedatasphere.dss.framework.project.dao;


import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkflowDefaultDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目模板关联关系
 */
@Mapper
public interface ECTemplateWorkflowDefaultMapper {

    /**
     * 插入工作流默认模板
     * @param templateProject
     * @return
     */
    int insert(ECTemplateWorkflowDefaultDO templateProject);

    /**
     * 批量插入工作流默认模板
     * @param defaultDO
     * @return
     */
    int batchInsert(@Param("list") List<ECTemplateWorkflowDefaultDO> defaultDO);

    /**
     * 删除工作流默认模板
     * @param orchestratorId
     */
    void deleteWorkflowTemplateRef(@Param("orchestratorId")Long orchestratorId);

    /**
     * 查询工作流默认模板
     * @param orchestratorId
     * @return
     */
    List<ECTemplateWorkflowDefaultDO> getWorkflowDefaultTemplates(@Param("orchestratorId") Long orchestratorId);

}

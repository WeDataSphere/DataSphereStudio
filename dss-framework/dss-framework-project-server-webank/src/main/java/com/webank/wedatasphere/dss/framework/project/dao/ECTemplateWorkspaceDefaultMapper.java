package com.webank.wedatasphere.dss.framework.project.dao;

import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkspaceDefaultDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author v_sunpengwang
 */
@Mapper
public interface ECTemplateWorkspaceDefaultMapper {


    /**
     * 查询命名空间默认模板
     **/
    List<ECTemplateWorkspaceDefaultDO> getWorkspaceDefaultTemplates(@Param("workspaceId") Long workspaceId);


    /**
     * 删除模板
     **/
    void deleteWorkspaceTemplateRef(@Param("workspaceId") Long workspaceId, @Param("templateIdList") List<String> templateIdList);


    void batchInsert(@Param("list") List<ECTemplateWorkspaceDefaultDO> list);


}

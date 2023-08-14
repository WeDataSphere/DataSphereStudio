package com.webank.wedatasphere.dss.framework.project.dao;

import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkflowDO;
import com.webank.wedatasphere.dss.framework.project.request.DSSWorkflowUseTemplateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @author v_kangkangyuan
 */
@Mapper
public interface ECTemplateWorkflowMapper {

    /**
     * 插入关联关系
     * @param template
     * @return
     */
    int insert(ECTemplateWorkflowDO template);

    /**
     * 批量插入关联关系
     * @param list
     * @return
     */
    int batchInsert(List<ECTemplateWorkflowDO> list);

    /**
     * 工作流更改默认模板
     * @param template
     * @return
     */
    int updateDefaultTemplate(ECTemplateWorkflowDO template);

    /**
     * 工作流引用模板列表查询
     * @param request
     * @return
     */
    List<ECTemplateWorkflowDO> getAllWorkflowUseTemplates(DSSWorkflowUseTemplateRequest request);

    /**
     * 工作流或节点取消引用关系时，删除关联关系
     * @param orchestratorId 编排的id
     * @param flowId 工作流的id
     * @return
     */
    int deleteWorkflowUse(@Param("orchestratorId") Long orchestratorId, @Param("flowId") Long flowId);

    /**
     * 删除一个编排下所有工作流（子工作流）的引用
     * @param orchestratorId 编排id
     * @return
     */
    int deleteAllWorkflowUseOfOrc(@Param("orchestratorId") Long orchestratorId);

    /**
     * 工作流引用模板搜索模块
     * 获取项目名称列表
     * @return
     */
    List<String> getTemplateProjectNames(@Param("templateId") String templateId);

    /**
     * 工作流引用模板搜索模块
     * 获取工作流名称列表
     * @return
     */
    List<String> getTemplateOrchestratorNames(@Param("templateId") String templateId);
}

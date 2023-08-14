package com.webank.wedatasphere.dss.framework.project.service;
import com.webank.wedatasphere.dss.framework.project.entity.vo.ProjectTemplateVO;

import java.util.List;

/**
 * 编排与参数模板相关的服务。
 * Author: xlinliu
 * Date: 2023/7/27
 */
public interface OrchestratorECConfTemplateService {
    /**
     * 获取项目列表
     * @param projectId 项目id，必选
     * @return
     */
    List<ProjectTemplateVO> getTemplates(Long workpaceId,Long projectId);

}

package com.webank.wedatasphere.dss.framework.project.service;
import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
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
    List<ProjectTemplateVO> getTemplates(Long workpaceId,Long projectId) throws DSSErrorException;

    /**
     * 获取项目列表
     * @param username 用户名，必选
     * @return
     */
    List<ProjectTemplateVO> getTemplatesByUser(Long workpaceId,String username);

    List<ProjectTemplateVO> getWorkspaceTemplates(Long workspaceId);

}

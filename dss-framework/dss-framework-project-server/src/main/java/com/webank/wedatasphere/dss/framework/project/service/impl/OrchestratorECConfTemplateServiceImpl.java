package com.webank.wedatasphere.dss.framework.project.service.impl;

import com.webank.wedatasphere.dss.common.constant.project.ProjectUserPrivEnum;
import com.webank.wedatasphere.dss.framework.project.dao.DSSProjectMapper;
import com.webank.wedatasphere.dss.framework.project.entity.vo.ProjectTemplateVO;
import com.webank.wedatasphere.dss.framework.project.service.OrchestratorECConfTemplateService;
import com.webank.wedatasphere.dss.framework.workspace.dao.ECConfigTemplateMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.ECConfigTemplateUserMapper;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateDO;
import com.webank.wedatasphere.dss.framework.workspace.dao.entity.ECConfigTemplateUserDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: xlinliu
 * Date: 2023/7/27
 */
@Service
public class OrchestratorECConfTemplateServiceImpl implements OrchestratorECConfTemplateService {

    @Autowired
    ECConfigTemplateUserMapper ecConfigTemplateUserMapper;
    @Autowired
    ECConfigTemplateMapper ecConfigTemplateMapper;
    @Autowired
    DSSProjectMapper dssProjectMapper;




    @Override
    public List<ProjectTemplateVO> getTemplates(Long  workspaceId,Long projectId) {
        List<String> releaseList = dssProjectMapper.getProjectUserNames(workspaceId, projectId, ProjectUserPrivEnum.PRIV_RELEASE.getRank());
        List<String> editList = dssProjectMapper.getProjectUserNames(workspaceId, projectId, ProjectUserPrivEnum.PRIV_EDIT.getRank());
        Set<String> users = new HashSet<>(releaseList);
        users.addAll(editList);
        Set<String> specifiedUserTemplateIds = ecConfigTemplateUserMapper.selectByUsers(workspaceId, new ArrayList<>(users)).stream()
                .map(ECConfigTemplateUserDO::getTemplateId).collect(Collectors.toSet());
        Map<String, List<ECConfigTemplateDO>> engineTypeGroupedTemplate =
                ecConfigTemplateMapper.getTemplatesByWorkspaceId(workspaceId).stream()
                        .filter(e -> e.getPermissionType() == 0 || specifiedUserTemplateIds.contains(e.getTemplateId()))
                        .collect(Collectors.groupingBy(ECConfigTemplateDO::getEngineType));
        List<ProjectTemplateVO> projectTemplateVOS = new ArrayList<>(engineTypeGroupedTemplate.size());
        for (Map.Entry<String, List<ECConfigTemplateDO>> entry : engineTypeGroupedTemplate.entrySet()) {
            List<ProjectTemplateVO.Item> template = entry.getValue().stream()
                    .map(e -> new ProjectTemplateVO.Item(e.getTemplateId(), e.getName(), false))
                    .collect(Collectors.toList());
            projectTemplateVOS.add(new ProjectTemplateVO(entry.getKey(), template));

        }
        return projectTemplateVOS;
    }
}

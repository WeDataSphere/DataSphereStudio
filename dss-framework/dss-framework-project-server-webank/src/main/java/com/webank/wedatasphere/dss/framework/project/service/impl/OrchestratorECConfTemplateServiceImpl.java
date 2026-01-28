package com.webank.wedatasphere.dss.framework.project.service.impl;

import com.webank.wedatasphere.dss.common.constant.project.ProjectUserPrivEnum;
import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.common.utils.DSSExceptionUtils;
import com.webank.wedatasphere.dss.framework.project.dao.ECTemplateWorkspaceDefaultMapper;
import com.webank.wedatasphere.dss.framework.project.dao.WebankDSSProjectMapper;
import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkspaceDefaultDO;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: xlinliu
 * Date: 2023/7/27
 */
@Service
public class OrchestratorECConfTemplateServiceImpl implements OrchestratorECConfTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorECConfTemplateServiceImpl.class);
    @Autowired
    ECConfigTemplateUserMapper ecConfigTemplateUserMapper;
    @Autowired
    ECConfigTemplateMapper ecConfigTemplateMapper;
    @Autowired
    WebankDSSProjectMapper webankDSSProjectMapper;

    @Autowired
    ECTemplateWorkspaceDefaultMapper ecTemplateWorkspaceDefaultMapper;


    @Override
    public List<ProjectTemplateVO> getTemplates(Long workspaceId, Long projectId) throws DSSErrorException {
        List<ProjectTemplateVO> projectTemplateVOS = new ArrayList<>();
        Long dbWorkspaceId = webankDSSProjectMapper.getProjectWorkspaceIdById(projectId);
        if (!Objects.equals(dbWorkspaceId, workspaceId)) {
            DSSExceptionUtils.dealErrorException(63335, "工作流所在工作空间和cookie中不一致，请刷新页面后，再次获取！", DSSErrorException.class);
        }
        List<String> releaseList = webankDSSProjectMapper.getProjectUserNames(workspaceId, projectId, ProjectUserPrivEnum.PRIV_RELEASE.getRank());
        if (releaseList == null || releaseList.isEmpty()) {
            LOGGER.warn("workspaceId is {}, projectId is {},get release list is empty, ", workspaceId, projectId);
            return projectTemplateVOS;
        }
        List<String> editList = webankDSSProjectMapper.getProjectUserNames(workspaceId, projectId, ProjectUserPrivEnum.PRIV_EDIT.getRank());
        Set<String> users = new HashSet<>(releaseList);
        users.addAll(editList);
        Set<String> specifiedUserTemplateIds = ecConfigTemplateUserMapper.selectByUsers(workspaceId, new ArrayList<>(users)).stream()
                .map(ECConfigTemplateUserDO::getTemplateId).collect(Collectors.toSet());
        Map<String, List<ECConfigTemplateDO>> engineTypeGroupedTemplate =
                ecConfigTemplateMapper.getTemplatesByWorkspaceId(workspaceId).stream()
                        .filter(e -> (e.getPermissionType() == 0 || specifiedUserTemplateIds.contains(e.getTemplateId()))&&!"*".equals(e.getEngineType()))
                        .collect(Collectors.groupingBy(ECConfigTemplateDO::getEngineType));
        // List<ProjectTemplateVO> projectTemplateVOS = new ArrayList<>(engineTypeGroupedTemplate.size());
        for (Map.Entry<String, List<ECConfigTemplateDO>> entry : engineTypeGroupedTemplate.entrySet()) {
            List<ProjectTemplateVO.Item> template = entry.getValue().stream()
                    .map(e -> new ProjectTemplateVO.Item(e.getTemplateId(), e.getName(), false))
                    .collect(Collectors.toList());
            projectTemplateVOS.add(new ProjectTemplateVO(entry.getKey(), template));

        }
        return projectTemplateVOS;
    }

    @Override
    public List<ProjectTemplateVO> getTemplatesByUser(Long workspaceId, String username) {
        List<ProjectTemplateVO> projectTemplateVOS = new ArrayList<>();
        Set<String> users = new HashSet<>();
        users.add(username);
        Set<String> specifiedUserTemplateIds = ecConfigTemplateUserMapper.selectByUsers(workspaceId, new ArrayList<>(users)).stream()
                .map(ECConfigTemplateUserDO::getTemplateId).collect(Collectors.toSet());
        Map<String, List<ECConfigTemplateDO>> engineTypeGroupedTemplate =
                ecConfigTemplateMapper.getTemplatesByWorkspaceId(workspaceId).stream()
                        .filter(e -> (e.getPermissionType() == 0 || specifiedUserTemplateIds.contains(e.getTemplateId()))&&!"*".equals(e.getEngineType()))
                        .collect(Collectors.groupingBy(ECConfigTemplateDO::getEngineType));
        for (Map.Entry<String, List<ECConfigTemplateDO>> entry : engineTypeGroupedTemplate.entrySet()) {
            List<ProjectTemplateVO.Item> template = entry.getValue().stream()
                    .map(e -> new ProjectTemplateVO.Item(e.getTemplateId(), e.getName(), false))
                    .collect(Collectors.toList());
            projectTemplateVOS.add(new ProjectTemplateVO(entry.getKey(), template));
        }
        return projectTemplateVOS;
    }

    @Override
    public List<ProjectTemplateVO> getWorkspaceTemplates(Long workspaceId) {

        List<ProjectTemplateVO> projectTemplateVOS = new ArrayList<>();
        // 过滤出用户全部可见的模板信息
        Map<String, List<ECConfigTemplateDO>> engineTypeGroupedTemplate = ecConfigTemplateMapper.getTemplatesByWorkspaceId(workspaceId).stream()
                .filter(e -> (e.getPermissionType() == 0) && !"*".equals(e.getEngineType()))
                .collect(Collectors.groupingBy(ECConfigTemplateDO::getEngineType));

        List<ECTemplateWorkspaceDefaultDO> defaultTemplates = ecTemplateWorkspaceDefaultMapper.getWorkspaceDefaultTemplates(workspaceId);
        Set<String> defaultTemplateIds = defaultTemplates.stream().map(ECTemplateWorkspaceDefaultDO::getTemplateId).collect(Collectors.toSet());

        for(Map.Entry<String, List<ECConfigTemplateDO>> entry: engineTypeGroupedTemplate.entrySet()){

            String  enginType = entry.getKey();

            List<ProjectTemplateVO.Item> child = entry.getValue().stream().map(template ->
                    new ProjectTemplateVO.Item(template.getTemplateId(),template.getName(),
                            defaultTemplateIds.contains(template.getTemplateId()))).collect(Collectors.toList());

            projectTemplateVOS.add(new ProjectTemplateVO(enginType,child));
        }


        return projectTemplateVOS;
    }
}

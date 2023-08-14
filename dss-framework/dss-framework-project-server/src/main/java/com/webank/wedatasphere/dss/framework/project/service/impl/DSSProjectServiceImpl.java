/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.framework.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.webank.wedatasphere.dss.appconn.core.AppConn;
import com.webank.wedatasphere.dss.appconn.manager.AppConnManager;
import com.webank.wedatasphere.dss.common.entity.BmlResource;
import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.common.label.DSSLabelUtil;
import com.webank.wedatasphere.dss.common.label.EnvDSSLabel;
import com.webank.wedatasphere.dss.common.utils.DSSCommonUtils;
import com.webank.wedatasphere.dss.common.utils.DSSExceptionUtils;
import com.webank.wedatasphere.dss.common.utils.RpcAskUtils;
import com.webank.wedatasphere.dss.framework.project.conf.ProjectConf;
import com.webank.wedatasphere.dss.framework.project.contant.ProjectServerResponse;
import com.webank.wedatasphere.dss.common.constant.project.ProjectUserPrivEnum;
import com.webank.wedatasphere.dss.framework.project.dao.DSSProjectMapper;
import com.webank.wedatasphere.dss.framework.project.dao.DSSProjectUserMapper;
import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectDO;
import com.webank.wedatasphere.dss.framework.project.entity.ProjectOperateRecordBO;
import com.webank.wedatasphere.dss.framework.project.entity.po.ProjectRelationPo;
import com.webank.wedatasphere.dss.framework.project.entity.request.*;
import com.webank.wedatasphere.dss.framework.project.entity.response.ProjectResponse;
import com.webank.wedatasphere.dss.framework.project.entity.vo.DSSProjectVo;
import com.webank.wedatasphere.dss.framework.project.entity.vo.ProjectCopyVO;
import com.webank.wedatasphere.dss.framework.project.entity.vo.ProjectInfoVo;
import com.webank.wedatasphere.dss.framework.project.entity.vo.QueryProjectVo;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateRecordStatusEnum;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateTypeEnum;
import com.webank.wedatasphere.dss.framework.project.exception.DSSProjectErrorException;
import com.webank.wedatasphere.dss.framework.project.job.ProjectCopyEnv;
import com.webank.wedatasphere.dss.framework.project.job.ProjectCopyJob;
import com.webank.wedatasphere.dss.framework.project.service.*;
import com.webank.wedatasphere.dss.framework.project.utils.ProjectStringUtils;
import com.webank.wedatasphere.dss.framework.release.entity.export.BatchExportResult;
import com.webank.wedatasphere.dss.framework.release.entity.orchestrator.OrchestratorBatchImportInfo;
import com.webank.wedatasphere.dss.framework.release.service.ExportService;
import com.webank.wedatasphere.dss.framework.release.service.ImportService;
import com.webank.wedatasphere.dss.framework.release.service.ReleaseService;
import com.webank.wedatasphere.dss.framework.workspace.bean.DSSWorkspace;
import com.webank.wedatasphere.dss.framework.workspace.bean.DSSWorkspaceUserRole;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceRoleService;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceService;
import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorDetail;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestFrameworkConvertOrchestration;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestOrchestratorInfos;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.ResponseOrchestratorInfos;
import com.webank.wedatasphere.dss.sender.service.DSSSenderServiceFactory;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.app.structure.project.ProjectDeletionOperation;
import com.webank.wedatasphere.dss.standard.app.structure.project.ProjectService;
import com.webank.wedatasphere.dss.standard.app.structure.project.ref.RefProjectContentRequestRef;
import com.webank.wedatasphere.dss.standard.common.desc.AppInstance;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.linkis.rpc.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.webank.wedatasphere.dss.framework.project.utils.ProjectOperationUtils.tryProjectOperation;

public class DSSProjectServiceImpl extends ServiceImpl<DSSProjectMapper, DSSProjectDO> implements DSSProjectService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DSSProjectServiceImpl.class);
    @Autowired
    private DSSProjectMapper projectMapper;
    @Autowired
    private DSSProjectUserService projectUserService;
    @Autowired
    private DSSProjectUserMapper projectUserMapper;
    @Autowired
    private DSSWorkspaceService dssWorkspaceService;
    @Autowired
    DSSWorkspaceRoleService dssWorkspaceRoleService;
    @Autowired
    private DSSFrameworkProjectService dssFrameworkProjectService;
    @Autowired
    private ProjectCopyEnv projectCopyEnv;
    @Autowired
    private DSSOrchestratorService DSSOrchestratorService;
    @Autowired
    private DSSProjectOperateService DSSProjectOperateService;
    @Autowired
    private ExportService exportService;
    @Resource
    private ImportService importService;
    @Resource
    private ReleaseService releaseService;

    private final ThreadFactory projectCopyThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("dss-project—operate-thread-%d")
            .setDaemon(false)
            .build();

    private final ExecutorService projectCopyThreadPool = new ThreadPoolExecutor(5, 200, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024), projectCopyThreadFactory, new ThreadPoolExecutor.AbortPolicy());

    public static final String MODE_SPLIT = ",";
    public static final String KEY_SPLIT = "-";
    private final String SUPPORT_ABILITY = ProjectConf.SUPPORT_ABILITY.getValue();
    /**
     * 单次导出的编排数量限制
     */
    public static final int ORCHS_SIZE_ONE_EXPORT = 20;
    public static final int CONCURRENT_EXPORT_IMPORT_SIZE = 20;

    @Override
    public DSSProjectDO createProject(String username, ProjectCreateRequest projectCreateRequest) {
        DSSProjectDO project = new DSSProjectDO();
        project.setName(projectCreateRequest.getName());
        project.setWorkspaceId(projectCreateRequest.getWorkspaceId());
        project.setCreateBy(username);
        project.setUsername(username);
        project.setCreateTime(new Date());
        project.setBusiness(projectCreateRequest.getBusiness());
        project.setProduct(projectCreateRequest.getProduct());
        project.setUpdateTime(new Date());
        project.setDescription(projectCreateRequest.getDescription());
        project.setApplicationArea(projectCreateRequest.getApplicationArea());
        //开发流程，编排模式组拼接 前后进行英文逗号接口
        project.setDevProcess(ProjectStringUtils.getModeStr(projectCreateRequest.getDevProcessList()));
        project.setOrchestratorMode(ProjectStringUtils.getModeStr(projectCreateRequest.getOrchestratorModeList()));
        projectMapper.insert(project);
        return project;
    }

    //修改dss_project工程字段
    @Override
    public void modifyProject(String username, ProjectModifyRequest projectModifyRequest) throws DSSProjectErrorException {
        //校验当前登录用户是否含有修改权限
//        projectUserService.isEditProjectAuth(projectModifyRequest.getId(), username);
        DSSProjectDO project = new DSSProjectDO();
        //修改的字段
        project.setDescription(projectModifyRequest.getDescription());
        project.setUpdateTime(new Date());
        project.setUpdateByStr(username);
        project.setDevProcess(ProjectStringUtils.getModeStr(projectModifyRequest.getDevProcessList()));
        if (StringUtils.isNotBlank(projectModifyRequest.getApplicationArea())) {
            project.setApplicationArea(Integer.valueOf(projectModifyRequest.getApplicationArea()));
        }
        project.setOrchestratorMode(ProjectStringUtils.getModeStr(projectModifyRequest.getOrchestratorModeList()));
        project.setBusiness(projectModifyRequest.getBusiness());
        project.setProduct(projectModifyRequest.getProduct());

        UpdateWrapper<DSSProjectDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", projectModifyRequest.getId());
        updateWrapper.eq("workspace_id", projectModifyRequest.getWorkspaceId());
        projectMapper.update(project, updateWrapper);
    }

    /**
     * 修改旧dss_project工程字段
     */
    @Override
    public void modifyOldProject(DSSProjectDO updateProject, DSSProjectDO dbProject) {
        DSSProjectDO project = new DSSProjectDO();
        //修改的字段
        project.setUpdateTime(new Date());
        project.setUpdateByStr(updateProject.getUpdateByStr());
        project.setDescription(updateProject.getDescription());
        project.setBusiness(updateProject.getBusiness());
        project.setApplicationArea(updateProject.getApplicationArea());

        UpdateWrapper<DSSProjectDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", dbProject.getId());
        updateWrapper.eq("workspace_id", dbProject.getWorkspaceId());
        projectMapper.update(project, updateWrapper);
    }

    @Override
    public DSSProjectDO getProjectByName(String name) {
        QueryWrapper<DSSProjectDO> projectQueryWrapper = new QueryWrapper<>();
        projectQueryWrapper.eq("name", name);
        List<DSSProjectDO> projectList = projectMapper.selectList(projectQueryWrapper);
        return CollectionUtils.isEmpty(projectList) ? null : projectList.get(0);
    }

    @Override
    public DSSProjectDO getProjectById(Long id) {
        return projectMapper.selectById(id);
    }

    @Override
    public List<ProjectResponse> getListByParam(ProjectQueryRequest projectRequest) {
        //根据dss_project、dss_project_user查询出所在空间登录用户相关的工程
        List<QueryProjectVo> list;
        //判断工作空间是否设置了管理员能否查看该工作空间下所有项目的权限
        Integer workspaceAdminPermission = projectUserMapper.getWorkspaceAdminPermission(projectRequest.getWorkspaceId());
        if (isWorkspaceAdmin(projectRequest.getWorkspaceId(), projectRequest.getUsername())) {
            if (workspaceAdminPermission == 1){
                list = projectMapper.getListForAdmin(projectRequest);
            } else {
                list = projectMapper.getListByParam(projectRequest);
            }
        } else {
            list = projectMapper.getListByParam(projectRequest);
        }
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<ProjectResponse> projectResponseList = new ArrayList<>();
        ProjectResponse projectResponse;
        for (QueryProjectVo projectVo : list) {
            if (projectVo.getVisible() == 0) {
                continue;
            }
            projectResponse = new ProjectResponse();
            projectResponse.setApplicationArea(projectVo.getApplicationArea());
            projectResponse.setId(projectVo.getId());
            projectResponse.setBusiness(projectVo.getBusiness());
            projectResponse.setCreateBy(projectVo.getCreateBy());
            projectResponse.setDescription(projectVo.getDescription());
            projectResponse.setName(projectVo.getName());
            projectResponse.setProduct(projectVo.getProduct());
            projectResponse.setSource(projectVo.getSource());
            projectResponse.setArchive(projectVo.getArchive());
            projectResponse.setCreateTime(projectVo.getCreateTime());
            projectResponse.setUpdateTime(projectVo.getUpdateTime());
            projectResponse.setDevProcessList(ProjectStringUtils.convertList(projectVo.getDevProcess()));
            projectResponse.setOrchestratorModeList(ProjectStringUtils.convertList(projectVo.getOrchestratorMode()));
            projectResponseList.add(projectResponse);

            String pusername = projectVo.getPusername();
            String editPriv = projectVo.getId() + KEY_SPLIT + ProjectUserPrivEnum.PRIV_EDIT.getRank()
                    + KEY_SPLIT + projectRequest.getUsername();

            Map<String, List<String>> userPricMap = new HashMap<>();
            String[] tempstrArr = pusername.split(MODE_SPLIT);

             // 拆分有projectId +"-" + priv + "-" + username的拼接而成的字段，
             // 从而得到：查看权限用户、编辑权限用户、发布权限用户
            for (String s : tempstrArr) {
                String[] strArr = s.split(KEY_SPLIT);
                if(strArr.length >= 3) {
                    String key = strArr[0] + KEY_SPLIT + strArr[1];
                    userPricMap.computeIfAbsent(key, k -> new ArrayList<>());
                    userPricMap.get(key).add(strArr[2]);
                }
            }
            List<String> accessUsers = userPricMap.get(projectVo.getId() + KEY_SPLIT + ProjectUserPrivEnum.PRIV_ACCESS.getRank());
            List<String> editUsers = userPricMap.get(projectVo.getId() + KEY_SPLIT + ProjectUserPrivEnum.PRIV_EDIT.getRank());
            List<String> releaseUsers = userPricMap.get(projectVo.getId() + KEY_SPLIT + ProjectUserPrivEnum.PRIV_RELEASE.getRank());
            projectResponse.setAccessUsers(CollectionUtils.isEmpty(accessUsers) ? new ArrayList<>() : accessUsers.stream().distinct().collect(Collectors.toList()));
            projectResponse.setEditUsers(CollectionUtils.isEmpty(editUsers) ? new ArrayList<>() : editUsers.stream().distinct().collect(Collectors.toList()));
            projectResponse.setReleaseUsers(CollectionUtils.isEmpty(releaseUsers) ? new ArrayList<>() : releaseUsers.stream().distinct().collect(Collectors.toList()));

            // 用户是否具有编辑权限  编辑权限和创建者都有
            if (!StringUtils.isEmpty(pusername) &&
                    (pusername.contains(editPriv) ||
                            projectVo.getCreateBy().equals(projectRequest.getUsername()) ||
                            isWorkspaceAdmin(projectRequest.getWorkspaceId(), projectRequest.getUsername())) || projectResponse.getReleaseUsers().contains(projectRequest.getUsername())) {
                projectResponse.setEditable(true);
            } else if (isWorkspaceAdmin(projectRequest.getWorkspaceId(), projectRequest.getUsername()) ||
                    projectVo.getCreateBy().equals(projectRequest.getUsername())) {
                projectResponse.setEditable(true);
            } else {
                projectResponse.setEditable(false);
            }
        }
        return projectResponseList;
    }

    @Override
    public ProjectInfoVo getProjectInfoById(Long id) {
        return projectMapper.getProjectInfoById(id);
    }


    @Override
    public void saveProjectRelation(DSSProjectDO project, Map<AppInstance, Long> projectMap) {
        List<ProjectRelationPo> relationPos = new ArrayList<>();
        projectMap.forEach((k, v) -> relationPos.add(new ProjectRelationPo(project.getId(), k.getId(), v)));
        projectMapper.saveProjectRelation(relationPos);
    }


    @Override
    public Long getAppConnProjectId(Long dssProjectId, String appConnName, List<DSSLabel> dssLabels) throws Exception {
        AppConn appConn = AppConnManager.getAppConnManager().getAppConn(appConnName);
        List<AppInstance> appInstances = appConn.getAppDesc().getAppInstancesByLabels(dssLabels);
        if (appInstances.get(0) != null) {
            Long appInstanceId = appInstances.get(0).getId();
            return getAppConnProjectId(appInstanceId, dssProjectId);
        } else {
            LOGGER.error("the appInstances of AppConn {} is null.", appConnName);
            return null;
        }
    }

    @Override
    public Long getAppConnProjectId(Long appInstanceId, Long dssProjectId) {
        return projectMapper.getAppConnProjectId(appInstanceId, dssProjectId);
    }

    @Override
    public void deleteProject(String username, ProjectDeleteRequest projectDeleteRequest, Workspace workspace, DSSProjectDO dssProjectDO) throws Exception  {
        if (dssProjectDO == null) {
            throw new DSSErrorException(600001, "工程不存在!");
        }
        LOGGER.warn("user {} begins to delete project {} in workspace {}.", username, dssProjectDO.getName(), workspace.getWorkspaceName());
        if(!dssProjectDO.getUsername().equalsIgnoreCase(username)){
            throw new DSSErrorException(600002, "刪除工程失敗，沒有删除权限!" );
        }
        if(projectDeleteRequest.isIfDelOtherSys()) {
            LOGGER.warn("User {} requires to delete all projects with name {} in third-party AppConns.", username, dssProjectDO.getName());
            Map<AppInstance, Long> appInstanceToRefProjectId = new HashMap<>(10);
            tryProjectOperation((appConn, appInstance) -> {
                    Long refProjectId = getAppConnProjectId(appInstance.getId(), projectDeleteRequest.getId());
                    if(refProjectId == null) {
                        LOGGER.warn("delete project {} for third-party AppConn {} is ignored, appInstance is {}. Caused by: the refProjectId is null.",
                                dssProjectDO.getName(), appConn.getAppDesc().getAppName(), appInstance.getBaseUrl());
                        return false;
                    } else {
                        appInstanceToRefProjectId.put(appInstance, refProjectId);
                        return true;
                    }
                }, workspace, ProjectService::getProjectDeletionOperation,
                null,
                (appInstance, refProjectContentRequestRef) -> refProjectContentRequestRef.setProjectName(dssProjectDO.getName())
                        .setRefProjectId(appInstanceToRefProjectId.get(appInstance)).setUserName(username),
                (structureOperation, structureRequestRef) -> ((ProjectDeletionOperation) structureOperation).deleteProject((RefProjectContentRequestRef) structureRequestRef),
                null, "delete refProject " + dssProjectDO.getName());
        }
        projectMapper.deleteProject(projectDeleteRequest.getId());
        LOGGER.warn("User {} deleted project {}.", username, dssProjectDO.getName());
    }

    @Override
    public List<String> getProjectAbilities(String username) {
        LOGGER.info("{} begins to get project ability", username);
        return Arrays.asList(SUPPORT_ABILITY.trim().split(","));
    }

    @Override
    public boolean isDeleteProjectAuth(Long projectId, String username) throws DSSProjectErrorException {
        //校验当前登录用户是否含有删除权限，默认创建用户可以删除工程
        QueryWrapper<DSSProjectDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", projectId);
        queryWrapper.eq("create_by", username);
        long count = projectMapper.selectCount(queryWrapper);
        if (count == 0) {
            DSSExceptionUtils.dealErrorException(ProjectServerResponse.PROJECT_NOT_EDIT_AUTH.getCode(), ProjectServerResponse.PROJECT_NOT_EDIT_AUTH.getMsg(), DSSProjectErrorException.class);
        }
        return true;
    }

    private boolean isWorkspaceAdmin(Long workspaceId, String username) {
        return !projectUserMapper.getUserWorkspaceAdminRole(workspaceId, username).isEmpty();
    }

    @Override
    public List<ProjectResponse> getDeletedProjects(ProjectQueryRequest projectRequest) {
        //根据dss_project、dss_project_user查询出所在空间登录用户相关的工程,再删选出其中是已删除的项目
        List<QueryProjectVo> list = projectMapper.getDeletedProjects(projectRequest);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<ProjectResponse> projectResponseList = new ArrayList<>();
        for (QueryProjectVo projectVo : list) {
            ProjectResponse projectResponse = new ProjectResponse();
            projectResponse.setApplicationArea(projectVo.getApplicationArea());
            projectResponse.setId(projectVo.getId());
            projectResponse.setBusiness(projectVo.getBusiness());
            projectResponse.setCreateBy(projectVo.getCreateBy());
            projectResponse.setDescription(projectVo.getDescription());
            projectResponse.setName(projectVo.getName());
            projectResponse.setProduct(projectVo.getProduct());
            projectResponse.setSource(projectVo.getSource());
            projectResponse.setArchive(projectVo.getArchive());
            projectResponse.setCreateTime(projectVo.getCreateTime());
            projectResponse.setUpdateTime(projectVo.getUpdateTime());
            projectResponse.setDevProcessList(ProjectStringUtils.convertList(projectVo.getDevProcess()));
            projectResponse.setOrchestratorModeList(ProjectStringUtils.convertList(projectVo.getOrchestratorMode()));
            projectResponseList.add(projectResponse);
            /**
             * 拆分有projectId +"-" + priv + "-" + username的拼接而成的字段，
             * 从而得到：查看权限用户、编辑权限用户、发布权限用户
             */
            String pusername = projectVo.getPusername();
            if (StringUtils.isEmpty(pusername)) {
                continue;
            }
            Map<String, List<String>> userPricMap = new HashMap<>();
            String[] tempstrArr = pusername.split(MODE_SPLIT);

            for (String s : tempstrArr) {
                String[] strArr = s.split(KEY_SPLIT);
                String key = strArr[0] + KEY_SPLIT + strArr[1];
                userPricMap.computeIfAbsent(key, k -> new ArrayList<>());
                userPricMap.get(key).add(strArr[2]);
            }
            List<String> accessUsers = userPricMap.get(projectVo.getId() + KEY_SPLIT + ProjectUserPrivEnum.PRIV_ACCESS.getRank());
            List<String> editUsers = userPricMap.get(projectVo.getId() + KEY_SPLIT + ProjectUserPrivEnum.PRIV_EDIT.getRank());
            List<String> releaseUsers = userPricMap.get(projectVo.getId() + KEY_SPLIT + ProjectUserPrivEnum.PRIV_RELEASE.getRank());
            projectResponse.setAccessUsers(CollectionUtils.isEmpty(accessUsers) ? new ArrayList<>() : accessUsers.stream().distinct().collect(Collectors.toList()));
            projectResponse.setEditUsers(CollectionUtils.isEmpty(editUsers) ? new ArrayList<>() : editUsers.stream().distinct().collect(Collectors.toList()));
            projectResponse.setReleaseUsers(CollectionUtils.isEmpty(releaseUsers) ? new ArrayList<>() : releaseUsers.stream().distinct().collect(Collectors.toList()));
        }
        return projectResponseList;
    }

    @Override
    public DSSProjectVo copyProject(ProjectCopyRequest projectCopyRequest, String username, String proxyUser, Workspace workspace) throws Exception {
        QueryWrapper queryWrapper = new QueryWrapper<DSSProjectDO>();
        queryWrapper.eq("workspace_id", projectCopyRequest.getWorkspaceId());
        queryWrapper.eq("id", projectCopyRequest.getProjectId());
        DSSProjectDO projectDO = projectMapper.selectOne(queryWrapper);
        if (projectDO == null) {
            DSSExceptionUtils.dealErrorException(ProjectServerResponse.PROJECT_NOT_EXIST.getCode(), ProjectServerResponse.PROJECT_NOT_EXIST.getMsg(), DSSProjectErrorException.class);
        }

        //保存DSS工程
        ProjectCreateRequest projectCreateRequest = new ProjectCreateRequest();
        projectCreateRequest.setName(projectCopyRequest.getCopyProjectName());
        projectCreateRequest.setApplicationArea(projectDO.getApplicationArea());
        projectCreateRequest.setBusiness(projectDO.getBusiness());
        projectCreateRequest.setProduct(projectDO.getProduct());
        projectCreateRequest.setDescription(projectDO.getDescription());
        List<String> userList= Lists.newArrayList(username);
        if(proxyUser!=null&&!proxyUser.equals(username)){
            userList.add(proxyUser);
        }
        projectCreateRequest.setReleaseUsers(userList);
        projectCreateRequest.setEditUsers(userList);
        projectCreateRequest.setAccessUsers(userList);
        projectCreateRequest.setWorkspaceId(projectDO.getWorkspaceId());
        projectCreateRequest.setWorkspaceName(workspace.getWorkspaceName());
        projectCreateRequest.setDevProcessList(ProjectStringUtils.convertList(projectDO.getDevProcess()));
        projectCreateRequest.setOrchestratorModeList(ProjectStringUtils.convertList(projectDO.getOrchestratorMode()));

        DSSProjectVo projectVo = dssFrameworkProjectService.createProject(projectCreateRequest, username, workspace);

        RequestOrchestratorInfos requestOrchestratorInfos = new RequestOrchestratorInfos();
        requestOrchestratorInfos.setWorkspaceId(projectDO.getWorkspaceId());
        requestOrchestratorInfos.setProjectId(projectCopyRequest.getProjectId());
        ResponseOrchestratorInfos responseOrchestratorInfos = RpcAskUtils.processAskException(DSSSenderServiceFactory.getOrCreateServiceInstance()
                .getOrcSender().ask(requestOrchestratorInfos), ResponseOrchestratorInfos.class, RequestOrchestratorInfos.class);

        ProjectCopyVO projectCopyVO = new ProjectCopyVO();
        projectCopyVO.setProjectDO(projectDO);
        projectCopyVO.setCopyProjectId(projectVo.getId());
        projectCopyVO.setCopyProjectName(projectVo.getName());
        projectCopyVO.setOrchestratorList(responseOrchestratorInfos.getOrchestratorInfos());
        projectCopyVO.setUsername(username);
        projectCopyVO.setWorkspace(workspace);
        projectCopyVO.setWorkspaceName(projectMapper.getWorkspaceName(projectVo.getId()));
        projectCopyVO.setDssLabel(DSSLabelUtil.createLabel(DSSCommonUtils.ENV_LABEL_VALUE_DEV));
        projectCopyVO.setInstanceName(Sender.getThisInstance());

        ProjectCopyJob projectCopyJob = new ProjectCopyJob();
        projectCopyJob.setProjectCopyVO(projectCopyVO);
        projectCopyJob.setProjectCopyEnv(projectCopyEnv);
        projectCopyThreadPool.submit(projectCopyJob);
        return projectVo;
    }

    @Override
    public String exportOrchestrators(BatchExportOrchestratorsRequest batchExportOrchestratorsRequest, String username, String proxyUser, Workspace workspace) throws Exception {
        Set<Long> orIdSet=new HashSet<>(batchExportOrchestratorsRequest.getOrchestratorIds());
        int realSize=orIdSet.size();
        //单次导出任务编排数上限校验
        if(realSize> ORCHS_SIZE_ONE_EXPORT){
            String msg=String.format("export to much orchestrators.size:%d,limit size:%d(单次导出的编排数超过上限。上限%d个，实际导出%d个)",
                    ORCHS_SIZE_ONE_EXPORT,realSize,ORCHS_SIZE_ONE_EXPORT,realSize);
            throw new DSSRuntimeException(msg);
        }
        //当前空间导出任务上限校验
        int runningTotal= DSSProjectOperateService.queryRunningTotal(ProjectOperateTypeEnum.EXPORT,workspace.getWorkspaceId());
        if(runningTotal> CONCURRENT_EXPORT_IMPORT_SIZE){
            throw new DSSRuntimeException("too much export task in this workspace,please wait a minute(当前工作空间中正在导出的任务数过多，请稍等几分钟后再试)");
        }
        //获取指定的编排的最新版本
        List<OrchestratorDetail> orchestrators = DSSOrchestratorService.getOrchestratorsByLabel(proxyUser,
                        batchExportOrchestratorsRequest.getLabels(),
                        batchExportOrchestratorsRequest.getProjectId(), workspace,false).stream()
                .filter(e->orIdSet.contains(e.getOrchestratorId()))
                .collect(Collectors.toList());
        Long projectId=batchExportOrchestratorsRequest.getProjectId();
        ProjectInfoVo projectVO=projectMapper.getProjectInfoById(projectId);
        String orgMeta= new Gson().toJson(orchestrators);
        String recordUser= String.format("%s(%s)", username, proxyUser);
        ProjectOperateRecordBO projectOperateRecordBO =
                ProjectOperateRecordBO.of(workspace.getWorkspaceId(), projectId, ProjectOperateTypeEnum.EXPORT, batchExportOrchestratorsRequest.getComment(),recordUser);

        String recordId=projectOperateRecordBO.getRecordId();
        projectOperateRecordBO.running();
        DSSProjectOperateService.addOneRecord(projectOperateRecordBO);
        EnvDSSLabel envLabel = new EnvDSSLabel(batchExportOrchestratorsRequest.getLabels());
        Exception[] exportException=new Exception[1];
        Runnable exportTask=
                ()->{
                    try {
                        BatchExportResult exportResult = exportService.batchExport(username, projectId, orchestrators, projectVO.getProjectName(), envLabel, workspace);
                        Map<String, Object> metaInfo = new HashMap<>();
                        metaInfo.put("checkCode", exportResult.getCheckSum());
                        metaInfo.put("content", orgMeta);
                        metaInfo.put("description", batchExportOrchestratorsRequest.getComment());
                        String metaInfoJson = new Gson().toJson(metaInfo);
                        DSSProjectOperateService.attachExportResource(recordId,exportResult.getBmlResource());
                        DSSProjectOperateService.updateRecord(recordId,  ProjectOperateRecordStatusEnum.SUCCESS,metaInfoJson);
                    }catch (Exception e){
                        DSSProjectOperateService.updateRecord(recordId, ProjectOperateRecordStatusEnum.FAILED,
                                ""+batchExportOrchestratorsRequest.getComment()+ ExceptionUtils.getStackTrace(e));
                        exportException[0]=e;
                    }
                };
        if(batchExportOrchestratorsRequest.getAsync()) {
            //异步
            projectCopyThreadPool.submit(exportTask);
        }else {
            //同步
            exportTask.run();
            if(exportException[0]!=null){
                throw exportException[0];
            }
        }
        return recordId;
    }

    @Override
    public void importOrchestrators(ProjectInfoVo projectInfo, BmlResource importResource, String username, String proxyUser, String checkCode, String packageInfo, EnvDSSLabel envLabel, Workspace workspace, List<Consumer<OrchestratorBatchImportInfo>> importHook, boolean async, String comment) throws Exception {
//当前空间导入任务上限校验
        int runningTotal= DSSProjectOperateService.queryRunningTotal(ProjectOperateTypeEnum.IMPORT,workspace.getWorkspaceId());
        if(runningTotal> CONCURRENT_EXPORT_IMPORT_SIZE){
            throw new DSSRuntimeException("too much import task in this workspace,please wait a minute(当前工作空间中正在导入的任务数过多，请稍等几分钟后再试)");
        }
        String recordUser= String.format("%s(%s)", username, proxyUser);
        ProjectOperateRecordBO projectOperateRecordBO =
                ProjectOperateRecordBO.of(workspace.getWorkspaceId(), projectInfo.getId(), ProjectOperateTypeEnum.IMPORT, comment,recordUser);

        String recordId=projectOperateRecordBO.getRecordId();
        projectOperateRecordBO.running();
        DSSProjectOperateService.addOneRecord(projectOperateRecordBO);
        Exception[] importException = new Exception[1];
        Runnable importTask=()-> {
            try {
                OrchestratorBatchImportInfo importResult = importService.batchImportOrc(username, projectInfo.getId(),
                        projectInfo.getProjectName(), importResource, checkCode, envLabel, workspace);
                Map<String,Object> infoMap=new HashMap<>(3);
                infoMap.put("from",importResult.getFrom());
                infoMap.put("to",importResult.getTo());
                infoMap.put("packageInfo", packageInfo);
                infoMap.put("comment", comment);
                String orgMeta = DSSCommonUtils.COMMON_GSON.toJson(infoMap);
                importHook.forEach(action -> action.accept(importResult));
                DSSProjectOperateService.updateRecord(recordId, ProjectOperateRecordStatusEnum.SUCCESS, orgMeta);
            } catch (Exception e) {
                DSSProjectOperateService.updateRecord(recordId, ProjectOperateRecordStatusEnum.FAILED,""+ comment + ExceptionUtils.getStackTrace(e));
                importException[0]=e;
            }
        };
        if(async) {
            projectCopyThreadPool.submit(importTask);
        }else {
            importTask.run();
            if(importException[0]!=null) {
                throw importException[0];
            }
        }
    }

    @Override
    public void publishOrchestratorsDirectly(RequestFrameworkConvertOrchestration convertOrchestrationRequest, String proxyUser) {
        Map<String,String> commentBody=new HashMap<>(2);
        commentBody.put("comment",convertOrchestrationRequest.getComment());
        commentBody.put("approvalId", convertOrchestrationRequest.getApproveId());
        String recordUser= String.format("%s(%s)", convertOrchestrationRequest.getUserName(), proxyUser);
        ProjectOperateRecordBO projectOperateRecordBO =
                ProjectOperateRecordBO.of(convertOrchestrationRequest.getWorkspace().getWorkspaceId(),
                        convertOrchestrationRequest.getProjectId(),
                        ProjectOperateTypeEnum.PUBLISH,
                        new Gson().toJson(commentBody),
                        recordUser);
        String recordId=projectOperateRecordBO.getRecordId();
        projectOperateRecordBO.running();
        DSSProjectOperateService.addOneRecord(projectOperateRecordBO);
        try {
            releaseService.releaseOrchestratorFromProToSchedulis(convertOrchestrationRequest);
            DSSProjectOperateService.updateRecord(recordId, ProjectOperateRecordStatusEnum.SUCCESS, null);
        }catch (Exception e) {
            DSSProjectOperateService.updateRecord(recordId, ProjectOperateRecordStatusEnum.FAILED, ExceptionUtils.getStackTrace(e));
        }
    }

    @Transactional
    @Override
    public Map<String, String> handoverWorkflowsOnUserLevel(String transferor, String recipient, Workspace workspace) throws Exception {
        Map<String, String> reMap = new HashMap<>();
        StringBuilder errorInfo = new StringBuilder();
        //1、修改工程
        List<DSSWorkspace> dssWorkspacesList = dssWorkspaceService.getWorkspaces(transferor);
        Set<Long> workSpaceIdSet = new HashSet<>();
        for (DSSWorkspace each : dssWorkspacesList) {
            getWorkspaceInfo(workspace, String.valueOf(each.getId()));
            handoverWorkflowsOnWorkSpace(transferor, recipient, workspace);
            workSpaceIdSet.add(Long.valueOf(each.getId()));
        }

        //2、添加工作空间对应的username、添加用户在工作空间中的角色信息
        addUserInfo(workSpaceIdSet, transferor, recipient);
        reMap.put("errorInfo", errorInfo.toString());
        return reMap;
    }

    @Transactional
    @Override
    public Map<String, String> handoverWorkflowsOnWorkspaceLevel(JsonObject handInfo, String transferor, String recipient, Workspace workspace) throws Exception {
        Map<String, String> reMap = new HashMap<>();
        StringBuilder errorInfo = new StringBuilder();
        //1、修改工程
        Set<Long> workSpaceIdSet = new HashSet<>();
        Map<String, Integer> dssWorkspacesMap = getDSSWorkspacesInfo(transferor);

        for (Map.Entry<String, JsonElement> entry : handInfo.entrySet()) {
            String workspaceName = entry.getKey();
            if (dssWorkspacesMap.containsKey(workspaceName)) {
                Integer workspaceId = dssWorkspacesMap.get(workspaceName);
                getWorkspaceInfo(workspace, workspaceId.toString());
                handoverWorkflowsOnWorkSpace(transferor, recipient, workspace);
                workSpaceIdSet.add(workspaceId.longValue());
            } else {
                errorInfo.append(workspaceName);
                LOGGER.info("workspaceName is error, workspaceName = " + workspaceName);
            }
        }

        //2、添加工作空间对应的username、添加用户在工作空间中的角色信息
        addUserInfo(workSpaceIdSet, transferor, recipient);
        reMap.put("errorInfo", errorInfo.toString());
        return reMap;
    }

    @Transactional
    @Override
    public Map<String, String> handoverWorkflowsOnProjectLevel(String transferor, String recipient, JsonObject handInfo, Workspace workspace) throws Exception {
        Map<String, String> reMap = new HashMap<>();
        StringBuilder errorInfo = new StringBuilder();

        //1、修改工程
        Map<String, Integer> dssWorkspacesMap = getDSSWorkspacesInfo(transferor);
        Set<Long> workSpaceIdSet = new HashSet<>();
        for (Map.Entry<String, JsonElement> entry : handInfo.entrySet()) {
            String workspaceName = entry.getKey().trim();
            String projectNames = entry.getValue().getAsString().trim();

            if (dssWorkspacesMap.containsKey(workspaceName)) {
                Integer workspaceId = dssWorkspacesMap.get(workspaceName);
                getWorkspaceInfo(workspace, workspaceId.toString());

                if (StringUtils.isNotBlank(projectNames)) {
                    String projectNamesArr[] = projectNames.split(",");
                    workSpaceIdSet.add(workspaceId.longValue());
                    List<String> errorProjectNameList = handoverWorkflowsOnProject(transferor, recipient, workspace, projectNamesArr);
                    errorProjectNameList.forEach(each -> errorInfo.append(workspaceName + "." + each));
                }
            } else {
                errorInfo.append(workspaceName);
                LOGGER.info("workspaceName is error, workspaceName = " + workspaceName);
            }
        }
        //2、添加工作空间对应的username、添加用户在工作空间中的角色信息
        addUserInfo(workSpaceIdSet, transferor, recipient);
        reMap.put("errorInfo", errorInfo.toString());
        return reMap;
    }

    /**
     * @Author: bradyli
     * @Date: 2021/10/29
     * @Description: 获取工作空间信息
     * @Param:
     * @return: dssWorkspacesMap<工作空间名, 工作空间id>
     **/
    public Map<String, Integer> getDSSWorkspacesInfo(String transferor) throws Exception {
        //<工作空间名,工作空间id>
        Map<String, Integer> dssWorkspacesMap = new HashMap<>();
        List<DSSWorkspace> dssWorkspacesList = dssWorkspaceService.getWorkspaces(transferor);
        dssWorkspacesList.forEach(eachDssWorkspace -> dssWorkspacesMap.put(eachDssWorkspace.getName(), eachDssWorkspace.getId()));
        return dssWorkspacesMap;
    }

    /**
     * @Author: bradyli
     * @Date: 2021/10/29
     * @Description: 添加工作空间对应的username、添加用户在工作空间中的角色信息
     * @Param:
     * @return:
     **/
    public void addUserInfo(Set<Long> workSpaceIdSet, String transferor, String recipient) throws Exception {
        if (workSpaceIdSet.size() > 0) {
            List<Long> workSpaceIdList = new ArrayList<>(workSpaceIdSet);
            //2、添加用户在工作空间中的角色信息
            List<DSSWorkspaceUserRole> transferorList = dssWorkspaceRoleService.queryUserRoleInWorkSpaces(workSpaceIdList, transferor);
            List<DSSWorkspaceUserRole> recipientList = dssWorkspaceRoleService.queryUserRoleInWorkSpaces(workSpaceIdList, recipient);
            //<工作空间，Set<角色>>
            HashMap<Long, HashSet<Integer>> transferorMap = new HashMap<Long, HashSet<Integer>>();
            HashMap<Long, HashSet<Integer>> recipientMap = new HashMap<Long, HashSet<Integer>>();
            transferorList.forEach(each -> {
                HashSet<Integer> roleSet = new HashSet<>();
                roleSet.add(each.getRoleId());
                roleSet.addAll(transferorMap.getOrDefault(each.getWorkspaceId(), new HashSet<>()));
                transferorMap.put(each.getWorkspaceId(), roleSet);
            });
            recipientList.forEach(each -> {
                HashSet<Integer> roleSet = new HashSet<>();
                roleSet.add(each.getRoleId());
                roleSet.addAll(recipientMap.getOrDefault(each.getWorkspaceId(), new HashSet<>()));
                recipientMap.put(each.getWorkspaceId(), roleSet);
            });
            //待添加的数据
            List<DSSWorkspaceUserRole> addDSSWorkspaceUserRoleList = new ArrayList<>();
            for (Map.Entry<Long, HashSet<Integer>> entry : transferorMap.entrySet()) {
                Long workspaceId = entry.getKey();
                HashSet<Integer> transferorRoleIdSet = entry.getValue();
                if (recipientMap.containsKey(workspaceId)) {
                    transferorRoleIdSet.removeAll(recipientMap.get(workspaceId));
                    transferorRoleIdSet.forEach(roleId -> addDSSWorkspaceUserRoleList.add(new DSSWorkspaceUserRole(workspaceId, recipient, roleId, new Date(), recipient)));
                } else {
                    transferorRoleIdSet.forEach(roleId -> addDSSWorkspaceUserRoleList.add(new DSSWorkspaceUserRole(workspaceId, recipient, roleId, new Date(), recipient)));
                }
            }

            if (addDSSWorkspaceUserRoleList.size() > 0) {
                dssWorkspaceRoleService.insertUserRoleList(addDSSWorkspaceUserRoleList);
            }
        }
    }

    public void handoverWorkflowsOnWorkSpace(String transferor, String recipient, Workspace workspace) throws Exception {
        //根据dss_project、dss_project_user查询出所在空间登录用户相关的工程
        ProjectQueryRequest projectRequest = new ProjectQueryRequest();
        projectRequest.setWorkspaceId(Long.valueOf(workspace.getWorkspaceName()));
        projectRequest.setUsername(transferor);

        List<QueryProjectVo> projectVoList = projectMapper.getListByParam(projectRequest);
        for (QueryProjectVo projectVo : projectVoList) {
            handoverProject(transferor, recipient, projectVo.getId(), workspace);
        }
    }

    /**
     * @Author: bradyli
     * @Date: 2021/12/8
     * @Description:
     * @Param:
     * @return: 返回异常的工程名称
     **/
    public List<String> handoverWorkflowsOnProject(String transferor, String recipient, Workspace workspace, String projectNamesArr[]) throws Exception {
        List<String> reList = new ArrayList<>();
        int workspaceId = Integer.valueOf(workspace.getWorkspaceName());

        //HashMap<projectName, projectId>
        HashMap<String, Long> projectInfoMap = new HashMap<>();
        List<HashMap<String, Object>> projectInfoMapList = projectMapper.getProjectInfoByWorkspaceId(workspaceId);
        projectInfoMapList.forEach(eachMap -> projectInfoMap.put(eachMap.get("name").toString(), (Long) eachMap.get("id")));

        for (String projectName : projectNamesArr) {
            if (projectInfoMap.containsKey(projectName)) {
                Long projectId = projectInfoMap.get(projectName);
                handoverProject(transferor, recipient, projectId, workspace);
            } else {
                reList.add(projectName);
                LOGGER.info("projectName is error, projectName = " + projectName);
            }
        }
        return reList;
    }

    @Transactional
    public void handoverProject(String transferor, String recipient, Long projectId, Workspace workspace) throws Exception {
        Long workspaceId = Long.valueOf(workspace.getWorkspaceName());

        //构建发布者、编辑者、可见者
        ProjectModifyRequest projectModifyRequest = new ProjectModifyRequest();
        List<String> releaseList = projectMapper.getProjectUserNames(workspaceId, projectId, ProjectUserPrivEnum.PRIV_RELEASE.getRank());
        List<String> editList = projectMapper.getProjectUserNames(workspaceId, projectId, ProjectUserPrivEnum.PRIV_EDIT.getRank());
        List<String> accessList = projectMapper.getProjectUserNames(workspaceId, projectId, ProjectUserPrivEnum.PRIV_ACCESS.getRank());
        releaseList.removeIf(i -> i.trim().equals(transferor));
        editList.removeIf(i -> i.trim().equals(transferor));
        accessList.removeIf(i -> i.trim().equals(transferor));
        releaseList.add(recipient);
        editList.add(recipient);
        accessList.add(recipient);
        projectModifyRequest.setReleaseUsers(releaseList);
        projectModifyRequest.setEditUsers(editList);
        projectModifyRequest.setAccessUsers(accessList);
        projectModifyRequest.setWorkspaceId(workspaceId);

        DSSProjectDO dbProject = projectMapper.selectById(Long.valueOf(projectId));

        projectUserService.modifyProjectUser(dbProject, projectModifyRequest, transferor, workspace);
    }

    /**
     * @Author: bradyli
     * @Date: 2021/11/2
     * @Description: 补充workspace的信息
     * @Param:
     * @return:
     **/
    private void getWorkspaceInfo(Workspace workspace, String workSpaceId) {
        workspace.addCookie("workspaceId", workSpaceId);
        //todo
//        workspace.getSSOUrlBuilderOperation().setWorkspace(workSpaceId);
        workspace.setWorkspaceName(workSpaceId);
    }

}

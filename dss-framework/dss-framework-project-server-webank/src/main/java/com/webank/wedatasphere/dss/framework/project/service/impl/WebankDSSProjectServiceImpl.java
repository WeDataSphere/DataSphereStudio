package com.webank.wedatasphere.dss.framework.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.webank.wedatasphere.dss.common.entity.BmlResource;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.label.DSSLabelUtil;
import com.webank.wedatasphere.dss.common.label.EnvDSSLabel;
import com.webank.wedatasphere.dss.common.label.LabelRouteVO;
import com.webank.wedatasphere.dss.common.service.BMLService;
import com.webank.wedatasphere.dss.common.utils.*;
import com.webank.wedatasphere.dss.framework.project.contant.ProjectServerResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.webank.wedatasphere.dss.common.constant.project.ProjectUserPrivEnum;
import com.webank.wedatasphere.dss.framework.project.dao.DSSProjectMapper;
import com.webank.wedatasphere.dss.framework.project.dao.WebankDSSProjectMapper;
import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectDO;
import com.webank.wedatasphere.dss.framework.project.entity.ProjectOperateRecordBO;
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
import com.webank.wedatasphere.dss.framework.project.entity.response.BatchExportResult;
import com.webank.wedatasphere.dss.framework.project.entity.OrchestratorBatchImportInfo;
import com.webank.wedatasphere.dss.framework.project.service.ExportService;
import com.webank.wedatasphere.dss.framework.project.service.ImportService;
import com.webank.wedatasphere.dss.framework.release.service.ReleaseService;
import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorDetail;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestFrameworkConvertOrchestration;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestOrchestratorInfos;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.ResponseOrchestratorInfos;
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.OrchestratorRequest;
import com.webank.wedatasphere.dss.orchestrator.server.entity.vo.OrchestratorBaseInfo;
import com.webank.wedatasphere.dss.orchestrator.server.service.OrchestratorService;
import com.webank.wedatasphere.dss.sender.service.DSSSenderServiceFactory;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.framework.workspace.bean.DSSWorkspace;
import com.webank.wedatasphere.dss.framework.workspace.bean.WebankDSSWorkspaceUserRole;
import com.webank.wedatasphere.dss.framework.workspace.service.WebankDSSWorkspaceRoleService;
import com.webank.wedatasphere.dss.framework.workspace.service.WebankDSSWorkspaceService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

import com.webank.wedatasphere.dss.standard.app.structure.project.ref.DSSProjectDataSource;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.linkis.rpc.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author: zorezeng on 2021/8/24
 * @description:
 */
@Service
public class WebankDSSProjectServiceImpl implements WebankDSSProjectService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebankDSSProjectServiceImpl.class);
    /**
     * 单次导出的编排数量限制
     */
    public static final int ORCHS_SIZE_ONE_EXPORT = 20;
    public static final int CONCURRENT_EXPORT_IMPORT_SIZE = 20;

    @Autowired
    private WebankDSSProjectMapper webankDSSProjectMapper;

    @Autowired
    private DSSProjectService projectService;

    @Autowired
    private DSSProjectUserService projectUserService;


    @Autowired
    private DSSProjectMapper projectMapper;

    @Autowired
    private WebankDSSWorkspaceService webankDSSWorkspaceService;

    @Autowired
    WebankDSSWorkspaceRoleService webankDSSWorkspaceRoleService;

    @Autowired
    private DSSFrameworkProjectService dssFrameworkProjectService;

    @Autowired
    private WebankDSSOrchestratorService webankDSSOrchestratorService;
    @Autowired
    private ProjectCopyEnv projectCopyEnv;
    @Autowired
    private OrchestratorService orchestratorService;
    @Autowired
    private WebankDSSProjectOperateService webankDSSProjectOperateService;
    @Autowired
    private ExportService exportService;
    @Resource
    private ImportService importService;
    @Resource
    private ReleaseService releaseService;
    @Autowired
    @Qualifier("projectBmlService")
    private BMLService bmlService;

    public static final String MODE_SPLIT = ",";
    public static final String KEY_SPLIT = "-";

    private final ThreadFactory projectCopyThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("dss-project-internal-operate-thread-%d")
            .setDaemon(false)
            .build();

    private final ExecutorService projectCopyThreadPool = new ThreadPoolExecutor(5, 200, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1024), projectCopyThreadFactory, new ThreadPoolExecutor.AbortPolicy());

    @Override
    public List<ProjectResponse> getDeletedProjects(ProjectQueryRequest projectRequest) {
        //根据dss_project、dss_project_user查询出所在空间登录用户相关的工程,再删选出其中是已删除的项目
        List<QueryProjectVo> list = webankDSSProjectMapper.getDeletedProjects(projectRequest);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<ProjectResponse> projectResponseList = new ArrayList<>();
        ProjectResponse projectResponse = null;
        for (QueryProjectVo projectVo : list) {
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
            if(projectVo.getDataSourceListJson()!=null){
                List<DSSProjectDataSource> dataSourceList = new Gson().fromJson(projectVo.getDataSourceListJson(),
                        new TypeToken<List<DSSProjectDataSource>>() {
                        }.getType());
                projectResponse.setDataSourceList(dataSourceList);
            }
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
    public DSSProjectVo copyProject(ProjectCopyRequest projectCopyRequest, String username,String proxyUser,
                                    Workspace sourceWorkspace,Workspace targetWorkspace) throws Exception {
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
        projectCreateRequest.setAssociateGit(projectCopyRequest.getAssociateGit());
        projectCreateRequest.setGitUser(projectCopyRequest.getGitUser());
        projectCreateRequest.setGitToken(projectCopyRequest.getGitToken());
        projectCreateRequest.setApplicationArea(projectDO.getApplicationArea());
        projectCreateRequest.setBusiness(projectDO.getBusiness());
        projectCreateRequest.setProduct(projectDO.getProduct());
        projectCreateRequest.setDescription(projectDO.getDescription());
        List<String> userList=Lists.newArrayList(username);
        if(proxyUser!=null&&!proxyUser.equals(username)){
            userList.add(proxyUser);
        }
        projectCreateRequest.setReleaseUsers(userList);
        projectCreateRequest.setEditUsers(userList);
        projectCreateRequest.setAccessUsers(userList);
        projectCreateRequest.setWorkspaceId(targetWorkspace.getWorkspaceId());
        projectCreateRequest.setWorkspaceName(targetWorkspace.getWorkspaceName());
        projectCreateRequest.setDevProcessList(ProjectStringUtils.convertList(projectDO.getDevProcess()));
        projectCreateRequest.setOrchestratorModeList(ProjectStringUtils.convertList(projectDO.getOrchestratorMode()));

        DSSProjectVo projectVo = dssFrameworkProjectService.createProject(projectCreateRequest, username, targetWorkspace);

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
        projectCopyVO.setTargetWorkspace(targetWorkspace);
        projectCopyVO.setSourceWorkspace(sourceWorkspace);
        projectCopyVO.setWorkspaceName(webankDSSProjectMapper.getWorkspaceName(projectVo.getId()));
        projectCopyVO.setDssLabel(DSSLabelUtil.createLabel(DSSCommonUtils.ENV_LABEL_VALUE_DEV));
        projectCopyVO.setInstanceName(Sender.getThisInstance());

        ProjectCopyJob projectCopyJob = new ProjectCopyJob();
        projectCopyJob.setProjectCopyVO(projectCopyVO);
        projectCopyJob.setProjectCopyEnv(projectCopyEnv);
        projectCopyThreadPool.submit(projectCopyJob);
        return projectVo;
    }


    @Override
    public String exportOrchestrators(BatchExportOrchestratorsRequest batchExportOrchestratorsRequest,String username,String proxyUser,Workspace workspace) throws Exception {
        Set<Long> orIdSet=new HashSet<>(batchExportOrchestratorsRequest.getOrchestratorIds());
        int realSize=orIdSet.size();
        //单次导出任务编排数上限校验
        if(realSize> ORCHS_SIZE_ONE_EXPORT){
            String msg=String.format("export to much orchestrators.size:%d,limit size:%d(单次导出的编排数超过上限。上限%d个，实际导出%d个)",
                    ORCHS_SIZE_ONE_EXPORT,realSize,ORCHS_SIZE_ONE_EXPORT,realSize);
            throw new DSSRuntimeException(msg);
        }
        //当前空间导出任务上限校验
        int runningTotal= webankDSSProjectOperateService.queryRunningTotal(ProjectOperateTypeEnum.EXPORT,workspace.getWorkspaceId());
        if(runningTotal> CONCURRENT_EXPORT_IMPORT_SIZE){
            throw new DSSRuntimeException("too much export task in this workspace,please wait a minute(当前工作空间中正在导出的任务数过多，请稍等几分钟后再试)");
        }
        //获取指定的编排的最新版本
        OrchestratorRequest orchestratorRequest = new OrchestratorRequest(workspace.getWorkspaceId(), batchExportOrchestratorsRequest.getProjectId());
        LabelRouteVO labelRouteVO = new LabelRouteVO();
        labelRouteVO.setRoute(batchExportOrchestratorsRequest.getLabels());
        orchestratorRequest.setLabels(labelRouteVO);
        List<OrchestratorBaseInfo> orchestrators = orchestratorService.getOrchestratorInfos(orchestratorRequest, username).stream()
                .filter(e->orIdSet.contains(e.getOrchestratorId()))
                .collect(Collectors.toList());
        Long projectId=batchExportOrchestratorsRequest.getProjectId();
        ProjectInfoVo projectVO=projectService.getProjectInfoById(projectId);
        String recordUser= String.format("%s(%s)", username, proxyUser);
        ProjectOperateRecordBO projectOperateRecordBO =
                ProjectOperateRecordBO.of(workspace.getWorkspaceId(), projectId, ProjectOperateTypeEnum.EXPORT, batchExportOrchestratorsRequest.getComment(),recordUser);

        String recordId=projectOperateRecordBO.getRecordId();
        projectOperateRecordBO.running();
        webankDSSProjectOperateService.addOneRecord(projectOperateRecordBO);
        EnvDSSLabel envLabel = new EnvDSSLabel(batchExportOrchestratorsRequest.getLabels());
        String projectName = projectVO.getProjectName();
        Exception[] exportException=new Exception[1];
        Runnable exportTask=
        ()->{
        try {
            String projectPath = exportService.batchExport(username, projectId, orchestrators, projectName, envLabel, workspace);
            String zipFile = ZipHelper.zip(projectPath,true);
            LOGGER.info("export zip file locate at {}",zipFile);
            //先上传
            InputStream inputStream = bmlService.readLocalResourceFile(username, zipFile);
            // upload会负责把inputStream关闭
            BmlResource bmlResource= bmlService.upload(username, inputStream, projectName + ".OrcsExport", projectName);
            //上传完之后，计算上传后二进制流的md5
            String checkCode;
            try(InputStream zipInputStream=(InputStream)bmlService.download(username,
                    bmlResource.getResourceId(),
                    bmlResource.getVersion()).get("is")) {
                checkCode=  DigestUtils.md5Hex(zipInputStream);
            } catch (IOException e) {
                LOGGER.error("md5 sum failed",e);
                throw new DSSRuntimeException(e.getMessage());
            }
            LOGGER.info("export zip file upload to bmlResourceId:{} bmlResourceVersion:{}",
                    bmlResource.getResourceId(),bmlResource.getVersion());
            FileUtils.deleteQuietly(new File(zipFile));
            BatchExportResult exportResult = new BatchExportResult(bmlResource,checkCode);
            Map<String, Object> metaInfo = new HashMap<>();
            List<OrchestratorDetail> orchestratorsContent = webankDSSOrchestratorService.getOrchestratorsByLabel(proxyUser,
                            batchExportOrchestratorsRequest.getLabels(),
                            batchExportOrchestratorsRequest.getProjectId(), workspace,false).stream()
                    .filter(e->orIdSet.contains(e.getOrchestratorId()))
                    .collect(Collectors.toList());
            String orgMeta= new Gson().toJson(orchestratorsContent);
            metaInfo.put("checkCode", exportResult.getCheckSum());
            metaInfo.put("content", orgMeta);
            metaInfo.put("description", batchExportOrchestratorsRequest.getComment());
            String metaInfoJson = new Gson().toJson(metaInfo);
            webankDSSProjectOperateService.attachExportResource(recordId,exportResult.getBmlResource());
            webankDSSProjectOperateService.updateRecord(recordId,  ProjectOperateRecordStatusEnum.SUCCESS,metaInfoJson);
        }catch (Exception e){
            webankDSSProjectOperateService.updateRecord(recordId, ProjectOperateRecordStatusEnum.FAILED,
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
    public void importOrchestrators(ProjectInfoVo projectInfo, BmlResource importResource, String username, String proxyUser,
                                    String checkCode, String packageInfo, EnvDSSLabel envLabel, Workspace workspace,
                                    List<Consumer<OrchestratorBatchImportInfo>> importHook,
                                    boolean async,String comment) throws Exception {
        //当前空间导入任务上限校验
        int runningTotal= webankDSSProjectOperateService.queryRunningTotal(ProjectOperateTypeEnum.IMPORT,workspace.getWorkspaceId());
        if(runningTotal> CONCURRENT_EXPORT_IMPORT_SIZE){
            throw new DSSRuntimeException("too much import task in this workspace,please wait a minute(当前工作空间中正在导入的任务数过多，请稍等几分钟后再试)");
        }
        String recordUser= String.format("%s(%s)", username, proxyUser);
        ProjectOperateRecordBO projectOperateRecordBO =
                ProjectOperateRecordBO.of(workspace.getWorkspaceId(), projectInfo.getId(), ProjectOperateTypeEnum.IMPORT, comment,recordUser);
        String projectName = projectInfo.getProjectName();
        String recordId=projectOperateRecordBO.getRecordId();
        projectOperateRecordBO.running();
        webankDSSProjectOperateService.addOneRecord(projectOperateRecordBO);
        Exception[] importException = new Exception[1];
       Runnable importTask=()-> {
           try {
               //先核对md5
               String md5code;
               try(InputStream zipInputStream=(InputStream)bmlService.download(username,
                       importResource.getResourceId(),
                       importResource.getVersion()).get("is")) {
                   md5code=  DigestUtils.md5Hex(zipInputStream);
               } catch (IOException e) {
                   LOGGER.error("md5 sum failed",e);
                   throw new DSSRuntimeException(e.getMessage());
               }
               if(!md5code.equals(checkCode)){
                   LOGGER.error("checkCode does not match the upload file。input code:{},real code:{}",checkCode,md5code);
                   throw new DSSRuntimeException("checkCode does not match the upload file,please check your upload file and checkCode");
               }
               //下载到本地处理
               String importSaveBasePath = IoUtils.generateTempIOPath(username);
               String importFile=importSaveBasePath+File.separator+projectName+".zip";

               LOGGER.info("import zip file locate at {}",importFile);
               //下载压缩包
               bmlService.downloadToLocalPath(username, importResource.getResourceId(), importResource.getVersion(), importFile);
               try{
                   String  originProjectName=ZipHelper.readImportZipProjectName(importFile);
                   if(!projectName.equals(originProjectName)){
                       String msg=String.format("target project name must be same with origin project name.origin project name:%s,target project name:%s(导入的目标工程名必须与导出时源工程名保持一致。源工程名：%s，目标工程名：%s)"
                               ,originProjectName,projectName,originProjectName,projectName);
                       throw new DSSRuntimeException(msg);
                   }
               }catch (IOException e){
                   throw new DSSRuntimeException("upload file format error(导入包格式错误)");
               }
               //解压
               ZipHelper.unzipFile(importFile,importSaveBasePath,true);
               String projectPath = IoUtils.addFileSeparator(importSaveBasePath, projectName);

               OrchestratorBatchImportInfo importResult = importService.batchImportOrc(username, projectInfo.getId(),
                       projectInfo.getProjectName(), projectPath, envLabel, workspace);
               Map<String,Object> infoMap=new HashMap<>(3);
               infoMap.put("from",importResult.getFrom());
               infoMap.put("to",importResult.getTo());
               infoMap.put("packageInfo", packageInfo);
               infoMap.put("comment", comment);
               String orgMeta = DSSCommonUtils.COMMON_GSON.toJson(infoMap);
               importHook.forEach(action -> action.accept(importResult));
               webankDSSProjectOperateService.updateRecord(recordId, ProjectOperateRecordStatusEnum.SUCCESS, orgMeta);
           } catch (Exception e) {
               webankDSSProjectOperateService.updateRecord(recordId, ProjectOperateRecordStatusEnum.FAILED,""+ comment + ExceptionUtils.getStackTrace(e));
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
    public void publishOrchestratorsDirectly( RequestFrameworkConvertOrchestration convertOrchestrationRequest,String proxyUser){
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
        webankDSSProjectOperateService.addOneRecord(projectOperateRecordBO);
        try {
            releaseService.releaseOrchestratorFromProToSchedulis(convertOrchestrationRequest);
            webankDSSProjectOperateService.updateRecord(recordId, ProjectOperateRecordStatusEnum.SUCCESS, null);
        }catch (Exception e) {
            webankDSSProjectOperateService.updateRecord(recordId, ProjectOperateRecordStatusEnum.FAILED, ExceptionUtils.getStackTrace(e));
        }
    }

    @Transactional
    @Override
    public Map<String, String> handoverWorkflowsOnUserLevel(String transferor, String recipient, Workspace workspace) throws Exception {
        Map<String, String> reMap = new HashMap<>();
        StringBuilder errorInfo = new StringBuilder();
        //1、修改工程
        List<DSSWorkspace> dssWorkspacesList = webankDSSWorkspaceService.getWorkspaces(transferor);
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
        List<DSSWorkspace> dssWorkspacesList = webankDSSWorkspaceService.getWorkspaces(transferor);
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
            List<WebankDSSWorkspaceUserRole> transferorList = webankDSSWorkspaceRoleService.queryUserRoleInWorkSpaces(workSpaceIdList, transferor);
            List<WebankDSSWorkspaceUserRole> recipientList = webankDSSWorkspaceRoleService.queryUserRoleInWorkSpaces(workSpaceIdList, recipient);
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
            List<WebankDSSWorkspaceUserRole> addWebankDSSWorkspaceUserRoleList = new ArrayList<>();
            for (Map.Entry<Long, HashSet<Integer>> entry : transferorMap.entrySet()) {
                Long workspaceId = entry.getKey();
                HashSet<Integer> transferorRoleIdSet = entry.getValue();
                if (recipientMap.containsKey(workspaceId)) {
                    transferorRoleIdSet.removeAll(recipientMap.get(workspaceId));
                    transferorRoleIdSet.forEach(roleId -> addWebankDSSWorkspaceUserRoleList.add(new WebankDSSWorkspaceUserRole(workspaceId, recipient, roleId, new Date(), recipient)));
                } else {
                    transferorRoleIdSet.forEach(roleId -> addWebankDSSWorkspaceUserRoleList.add(new WebankDSSWorkspaceUserRole(workspaceId, recipient, roleId, new Date(), recipient)));
                }
            }

            if (addWebankDSSWorkspaceUserRoleList.size() > 0) {
                webankDSSWorkspaceRoleService.insertUserRoleList(addWebankDSSWorkspaceUserRoleList);
            }
        }
    }

    /**
     * @Author: bradyli
     * @Date: 2021/10/29
     * @Description: 权限出让人对应工作空间下没有工作空间时，删除他拥有的工作空间数据和角色数据
     * @Param:
     * @return:
     **/
    public void deleteExtraUserInfo(Long workSpaceId, String transferor) throws Exception {
        Long workspaceNum = webankDSSProjectMapper.countWorkspaceIdOfUser(workSpaceId.intValue(), transferor);
        if (workspaceNum <= 0) {
            //2、删除角色数据，权力应该下放到权限移动接口或者独立出管理页面
            webankDSSWorkspaceRoleService.deleteWorkspaceRoleOfUser(workSpaceId, transferor);
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
        List<HashMap<String, Object>> projectInfoMapList = webankDSSProjectMapper.getProjectInfoByWorkspaceId(workspaceId);
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
        List<String> releaseList = webankDSSProjectMapper.getProjectUserNames(workspaceId, projectId, ProjectUserPrivEnum.PRIV_RELEASE.getRank());
        List<String> editList = webankDSSProjectMapper.getProjectUserNames(workspaceId, projectId, ProjectUserPrivEnum.PRIV_EDIT.getRank());
        List<String> accessList = webankDSSProjectMapper.getProjectUserNames(workspaceId, projectId, ProjectUserPrivEnum.PRIV_ACCESS.getRank());
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

        DSSProjectDO dbProject = projectService.getProjectById(Long.valueOf(projectId));

        projectUserService.modifyProjectUser(dbProject, projectModifyRequest);
    }
}

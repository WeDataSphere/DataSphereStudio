package com.webank.wedatasphere.dss.framework.project.restful;

import com.google.gson.JsonObject;
import com.webank.wedatasphere.dss.common.constant.project.ProjectUserPrivEnum;
import com.webank.wedatasphere.dss.common.entity.BmlResource;
import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.label.EnvDSSLabel;
import com.webank.wedatasphere.dss.common.service.BMLService;
import com.webank.wedatasphere.dss.common.utils.DSSExceptionUtils;
import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectCopyTask;
import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectDO;
import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectUser;
import com.webank.wedatasphere.dss.framework.project.entity.ProjectOperateRecordBO;
import com.webank.wedatasphere.dss.framework.project.entity.request.BatchExportOrchestratorsRequest;
import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectCopyRequest;
import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectCopyStatusRequest;
import com.webank.wedatasphere.dss.framework.project.entity.vo.DSSProjectVo;
import com.webank.wedatasphere.dss.framework.project.entity.vo.ProjectInfoVo;
import com.webank.wedatasphere.dss.framework.project.entity.vo.ProjectOperateRecordVO;
import com.webank.wedatasphere.dss.framework.project.enums.HandOverTypeEnum;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateRecordStatusEnum;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateTypeEnum;
import com.webank.wedatasphere.dss.framework.project.request.ReleaseHandoverProjectByOpsRequest;
import com.webank.wedatasphere.dss.framework.project.service.*;
import com.webank.wedatasphere.dss.framework.proxy.conf.ProxyUserConfiguration;
import com.webank.wedatasphere.dss.framework.proxy.exception.DSSProxyUserErrorException;
import com.webank.wedatasphere.dss.framework.proxy.service.DssProxyUserService;
import com.webank.wedatasphere.dss.framework.release.entity.orchestrator.OrchestratorBatchImportInfo;
import com.webank.wedatasphere.dss.framework.release.entity.request.PublishWholeProjectRequest;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestFrameworkConvertOrchestration;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.sso.utils.SSOHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.filesystem.service.FsService;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.apache.linkis.storage.fs.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/dss/framework/project", produces = {"application/json"})
public class FrameworkProjectRestfulApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkProjectRestfulApi.class);

    @Autowired
    private DSSProjectService dssProjectService;
    @Autowired
    private DSSProjectCopyTaskService dssProjectCopyTaskService;
    @Autowired
    private HttpServletRequest request;
    @Resource
    private DSSProjectOperateService DSSProjectOperateService;
    @Autowired
    @Qualifier("projectBmlService")
    private BMLService bmlService;
    @Autowired
    private DSSOrchestratorService DSSOrchestratorService;
    @Autowired
    private DssProxyUserService dssProxyUserService;
    @Autowired
    private DSSProjectUserService dssProjectUserService;
    @Autowired
    private FsService fsService;

    /**
     * 复制工程
     * @return
     */
    @PostMapping(path = "/copyProject")
    public Message copyProject(@RequestBody ProjectCopyRequest projectCopyRequest) throws Exception {
        String username = SecurityFilter.getLoginUsername(request);
        Workspace workspace = SSOHelper.getWorkspace(request);
        DSSProjectVo projectVo = null;
        try {
            String proxyUser=username;
            if(ProxyUserConfiguration.isProxyUserEnable()){
                proxyUser = dssProxyUserService.getProxyUser(request);
            }
            LOGGER.info("user {} try to copyProject, the request params is:{}", username, projectCopyRequest);
            projectVo = dssProjectService.copyProject(projectCopyRequest, username,proxyUser, workspace);
        } catch (Exception e) {
            LOGGER.error("复制工程失败", e);
            return Message.error(e.getMessage());
        }
        Message message = Message.ok("复制工程已经成功，工作流正在后台复制中...").data("projectId", projectVo.getId());
        return message;
    }

    /**
     * 获取复制工程信息
     */
    @PostMapping(path = "/getCopyProjectInfo")
    public Message getCopyProjectInfo(@RequestBody ProjectCopyStatusRequest projectCopyStatusRequest) throws Exception {
        String username = SecurityFilter.getLoginUsername(request);
        Workspace workspace = SSOHelper.getWorkspace(request);
        if (projectCopyStatusRequest.getCopyProjectId() == null) {
            return Message.error("copyProjectId不能为空");
        }
        LOGGER.info("user {} begin to copy project info in workspace {}, params: {}", username, workspace.getWorkspaceId(),projectCopyStatusRequest);
        DSSProjectCopyTask copyTaskInfo = dssProjectCopyTaskService.getCopyTaskInfo(projectCopyStatusRequest);
        return Message.ok("获取复制工程状态成功")
                .data("surplusCount", copyTaskInfo.getSurplusCount())
                .data("sumCount", copyTaskInfo.getSumCount())
                .data("status", copyTaskInfo.getStatus())
                .data("errorMsg", copyTaskInfo.getErrorMsg());
    }

    /**
     * @Author: bradyli
     * @Date: 2021/10/27
     * @Description: 通过OPS将工程交接给新用户
     * @Param: recipient 权限接收者
     * @Param: type 交接类型
     * @Param: workspaceName 工作空间名称
     * @Param: projectName 工程名称
     * @return:
     **/
    @PostMapping(path = "/handoverWorkflowsByOps")
    public Message handoverWorkflows( @RequestBody ReleaseHandoverProjectByOpsRequest releaseHandoverProjectByOpsRequest) {
        Message response = null;
        try {
            String username = SecurityFilter.getLoginUsername(request);
            Workspace workspace = SSOHelper.getWorkspace(request);

            String recipient = releaseHandoverProjectByOpsRequest.getRecipient();
            String type = releaseHandoverProjectByOpsRequest.getType();
            String workspaceName = releaseHandoverProjectByOpsRequest.getWorkspaceName();
            String projectName = releaseHandoverProjectByOpsRequest.getProjectName();

            LOGGER.info("user {} try to handoverWorkflows, the request params is:{}", username, releaseHandoverProjectByOpsRequest);

            if (type.equals(HandOverTypeEnum.ONE.getValue()) || type.equals(HandOverTypeEnum.TWO.getValue()) || type.equals(HandOverTypeEnum.THREE.getValue())) {
                if ("1".equals(type)) {
                    //type=1,基于用户级别交接
                    Map<String, String> reMap = dssProjectService.handoverWorkflowsOnUserLevel(username, recipient, workspace);
                    response = Message.ok().data("result", "success").data("errorInfo", reMap.get("errorInfo"));
                } else if ("2".equals(type) && !StringUtils.isEmpty(workspaceName)) {
                    //type=2,基于工作空间级别交接
                    JsonObject handInfo = new JsonObject();
                    handInfo.addProperty(workspaceName, "");
                    Map<String, String> reMap = dssProjectService.handoverWorkflowsOnWorkspaceLevel(handInfo, username, recipient, workspace);
                    response = Message.ok().data("result", "success").data("errorInfo", reMap.get("errorInfo"));
                } else if ("3".equals(type) && !StringUtils.isEmpty(workspaceName) && !StringUtils.isEmpty(projectName)) {
                    //type=3,基于工程级别交接
                    JsonObject handInfo = new JsonObject();
                    handInfo.addProperty(workspaceName, projectName);
                    Map<String, String> reMap = dssProjectService.handoverWorkflowsOnProjectLevel(username, recipient.trim(), handInfo, workspace);
                    response = Message.ok().data("result", "success").data("errorInfo", reMap.get("errorInfo"));
                }
            } else {
                LOGGER.info("parameters type = " + type);
                response = Message.error("参数校验失败！");
            }
        } catch (Exception e) {
            LOGGER.error(releaseHandoverProjectByOpsRequest.toString());
            LOGGER.error("failed to handover Workflows!", e);
            response = Message.error("基于用户或者工作空间的工作流交接失败:" + e.getMessage());
            return response;
        }
        return response;
    }

    /**
     * 获取项目操作记录的操作类型枚举列表
     * @param projectId 项目id
     * @return 操作类型枚举列表
     */
    @GetMapping("/listOperateType")
    public Message listOperateType(@RequestParam("projectId") Long projectId){
        List<ProjectOperateTypeEnum.VO> types = Arrays.stream(ProjectOperateTypeEnum.values())
                .map(ProjectOperateTypeEnum::toVO).collect(Collectors.toList());
        return Message.ok().data("result", types);
    }

    /**
     * 查看项目的操作记录
     * @param projectId 项目id
     * @param operateType 操作类型过滤参数，为空则不过滤
     * @param status 操作状态过滤器，为空则不过滤
     * @param currentPage 页号，从0开始
     * @param pageSize 页大小，表示每页有多少项
     * @return 操作记录列表
     */
    @GetMapping("/operateRecordList")
    public Message operateRecordList(@RequestParam("projectId") Long projectId,
                                     @RequestParam(value="operateType",required = false) Integer operateType,
                                     @RequestParam(value ="status",required = false) Integer status,
                                     @RequestParam(value = "pageSize",defaultValue = "50") Integer pageSize,
                                     @RequestParam(value ="currentPage",defaultValue = "1") Integer currentPage,
                                     @RequestParam(value="recordId",required = false) String recordId
    ){
        if(currentPage<1){
            currentPage=1;
        }
        Workspace workspace = SSOHelper.getWorkspace(request);
        try{
            //水平鉴权
            DSSProjectDO projectDO= dssProjectService.getProjectById(projectId);
            if (projectDO == null || !projectDO.getWorkspaceId().equals(workspace.getWorkspaceId())) {
                LOGGER.warn("try to get illegal project operate record.projectDO:{},project workspace:{},real workspaceId:{}",
                        projectId,
                        projectDO == null?-1:projectDO.getWorkspaceId(),
                        workspace.getWorkspaceId());
                return Message.error("尝试获取非法项目记录");
            }
            ProjectOperateTypeEnum operateTypeEnum=Optional.ofNullable(operateType)
                    .map(ProjectOperateTypeEnum::getByCode).orElse(null);
            ProjectOperateRecordStatusEnum statusEnum = Optional.ofNullable(status)
                    .map(ProjectOperateRecordStatusEnum::getByCode).orElse(null);
            PageInfo<ProjectOperateRecordBO> pageInfo =
                    DSSProjectOperateService.queryRecords(projectId, pageSize, currentPage, recordId,
                            statusEnum, operateTypeEnum);
            List<ProjectOperateRecordVO> recordVOList=
                    pageInfo.getData()
                            .stream().map(ProjectOperateRecordVO::fromBO)
                            .collect(Collectors.toList());
            return Message.ok("获取项目操作记录成功").data("result",recordVOList).data("total",pageInfo.getTotal());
        }catch (Exception e){
            LOGGER.error("failed to get project record!", e);
            return Message.error("获取项目操作记录失败:" + e.getMessage());
        }
    }

    /**
     * 下载导出结果
     * @param recordId
     * @param httpServletResponse
     */
    @GetMapping("/downloadResourcePackage")
    public void downloadResourceOfRecord(@RequestParam("recordId") String recordId, HttpServletResponse httpServletResponse) {
        try {
            String username = SecurityFilter.getLoginUsername(request);
            String projectName=Optional.ofNullable( DSSProjectOperateService.getRecordProjectName(recordId)).orElse(recordId);
            httpServletResponse.addHeader("Content-Disposition", String.format("attachment;filename=%s.zip",projectName+"-prod"));
            OutputStream outputStream = httpServletResponse.getOutputStream();
            BmlResource bmlResource= DSSProjectOperateService.getExportResultByRecordId(recordId);
            Map<String,Object> downRes=
            bmlService.download(username,bmlResource.getResourceId(),bmlResource.getVersion());
            InputStream inputStream = (InputStream) downRes.get("is");
            try {
                IOUtils.copy(inputStream, outputStream);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
            outputStream.flush();
        } catch (IOException e) {
            LOGGER.error("failed to download resource of record：" + recordId, e);
        }
    }
    @PostMapping("/batchExportOrchestrators")
    public  Message batchExportOrchestrators(@RequestBody BatchExportOrchestratorsRequest batchExportOrchestratorsRequest){
        String username = SecurityFilter.getLoginUsername(request);
        Workspace workspace = SSOHelper.getWorkspace(request);
        try {
            if (!hasReleasePermission(username, batchExportOrchestratorsRequest.getProjectId(),true)) {
                return Message.error( "you has no priority to export this project(本用户此项目没有发布权限，不允许导出)");
            }
        }catch (DSSProxyUserErrorException e) {
            return Message.error(e.getMessage());
        }
        try {
            //代理用户开启的话，用户名变成代理用户。
            String proxyUser = ProxyUserConfiguration.isProxyUserEnable() ? dssProxyUserService.getProxyUser(request) : username;
            String recordId= dssProjectService.exportOrchestrators(batchExportOrchestratorsRequest, username,proxyUser, workspace);
            return Message.ok("工作流导出中，导出包请稍后在操作历史中下载").data("recordId",recordId);
        }catch (Exception e){
            LOGGER.error("fail to batch export orchestrators",e);
            return Message.error("执行导出任务失败" + e.getMessage());
        }
    }
    @PostMapping("/batchImportOrchestrators")
    public Message batchImportOrchestrators( @RequestParam( "importType") String importType,
                                             @RequestParam("projectId") Long projectId,
                                             @RequestParam( "labels") String  labels,
                                             @RequestParam( "checkCode") String  checkCode,
                                             @RequestParam(required = false, name = "packageFile") MultipartFile file,
                                             @RequestParam(required = false, name = "packageUri") String packageUri,
                                             @RequestParam(required = false,name= "comment") String comment,
                                             @RequestParam(name="async",defaultValue = "true") Boolean async
                                             ){
        String username = SecurityFilter.getLoginUsername(request);
        try {
            if (!hasReleasePermission(username, projectId,true)) {
                return Message.error( "you has no priority to import into this project(本用户没有此项目发布权限，不允许导入)");
            }
        }catch (DSSProxyUserErrorException e) {
            return Message.error(e.getMessage());
        }
        Workspace workspace = SSOHelper.getWorkspace(request);
        ProjectInfoVo projectInfo = dssProjectService.getProjectInfoById(projectId);
        EnvDSSLabel envLabel = new EnvDSSLabel(labels);
        Message message;
        BmlResource bmlResource=null;
        String packageInfo=null;
        if("file".equals(importType)){
            String fileName;
            try {
                fileName = new String(file.getOriginalFilename().getBytes("ISO8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException unsupportedEncodingException) {
                throw new RuntimeException(unsupportedEncodingException);
            }
            try(InputStream inputStream= file.getInputStream()) {
                bmlResource = bmlService.upload(username, inputStream, fileName, projectInfo.getProjectName());
                message = Message.ok("编排导入中，导入进程请从操作历史中查看");
            }catch (IOException ioException){
                message = Message.error("文件上传失败"+ioException.getMessage());
            }
            packageInfo=fileName;
        }else if("hdfs".equals(importType)){
            try {
                FsPath fsPath=new FsPath(packageUri);
                FileSystem fileSystem = fsService.getFileSystem(username, fsPath);
                if (!fileSystem.exists(fsPath)) {
                    throw new DSSRuntimeException("路径文件不存在");
                }
                try( InputStream inputStream = fileSystem.read(fsPath)){
                    String  fileName = packageUri.substring(packageUri.lastIndexOf('/') + 1);
                    bmlResource= bmlService.upload(username, inputStream, fileName, projectInfo.getProjectName());
                }
                message = Message.ok("编排导入中，导入进程请从操作历史中查看");
            } catch (Exception e) {
                message = Message.error("无法从提供的zip包路径获取zip文件，请确保提供的hdfs链接正确"+e.getMessage());
            }
            packageInfo=packageUri;
        }else {
            String msg=String.format("不支持的资源包上传方式：%s",importType);
            message=Message.error(msg);
        }
        List<String> accessUsers;
        String proxyUser=username;
        if(ProxyUserConfiguration.isProxyUserEnable()) {
            try {
                proxyUser = dssProxyUserService.getProxyUser(request);
                // 这里不把导入用户加上，是因为导入用户有可能不是工程创建用户。
                // 因为 getProdOrchestrators 接口是通过代理用户获取的所有工作流，所以这里不会存在权限问题
                accessUsers = Arrays.asList(proxyUser);
            }catch (DSSProxyUserErrorException e) {
                return Message.error(e.getMessage());
            }

        } else {
            accessUsers = Arrays.asList(username);
        }
        if(bmlResource!=null) {
            Consumer<OrchestratorBatchImportInfo> addPriv = importResult -> importResult.getTo().forEach(DSSExceptionUtils.handling(info -> {
                LOGGER.info("user {} try to setOrchestratorPriv, workspace: {}, project: {}, orchestration: {}, accessUsers: {}.", username,
                    workspace.getWorkspaceName(), projectInfo.getProjectName(), info.getOrchestratorId(), accessUsers);
                DSSOrchestratorService.setOrchestratorPriv(username, (int) workspace.getWorkspaceId(), projectInfo.getId(),
                        projectInfo.getProjectName(), info.getOrchestratorId().intValue(), accessUsers, 1, workspace); // 1 表示公开权限
            }));
            try {
                dssProjectService.importOrchestrators(projectInfo, bmlResource, username, proxyUser, checkCode,
                        packageInfo, envLabel, workspace, Collections.singletonList(addPriv), async, comment);
            }catch (Exception e){
                LOGGER.error("fail to batch import orchestrators",e);
                message= Message.error("编排导入失败" + e.getMessage());
            }
        }

        return message;
    }
    /**
     * 把在生产中心的整个项目发布到调度系统中
     */
    @PostMapping(path="/publishWholeProject")
    public Message publishOrchestratorsDirectly(@NotNull(message = "发布工程id不能为空") @RequestBody PublishWholeProjectRequest publishWholeProjectRequest) {
        Workspace workspace = SSOHelper.getWorkspace(request);
        Long projectId = publishWholeProjectRequest.getProjectId();
        String username = SecurityFilter.getLoginUsername(request);
        String proxyUser;
        try {
            proxyUser = ProxyUserConfiguration.isProxyUserEnable() ? dssProxyUserService.getProxyUser(request) : username;
            if (!hasReleasePermission(username, projectId,false)) {
                return Message.error("you has no permission to publish this project (本用户没有发布权限)");
            }
        }catch (DSSProxyUserErrorException e) {
            return Message.error(e.getMessage());
        }
        Map<String, Object> labels = new HashMap<>();
        labels.put(EnvDSSLabel.DSS_ENV_LABEL_KEY, publishWholeProjectRequest.getLabels());
        RequestFrameworkConvertOrchestration convertOrchestrationRequest = new RequestFrameworkConvertOrchestration();
        convertOrchestrationRequest.setConvertAllOrcs(true);
        convertOrchestrationRequest.setUserName(username);
        convertOrchestrationRequest.setApproveId(publishWholeProjectRequest.getApproveId());
        convertOrchestrationRequest.setProjectId(publishWholeProjectRequest.getProjectId());
        convertOrchestrationRequest.setLabels(labels);
        convertOrchestrationRequest.setWorkspace(workspace);
        convertOrchestrationRequest.setComment(publishWholeProjectRequest.getComment());
        dssProjectService.publishOrchestratorsDirectly(convertOrchestrationRequest,proxyUser);
        return Message.ok("发布中，发布进程请从操作历史中查看");
    }

    /**
     * 判断用户是否有发布权限
     * @param userName 实名用户
     * @param projectId  项目id
     * @return 有则true ，无则false
     */
    private boolean hasReleasePermission(String userName,Long projectId,boolean onlyByProxyUser) throws DSSProxyUserErrorException {
        boolean proxyOk;
        Set<String> releaseUsers = dssProjectUserService.getProjectPriv(projectId).stream()
                .filter(e->e.getPriv()== ProjectUserPrivEnum.PRIV_RELEASE.getRank())
                .map(DSSProjectUser::getUsername).collect(Collectors.toSet());
        boolean userNameOk = releaseUsers.contains(userName);
        if(ProxyUserConfiguration.isProxyUserEnable()) {
            String proxyUser;
            proxyUser = dssProxyUserService.getProxyUser(request);
            proxyOk=releaseUsers.contains(proxyUser);
        }else {
            //没开代理用户功能，只看实名用户
            return userNameOk;
        }
        return onlyByProxyUser?proxyOk: proxyOk||userNameOk;
    }

}

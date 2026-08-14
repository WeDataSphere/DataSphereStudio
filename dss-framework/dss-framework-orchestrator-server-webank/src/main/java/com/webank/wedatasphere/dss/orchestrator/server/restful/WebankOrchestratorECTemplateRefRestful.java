package com.webank.wedatasphere.dss.orchestrator.server.restful;

import com.webank.wedatasphere.dss.common.auditlog.OperateTypeEnum;
import com.webank.wedatasphere.dss.common.auditlog.TargetTypeEnum;
import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.utils.AuditLogUtils;
import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkspaceDefaultDO;
import com.webank.wedatasphere.dss.framework.project.entity.vo.ProjectTemplateVO;
import com.webank.wedatasphere.dss.framework.project.request.WorkspaceDefaultTemplateRequest;
import com.webank.wedatasphere.dss.framework.project.service.OrchestratorECConfTemplateService;
import com.webank.wedatasphere.dss.orchestrator.server.bean.ECTemplateWorkflow;
import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkflowDefaultDO;
import com.webank.wedatasphere.dss.framework.project.dao.ECTemplateWorkflowDefaultMapper;
import com.webank.wedatasphere.dss.framework.project.request.DSSWorkflowUseTemplateRequest;
import com.webank.wedatasphere.dss.framework.project.request.WorkflowDefaultTemplateRequest;
import com.webank.wedatasphere.dss.orchestrator.server.bean.ECTemplateWorkflowDefault;
import com.webank.wedatasphere.dss.orchestrator.server.service.WebankDSSWorkflowUseTemplateService;
import com.webank.wedatasphere.dss.orchestrator.server.service.WebankDSSWorkflowDefaultTemplateService;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.sso.utils.SSOHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 编排应用模板相关接口
 * Author: xlinliu
 * Date: 2023/7/27
 */
@RestController
@RequestMapping(path = "/dss/framework/orchestrator", produces = {"application/json"})
public class WebankOrchestratorECTemplateRefRestful {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebankOrchestratorECTemplateRefRestful.class);
    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    OrchestratorECConfTemplateService orchestratorECConfTemplateService;
    @Autowired
    ECTemplateWorkflowDefaultMapper ecTemplateWorkflowDefaultMapper;
    @Autowired
    WebankDSSWorkflowUseTemplateService workflowUseTemplateService;
    @Autowired
    WebankDSSWorkflowDefaultTemplateService webankDSSWorkflowDefaultTemplateService;

    @RequestMapping(path="getProjectTemplates",method = RequestMethod.GET)
    public Message getProjectTemplates(@RequestParam("projectId") String projectId,
                                       @RequestParam(name = "jobType",required = false)  String jobType,
                                       @RequestParam(name="orchestratorId",required = false) String orchestratorId){
        long workspaceId= SSOHelper.getWorkspace(httpServletRequest).getWorkspaceId();
        try {
            List<ProjectTemplateVO> templateVOS = orchestratorECConfTemplateService.getTemplates(workspaceId, Long.parseLong(projectId));
            if (jobType != null) {
                if (!jobType.startsWith("linkis")) {
                    templateVOS = Collections.emptyList();
                } else {
                    String[] jobTypeSplit = jobType.split("\\.");
                    if (jobTypeSplit.length < 3) {
                        templateVOS = Collections.emptyList();
                    }
                    String engineType = jobTypeSplit[1];
                    templateVOS = templateVOS.stream().filter(e -> engineType.equals(e.getEnginType())).collect(Collectors.toList());
                }
            }
            if (orchestratorId != null) {
                Set<String> defaultTemplates = ecTemplateWorkflowDefaultMapper.getWorkflowDefaultTemplates(Long.parseLong(orchestratorId)).stream()
                        .map(ECTemplateWorkflowDefaultDO::getTemplateId)
                        .collect(Collectors.toSet());
                templateVOS.forEach(
                        e -> e.getChild().forEach(
                                item -> item.setWorkflowDefault(defaultTemplates.contains(item.getTemplateId()))
                        )
                );
            }
            Message message = Message.ok("获取模板列表成功");
            message.data("templates", templateVOS);
            return message;
        } catch (Exception e) {
            LOGGER.error("获取资源模板失败，原因为", e);
            return Message.error("获取资源模板失败，原因为" + e.getMessage());
        }
    }

    @RequestMapping(path="getProjectTemplatesByUser",method = RequestMethod.GET)
    public Message getProjectTemplatesByUser(HttpServletRequest req,
                                       @RequestParam(name = "jobType",required = false)  String jobType){
        long workspaceId= SSOHelper.getWorkspace(httpServletRequest).getWorkspaceId();
        String userName = SecurityFilter.getLoginUsername(req);
        List<ProjectTemplateVO> templateVOS= orchestratorECConfTemplateService.getTemplatesByUser(workspaceId, userName);
        if (jobType != null) {
            if (!jobType.startsWith("linkis")) {
                templateVOS = Collections.emptyList();
            } else {
                String[] jobTypeSplit = jobType.split("\\.");
                if (jobTypeSplit.length < 3) {
                    templateVOS = Collections.emptyList();
                }
                String engineType = jobTypeSplit[1];
                templateVOS = templateVOS.stream().filter(e -> engineType.equals(e.getEnginType())).collect(Collectors.toList());
            }
        }
        Message message=Message.ok("获取模板列表成功");
        message.data("templates", templateVOS);
        return message;
    }

    @RequestMapping(path = "/getTemplateWorkflowPageInfo", method = RequestMethod.GET)
    public Message getTemplateWorkflowList(@Valid DSSWorkflowUseTemplateRequest request) {
        try {
            LOGGER.info("call /orchestrator/getTemplateWorkflowPageInfo request params: {}", request);
            Workspace workspace = SSOHelper.getWorkspace(httpServletRequest);
            PageInfo<ECTemplateWorkflow> pageInfo = workflowUseTemplateService.getTemplateWorkflowList(request);
            return Message.ok().data("pageInfo", pageInfo.getData()).data("total", pageInfo.getTotal());
        } catch (DSSRuntimeException e) {
            return Message.error("获取列表失败。" + e.getMessage());
        }
    }

    @RequestMapping(path = "/getTemplateProjectNames", method = RequestMethod.GET)
    public Message getTemplateProjectNames(@RequestParam("templateId")String templateId) {
        try {
            List<String> templateProjectNames = workflowUseTemplateService.getTemplateProjectNames(templateId);
            return Message.ok().data("projectNames", templateProjectNames);
        } catch (DSSRuntimeException e) {
            return Message.error("获取项目名失败。" + e.getMessage());
        }
    }

    @RequestMapping(path = "/getTemplateflowNames", method = RequestMethod.GET)
    public Message getTemplateflowNames(@RequestParam("templateId")String templateId) {
        try {
            List<String> flowNames = workflowUseTemplateService.getTemplateflowNames(templateId);
            return Message.ok().data("orchestratorNames", flowNames);
        } catch (DSSRuntimeException e) {
            return Message.error("获取项目名失败。" + e.getMessage());
        }
    }

    @RequestMapping(path = "/saveTemplateRef", method = RequestMethod.PUT)
    public Message saveTemplateRef(HttpServletRequest req,@RequestBody WorkflowDefaultTemplateRequest request) {
        try {
            if(request.getProjectId() == null){
                throw new DSSRuntimeException("项目ID不能为空");
            }
            if(StringUtils.isEmpty(request.getOrchestratorId())){
                throw new DSSRuntimeException("编排ID不能为空");
            }
            Workspace workspace = SSOHelper.getWorkspace(httpServletRequest);
            Long workspaceId = workspace.getWorkspaceId();
            String workspaceName = workspace.getWorkspaceName();
            String userName = SecurityFilter.getLoginUsername(req);
            request.setCreateUser(userName);
            webankDSSWorkflowDefaultTemplateService.saveTemplateRef(request);
            AuditLogUtils.printLog(userName, workspaceId.toString(), workspaceName, TargetTypeEnum.DSS_WORKFLOW_DEFAULT_TEMPLATE,
                    request.getTemplateIds().toString(), null, OperateTypeEnum.UPDATE, request);
            return Message.ok();
        } catch (DSSRuntimeException e) {
            return Message.error("模板报错失败。" + e.getMessage());
        }
    }

    @RequestMapping(path = "/getWrokflowDefaultTemplates", method = RequestMethod.GET)
    public Message getWorkflowDefaultTemplates(@RequestParam(value = "orchestratorId",required = true) Long orchestratorId){
        try {
            List<ECTemplateWorkflowDefault> wrokflowDefaultTemplates = webankDSSWorkflowDefaultTemplateService.getWrokflowDefaultTemplates(orchestratorId);
            return Message.ok().data("wrokflowDefaultTemplates",wrokflowDefaultTemplates);
        } catch (DSSRuntimeException e) {
            return Message.error("模板报错失败。" + e.getMessage());
        }
    }


    @RequestMapping(path = "/getWorkspaceTemplates",method = RequestMethod.GET)
    public Message getWorkspaceTemplates(@RequestParam(value = "workspaceId",required = false) Long workspaceId){

        if(workspaceId == null){
            workspaceId= SSOHelper.getWorkspace(httpServletRequest).getWorkspaceId();
        }

        try{
            List<ProjectTemplateVO> templateVOS = orchestratorECConfTemplateService.getWorkspaceTemplates(workspaceId);

            return  Message.ok("获取模板列表成功").data("templates", templateVOS);
        }catch (Exception e){
            LOGGER.error("获取模板列表失败: ", e);
            return Message.error("获取模板列表失败: " + e.getMessage());
        }

    }

    @RequestMapping(path = "/saveWorkspaceTemplateRef",method = RequestMethod.POST)
    public Message saveWorkspaceTemplateRef(@RequestBody WorkspaceDefaultTemplateRequest request) {

        try{

            Workspace workspace = SSOHelper.getWorkspace(httpServletRequest);

            if(request.getWorkspaceId() == null){
                request.setWorkspaceId(workspace.getWorkspaceId());
            }

            if(!Objects.equals(workspace.getWorkspaceId(),request.getWorkspaceId())){
                throw new DSSErrorException(90053,"当前工作空间与cookie中的不一致，重新刷新页面后在操作");
            }

            String username = SecurityFilter.getLoginUsername(httpServletRequest);

            webankDSSWorkflowDefaultTemplateService.saveWorkspaceTemplateRef(request,username);

            AuditLogUtils.printLog(username,String.valueOf(request.getWorkspaceId()), workspace.getWorkspaceName(), TargetTypeEnum.DSS_WORKSPACE_DEFAULT_TEMPLATE,
                    String.valueOf(request.getTemplateIds()), null, OperateTypeEnum.UPDATE, request);

            return Message.ok("当前工作空间默认模板信息更新成功");
        }catch (Exception e){

            LOGGER.error("工作空间更新失败: " ,e);
            return  Message.error("工作空间更新失败: " + e.getMessage());
        }

    }


    @RequestMapping(path = "/getWorkspaceDefaultTemplates", method = RequestMethod.GET)
    public Message getWorkspaceDefaultTemplates(@RequestParam(value = "workspaceId",required = false) Long workspaceId){
        try {
            Workspace workspace = SSOHelper.getWorkspace(httpServletRequest);
            if(workspaceId == null){
                workspaceId = workspace.getWorkspaceId();
            }

            if(!Objects.equals(workspace.getWorkspaceId(),workspaceId)){
                throw new DSSErrorException(90053,"当前工作空间与cookie中的不一致，重新刷新页面后在操作");
            }

            List<ECTemplateWorkspaceDefaultDO> workspaceDefaultTemplates = webankDSSWorkflowDefaultTemplateService.getWorkspaceDefaultTemplates(workspaceId);
            return Message.ok().data("workspaceDefaultTemplates",workspaceDefaultTemplates);
        } catch (Exception e) {
            LOGGER.error("获取默认模板失败: " ,e);
            return Message.error("获取默认模板失败: " + e.getMessage());
        }
    }



}

package com.webank.wedatasphere.dss.orchestrator.server.restful;

import com.webank.wedatasphere.dss.common.auditlog.OperateTypeEnum;
import com.webank.wedatasphere.dss.common.auditlog.TargetTypeEnum;
import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.utils.AuditLogUtils;
import com.webank.wedatasphere.dss.framework.project.dao.ECTemplateWorkflowDefaultMapper;
import com.webank.wedatasphere.dss.framework.project.dao.entity.ECTemplateWorkflowDefaultDO;
import com.webank.wedatasphere.dss.framework.project.entity.vo.ProjectTemplateVO;
import com.webank.wedatasphere.dss.framework.project.request.DSSWorkflowUseTemplateRequest;
import com.webank.wedatasphere.dss.framework.project.request.WorkflowDefaultTemplateRequest;
import com.webank.wedatasphere.dss.framework.project.service.OrchestratorECConfTemplateService;
import com.webank.wedatasphere.dss.orchestrator.server.bean.ECTemplateWorkflow;
import com.webank.wedatasphere.dss.orchestrator.server.bean.ECTemplateWorkflowDefault;
import com.webank.wedatasphere.dss.orchestrator.server.service.DSSWorkflowDefaultTemplateService;
import com.webank.wedatasphere.dss.orchestrator.server.service.DSSWorkflowUseTemplateService;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.sso.utils.SSOHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 编排应用模板相关接口
 * Author: xlinliu
 * Date: 2023/7/27
 */
@RestController
@RequestMapping(path = "/dss/framework/orchestrator", produces = {"application/json"})
public class OrchestratorECTemplateRefRestful {
    private final static Logger LOGGER = LoggerFactory.getLogger(OrchestratorECTemplateRefRestful.class);
    @Autowired
    HttpServletRequest httpServletRequest;
    @Autowired
    OrchestratorECConfTemplateService orchestratorECConfTemplateService;
    @Autowired
    ECTemplateWorkflowDefaultMapper ecTemplateWorkflowDefaultMapper;
    @Autowired
    DSSWorkflowUseTemplateService workflowUseTemplateService;
    @Autowired
    DSSWorkflowDefaultTemplateService DSSWorkflowDefaultTemplateService;

    @RequestMapping(path="getProjectTemplates",method = RequestMethod.GET)
    public Message getProjectTemplates(@RequestParam("projectId") String projectId,
                                       @RequestParam(name = "jobType",required = false)  String jobType,
                                       @RequestParam(name="orchestratorId",required = false) String orchestratorId){
        long workspaceId= SSOHelper.getWorkspace(httpServletRequest).getWorkspaceId();

        List<ProjectTemplateVO> templateVOS= orchestratorECConfTemplateService.getTemplates(workspaceId, Long.parseLong(projectId));
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
            DSSWorkflowDefaultTemplateService.saveTemplateRef(request);
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
            List<ECTemplateWorkflowDefault> wrokflowDefaultTemplates = DSSWorkflowDefaultTemplateService.getWrokflowDefaultTemplates(orchestratorId);
            return Message.ok().data("wrokflowDefaultTemplates",wrokflowDefaultTemplates);
        } catch (DSSRuntimeException e) {
            return Message.error("模板报错失败。" + e.getMessage());
        }
    }
}

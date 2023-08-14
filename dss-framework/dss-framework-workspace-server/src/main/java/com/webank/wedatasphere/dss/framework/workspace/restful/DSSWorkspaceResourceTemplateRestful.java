package com.webank.wedatasphere.dss.framework.workspace.restful;

import com.webank.wedatasphere.dss.common.auditlog.OperateTypeEnum;
import com.webank.wedatasphere.dss.common.auditlog.TargetTypeEnum;
import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.common.utils.AuditLogUtils;
import com.webank.wedatasphere.dss.framework.workspace.bean.*;
import com.webank.wedatasphere.dss.framework.workspace.bean.request.*;
import com.webank.wedatasphere.dss.framework.workspace.bean.vo.ECConfTemplateApplyInfoVO;
import com.webank.wedatasphere.dss.framework.workspace.bean.vo.ECConfTemplateApplyRuleVO;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceService;
import com.webank.wedatasphere.dss.framework.workspace.service.ECConfTemplateApplyRuleService;
import com.webank.wedatasphere.dss.framework.workspace.service.ECConfTemplateService;
import com.webank.wedatasphere.dss.framework.workspace.util.DSSWorkspaceConstant;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.sso.utils.SSOHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工作空间管理中与资源模板相关操作的接口
 * Author: xlinliu
 * Date: 2023/6/14
 */
@RequestMapping(path = "/dss/framework/workspace/engineconf", produces = {"application/json"})
@RestController
public class DSSWorkspaceResourceTemplateRestful {
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    DSSWorkspaceService dssWorkspaceService;
    @Autowired
    private ECConfTemplateService ecConfTemplateService;
    @Autowired
    private ECConfTemplateApplyRuleService ecConfTemplateApplyRuleService;
    @GetMapping("checkConfTemplateName")
    public Message checkConfTemplateName(@RequestParam("name") String name){
        try {
            ecConfTemplateService.checkConfTemplateName(name);
            return Message.ok().data("repeat",false);
        }catch (DSSRuntimeException e){
            return Message.error(e.getMessage());
        }
    }
    @GetMapping("getEngineTypeList")
    public Message getEngineTypeList(@RequestParam(name = "application", required = false) String application) {
        String username = SecurityFilter.getLoginUsername(httpServletRequest);
        try {
            List<String> engineTypes = ecConfTemplateService.getEngineTypeList(application, username);
            return Message.ok().data("engineTypes", engineTypes);
        } catch ( DSSRuntimeException e) {
            return Message.error("获取引擎列表失败。" + e.getMessage());
        }
    }
    @GetMapping("getEngineNameList")
    public Message getEngineNameList(@RequestParam(name = "application", required = false) String application) {
        String username = SecurityFilter.getLoginUsername(httpServletRequest);
        try {
            List<String> engineTypes = ecConfTemplateService.getEngineNameList(application, username);
            return Message.ok().data("engineTypes", engineTypes);
        } catch ( DSSRuntimeException e) {
            return Message.error("获取引擎列表失败。" + e.getMessage());
        }
    }
    @GetMapping("getApplicationList")
    public Message getApplicationList(){
        String username = SecurityFilter.getLoginUsername(httpServletRequest);
        try {
            List<String> applications = ecConfTemplateService.getApplicationList(username);
            return Message.ok().data("applications", applications);
        } catch ( DSSRuntimeException e) {
            return Message.error("获取应用列表失败。" + e.getMessage());
        }
    }
    @PutMapping("saveConfTemplate")
    public Message saveConfTemplate(@RequestBody ECConfTemplate ecConfTemplate){
        String userName=  SecurityFilter.getLoginUsername(httpServletRequest);
        Workspace workspace= SSOHelper.getWorkspace(httpServletRequest);
        Long workspaceId=workspace.getWorkspaceId();
        String workspaceName = workspace.getWorkspaceName();
        if (!dssWorkspaceService.isAdminUser(workspaceId,userName)) {
            return Message.error("you have no permission to admin this workspace!(您好，您不是本工作空间管理员，没有权限管理空间)");
        }
        try {
            ECConfTemplate savedTemplate = ecConfTemplateService.saveTemplate(ecConfTemplate, workspaceId, userName);
            AuditLogUtils.printLog(userName, workspaceId.toString(), workspaceName, TargetTypeEnum.EC_CONF_TEMPLATE,
                    savedTemplate.getTemplateId(),
                    savedTemplate.getName(),
                    ecConfTemplate.getTemplateId()==null? OperateTypeEnum.CREATE: OperateTypeEnum.UPDATE,
                    savedTemplate);
            return Message.ok().data("templateId", savedTemplate.getTemplateId());
        }catch (DSSRuntimeException e){
            return Message.error("保存模板失败。"+e.getMessage());
        }
    }
    @GetMapping("getConfTemplateList")
    public Message getConfTemplateList(@Valid ECConfTemplateGetRequest request){
        try {
            Workspace workspace= SSOHelper.getWorkspace(httpServletRequest);
            Long workspaceId=workspace.getWorkspaceId();
            if(!workspaceId.equals(request.getWorkspaceId())){
                return Message.error("您非本工作空间成员，无权查看模板");
            }
            PageInfo<ECConfTemplate> pageInfo = ecConfTemplateService.getTemplateList(request);
            return Message.ok().data("templateList", pageInfo.getData())
                    .data("total",pageInfo.getTotal());
        }catch (DSSRuntimeException e){
            return Message.error("获取模板失败。"+e.getMessage());
        }
    }
    @GetMapping("getConfTemplateParamDetail")
    public Message getConfTemplateParamDetail(@RequestParam(name="templateId",required = false)String templateId
            ,@RequestParam(name="engineType",required = false) String engineType){
        String userName=  SecurityFilter.getLoginUsername(httpServletRequest);
        List<ECConfItem> conf;
        String description=null;
        try{
            if (templateId != null) {
                ECConfTemplate template = ecConfTemplateService.getTemplateParamDetail(templateId, userName);
                conf = template.getParamDetails();
                description = template.getDescription();
            } else if (engineType != null) {
                conf = ecConfTemplateService.getTemplateParamConf(engineType, userName);
            } else {
                return Message.error("接口参数错误，templateId、engineType至少需要一个参数。");
            }
            return Message.ok().data("conf", conf).data("description",description);
        } catch ( DSSRuntimeException e) {
            return Message.error("获取模板参数失败。" + e.getMessage());
        }
    }

    @GetMapping("getConfTemplateUserList")
    public Message getConfTemplateUserList(@Valid TemplatePermissionUsersGetRequest request) {
        try{
            PageInfo<String> pageInfo = ecConfTemplateService.getPermissionUserList(request);
            List<PermissionUser> users = pageInfo.getData().stream()
                    .filter(
                            e -> request.getUsername() == null || e.equals(request.getUsername())
                    )
                    .map(PermissionUser::new).collect(Collectors.toList());
            long total=request.getUsername() == null || pageInfo.getTotal()==0 ?pageInfo.getTotal() : 1L;
            return Message.ok().data("users", users).data("total", total);
        } catch ( DSSRuntimeException e) {
            return Message.error("获取可见用户列表失败。" + e.getMessage());
        }
    }
    @GetMapping("getConfTemplateApplyInfo")
    public Message getConfTemplateApplyInfo(@Valid ConfTemplateApplyInfoGetRequest request){
        try{
            Workspace workspace= SSOHelper.getWorkspace(httpServletRequest);
            Long workspaceId=workspace.getWorkspaceId();
            PageInfo<ECConfTemplateApplyInfoVO> pageInfo = ecConfTemplateApplyRuleService.getTemplateApplyRecordGroupByApplication(request,workspaceId);
            return Message.ok().data("applyInfo", pageInfo.getData()).data("total", pageInfo.getTotal());
        } catch ( DSSRuntimeException e) {
            return Message.error("获取应用引用列表失败。" + e.getMessage());
        }
    }
    @PostMapping("deleteConfTemplate/{templateId}")
    public Message deleteConfTemplate(@PathVariable("templateId") String templateId){
        String userName=  SecurityFilter.getLoginUsername(httpServletRequest);
        Workspace workspace= SSOHelper.getWorkspace(httpServletRequest);
        Long workspaceId=workspace.getWorkspaceId();
        String workspaceName = workspace.getWorkspaceName();
        if (!dssWorkspaceService.isAdminUser(workspaceId,userName)) {
            return Message.error("you have no permission to admin this workspace!(您好，您不是本工作空间管理员，没有权限管理空间)");
        }
        try {
            ECConfTemplate template = ecConfTemplateService.getTemplate(templateId);
            if(template==null){
                return Message.error("删除的模板不存在。");
            }
            //根据模板ID查询该模板是否被引用
            if(ecConfTemplateService.checkTemplateUse(templateId)){
                return Message.error("请先解除模板在工作流的引用");
            }
            ECConfTemplateRuleGetRequest ruleGetRequest=new ECConfTemplateRuleGetRequest();
            ruleGetRequest.setWorkspaceId(workspaceId);
            ruleGetRequest.setTemplateName(template.getName());
            ruleGetRequest.setRuleType(ECConfTemplateApplyRule.RULE_TYPE_NEWUSER);
            ruleGetRequest.setPageNow(1);
            ruleGetRequest.setPageSize(10);
            PageInfo<?> rules = ecConfTemplateApplyRuleService.getRuleList(ruleGetRequest);
            if(rules.getTotal()!=0){
                return Message.error("请先删除该模板关联的工作空间新用户规则");
            }
            ecConfTemplateService.deleteTemplate(templateId);
            AuditLogUtils.printLog(userName, workspaceId.toString(), workspaceName, TargetTypeEnum.EC_CONF_TEMPLATE,
                    template.getTemplateId(),
                    template.getName(),
                    OperateTypeEnum.DELETE,
                    template);
            return Message.ok().data("templateId", template.getTemplateId());
        }catch (DSSRuntimeException e){
            return Message.error("删除模板失败。"+e.getMessage());
        }
    }
    @PutMapping("saveConfTemplateApplyRule")
    public Message saveConfTemplateApplyRule(@RequestBody ECConfTemplateApplyRuleSaveRequest request){
        String userName=  SecurityFilter.getLoginUsername(httpServletRequest);
        Workspace workspace= SSOHelper.getWorkspace(httpServletRequest);
        Long workspaceId=workspace.getWorkspaceId();
        String workspaceName = workspace.getWorkspaceName();
        if (!dssWorkspaceService.isAdminUser(workspaceId,userName)) {
            return Message.error("you have no permission to admin this workspace!(您好，您不是本工作空间管理员，没有权限管理空间)");
        }
        if ((DSSWorkspaceConstant.DEFAULT_WORKSPACE_NAME.getValue().equals(workspaceName)
                || DSSWorkspaceConstant.DEFAULT_0XWORKSPACE_NAME.getValue().equals(workspaceName))
        &&(request.getPermissionType() == ECConfTemplateApplyRule.ALL_USER_PERMISSION_TYPE
                || request.getPermissionType() == ECConfTemplateApplyRule.ONLY_NEW_USER_PERMISSION_TYPE)) {

            return Message.error("" + workspaceName + "是系统默认工作空间，默认工作空间内不允许对所有用户执行规则");
        }

        try {
            ECConfTemplateApplyRule savedRule = ecConfTemplateApplyRuleService.saveRule(request, userName,workspaceId);
            if(savedRule.getRuleType()==ECConfTemplateApplyRule.RULE_TYPE_NORMAL) {
                ecConfTemplateApplyRuleService.executeRule(savedRule, userName, workspaceId);
            }
            AuditLogUtils.printLog(userName, workspaceId.toString(), workspaceName, TargetTypeEnum.EC_CONF_TEMPLATE,
                    savedRule.getRuleId(),
                    savedRule.getTemplate().getName(),
                     OperateTypeEnum.CREATE,
                    savedRule);
            return Message.ok().data("ruleId", savedRule.getRuleId());
        }catch (DSSRuntimeException e){
            return Message.error("保存应用规则失败。"+e.getMessage());
        }
    }
    @GetMapping("getConfTemplateApplyRuleList")
    public Message getConfTemplateApplyRuleList(@Valid ECConfTemplateRuleGetRequest request){
        try {
            Workspace workspace= SSOHelper.getWorkspace(httpServletRequest);
            Long workspaceId=workspace.getWorkspaceId();
            if(!workspaceId.equals(request.getWorkspaceId())){
                return Message.error("您非本工作空间成员，无权查看应用规则");
            }
            PageInfo<ECConfTemplateApplyRuleVO> pageInfo = ecConfTemplateApplyRuleService.getRuleList(request);
            return Message.ok().data("ruleList", pageInfo.getData())
                    .data("total",pageInfo.getTotal());
        }catch (DSSRuntimeException e){
            return Message.error("获取应用规则失败。"+e.getMessage());
        }
    }

    @GetMapping("getConfTemplateApplyRuleUserList")
    public Message getConfTemplateApplyRuleUserList(@Valid RulePermissionUsersGetRequest request) {
        try{
            PageInfo<String> pageInfo = ecConfTemplateApplyRuleService.getPermissionUserList(request);
            List<PermissionUser> users = pageInfo.getData().stream()
                    .filter(
                            e -> request.getUsername() == null || e.equals(request.getUsername())
                    )
                    .map(PermissionUser::new).collect(Collectors.toList());
            long total = request.getUsername() == null || pageInfo.getTotal()==0  ? pageInfo.getTotal() : 1L;
            return Message.ok().data("users", users).data("total", total);
        } catch ( DSSRuntimeException e) {
            return Message.error("获取覆盖范围用户列表失败。" + e.getMessage());
        }
    }
    @PostMapping("deleteConfTemplateApplyRule/{ruleId}")
    public Message  deleteConfTemplateApplyRule(@PathVariable("ruleId")String ruleId){
        String userName=  SecurityFilter.getLoginUsername(httpServletRequest);
        Workspace workspace= SSOHelper.getWorkspace(httpServletRequest);
        Long workspaceId=workspace.getWorkspaceId();
        String workspaceName = workspace.getWorkspaceName();
        if (!dssWorkspaceService.isAdminUser(workspaceId,userName)) {
            return Message.error("you have no permission to admin this workspace!(您好，您不是本工作空间管理员，没有权限管理空间)");
        }
        try {
            ECConfTemplateApplyRule rule = ecConfTemplateApplyRuleService.getRule(ruleId);
            if(rule==null){
                return Message.error("删除的应用规则不存在。");
            }
            if (rule.getRuleType() == ECConfTemplateApplyRule.RULE_TYPE_NORMAL) {
                return Message.error("临时规则不允许删除");
            }
            ecConfTemplateApplyRuleService.deleteRule(ruleId);
            AuditLogUtils.printLog(userName, workspaceId.toString(), workspaceName, TargetTypeEnum.EC_CONF_TEMPLATE,
                    rule.getRuleId(),
                    rule.getTemplate().getName(),
                    OperateTypeEnum.DELETE,
                    rule);
            return Message.ok().data("ruleId", rule.getRuleId());
        }catch (DSSRuntimeException e){
            return Message.error("删除应用规则失败。"+e.getMessage());
        }
    }

    @GetMapping("getConfTemplateApplyHistory")
    public Message getConfTemplateApplyHistory(@Valid ECConfTemplateRuleExecuteRecordGetRequest request){
        try {
            Workspace workspace= SSOHelper.getWorkspace(httpServletRequest);
            Long workspaceId=workspace.getWorkspaceId();
            if(!workspaceId.equals(request.getWorkspaceId())){
                return Message.error("您非本工作空间成员，无权查看规则执行记录");
            }
            PageInfo<ECTemplateApplyRecord> pageInfo = ecConfTemplateApplyRuleService.getExecuteRecords(request);
            return Message.ok().data("records", pageInfo.getData())
                    .data("total",pageInfo.getTotal());
        }catch (DSSRuntimeException e){
            return Message.error("获取模板规则执行记录失败。"+e.getMessage());
        }
    }


}

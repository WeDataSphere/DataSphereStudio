package com.webank.wedatasphere.dss.framework.project.service.impl;

import com.webank.wedatasphere.dss.framework.common.exception.DSSFrameworkWarnException;
import com.webank.wedatasphere.dss.framework.project.entity.ProjectOperateRecordBO;
import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectCreateRequest;
import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectDeleteOrRestoreRequest;
import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectModifyRequest;
import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectQueryRequest;
import com.webank.wedatasphere.dss.framework.project.entity.vo.DSSProjectVo;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateTypeEnum;
import com.webank.wedatasphere.dss.framework.project.service.ProjectHttpRequestHook;
import com.webank.wedatasphere.dss.framework.project.service.WebankDSSProjectOperateService;
import com.webank.wedatasphere.dss.framework.proxy.conf.ProxyUserConfiguration;
import com.webank.wedatasphere.dss.framework.proxy.exception.DSSProxyUserErrorException;
import com.webank.wedatasphere.dss.framework.proxy.service.DssProxyUserService;
import com.webank.wedatasphere.dss.standard.sso.utils.SSOHelper;
import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectTransferRequest;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Function;

/**
 * Created by enjoyyin on 2022/9/22.
 */
@Component
public class ProjectAuditHttpRequestHook implements ProjectHttpRequestHook {

    @Autowired
    private WebankDSSProjectOperateService webankDSSProjectOperateService;
    @Autowired
    private DssProxyUserService dssProxyUserService;

    @Override
    public Message beforeGetAllProjects(HttpServletRequest httpServletRequest, ProjectQueryRequest projectQueryRequest) {
        return null;
    }

    @Override
    public Message beforeCreateProject(HttpServletRequest httpServletRequest, ProjectCreateRequest projectCreateRequest) {
        return null;
    }

    public static ProjectOperateRecordBO createProjectOperateRecordBO(HttpServletRequest httpServletRequest, Long projectId,
        Function<String, String> createContent, DssProxyUserService dssProxyUserService) {
        String username = SecurityFilter.getLoginUsername(httpServletRequest);
        if(ProxyUserConfiguration.isProxyUserEnable()) {
            String proxyUser = null;
            try {
                proxyUser = dssProxyUserService.getProxyUser(httpServletRequest);
            } catch (DSSProxyUserErrorException e) {
                DSSFrameworkWarnException.dealWarnException(e.getErrCode(), e.getDesc(), e);
            }
            username = String.format("%s(%s)", username, proxyUser);
        }
        Long workspaceId=SSOHelper.getWorkspace(httpServletRequest).getWorkspaceId();
        ProjectOperateRecordBO record =  ProjectOperateRecordBO.of(workspaceId,projectId,null,
                createContent.apply(username),username);
        record.success();
        return record;
    }

    public void afterCreateProject(HttpServletRequest httpServletRequest, ProjectCreateRequest projectCreateRequest, DSSProjectVo dssProjectVo) {
        ProjectOperateRecordBO record = createProjectOperateRecordBO(httpServletRequest, dssProjectVo.getId(),
            username -> String.format("%s created project %s.", username, projectCreateRequest.getName()), dssProxyUserService);
        record.setOperateType(ProjectOperateTypeEnum.CREATE_PROJECT);
        webankDSSProjectOperateService.addOneRecord(record);
    }

    @Override
    public Message beforeModifyProject(HttpServletRequest httpServletRequest, ProjectModifyRequest projectModifyRequest) {
        return null;
    }

    public void afterModifyProject(HttpServletRequest httpServletRequest, ProjectModifyRequest projectModifyRequest) {
        ProjectOperateRecordBO record = createProjectOperateRecordBO(httpServletRequest, projectModifyRequest.getId(),
            username -> String.format("%s modified project %s. New information: %s.", username, projectModifyRequest.getName(), projectModifyRequest),
            dssProxyUserService);
        record.setOperateType(ProjectOperateTypeEnum.PROJECT_GRANT);
        webankDSSProjectOperateService.addOneRecord(record);
    }



    @Override
    public Message beforeDeleteProject(HttpServletRequest httpServletRequest, ProjectDeleteOrRestoreRequest projectDeleteRequest) {
        return null;
    }

    public void afterDeleteProject(HttpServletRequest httpServletRequest, ProjectDeleteOrRestoreRequest projectDeleteRequest) {
        ProjectOperateRecordBO record = createProjectOperateRecordBO(httpServletRequest, projectDeleteRequest.getId(),
            username -> String.format("%s modified project.", username), dssProxyUserService);
        record.setOperateType(ProjectOperateTypeEnum.DELETE_PROJECT);
        webankDSSProjectOperateService.addOneRecord(record);
    }

    @Override
    public Message beforeGetDeletedProject(HttpServletRequest httpServletRequest, ProjectQueryRequest projectQueryRequest) {
        return null;
    }
}

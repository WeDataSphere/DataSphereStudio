package com.webank.wedatasphere.dss.framework.project.service.impl;

import com.webank.wedatasphere.dss.common.conf.DSSConfiguration;
import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectQueryRequest;
import com.webank.wedatasphere.dss.framework.project.entity.response.ProjectResponse;
import java.util.List;
import java.util.stream.Collectors;

import com.webank.wedatasphere.dss.framework.proxy.conf.ProxyUserConfiguration;
import com.webank.wedatasphere.dss.framework.proxy.exception.DSSProxyUserErrorException;
import com.webank.wedatasphere.dss.framework.proxy.service.DssProxyUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by enjoyyin on 2021/7/5.
 */
@Component
public class InternalDSSProjectServiceImpl extends DSSProjectServiceImpl {

    @Autowired
    private DssProxyUserService dssProxyUserService;

    @Override
    public List<ProjectResponse> getListByParam(ProjectQueryRequest projectRequest) {
        List<ProjectResponse> projectResponses = super.getListByParam(projectRequest);
        projectResponses.forEach(projectResponse -> {
            //如果单个查询工程，根据登录用户是否含有发布权限
            String username = projectRequest.getUsername();
            projectResponse.setDevProcessPermission(projectResponse.getDevProcessList().stream().collect(Collectors.toList()));
            if(projectRequest.getId() != null && !username.equals(projectResponse.getCreateBy())){
                if(!projectResponse.getReleaseUsers().contains(username)&&
                        (projectRequest.getProxyUser()==null||!projectResponse.getReleaseUsers().contains(projectRequest.getProxyUser())) &&
                        projectResponse.getDevProcessList().contains(DSSConfiguration.ENV_LABEL_VALUE_PROD)){
                    List<String> devProcessPermission = projectResponse.getDevProcessPermission();
                    devProcessPermission.remove(DSSConfiguration.ENV_LABEL_VALUE_PROD);
                }
            }
        });
        return projectResponses;
    }

}

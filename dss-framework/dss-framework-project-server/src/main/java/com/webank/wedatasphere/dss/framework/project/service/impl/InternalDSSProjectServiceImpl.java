package com.webank.wedatasphere.dss.framework.project.service.impl;

import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectQueryRequest;
import com.webank.wedatasphere.dss.framework.project.entity.response.ProjectResponse;
import com.webank.wedatasphere.dss.sender.conf.DSSConfiguration;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by enjoyyin on 2021/7/5.
 */
@Component
public class InternalDSSProjectServiceImpl extends DSSProjectServiceImpl {

    @Override
    public List<ProjectResponse> getListByParam(ProjectQueryRequest projectRequest) {
        List<ProjectResponse> projectResponses = super.getListByParam(projectRequest);
        projectResponses.forEach(projectResponse -> {
            //如果单个查询工程，根据登录用户是否含有发布权限
            String username = projectRequest.getUsername();
            if(projectRequest.getId() != null && !username.equals(projectResponse.getCreateBy())){
                List<String> devProcessList = projectResponse.getDevProcessList();
                if(!projectResponse.getReleaseUsers().contains(username) && devProcessList.contains(DSSConfiguration.ENV_LABEL_VALUE_PROD)){
                    devProcessList.remove(DSSConfiguration.ENV_LABEL_VALUE_PROD);
                }
            }
        });
        return projectResponses;
    }

}

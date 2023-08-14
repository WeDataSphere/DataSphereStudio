package com.webank.wedatasphere.dss.framework.workspace.service.impl;

import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import com.webank.wedatasphere.dss.framework.workspace.dao.DSSWorkspaceMapper;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceRoleCheckService;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DSSWorkspaceRoleCheckServiceImpl implements DSSWorkspaceRoleCheckService {

    @Autowired
    private DSSWorkspaceMapper dssWorkspaceMapper;

    @Autowired
    DSSWorkspaceService dssWorkspaceService;

    private static final List<String> EXCLUDE_USER = Arrays.asList("v_","hduser");

    @Override
    public boolean checkRolesOperation(int workspaceId, String loginUser, String username, List<Integer> roles) {
        boolean flag = EXCLUDE_USER.stream().anyMatch(e -> username.toLowerCase().startsWith(e)) && roles.contains(1);
        if(flag){
            throw new DSSRuntimeException("不支持添加代理用户和微众银行合作伙伴为工作空间管理员");
        }
        // 获取工作空间创建者
        String createBy = dssWorkspaceMapper.getWorkspace(workspaceId).getCreateBy();
        return StringUtils.equals(loginUser, createBy) ?
                (!StringUtils.equals(createBy, username) || roles.contains(1)) :
                (dssWorkspaceService.isAdminUser((long) workspaceId, loginUser) && ((!dssWorkspaceService.isAdminUser((long) workspaceId, username) && !roles.contains(1))
                        || (StringUtils.equals(loginUser, username) && roles.contains(1))));
    }
}

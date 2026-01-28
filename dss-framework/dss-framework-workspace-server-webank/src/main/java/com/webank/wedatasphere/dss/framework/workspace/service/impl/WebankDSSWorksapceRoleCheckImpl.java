package com.webank.wedatasphere.dss.framework.workspace.service.impl;

import com.webank.wedatasphere.dss.common.exception.DSSRuntimeException;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class WebankDSSWorksapceRoleCheckImpl extends DSSWorkspaceRoleCheckServiceImpl {

    private static final List<String> EXCLUDE_USER = Arrays.asList("v_","hduser");

    @Override
    public boolean checkRolesOperation(int workspaceId, String loginUser, String username, List<Integer> roles) {
        boolean flag = EXCLUDE_USER.stream().anyMatch(e -> username.toLowerCase().startsWith(e)) && roles.contains(1);
        if(flag){
            throw new DSSRuntimeException("不支持添加代理用户和微众银行合作伙伴为工作空间管理员");
        }
        return super.checkRolesOperation(workspaceId, loginUser, username, roles);
    }


    @Override
    public boolean checkUserRolesOperation(String username, List<Integer> roles) {

        boolean flag = EXCLUDE_USER.stream().anyMatch(e -> username.toLowerCase().startsWith(e)) && roles.contains(1);

        if(flag){
            throw new DSSRuntimeException("不支持添加代理用户和微众银行合作伙伴为工作空间管理员");
        }

        return  super.checkUserRolesOperation(username, roles);
    }

}

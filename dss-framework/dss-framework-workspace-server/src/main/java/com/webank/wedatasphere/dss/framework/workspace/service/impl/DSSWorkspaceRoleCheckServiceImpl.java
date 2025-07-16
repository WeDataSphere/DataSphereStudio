/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.webank.wedatasphere.dss.framework.workspace.service.impl;

import com.webank.wedatasphere.dss.framework.workspace.dao.DSSWorkspaceMapper;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceRoleCheckService;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DSSWorkspaceRoleCheckServiceImpl implements DSSWorkspaceRoleCheckService {

    @Autowired
    private DSSWorkspaceMapper dssWorkspaceMapper;

    @Autowired
    DSSWorkspaceService dssWorkspaceService;

    @Override
    public boolean checkRolesOperation(int workspaceId, String loginUser, String username, List<Integer> roles) {
        // 获取工作空间创建者
        String createBy = dssWorkspaceMapper.getWorkspace(workspaceId).getCreateBy();
        return StringUtils.equals(loginUser, createBy) ?
                (!StringUtils.equals(createBy, username) || roles.contains(1)) :
                (dssWorkspaceService.isAdminUser((long) workspaceId, loginUser) && ((!dssWorkspaceService.isAdminUser((long) workspaceId, username) && !roles.contains(1))
                        || (StringUtils.equals(loginUser, username) && roles.contains(1))));
    }

    @Override
    public boolean checkUserRolesOperation(String username, List<Integer> roles) {
        return true;
    }


}

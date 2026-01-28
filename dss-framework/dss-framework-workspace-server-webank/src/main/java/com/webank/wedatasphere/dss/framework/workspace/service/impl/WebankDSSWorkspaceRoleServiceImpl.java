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

import com.webank.wedatasphere.dss.framework.workspace.bean.WebankDSSWorkspaceUserRole;
import com.webank.wedatasphere.dss.framework.workspace.dao.WebankDSSWorkspaceRoleMapper;
import com.webank.wedatasphere.dss.framework.workspace.service.WebankDSSWorkspaceRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @Author: bradyli
* @Date: 2021/12/7
* @Description: 
* @Param: 
* @return:  
**/
@Service
public class WebankDSSWorkspaceRoleServiceImpl implements WebankDSSWorkspaceRoleService {

    @Autowired
    private WebankDSSWorkspaceRoleMapper webankDSSWorkspaceRoleMapper;

    @Override
    public void deleteWorkspaceRoleOfUser(Long workspaceId, String userName) throws Exception {
        webankDSSWorkspaceRoleMapper.deleteWorkspaceRoleOfUser(workspaceId,userName);
    }

    @Override
    public List<WebankDSSWorkspaceUserRole> queryUserRoleInWorkSpaces(List<Long> workspaceIdList, String userName) throws Exception {
        List<WebankDSSWorkspaceUserRole> userRoleList = webankDSSWorkspaceRoleMapper.queryUserRoleInWorkSpaces(workspaceIdList,userName);
        return userRoleList;
    }

    @Override
    public void insertUserRoleList(List<WebankDSSWorkspaceUserRole> dssWorkspaceUserRoleList) throws Exception {
        webankDSSWorkspaceRoleMapper.insertUserRoleList(dssWorkspaceUserRoleList);
    }
}

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

package com.webank.wedatasphere.dss.framework.workspace.service;


import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.framework.workspace.bean.vo.StaffInfoVO;

import java.util.List;
import java.util.Map;


public interface DSSWorkspaceUserService {

    void updateWorkspaceUser(List<Integer> roles, int workspaceId, String userName, String creator);

    void deleteWorkspaceUser(String userName, int workspaceId);

    List<StaffInfoVO> listAllDSSUsers();

    List<String> getAllWorkspaceUsers(long workspaceId);

    List<Integer> getUserWorkspaceIds(String userName);

    List<String> getWorkspaceEditUsers(int workspaceId);

    List<String> getWorkspaceReleaseUsers(int workspaceId);

    Long getCountByUsername(String username,int workspaceId);

    Long getUserCount(long workspaceId);

    PageInfo<String> getAllWorkspaceUsersPage(long workspaceId, Integer pageNow, Integer pageSize);

    List<Map<String,Object>> getUserRoleByUserName(String userName);

    boolean clearUserByUserName(String userName);
}

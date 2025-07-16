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
package com.webank.wedatasphere.dss.framework.workspace.bean.request;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.Objects;

public class RevokeUserRole {
    @NotBlank(message = "Required String parameter 'userName' is not present")
    private String userName;

    private Integer[] workspaceIds;

    private Integer[] roleIds;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer[] getWorkspaceIds() {
        return workspaceIds;
    }

    public void setWorkspaceIds(Integer[] workspaceIds) {
        this.workspaceIds = workspaceIds;
    }

    public Integer[] getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Integer[] roleIds) {
        this.roleIds = roleIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevokeUserRole that = (RevokeUserRole) o;
        return Objects.equals(userName, that.userName) && Arrays.equals(workspaceIds, that.workspaceIds) && Arrays.equals(roleIds, that.roleIds);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(userName);
        result = 31 * result + Arrays.hashCode(workspaceIds);
        result = 31 * result + Arrays.hashCode(roleIds);
        return result;
    }
}

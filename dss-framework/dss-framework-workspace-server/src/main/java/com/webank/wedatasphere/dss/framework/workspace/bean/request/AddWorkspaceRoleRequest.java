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

import java.io.Serializable;
import java.util.List;

public class AddWorkspaceRoleRequest implements Serializable {

    private int workspaceId;
    private String roleName ;
    private List<Integer> menuIds;
    private List<Integer> componentIds;

    public int getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(int workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<Integer> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<Integer> menuIds) {
        this.menuIds = menuIds;
    }

    public List<Integer> getComponentIds() {
        return componentIds;
    }

    public void setComponentIds(List<Integer> componentIds) {
        this.componentIds = componentIds;
    }

    @Override
    public String toString() {
        return "AddWorkspaceRoleRequest{" +
                "workspaceId=" + workspaceId +
                ", roleName='" + roleName + '\'' +
                ", menuIds=" + menuIds +
                ", componentIds=" + componentIds +
                '}';
    }
}

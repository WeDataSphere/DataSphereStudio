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
import java.util.Map;

public class UpdateRoleMenuPrivRequest implements Serializable {

    private int menuId;
    private int workspaceId;
    private Map<String,Boolean> menuPrivs;

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }

    public int getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(int workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Map<String, Boolean> getMenuPrivs() {
        return menuPrivs;
    }

    public void setMenuPrivs(Map<String, Boolean> menuPrivs) {
        this.menuPrivs = menuPrivs;
    }

    @Override
    public String toString() {
        return "UpdateRoleMenuPrivRequest{" +
                "menuId=" + menuId +
                ", workspaceId=" + workspaceId +
                ", menuPrivs=" + menuPrivs +
                '}';
    }
}

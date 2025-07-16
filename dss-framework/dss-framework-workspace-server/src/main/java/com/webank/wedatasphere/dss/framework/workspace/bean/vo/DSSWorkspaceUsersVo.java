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
package com.webank.wedatasphere.dss.framework.workspace.bean.vo;

import java.util.List;

public class DSSWorkspaceUsersVo {


    private List<String> editUsers;

    private List<String> accessUsers;

    private List<String> releaseUsers;

    private List<String> createUsers;

    public void setAccessUsers(List<String> accessUsers) {
        this.accessUsers = accessUsers;
    }

    public void setReleaseUsers(List<String> releaseUsers) {
        this.releaseUsers = releaseUsers;
    }

    public void setEditUsers(List<String> editUsers) {
        this.editUsers = editUsers;
    }

    public List<String> getEditUsers() {
        return editUsers;
    }

    public List<String> getAccessUsers() {
        return accessUsers;
    }

    public List<String> getReleaseUsers() {
        return releaseUsers;
    }


    public List<String> getCreateUsers() {
        return createUsers;
    }

    public void setCreateUsers(List<String> createUsers) {
        this.createUsers = createUsers;
    }
}

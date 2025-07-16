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
package com.webank.wedatasphere.dss.git.common.protocol.request;


import java.util.List;

public class GitDeleteRequest extends GitBaseRequest{
    /**
     * 提交更新时的comment
     */
    private String comment;
    /**
     * 需删除的文件列表
     */
    private List<String> deleteFileList;

    public GitDeleteRequest() {
    }

    public GitDeleteRequest(Long workspaceId, String projectName) {
        super(workspaceId, projectName);
    }

    public GitDeleteRequest(String comment, List<String> deleteFileList) {
        this.comment = comment;
        this.deleteFileList = deleteFileList;
    }

    public GitDeleteRequest(Long workspaceId, String projectName, String comment, List<String> deleteFileList) {
        super(workspaceId, projectName);
        this.comment = comment;
        this.deleteFileList = deleteFileList;
    }

    public List<String> getDeleteFileList() {
        return deleteFileList;
    }

    public void setDeleteFileList(List<String> deleteFileList) {
        this.deleteFileList = deleteFileList;
    }

}

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
package com.webank.wedatasphere.dss.git.common.protocol.response;


import com.webank.wedatasphere.dss.git.common.protocol.GitTree;

import java.util.List;

public class GitDiffResponse {
    private List<GitTree> codeTree;
    private List<GitTree> metaTree;
    private String commitId;

    public GitDiffResponse(List<GitTree> codeTree, List<GitTree> metaTree, String commitId) {
        this.codeTree = codeTree;
        this.metaTree = metaTree;
        this.commitId = commitId;
    }

    public GitDiffResponse() {
    }


    public List<GitTree> getCodeTree() {
        return codeTree;
    }

    public void setCodeTree(List<GitTree> codeTree) {
        this.codeTree = codeTree;
    }

    public List<GitTree> getMetaTree() {
        return metaTree;
    }

    public void setMetaTree(List<GitTree> metaTree) {
        this.metaTree = metaTree;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }
}

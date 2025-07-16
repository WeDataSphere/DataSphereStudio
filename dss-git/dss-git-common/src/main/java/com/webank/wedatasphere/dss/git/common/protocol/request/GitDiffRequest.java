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

import com.webank.wedatasphere.dss.common.entity.BmlResource;

import java.util.Map;


public class GitDiffRequest extends GitBaseRequest{
    /**
     * 待修改的文件（key：文件相对路径，value：文件对应BML）
     */
    private Map<String, BmlResource> bmlResourceMap;
    /**
     * 下载BMLReource使用的用户名
     */
    private String username;

    public GitDiffRequest() {
    }

    public GitDiffRequest(Map<String, BmlResource> bmlResourceMap, String username) {
        this.bmlResourceMap = bmlResourceMap;
        this.username = username;
    }

    public GitDiffRequest(Long workspaceId, String projectName, Map<String, BmlResource> bmlResourceMap, String username) {
        super(workspaceId, projectName);
        this.bmlResourceMap = bmlResourceMap;
        this.username = username;
    }


    public Map<String, BmlResource> getBmlResourceMap() {
        return bmlResourceMap;
    }

    public void setBmlResourceMap(Map<String, BmlResource> bmlResourceMap) {
        this.bmlResourceMap = bmlResourceMap;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

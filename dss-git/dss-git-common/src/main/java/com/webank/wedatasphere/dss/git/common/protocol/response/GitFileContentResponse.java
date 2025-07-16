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

public class GitFileContentResponse{
    // 提交前 发布前 文件内容
    private String before;
    // 提交后 发布后 文件内容
    private String after;
    // 反显注释内容 --仅发布时diff需要
    private String beforeAnnotate;
    // 反显CommitId --仅发布时diff需要
    private String beforeCommitId;
    // 反显注释内容 --仅发布时diff需要
    private String AfterAnnotate;
    // 反显CommitId --仅发布时diff需要
    private String AfterCommitId;

    // 文件路径
    private String filePath;

    public GitFileContentResponse() {
    }

    public GitFileContentResponse(String before, String after, String beforeAnnotate, String beforeCommitId, String afterAnnotate, String afterCommitId) {
        this.before = before;
        this.after = after;
        this.beforeAnnotate = beforeAnnotate;
        this.beforeCommitId = beforeCommitId;
        AfterAnnotate = afterAnnotate;
        AfterCommitId = afterCommitId;
    }

    public GitFileContentResponse(String before, String after, String beforeAnnotate, String beforeCommitId, String afterAnnotate, String afterCommitId, String filePath) {
        this.before = before;
        this.after = after;
        this.beforeAnnotate = beforeAnnotate;
        this.beforeCommitId = beforeCommitId;
        AfterAnnotate = afterAnnotate;
        AfterCommitId = afterCommitId;
        this.filePath = filePath;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getBeforeAnnotate() {
        return beforeAnnotate;
    }

    public void setBeforeAnnotate(String beforeAnnotate) {
        this.beforeAnnotate = beforeAnnotate;
    }

    public String getBeforeCommitId() {
        return beforeCommitId;
    }

    public void setBeforeCommitId(String beforeCommitId) {
        this.beforeCommitId = beforeCommitId;
    }

    public String getAfterAnnotate() {
        return AfterAnnotate;
    }

    public void setAfterAnnotate(String afterAnnotate) {
        AfterAnnotate = afterAnnotate;
    }

    public String getAfterCommitId() {
        return AfterCommitId;
    }

    public void setAfterCommitId(String afterCommitId) {
        AfterCommitId = afterCommitId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

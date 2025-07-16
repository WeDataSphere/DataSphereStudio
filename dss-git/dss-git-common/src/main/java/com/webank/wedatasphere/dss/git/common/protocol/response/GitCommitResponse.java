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


import com.google.common.base.Objects;

public class GitCommitResponse{
    private String commitId;
    private String commitTime;
    private String commitUser;
    private String comment;
    private String commentFull;

    public GitCommitResponse(String commitId, String commitTime, String commitUser, String comment) {
        this.commitId = commitId;
        this.commitTime = commitTime;
        this.commitUser = commitUser;
        this.comment = comment;
    }

    public GitCommitResponse() {
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(String commitTime) {
        this.commitTime = commitTime;
    }

    public String getCommitUser() {
        return commitUser;
    }

    public void setCommitUser(String commitUser) {
        this.commitUser = commitUser;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommentFull() {
        return commentFull;
    }

    public void setCommentFull(String commentFull) {
        this.commentFull = commentFull;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GitCommitResponse that = (GitCommitResponse) o;
        return Objects.equal(commitId, that.commitId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(commitId);
    }

    @Override
    public String toString() {
        return "GitCommitResponse{" +
                "commitId=" + commitId +
                ", commitTime='" + commitTime + '\'' +
                ", commitUser='" + commitUser + '\'' +
                ", comment='" + comment + '\'' +
                ", commentFull='" + commentFull + '\'' +
                '}';
    }
}

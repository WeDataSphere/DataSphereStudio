package com.webank.wedatasphere.dss.framework.project.entity.request;

import java.io.Serializable;

public class ProjectCopyRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long projectId;

    private String copyProjectName;

    private Long workspaceId;

    private Boolean associateGit;

    private String gitUser;

    private String gitToken;
    /**
     * 跨工作空间复制项目，目标工作空间。
     */
    private Long targetWorkspaceId;


    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getCopyProjectName() {
        return copyProjectName;
    }

    public void setCopyProjectName(String copyProjectName) {
        this.copyProjectName = copyProjectName;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ProjectCopyRequest{");
        sb.append("projectId=").append(projectId);
        sb.append(", copyProjectName='").append(copyProjectName).append('\'');
        sb.append(", workspaceId=").append(workspaceId);
        sb.append(", associateGit=").append(associateGit);
        sb.append(", gitUser='").append(gitUser).append('\'');
        sb.append(", gitToken='").append(gitToken).append('\'');
        sb.append(", targetWorkspaceId='").append(targetWorkspaceId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public Boolean getAssociateGit() {
        return associateGit;
    }

    public void setAssociateGit(Boolean associateGit) {
        this.associateGit = associateGit;
    }

    public String getGitUser() {
        return gitUser;
    }

    public void setGitUser(String gitUser) {
        this.gitUser = gitUser;
    }

    public String getGitToken() {
        return gitToken;
    }

    public void setGitToken(String gitToken) {
        this.gitToken = gitToken;
    }

    public Long getTargetWorkspaceId() {
        return targetWorkspaceId;
    }

    public void setTargetWorkspaceId(Long targetWorkspaceId) {
        this.targetWorkspaceId = targetWorkspaceId;
    }
}

package com.webank.wedatasphere.dss.framework.workspace.bean.request;

import java.io.Serializable;
import java.util.List;

public class CreateWorkspaceUserRequest implements Serializable {

    private int workspaceId;
    private List<String> userName ;
    private List<Integer> roles;
    private List<String> userId;

    public List<String> getUserName() {
        return userName;
    }

    public void setUserName(List<String> userName) {
        this.userName = userName;
    }

    public List<String> getUserId() {
        return userId;
    }

    public void setUserId(List<String> userId) {
        this.userId = userId;
    }

    public int getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(int workspaceId) {
        this.workspaceId = workspaceId;
    }

    public List<Integer> getRoles() {
        return roles;
    }

    public void setRoles(List<Integer> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "CreateWorkspaceUserRequest{" +
                "workspaceId=" + workspaceId +
                ", userName='" + userName + '\'' +
                ", roles=" + roles +
                ", userId='" + userId + '\'' +
                '}';
    }
}

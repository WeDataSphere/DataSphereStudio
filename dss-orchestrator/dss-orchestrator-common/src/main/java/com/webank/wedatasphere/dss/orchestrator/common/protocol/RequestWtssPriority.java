package com.webank.wedatasphere.dss.orchestrator.common.protocol;

import java.io.Serializable;
import java.util.List;

public class RequestWtssPriority implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> releaseUsers;
    private String loginuser;
    private String projectName;

    public List<String> getReleaseUsers() {
        return releaseUsers;
    }

    public void setReleaseUsers(List<String> releaseUsers) {
        this.releaseUsers = releaseUsers;
    }

    public String getLoginuser() {
        return loginuser;
    }

    public void setLoginuser(String loginuser) {
        this.loginuser = loginuser;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}

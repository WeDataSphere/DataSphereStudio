package com.webank.wedatasphere.dss.detection.server.message;

import java.util.HashMap;

/**
 * @date 2023/3/15 16:05
 */
public class SubmitInformation {

    //提交检测数据的用户
    private String user;

    //提交检测的项目,例如linkis
    private String projectName;

    //数据名，可以是表名等
    private String dataName;

    private HashMap<String, String> tags = new HashMap<>();

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public HashMap<String, String> getTags() {
        return tags;
    }

    public void setTags(HashMap<String, String> tags) {
        this.tags = tags;
    }
}

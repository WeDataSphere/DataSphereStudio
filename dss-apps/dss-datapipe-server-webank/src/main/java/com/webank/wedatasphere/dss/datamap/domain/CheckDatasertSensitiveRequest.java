package com.webank.wedatasphere.dss.datamap.domain;

import java.util.List;

/**
 * Author: xlinliu
 * Date: 2024/10/30
 */
public class CheckDatasertSensitiveRequest {
    private String scriptType;
    private List<String> paths;
    private String taskId;

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}

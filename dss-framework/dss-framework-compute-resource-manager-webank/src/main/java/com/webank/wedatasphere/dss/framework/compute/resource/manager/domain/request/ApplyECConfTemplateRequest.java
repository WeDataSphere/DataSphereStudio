package com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request;

import java.util.List;

/**
 * 模板应用到用户
 * Author: xlinliu
 * Date: 2023/6/28
 */
public class ApplyECConfTemplateRequest {
    private String templateUid;
    private String application;
    private String engineType;
    private String engineVersion;
    private String operator;
    private List<String> userList;

    public ApplyECConfTemplateRequest() {
    }

    public ApplyECConfTemplateRequest(String templateUid, String application, String engineType, String engineVersion, String operator, List<String> userList) {
        this.templateUid = templateUid;
        this.application = application;
        this.engineType = engineType;
        this.engineVersion = engineVersion;
        this.operator = operator;
        this.userList = userList;
    }

    public String getTemplateUid() {
        return templateUid;
    }

    public void setTemplateUid(String templateUid) {
        this.templateUid = templateUid;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }
}

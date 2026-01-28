package com.webank.wedatasphere.dss.framework.workspace.bean.request;

import javax.validation.constraints.NotNull;

/**
 * Author: xlinliu
 * Date: 2023/6/19
 */
public class ECConfTemplateRuleExecuteRecordGetRequest {
    @NotNull
    private Long workspaceId;

    @NotNull
    private Integer pageNow;

    @NotNull
    private Integer pageSize;
    private String engineType;
    private String templateName;
    private String application;
    private String username;

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Integer getPageNow() {
        return pageNow;
    }

    public void setPageNow(Integer pageNow) {
        this.pageNow = pageNow;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

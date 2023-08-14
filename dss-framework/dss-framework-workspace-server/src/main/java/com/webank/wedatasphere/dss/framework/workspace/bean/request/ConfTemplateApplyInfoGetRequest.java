package com.webank.wedatasphere.dss.framework.workspace.bean.request;

import javax.validation.constraints.NotNull;

/**
 * Author: xlinliu
 * Date: 2023/6/27
 */
public class ConfTemplateApplyInfoGetRequest {
    @NotNull
    private String templateId;

    @NotNull
    private Integer pageNow;

    @NotNull
    private Integer pageSize;

    private String username;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

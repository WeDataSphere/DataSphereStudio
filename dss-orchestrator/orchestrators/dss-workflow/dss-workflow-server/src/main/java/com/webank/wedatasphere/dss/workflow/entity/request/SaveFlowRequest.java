package com.webank.wedatasphere.dss.workflow.entity.request;

import com.webank.wedatasphere.dss.common.label.LabelRouteVO;

public class SaveFlowRequest {

    private Long id;
    private String json;
    private String workspaceName;
    private String projectName;
    private String flowEditLock;
    private Boolean isNotHaveLock;
    private LabelRouteVO labels;
    /**
     * DAG 结构校验 warn 确认继续标记（design-doc §6.2）。
     *
     * <p>默认 null/false。仅 warn（开始结束结构）时，前端二次确认后带 forceSave=true 重新请求以放行；
     * error（边引用/环路/重名）永远阻断，forceSave 不能绕过 error。</p>
     */
    private Boolean forceSave;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public LabelRouteVO getLabels() {
        return labels;
    }

    public void setLabels(LabelRouteVO labels) {
        this.labels = labels;
    }

    public String getFlowEditLock() {
        return flowEditLock;
    }

    public void setFlowEditLock(String flowEditLock) {
        this.flowEditLock = flowEditLock;
    }

    public Boolean getNotHaveLock() {
        return isNotHaveLock;
    }

    public void setNotHaveLock(Boolean notHaveLock) {
        isNotHaveLock = notHaveLock;
    }

    public Boolean getForceSave() {
        return forceSave;
    }

    public void setForceSave(Boolean forceSave) {
        this.forceSave = forceSave;
    }
}

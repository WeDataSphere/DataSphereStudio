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
     * <p><b>v2.3</b>：移除④「开始结束结构」后后端不再产生 warn，该字段已无实际用途。
     * 为前端兼容（前端可能仍传 forceSave，删除字段会导致 400 反序列化失败）予以保留，后端忽略即可，零风险。</p>
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

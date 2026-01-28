package com.webank.wedatasphere.dss.framework.release.entity.request;

import javax.validation.constraints.NotNull;

/**
 * 从生产中心发布整个工程到调度系统的请求类
 */
public class PublishWholeProjectRequest {
    @NotNull(message = "项目id不能为空")
    private Long projectId;


    @NotNull(message = "审批单号不能为空")
    private String approveId;


    @NotNull(message = "发布注释不能为空")
    private String comment;

    @NotNull(message = "labels不能为空")
    private String labels;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getApproveId() {
        return approveId;
    }

    public void setApproveId(String approveId) {
        this.approveId = approveId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }
}

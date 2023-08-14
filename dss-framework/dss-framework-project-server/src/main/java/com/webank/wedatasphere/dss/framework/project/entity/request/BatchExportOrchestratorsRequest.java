package com.webank.wedatasphere.dss.framework.project.entity.request;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量导出编排的请求入参
 * Author: xlinliu
 * Date: 2022/8/25
 */
public class BatchExportOrchestratorsRequest {
    /**
     * 项目id
     */
    @NotNull
    private Long projectId;
    /**
     * 需要导出的编排id
     */
    @NotNull(message = "orchestratorIds can not be empty")
    private List<Long> orchestratorIds ;
    /**
     * 环境标签
     */
    @NotNull
    private String labels;

    /**
     * 是否异步请求,默认是
     */
    @NotNull
    private Boolean async = true;
    /**
     * 导出备注
     */
    private String comment;


    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public List<Long> getOrchestratorIds() {
        return orchestratorIds;
    }

    public void setOrchestratorIds(List<Long> orchestratorIds) {
        this.orchestratorIds = orchestratorIds;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

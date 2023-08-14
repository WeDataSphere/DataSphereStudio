package com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.request;

import java.util.List;

/**
 * 引擎列表请求参数
 * Author: xlinliu
 * Date: 2022/11/28
 */
public class ECInstanceRequest {
    /**
     * 引擎实例名
     */
    private List<String> ecInstanceNames;
    /**
     * 引擎状态
     */
    private List<String> status;
    /**
     * 引擎创建者
     */
    private List<String> createUser;
    /**
     * 引擎类型
     */
    private List<String> engineType;
    /**
     * 队列名
     */
    private String yarnQueue;

    /**
     * 工作空间id
     */
    private Long workspaceId;

    /**
     * 分页信息
     */
    private Integer pageNow= 1;
    private  Integer pageSize;
    /**
     * 排序列
     */
    private String sortBy;
    /**
     * 升序降序
     */
    private String orderBy;

    public List<String> getEcInstanceNames() {
        return ecInstanceNames;
    }

    public void setEcInstanceNames(List<String> ecInstanceNames) {
        this.ecInstanceNames = ecInstanceNames;
    }

    public List<String> getStatus() {
        return status;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }

    public List<String> getCreateUser() {
        return createUser;
    }

    public void setCreateUser(List<String> createUser) {
        this.createUser = createUser;
    }

    public List<String> getEngineType() {
        return engineType;
    }

    public void setEngineType(List<String> engineType) {
        this.engineType = engineType;
    }

    public String getYarnQueue() {
        return yarnQueue;
    }

    public void setYarnQueue(String yarnQueue) {
        this.yarnQueue = yarnQueue;
    }

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

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }
}

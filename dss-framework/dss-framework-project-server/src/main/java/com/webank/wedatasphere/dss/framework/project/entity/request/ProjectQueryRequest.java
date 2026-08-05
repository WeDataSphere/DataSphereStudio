/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.framework.project.entity.request;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;


@XmlRootElement
public class ProjectQueryRequest implements Serializable {

    private static final long serialVersionUID=1L;

    private Long id;

    @NotNull(message = "workspaceId不能为空")
    private Long workspaceId;

    private String username;

    private boolean filterProject;
    /**
     * 标签
     */
    private String creatorLabel;

    /**
     * 项目名称列表
     * **/
    private List<String> projectNames;


    /**
     * 项目创建人
     * **/
    private  List<String> createUsers;

    /**
     * 发布权限用户
     * **/
    private List<String> releaseUsers;

    /**
     * 编辑权限用户
     * **/
    private List<String> editUsers;

    /**
     * 查看权限用户
     * **/
    private List<String> accessUsers;

    private Integer pageNow;

    private Integer pageSize;

    private String sortBy;

    private String orderBy;

    private String orderBySql;

    private String queryUser;

    private List<Integer> projectIdList;

    private String proxyUser;

    /**
     * [台账增强] 更新时间范围 - 开始（F3），格式 yyyy-MM-dd，默认 null 跳过。
     *
     * <p>字段类型为 String 而非 Date：DSS 全局 Jackson 配置对 Date 字段反序列化时会
     * 将其置为 null（联调实测传字符串与 timestamp 均失效），改用 String 接收后由
     * service 层显式 parseDate 解析，规避该反序列化问题。
     */
    private String updateStartTime;

    /**
     * [台账增强] 更新时间范围 - 结束（F3），格式 yyyy-MM-dd，默认 null 跳过。
     *
     * <p>字段类型为 String，原因同 {@link #updateStartTime}。
     */
    private String updateEndTime;

    /**
     * [台账增强] 健康状态多选（F3）：EMPTY_PROJECT / STALE / NO_DESCRIPTION，默认 null 跳过
     */
    private List<String> healthStatus;

    public List<Integer> getProjectIdList() {
        return projectIdList;
    }

    public void setProjectIdList(List<Integer> projectIdList) {
        this.projectIdList = projectIdList;
    }

    public String getQueryUser() {
        return queryUser;
    }

    public void setQueryUser(String queryUser) {
        this.queryUser = queryUser;
    }

    public String getOrderBySql() {
        return orderBySql;
    }

    public void setOrderBySql(String orderBySql) {
        this.orderBySql = orderBySql;
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

    public boolean getFilterProject() {
        return filterProject;
    }

    public void setFilterProject(boolean filterProject) {
        this.filterProject = filterProject;
    }

    public String getCreatorLabel() {
        return creatorLabel;
    }

    public void setCreatorLabel(String creatorLabel) {
        this.creatorLabel = creatorLabel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isFilterProject() {
        return filterProject;
    }

    public List<String> getProjectNames() {
        return projectNames;
    }

    public void setProjectNames(List<String> projectNames) {
        this.projectNames = projectNames;
    }

    public List<String> getCreateUsers() {
        return createUsers;
    }

    public void setCreateUsers(List<String> createUsers) {
        this.createUsers = createUsers;
    }

    public List<String> getReleaseUsers() {
        return releaseUsers;
    }

    public void setReleaseUsers(List<String> releaseUsers) {
        this.releaseUsers = releaseUsers;
    }

    public List<String> getEditUsers() {
        return editUsers;
    }

    public void setEditUsers(List<String> editUsers) {
        this.editUsers = editUsers;
    }

    public List<String> getAccessUsers() {
        return accessUsers;
    }

    public void setAccessUsers(List<String> accessUsers) {
        this.accessUsers = accessUsers;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getUpdateStartTime() {
        return updateStartTime;
    }

    public void setUpdateStartTime(String updateStartTime) {
        this.updateStartTime = updateStartTime;
    }

    public String getUpdateEndTime() {
        return updateEndTime;
    }

    public void setUpdateEndTime(String updateEndTime) {
        this.updateEndTime = updateEndTime;
    }

    public List<String> getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(List<String> healthStatus) {
        this.healthStatus = healthStatus;
    }
}

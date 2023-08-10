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
package com.webank.wedatasphere.dss.apiservice.core.vo;


import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ApiServiceVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    //api名称
    private String name;
    //API中文名称
    private String aliasName;
    //api路径
    private String path;
    //协议，1：http，2：https
    private int protocol;
    //方法：POST、GET等
    private String method;
    //标签
    private String tag;
    //可见范围，grantView: 授权可见
    private String scope;
    private String description;
    //0是停止，1是运行中，2是删除
    private Integer status = 1;
    //引擎类型，如spark
    private String type;
    //脚本类型，如sql
    private String runType;
    private Date createTime;
    private Date modifyTime;
    private String creator;
    private String modifier;

    //代理执行用户
    private String executeUser;
    //脚本路径
    private String scriptPath;

    private Long  latestVersionId;
    private String userToken;
    //参数字段详细信息
    private List<ParamVo> params;
    //参数元信息
    private transient Map<String, Object> metadata;
    //脚本内容
    private String content;

    private List<ApiVersionVo> versionVos;

    private Long  workspaceId = 1L;
    //更新时的数据服务id
    private  Long targetServiceId;
    private String comment;
    //是否有新版本，用于前端页面展示
    private boolean existNewerVersion;
    //queryById接口返回给前端的审批单信息
    private ApprovalVo approvalVo;

    public ApprovalVo getApprovalVo() {
        return approvalVo;
    }

    public void setApprovalVo(ApprovalVo approvalVo) {
        this.approvalVo = approvalVo;
    }

    public boolean isExistNewerVersion() {
        return existNewerVersion;
    }

    public void setExistNewerVersion(boolean existNewerVersion) {
        this.existNewerVersion = existNewerVersion;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getExecuteUser() {
        return executeUser;
    }

    public void setExecuteUser(String executeUser) {
        this.executeUser = executeUser;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Long getTargetServiceId() {
        return targetServiceId;
    }

    public void setTargetServiceId(Long targetServiceId) {
        this.targetServiceId = targetServiceId;
    }

    public Long getLatestVersionId() {
        return latestVersionId;
    }

    public void setLatestVersionId(Long latestVersionId) {
        this.latestVersionId = latestVersionId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public List<ApiVersionVo> getVersionVos() {
        return versionVos;
    }

    public void setVersionVos(List<ApiVersionVo> versionVos) {
        this.versionVos = versionVos;
    }

    public List<ParamVo> getParams() {
        return params;
    }

    public void setParams(List<ParamVo> params) {
        this.params = params;
    }


    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }
    public String getRunType() {
        return runType;
    }

    public void setRunType(String runType) {
        this.runType = runType;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public String toString() {
        return "ApiServiceVo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", aliasName='" + aliasName + '\'' +
                ", path='" + path + '\'' +
                ", protocol=" + protocol +
                ", method='" + method + '\'' +
                ", tag='" + tag + '\'' +
                ", scope='" + scope + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", type='" + type + '\'' +
                ", runType='" + runType + '\'' +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", creator='" + creator + '\'' +
                ", modifier='" + modifier + '\'' +
                ", executeUser='" + executeUser + '\'' +
                ", scriptPath='" + scriptPath + '\'' +
                ", latestVersionId=" + latestVersionId +
                ", userToken='" + userToken + '\'' +
                ", params=" + params +
                ", metadata=" + metadata +
                ", content='" + content + '\'' +
                ", versionVos=" + versionVos +
                ", workspaceId=" + workspaceId +
                ", targetServiceId=" + targetServiceId +
                ", comment='" + comment + '\'' +
                '}';
    }
}
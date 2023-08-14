package com.webank.wedatasphere.dss.orchestrator.common.entity.response;

import com.webank.wedatasphere.dss.common.entity.Resource;

import java.io.Serializable;
import java.util.List;

public class CompareWorkflowResult implements Serializable {
    private static final long serialVersionUID = 1L;

    //工作流ID
    private Long appId;

    //工作流名称
    private String appName;

    //状态：新增、删除、修改
    private String status;

    //状态中文名称
    private String statusCn;

    //游节点名称
    private String nodeId;
    //节点名称
    private String nodeName;

    //节点类型
    private String nodeType;

    //节点所在的resource（老版本）
    private List<Resource> resourceList;

    //比较的resource(新版本）
    private List<Resource> compareResourceList;

    //上游节点
    private List<CompareWorkflowResult> parentNodeList;

    //下游节点
    private List<CompareWorkflowResult> childNodeList;
    /**
     * 节点基本信息（老版本）
     */
    private String nodeContent;
    /**
     * 比较的节点基本信息（新版本）
     */
    private String compareNodeContent;

    //更新人
    private String updateUser;

    //更新时间
    private Long updateTime;

    //对应的第一个orc的版本号
    private String firstOrcVersion;

    //对应的第二个orc的版本号
    private String secondOrcVersion;

    public String getFirstOrcVersion() {
        return firstOrcVersion;
    }

    public void setFirstOrcVersion(String firstOrcVersion) {
        this.firstOrcVersion = firstOrcVersion;
    }

    public String getSecondOrcVersion() {
        return secondOrcVersion;
    }

    public void setSecondOrcVersion(String secondOrcVersion) {
        this.secondOrcVersion = secondOrcVersion;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusCn() {
        return statusCn;
    }

    public void setStatusCn(String statusCn) {
        this.statusCn = statusCn;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public List<Resource> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<Resource> resourceList) {
        this.resourceList = resourceList;
    }

    public List<CompareWorkflowResult> getParentNodeList() {
        return parentNodeList;
    }

    public void setParentNodeList(List<CompareWorkflowResult> parentNodeList) {
        this.parentNodeList = parentNodeList;
    }

    public List<CompareWorkflowResult> getChildNodeList() {
        return childNodeList;
    }

    public void setChildNodeList(List<CompareWorkflowResult> childNodeList) {
        this.childNodeList = childNodeList;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public List<Resource> getCompareResourceList() {
        return compareResourceList;
    }

    public void setCompareResourceList(List<Resource> compareResourceList) {
        this.compareResourceList = compareResourceList;
    }

    public String getNodeContent() {
        return nodeContent;
    }

    public void setNodeContent(String nodeContent) {
        this.nodeContent = nodeContent;
    }

    public String getCompareNodeContent() {
        return compareNodeContent;
    }

    public void setCompareNodeContent(String compareNodeContent) {
        this.compareNodeContent = compareNodeContent;
    }
}

package com.webank.wedatasphere.dss.orchestrator.common.entity.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: xlinliu
 * Date: 2023/12/19
 */
public class CompareWorkflowAndSubFlowResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<NodeCompareInfo> nodeList = new ArrayList<>();
    private String historyName;
    private String newName;
    private String status;

    public CompareWorkflowAndSubFlowResult() {
    }

    public CompareWorkflowAndSubFlowResult(List<NodeCompareInfo> nodeList, String historyName, String newName, String status) {
        this.nodeList = nodeList;
        this.historyName = historyName;
        this.newName = newName;
        this.status = status;
    }

    public List<NodeCompareInfo> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<NodeCompareInfo> nodeList) {
        this.nodeList = nodeList;
    }

    public String getHistoryName() {
        return historyName;
    }

    public void setHistoryName(String historyName) {
        this.historyName = historyName;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static class  NodeCompareInfo{

        public NodeCompareInfo() {
        }

        public NodeCompareInfo(String status, String nodeHistoryName, String nodeNewName, String nodeType) {
            this.status = status;
            this.nodeHistoryName = nodeHistoryName;
            this.nodeNewName = nodeNewName;
            this.nodeType = nodeType;
        }

        private String status;
        private String nodeId;
        private String nodeHistoryName;
        private String nodeNewName;
        private String nodeType;
        private String updateUser;
        private long updateTime;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getNodeHistoryName() {
            return nodeHistoryName;
        }

        public void setNodeHistoryName(String nodeHistoryName) {
            this.nodeHistoryName = nodeHistoryName;
        }

        public String getNodeNewName() {
            return nodeNewName;
        }

        public void setNodeNewName(String nodeNewName) {
            this.nodeNewName = nodeNewName;
        }

        public String getNodeType() {
            return nodeType;
        }

        public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
        }

        public String getUpdateUser() {
            return updateUser;
        }

        public void setUpdateUser(String updateUser) {
            this.updateUser = updateUser;
        }

        public long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }
    }
}

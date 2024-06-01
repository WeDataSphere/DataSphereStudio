package com.webank.wedatasphere.dss.framework.appconn.entity;

import org.apache.ibatis.type.Alias;

/**
 * 保存节点关联的UI属性
 */
@Alias("appconnFrameworkNodeToUi")
public class NodeToUi {
    /**
     * 
     */
    private Integer id;
    /**
     * 节点id
     */
    private Integer nodeId;
    /**
     * UI属性的id
     */
    private Integer uiId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getUiId() {
        return uiId;
    }

    public void setUiId(Integer uiId) {
        this.uiId = uiId;
    }
}
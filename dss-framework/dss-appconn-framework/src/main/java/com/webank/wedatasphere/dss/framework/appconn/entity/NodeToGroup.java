package com.webank.wedatasphere.dss.framework.appconn.entity;

import org.apache.ibatis.type.Alias;

/**
 * 保存节点所属类型
 */
@Alias("appconnFrameworkNodeToGroup")
public class NodeToGroup {

    private Integer id;
    /**
     * 节点id
     */
    private Integer nodeId;
    /**
     * 分类id
     */
    private Integer groupId;

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

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }
}
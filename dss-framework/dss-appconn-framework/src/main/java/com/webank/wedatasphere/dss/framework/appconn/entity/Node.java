package com.webank.wedatasphere.dss.framework.appconn.entity;




/**
 * 工作流节点
 * @author arionliu
 * 该对象由如意研发插件生成，对应APIDesign文档信息为(项目名:[5425]DSS-IDE，接口ID:292843，接口标题:保存节点)
 */

public class Node {

    private Integer id;

    private Integer nodeGroup;

    /**
     * 节点名
     */
    private String name;

    /**
     * 对应的appconn名
     */
    private String appconnName;

    /**
     * 节点全路径名，如linkis.python.python
     */
    private String nodeType;

    private Integer jumpType;

    private Integer supportJump;

    private Integer submitToScheduler;

    private Integer enableCopy;

    private Integer shouldCreationBeforeNode;

    /**
     * 工作流节点图标路径，每个工作流节点的图标都存储在对应的 AppConn 之中
     */
    private String iconPath;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public Integer getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(Integer nodeGroup) {
        this.nodeGroup = nodeGroup;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getAppconnName() {
        return appconnName;
    }

    public void setAppconnName(String appconnName) {
        this.appconnName = appconnName;
    }
    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
    public Integer getJumpType() {
        return jumpType;
    }

    public void setJumpType(Integer jumpType) {
        this.jumpType = jumpType;
    }
    public Integer getSupportJump() {
        return supportJump;
    }

    public void setSupportJump(Integer supportJump) {
        this.supportJump = supportJump;
    }
    public Integer getSubmitToScheduler() {
        return submitToScheduler;
    }

    public void setSubmitToScheduler(Integer submitToScheduler) {
        this.submitToScheduler = submitToScheduler;
    }
    public Integer getEnableCopy() {
        return enableCopy;
    }

    public void setEnableCopy(Integer enableCopy) {
        this.enableCopy = enableCopy;
    }
    public Integer getShouldCreationBeforeNode() {
        return shouldCreationBeforeNode;
    }

    public void setShouldCreationBeforeNode(Integer shouldCreationBeforeNode) {
        this.shouldCreationBeforeNode = shouldCreationBeforeNode;
    }
    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }
}
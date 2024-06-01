package com.webank.wedatasphere.dss.framework.appconn.entity;

/**
 * 节点类型
 * @author arionliu
 * 该对象由如意研发插件生成，对应APIDesign文档信息为(项目名:[5425]DSS-IDE，接口ID:292986，接口标题:查询节点分类枚举值)
 */
public class NodeGroup {

    /**
     * 分类的id
     */
    private Integer id;

    /**
     * 中文分类名
     */
    private String name;

    /**
     * 英文分类名
     */
    private String nameEn;

    /**
     * 描述
     */
    private String description;

    /**
     * 数字越小越靠前
     */
    private Integer order;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

}
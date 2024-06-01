package com.webank.wedatasphere.dss.framework.appconn.entity;


import java.util.Date;

/**
 * 菜单类型实体类
 */
public class MenuType {
    /**
     * 
     */
    private Integer id;
    /**
     * 
     */
    private String name;
    /**
     * 
     */
    private String titleEn;
    /**
     * 
     */
    private String titleCn;
    /**
     * 
     */
    private String description;
    /**
     * 
     */
    private Integer isActive;
    /**
     * 
     */
    private String icon;
    /**
     * 
     */
    private Integer order;
    /**
     * 
     */
    private String createBy;
    /**
     * 
     */
    private Date createTime;
    /**
     * 
     */
    private Date lastUpdateTime;
    /**
     * 
     */
    private String lastUpdateUser;

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

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getTitleCn() {
        return titleCn;
    }

    public void setTitleCn(String titleCn) {
        this.titleCn = titleCn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        isActive = isActive;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getLastUpdateUser() {
        return lastUpdateUser;
    }

    public void setLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }
}
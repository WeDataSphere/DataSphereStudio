package com.webank.wedatasphere.dss.framework.compute.resource.manager.domain.response;

import java.util.ArrayList;
import java.util.List;

/**
 * CategoryLabel data体
 * Author: xlinliu
 * Date: 2023/6/15
 */
public class CategoryLabel {


    private Integer categoryId;

    private Integer labelId;

    private String categoryName;

    private List<CategoryLabel> childCategory = new ArrayList<>();

    private String description;

    private String tag;


    private Integer level;

    private String fatherCategoryName;

    public Integer getLabelId() {
        return labelId;
    }

    public void setLabelId(Integer labelId) {
        this.labelId = labelId;
    }

    public String getFatherCategoryName() {
        return fatherCategoryName;
    }

    public void setFatherCategoryName(String fatherCategoryName) {
        this.fatherCategoryName = fatherCategoryName;
    }

    public List<CategoryLabel> getChildCategory() {
        return childCategory;
    }

    public void setChildCategory(List<CategoryLabel> childCategory) {
        this.childCategory = childCategory;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}


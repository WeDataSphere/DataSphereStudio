package com.webank.wedatasphere.dss.bean;

/**
 * 项目实体
 * Author: xlinliu
 * Date: 2023/10/8
 */
public class DWSProject {
    private String projectName;
    private Long projectTaxonomyID;
    private Long projectVersionID;

    public DWSProject() {
    }

    public DWSProject(String projectName, Long projectTaxonomyID, Long projectVersionID) {
        this.projectName = projectName;
        this.projectTaxonomyID = projectTaxonomyID;
        this.projectVersionID = projectVersionID;
    }

    public Long getProjectTaxonomyID() {
        return projectTaxonomyID;
    }

    public void setProjectTaxonomyID(Long projectTaxonomyID) {
        this.projectTaxonomyID = projectTaxonomyID;
    }

    public Long getProjectVersionID() {
        return projectVersionID;
    }

    public void setProjectVersionID(Long projectVersionID) {
        this.projectVersionID = projectVersionID;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}

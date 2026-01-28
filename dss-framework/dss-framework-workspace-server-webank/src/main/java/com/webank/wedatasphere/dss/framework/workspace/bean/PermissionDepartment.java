package com.webank.wedatasphere.dss.framework.workspace.bean;

/**
 * Author: xlinliu
 * Date: 2023/6/26
 */
public class PermissionDepartment {
    private String name;

    public PermissionDepartment() {
    }

    public PermissionDepartment(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

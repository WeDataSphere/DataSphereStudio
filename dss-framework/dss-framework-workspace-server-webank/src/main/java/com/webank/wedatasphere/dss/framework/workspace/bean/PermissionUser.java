package com.webank.wedatasphere.dss.framework.workspace.bean;

/**
 * Author: xlinliu
 * Date: 2023/6/26
 */
public class PermissionUser {
    private String name;

    public PermissionUser() {
    }

    public PermissionUser(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package com.webank.wedatasphere.dss.framework.workspace.bean.vo;

import java.util.List;

/**
 * 模板应用引用记录
 * Author: xlinliu
 * Date: 2023/6/27
 */
public class ECConfTemplateApplyInfoVO {
    private String application;
    private List<String> permissionUsers;

    public ECConfTemplateApplyInfoVO(String application, List<String> permissionUsers) {
        this.application = application;
        this.permissionUsers = permissionUsers;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public List<String> getPermissionUsers() {
        return permissionUsers;
    }

    public void setPermissionUsers(List<String> permissionUsers) {
        this.permissionUsers = permissionUsers;
    }
}

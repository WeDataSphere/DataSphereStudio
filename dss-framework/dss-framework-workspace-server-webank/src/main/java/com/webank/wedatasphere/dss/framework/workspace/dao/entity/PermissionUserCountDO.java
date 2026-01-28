package com.webank.wedatasphere.dss.framework.workspace.dao.entity;

/**
 * Author: xlinliu
 * Date: 2023/6/19
 */
public class PermissionUserCountDO {
    private String groupId;
    private Long userCount;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }
}

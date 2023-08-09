package com.webank.wedatasphere.dss.common.server.beans;

import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

@TableName(value = "dss_user_access_audit")
public class UserAccessAuditBean implements Serializable {
    /**
     * 用户名
     */
    private String userName;
    /**
     * 登录次数
     */
    private Long loginCount;
    /**
     * 第一次登录
     */
    private Date firstLogin;
    /**
     * 最近一次登录
     */
    private Date lastLogin;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Long loginCount) {
        this.loginCount = loginCount;
    }

    public Date getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(Date firstLogin) {
        this.firstLogin = firstLogin;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
}

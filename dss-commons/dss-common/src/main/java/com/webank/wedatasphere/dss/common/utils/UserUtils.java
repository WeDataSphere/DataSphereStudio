package com.webank.wedatasphere.dss.common.utils;

import org.apache.linkis.server.security.SecurityFilter;

import javax.servlet.http.HttpServletRequest;

public class UserUtils {
    public static String getLoginUserName(HttpServletRequest request){
        return SecurityFilter.getLoginUsername(request);
    }
}

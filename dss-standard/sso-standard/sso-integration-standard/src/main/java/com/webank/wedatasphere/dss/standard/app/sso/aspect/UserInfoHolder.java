package com.webank.wedatasphere.dss.standard.app.sso.aspect;

import com.webank.wedatasphere.dss.standard.app.sso.Workspace;

public class UserInfoHolder {
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    private static final ThreadLocal<Workspace> WORKSPACE = new ThreadLocal<>();

    public static void setUserName(String userName){
        USERNAME.set(userName);
    }

    public static String getUserName(){
        return USERNAME.get();
    }

    public static void removeUserName(){
        USERNAME.remove();
    }

    public static void setWorkspace(Workspace workspace){
        WORKSPACE.set(workspace);
    }

    public static  Workspace getWorkspace(){
        return WORKSPACE.get();
    }

    public static void removeWorkspace(){
        WORKSPACE.remove();
    }
}

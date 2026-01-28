package com.webank.wedatasphere.dss.bean;

/**
 * 命令行参数
 * Author: xlinliu
 * Date: 2023/10/7
 */
public class CommandArg {
    private String cookie;
    private String projectName;
    private String outDir;

    public CommandArg(String cookie, String projectName, String outDir) {
        this.cookie = cookie;
        this.projectName = projectName;
        this.outDir = outDir;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getOutDir() {
        return outDir;
    }

    public void setOutDir(String outDir) {
        this.outDir = outDir;
    }
}

package com.webank.wedatasphere.dss.bean.flowcomponent;

/**
 * 节点资源
 * Author: xlinliu
 * Date: 2023/10/12
 */
public class DSSNodeResource {
    private String fileName;
    private String resourceId;
    private String version;

    public DSSNodeResource() {
    }

    public DSSNodeResource(String fileName, String resourceId, String version) {
        this.fileName = fileName;
        this.resourceId = resourceId;
        this.version = version;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

package com.webank.wedatasphere.dss.framework.appconn.common;

public enum ResourceTypeEnum {

    RELATED("related"),
    UPLOAD("upload");

    private String name;

    ResourceTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}

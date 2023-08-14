package com.webank.wedatasphere.dss.framework.project.enums;

public enum HandOverTypeEnum {

    ONE("1", "基于用户级别交接"),

    TWO("2", "基于工作空间级别交接"),

    THREE("3", "基于工程级别交接");

    private String value;
    private String desc;

    HandOverTypeEnum(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }
}

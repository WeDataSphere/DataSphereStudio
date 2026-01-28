package com.webank.wedatasphere.dss.detection.server.result;

/**
 * @date 2023/3/15 14:36
 */
public enum ResultType {
    /**
     * NORMAL默认类型
     */
    NORMAL("normal");

    private String name;

    ResultType(String name) {
        this.name = name;
    }

    public String getName() {
        return name();
    }
}

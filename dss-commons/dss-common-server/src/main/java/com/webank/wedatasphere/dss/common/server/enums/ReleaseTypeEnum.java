package com.webank.wedatasphere.dss.common.server.enums;

import java.util.Arrays;

public enum ReleaseTypeEnum {
    /**
     * 发布成DSS
     */
    DSS(0),
    /**
     * 发不成scriptis
     */
    SCRIPTIS(1),
    ;
    private int code;
    private ReleaseTypeEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
    public static ReleaseTypeEnum getByCode(int code){
        return Arrays.stream(ReleaseTypeEnum.values()).filter(e->e.code==code).findAny().orElse(null);
    }
}

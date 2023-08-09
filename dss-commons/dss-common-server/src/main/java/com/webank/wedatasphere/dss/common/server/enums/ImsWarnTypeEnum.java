package com.webank.wedatasphere.dss.common.server.enums;

import java.util.Arrays;

public enum ImsWarnTypeEnum {
    IMS_ALERT_CLEAR("0", "clear"),
    IMS_ALERT_CRITICAL("1","critical"),
    IMS_ALERT_MAJOR("2","major"),
    IMS_ALERT_MINOR("3","minor"),
    IMS_ALERT_WARNING("4","warning"),
    IMS_ALERT_INFO("5","info"),
    ;

    private String code;
    private String type;

    ImsWarnTypeEnum(String code, String type) {
        this.code = code;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static String getCodeByType(String type){
        return Arrays.stream(ImsWarnTypeEnum.values()).filter(e -> e.getType().equals(type.toLowerCase()))
                .map(ImsWarnTypeEnum::getCode)
                .findFirst()
                .orElse("type error!");
    }

}

package com.webank.wedatasphere.dss.datamap.constant;

public enum EnvTypeEnum {

    PROD("prod", "生产"),
    TEST("test", "测试"),
    OA("oa", "OA");

    EnvTypeEnum(String enName, String cnName) {
        this.cnName = cnName;
        this.enName = enName;
    }

    private String enName;
    private String cnName;

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public static String getCnName(String enName) {
        for (EnvTypeEnum value : EnvTypeEnum.values()) {
            if (value.getEnName().equals(enName)) {
                return value.getCnName();
            }
        }
        throw new RuntimeException("配置文件中环境信息配置不正确");
    }
}

package com.webank.wedatasphere.dss.datamap.constant;

public enum ClusterTypeEnum {

    DG_BDAP("HDP-DG-BDAP-MAIN", "BDAP生产集群（HDP-DG-BDAP-MAIN）"),
    NS_QDY("HDP-NS-QDY-MAIN", "企贷云生产集群（HDP-NS-QDY-MAIN）"),
    GZPC_ATHENA("HDP-GZPC-ATHENA-MAIN", "公有云生产集群（HDP-GZPC-ATHENA-MAIN）"),
    GZPC_BDAP_UAT("HDP-GZPC-BDAP-UAT", "BDAP测试环境（HDP-GZPC-BDAP-UAT）");

    private String enName;
    private String cnName;

    ClusterTypeEnum(String enName, String cnName) {
        this.enName = enName;
        this.cnName = cnName;
    }

    public String getEnName() {
        return enName;
    }

    public void setEnName(String enName) {
        this.enName = enName;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public static String getCnName(String enName) {
        for (ClusterTypeEnum value : ClusterTypeEnum.values()) {
            if (value.getEnName().equals(enName)) {
                return value.getCnName();
            }
        }
        throw new RuntimeException("配置文件中集群信息配置不正确");
    }
}

package com.webank.wedatasphere.dss.datamap.datamap;

public class DMSBdpTablePartitionInfo {

    //分区名称
    private String partName;

    //分区大小
    private String partSize;

    //分区文件数量
    private String partFileCount;

    //分区访问时间
    private String partAccessTime;

    //最近写入时间
    private String partCreateTime;

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getPartSize() {
        return partSize;
    }

    public void setPartSize(String partSize) {
        this.partSize = partSize;
    }

    public String getPartFileCount() {
        return partFileCount;
    }

    public void setPartFileCount(String partFileCount) {
        this.partFileCount = partFileCount;
    }

    public String getPartAccessTime() {
        return partAccessTime;
    }

    public void setPartAccessTime(String partAccessTime) {
        this.partAccessTime = partAccessTime;
    }

    public String getPartCreateTime() {
        return partCreateTime;
    }

    public void setPartCreateTime(String partCreateTime) {
        this.partCreateTime = partCreateTime;
    }
}

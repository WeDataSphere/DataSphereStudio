package com.webank.wedatasphere.dss.datamap.datamap;

import java.util.List;

public class DMSBdpTableInfo {
    //数据表容量
    private String tableStorageUsedBytes;

    //小文件数
    private String smallFileNum;

    //分区个数
    private String tablePartNum;

    //最近访问时间
    private String latestPartAccessTime;

    List<DMSBdpTablePartitionInfo> partitions;

    public String getTableStorageUsedBytes() {
        return tableStorageUsedBytes;
    }

    public void setTableStorageUsedBytes(String tableStorageUsedBytes) {
        this.tableStorageUsedBytes = tableStorageUsedBytes;
    }

    public String getSmallFileNum() {
        return smallFileNum;
    }

    public void setSmallFileNum(String smallFileNum) {
        this.smallFileNum = smallFileNum;
    }

    public String getTablePartNum() {
        return tablePartNum;
    }

    public void setTablePartNum(String tablePartNum) {
        this.tablePartNum = tablePartNum;
    }

    public String getLatestPartAccessTime() {
        return latestPartAccessTime;
    }

    public void setLatestPartAccessTime(String latestPartAccessTime) {
        this.latestPartAccessTime = latestPartAccessTime;
    }

    public List<DMSBdpTablePartitionInfo> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<DMSBdpTablePartitionInfo> partitions) {
        this.partitions = partitions;
    }
}

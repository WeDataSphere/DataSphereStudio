package com.webank.wedatasphere.dss.datamap.datamap;

/**
 * @author: jinyangrao on 2020/12/24
 */
public class DMSpaceInfoBean {
    private String urn;
    private String spaceName;
    private Integer spaceId;
    private String partitionNumber;
    private String replicaFactor;
    private String charset;
    private String collate_;
    private String vidType;
    private String comment;
    private String clusterCode;

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public String getPartitionNumber() {
        return partitionNumber;
    }

    public void setPartitionNumber(String partitionNumber) {
        this.partitionNumber = partitionNumber;
    }

    public String getReplicaFactor() {
        return replicaFactor;
    }

    public void setReplicaFactor(String replicaFactor) {
        this.replicaFactor = replicaFactor;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCollate_() {
        return collate_;
    }

    public void setCollate_(String collate_) {
        this.collate_ = collate_;
    }

    public String getVidType() {
        return vidType;
    }

    public void setVidType(String vidType) {
        this.vidType = vidType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Integer getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(Integer spaceId) {
        this.spaceId = spaceId;
    }

    public String getClusterCode() {
        return clusterCode;
    }

    public void setClusterCode(String clusterCode) {
        this.clusterCode = clusterCode;
    }
}

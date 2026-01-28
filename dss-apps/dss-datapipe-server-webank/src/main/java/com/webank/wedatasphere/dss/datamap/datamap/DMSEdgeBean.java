package com.webank.wedatasphere.dss.datamap.datamap;

/**
 * @author: jinyangrao on 2020/12/24
 */
public class DMSEdgeBean {

    private String clusterCode;
    private String urn;
    private String spaceName;
    private String edgeTypeName;
    private Long ttlDuration;
    private String ttlCol;
    private String comment;
    private String createEdge;

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

    public String getEdgeTypeName() {
        return edgeTypeName;
    }

    public void setEdgeTypeName(String tagName) {
        this.edgeTypeName = tagName;
    }

    public Long getTtlDuration() {
        return ttlDuration;
    }

    public void setTtlDuration(Long ttlDuration) {
        this.ttlDuration = ttlDuration;
    }

    public String getTtlCol() {
        return ttlCol;
    }

    public void setTtlCol(String ttlCol) {
        this.ttlCol = ttlCol;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getClusterCode() {
        return clusterCode;
    }

    public void setClusterCode(String clusterCode) {
        this.clusterCode = clusterCode;
    }

    public String getCreateEdge() {
        return createEdge;
    }

    public void setCreateEdge(String createEdge) {
        this.createEdge = createEdge;
    }
}

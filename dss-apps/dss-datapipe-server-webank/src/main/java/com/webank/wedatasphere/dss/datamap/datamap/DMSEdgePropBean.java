package com.webank.wedatasphere.dss.datamap.datamap;

/**
 * @author: jinyangrao on 2020/12/24
 */
public class DMSEdgePropBean {
    private String urn;
    private String spaceName;
    private String edgeTypeName;
    private String propName;
    private String dataType;
    private String defaultValue;
    private String comment;
    private String clusterCode;
    private Integer isIndex;
    private String isIndexStr;

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

    public void setEdgeTypeName(String edgeTypeName) {
        this.edgeTypeName = edgeTypeName;
    }

    public String getPropName() {
        return propName;
    }

    public void setPropName(String propName) {
        this.propName = propName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
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

    public Integer getIsIndex() {
        return isIndex;
    }

    public void setIsIndex(Integer isIndex) {
        this.isIndex = isIndex;
    }

    public String getIsIndexStr() {
        return isIndexStr;
    }

    public void setIsIndexStr(String isIndexStr) {
        this.isIndexStr = isIndexStr;
    }
}

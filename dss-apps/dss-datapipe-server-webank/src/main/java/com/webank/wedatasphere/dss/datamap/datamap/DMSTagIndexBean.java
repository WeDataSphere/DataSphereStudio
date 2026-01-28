package com.webank.wedatasphere.dss.datamap.datamap;

import java.util.List;

/**
 * @author: jinyangrao on 2020/12/24
 */
public class DMSTagIndexBean {
    private List<String> types;
    private String byTag;
    private String clusterCode;
    private String fieldStr;
    private List<String> fields;
    private String indexName;
    private String spaceName;
    private String urn;

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public String getByTag() {
        return byTag;
    }

    public void setByTag(String byTag) {
        this.byTag = byTag;
    }

    public String getClusterCode() {
        return clusterCode;
    }

    public void setClusterCode(String clusterCode) {
        this.clusterCode = clusterCode;
    }

    public String getFieldStr() {
        return fieldStr;
    }

    public void setFieldStr(String fieldStr) {
        this.fieldStr = fieldStr;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }
}

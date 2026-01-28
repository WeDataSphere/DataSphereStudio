package com.webank.wedatasphere.dss.datamap.domain;

/**
 * Author: xlinliu
 * Date: 2024/10/21
 */
public class ExplainCodeMetaRequest {
    private String nodeType;
    private String scriptContent;

    public String getScriptContent() {
        return scriptContent;
    }

    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
}

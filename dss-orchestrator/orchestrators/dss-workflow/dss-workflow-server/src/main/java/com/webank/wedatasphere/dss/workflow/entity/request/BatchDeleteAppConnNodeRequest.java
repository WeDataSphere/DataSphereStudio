package com.webank.wedatasphere.dss.workflow.entity.request;

import com.webank.wedatasphere.dss.common.label.LabelRouteVO;

import java.util.List;
import java.util.Map;

public class BatchDeleteAppConnNodeRequest {

    private List<Map<String,Object>> nodes;

    public List<Map<String, Object>> getNodes() {
        return nodes;
    }

    public void setNodes(List<Map<String, Object>> nodes) {
        this.nodes = nodes;
    }
}

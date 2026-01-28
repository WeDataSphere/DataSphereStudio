package com.webank.wedatasphere.dss.orchestrator.common.entity.response;

import java.io.Serializable;
import java.util.List;

/**
 * Author: xlinliu
 * Date: 2023/12/20
 */
public class ResponseAppAndSubFlowCompare  implements Serializable {
    private List<CompareWorkflowAndSubFlowResult> list;

    public List<CompareWorkflowAndSubFlowResult> getList() {
        return list;
    }

    public void setList(List<CompareWorkflowAndSubFlowResult> list) {
        this.list = list;
    }
}

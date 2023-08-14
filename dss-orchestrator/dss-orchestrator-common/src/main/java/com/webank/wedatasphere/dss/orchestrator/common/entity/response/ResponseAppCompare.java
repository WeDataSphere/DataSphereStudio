package com.webank.wedatasphere.dss.orchestrator.common.entity.response;


import java.io.Serializable;
import java.util.List;


public class ResponseAppCompare implements Serializable {

   private List<CompareWorkflowResult> list;

    public List<CompareWorkflowResult> getList() {
        return list;
    }

    public void setList(List<CompareWorkflowResult> list) {
        this.list = list;
    }
}

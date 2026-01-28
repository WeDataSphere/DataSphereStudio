package com.webank.wedatasphere.dss.workflow.core.ref.impl;

import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRefImpl;
import com.webank.wedatasphere.dss.workflow.core.ref.RefOrchestrationExecutionInfoResponseRef;

import java.util.List;
import java.util.Map;

/**
 * Created by enjoyyin on 2021/6/29.
 */
public class RefOrchestrationExecutionInfoResponseRefImpl extends ResponseRefImpl implements RefOrchestrationExecutionInfoResponseRef {

    private List<Execution> executions;

    public RefOrchestrationExecutionInfoResponseRefImpl(String responseBody, int status,
        String errorMsg, Map<String, Object> responseMap, List<Execution> executions) {
        super(responseBody, status, errorMsg, responseMap);
        this.executions = executions;
    }

    @Override
    public List<Execution> getExecutions() {
        return executions;
    }
}

package com.webank.wedatasphere.dss.workflow.core.operation;

import com.webank.wedatasphere.dss.standard.app.structure.optional.OptionalOperation;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;
import com.webank.wedatasphere.dss.workflow.core.ref.RefOrchestrationActiveFlagRequestRef;

/**
 * 禁用和启用工作流调度
 */
public interface RefOrchestrationActiveFlagOperation<R extends RefOrchestrationActiveFlagRequestRef<R>>
    extends OptionalOperation<R, ResponseRef> {

    String OPERATION_NAME = "setRefOrchestrationActiveFlag";

    @Override
    default String getOperationName() {
        return OPERATION_NAME;
    }
}

package com.webank.wedatasphere.dss.workflow.core.operation;

import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.ref.RefOrchestrationContentRequestRef;
import com.webank.wedatasphere.dss.standard.app.structure.optional.OptionalOperation;
import com.webank.wedatasphere.dss.workflow.core.ref.RefOrchestrationExecutionInfoResponseRef;

/**
 * Created by enjoyyin on 2021/6/29.
 */
public interface RefOrchestrationExecutionInfoOperation<R extends RefOrchestrationContentRequestRef<R>>
    extends OptionalOperation<R, RefOrchestrationExecutionInfoResponseRef> {

    String OPERATION_NAME = "fetchRefOrchestrationExecutions";

    @Override
    default String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    RefOrchestrationExecutionInfoResponseRef apply(R r);
}

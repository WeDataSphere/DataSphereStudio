package com.webank.wedatasphere.dss.workflow.core.operation;

import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.ref.RefOrchestrationContentRequestRef;
import com.webank.wedatasphere.dss.standard.app.structure.optional.OptionalOperation;
import com.webank.wedatasphere.dss.workflow.core.ref.RefOrchestrationScheduleInfoResponseRef;

/**
 * Created by enjoyyin on 2021/6/29.
 */
public interface RefOrchestrationScheduleInfoOperation<R extends RefOrchestrationContentRequestRef<R>>
    extends OptionalOperation<R, RefOrchestrationScheduleInfoResponseRef> {

    String OPERATION_NAME = "fetchRefOrchestrationScheduleInfo";

    @Override
    default String getOperationName() {
        return OPERATION_NAME;
    }

    @Override
    RefOrchestrationScheduleInfoResponseRef apply(R r);
}

package com.webank.wedatasphere.dss.workflow.core.operation;

import com.webank.wedatasphere.dss.appconn.scheduler.structure.orchestration.ref.RefOrchestrationContentRequestRef;
import com.webank.wedatasphere.dss.standard.app.structure.optional.OptionalOperation;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;


/**
 * Created by enjoyyin on 2021/6/29.
 */
public interface RefOrchestrationUncronOperation<R extends RefOrchestrationContentRequestRef<R>>
    extends OptionalOperation<R, ResponseRef> {

    String OPERATION_NAME = "unsetRefOrchestrationCron";

    @Override
    default String getOperationName() {
        return OPERATION_NAME;
    }

}

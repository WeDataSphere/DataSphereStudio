package com.webank.wedatasphere.dss.workflow.core.operation;

import com.webank.wedatasphere.dss.standard.app.structure.optional.OptionalOperation;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;
import com.webank.wedatasphere.dss.workflow.core.ref.RefOrchestrationCronRequestRef;

/**
 * 设置调度
 * Created by enjoyyin on 2021/6/29.
 */
public interface RefOrchestrationCronOperation<R extends RefOrchestrationCronRequestRef<R>> extends OptionalOperation<R, ResponseRef> {

    String OPERATION_NAME = "setRefOrchestrationCron";

    @Override
    default String getOperationName() {
        return OPERATION_NAME;
    }
}

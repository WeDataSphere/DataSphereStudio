package com.webank.wedatasphere.dss.workflow.core.operation;

import com.webank.wedatasphere.dss.standard.app.structure.StructureRequestRefImpl;
import com.webank.wedatasphere.dss.standard.app.structure.optional.OptionalOperation;
import com.webank.wedatasphere.dss.workflow.core.operation.RefProxyUserFetchOperation.RefProxyUserFetchRequestRefImpl;
import com.webank.wedatasphere.dss.workflow.core.ref.RefProxyUserFetchResponseRef;

/**
 * Created by enjoyyin on 2022/9/8.
 */
public interface RefProxyUserFetchOperation extends OptionalOperation<RefProxyUserFetchRequestRefImpl, RefProxyUserFetchResponseRef> {

    String OPERATION_NAME = "fetchProxyUser";

    @Override
    default String getOperationName() {
        return OPERATION_NAME;
    }

    class RefProxyUserFetchRequestRefImpl extends StructureRequestRefImpl<RefProxyUserFetchRequestRefImpl> {}

}

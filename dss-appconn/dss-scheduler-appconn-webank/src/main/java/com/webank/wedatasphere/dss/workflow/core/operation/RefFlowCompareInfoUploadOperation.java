package com.webank.wedatasphere.dss.workflow.core.operation;

import com.webank.wedatasphere.dss.standard.app.structure.StructureRequestRefImpl;
import com.webank.wedatasphere.dss.standard.app.structure.optional.OptionalOperation;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;
import com.webank.wedatasphere.dss.workflow.core.operation.RefFlowCompareInfoUploadOperation.RefFlowCompareInfoUploadRequestRefImpl;

/**
 * Created by xlinliu on 2023/12/21.
 */
public interface RefFlowCompareInfoUploadOperation extends OptionalOperation<RefFlowCompareInfoUploadRequestRefImpl, ResponseRef> {

    String OPERATION_NAME = "uploadFlowCompareInfo";

    @Override
    default String getOperationName() {
        return OPERATION_NAME;
    }



    class RefFlowCompareInfoUploadRequestRefImpl extends StructureRequestRefImpl<RefFlowCompareInfoUploadRequestRefImpl> {
        private Long refProjectId;
        private Object flowModifyInfos;

        public Long getRefProjectId() {
            return refProjectId;
        }

        public void setRefProjectId(Long refProjectId) {
            this.refProjectId = refProjectId;
        }

        public Object getFlowModifyInfos() {
            return flowModifyInfos;
        }

        public void setFlowModifyInfos(Object flowModifyInfos) {
            this.flowModifyInfos = flowModifyInfos;
        }
    }

}

package com.webank.wedatasphere.dss.workflow.core.ref;


import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRefBuilder.ExternalResponseRefBuilder;
import com.webank.wedatasphere.dss.workflow.core.ref.impl.RefOrchestrationScheduleInfoResponseRefImpl;

/**
 * Created by enjoyyin on 2021/6/29.
 */
public interface RefOrchestrationScheduleInfoResponseRef extends ResponseRef {

    String getScheduleId();

    static Builder newBuild() {
        return new Builder();
    }

    class Builder extends ExternalResponseRefBuilder<Builder, RefOrchestrationScheduleInfoResponseRef> {

        private String schedulerId;

        public Builder setSchedulerId(String schedulerId) {
            this.schedulerId = schedulerId;
            return this;
        }

        @Override
        protected RefOrchestrationScheduleInfoResponseRef createResponseRef() {
            return new RefOrchestrationScheduleInfoResponseRefImpl(responseBody, status, errorMsg, responseMap, schedulerId);
        }
    }

}

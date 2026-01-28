package com.webank.wedatasphere.dss.workflow.core.ref;

import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRefBuilder.ExternalResponseRefBuilder;
import com.webank.wedatasphere.dss.workflow.core.ref.impl.RefOrchestrationExecutionInfoResponseRefImpl;
import java.util.List;

/**
 * Created by enjoyyin on 2021/6/29.
 */
public interface RefOrchestrationExecutionInfoResponseRef extends ResponseRef {

    List<Execution> getExecutions();

    interface Execution {

        long getSubmitTime();

        long getEndTime();

        String getStatus();

        String getSubmitUser();

        String getComment();

        static ExecutionBuilder newBuilder() {
            return new ExecutionBuilder();
        }

        class ExecutionBuilder {

            private long submitTime;
            private long endTime;
            private String status;
            private String submitUser;
            private String comment;

            public ExecutionBuilder setExecution(Execution execution) {
                this.submitTime = execution.getSubmitTime();
                this.endTime = execution.getEndTime();
                this.status = execution.getStatus();
                this.submitUser = execution.getSubmitUser();
                this.comment = execution.getComment();
                return this;
            }

            public ExecutionBuilder setSubmitTime(long submitTime) {
                this.submitTime = submitTime;
                return this;
            }

            public ExecutionBuilder setEndTime(long endTime) {
                this.endTime = endTime;
                return this;
            }

            public ExecutionBuilder setStatus(String status) {
                this.status = status;
                return this;
            }

            public ExecutionBuilder setSubmitUser(String submitUser) {
                this.submitUser = submitUser;
                return this;
            }

            public ExecutionBuilder setComment(String comment) {
                this.comment = comment;
                return this;
            }

            public Execution build() {
                return new Execution() {
                    @Override
                    public long getSubmitTime() {
                        return submitTime;
                    }

                    @Override
                    public long getEndTime() {
                        return endTime;
                    }

                    @Override
                    public String getStatus() {
                        return status;
                    }

                    @Override
                    public String getSubmitUser() {
                        return submitUser;
                    }

                    @Override
                    public String getComment() {
                        return comment;
                    }
                };
            }
        }

    }

    static Builder newBuilder() {
        return new Builder();
    }

    class Builder extends ExternalResponseRefBuilder<Builder, RefOrchestrationExecutionInfoResponseRef> {

        private List<Execution> executions;

        public Builder setExecutions(List<Execution> executions) {
            this.executions = executions;
            return this;
        }

        @Override
        protected RefOrchestrationExecutionInfoResponseRef createResponseRef() {
            return new RefOrchestrationExecutionInfoResponseRefImpl(responseBody, status, errorMsg, responseMap, executions);
        }
    }

}

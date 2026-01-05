package com.webank.wedatasphere.dss.workflow.entity.request;

import java.util.List;

public class ListNodeTypeRequest {

    public static class BatchOrchestrator {


        private Long projectId;

        private Long orchestratorId;

        public BatchOrchestrator() {
        }

        public BatchOrchestrator(Long projectId, Long orchestratorId) {
            this.projectId = projectId;
            this.orchestratorId = orchestratorId;
        }

        public Long getProjectId() {
            return projectId;
        }

        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }

        public Long getOrchestratorId() {
            return orchestratorId;
        }

        public void setOrchestratorId(Long orchestratorId) {
            this.orchestratorId = orchestratorId;
        }


        @Override
        public String toString() {
            return "{" +
                    "projectId=" + projectId +
                    ", orchestratorId=" + orchestratorId +
                    '}';
        }
    }


    private List<BatchOrchestrator> batchOrchestratorInfo;

    public ListNodeTypeRequest() {
    }

    public ListNodeTypeRequest(List<BatchOrchestrator> batchOrchestratorInfo) {
        this.batchOrchestratorInfo = batchOrchestratorInfo;
    }

    public List<BatchOrchestrator> getBatchOrchestratorInfo() {
        return batchOrchestratorInfo;
    }

    public void setBatchOrchestratorInfo(List<BatchOrchestrator> batchOrchestratorInfo) {
        this.batchOrchestratorInfo = batchOrchestratorInfo;
    }

    @Override
    public String toString() {
        return "ListNodeTypeRequest{" +
                "batchOrchestratorInfo=" + batchOrchestratorInfo +
                '}';
    }
}




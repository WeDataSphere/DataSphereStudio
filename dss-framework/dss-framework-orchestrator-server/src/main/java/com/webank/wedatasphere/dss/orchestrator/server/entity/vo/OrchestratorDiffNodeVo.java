package com.webank.wedatasphere.dss.orchestrator.server.entity.vo;

import java.util.List;

public class OrchestratorDiffNodeVo {

    private String orchestratorName;

    private Long orchestratorId;

    private String projectName;

    private Long projectId;

    private List<String> notContainsKeywordsNodeList;


    public OrchestratorDiffNodeVo() {
    }


    public OrchestratorDiffNodeVo(String orchestratorName, Long orchestratorId, List<String> notContainsKeywordsNodeList) {
        this.orchestratorName = orchestratorName;
        this.orchestratorId = orchestratorId;
        this.notContainsKeywordsNodeList = notContainsKeywordsNodeList;
    }

    public String getOrchestratorName() {
        return orchestratorName;
    }

    public void setOrchestratorName(String orchestratorName) {

        this.orchestratorName = orchestratorName;
    }


    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public List<String> getNotContainsKeywordsNodeList() {
        return notContainsKeywordsNodeList;
    }

    public void setNotContainsKeywordsNodeList(List<String> notContainsKeywordsNodeList) {
        this.notContainsKeywordsNodeList = notContainsKeywordsNodeList;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}

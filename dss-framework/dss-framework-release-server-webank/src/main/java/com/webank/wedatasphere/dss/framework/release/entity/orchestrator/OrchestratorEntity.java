package com.webank.wedatasphere.dss.framework.release.entity.orchestrator;


import com.webank.wedatasphere.dss.framework.release.entity.project.ProjectInfo;
import com.webank.wedatasphere.dss.framework.release.entity.task.ReleaseTask;

public class OrchestratorEntity {
    private Long orchestratorId;
    private Long orchestratorVersionId;
    private String orchestratorName;
    private ReleaseTask releaseTask;
    private ProjectInfo projectInfo;

    public OrchestratorEntity(Long orchestratorId, Long orchestratorVersionId, String orchestratorName) {
        this.orchestratorId = orchestratorId;
        this.orchestratorVersionId = orchestratorVersionId;
        this.orchestratorName = orchestratorName;
    }

    public OrchestratorEntity() {
    }

    public OrchestratorEntity(Long orchestratorId, Long orchestratorVersionId, String orchestratorName, ReleaseTask releaseTask, ProjectInfo projectInfo) {
        this.orchestratorId = orchestratorId;
        this.orchestratorVersionId = orchestratorVersionId;
        this.orchestratorName = orchestratorName;
        this.releaseTask = releaseTask;
        this.projectInfo = projectInfo;
    }

    public Long getOrchestratorId() {
        return orchestratorId;
    }

    public void setOrchestratorId(Long orchestratorId) {
        this.orchestratorId = orchestratorId;
    }

    public Long getOrchestratorVersionId() {
        return orchestratorVersionId;
    }

    public void setOrchestratorVersionId(Long orchestratorVersionId) {
        this.orchestratorVersionId = orchestratorVersionId;
    }

    public String getOrchestratorName() {
        return orchestratorName;
    }

    public void setOrchestratorName(String orchestratorName) {
        this.orchestratorName = orchestratorName;
    }

    public ReleaseTask getReleaseTask() {
        return releaseTask;
    }

    public void setReleaseTask(ReleaseTask releaseTask) {
        this.releaseTask = releaseTask;
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }

    public void setProjectInfo(ProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
    }
}

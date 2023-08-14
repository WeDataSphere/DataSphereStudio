package com.webank.wedatasphere.dss.framework.project.entity.vo;

import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectDO;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorInfo;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;

import java.io.Serializable;

public class OrchestratorCopyVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private DSSProjectDO projectDO;
    private Long copyProjectId;
    private String copyProjectName;
    private DSSOrchestratorInfo orchestrator;
    private DSSLabel dssLabel;
    private Workspace workspace;
    private Integer currentNum;
    private Integer sumCount;
    private String workspaceName;
    private Long copyTaskId;

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public DSSProjectDO getProjectDO() {
        return projectDO;
    }

    public void setProjectDO(DSSProjectDO projectDO) {
        this.projectDO = projectDO;
    }

    public Long getCopyProjectId() {
        return copyProjectId;
    }

    public void setCopyProjectId(Long copyProjectId) {
        this.copyProjectId = copyProjectId;
    }

    public String getCopyProjectName() {
        return copyProjectName;
    }

    public void setCopyProjectName(String copyProjectName) {
        this.copyProjectName = copyProjectName;
    }

    public DSSOrchestratorInfo getOrchestrator() {
        return orchestrator;
    }

    public void setOrchestrator(DSSOrchestratorInfo orchestrator) {
        this.orchestrator = orchestrator;
    }

    public DSSLabel getDssLabel() {
        return dssLabel;
    }

    public void setDssLabel(DSSLabel dssLabel) {
        this.dssLabel = dssLabel;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public Integer getCurrentNum() {
        return currentNum;
    }

    public void setCurrentNum(Integer currentNum) {
        this.currentNum = currentNum;
    }

    public Integer getSumCount() {
        return sumCount;
    }

    public void setSumCount(Integer sumCount) {
        this.sumCount = sumCount;
    }

    public Long getCopyTaskId() {
        return copyTaskId;
    }

    public void setCopyTaskId(Long copyTaskId) {
        this.copyTaskId = copyTaskId;
    }
}

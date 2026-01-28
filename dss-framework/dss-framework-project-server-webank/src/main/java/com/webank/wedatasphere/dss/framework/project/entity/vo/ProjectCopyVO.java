package com.webank.wedatasphere.dss.framework.project.entity.vo;

import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectDO;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorInfo;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;

import java.io.Serializable;
import java.util.List;

public class ProjectCopyVO implements Serializable{
    private static final long serialVersionUID = 1L;

    private String username;
    private DSSProjectDO projectDO;
    private Long copyProjectId;
    private String copyProjectName;
    private List<DSSOrchestratorInfo> orchestratorList;
    private DSSLabel dssLabel;
    private Workspace targetWorkspace;
    private Workspace sourceWorkspace;
    private String workspaceName;
    private String instanceName;

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

    public List<DSSOrchestratorInfo> getOrchestratorList() {
        return orchestratorList;
    }

    public void setOrchestratorList(List<DSSOrchestratorInfo> orchestratorList) {
        this.orchestratorList = orchestratorList;
    }

    public DSSLabel getDssLabel() {
        return dssLabel;
    }

    public void setDssLabel(DSSLabel dssLabel) {
        this.dssLabel = dssLabel;
    }

    public Workspace getTargetWorkspace() {
        return targetWorkspace;
    }

    public void setTargetWorkspace(Workspace targetWorkspace) {
        this.targetWorkspace = targetWorkspace;
    }

    public Workspace getSourceWorkspace() {
        return sourceWorkspace;
    }

    public void setSourceWorkspace(Workspace sourceWorkspace) {
        this.sourceWorkspace = sourceWorkspace;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
}

package com.webank.wedatasphere.dss.orchestrator.common.entity;


import java.io.Serializable;
import java.util.List;

/**
 * 编排详情。
 * 它许多属性是把{@link DSSOrchestratorVersion} 和{@link DSSOrchestratorInfo}的属性打平后放一起。
 * 其他属性是和调度、更新相关的
 */
public class OrchestratorDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 编排名
     */
    private String orchestratorName;
    /**
     * 编排id
     */
    private Long orchestratorId;
    /**
     * 最新的版本id
     */
    private Long orchestratorVersionId;
    /**
     * 关联的app的id，比如workflowId
     */
    private Long appId;
    /**
     * 发起调度的时间
     */
    private String scheduleTime;
    /**
     * 最新的版本号
     */
    private String latestVersion;
    /**
     * 发布者
     */
    private String releaseUser;
    /**
     * 更新者
     */
    private String lastUpdater;
    /**
     * 更新时间（比如调度结束后）
     */
    private String lastUpdateTime;
//    发布时间，看上去和lastUpdateTime重复了
    private String publishTime;

    private String status;

    private String comment;

    private List<String> labels;

    private String scheduleSettings;

    private Integer privModel;
    private String projectName;
    private DSSReleasedFlowVO.ScheduleInfo scheduleInfo;
    private Long projectId;
    private DSSReleasedFlowVO.FlowPriv flowPriv;

    /**
     * 调度标示: true-启用,false-禁用
     */
    private Boolean activeFlag;
    /**
     * 创建人
     */
    private String creator;

    public OrchestratorDetail() {
    /*    this.projectName = "test20200126";
        DSSReleasedFlowVO.ScheduleInfo scheduleInfo = new DSSReleasedFlowVO.ScheduleInfo();
        scheduleInfo.setAlarmLevel("INFO");
        scheduleInfo.setAlarmUserEmails("jianfuzhang, cooperyang");
        scheduleInfo.setScheduleTime("0 /5 * * * ? *");
        this.scheduleInfo = scheduleInfo;
        this.privModel = 2;
        this.projectId = 10L;
        DSSReleasedFlowVO.FlowPriv flowPriv = new DSSReleasedFlowVO.FlowPriv();
        flowPriv.setPrivModel(2);
        flowPriv.setUsernames(new ArrayList<>(Arrays.asList("cooperyang", "jianfuzhang", "johnnwang")));
        this.flowPriv = flowPriv;*/
    }

    public Boolean getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(Boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
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

    public Long getOrchestratorVersionId() {
        return orchestratorVersionId;
    }

    public void setOrchestratorVersionId(Long orchestratorVersionId) {
        this.orchestratorVersionId = orchestratorVersionId;
    }

    public String getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(String scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getReleaseUser() {
        return releaseUser;
    }

    public void setReleaseUser(String releaseUser) {
        this.releaseUser = releaseUser;
    }

    public String getLastUpdater() {
        return lastUpdater;
    }

    public void setLastUpdater(String lastUpdater) {
        this.lastUpdater = lastUpdater;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String getScheduleSettings() {
        return scheduleSettings;
    }

    public void setScheduleSettings(String scheduleSettings) {
        this.scheduleSettings = scheduleSettings;
    }

    public Integer getPrivModel() {
        return privModel;
    }

    public void setPrivModel(Integer privModel) {
        this.privModel = privModel;
    }

    public DSSReleasedFlowVO.ScheduleInfo getScheduleInfo() {
        return scheduleInfo;
    }

    public void setScheduleInfo(DSSReleasedFlowVO.ScheduleInfo scheduleInfo) {
        this.scheduleInfo = scheduleInfo;
    }


    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public DSSReleasedFlowVO.FlowPriv getFlowPriv() {
        return flowPriv;
    }

    public void setFlowPriv(DSSReleasedFlowVO.FlowPriv flowPriv) {
        this.flowPriv = flowPriv;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(String publishTime) {
        this.publishTime = publishTime;
    }

    @Override
    public String toString() {
        return "OrchestratorDetail{" +
                "orchestratorName='" + orchestratorName + '\'' +
                ", orchestratorId=" + orchestratorId +
                ", orchestratorVersionId=" + orchestratorVersionId +
                ", appId=" + appId +
                ", scheduleTime='" + scheduleTime + '\'' +
                ", latestVersion='" + latestVersion + '\'' +
                ", releaseUser='" + releaseUser + '\'' +
                ", lastUpdater='" + lastUpdater + '\'' +
                ", lastUpdateTime='" + lastUpdateTime + '\'' +
                ", publishTime='" + publishTime + '\'' +
                ", status='" + status + '\'' +
                ", comment='" + comment + '\'' +
                ", labels=" + labels +
                ", scheduleSettings='" + scheduleSettings + '\'' +
                ", privModel=" + privModel +
                ", projectName='" + projectName + '\'' +
                ", scheduleInfo=" + scheduleInfo +
                ", projectId=" + projectId +
                ", flowPriv=" + flowPriv +
                ", activeFlag=" + activeFlag +
                ", creator='" + creator + '\'' +
                '}';
    }
}

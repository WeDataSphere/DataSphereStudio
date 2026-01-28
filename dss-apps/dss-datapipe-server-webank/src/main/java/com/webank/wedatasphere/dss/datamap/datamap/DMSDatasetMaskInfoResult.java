package com.webank.wedatasphere.dss.datamap.datamap;

import java.util.List;

public class DMSDatasetMaskInfoResult {
    // 主键
    private Long id;

    // 源集群
    private String originCluster;

    // 源库
    private String originDb;

    // 源表 
    private String originTable;

    // 源表id
    private Long originTableId;

    // 目标集群
    private String targetCluster;

    // 目标库
    private String targetDb;

    // 目标表
    private String targetTable;

    // 目标表id
    private Long targetTableId;

    // DM单号
    private String dmRequestId;

    // BDAP表开始推数时间
    private String maskStartTime;

    // BDAP表近一个月的分区平均写入时间
    private String maskFinishTime;

    // BDP源表近一个月变更类型：1，表更新方式、2，表更新时效、3，表结构
    private String originTableChangeType;

    // 创建时间
    private String createDate;

    // 修改时间
    private String modifyDate;

    private MultiChanelInfo originTableComment;

    private MultiChanelInfo targetTableComment;

    private MultiChanelInfo originBusinessMeaning;

    private MultiChanelInfo targetBusinessMeaning;

    private MultiChanelInfo updateMethod;

    private MultiChanelInfo updateFrequency;

    // 表字典的业务属主部门
    private String deptName;

    private MultiChanelInfo proNames  = new MultiChanelInfo();;

    private String devmanager;

    private String devdept;

    private List<DMSDatasetSubsystem> subsystemList;

    private String subsystems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginCluster() {
        return originCluster;
    }

    public void setOriginCluster(String originCluster) {
        this.originCluster = originCluster;
    }

    public String getOriginDb() {
        return originDb;
    }

    public void setOriginDb(String originDb) {
        this.originDb = originDb;
    }

    public String getOriginTable() {
        return originTable;
    }

    public void setOriginTable(String originTable) {
        this.originTable = originTable;
    }

    public Long getOriginTableId() {
        return originTableId;
    }

    public void setOriginTableId(Long originTableId) {
        this.originTableId = originTableId;
    }

    public String getTargetCluster() {
        return targetCluster;
    }

    public void setTargetCluster(String targetCluster) {
        this.targetCluster = targetCluster;
    }

    public String getTargetDb() {
        return targetDb;
    }

    public void setTargetDb(String targetDb) {
        this.targetDb = targetDb;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public Long getTargetTableId() {
        return targetTableId;
    }

    public void setTargetTableId(Long targetTableId) {
        this.targetTableId = targetTableId;
    }

    public String getDmRequestId() {
        return dmRequestId;
    }

    public void setDmRequestId(String dmRequestId) {
        this.dmRequestId = dmRequestId;
    }

    public String getMaskStartTime() {
        return maskStartTime;
    }

    public void setMaskStartTime(String maskStartTime) {
        this.maskStartTime = maskStartTime;
    }

    public String getMaskFinishTime() {
        return maskFinishTime;
    }

    public void setMaskFinishTime(String maskFinishTime) {
        this.maskFinishTime = maskFinishTime;
    }

    public String getOriginTableChangeType() {
        return originTableChangeType;
    }

    public void setOriginTableChangeType(String originTableChangeType) {
        this.originTableChangeType = originTableChangeType;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(String modifyDate) {
        this.modifyDate = modifyDate;
    }

    public MultiChanelInfo getOriginTableComment() {
        return originTableComment;
    }

    public void setOriginTableComment(MultiChanelInfo originTableComment) {
        this.originTableComment = originTableComment;
    }

    public MultiChanelInfo getTargetTableComment() {
        return targetTableComment;
    }

    public void setTargetTableComment(MultiChanelInfo targetTableComment) {
        this.targetTableComment = targetTableComment;
    }

    public MultiChanelInfo getOriginBusinessMeaning() {
        return originBusinessMeaning;
    }

    public void setOriginBusinessMeaning(MultiChanelInfo originBusinessMeaning) {
        this.originBusinessMeaning = originBusinessMeaning;
    }

    public MultiChanelInfo getTargetBusinessMeaning() {
        return targetBusinessMeaning;
    }

    public void setTargetBusinessMeaning(MultiChanelInfo targetBusinessMeaning) {
        this.targetBusinessMeaning = targetBusinessMeaning;
    }

    public MultiChanelInfo getUpdateMethod() {
        return updateMethod;
    }

    public void setUpdateMethod(MultiChanelInfo updateMethod) {
        this.updateMethod = updateMethod;
    }

    public MultiChanelInfo getUpdateFrequency() {
        return updateFrequency;
    }

    public void setUpdateFrequency(MultiChanelInfo updateFrequency) {
        this.updateFrequency = updateFrequency;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public MultiChanelInfo getProNames() {
        return proNames;
    }

    public void setProNames(MultiChanelInfo proNames) {
        this.proNames = proNames;
    }

    public String getDevmanager() {
        return devmanager;
    }

    public void setDevmanager(String devmanager) {
        this.devmanager = devmanager;
    }

    public String getDevdept() {
        return devdept;
    }

    public void setDevdept(String devdept) {
        this.devdept = devdept;
    }

    public List<DMSDatasetSubsystem> getSubsystemList() {
        return subsystemList;
    }

    public void setSubsystemList(List<DMSDatasetSubsystem> subsystemList) {
        this.subsystemList = subsystemList;
    }

    public String getSubsystems() {
        return subsystems;
    }

    public void setSubsystems(String subsystems) {
        this.subsystems = subsystems;
    }
}

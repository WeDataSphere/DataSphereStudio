package com.webank.wedatasphere.dss.framework.project.entity;

import com.webank.wedatasphere.dss.framework.project.dao.entity.ProjectOperateRecordDO;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateRecordStatusEnum;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateTypeEnum;
import org.apache.linkis.rpc.Sender;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateRecordStatusEnum.*;

/**
 * 项目操作记录实体类
 */
public class ProjectOperateRecordBO {
    /**
     * 项目Id
     */
    private Long projectId;
    /**
     * 空间id
     */
    private Long workspaceId;

    /**
     * 操作记录唯一Id
     */
    private String recordId;

    /**
     * 操作类型
     */
    private ProjectOperateTypeEnum operateType;
    /**
     * 操作状态
     */
    private ProjectOperateRecordStatusEnum status;

    /**
     * 操作内容说明
     */
    private String content;
    /**
     * 校验码，只有导出成功的操作，才有校验码
     */
    private String checkCode;
    /**
     * 是否可以下载。 true说明有资源可以下载，false说明没有。
     * 如果有资源可以下载，前端展示下载按钮。
     */
    private boolean canDownload;

    private String creator;
    private Date createTime;

    private ProjectOperateRecordBO() {
    }

    private ProjectOperateRecordBO(Long workspaceId, Long projectId, String recordId,  ProjectOperateTypeEnum operateType,
                                  ProjectOperateRecordStatusEnum status, String content,
                                  boolean canDownload, String creator, Date createTime) {
        this.projectId=projectId;
        this.workspaceId=workspaceId;
        this.recordId = recordId;
        this.operateType = operateType;
        this.status = status;
        this.content = content;
        this.canDownload = canDownload;
        this.creator = creator;
        this.createTime = createTime;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public ProjectOperateTypeEnum getOperateType() {
        return operateType;
    }

    public void setOperateType(ProjectOperateTypeEnum operateType) {
        this.operateType = operateType;
    }

    public ProjectOperateRecordStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ProjectOperateRecordStatusEnum status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public boolean isCanDownload() {
        return canDownload;
    }

    public void setCanDownload(boolean canDownload) {
        this.canDownload = canDownload;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }


    /**
     * 改变状态
     */
    public ProjectOperateRecordBO running(){
        setStatus(RUNNING);
        return this;
    }
    public ProjectOperateRecordBO success(){
        setStatus(SUCCESS);
        return this;
    }
    public ProjectOperateRecordBO failed(){
        setStatus(FAILED);
        return this;
    }

    /**
     * 从Do持久化对象活化过来
     */
    public static ProjectOperateRecordBO fromDO(ProjectOperateRecordDO recordDO){
        ProjectOperateRecordBO recordBO = new ProjectOperateRecordBO();
        BeanUtils.copyProperties(recordDO,recordBO);
        recordBO.setStatus(ProjectOperateRecordStatusEnum.getByCode(recordDO.getStatus()));
        recordBO.setOperateType(ProjectOperateTypeEnum.getByCode(recordDO.getOperateType()));
        return recordBO;
    }

    /**
     * 转为DO持久化对象
     */
    public  ProjectOperateRecordDO toDO(){
        ProjectOperateRecordBO recordVO=this;
        ProjectOperateRecordDO recordDO=new ProjectOperateRecordDO();
        BeanUtils.copyProperties(recordVO,recordDO);
        Optional.ofNullable(recordVO.getStatus()).map(ProjectOperateRecordStatusEnum::getCode).ifPresent(
                recordDO::setStatus
        );

        Optional.ofNullable(recordVO.getOperateType()).map(ProjectOperateTypeEnum::getCode).ifPresent(
                recordDO::setOperateType
        );
        recordDO.setInstanceName(Sender.getThisInstance());
        return recordDO;
    }

    /**
     * 初始化一个全新的记录实体
     * @param projectId 项目id
     * @param operateType 操作类型
     * @param content 内容
     * @param creator 创建人
     * @return 一个全新的记录实体
     */
    public static ProjectOperateRecordBO of(Long workspaceId, Long projectId, ProjectOperateTypeEnum operateType,
                                            String content, String creator){
        String recordId= UUID.randomUUID().toString();
        ProjectOperateRecordStatusEnum status=INIT;
        Date now=new Date();
       return new ProjectOperateRecordBO(workspaceId,projectId,recordId,operateType,status,content,false,creator,now);
    }
}

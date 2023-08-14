package com.webank.wedatasphere.dss.framework.project.entity.vo;

import com.webank.wedatasphere.dss.framework.project.entity.ProjectOperateRecordBO;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateRecordStatusEnum;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateTypeEnum;
import org.springframework.beans.BeanUtils;

import java.util.Date;

/**
 * 项目操作记录显示类
 * Author: xlinliu
 * Date: 2022/9/7
 */
public class ProjectOperateRecordVO {

    /**
     * 操作记录唯一Id
     */
    private String recordId;

    /**
     * 操作类型
     */
    private ProjectOperateTypeEnum.VO operateType;
    /**
     * 操作状态
     */
    private ProjectOperateRecordStatusEnum.VO status;

    /**
     * 操作内容说明
     */
    private String content;
    /**
     * 是否可以下载。 true说明有资源可以下载，false说明没有。
     * 如果有资源可以下载，前端展示下载按钮。
     */
    private boolean canDownload;

    private String creator;
    private Date createTime;
    private String checkCode;

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public ProjectOperateTypeEnum.VO getOperateType() {
        return operateType;
    }

    public void setOperateType(ProjectOperateTypeEnum.VO operateType) {
        this.operateType = operateType;
    }

    public ProjectOperateRecordStatusEnum.VO getStatus() {
        return status;
    }

    public void setStatus(ProjectOperateRecordStatusEnum.VO status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public static ProjectOperateRecordVO fromBO(ProjectOperateRecordBO recordBO){
        ProjectOperateRecordVO vo = new ProjectOperateRecordVO();
        BeanUtils.copyProperties(recordBO,vo);

        vo.setStatus(recordBO.getStatus().toVO());
        vo.setOperateType(recordBO.getOperateType().toVO());
        return vo;
    }
}

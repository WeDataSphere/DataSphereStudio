package com.webank.wedatasphere.dss.framework.project.service;

import com.webank.wedatasphere.dss.common.entity.BmlResource;
import com.webank.wedatasphere.dss.common.entity.PageInfo;
import com.webank.wedatasphere.dss.framework.project.entity.ProjectOperateRecordBO;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateRecordStatusEnum;
import com.webank.wedatasphere.dss.framework.project.enums.ProjectOperateTypeEnum;

import javax.annotation.Nullable;

/**
 * 项目操作相关服务
 *  Author: xlinliu
 *  Date: 2022/9/8
 */
public interface DSSProjectOperateService {
    /**
     * 添加一条操作记录
     * @param recordVO 要添加的操作记录
     */
    void addOneRecord(ProjectOperateRecordBO recordVO);
    /**
     * 通过recordId获取记录所在项目名
     * @param recordId
     * @return
     */
    String getRecordProjectName(String recordId);

    /**
     * 更新任务的内容和状态
     * @param recordId 要更新的任务id
     * @param statusEnum 新的任务状态
     * @param content 新的任务详情描述。为空则不更新此属性
     */
    void updateRecord(String recordId,
                      ProjectOperateRecordStatusEnum statusEnum,
                      @Nullable String content);

    /**
     * 给记录附带上导出结果
     * @param recordId 记录id
     * @param resource 导出结果
     */
    void attachExportResource(String recordId,BmlResource resource);

    /**
     * 查询项目操作记录
     * @param projectId 要查询的项目id
     * @param pageSize 分页大小
     * @param currentPage 分页号，从0开始计数
     * @param recordId 精确根据记录id查找
     * @param statusFilter 状态过滤器
     * @param operateTypeFilter 操作方式过滤器
     * @return 符合条件的操作记录
     */
    PageInfo<ProjectOperateRecordBO> queryRecords(Long projectId, int pageSize, int currentPage,
                                                  @Nullable
                                                String recordId,
                                                  @Nullable
                                              ProjectOperateRecordStatusEnum statusFilter,
                                                  @Nullable
                                              ProjectOperateTypeEnum operateTypeFilter);

    /**
     * 根据recordId获取下载结果
     * @exception com.webank.wedatasphere.dss.common.exception.DSSRuntimeException 获取的记录不存在，或者记录没有导出结果。
     */
    BmlResource getExportResultByRecordId(String recordId);

    /**
     * 查询工作空间下正在运行的特定任务总数
     * @param operateTypeEnum 任务类型
     * @param workspaceId 工作空间Id
     */
    int queryRunningTotal(ProjectOperateTypeEnum operateTypeEnum,Long workspaceId);

}

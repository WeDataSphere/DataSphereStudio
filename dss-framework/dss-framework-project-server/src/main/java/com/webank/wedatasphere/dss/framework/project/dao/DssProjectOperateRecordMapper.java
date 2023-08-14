package com.webank.wedatasphere.dss.framework.project.dao;

import com.webank.wedatasphere.dss.framework.project.dao.entity.ProjectOperateRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author arionliu
* @description 针对表【dss_project_operate_record(项目操作记录表)】的数据库操作Mapper
* @createDate 2022-09-06 10:34:23
* @Entity com.webank.wedatasphere.dss.framework.project.dao.entity.ProjectOperateRecordDO
*/
@Mapper
public interface DssProjectOperateRecordMapper {
    /**
     * 获取某个项目的操作记录
     * @param projectId 项目id，必填
     * @param limit 分页参数
     * @param offset 分页参数
     * @param operateType 操作类型过滤条件，可选，为空则不过滤
     * @param status 操作状态过滤条件，可选，为空则不过滤
     * @return 满足条件的操作记录
     */
    List<ProjectOperateRecordDO> getRecords(@Param("projectId")Long projectId,
                                            @Param("limit")int limit,
                                            @Param("offset")int offset,
                                            @Param("operateType") Integer operateType,
                                            @Param("status")Integer status);
    Integer getCount(@Param("projectId")Long projectId,
                 @Param("operateType") Integer operateType,
                 @Param("status")Integer status);

    /**
     * 查询工作空间下满足条件的任务数
     */
    Integer getCountByWorkspace(@Param("workspaceId")Long workspaceId,
                     @Param("operateType") Integer operateType,
                     @Param("status")Integer status);


    void insert(ProjectOperateRecordDO operateRecordDO);
    void updateByRecordId(ProjectOperateRecordDO operateRecordDO);
    ProjectOperateRecordDO getByRecordId(@Param("recordId")String recordId);

    /**
     * 通过recordId获取记录所在项目名
     * @param recordId
     * @return
     */
    String getRecordProjectName(@Param("recordId")String recordId);

    List<ProjectOperateRecordDO> getRecordByStatus(@Param("statuses") List<Integer> statuses, @Param("operateTypes") List<Integer> operateTypes);

    void batchUpdateRecords(@Param("operateRecordDOS") List<ProjectOperateRecordDO> operateRecordDOS);
}





package com.webank.wedatasphere.dss.framework.workspace.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工作空间内队列查询器
 * Author: xlinliu
 * Date: 2023/4/17
 */
@Mapper
public interface DSSWorkspaceQueueMapper extends BaseMapper<String> {
    @Select("SELECT queue FROM dss_queue_in_workspace WHERE workspace_id=#{workspaceId}")
    List<String> getQueueList(@Param("workspaceId") Long workspaceId);
}

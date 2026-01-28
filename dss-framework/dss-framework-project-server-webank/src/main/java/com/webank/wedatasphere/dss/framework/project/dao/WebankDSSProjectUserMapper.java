package com.webank.wedatasphere.dss.framework.project.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: v_wbzwchen on 2021/11/19
 * @description:
 */

@Mapper
public interface WebankDSSProjectUserMapper {

    @Select("SELECT username FROM dss_project_user WHERE priv = 3 and project_id = #{projectId}")
    List<String> selectUserByProjectId(@Param("projectId")Long projectId);

}

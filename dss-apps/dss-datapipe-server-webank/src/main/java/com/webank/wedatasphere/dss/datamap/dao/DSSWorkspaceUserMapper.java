package com.webank.wedatasphere.dss.datamap.dao;

import org.apache.ibatis.annotations.*;


import java.util.List;

@Mapper
public interface DSSWorkspaceUserMapper {

    @Select("select role_id from dss_workspace_user_role where workspace_id = #{workspaceId} and username = #{username}")
    List<Integer> getRoleInWorkspace(@Param("workspaceId") int workspaceId, @Param("username") String username);

}

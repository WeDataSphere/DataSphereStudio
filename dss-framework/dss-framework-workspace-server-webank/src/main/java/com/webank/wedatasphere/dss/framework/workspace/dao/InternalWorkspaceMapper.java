package com.webank.wedatasphere.dss.framework.workspace.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InternalWorkspaceMapper {

    @Select("select count(1) from dss_workspace_user_favorites_appconn where menu_appconn_id=#{menuAppId} and workspace_id=#{workspaceId}" +
            " and username=#{userName} and type=#{type}")
    int getByMenuAppIdAndUser(@Param("menuAppId") Long menuAppId, @Param("workspaceId") Long workspaceId,
                              @Param("userName") String userName, @Param("type") String type);
}

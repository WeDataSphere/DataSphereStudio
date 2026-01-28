package com.webank.wedatasphere.dss.framework.project.dao;

import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectQueryRequest;
import com.webank.wedatasphere.dss.framework.project.entity.vo.QueryProjectVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface WebankDSSProjectMapper {

    Long getProjectWorkspaceIdById(@Param("projectId") Long projectId);
    /**
     * 获取已删除的工程
     *
     */
    List<QueryProjectVo> getDeletedProjects(ProjectQueryRequest projectRequest);

    /**
     * @Author: bradyli
     * @Date: 2021/10/28
     * @Description: 查询用户
     * @Param:
     * @return:
     **/
    List<String> getProjectUserNames(@Param("workspaceId") Long workspaceId, @Param("projectId") Long projectId, @Param("priv") Integer priv);

    /**
     * @Author: bradyli
     * @Date: 2021/10/28
     * @Description:
     * @Param: HashMap<projectName, projectId>
     * @return:
     **/
    List<HashMap<String, Object>> getProjectInfoByWorkspaceId(@Param("workspaceId") Integer workspaceId);

    /**
     * @Author: bradyli
     * @Date: 2021/10/27
     * @Description: 统计指定用户和空间下的项目数
     * @Param:
     * @return:
     **/
    Long countWorkspaceIdOfUser(@Param("workspaceId") Integer workspaceId, @Param("transferor") String transferor);

    @Select("select w.`name` from dss_workspace w join dss_project p on w.`id` = p.`workspace_id` and p.id = #{projectId}")
    String getWorkspaceName(@Param("projectId") Long projectId);

    @Select("SELECT a.url FROM dss_appconn_instance a LEFT JOIN dss_appconn b ON a.appconn_id = b.id WHERE LOWER(b.appconn_name) =LOWER(#{schedulisName}) LIMIT 1")
    String getSchedualisUrl(@Param("schedulisName")String schedulisName);

    @Select("SELECT a.appconn_instance_project_id FROM dss_appconn_project_relation a " +
            "WHERE a.project_id = #{projectId} " +
            "and a.appconn_instance_id = (SELECT a.id FROM dss_appconn_instance a LEFT JOIN dss_appconn b ON a.appconn_id = b.id WHERE b.appconn_name = #{schedulisName} limit 1) " +
            "limit 1")
    Long getSchedulisProjectId(@Param("schedulisName") String schedulisName,@Param("projectId")Long projectId);
}

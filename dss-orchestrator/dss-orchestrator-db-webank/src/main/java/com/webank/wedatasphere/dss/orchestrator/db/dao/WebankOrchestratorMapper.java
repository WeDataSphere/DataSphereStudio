/*
 *
 *  * Copyright 2019 WeBank
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.orchestrator.db.dao;

import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSReleasedFlowVO;
import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorDetail;
import com.webank.wedatasphere.dss.orchestrator.common.entity.DSSOrchestratorVersion;
import com.webank.wedatasphere.dss.orchestrator.common.entity.OrchestratorUser;
import com.webank.wedatasphere.dss.orchestrator.common.entity.ReleaseInfoVO;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestPublishHistory;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.ReleaseInfoRequest;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface WebankOrchestratorMapper {
    /**
     * 根据id查找最新的版本信息
     *
     * @param orchestratorId
     * @return
     */
    DSSOrchestratorVersion getLatestOrchestratorVersionById(Long orchestratorId);

    /**
     * 根据id查找所有的版本信息
     *
     * @param orchestratorId
     * @return
     *
     */
    List<DSSOrchestratorVersion> getVersionByOrchestratorId(Long orchestratorId);

    /**
     * 查询项目下所有编排的最新版本。
     * @param projectId 项目id
     */
    List<OrchestratorDetail> getOrchestratorDetails(@Param("projectId") Long projectId);

    @Select("select `name` from dss_orchestrator_info where `id` = #{orchestratorId}")
    String getOrchestratorNameById(@Param("orchestratorId") int orchestratorId);

    @Delete("delete from `dss_orchestrator_schedule_info` where `orchestrator_id` = #{orchestratorId}")
    int deleteScheduleInfo(@Param("orchestratorId") Integer orchestratorId);

    @Update("update dss_orchestrator_schedule_info set active_flag = #{activeFlag}  where `orchestrator_id` = #{orchestratorId}")
    int updateScheduleInfoActiveFlag(@Param("orchestratorId") Long orchestratorId, @Param("activeFlag") String activeFlag);

    @Insert("insert into `dss_orchestrator_schedule_info`" +
            "(`orchestrator_id`, `project_name`, `schedule_user`, `schedule_time`, `alarm_user_emails`, `alarm_level`, `last_update_time`) " +
            "values(#{orchestratorId}, #{projectName}, #{scheduleUser}, #{scheduleTime}, #{alarmEmails}, #{alarmLevel}, now())")
    void setScheduleInfo(@Param("projectName") String projectName, @Param("scheduleUser") String scheduleUser,
                         @Param("scheduleTime") String scheduleTime, @Param("alarmEmails") String alarmEmails,
                         @Param("alarmLevel") String alarmLevel, @Param("orchestratorId") Integer orchestratorId);

    DSSReleasedFlowVO.ScheduleInfo getScheduleInfo(@Param("orchestratorId") Long orchestratorId);

    @Insert({
            "<script>",
            "insert into `dss_orchestrator_user`",
            "(`workspace_id`, `project_id`, `orchestrator_id`, `username`, `priv`, `last_update_time`)",
            "values",
            "<foreach collection='accessUsers' item='accessUser' open='(' separator='),(' close=')'>",
            " #{workspaceId}, #{projectId}, #{orchestratorId}, #{accessUser}, #{priv}, #{updateTime}",
            "</foreach>",
            "</script>"
    })
    void setOrchestratorPriv(@Param("workspaceId") int workspaceId,
                             @Param("projectId") Long projectId, @Param("orchestratorId") int orchestratorId,
                             @Param("accessUsers") List<String> accessUsers, @Param("priv") int priv, @Param("updateTime") Date date);

    @Delete("delete from `dss_orchestrator_user` " +
            "where `workspace_id` = #{workspaceId} " +
            "and `project_id` = #{projectId} " +
            "and `orchestrator_id` = #{orchestratorId}")
    void deleteAllOrchestratorPriv(@Param("workspaceId") int workspaceId, @Param("projectId") Long projectId, @Param("orchestratorId") int orchestratorId);


    List<OrchestratorUser> getOrchestratorUserByOrcId(@Param("orchestratorId") Long orchestratorId);

    //生產的编排Id
    @Select("select id from `dss_orchestrator_info` where `project_id` = #{projectId} and uuid = #{uuid} ")
    Long getOrcIsPublishFlag(@Param("projectId")Long projectId,@Param("uuid")String uuid);

    DSSOrchestratorVersion getAppIdByVersionId(@Param("id")Long id);

    DSSOrchestratorVersion getNextAppIdByVersionId(@Param("orchestratorId")Long orchestratorId,@Param("id")Long secondId);

    @Select("SELECT DISTINCT(updater) FROM dss_orchestrator_version_info WHERE orchestrator_id = #{id} AND valid_flag =  #{validFlag} and updater is not null")
    List<String> getOrchestratorVersionUserList(@Param("id")Long id,@Param("validFlag")int validFlag);

    List<DSSOrchestratorVersion> getOrchestratorVersionByParam(RequestPublishHistory requestPublishHistory);

    @Select("select id from `dss_orchestrator_info` where `project_id` = #{projectId} and uuid = #{uuid} and name = #{orchestratorName} limit 1")
    Long getOrcIdByUuid(@Param("projectId")Long projectId,@Param("orchestratorName")String orchestratorName,@Param("uuid")String uuid);

    /**
     * 查询批量编排的发布信息（最新发布成功版本），根据项目名称和编排名称列表查询
     * @param request 查询请求，包含项目名称和编排名称列表
     * @return 发布信息列表
     */
    List<ReleaseInfoVO> getReleaseInfoByNames(ReleaseInfoRequest request);
}

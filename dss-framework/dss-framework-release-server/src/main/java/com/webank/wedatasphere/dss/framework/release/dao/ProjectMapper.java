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

package com.webank.wedatasphere.dss.framework.release.dao;

import com.webank.wedatasphere.dss.framework.release.entity.project.ProjectInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * created by cooperyang on 2020/12/11
 * Description:用于查询工程和的orchestrator的基本内容信息
 */
@Mapper
public interface ProjectMapper {

    ProjectInfo getProjectInfoById(@Param("projectId")Long projectId);

    //todo release-server包移到orchestrator-server
    @Select("Select project_id from dss_orchestrator_info where id = #{orchestratorId}")
    Long getProjectIdByOrcId(@Param("orchestratorId") Long orchestratorId);

    @Select("select name from dss_orchestrator_info where id = #{orchestratorId}")
    String getOrchestratorName(@Param("orchestratorId") Long orchestratorId,
                               @Param("orchestratorVersionId") Long orchestratorVersionId);

    @Select("select w.`name` from dss_workspace w join dss_project p on w.`id` = p.`workspace_id` and p.id = #{projectId}")
    String getWorkspaceName(@Param("projectId") Long projectId);
}

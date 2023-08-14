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

import com.webank.wedatasphere.dss.framework.release.entity.task.ReleaseTask;
import com.webank.wedatasphere.dss.orchestrator.common.entity.ReleaseHistoryDetail;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestPublishHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * created by cooperyang on 2020/12/14
 * Description: 用于持久化发布任务
 */
@Mapper
public interface ReleaseTaskMapper {

    void insertReleaseTask(ReleaseTask releaseTask);

    void updateReleaseTask(ReleaseTask releaseTask);

    ReleaseTask getReleaseTask(@Param("taskId") Long taskId);

    Integer getReleaseTaskByOrcId(@Param("orchestratorId") Long orchestratorId);

    List<ReleaseHistoryDetail> getReleaseHistoryByOrchestratorId(RequestPublishHistory request);

    @Select("SELECT distinct(release_user) FROM dss_release_task WHERE orchestrator_id = #{orchestratorId}  ")
    List<String> getReleaseUserListByOrcId(@Param("orchestratorId") Long orchestratorId);

    List<ReleaseTask> getReleaseTaskByStatus(@Param("statuses") List<String> statuses);

    void batchUpdateReleaseJob(@Param("failedJobs") List<ReleaseTask> failedJobs);
}
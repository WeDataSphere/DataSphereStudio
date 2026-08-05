/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.orchestrator.db.dao;

import com.webank.wedatasphere.dss.common.entity.BmlResource;
import com.webank.wedatasphere.dss.orchestrator.common.entity.*;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrchestratorMapper {

    void addOrchestrator(DSSOrchestratorInfo dssOrchestratorInfo);

    DSSOrchestratorInfo getOrchestrator(Long id);

    DSSOrchestratorInfo getOrchestratorByUUID(String uuid);

    void updateOrchestrator(DSSOrchestratorInfo dssOrchestratorInfo);

    void updateOrchestratorBmlVersion(@Param("id") Long id, @Param("resourceId") String resourceId, @Param("bmlVersion") String bmlVersion);

    BmlResource getOrchestratorBmlVersion(@Param("id") Long id);

    void deleteOrchestrator(Long id);

    /**
     * @param dssOrchestratorVersion
     */

    void addOrchestratorVersion(DSSOrchestratorVersion dssOrchestratorVersion);

    DSSOrchestratorVersion getOrchestratorVersion(Long versionId);

    /**
     * 获取orc指定版本信息
     *
     * @param orchestratorId id
     * @param orcVersionId   orc版本id
     * @param validFlag      有效标志
     * @return DSSOrchestratorVersion
     */
    DSSOrchestratorVersion getOrcVersionByIdAndOrcVersionId(@Param("orchestratorId") Long orchestratorId,
                                                            @Param("orcVersionId") Long orcVersionId,
                                                            @Param("validFlag") Integer validFlag);


    /**
     * 根据id查找最新的版本信息
     *
     * @param orchestratorId
     * @return
     */
    DSSOrchestratorVersion getLatestOrchestratorVersionById(Long orchestratorId);

    /**
     * 根据id查找最新的版本信息
     *
     * @param orchestratorId
     * @return
     */
    DSSOrchestratorVersion getLatestOrchestratorVersionByIdAndValidFlag(@Param("orchestratorId") Long orchestratorId, @Param("validFlag") Integer validFlag);

    void updateOrchestratorVersion(DSSOrchestratorVersion dssOrchestratorVersion);

    void updateOrchestratorVersionUpdateTime(@Param("id") Long id);

    void deleteOrchestratorVersion(Long versionId);

    /**
     * 根据id查找所有的版本信息
     *
     * @param orchestratorId
     * @return
     */
    List<DSSOrchestratorVersion> getVersionByOrchestratorId(Long orchestratorId);

    OrchestratorInfo getOrcInfoByAppId(@Param("appId") Long appId);
    List<OrchestratorInfo> getOrcInfosByAppIds(@Param("appIds") List<Long> appIds);

    @Select("select max(id) from dss_orchestrator_version_info where `orchestrator_id` = #{orchestratorId}")
    Long findLatestOrcVersionId(@Param("orchestratorId") Long orchestratorId);

    @Select("select id from dss_orchestrator_version_info where `orchestrator_id` = #{orchestratorId} and valid_flag = #{validFlag} ORDER BY version DESC LIMIT 1")
    Long findLatestVIdByOrcIdAndValidFlag(@Param("orchestratorId") Long orchestratorId, @Param("validFlag") Integer validFlag);

    List<DSSOrchestratorVersion> getOrchestratorVersions(@Param("projectId") Long projectId, @Param("orchestratorId") Long orchestratorId);

    @Select("select max(`version`) from dss_orchestrator_version_info where orchestrator_id = #{orchestratorId} and valid_flag = #{validFlag}")
    String getLatestVersion(@Param("orchestratorId") Long orchestratorId, @Param("validFlag") Integer validFlag);

    @Select("select uuid from dss_orchestrator_info where project_id = #{projectId} and name = #{name}")
    String getOrcNameByParam(@Param("projectId") Long projectId, @Param("name") String name);

    @Select("select id from `dss_orchestrator_info` where `project_id` = #{projectId} and `is_published` = #{isPublished}")
    List<Long> getAllOrcIdsByProjectId(@Param("projectId") Long projectId, @Param("isPublished")Integer isPublished);

    @Select("select max(`app_id`) from `dss_orchestrator_version_info` where `orchestrator_id` = #{orchestratorId} and `version` = #{version}")
    Long getAppIdByVersion(@Param("orchestratorId") Long orchestratorId, @Param("version") String version);

    DSSOrchestratorVersion getVersionByOrchestratorIdAndVersion(@Param("orchestratorId") Long orchestratorId, @Param("version") String version);

    @Update("update `dss_orchestrator_info` set `is_published` =  1 where id = #{orchestratorId}")
    void setPublished(@Param("orchestratorId") Long orchestratorId);

    @Select("select `name` from dss_orchestrator_info where `id` = #{orchestratorId}")
    String getOrchestratorNameById(@Param("orchestratorId") int orchestratorId);

    @Select("select * from dss_orchestrator_info where `project_id` = #{projectId} and `name` = #{name}")
    List<DSSOrchestratorInfo> getByNameAndProjectId(@Param("projectId") Long projectId, @Param("name") String name);

    void addOrchestratorRefOrchestration(DSSOrchestratorRefOrchestration dssOrchestratorRefOrchestration);

    List<DSSOrchestratorInfo> queryOrchestratorInfos(@Param("params") Map<String, Object> params);

    DSSOrchestratorRefOrchestration getRefOrchestrationId(@Param("orchestratorId") Long orchestratorId);

    List<DSSOrchestratorVersion> getHistoryOrcVersion(@Param("remainVersion") int remainVersion);

    void batchUpdateOrcInfo(@Param("list") List<DSSOrchestratorVersion> historyOrcVersion);

    void updateOrchestratorSubmitJobStatus(@Param("id") Long id, @Param("status") String status, @Param("errMsg") String errMsg);

    Long getLatestOrchestratorSubmitJobId(@Param("orchestratorId") Long orchestratorId);

    void updateOrchestratorSubmitResult(@Param("id") Long id, @Param("status") String status, @Param("result") String result);

    void batchUpdateOrchestratorSubmitJobStatus(@Param("list") List<Long> list, @Param("status") String status, @Param("errMsg") String errMsg);

    void insertOrchestratorSubmitJob(OrchestratorSubmitJob orchestratorSubmitJob);

    OrchestratorSubmitJob selectSubmitJobStatus(@Param("orchestratorId") Long orchestratorId);

    OrchestratorSubmitJob selectSubmitJobStatusById(@Param("id") Long id);

    String selectResult(@Param("id") Long id);

    List<OrchestratorMeta> getAllOrchestratorMeta(OrchestratorMetaRequest orchestratorMetaRequest);

    OrchestratorMeta getOrchestratorMeta(@Param("orchestratorId") Long orchestratorId);

    List<String> getOrchestratorName(List<Long> list);

    List<OrchestratorReleaseVersionInfo> getOrchestratorReleaseVersionInfo(@Param("orchestratorIdList") List<Long> orchestratorIdList);

    List<OrchestratorTemplateInfo> getOrchestratorDefaultTemplateInfo(@Param("orchestratorIdList")List<Long> orchestratorIdList);


    List<String> getAllOrchestratorName(@Param("workspaceId") Long workspaceId,@Param("projectName") String projectName);


    OrchestratorReleaseVersionInfo getOrchestratorVersionById(@Param("orchestratorId") Long orchestratorId);


    List<OrchestratorSubmitJob> getSubmitJobStatus(@Param("orchestratorIdList") List<Long> orchestratorIdList);


    void updateOrchestratorNotContainsKeywordsNode(@Param("orchestratorId") Long orchestratorId, @Param("notContainsKeywordsNode") String notContainsKeywordsNode);

    DSSOrchestratorInfo getOrchestratorNotContainsKeywordsNode(@Param("orchestratorId") Long orchestratorId);

    List<DSSOrchestratorInfo> getOrchestratorListById(@Param("orchestratorIdList") List<Long> orchestratorIdList);


    // ==================== 项目台账资产治理增强（只读统计方法） ====================

    /**
     * 按 projectId 批量统计有效工作流数（validFlag=1 去重 orchestrator 数）。
     *
     * @param projectIds 项目 ID 列表
     * @return 每个项目的 projectId + count，仅返回有匹配的行
     */
    List<ProjectOrcStatVO> countOrcByProjectIds(@Param("projectIds") List<Long> projectIds);

    /**
     * 按 projectId 批量取最近工作流更新时间（max update_time）。
     *
     * @param projectIds 项目 ID 列表
     * @return 每个项目的 projectId + updateTime，仅返回有匹配的行
     */
    List<ProjectOrcStatVO> maxUpdateTimeByProjectIds(@Param("projectIds") List<Long> projectIds);

    /**
     * 单项目工作流摘要（名称 + 更新时间 + 发布状态），用于详情抽屉。
     *
     * @param projectId 项目 ID
     * @return 工作流摘要列表
     */
    List<WorkflowSummaryVO> listOrcSummaryByProjectId(@Param("projectId") Long projectId);

    /**
     * [节点统计] 按 projectId 批量统计节点数（dss_workflow_node_content 计数）。
     * LEFT JOIN 保证无节点的项目返回 nodeCount=0。
     *
     * @param projectIds 项目 ID 列表
     * @return 每个项目的 projectId + nodeCount，仅返回有 orchestrator 的行
     */
    List<ProjectNodeStatVO> countNodesByProjectIds(@Param("projectIds") List<Long> projectIds);

    /**
     * [节点统计] 单项目节点类型分布（job_type 分组计数 + 节点显示名），按 count 降序。
     *
     * @param projectId 项目 ID
     * @return 节点类型分布列表（jobType + nodeTypeName + count）
     */
    List<NodeTypeDistributionVO> listNodeTypeDistributionByProjectId(@Param("projectId") Long projectId);

}

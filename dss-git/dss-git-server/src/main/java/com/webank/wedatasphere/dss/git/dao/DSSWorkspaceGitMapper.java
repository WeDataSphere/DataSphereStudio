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
package com.webank.wedatasphere.dss.git.dao;

import com.webank.wedatasphere.dss.git.common.protocol.GitUserEntity;

import com.webank.wedatasphere.dss.git.dto.GitProjectGitInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DSSWorkspaceGitMapper {
    void insert(GitUserEntity gitUserDO);

    void update(GitUserEntity gitUserDO);

    GitUserEntity selectByWorkspaceId(@Param("workspaceId") Long workspaceId, @Param("type") String type);

    GitUserEntity selectByUser(@Param("gitUser") String gitUser);

    List<GitUserEntity> selectGitUser(@Param("workspaceId") Long workspaceId, @Param("type") String type, @Param("gitUser") String gitUser);

    List<Long> getAllWorkspaceId();

    List<GitProjectGitInfo> getProjectInfoByWorkspaceId(@Param ("workspaceId") Long workspaceId);

    Long getWorkspaceIdByUserName(@Param("gitUser") String gitUser);

    GitProjectGitInfo getProjectInfoByProjectName(@Param ("projectName") String projectName);

    void insertProjectInfo(GitProjectGitInfo projectGitInfo);

    void updateProjectToken(@Param("projectName") String projectName, @Param("gitToken") String gitToken);

    void updateProjectId(@Param("projectName") String projectName, @Param("gitProjectId") String gitProjectId);
}

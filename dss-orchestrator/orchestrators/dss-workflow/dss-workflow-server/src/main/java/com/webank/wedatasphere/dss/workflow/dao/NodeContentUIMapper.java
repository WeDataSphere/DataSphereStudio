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
package com.webank.wedatasphere.dss.workflow.dao;


import com.webank.wedatasphere.dss.workflow.dto.NodeContentUIDO;

import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper
public interface NodeContentUIMapper {
    void insertNodeContentUI(NodeContentUIDO contentUIDO);

    void batchInsertNodeContentUI(List<NodeContentUIDO> list);

    void updateNodeContentUI(NodeContentUIDO contentUIDO);

    NodeContentUIDO getNodeContentUI(@Param("contentId") Long contentId);

    void batchUpdateNodeContentUI(NodeContentUIDO contentUIDO);

    void deleteNodeContentUIByContentList(@Param("list") List<Long> list);

    List<NodeContentUIDO> queryNodeContentUIList(@Param("contentIdList") List<Long> contentIdList);

    List<NodeContentUIDO> getNodeContentUIByContentId(@Param("contentId") Long contentId);

    List<NodeContentUIDO> getNodeContentUIByNodeUIKey(@Param("contentIdList") List<Long> contentIdList,@Param("nodeUIKey")String nodeKey);
}

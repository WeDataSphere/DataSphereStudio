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
package com.webank.wedatasphere.dss.data.asset.service;

import com.webank.wedatasphere.dss.data.asset.entity.*;
import com.webank.wedatasphere.dss.data.common.exception.DataGovernanceException;
import com.webank.wedatasphere.dss.data.common.atlas.AtlasClassificationV2.AtlasClassificationsV2;
import org.apache.atlas.model.instance.AtlasClassification;
import org.apache.atlas.model.lineage.AtlasLineageInfo;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AssetService {
    public Map<String, Long> getHiveSummary() throws DataGovernanceException;

    public List<HiveTblSimpleInfo> searchHiveTable(String classification, String query,
                                                   int limit, int offset) throws DataGovernanceException;

    public HiveTblDetailInfo getHiveTblDetail(String guid) throws DataGovernanceException;

    public List<HivePartInfo> getHiveTblPartition(String guid) throws DataGovernanceException;

    public String getHiveTblGuid(String qualifiedName) throws DataGovernanceException;

    public String getTbSelect(String guid) throws DataGovernanceException;

    public String getTbCreate(String guid) throws DataGovernanceException;

    public void modifyComment(String guid, String commentStr) throws DataGovernanceException;

    public void bulkModifyComment(Map<String, String> commentMap) throws DataGovernanceException;

    public void setLabels(String guid, Set<String> labels) throws DataGovernanceException;

    public void removeLabels(String guid, Set<String> labels) throws DataGovernanceException;

    /**
     * 获取表实体的血缘信息
     */
    public AtlasLineageInfo getHiveTblLineage(final String guid, final AtlasLineageInfo.LineageDirection direction, final int depth) throws DataGovernanceException;

    public List<HiveStorageInfo> getTop10Table() throws DataGovernanceException, SQLException;

    public void addClassifications(String guid, List<AtlasClassification> classifications) throws DataGovernanceException;

    public void deleteClassification(String guid, String classificationName) throws DataGovernanceException;

    public void deleteClassifications(String guid, List<AtlasClassification> classifications) throws DataGovernanceException;

    public void updateClassifications(String guid, List<AtlasClassification> classifications) throws DataGovernanceException;

    /**
     * 为实体删除已有的分类，添加新的分类
     */
    public void removeAndAddClassifications(String guid, List<AtlasClassification> newClassifications) throws DataGovernanceException;

    public AtlasClassificationsV2 getClassifications(String guid) throws DataGovernanceException;
}

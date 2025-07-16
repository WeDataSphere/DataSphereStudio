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
package com.webank.wedatasphere.dss.data.classification.service;

import com.webank.wedatasphere.dss.data.common.exception.DataGovernanceException;
import org.apache.atlas.AtlasServiceException;
import org.apache.atlas.model.typedef.AtlasClassificationDef;
import org.apache.atlas.model.typedef.AtlasTypesDef;

import java.util.List;

public interface ClassificationService {
    public AtlasTypesDef getClassificationDef( ) throws DataGovernanceException;

    public AtlasClassificationDef getClassificationDefByName(String name) throws DataGovernanceException;

    public List<AtlasClassificationDef> getClassificationDefListByName(String name) throws DataGovernanceException;

    public List<AtlasClassificationDef> getClassificationDefListForLayer() throws DataGovernanceException;

    public AtlasTypesDef createAtlasTypeDefs(final AtlasTypesDef typesDef) throws DataGovernanceException;

    public void updateAtlasTypeDefs(final AtlasTypesDef typesDef) throws DataGovernanceException;

    public void deleteClassificationDefByName(String name) throws DataGovernanceException;


}

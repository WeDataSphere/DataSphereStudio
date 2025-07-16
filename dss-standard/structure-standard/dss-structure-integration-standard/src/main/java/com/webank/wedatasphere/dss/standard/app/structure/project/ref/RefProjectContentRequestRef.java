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
package com.webank.wedatasphere.dss.standard.app.structure.project.ref;

import com.webank.wedatasphere.dss.standard.app.structure.StructureRequestRef;
import com.webank.wedatasphere.dss.standard.app.structure.StructureRequestRefImpl;

/**
 * @author enjoyyin
 * @date 2022-03-13
 * @since 0.5.0
 */
public interface RefProjectContentRequestRef<R extends RefProjectContentRequestRef<R>>
        extends StructureRequestRef<R> {

    default Long getRefProjectId() {
        return (Long) getParameter("refProjectId");
    }

    default R setRefProjectId(Long refProjectId) {
        setParameter("refProjectId", refProjectId);
        return (R) this;
    }

    default String getProjectName() {
        return (String) getParameter("projectName");
    }

    default R setProjectName(String projectName) {
        setParameter("projectName", projectName);
        return (R) this;
    }

    class RefProjectContentRequestRefImpl extends StructureRequestRefImpl<RefProjectContentRequestRefImpl>
        implements RefProjectContentRequestRef<RefProjectContentRequestRefImpl>{}

}

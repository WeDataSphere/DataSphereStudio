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
package com.webank.wedatasphere.dss.standard.app.structure;

import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.app.sso.ref.WorkspaceRequestRef;
import com.webank.wedatasphere.dss.standard.common.entity.ref.RequestRefImpl;

import java.util.List;

/**
 * @author enjoyyin
 * @date 2022-03-01
 * @since 0.5.0
 */
public interface StructureRequestRef<R extends StructureRequestRef<R>> extends WorkspaceRequestRef {

    R setName(String name);

    String getUserName();

    R setUserName(String userName);

    R setDSSLabels(List<DSSLabel> dssLabels);

    R setType(String type);

    R setWorkspace(Workspace workspace);

}

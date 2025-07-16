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
package com.webank.wedatasphere.dss.standard.app.development.ref;

import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.app.sso.ref.WorkspaceRequestRef;

import java.util.List;

/**
 * @author enjoyyin
 * @date 2022-03-09
 * @since 0.5.0
 */
public interface DevelopmentRequestRef<R extends DevelopmentRequestRef<R>> extends WorkspaceRequestRef {

    default R setUserName(String userName) {
        setParameter("userName", userName);
        return (R) this;
    }

    default String getUserName() {
        return (String) getParameter("userName");
    }

    R setName(String name);

    R setDSSLabels(List<DSSLabel> dssLabels);

    R setType(String type);

    R setWorkspace(Workspace workspace);

}

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
package com.webank.wedatasphere.dss.standard.app.structure.role.ref;

import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.app.sso.ref.WorkspaceRequestRef;
import com.webank.wedatasphere.dss.standard.app.structure.StructureRequestRef;

/**
 * @author enjoyyin
 * @date 2022-05-07
 * @since 1.1.0
 */
public interface DSSRoleContentRequestRef<R extends DSSRoleContentRequestRef<R>>
        extends RoleRequestRef<R> {

    /**
     * 包含了 DSS {@code Role} 的信息
     * @return Role
     */
    default Role getRole() {
        return (Role) getParameter("role");
    }

    default R setRole(Role role) {
        setParameter("role", role);
        return (R) this;
    }

}

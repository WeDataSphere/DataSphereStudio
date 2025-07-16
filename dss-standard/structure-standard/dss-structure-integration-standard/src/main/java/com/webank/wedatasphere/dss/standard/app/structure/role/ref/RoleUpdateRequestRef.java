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

import com.webank.wedatasphere.dss.standard.common.exception.operation.ExternalOperationWarnException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author enjoyyin
 * @since 1.1.0
 */
public interface RoleUpdateRequestRef<R extends RoleUpdateRequestRef<R>> extends
    DSSRoleContentRequestRef<R>, RefRoleContentRequestRef<R> {

    @Override
    default String getRoleName() {
        if(getRole() == null) {
            return null;
        }
        return getRole().getName();
    }

    @Override
    default R setRoleName(String roleName) {
        throw new ExternalOperationWarnException(90030, "not support method.");
    }

    /**
     * 该角色本次新增的所有用户列表
     * @return 本次新增的所有用户列表
     */
    default List<String> getAddedUserNames() {
        if(!getParameters().containsKey("addedUserNames")) {
            return new ArrayList<>();
        }
        return (List<String>) getParameter("addedUserNames");
    }

    default R setAddedUserNames(List<String> addedUserNames) {
        setParameter("addedUserNames", addedUserNames);
        return (R) this;
    }

    /**
     * 该角色本次删除的所有用户列表
     * @return 本次新增的所有用户列表
     */
    default List<String> getRemovedUserNames() {
        if(!getParameters().containsKey("removedUserNames")) {
            return new ArrayList<>();
        }
        return (List<String>) getParameter("removedUserNames");
    }

    default R setRemovedUserNames(List<String> removedUserNames) {
        setParameter("removedUserNames", removedUserNames);
        return (R) this;
    }

}

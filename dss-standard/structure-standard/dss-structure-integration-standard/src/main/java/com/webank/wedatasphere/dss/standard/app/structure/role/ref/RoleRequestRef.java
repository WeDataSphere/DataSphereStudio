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

import com.webank.wedatasphere.dss.standard.app.structure.StructureRequestRef;
import com.webank.wedatasphere.dss.standard.app.structure.StructureRequestRefImpl;

/**
 * RoleRequestRef 为 RoleOperation 的 RequestRef 基类。
 * 主要提供了各个 RoleRequestRef 子接口的实现类以供使用。
 * @author enjoyyin
 * @since 1.1.0
 */
public interface RoleRequestRef<R extends RoleRequestRef<R>> extends StructureRequestRef<R> {

    class DSSRoleContentRequestRefImpl extends StructureRequestRefImpl<DSSRoleContentRequestRefImpl>
        implements DSSRoleContentRequestRef<DSSRoleContentRequestRefImpl>{}

    class RefRoleContentRequestRefImpl extends StructureRequestRefImpl<RefRoleContentRequestRefImpl>
        implements RefRoleContentRequestRef<RefRoleContentRequestRefImpl> {}

    class RoleUpdateRequestRefImpl extends StructureRequestRefImpl<RoleUpdateRequestRefImpl>
        implements RoleUpdateRequestRef<RoleUpdateRequestRefImpl> {}

}

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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.webank.wedatasphere.dss.standard.app.structure.StructureRequestRefImpl;

import java.util.Collections;
import java.util.List;

/**
 * @author enjoyyin
 * @date 2022-03-13
 * @since 1.1.0
 */
public interface ProjectUpdateRequestRef<R extends ProjectUpdateRequestRef<R>>
        extends DSSProjectContentRequestRef<R>, RefProjectContentRequestRef<R> {

    @Override
    default String getProjectName() {
        return getDSSProject().getName();
    }

    @Override
    default R setProjectName(String projectName) {
        getDSSProject().setName(projectName);
        return (R) this;
    }

    /**
     * 只包含本次新增的 DSS 工程相关权限用户
     * @return
     */
    default DSSProjectPrivilege getAddedDSSProjectPrivilege() {
        return (DSSProjectPrivilege) this.getParameter("addedDSSProjectPrivilege");
    }

    default R setAddedDSSProjectPrivilege(DSSProjectPrivilege addedDSSProjectPrivilege) {
        setParameter("addedDSSProjectPrivilege", addedDSSProjectPrivilege);
        return (R) this;
    }


    /**
     * 新的项目owner，用于项目交接
     * @return
     */
    default String getNewOwner() {
        return (String) this.getParameter("newOwner");
    }

    default R setNewOwner(String newOwner) {
        setParameter("newOwner", newOwner);
        return (R) this;
    }

    /**
     * 只包含本次移除的 DSS 工程相关权限用户
     * @return
     */
    default DSSProjectPrivilege getRemovedDSSProjectPrivilege() {
        return (DSSProjectPrivilege) this.getParameter("removedDSSProjectPrivilege");
    }

    default R setRemovedDSSProjectPrivilege(DSSProjectPrivilege removedDSSProjectPrivilege) {
        setParameter("removedDSSProjectPrivilege", removedDSSProjectPrivilege);
        return (R) this;
    }
    /**
     * DSS 工程的全量最新权数据源列表，包含了 DSS 工程所有的数据源
     * 第三方组件需要根据这个列表来判断哪些数据源是新增的，哪些数据源是删除的，时刻保持同步
     * @return DSSProjectPrivilege
     */
    default List<DSSProjectDataSource> getDSSProjectDataSources() {
        String json = (String) this.getParameter("dssProjectDataSources");
        if(json != null) {
            return   new Gson().fromJson(json, new TypeToken<List<DSSProjectDataSource>>(){}.getType());
        }
        return Collections.emptyList();
    }


    default R setDSSProjectDataSources(List<DSSProjectDataSource> dssProjectDataSources) {
        //为了让第三方组件可以不用升级dss依赖包，转成json string再存储
        String json = new Gson().toJson(dssProjectDataSources);
        setParameter("dssProjectDataSources", json);
        return (R) this;
    }
    class ProjectUpdateRequestRefImpl extends StructureRequestRefImpl<ProjectUpdateRequestRefImpl>
            implements ProjectUpdateRequestRef<ProjectUpdateRequestRefImpl> {}

}

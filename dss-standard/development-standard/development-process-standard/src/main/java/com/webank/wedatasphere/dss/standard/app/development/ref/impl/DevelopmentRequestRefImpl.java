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
package com.webank.wedatasphere.dss.standard.app.development.ref.impl;

import com.webank.wedatasphere.dss.common.label.DSSLabel;
import com.webank.wedatasphere.dss.standard.app.development.ref.DevelopmentRequestRef;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.common.entity.ref.RequestRefImpl;

import java.util.List;

/**
 * @author enjoyyin
 * @date 2022-03-09
 * @since 0.5.0
 */
public class DevelopmentRequestRefImpl<R extends DevelopmentRequestRefImpl<R>> extends RequestRefImpl
    implements DevelopmentRequestRef<R> {

    private Workspace workspace;

    @Override
    public R setName(String name) {
        this.name = name;
        return (R) this;
    }

    @Override
    public R setDSSLabels(List<DSSLabel> dssLabels) {
        this.dssLabels = dssLabels;
        return (R) this;
    }

    @Override
    public R setType(String type) {
        this.type = type;
        return (R) this;
    }

    @Override
    public R setWorkspace(Workspace workspace) {
        this.workspace = workspace;
        return (R) this;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}

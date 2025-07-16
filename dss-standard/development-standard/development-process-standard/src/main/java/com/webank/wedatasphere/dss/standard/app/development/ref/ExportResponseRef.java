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

import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRefBuilder;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRefImpl;

import java.util.Map;

/**
 * @author enjoyyin
 * @date 2022-03-09
 * @since 0.5.0
 */
public interface ExportResponseRef extends ResponseRef {

    /**
     * Now, DSS only supports to import/export Linkis BML resources or one inputStream,
     * so the resourceMap is consisted of `resourceId` and `version` if you choose Linkis BML resources,
     * or `InputStream` and `Closeable` if you choose Stream resources.
     * <br>
     * If third-part AppConn want to achieve the {@code RefExportOperation}, it is necessary that the third-part AppConn
     * must upload the meta and resources of third-part AppConn job to Linkis BML at first, or packages the the meta and
     * resources of third-part AppConn job as a InputStream, then return the resourceMap to DSS.
     * @return a refJobContent related a only third appConn refJob
     */
    Map<String, Object> getResourceMap();

    default boolean isLinkisBMLResources() {
        return getResourceMap().containsKey(ImportRequestRef.RESOURCE_ID_KEY)
                && getResourceMap().containsKey(ImportRequestRef.RESOURCE_VERSION_KEY);
    }

    static ExportResponseRefBuilder newBuilder() {
        return new ExportResponseRefBuilder();
    }

    class ExportResponseRefBuilder
            extends ResponseRefBuilder.ExternalResponseRefBuilder<ExportResponseRefBuilder, ExportResponseRef> {

        private Map<String, Object> resourceMap;

        public ExportResponseRefBuilder setResourceMap(Map<String, Object> resourceMap) {
            this.resourceMap = resourceMap;
            return this;
        }

        class ExportResponseRefImpl extends ResponseRefImpl implements ExportResponseRef {
            public ExportResponseRefImpl() {
                super(ExportResponseRefBuilder.this.responseBody, ExportResponseRefBuilder.this.status,
                        ExportResponseRefBuilder.this.errorMsg, ExportResponseRefBuilder.this.responseMap);
            }
            @Override
            public Map<String, Object> getResourceMap() {
                return resourceMap;
            }
        }

        @Override
        protected ExportResponseRefImpl createResponseRef() {
            return new ExportResponseRefImpl();
        }
    }
}

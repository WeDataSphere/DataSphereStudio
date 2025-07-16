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
 * The class is used by the DevelopmentOperation of RefCreationOperation, RefImportOperation, which should return
 * this class to deliver a refJobContent related a only third appConn refJob to DSS framework.
 * @author enjoyyin
 * @date 2022-03-09
 * @since 0.5.0
 */
public interface RefJobContentResponseRef extends ResponseRef {

    /**
     * This refJobContent must not be empty.
     * The third appConn should ensure this refJObContent can relate to a only third appConn refJob.
     * @return a refJobContent related a only third appConn refJob
     */
    Map<String, Object> getRefJobContent();

    static RefJobContentResponseRefBuilder newBuilder() {
        return new RefJobContentResponseRefBuilder();
    }

    class RefJobContentResponseRefBuilder
            extends ResponseRefBuilder.ExternalResponseRefBuilder<RefJobContentResponseRefBuilder, RefJobContentResponseRef> {

        private Map<String, Object> refJobContent;

        public RefJobContentResponseRefBuilder setRefJobContent(Map<String, Object> refJobContent) {
            this.refJobContent = refJobContent;
            return this;
        }

        class RefJobContentResponseRefImpl extends ResponseRefImpl implements RefJobContentResponseRef{
            public RefJobContentResponseRefImpl() {
                super(RefJobContentResponseRefBuilder.this.responseBody, RefJobContentResponseRefBuilder.this.status,
                        RefJobContentResponseRefBuilder.this.errorMsg, RefJobContentResponseRefBuilder.this.responseMap);
            }
            @Override
            public Map<String, Object> getRefJobContent() {
                return refJobContent;
            }
        }

        @Override
        protected RefJobContentResponseRef createResponseRef() {
            return new RefJobContentResponseRefImpl();
        }
    }

}

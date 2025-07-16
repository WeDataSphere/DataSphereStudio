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
package com.webank.wedatasphere.dss.standard.app.sso.user.ref;

import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRef;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRefBuilder;
import com.webank.wedatasphere.dss.standard.common.entity.ref.ResponseRefImpl;

import java.util.Map;

/**
 * @author enjoyyin
 * @date 2022-04-25
 * @since 1.1.0
 */
public interface RefUserContentResponseRef extends ResponseRef {

    /**
     * 第三方 AppConn 的 userId
     * @return
     */
    String getRefUserId();

    /**
     * 第三方 AppConn 的额外用户信息，可以为空
     * @return
     */
    Map<String, Object> getRefUserContent();

    static Builder newBuilder() {
        return new Builder();
    }

    class Builder extends ResponseRefBuilder.ExternalResponseRefBuilder<Builder, RefUserContentResponseRef> {

        private String refUserId;
        private Map<String, Object> refUserContent;

        public Builder setRefUserId(String refUserId) {
            this.refUserId = refUserId;
            return this;
        }

        public Builder setRefUserContent(Map<String, Object> refUserContent) {
            this.refUserContent = refUserContent;
            return this;
        }

        private class RefUserContentResponseRefImpl extends ResponseRefImpl implements RefUserContentResponseRef {

            public RefUserContentResponseRefImpl() {
                super(Builder.this.responseBody, Builder.this.status, Builder.this.errorMsg, Builder.this.responseMap);
            }

            @Override
            public String getRefUserId() {
                return refUserId;
            }

            @Override
            public Map<String, Object> getRefUserContent() {
                return refUserContent;
            }
        }

        @Override
        protected RefUserContentResponseRef createResponseRef() {
            return new RefUserContentResponseRefImpl();
        }
    }

}

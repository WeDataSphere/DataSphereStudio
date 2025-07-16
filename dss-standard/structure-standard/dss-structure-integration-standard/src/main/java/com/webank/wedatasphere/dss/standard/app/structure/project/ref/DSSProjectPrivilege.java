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

import java.util.ArrayList;
import java.util.List;

/**
 * @author enjoyyin
 * @date 2022-04-19
 * @since 1.1.0
 */
public interface DSSProjectPrivilege {

    DSSProjectPrivilege EMPTY = newBuilder().setAccessUsers(new ArrayList<>(0))
            .setEditUsers(new ArrayList<>(0))
            .setReleaseUsers(new ArrayList<>(0)).build();

    List<String> getAccessUsers();

    List<String> getEditUsers();

    List<String> getReleaseUsers();

    static Builder newBuilder() {
        return new Builder();
    }

    class Builder {

        protected List<String> accessUsers;
        protected List<String> editUsers;
        protected List<String> releaseUsers;

        public Builder setAccessUsers(List<String> accessUsers) {
            this.accessUsers = accessUsers;
            return this;
        }

        public Builder setEditUsers(List<String> editUsers) {
            this.editUsers = editUsers;
            return this;
        }

        public Builder setReleaseUsers(List<String> releaseUsers) {
            this.releaseUsers = releaseUsers;
            return this;
        }

        public DSSProjectPrivilege build() {
            return new DSSProjectPrivilege(){
                @Override
                public List<String> getAccessUsers() {
                    return accessUsers;
                }

                @Override
                public List<String> getEditUsers() {
                    return editUsers;
                }

                @Override
                public List<String> getReleaseUsers() {
                    return releaseUsers;
                }
            };
        }
    }

}

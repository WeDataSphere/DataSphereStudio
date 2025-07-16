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
package com.webank.wedatasphere.dss.appconn.dolphinscheduler.sso;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author enjoyyin
 * @date 2022-03-17
 * @since 0.5.0
 */
public class UserCreationFactory {

    public User createUser(String userName) {
        return new User() {
            @Override
            public String getUserName() {
                return userName;
            }
            @Override
            public String getUserPassword() {
                return RandomStringUtils.random(8);
            }
            @Override
            public String getTenantId() {
                return "1";
            }
            @Override
            public String getEmail() {
                return "xx@qq.com";
            }
            @Override
            public String getQueue() {
                return "default";
            }
        };
    }

    public interface User {

        String getUserName();

        String getUserPassword();

        String getTenantId();

        String getEmail();

        String getQueue();
    }
}

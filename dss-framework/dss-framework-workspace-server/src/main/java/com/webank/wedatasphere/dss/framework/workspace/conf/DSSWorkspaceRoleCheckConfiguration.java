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
package com.webank.wedatasphere.dss.framework.workspace.conf;

import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceAddUserHook;
import com.webank.wedatasphere.dss.framework.workspace.service.DSSWorkspaceRoleCheckService;
import com.webank.wedatasphere.dss.framework.workspace.service.impl.DSSWorkspaceAddUserHookImpl;
import com.webank.wedatasphere.dss.framework.workspace.service.impl.DSSWorkspaceRoleCheckServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DSSWorkspaceRoleCheckConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DSSWorkspaceRoleCheckService createDSSWorkspaceRoleCheckService(){
        return new DSSWorkspaceRoleCheckServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public DSSWorkspaceAddUserHook createDSSWorkspaceAddUserHook(){
        return new DSSWorkspaceAddUserHookImpl();
    }

}

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
package com.webank.wedatasphere.dss.flow.execution.entrance.conf;

/**
 * Author: xlinliu
 * Date: 2023/1/16
 */

import org.apache.linkis.entrance.interceptor.EntranceInterceptor;
import org.apache.linkis.entrance.interceptor.impl.CommentInterceptor;
import org.apache.linkis.entrance.interceptor.impl.LabelCheckInterceptor;
import org.apache.linkis.entrance.interceptor.impl.LogPathCreateInterceptor;
import org.apache.linkis.entrance.constant.ServiceNameConsts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlowEntranceSpringConfiguration  {
    private  final Logger logger = LoggerFactory.getLogger(getClass());
    @Bean(name=ServiceNameConsts.ENTRANCE_INTERCEPTOR)
    public EntranceInterceptor[] entranceInterceptors() {
        logger.info("dss workflow entrance load entranceInterceptors");
        return new EntranceInterceptor[] {
                new LabelCheckInterceptor(),
                new LogPathCreateInterceptor(),
                new CommentInterceptor(),
        };
    }
}
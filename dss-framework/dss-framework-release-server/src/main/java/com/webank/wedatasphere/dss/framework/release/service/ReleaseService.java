/*
 *
 *  * Copyright 2019 WeBank
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.framework.release.service;

import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ResponsePublishUser;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.*;
import org.apache.linkis.common.exception.ErrorException;

import java.util.List;

/**
 * created by cooperyang on 2020/11/17
 * Description: release是一个复杂的过程，应该是要做成异步的操作,采用发布线程池来进行控制。
 */
public interface ReleaseService {

    ResponseConvertOrchestrator releaseOrchestrator(RequestReleaseOrchestration request) ;

    /**
     * 从生产中心直接发出任务。只用在生产中心
     * @param request
     * @throws ErrorException
     */
    void releaseOrchestratorFromProToSchedulis(RequestFrameworkConvertOrchestration request) throws ErrorException;


    ResponseConvertOrchestrator getStatus(String jobId);

    ResponsePublishHistory getReleaseHistory(RequestPublishHistory request);

    ResponsePublishUser getReleaseUserList(RequestPublishUser request);
}

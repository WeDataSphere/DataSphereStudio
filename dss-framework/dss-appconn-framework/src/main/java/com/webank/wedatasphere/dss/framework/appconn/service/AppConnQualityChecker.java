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
package com.webank.wedatasphere.dss.framework.appconn.service;

import com.webank.wedatasphere.dss.appconn.core.AppConn;
import com.webank.wedatasphere.dss.framework.appconn.exception.AppConnQualityErrorException;

/**
 * @author enjoyyin
 * @date 2022-04-14
 * @since 0.5.0
 */
public interface AppConnQualityChecker {

    /**
     * 检查用户实现的 AppConn 是否存在质量问题
     * @throws AppConnQualityErrorException 如果存在质量问题，请抛出该异常
     */
    void checkQuality(AppConn appConn) throws AppConnQualityErrorException;

}

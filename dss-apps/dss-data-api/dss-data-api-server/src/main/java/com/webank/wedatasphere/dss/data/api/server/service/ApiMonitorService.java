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
package com.webank.wedatasphere.dss.data.api.server.service;



import com.webank.wedatasphere.dss.data.api.server.entity.request.CallMonitorResquest;
import com.webank.wedatasphere.dss.data.api.server.entity.request.SingleCallMonitorRequest;
import com.webank.wedatasphere.dss.data.api.server.entity.response.ApiCallInfoByCnt;
import com.webank.wedatasphere.dss.data.api.server.entity.response.ApiCallInfoByFailRate;
import com.webank.wedatasphere.dss.data.api.server.entity.response.HourMonitorInfo;

import java.util.List;

public interface ApiMonitorService {
    /**
     * 已发布API数量
     */
    Long getOnlineApiCnt(Long workspaceId);

    /**
     * 未发布API数量
     */
    Long getOfflineApiCnt(Long workspaceId);

    /**
     * 总调用次数
     */
    Long getCallTotalCnt(CallMonitorResquest callMonitorResquest);

    /**
     * 总执行时长
     */
    Long getCallTotalTime(CallMonitorResquest callMonitorResquest);

    /**
     * 出错率排行TOP10
     */
    List<ApiCallInfoByCnt> getCallListByCnt(CallMonitorResquest callMonitorResquest);

    /**
     * 调用量排行TOP10
     */
    List<ApiCallInfoByFailRate> getCallListByFailRate(CallMonitorResquest callMonitorResquest);

    /**
     * 过去24小时，每小时的请求数目
     */
    List<HourMonitorInfo> getCallCntForPast24H(Long workspaceId) throws Exception;

    /**
     * 时间范围内指定API的每小时的平均响应时间
     */
    List<HourMonitorInfo> getCallTimeForSinleApi(SingleCallMonitorRequest singleCallMonitorRequest) throws Exception;

    /**
     * 时间范围内指定API的每小时的请求次数
     */
    List<HourMonitorInfo> getCallCntForSinleApi(SingleCallMonitorRequest singleCallMonitorRequest)  throws Exception;
}

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

package com.webank.wedatasphere.dss.apiservice.core.dao;

import com.webank.wedatasphere.dss.apiservice.core.vo.ApiAccessVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author allenlliu
 * @date 2020/11/11 10:25
 */
public interface ApiServiceAccessDao {
    void addAccessRecord(ApiAccessVo apiAccessVo);

    ApiAccessVo  queryByVersionId(Long versionId);

    ApiAccessVo  queryByApiId(Long apiServiceId);

    void updateTaskStatus(ApiAccessVo apiAccessVo);

    List<String> getExecuteTaskIds(@Param("apiId") Long apiId,@Param("apiVersionId") Long apiVersionId,@Param("username") String username, @Param("limitSize") int limitSize);

    String getHistoryQueryParams(@Param("username") String username, @Param("taskId") Long taskId);
}

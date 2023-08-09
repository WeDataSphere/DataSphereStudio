/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.dss.errorcode.server.dao;

import com.webank.wedatasphere.dss.errorcode.common.LinkisErrorCode;

import com.webank.wedatasphere.dss.errorcode.server.entity.ErrorCodeReport;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ErrorCodeMapper {

    @Select("SELECT * FROM dss_error_code_solution")
    @Results({
            @Result(property = "matchContent", column = "match_content"),
            @Result(property = "matchType", column = "match_type"),
            @Result(property = "appName", column = "app_name"),
            @Result(property = "uri", column = "uri"),
            @Result(property = "solution", column = "solution"),
            @Result(property = "updateBy", column = "update_by"),
            @Result(property = "skipped", column = "is_skipped"),
    })
    List<LinkisErrorCode> getAllErrorCodes();

    @Insert("insert into `dss_error_code_report`(`uri`,`request_body`,`request_headers`,`query_params`,`response_body`,`error_code`,`report_by`,`error_desc`)" +
            "values(#{uri},#{requestBody},#{requestHeaders},#{queryParams},#{responseBody},#{errCode},#{reportBy},#{errorDesc})")
    void addTaskProblem(@Param("uri") String requestUrl, @Param("requestBody") String requestBody, @Param("requestHeaders") String requestHeaders,
                        @Param("queryParams") String queryParams, @Param("responseBody") String responseBody, @Param("errCode") String errCode,
                        @Param("reportBy") String reportBy, @Param("errorDesc") String errorDesc);

    @Select("SELECT * FROM dss_error_code_report")
    @Results({
            @Result(property = "responseBody", column = "response_body"),
            @Result(property = "requestBody", column = "request_body"),
            @Result(property = "requestHeaders", column = "request_headers"),
            @Result(property = "uri", column = "uri"),
            @Result(property = "queryParams", column = "query_params"),
            @Result(property = "reportBy", column = "report_by"),
            @Result(property = "errCode", column = "error_code"),
    })
    List<ErrorCodeReport> getAllErrorReports();
}

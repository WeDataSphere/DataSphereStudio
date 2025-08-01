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
package com.webank.wedatasphere.dss.framework.admin.common.utils;

import com.webank.wedatasphere.dss.framework.admin.exception.DSSAdminWarnException;

public class SqlUtil {

    public static String SQL_PATTERN = "[a-zA-Z0-9_\\ \\,\\.]+";
    public static String escapeOrderBySql(String value)
    {
        if (StringUtils.isNotEmpty(value) && !isValidOrderBySql(value))
        {
            throw new DSSAdminWarnException("The parameters do not meet the specifications and cannot be queried (参数不符合规范，不能进行查询)");
        }
        return value;
    }
    public static boolean isValidOrderBySql(String value)
    {
        return value.matches(SQL_PATTERN);
    }
}

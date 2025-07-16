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
package com.webank.wedatasphere.dss.framework.project.contant;

import org.apache.commons.lang3.StringUtils;

public class DSSProjectConstant {

    // 升序
    public static final String ASCEND = "ascend";
    // 降序
    public static final String DESCEND = "descend";


    public static String concatOrderBySql(String sortBy, String orderBy) {

        if (StringUtils.isEmpty(sortBy) || StringUtils.isEmpty(orderBy)) {
            return null;
        }

        String[] sortArray = sortBy.split(",");
        String[] orderArray = orderBy.split(",");
        if (sortArray.length != orderArray.length) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < sortArray.length; i++) {
            buffer.append(sortArray[i]);
            if (ASCEND.equalsIgnoreCase(orderArray[i])) {
                buffer.append(" asc");
            } else {
                buffer.append(" desc");
            }

            if (i < sortArray.length - 1) {
                buffer.append(",");
            }
        }

        return buffer.toString();

    }

}

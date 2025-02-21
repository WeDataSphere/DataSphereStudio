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

package com.webank.wedatasphere.dss.orchestrator.server.constant;


public class DSSOrchestratorConstant {

    //每次提交linkis清理数量控制在10个以下，超了接口就会返回超时
    public static final int MAX_CLEAR_SIZE = 10;
    public static final String CHROME_DRIVER_PATH = "chromedriver";


    // 升序
    public static final  String ASCEND="ascend";
    // 降序
    public static final  String DESCEND="descend";
}

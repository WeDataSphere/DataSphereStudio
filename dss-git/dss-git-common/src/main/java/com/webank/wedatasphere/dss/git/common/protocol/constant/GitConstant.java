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
package com.webank.wedatasphere.dss.git.common.protocol.constant;


import java.util.Arrays;
import java.util.List;

public class GitConstant {
    public static String GIT_ACCESS_WRITE_TYPE = "write";
    public static String GIT_ACCESS_READ_TYPE = "read";
    public static String GIT_SERVER_META_PATH = ".metaConf";
    public static List<String> GIT_SERVER_SEARCH_TYPE = Arrays.asList(".sql",".hql",".jdbc", ".py", ".python", ".scala", ".sh");
}

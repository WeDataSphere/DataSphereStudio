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
package com.webank.wedatasphere.dss.guide.server.conf;

import org.apache.linkis.common.conf.CommonVars;

public interface GuideConf {
    CommonVars<String> GUIDE_CONTENT_IMAGES_PATH = CommonVars.apply("guide.content.images.path", "/usr/local/anlexander/all_bak/dss_linkis/dss-linkis-1.0.2/images");

    CommonVars<String> GUIDE_CHAPTER_IMAGES_PATH = CommonVars.apply("guide.chapter.images.path", "/usr/local/anlexander/all_bak/dss_linkis/dss-linkis-1.0.2/images");

    /**gitbook路径*/
    CommonVars<String> HOST_GITBOOK_PATH = CommonVars.apply("host.gitbook.path", "/appcom/Install/ApacheInstall/gitbook_books");

    CommonVars<String> HOST_IP_ADDRESS = CommonVars.apply("target.ip.address", "127.0.0.1");

    CommonVars<String> TARGET_GITBOOK_PATH = CommonVars.apply("target.gitbook.path", "/appcom/Install/ApacheInstall/gitbook_books");

    CommonVars<String> SUMMARY_IGNORE_MODEL = CommonVars.apply("summary.ignore.model","km");

    CommonVars<String> GUIDE_SYNC_MODEL = CommonVars.apply("guide.sync.model","gitbook");

}

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
package com.webank.wedatasphere.dss.guide.server.service;

import com.webank.wedatasphere.dss.guide.server.entity.GuideGroup;
import com.webank.wedatasphere.dss.guide.server.util.GuideException;

import java.util.List;

public interface GuideGroupService {
    public boolean saveGuideGroup(GuideGroup guideGroup);

    public GuideGroup queryGuideGroupByPath(String path);

    public List<GuideGroup> getAllGuideGroupDetails();

    public void deleteGuideGroup(Long id);

    public void asyncGuide(String summaryPath, String ignoreModel) throws GuideException;
}

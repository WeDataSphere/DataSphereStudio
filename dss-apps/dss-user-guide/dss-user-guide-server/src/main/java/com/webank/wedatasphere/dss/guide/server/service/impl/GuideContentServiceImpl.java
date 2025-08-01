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
package com.webank.wedatasphere.dss.guide.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedatasphere.dss.guide.server.dao.GuideContentMapper;
import com.webank.wedatasphere.dss.guide.server.entity.GuideContent;
import com.webank.wedatasphere.dss.guide.server.service.GuideContentService;
import com.webank.wedatasphere.dss.guide.server.util.GuideException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class GuideContentServiceImpl extends ServiceImpl<GuideContentMapper, GuideContent> implements GuideContentService {
    private GuideContentMapper guideContentMapper;

    @Override
    public boolean saveGuideContent(GuideContent guideContent) {
        Long id = guideContent.getId();

        if(id != null){
            return this.updateById(guideContent);
        }
        else {
            return this.save(guideContent);
        }
    }

    @Override
    public GuideContent getGuideContent(long id){
        return guideContentMapper.selectById(id);
    }

    @Override
    public List<GuideContent> queryGuideContentByPath(String path) {
        return guideContentMapper.getGuideContentListByPath(path);
    }


    @Override
    public void updateGuideContentById(long id, Map<String, Object> map) throws GuideException {
        Object content = map.get("content");
        if(content == null){
            throw new GuideException("Please set the content parameter (请设置content参数)");
        }
        Object contentHtml = map.get("contentHtml");
        if(contentHtml == null){
            throw new GuideException("Please set the contentHtml parameter (请设置contentHtml参数)");
        }
        guideContentMapper.updateGuideContentById(id, content.toString(), contentHtml.toString());
    }

    @Override
    public void deleteGuideContent(Long id) {
        this.removeById(id);
    }
}

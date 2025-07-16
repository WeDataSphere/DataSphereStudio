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
package com.webank.wedatasphere.dss.guide.server.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webank.wedatasphere.dss.guide.server.entity.GuideContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GuideContentMapper extends BaseMapper<GuideContent> {
    @Select("SELECT * FROM dss_guide_content WHERE path=#{path} ORDER BY type,seq")
    List<GuideContent> getGuideContentListByPath(@Param("path") String path);

    @Select("SELECT content FROM dss_guide_content WHERE id=#{id}")
    String getGuideContentById(@Param("id") long id);

    @Update("UPDATE dss_guide_content SET content =#{content},content_html =#{contentHtml} WHERE id =#{id}")
    void updateGuideContentById(@Param("id") long id, @Param("content") String content, @Param("contentHtml") String contentHtml);

    int batchInsert(@Param("list") List<GuideContent> list);

    void initContentId();

//    @Update("UPDATE dss_guide_content SET `is_delete` = 1,`update_time` = NOW() WHERE `id` = #{id}")
//    void deleteGuideContent(@Param("id") Long id);
}

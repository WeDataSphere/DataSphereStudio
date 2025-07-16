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
import com.webank.wedatasphere.dss.guide.server.entity.GuideChapter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import javax.ws.rs.QueryParam;
import java.util.List;

@Mapper
public interface GuideChapterMapper extends BaseMapper<GuideChapter> {
    @Select("SELECT * FROM dss_guide_chapter WHERE is_delete =0 AND catalog_id =#{catalogId} ORDER BY id ASC")
    List<GuideChapter> queryGuideChapterListByCatalogId(@Param("catalogId") Long catalogId);

    @Select("SELECT * FROM dss_guide_chapter WHERE is_delete =0 AND (content LIKE CONCAT('%', #{keyword}, '%') OR title LIKE CONCAT('%', #{keyword}, '%')) ORDER BY id ASC")
    List<GuideChapter> searchGuideChapterListByKeyword(@Param("keyword") String keyword);

    int batchInsert(@Param("list") List<GuideChapter> list);

    @Select("truncate table dss_guide_chapter;")
    void initChapterId();
}

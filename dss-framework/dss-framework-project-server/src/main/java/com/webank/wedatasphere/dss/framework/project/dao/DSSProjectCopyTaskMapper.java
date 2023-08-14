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

package com.webank.wedatasphere.dss.framework.project.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectCopyTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * created by v_wbzwchen  on 2020/12/16
 * Description:
 */
@Mapper
public interface DSSProjectCopyTaskMapper extends BaseMapper<DSSProjectCopyTask> {
    @Select("select * from dss_project_copy_task where status in (0, 1)")
    List<DSSProjectCopyTask> selectTaskByStatus();

    @Update("update dss_project_copy_task set surplus_count = (surplus_count - 1),status = #{status},update_time = now() where id = #{id}")
    public Integer updateCopyStatus(@Param("status") int status, @Param("id") Long id);

    @Update("update dss_project_copy_task set error_msg = concat(error_msg, ';', #{errorMsg}), error_orc = concat(error_orc, ';', #{errorOrc}), status = #{status}, update_time = now() where id = #{id}")
    public Integer updateErrorMsgById(@Param("errorMsg") String errorMsg, @Param("errorOrc") String errorOrc, @Param("status") int status, @Param("id") Long id);

    void batchUpdateCopyTask(@Param("failedJobs") List<DSSProjectCopyTask> failedJobs);

}



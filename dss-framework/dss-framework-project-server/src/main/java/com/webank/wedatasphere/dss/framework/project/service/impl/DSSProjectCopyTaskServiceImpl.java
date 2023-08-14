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

package com.webank.wedatasphere.dss.framework.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedatasphere.dss.framework.project.dao.DSSProjectCopyTaskMapper;
import com.webank.wedatasphere.dss.framework.project.entity.DSSProjectCopyTask;
import com.webank.wedatasphere.dss.framework.project.entity.request.ProjectCopyStatusRequest;
import com.webank.wedatasphere.dss.framework.project.service.DSSProjectCopyTaskService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @author v_wbzwchen
 * @since 2022-01-06
 */
@Service
public class DSSProjectCopyTaskServiceImpl extends ServiceImpl<DSSProjectCopyTaskMapper, DSSProjectCopyTask> implements DSSProjectCopyTaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DSSProjectCopyTaskServiceImpl.class);

    @Autowired
    private DSSProjectCopyTaskMapper projectCopyTaskMapper;

    @Override
    public DSSProjectCopyTask getCopyTaskInfo(ProjectCopyStatusRequest projectCopyStatusRequest) {
        QueryWrapper<DSSProjectCopyTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("workspace_id", projectCopyStatusRequest.getWorkspaceId());
        queryWrapper.eq("copy_project_id", projectCopyStatusRequest.getCopyProjectId());
        DSSProjectCopyTask dssProjectCopyTask = projectCopyTaskMapper.selectOne(queryWrapper);
        if (dssProjectCopyTask != null) {
            Integer status = dssProjectCopyTask.getStatus();
            if (status == 2) {
                if (dssProjectCopyTask.getErrorMsg() != null && dssProjectCopyTask.getErrorOrc() != null) {
                    // 数据库中以分号分割复制异常的编排
                    long count = Arrays.stream(dssProjectCopyTask.getErrorOrc().split(";")).filter(StringUtils::isNotBlank).count();
                    if (count == 0) {
                        LOGGER.info("复制完成，所有工作流复制成功！");
                        dssProjectCopyTask.setErrorMsg("复制完成，所有工作流复制成功！");
                    } else if (count == 1) {
                        LOGGER.error("复制完成，但以下" + count + "个工作流复制失败：" + dssProjectCopyTask.getErrorOrc().split(";")[1]);
                        dssProjectCopyTask.setErrorMsg("复制完成，但以下" + count + "个工作流复制失败：" + dssProjectCopyTask.getErrorOrc().split(";")[1]);
                    } else {
                        LOGGER.error("复制完成，但以下" + count + "个工作流复制失败：" + dssProjectCopyTask.getErrorOrc().substring(1));
                        dssProjectCopyTask.setErrorMsg("复制完成，但以下" + count + "个工作流复制失败：" + dssProjectCopyTask.getErrorOrc().substring(1));
                    }
                } else {
                    LOGGER.info("复制完成，所有工作流复制成功！");
                    dssProjectCopyTask.setErrorMsg("复制完成，所有工作流复制成功！");
                }
            } else if (status == 3) {
                LOGGER.info("系统打盹，请稍后重试！");
                dssProjectCopyTask.setErrorMsg("系统打盹，请稍后重试！");
            } else {
                dssProjectCopyTask.setErrorMsg("复制中");
            }
            return dssProjectCopyTask;
        }
        return null;
    }
}

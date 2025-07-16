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
package com.webank.wedatasphere.dss.common.utils;

import com.google.gson.Gson;
import com.webank.wedatasphere.dss.common.auditlog.OperateTypeEnum;
import com.webank.wedatasphere.dss.common.auditlog.TargetTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;

/**
 * 审计日志工具类
 */
public class AuditLogUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogUtils.class);

    /**
     * 打印审计日志，id类的属性都是String
     * @param user 执行操作的用户名
     * @param workspaceId 操作发生的工作空间id
     * @param workspaceName 操作发生的工作空间名称
     * @param targetType 操作针对的对象类型
     * @param targetId 操作针对的对象id
     * @param targetName 操作针对的对象名称
     * @param operateType 操作类型
     * @param params 操作相关的参数
     */
    public static void printLog(String user, String workspaceId, String workspaceName, TargetTypeEnum targetType,
                                String targetId, String targetName, OperateTypeEnum operateType,Object params) {
        String detailInfo=new Gson().toJson(params);
        LOGGER.info("[{}],[{}],[{}],[{}],[{}],[{}],[{}],[{}],[{}]",
                new Date(),user, workspaceId,workspaceName,targetType.getName(),
                targetId,targetName,operateType.getName(), detailInfo);
    }
    /**
     * 打印审计日志，id类的属性都是Long类型
     * @param user 执行操作的用户名
     * @param workspaceId 操作发生的工作空间id
     * @param workspaceName 操作发生的工作空间名称
     * @param targetType 操作针对的对象类型
     * @param targetId 操作针对的对象id
     * @param targetName 操作针对的对象名称
     * @param operateType 操作类型
     * @param params 操作相关的参数
     */
    public static void printLog(String user, long workspaceId, String workspaceName, TargetTypeEnum targetType,
                                long targetId, String targetName, OperateTypeEnum operateType,Object params) {
        printLog(user, String.valueOf(workspaceId), workspaceName, targetType,
                String.valueOf(targetId), targetName, operateType, params);
    }
}

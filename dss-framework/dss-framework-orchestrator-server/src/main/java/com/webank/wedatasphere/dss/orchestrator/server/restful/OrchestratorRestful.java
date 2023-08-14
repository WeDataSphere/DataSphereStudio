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

package com.webank.wedatasphere.dss.orchestrator.server.restful;

import com.webank.wedatasphere.dss.common.exception.DSSErrorException;
import com.webank.wedatasphere.dss.orchestrator.common.entity.ReleaseHistoryDetail;
import com.webank.wedatasphere.dss.orchestrator.common.entity.response.ExecutionHistoryVo;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestExecutionHistory;
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.OrchestratorCompareRequest;
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.ReleaseHistoryRequest;
import com.webank.wedatasphere.dss.orchestrator.server.entity.request.ReleaseUserRequest;
import com.webank.wedatasphere.dss.orchestrator.server.service.AppService;
import org.apache.commons.math3.util.Pair;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping(path = "/dss/framework/orchestrator", produces = {"application/json"})
public class OrchestratorRestful {
    private final static Logger LOGGER = LoggerFactory.getLogger(OrchestratorRestful.class);

    @Autowired
    AppService appService;
    @Autowired
    HttpServletRequest httpServletRequest;

    @RequestMapping(path = "/getExecutionHistory", method = RequestMethod.POST)
    public Message getExecutionHistory( @RequestBody RequestExecutionHistory requestExecutionHistory) {
        if (null == requestExecutionHistory.getOrchestratorId()) {
            LOGGER.error("Failed to get execution history, because orchestrator id is null!");
            return Message.error("请到父工作流查看执行历史信息！");
        }
        if (null != requestExecutionHistory.getAppId()) {
            Pair<Integer, List<ExecutionHistoryVo>> executionHistory =
                    appService.getExecutionHistory(requestExecutionHistory);
            Message message = null;
            if (null != executionHistory) {
                message = Message.ok("获取执行历史成功")
                        .data("executionHistory", executionHistory.getSecond())
                        .data("totalPage", executionHistory.getFirst());
            }else {
                LOGGER.warn("Query execution history is empty.");
            }
            return message;

        } else {
            LOGGER.error("工作流ID is empty.");
            Message message = Message.error("The workflow ID is empty.[工作流ID为空]");
            return message;
        }
    }

    /**
     * 获取发布历史
     */
    @RequestMapping(path = "/getReleaseHistory", method = RequestMethod.POST)
    public Message getReleaseHistory(@RequestBody ReleaseHistoryRequest releaseHistoryRequest) {
        if (null == releaseHistoryRequest.getOrchestratorId()) {
            LOGGER.error("Failed to get release history, because orchestrator id is null!");
            return Message.error("请到父工作流查看发布历史信息！");
        }
        Pair<Integer, List<ReleaseHistoryDetail>> releaseHistory = null;
        try {
            releaseHistory = appService.getReleaseHistory(releaseHistoryRequest);
        } catch (DSSErrorException e) {
            LOGGER.error("Failed to get release history, because {}", e);
            return Message.error("Orchestrator id is null![编排ID为空]");
        }
        return Message.ok("获取执行历史成功")
                .data("releaseDetails", releaseHistory.getSecond())
                .data("totalPage", releaseHistory.getFirst());
    }

    /**
     * 获取发布人
     */
    @RequestMapping(path = "/getReleaseUserList", method = RequestMethod.POST)
    public Message getReleaseUserList(@RequestBody ReleaseUserRequest requestReleaseUser) {
        if (null == requestReleaseUser.getOrchestratorId()) {
            LOGGER.error("Failed to get release history, because orchestrator id is null!");
            return Message.error("Orchestrator id is null![编排ID为空]");
        }
        List<String> userList = null;
        try {
            userList = appService.getReleaseUserList(requestReleaseUser);
        } catch (DSSErrorException e) {
            LOGGER.error("Failed to get release users, because {}", e);
            return Message.error("Orchestrator id is null![编排ID为空]");
        }
        Message message = Message.ok("获取发布人成功").data("releaseUserList",userList);
        return message;
    }


    /**
     * 获取编排版本
     */
    @RequestMapping(path = "/getOrchestratorVersionList",method = RequestMethod.POST)
    public Message getOrchestratorVersionList( @RequestBody ReleaseHistoryRequest releaseHistoryRequest) {
        if (null == releaseHistoryRequest.getOrchestratorId()) {
            LOGGER.error("Failed to get release history, because orchestrator id is null!");
            return Message.error("请到父工作流查看版本比对信息！");
        }
        Pair<Integer, List<ReleaseHistoryDetail>> releaseHistory = null;
        try {
            releaseHistory = appService.getOrchestratorVersionList(releaseHistoryRequest);
        } catch (DSSErrorException e) {
            LOGGER.error("Failed to get release history, because {}", e);
            return Message.error("Orchestrator id is null![编排ID为空]");
        }
        Message message = Message.ok("获取编排版本LIST成功")
                .data("releaseDetails", releaseHistory.getSecond())
                .data("totalPage", releaseHistory.getFirst());
        return message;
    }

    /**
     * 获取编排版本创建人
     */
    @RequestMapping(path = "/getOrchestratorVersionUserList",method = RequestMethod.POST)
    public Message getOrchestratorVersionUserList(@RequestBody ReleaseUserRequest requestReleaseUser) {
        if (null == requestReleaseUser.getOrchestratorId()) {
            LOGGER.error("Failed to get release history, because orchestrator id is null!");
            return Message.error("请到父工作流查看版本比对信息！");
        }
        List<String> userList = null;
        try {
            userList = appService.getOrchestratorVersionUserList(requestReleaseUser.getOrchestratorId());
        } catch (DSSErrorException e) {
            LOGGER.error("Failed to get release users, because {}", e);
            return Message.error("Orchestrator id is null![编排ID为空]");
        }
        Message message = Message.ok("获取编排版本创建人成功").data("releaseUserList", userList);
        return message;
    }

    /**
     * 比较两个工作流
     */
    @RequestMapping(path = "/compareOrchestrator",method = RequestMethod.POST)
    public Message compareOrchestrator(@RequestBody OrchestratorCompareRequest orchestratorCompareRequest) {
        String username = SecurityFilter.getLoginUsername(httpServletRequest);
        LOGGER.info("user {} try to compareOrchestrator, the request params is:{}", username, orchestratorCompareRequest);
        if (null == orchestratorCompareRequest.getSecondVersionId()) {
            return Message.error("第一个版本ID不能为空");
        }
        Message message = Message.ok("比较两个编排模式成功");
        try {
            message.data("list", appService.compareOrchestrator(orchestratorCompareRequest));
        } catch (DSSErrorException e) {
            LOGGER.error("Failed to get compare workflows, because {}", e);
            return Message.error("比较两个编排模式报错："+e.getMessage());
        }
        return message;
    }
}

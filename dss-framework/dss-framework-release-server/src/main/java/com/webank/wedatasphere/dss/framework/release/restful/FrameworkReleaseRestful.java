/*
 *
 *  * Copyright 2019 WeBank
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.webank.wedatasphere.dss.framework.release.restful;

import com.webank.wedatasphere.dss.common.label.EnvDSSLabel;
import com.webank.wedatasphere.dss.framework.release.conf.ReleaseConstant;
import com.webank.wedatasphere.dss.framework.release.entity.request.ReleaseOrchestratorRequest;
import com.webank.wedatasphere.dss.framework.release.service.ReleaseService;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.RequestReleaseOrchestration;
import com.webank.wedatasphere.dss.orchestrator.common.protocol.ResponseConvertOrchestrator;
import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.sso.utils.SSOHelper;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.security.SecurityFilter;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * created by cooperyang on 2020/11/17
 * Description:
 */
@RestController
@RequestMapping(path = "/dss/framework/release", produces = {"application/json"})
public class FrameworkReleaseRestful {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkReleaseRestful.class);

    @Autowired
    private ReleaseService releaseService;
    @Autowired
    private HttpServletRequest request;

    @PostMapping(path = "/releaseOrchestrator")
    public Message releaseOrchestrator(@Valid @RequestBody ReleaseOrchestratorRequest releaseOrchestratorRequest) {
        String username = SecurityFilter.getLoginUsername(request);
        Workspace workspace = SSOHelper.getWorkspace(request);
        //需要是appID,而不是编排ID
        Long orchestratorId = releaseOrchestratorRequest.getOrchestratorId();
        Long orchestratorVersionId = releaseOrchestratorRequest.getOrchestratorVersionId();
        String comment = releaseOrchestratorRequest.getComment();
        String dssLabel = releaseOrchestratorRequest.getDssLabel();

        Map<String, Object> labels = new HashMap<>();
        labels.put(EnvDSSLabel.DSS_ENV_LABEL_KEY, dssLabel);

        try {
            RequestReleaseOrchestration requestReleaseOrchestration = new RequestReleaseOrchestration();
            requestReleaseOrchestration.setUserName(username);
            requestReleaseOrchestration.setOrcAppId(orchestratorId);
            requestReleaseOrchestration.setLabels(labels);
            requestReleaseOrchestration.setWorkspace(workspace);
            requestReleaseOrchestration.setComment(comment);
            ResponseConvertOrchestrator releaseTaskId = releaseService.releaseOrchestrator(requestReleaseOrchestration);
            LOGGER.info("Succeed to submit orcId {}, orcVersionId {} to release server, and taskId is {}",
                    orchestratorId, orchestratorVersionId, releaseTaskId.getId());
            return Message.ok("成功提交发布任务").data("releaseTaskId", releaseTaskId.getId());
        } catch (final Exception e) {
            LOGGER.error("Failed to release orchestrator id : {}, orcVersionId : {} for user {}",
                    orchestratorId, orchestratorVersionId, username, e);
            return Message.error("提交发布任务失败");
        }
    }
    @GetMapping(path = "/getReleaseStatus")
    public Message getReleaseStatus(HttpServletRequest request,
                                     @NotNull(message = ReleaseConstant.DSSLABEL_NOT_NULL) @RequestParam("dssLabel") String dssLabel,
                                     @NotNull(message = "查询的发布id不能为空") @RequestParam("releaseTaskId") Long releaseTaskId) {
        try {
            ResponseConvertOrchestrator releaseStatus = releaseService.getStatus(String.valueOf(releaseTaskId));
            if (null == releaseStatus) {
                LOGGER.error("get releaseStatus is null for id {}", releaseTaskId);
                return Message.error("获取发布进度失败");
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("get {} status ok {} ", releaseTaskId, releaseStatus.getResponse().getJobStatus());
            }
            return Message.ok("获取任务进度成功")
                    .data("status", releaseStatus.getResponse().getJobStatus())
                    .data("errorMsg", releaseStatus.getResponse().getMessage());
        } catch (final Exception e) {
            LOGGER.error("Failed to get release status for {}", releaseTaskId, e);
            return Message.error("获取发布进度失败");
        }
    }

//    /**
//     * release batch是为了进行批量发布编排模式
//     */
//    @PostMapping(path = "/releaseBatch")
//    public Message releaseBatch(HttpServletRequest request, @Valid @RequestBody ReleaseBatchRequest releaseBatchRequest) {
//        return null;
//    }

//    /**
//     * 临时构建标准labe map
//     */
//    private Map<String, Object> consltructLabelMap(String label) {
//        HashMap<String, Object> labelMap = new HashMap<>();
//        labelMap.put("route", label);
//        return labelMap;
//    }

}

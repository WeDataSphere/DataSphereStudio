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

package com.webank.wedatasphere.dss.framework.appconn.restful;

import com.webank.wedatasphere.dss.appconn.core.AppConn;
import com.webank.wedatasphere.dss.appconn.manager.AppConnManager;
import com.webank.wedatasphere.dss.appconn.manager.conf.AppConnManagerCoreConf;
import com.webank.wedatasphere.dss.appconn.manager.entity.AppConnInfo;
import com.webank.wedatasphere.dss.appconn.manager.entity.AppInstanceInfo;
import com.webank.wedatasphere.dss.appconn.manager.service.AppConnInfoService;
import com.webank.wedatasphere.dss.appconn.manager.utils.AppConnManagerUtils;
import com.webank.wedatasphere.dss.common.utils.DSSExceptionUtils;
import com.webank.wedatasphere.dss.framework.appconn.entity.AppConnBean;
import com.webank.wedatasphere.dss.framework.appconn.entity.AppInstanceBean;
import com.webank.wedatasphere.dss.framework.appconn.service.AppConnQualityChecker;
import com.webank.wedatasphere.dss.framework.appconn.service.AppConnResourceUploadService;
import com.webank.wedatasphere.dss.framework.appconn.service.AppConnService;
import com.webank.wedatasphere.dss.sender.service.conf.DSSSenderServiceConf;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.server.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static com.webank.wedatasphere.dss.framework.appconn.conf.AppConnConf.APPCONN_UPLOAD_THREAD_NUM;

@RequestMapping(path = "/dss/framework/project/appconn", produces = {"application/json"})
@RestController
public class AppConnManagerRestfulApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConnManagerRestfulApi.class);

    @Autowired
    private AppConnInfoService appConnInfoService;
    @Autowired
    private AppConnResourceUploadService appConnResourceUploadService;
    @Autowired
    private List<AppConnQualityChecker> appConnQualityCheckers;
    @Autowired
    private AppConnService appConnService;

    private ExecutorService uploadThreadPool = Utils.newFixedThreadPool(APPCONN_UPLOAD_THREAD_NUM.getValue(), "Upload-Appconn-Thread-", false);


    @PostConstruct
    public void init() throws InterruptedException {
        //仅dss-server-dev的其中一个服务需要作为appconn-manager节点上传appconn包，其他服务都是client端
        if ("dss-server-dev".equals(DSSSenderServiceConf.CURRENT_DSS_SERVER_NAME.getValue())) {
            LOGGER.info("First, try to load all AppConn...");
            AppConnManager.getAppConnManager().listAppConns().forEach(appConn -> {
                LOGGER.info("Try to check the quality of AppConn {}.", appConn.getAppDesc().getAppName());
                appConnQualityCheckers.forEach(DSSExceptionUtils.handling(checker -> checker.checkQuality(appConn)));
            });
            LOGGER.info("All AppConn have loaded successfully.");
        } else {
            LOGGER.info("Not appConn manager, will not scan plugins.");
            AppConnManagerUtils.autoLoadAppConnManager();
        }
    }

    @RequestMapping(path = "listAppConnInfos", method = RequestMethod.GET)
    public Message listAppConnInfos() {
        List<? extends AppConnInfo> appConnInfos = appConnInfoService.getAppConnInfos();
        Message message = Message.ok("Get AppConnInfo list succeed.");
        message.data("appConnInfos", appConnInfos);
        return message;
    }

    @RequestMapping(path = "{appConnName}/get", method = RequestMethod.GET)
    public Message get(@PathVariable("appConnName") String appConnName) {
        LOGGER.info("try to get appconn info:{}.", appConnName);
        AppConnInfo appConnInfo = appConnInfoService.getAppConnInfo(appConnName);
        Message message = Message.ok("Get AppConnInfo succeed.");
        message.data("appConnInfo", appConnInfo);
        return message;
    }

    @RequestMapping(path = "{appConnName}/getAppInstances", method = RequestMethod.GET)
    public Message getAppInstancesByAppConnInfo(@PathVariable("appConnName") String appConnName) {
        LOGGER.debug("try to get instances for appconn: {}.", appConnName);
        List<? extends AppInstanceInfo> appInstanceInfos = appConnInfoService.getAppInstancesByAppConnName(appConnName);
        Message message = Message.ok("Get AppInstance list succeed.");
        message.data("appInstanceInfos", appInstanceInfos);
        return message;
    }

    @RequestMapping(path = "{appConnName}/load", method = RequestMethod.GET)
    public Message load(@PathVariable("appConnName") String appConnName) {
        if (!Objects.equals(AppConnManagerCoreConf.IS_APPCONN_MANAGER.getValue(), AppConnManagerCoreConf.hostname)){
            return Message.error("not appconn manager node,please try again");
        }
        LOGGER.info("Try to reload AppConn {}.", appConnName);
        try {
            LOGGER.info("First, reload AppConn {}.", appConnName);
            AppConnManager.getAppConnManager().reloadAppConn(appConnName);
            AppConn appConn = AppConnManager.getAppConnManager().getAppConn(appConnName);
            LOGGER.info("Second, check the quality of AppConn {}.", appConnName);
            appConnQualityCheckers.forEach(DSSExceptionUtils.handling(checker -> checker.checkQuality(appConn)));
            LOGGER.info("Last, upload AppConn {} resources.", appConnName);
            appConnResourceUploadService.upload(appConnName);
        } catch (Exception e) {
            LOGGER.error("Load AppConn " + appConnName + " failed.", e);
            return Message.error("Load AppConn " + appConnName + " failed. Reason: " + ExceptionUtils.getRootCauseMessage(e));
        }
        return Message.ok("Load AppConn " + appConnName + " succeed.");
    }


    @RequestMapping(path = "/getAppConns", method = RequestMethod.GET)
    public Message getAppConns(@RequestParam(defaultValue = "1") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String appConnName,
                               @RequestParam(required = false) String className) {
        List<AppConnBean> appConnInfos;
        try {

            appConnInfos = appConnService.getAppConns(appConnName, className, page, size);
        } catch (Exception e) {
            LOGGER.error("Get AppConn list failed.", e);
            return Message.error("Get AppConn list failed. Reason: " + ExceptionUtils.getRootCauseMessage(e));
        }
        Message message = Message.ok("Get AppConn list succeed.");
        message.data("appConnInfos", appConnInfos);
        return message;
    }

    @RequestMapping(path = "/getAppConnsName", method = RequestMethod.GET)
    public Message getAppConnsName() {
        List<String> appConnsName;
        try {
            appConnsName = appConnService.getAppConnsName();
        } catch (Exception e) {
            LOGGER.error("Get all AppConn name failed.", e);
            return Message.error("Get all AppConn namefailed. Reason: " + ExceptionUtils.getRootCauseMessage(e));
        }
        Message message = Message.ok("Get all AppConn name succeed.");
        message.data("appConnsName", appConnsName);
        return message;
    }
    @RequestMapping(path = "/deleteAppConn", method = RequestMethod.POST)
    public Message deleteAppConn(@RequestParam Long id) {
        try {
            appConnService.deleteAppConn(id);
        } catch (Exception e) {
            LOGGER.error("Delete AppConn failed.", e);
            return Message.error("Delete AppConn failed. Reason: " + ExceptionUtils.getRootCauseMessage(e));
        }
        return Message.ok("Delete AppConn succeed.");
    }

    @RequestMapping(path = "/addAppConn", method = RequestMethod.POST)
    public Message addAppConn(@RequestBody AppConnBean appConnBean) {
        checkParams(appConnBean);
        AppConnBean rerurnAppConnBean;
        try {
            rerurnAppConnBean = appConnService.addAppConn(appConnBean);
        } catch (Exception e) {
            LOGGER.error("Add AppConn failed.", e);
            return Message.error("Add AppConn failed. Reason: " + ExceptionUtils.getRootCauseMessage(e));
        }
        Message message = Message.ok("Add AppConn succeed.");
        message.data("appConnInfo", rerurnAppConnBean);
        return message;
    }

    @RequestMapping(path = "/editAppConn", method = RequestMethod.POST)
    public Message editAppConn(@RequestBody AppConnBean appConnBean) {
        checkParams(appConnBean);
        AppConnBean rerurnAppConnBean;
        try {
            rerurnAppConnBean = appConnService.updateAppConn(appConnBean);
        } catch (Exception e) {
            LOGGER.error("Update AppConn failed.", e);
            return Message.error("Update AppConn failed. Reason: " + ExceptionUtils.getRootCauseMessage(e));
        }
        Message message = Message.ok("Update AppConn succeed.");
        message.data("appConnInfo", rerurnAppConnBean);
        return message;
    }

    private Message checkParams (AppConnBean appConnBean) {

    }

}

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wedatasphere.dss.appconn.core.AppConn;
import com.webank.wedatasphere.dss.appconn.manager.AppConnManager;
import com.webank.wedatasphere.dss.appconn.manager.conf.AppConnManagerCoreConf;
import com.webank.wedatasphere.dss.appconn.manager.entity.AppConnInfo;
import com.webank.wedatasphere.dss.appconn.manager.entity.AppInstanceInfo;
import com.webank.wedatasphere.dss.appconn.manager.service.AppConnInfoService;
import com.webank.wedatasphere.dss.appconn.manager.utils.AppConnManagerUtils;
import com.webank.wedatasphere.dss.common.auditlog.OperateTypeEnum;
import com.webank.wedatasphere.dss.common.auditlog.TargetTypeEnum;
import com.webank.wedatasphere.dss.common.utils.AuditLogUtils;
import com.webank.wedatasphere.dss.common.utils.DSSExceptionUtils;
import com.webank.wedatasphere.dss.framework.appconn.common.ResourceTypeEnum;
import com.webank.wedatasphere.dss.framework.appconn.entity.AppConnBean;
import com.webank.wedatasphere.dss.framework.appconn.entity.AppInstanceBean;
import com.webank.wedatasphere.dss.framework.appconn.service.AppConnQualityChecker;
import com.webank.wedatasphere.dss.framework.appconn.service.AppConnResourceUploadService;
import com.webank.wedatasphere.dss.framework.appconn.service.AppConnService;
import com.webank.wedatasphere.dss.framework.appconn.service.AppInstanceService;
import com.webank.wedatasphere.dss.sender.service.conf.DSSSenderServiceConf;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.server.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @Autowired
    private AppInstanceService appInstanceService;

    private ExecutorService uploadThreadPool = Utils.newFixedThreadPool(APPCONN_UPLOAD_THREAD_NUM.getValue(), "Upload-Appconn-Thread-", false);


    @PostConstruct
    public void init() throws InterruptedException {
        //仅dss-server-dev的其中一个服务需要作为appconn-manager节点上传appconn包，其他服务都是client端
        if ("dss-server-dev".equals(DSSSenderServiceConf.CURRENT_DSS_SERVER_NAME.getValue())) {
            LOGGER.info("First, try to load all AppConn...");
            AppConnManager.getAppConnManager().listAppConns().forEach(appConn -> {
                LOGGER.info("Try to check the quality of AppConn {}.", appConn.getAppDesc().getAppName());
                try {
                    appConnQualityCheckers.forEach(DSSExceptionUtils.handling(checker -> checker.checkQuality(appConn)));
                } catch (Exception e) {
                    LOGGER.error("Failed to check the quality of AppConn {}.", appConn.getAppDesc().getAppName(), e);
                }
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
//            AppConnManager.getAppConnManager().reloadAppConn(appConnName);
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
    public Message getAppConns(@RequestParam(required = false) String appConnName,
                               @RequestParam(required = false) String className) {
        List<AppConnBean> appConnInfos;
        try {
            appConnInfos = appConnService.getAppConns(appConnName, className);
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
    public Message deleteAppConn(@RequestParam Long id) throws Exception{
        try {
            appConnService.deleteAppConn(id);
        } catch (Exception e) {
            LOGGER.error("Delete AppConn failed.", e);
            return Message.error("Delete AppConn failed. Reason: " + ExceptionUtils.getRootCauseMessage(e));
        }
        AuditLogUtils.printLog(null ,null, null, TargetTypeEnum.APPCONN, id.toString(), "AppConn", OperateTypeEnum.DELETE, id);
        return Message.ok("Delete AppConn succeed.");
    }

    @RequestMapping(path = "/addAppConn", method = RequestMethod.POST)
    public Message addAppConn(@RequestBody AppConnBean appConnBean) {
        Message checkMessage = checkParams(appConnBean);
        if (checkMessage.getStatus() == 1) {
            return Message.error(checkMessage.getMessage());
        }
        AppConnBean returnAppConnBean;
        try {
            returnAppConnBean = appConnService.addAppConn(appConnBean);
        } catch (Exception e) {
            LOGGER.error("Add AppConn failed.", e);
            return Message.error("Add AppConn failed. Reason: " + ExceptionUtils.getRootCauseMessage(e));
        }
        AuditLogUtils.printLog(null ,null, null, TargetTypeEnum.APPCONN, returnAppConnBean.getId().toString(), "AppConn", OperateTypeEnum.CREATE, appConnBean);
        Message message = Message.ok("Add AppConn succeed.");
        message.data("appConnInfo", returnAppConnBean);
        return message;
    }

    @RequestMapping(path = "/editAppConn", method = RequestMethod.POST)
    public Message editAppConn(@RequestBody AppConnBean appConnBean) {
        Message checkMessage = checkParams(appConnBean);
        if (checkMessage.getStatus() == 1) {
            return Message.error(checkMessage.getMessage());
        }
        AppConnBean returnAppConnBean;
        try {
            returnAppConnBean = appConnService.updateAppConn(appConnBean);
        } catch (Exception e) {
            LOGGER.error("Update AppConn failed.", e);
            return Message.error("Update AppConn failed. Reason: " + ExceptionUtils.getRootCauseMessage(e));
        }
        AuditLogUtils.printLog(null ,null, null, TargetTypeEnum.APPCONN, returnAppConnBean.getId().toString(), "AppConn", OperateTypeEnum.UPDATE, appConnBean);
        Message message = Message.ok("Update AppConn succeed.");
        message.data("appConnInfo", returnAppConnBean);
        return message;
    }

    @RequestMapping(path = "/uploadAppConnResource", method = RequestMethod.POST)
    public Message uploadAppConnResource(@RequestParam MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.endsWith(".zip")) {
            return Message.error("The file extension must be .zip.");
        }
        String resource;
        try {
            resource = appConnResourceUploadService.upload(file);
        } catch (Exception e) {
            LOGGER.error("Upload AppConn resource failed.", e);
            return Message.error("Upload AppConn resource failed. Reason: " + ExceptionUtils.getRootCauseMessage(e));
        }
        Message message = Message.ok("Upload AppConn resource succeed.");
        message.data("resource", resource);
        return message;
    }

    @RequestMapping(path = "/addAppConnInstance", method = RequestMethod.POST)
    public Message addAppConnInstance(@RequestBody AppInstanceBean appInstanceBean) {
        Message checkMessage = checkInstanceParams(appInstanceBean);
        if (checkMessage.getStatus() == 1) {
            return Message.error(checkMessage.getMessage());
        }
        AppInstanceBean returnAppConnInstance;
        try {
            List<AppInstanceBean> appInstancesByAppConnId = appInstanceService.getAppInstancesByAppConnId(appInstanceBean.getAppConnId());
            boolean b = appInstancesByAppConnId.stream().map(AppInstanceBean::getLabel).anyMatch(appInstanceBean.getLabel()::equals);
            if (b) {
                return Message.error("There have same label of AppInstance, please modify.");
            }
            returnAppConnInstance = appInstanceService.addAppInstance(appInstanceBean);
        } catch (Exception e) {
            LOGGER.error("Add AppConn instance failed.", e);
            return Message.error("Add AppConn instance failed. Reason: " + ExceptionUtils.getRootCauseMessage(e));
        }
        AuditLogUtils.printLog(null ,null, null, TargetTypeEnum.APPCONN, returnAppConnInstance.getId().toString(), "AppConnInstance", OperateTypeEnum.CREATE, returnAppConnInstance);
        Message message = Message.ok("Add AppConn instance succeed.");
        message.data("appConnInstance", returnAppConnInstance);
        return message;
    }

    @RequestMapping(path = "/editAppConnInstance", method = RequestMethod.POST)
    public Message editAppConnInstance(@RequestBody AppInstanceBean appInstanceBean) {
        Message checkMessage = checkInstanceParams(appInstanceBean);
        if (checkMessage.getStatus() == 1) {
            return Message.error(checkMessage.getMessage());
        }
        AppInstanceBean returnAppConnInstance;
        try {
            returnAppConnInstance = appInstanceService.updateAppInstance(appInstanceBean);
        } catch (Exception e) {
            LOGGER.error("Update AppConn instance failed.", e);
            return Message.error("Update AppConn instance failed. Reason: " + ExceptionUtils.getRootCauseMessage(e));
        }
        AuditLogUtils.printLog(null ,null, null, TargetTypeEnum.APPCONN, returnAppConnInstance.getId().toString(), "AppConnInstance", OperateTypeEnum.UPDATE, returnAppConnInstance);
        Message message = Message.ok("Update AppConn instance succeed.");
        message.data("appConnInstance", returnAppConnInstance);
        return message;
    }

    @RequestMapping(path = "/deleteAppConnInstance", method = RequestMethod.POST)
    public Message deleteAppConnInstance(@RequestParam Long id) {
        try {
            appInstanceService.deleteAppInstance(id);
        } catch (Exception e) {
            LOGGER.error("Delete AppConn instance failed.", e);
            return Message.error("Delete AppConn instance failed. Reason: " + ExceptionUtils.getRootCauseMessage(e));
        }
        AuditLogUtils.printLog(null ,null, null, TargetTypeEnum.APPCONN, id.toString(), "AppConnInstance", OperateTypeEnum.DELETE, id);
        return Message.ok("Delete AppConn instance succeed.");
    }

    @RequestMapping(path = "/getAppConnInstances", method = RequestMethod.GET)
    public Message getAppConnInstances(@RequestParam Long appConnId) {
        List<AppInstanceBean> appConnInstances;
        try {
            appConnInstances = appInstanceService.getAppInstancesByAppConnId(appConnId);
        } catch (Exception e) {
            LOGGER.error("Get AppConn instance list failed.", e);
            return Message.error("Get AppConn instance list failed. Reason: " + ExceptionUtils.getRootCauseMessage(e));
        }
        Message message = Message.ok("Get AppConn instance list succeed.");
        message.data("appConnInstances", appConnInstances);
        return message;
    }

    private Message checkParams (AppConnBean appConnBean) {
        Message message = new Message();
        if (StringUtils.isBlank(appConnBean.getAppConnName())) {
            message.setStatus(1);
            message.setMessage("AppConn name can not be empty.");
            return message;
        }
        if (appConnBean.getAppConnName().length() > 64) {
            message.setStatus(1);
            message.setMessage("AppConn name cannot exceed 64 characters.");
            return message;
        }
//        if (!appConnBean.getAppConnName().matches("^[a-zA-Z]+$")) {
//            message.setStatus(1);
//            message.setMessage("AppConn name can only contain letters.");
//            return message;
//        }
        if (StringUtils.isBlank(appConnBean.getIsUserNeedInit())) {
            message.setStatus(1);
            message.setMessage("AppConn isUserNeedInit can not be empty.");
            return message;
        }

        if (appConnBean.getIfIframe() == null) {
            message.setStatus(1);
            message.setMessage("AppConn ifIframe can not be empty.");
            return message;
        }

        if (appConnBean.getIsMicroApp() == null) {
            message.setStatus(1);
            message.setMessage("AppConn isMicroApp can not be empty.");
            return message;
        }

        if (appConnBean.getIsExternal() == null) {
            message.setStatus(1);
            message.setMessage("AppConn isExternal can not be empty.");
            return message;
        }

        if (appConnBean.getResourceFetchMethod().equals(ResourceTypeEnum.RELATED.getName())) {
            if (StringUtils.isNotBlank(appConnBean.getResource())) {
                message.setStatus(1);
                message.setMessage("Resource can not be set when resource fetch method is related.");
                return message;
            }
            if (StringUtils.isBlank(appConnBean.getReference())) {
                message.setStatus(1);
                message.setMessage("Reference can not be null when resource fetch method is related.");
                return message;
            }
        }
        if (appConnBean.getResourceFetchMethod().equals(ResourceTypeEnum.UPLOAD.getName())) {
            if (StringUtils.isBlank(appConnBean.getResource())) {
                message.setStatus(1);
                message.setMessage("Resource can not be null when resource fetch method is upload.");
                return message;
            }
            if (StringUtils.isNotBlank(appConnBean.getReference())) {
                message.setStatus(1);
                message.setMessage("Reference can not be set when resource fetch method is upload.");
                return message;
            }
        }
        return message;
    }

    private Message checkInstanceParams (AppInstanceBean appInstanceBean) {
        Message message = new Message();
        if (StringUtils.isBlank(appInstanceBean.getUrl())) {
            message.setStatus(1);
            message.setMessage("Url can not be null.");
            return message;
        }

        if (StringUtils.isBlank(appInstanceBean.getLabel())) {
            message.setStatus(1);
            message.setMessage("Label can not be null.");
            return message;
        }
        //属性label只能是DEV或者PROD
        if (!"DEV".equals(appInstanceBean.getLabel()) && !"PROD".equals(appInstanceBean.getLabel())) {
            message.setStatus(1);
            message.setMessage("Label can only be DEV or PROD.");
            return message;
        }

        //属性enhanceJson可以为空，如果不为空就必须是JSON格式字符串
        if (StringUtils.isNotBlank(appInstanceBean.getEnhanceJson()) && !isValidJson(appInstanceBean.getEnhanceJson())) {
            message.setStatus(1);
            message.setMessage("EnhanceJson format is incorrect.");
            return message;
        }
        return message;
    }

    private boolean isValidJson(String jsonInString) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

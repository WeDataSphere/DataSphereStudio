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

package com.webank.wedatasphere.dss.framework.appconn.service.impl;

import com.webank.wedatasphere.dss.appconn.manager.AppConnManager;
import com.webank.wedatasphere.dss.appconn.manager.entity.AppConnInfo;
import com.webank.wedatasphere.dss.appconn.manager.entity.AppInstanceInfo;
import com.webank.wedatasphere.dss.appconn.manager.service.AppConnInfoService;
import com.webank.wedatasphere.dss.framework.appconn.common.ResourceTypeEnum;
import com.webank.wedatasphere.dss.framework.appconn.conf.AppConnConf;
import com.webank.wedatasphere.dss.framework.appconn.dao.AppConnMapper;
import com.webank.wedatasphere.dss.framework.appconn.entity.AppConnBean;
import com.webank.wedatasphere.dss.framework.appconn.exception.AppConnDeleteErrorException;
import com.webank.wedatasphere.dss.framework.appconn.service.*;
import com.webank.wedatasphere.dss.framework.appconn.utils.AppConnServiceUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppConnInfoServiceImpl implements AppConnInfoService, AppConnService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppConnInfoServiceImpl.class);
    @Autowired
    private AppConnMapper appConnMapper;
    @Autowired
    private List<AppConnQualityChecker> appConnQualityCheckers;
    @Autowired
    private AppInstanceService appInstanceService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private AppConnMenuService appConnMenuService;

    @Override
    public List<? extends AppConnInfo> getAppConnInfos() {
        List<AppConnBean> appConnBeans = appConnMapper.getAllAppConnBeans().stream()
                .filter(appConnBean -> !AppConnConf.DISABLED_APP_CONNS.contains(appConnBean.getAppConnName()))
                .collect(Collectors.toList());
        appConnBeans.forEach(appConnBean -> {
            String resource = appConnBean.getResource();
            if(StringUtils.isNotBlank(resource)) {
                appConnBean.setAppConnResource(AppConnServiceUtils.stringToResource(resource).getResource());
            }
        });
        return appConnBeans;
    }

    @Override
    public AppConnInfo getAppConnInfo(String appConnName) {
        return appConnMapper.getAppConnBeanByName(appConnName);
    }

    @Override
    public List<? extends AppInstanceInfo> getAppInstancesByAppConnInfo(AppConnInfo appConnInfo) {
        Long id = ((AppConnBean) appConnInfo).getId();
        return appInstanceService.getAppInstancesByAppConnId(id);
    }

    @Override
    public List<? extends AppInstanceInfo> getAppInstancesByAppConnName(String appConnName) {
        AppConnBean appConnBean = appConnMapper.getAppConnBeanByName(appConnName);
        return getAppInstancesByAppConnInfo(appConnBean);
    }

    @Override
    public List<AppConnBean> getAppConns(String appConnName, String className) {
        List<AppConnBean> appConnBeanList =  appConnMapper.getAppConns(appConnName, className);
        appConnBeanList.forEach(appConnBean -> {
            String resource = appConnBean.getResource();
            if(StringUtils.isNotBlank(resource)) {
                appConnBean.setAppConnResource(AppConnServiceUtils.stringToResource(resource).getResource());
            }
            if (StringUtils.isNotBlank(appConnBean.getReference())) {
                appConnBean.setResourceFetchMethod(ResourceTypeEnum.RELATED.getName());
            } else {
                appConnBean.setResourceFetchMethod(ResourceTypeEnum.UPLOAD.getName());
            }
        });

        return appConnBeanList;
    }

    @Override
    public AppConnBean addAppConn(AppConnBean appConnBean) {
        LOGGER.info("Try to reload AppConn {}.", appConnBean.getAppConnName());
        LOGGER.info("First, reload AppConn {}.", appConnBean.getAppConnName());
        AppConnManager.getAppConnManager().reloadAppConn(appConnBean);
//        AppConn appConn = AppConnManager.getAppConnManager().getAppConn(appConnBean.getAppConnName());
//        LOGGER.info("Second, check the quality of AppConn {}.", appConnBean.getAppConnName());
//        appConnQualityCheckers.forEach(DSSExceptionUtils.handling(checker -> checker.checkQuality(appConn)));
        LOGGER.info("Last, add AppConn {} to db.", appConnBean.getAppConnName());
        appConnMapper.addAppConn(appConnBean);
        AppConnBean appConnBeanById = appConnMapper.getAppConnBeanById(appConnBean.getId());
        appConnBeanById.setResourceFetchMethod(appConnBeanById.getResourceFetchMethod());
        return appConnBeanById;
    }

    @Override
    public AppConnBean updateAppConn(AppConnBean appConnBean) {
        //查看resource是否发生变化，获取该appConn在数据库信息，如果resource为空且新上传的appConnBean的resource也为空，那么就只需要更新数据库，
        //如果二者的resource都不为空且不相同，那么就需要reload appconn，否则不用reload
        AppConnBean appConnBeanById = appConnMapper.getAppConnBeanById(appConnBean.getId());
        if (StringUtils.isBlank(appConnBeanById.getResource())) {
            if (StringUtils.isNotBlank(appConnBean.getResource())) {
                AppConnManager.getAppConnManager().reloadAppConn(appConnBean);
                appConnMapper.updateAppConn(appConnBean);
                appConnBean.setResourceFetchMethod(ResourceTypeEnum.UPLOAD.getName());
            } else {
                appConnMapper.updateAppConn(appConnBean);
            }
        } else {
            if (StringUtils.isNotBlank(appConnBean.getResource())) {
                if (!appConnBeanById.getResource().equals(appConnBean.getResource())) {
                    AppConnManager.getAppConnManager().reloadAppConn(appConnBean);
                }
                appConnMapper.updateAppConn(appConnBean);
            } else {
                appConnMapper.updateAppConn(appConnBean);
                appConnBean.setResourceFetchMethod(ResourceTypeEnum.RELATED.getName());
            }
        }
        return appConnBean;
    }

    @Override
    public void deleteAppConn(Long appConnId) throws Exception{
        //校验该AppConn是否存在关键的实例和节点，如果存在就不允许删除
        //不存在就删除该AppConn和其菜单
        AppConnBean appConnBeanById = appConnMapper.getAppConnBeanById(appConnId);
        String appConnName = appConnBeanById.getAppConnName();
        if (CollectionUtils.isNotEmpty(appInstanceService.getAppInstancesByAppConnId(appConnId))) {
            throw new AppConnDeleteErrorException(20353, "该AppConn存在关键实例，不允许删除");
        }
        if (CollectionUtils.isNotEmpty(nodeService.getNodesByAppconnName(appConnName))) {
            throw new AppConnDeleteErrorException(20353, "该AppConn存在关键节点，不允许删除");
        }
        //删除AppConn菜单
        appConnMenuService.deleteMenusByAppconnId(appConnId.intValue());
        //删除AppConn
        appConnMapper.deleteAppConn(appConnId);
    }

    @Override
    public List<String> getAppConnsName() {
        return appConnMapper.getAllAppConnsName();
    }

}

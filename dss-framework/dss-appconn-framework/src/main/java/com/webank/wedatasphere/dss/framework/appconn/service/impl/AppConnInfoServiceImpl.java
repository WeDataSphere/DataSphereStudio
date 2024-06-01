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

import com.webank.wedatasphere.dss.appconn.manager.entity.AppConnInfo;
import com.webank.wedatasphere.dss.appconn.manager.entity.AppInstanceInfo;
import com.webank.wedatasphere.dss.appconn.manager.service.AppConnInfoService;
import com.webank.wedatasphere.dss.framework.appconn.conf.AppConnConf;
import com.webank.wedatasphere.dss.framework.appconn.dao.AppConnMapper;
import com.webank.wedatasphere.dss.framework.appconn.dao.AppInstanceMapper;
import com.webank.wedatasphere.dss.framework.appconn.entity.AppConnBean;
import com.webank.wedatasphere.dss.framework.appconn.service.AppConnService;
import com.webank.wedatasphere.dss.framework.appconn.utils.AppConnServiceUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppConnInfoServiceImpl implements AppConnInfoService, AppConnService {
    @Autowired
    private AppConnMapper appConnMapper;
    @Autowired
    private AppInstanceMapper appInstanceMapper;

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
        return appInstanceMapper.getAppInstancesByAppConnId(id);
    }

    @Override
    public List<? extends AppInstanceInfo> getAppInstancesByAppConnName(String appConnName) {
        AppConnBean appConnBean = appConnMapper.getAppConnBeanByName(appConnName);
        return getAppInstancesByAppConnInfo(appConnBean);
    }

    @Override
    public List<AppConnBean> getAppConns(String appConnName, String className, RowBounds rowBounds) {
        try {
            return appConnMapper.getAppConns(appConnName, className, rowBounds.getOffset(), rowBounds.getLimit());
        } catch (DataAccessException e) {
            // Handle data access exceptions, log, and possibly rethrow
            throw new RuntimeException("Error fetching AppConns", e);
        }
    }

    @Override
    public AppConnBean addAppConn(AppConnBean appConnBean) {
        AppConnBean addedBean = appConnMapper.addAppConn(appConnBean);
        if (addedBean != null) {
            return addedBean;
        } else {
            throw new RuntimeException("Failed to add AppConnBean");
        }
    }

    @Override
    public AppConnBean updateAppConn(AppConnBean appConnBean) {
        if (appConnMapper.updateAppConn(appConnBean) > 0) {
            return appConnBean;
        } else {
            throw new RuntimeException("Failed to update AppConnBean");
        }
    }

    @Override
    public void deleteAppConn(Long appConnId) {
        if (appConnMapper.deleteAppConn(appConnId) == 0) {
            throw new RuntimeException("Failed to delete AppConn with ID: " + appConnId);
        }
    }

    @Override
    public List<String> getAppConnsName() {
        return appConnMapper.getAllAppConnsName();
    }

    @Override
    @Async
    public void uploadMaterial(MultipartFile file) {
        try {
            String md5Hash = calculateMD5(file);
            // Assuming there's a method to save the file and its MD5 hash
            saveFileAndHash(file, md5Hash);
        } catch (IOException | NoSuchAlgorithmException e) {
            // Handle exceptions related to file upload or MD5 calculation
            throw new RuntimeException("Error uploading material", e);
        }
    }

    private String calculateMD5(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] fileBytes = file.getBytes();
        md.update(fileBytes);
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Placeholder for actual file saving logic and MD5 storage
    private void saveFileAndHash(MultipartFile file, String md5Hash) {
        // Implement the logic to save the file and its MD5 hash to the storage
        // This could involve saving to a database or file system, and associating the hash
        throw new UnsupportedOperationException("File saving and MD5 storage not implemented");
    }
}

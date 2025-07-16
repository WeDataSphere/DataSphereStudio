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
package com.webank.wedatasphere.dss.framework.proxy.service;


import com.webank.wedatasphere.dss.common.entity.DSSWorkspace;
import com.webank.wedatasphere.dss.framework.proxy.exception.DSSProxyUserErrorException;
import com.webank.wedatasphere.dss.framework.proxy.pojo.entity.DssProxyUser;
import org.apache.linkis.server.security.ProxyUserSSOUtils;
import scala.Option;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface DssProxyUserService {

    /**
     * 查询代理用户数据
     *
     * @param userName 查询代理用户的用户名
     * @return 代理用户的集合
     */
    List<DssProxyUser> selectProxyUserList(String userName, DSSWorkspace workspace);

    default boolean isExists(String userName, String proxyUserName, DSSWorkspace workspace) {
        List<DssProxyUser> proxyUserList = selectProxyUserList(userName, workspace);
        if(proxyUserList == null || proxyUserList.isEmpty()) {
            return false;
        }
        return proxyUserList.stream().anyMatch(proxyUser -> proxyUser.getProxyUserName().equals(proxyUserName));
    }

    default String getProxyUser(HttpServletRequest request) throws DSSProxyUserErrorException {
        Option<String> proxyUser = ProxyUserSSOUtils.getProxyUserUsername(request);
        if(proxyUser.isEmpty()) {
            throw new DSSProxyUserErrorException(60050, "proxy user is not exists in cookies.");
        }
        return proxyUser.get();
    }



}

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

package com.webank.wedatasphere.dss.standard.sso.utils;

import com.webank.wedatasphere.dss.standard.app.sso.Workspace;
import com.webank.wedatasphere.dss.standard.app.sso.builder.SSOUrlBuilderOperation;
import com.webank.wedatasphere.dss.standard.app.sso.builder.impl.SSOUrlBuilderOperationImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.common.conf.Configuration;
import java.util.Arrays;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class SSOHelper {

    public static Workspace getWorkspace(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Cookie workspaceCookie = Arrays.stream(cookies).
                filter(cookie -> "workspaceId".equals(cookie.getName())).findAny().orElse(null);
        Workspace workspace = new Workspace();
        if (workspaceCookie != null) {
            workspace.setWorkspaceName(workspaceCookie.getValue());
        }
        SSOUrlBuilderOperation ssoUrlBuilderOperation = new SSOUrlBuilderOperationImpl();
        Arrays.stream(cookies).forEach(cookie -> ssoUrlBuilderOperation.addCookie(cookie.getName(), cookie.getValue()));
        String gateWayUrl = Configuration.GATEWAY_URL().getValue();
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (StringUtils.isNotEmpty(forwardedHost)) {
            gateWayUrl = "http://" + request.getHeader("X-Forwarded-Host") + "/";
        }
//        ssoUrlBuilderOperation.setDSSUrl(Configuration.GATEWAY_URL().getValue());
        ssoUrlBuilderOperation.setDSSUrl(gateWayUrl);
        ssoUrlBuilderOperation.setWorkspace(workspace.getWorkspaceName());
        workspace.setSSOUrlBuilderOperation(ssoUrlBuilderOperation);
        return workspace;
    }

    public static Workspace setWorkspaceId(HttpServletRequest request, HttpServletResponse response, long workspaceId, String workspaceName) {
        response.addCookie(new Cookie("workspaceId", String.valueOf(workspaceId)));
        SSOUrlBuilderOperation ssoUrlBuilderOperation = new SSOUrlBuilderOperationImpl();
        Cookie[] cookies = request.getCookies();
        Arrays.stream(cookies).forEach(cookie -> {
            if (cookie.getName().equals("workspaceId")) {
                ssoUrlBuilderOperation.addCookie(cookie.getName(), String.valueOf(workspaceId));
            } else {
                ssoUrlBuilderOperation.addCookie(cookie.getName(), cookie.getValue());
            }
        });
        Workspace workspace = new Workspace();
        ssoUrlBuilderOperation.setDSSUrl(Configuration.GATEWAY_URL().getValue());
        ssoUrlBuilderOperation.setWorkspace(workspaceName);
        workspace.setSSOUrlBuilderOperation(ssoUrlBuilderOperation);
        return workspace;
    }

}

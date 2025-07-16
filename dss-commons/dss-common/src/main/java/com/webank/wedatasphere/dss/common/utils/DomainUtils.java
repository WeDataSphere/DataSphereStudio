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

import com.webank.wedatasphere.dss.common.conf.DSSCommonConf;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DomainUtils {

    private static final Pattern DOMAIN_REGEX = Pattern.compile("[a-zA-Z][a-zA-Z0-9\\.]+");
    private static final Pattern IP_REGEX = Pattern.compile("([^:]+):.+");

    public static String getCookieDomain(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if(referer==null){
            referer=request.getServerName();
        }
        return getCookieDomain(referer);
    }

    /**
     * "dss.com" -> ".dss.com"
     * "127.0.0.1" -> "127.0.0.1"
     * "127.0.0.1:8080" -> "127.0.0.1"
     * @param host the Host in HttpRequest Headers
     * @return
     */
    public static String getCookieDomain(String host) {
        int level = DSSCommonConf.DSS_DOMAIN_LEVEL.getValue();
        if(host.startsWith("https://")) {
            host = host.substring(8);
        } else if(host.startsWith("http://")) {
            host = host.substring(7);
        }
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        if(DOMAIN_REGEX.matcher(host).find()) {
            String[] domains = host.split("\\.");
            int index = level;
            if (domains.length == level) {
                index = level - 1;
            } else if (domains.length < level) {
                index = domains.length;
            }
            if (index < 0) {
                return host;
            }
            String[] parsedDomains = Arrays.copyOfRange(domains, index, domains.length);
            if (parsedDomains.length < level) {
                return host;
            }
            String domain = String.join(".", parsedDomains);
            if(domains.length >= level) {
                return "." + domain;
            }
            return domain;
        }
        Matcher matcher = IP_REGEX.matcher(host);
        if(matcher.find()) {
            return matcher.group(1);
        } else {
            return host;
        }
    }

}

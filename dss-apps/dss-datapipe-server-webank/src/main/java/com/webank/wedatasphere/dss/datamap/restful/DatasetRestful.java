/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.dss.datamap.restful;

import com.webank.wedatasphere.dss.datamap.conf.DataMapConnConf;

import com.webank.wedatasphere.dss.datamap.domain.*;

import com.webank.wedatasphere.dss.datamap.domain.vo.CheckDatasetSensitiveResult;
import com.webank.wedatasphere.dss.datamap.domain.vo.DeductSensitiveDatasetQuotaResult;
import com.webank.wedatasphere.dss.datamap.exception.DataMapException;
import com.webank.wedatasphere.dss.datamap.service.SchemaInfoService;


import com.webank.wedatasphere.dss.datamap.service.impl.WebankDatasetServiceImpl;
import org.apache.linkis.server.Message;

import org.apache.linkis.server.security.ProxyUserSSOUtils;
import org.apache.linkis.server.security.SecurityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import scala.Option;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author: jinyangrao on 2020/11/04
 * @description: by this restful api to get schema metadata.
 */

@RestController
@RequestMapping(path = "/dss/datapipe/dataset", produces = {"application/json"})
public class DatasetRestful {

    private static final Logger logger = LoggerFactory.getLogger(DatasetRestful.class);

    private static final String MESSAGE_ERROR_DBNAME_EMPTY = "dbName is empty[数据库名为空]";

    @Autowired
    private SchemaInfoService schemaInfoService;
    @Autowired
    HttpServletRequest req;
    private final String dbRestriction = DataMapConnConf.DB_RESTRICTION();

    @Autowired
    private WebankDatasetServiceImpl webankDatasetService;

    @RequestMapping(path = "/deductQuota",method = RequestMethod.POST)
    public Message deductQuota(@RequestBody DeductSensitiveDatasetQuotaRequest deductSensitiveDatasetQuotaRequest) {
        String userName = SecurityFilter.getLoginUsername(req);
        DeductSensitiveDatasetQuotaResult result =
                webankDatasetService.deductSensitiveDatasetQuota(deductSensitiveDatasetQuotaRequest, userName);
        return Message.ok().data("result", result);
    }

    @RequestMapping(path = "/checkDatasetSensitive",method = RequestMethod.POST)
    public Message checkDatasetSensitive(@RequestBody CheckDatasertSensitiveRequest checkDatasertSensitiveRequest) {
        String userName = SecurityFilter.getLoginUsername(req);
        Option<String> proxyUserOption = ProxyUserSSOUtils.getProxyUserUsername(req);
        String proxyUser = proxyUserOption.isEmpty() ? userName : proxyUserOption.get();
        List<CheckDatasetSensitiveResult> result =
                webankDatasetService.checkDatasetSensitive(checkDatasertSensitiveRequest, userName,proxyUser);
        return Message.ok().data("result", result);
    }


}
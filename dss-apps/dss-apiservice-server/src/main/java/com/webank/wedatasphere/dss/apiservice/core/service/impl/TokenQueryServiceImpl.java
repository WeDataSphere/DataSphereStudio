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

package com.webank.wedatasphere.dss.apiservice.core.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.webank.wedatasphere.dss.apiservice.core.dao.ApiServiceTokenManagerDao;
import com.webank.wedatasphere.dss.apiservice.core.dao.ApiServiceVersionDao;
import com.webank.wedatasphere.dss.apiservice.core.service.TokenQueryService;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApiVersionVo;
import com.webank.wedatasphere.dss.apiservice.core.vo.TokenManagerVo;
import com.webank.wedatasphere.dss.apiservice.core.bo.TokenQuery;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TokenQueryServiceImpl implements TokenQueryService {
    private static final Logger LOG = LoggerFactory.getLogger(TokenQueryServiceImpl.class);

    @Autowired
    ApiServiceTokenManagerDao apiServiceTokenManagerDao;
    @Autowired
    ApiServiceVersionDao apiServiceVersionDao;

    @Override
    public PageInfo<TokenManagerVo> query(TokenQuery tokenQuery) {
        List<ApiVersionVo> apiVersionVoList = apiServiceVersionDao.queryApiVersionByApiServiceId(tokenQuery.getApiId());
        PageHelper.startPage(tokenQuery.getCurrentPage(), tokenQuery.getPageSize());
        List<TokenManagerVo> tokenList = apiServiceTokenManagerDao.query(tokenQuery);
        LOG.info("token查询的结果列表大小为{}", tokenList.size());
        //返回前端页面版本号
        tokenList.forEach(tokenManagerVo -> {
            tokenManagerVo.setVersion(apiVersionVoList.stream().filter(l -> l.getId().equals(tokenManagerVo.getApiVersionId())).collect(Collectors.toList()).get(0).getVersion());
        });
        //根据版本号搜索
        if (StringUtils.isNotBlank(tokenQuery.getVersion())) {
            tokenList = tokenList.stream().filter(l -> l.getVersion().equals(tokenQuery.getVersion())).collect(Collectors.toList());
        }
        return new PageInfo<>(tokenList);
    }
}

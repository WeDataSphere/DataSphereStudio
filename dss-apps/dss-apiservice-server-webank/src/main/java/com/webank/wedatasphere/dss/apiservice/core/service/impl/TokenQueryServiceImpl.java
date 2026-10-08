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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TokenQueryServiceImpl implements TokenQueryService {
    private static final Logger LOG = LoggerFactory.getLogger(TokenQueryServiceImpl.class);

    @Autowired
    ApiServiceTokenManagerDao apiServiceTokenManagerDao;
    @Autowired
    ApiServiceVersionDao apiServiceVersionDao;

    /**
     * 查询Token列表（支持分页、版本过滤、脏数据清洗）
     * @param tokenQuery Token查询条件对象
     *                    - apiId: API服务ID（必填）
     *                    - currentPage: 当前页码（必填）
     *                    - pageSize: 每页条数（必填）
     *                    - version: 版本号（可选，用于精确过滤）
     * @return PageInfo&lt;TokenManagerVo&gt; 分页结果对象
     *         - list: 当前页Token列表
     *         - total: 过滤后的总记录数
     *         - pageNum: 当前页码
     *         - pageSize: 每页条数
     */
    @Override
    public PageInfo<TokenManagerVo> query(TokenQuery tokenQuery) {
        // 步骤1: 查询API的所有版本信息，用于后续关联Token的版本号
        List<ApiVersionVo> apiVersionVoList = apiServiceVersionDao.queryApiVersionByApiServiceId(tokenQuery.getApiId());
        
        // 步骤2: 查询Token列表（此处不分页，在内存中过滤后再分页）
        List<TokenManagerVo> tokenList = apiServiceTokenManagerDao.query(tokenQuery);
        LOG.info("token查询的结果列表大小为{}", tokenList.size());
        LOG.info("apiVersionVoList查询的结果列表大小为{}", apiVersionVoList.size());

        // 步骤3: 优化版本号查找
        // 将版本列表转为 Map<Long, String>，key为版本ID，value为版本号
        // 时间复杂度从 O(n²) 降低到 O(n)，提高Token数量大时的性能
        Map<Long, String> versionMap = apiVersionVoList.stream()
                .collect(Collectors.toMap(ApiVersionVo::getId, ApiVersionVo::getVersion));

        // 步骤4: 关联版本号并过滤脏数据
        // 使用 peek 操作在不打断流的情况下设置版本号
        // 使用 filter 过滤掉版本号为null的Token（即版本已被删除的脏数据）
        tokenList = tokenList.stream()
                .peek(token -> token.setVersion(versionMap.get(token.getApiVersionId())))
                .filter(token -> token.getVersion() != null)
                .collect(Collectors.toList());

        // 步骤5: 根据版本号精确过滤（可选）
        // 如果查询条件中指定了版本号，则只返回匹配该版本号的Token
        if (StringUtils.isNotBlank(tokenQuery.getVersion())) {
            tokenList = tokenList.stream()
                    .filter(l -> l.getVersion().equals(tokenQuery.getVersion()))
                    .collect(Collectors.toList());
        }

        // 步骤6: 手动分页
        // 必须先获取完整数据再分页，确保分页信息（总记录数、总页数）准确
        // 如果使用数据库分页后过滤，会导致分页信息不准确
        int total = tokenList.size();                    // 过滤后的总记录数
        int pageNum = tokenQuery.getCurrentPage();       // 当前页码
        int pageSize = tokenQuery.getPageSize();         // 每页条数
        int fromIndex = (pageNum - 1) * pageSize;        // 起始索引
        int toIndex = Math.min(fromIndex + pageSize, total); // 结束索引

        // 计算当前页的数据列表
        List<TokenManagerVo> pageData = fromIndex < total ?
                tokenList.subList(fromIndex, toIndex) : Collections.emptyList();

        // 记录分页信息日志
        LOG.info("过滤后token列表大小为{}, 当前页{}，每页{}条，实际返回{}条", 
                total, pageNum, pageSize, pageData.size());
        
        // 创建分页结果对象，并手动设置总记录数（确保准确）
        PageInfo<TokenManagerVo> pageInfo = new PageInfo<>(pageData);
        pageInfo.setTotal(total);
        
        return pageInfo;
    }
}

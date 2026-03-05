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

package com.webank.wedatasphere.dss.apiservice.core.token;

import com.webank.wedatasphere.dss.apiservice.core.config.ApiServiceConfiguration;
import com.webank.wedatasphere.dss.apiservice.core.constant.ApiCommonConstant;
import com.webank.wedatasphere.dss.apiservice.core.constant.SaveTokenEnum;
import com.webank.wedatasphere.dss.apiservice.core.dao.ApiServiceTokenManagerDao;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiServiceTokenException;
import com.webank.wedatasphere.dss.apiservice.core.bo.ApiServiceToken;
import com.webank.wedatasphere.dss.apiservice.core.service.ApprovalService;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApprovalVo;
import com.webank.wedatasphere.dss.apiservice.core.vo.TokenManagerVo;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author allenlliu
 * @version 2.0.0
 * @date 2020/08/14 11:52 AM
 */
@Component
public class DataMapTokenImpl implements TokenAuth {

    private static final Logger logger = LoggerFactory.getLogger(DataMapTokenImpl.class);

    @Autowired
    ApiServiceTokenManagerDao apiServiceTokenManagerDao;

    @Autowired
    ApprovalService approvalService;

    @Override
    @Transactional(rollbackFor = ErrorException.class)
    public SaveTokenEnum saveTokensToDb(List<TokenManagerVo> tokenManagerVos, String approvalNo) throws ApiServiceTokenException {
        boolean isEmptyToken = tokenManagerVos.stream().anyMatch(tokenManagerVo -> StringUtils.isEmpty(tokenManagerVo.getToken()));
        if (isEmptyToken) {
            throw new ApiServiceTokenException(800001, "Failed to save to db for Some token is empty");
        }
        //apiVersion存在对应的approvalNo，并且不是默认的approvalNo
        //查询该api_version和approvalNo是否有记录了，并且不是默认的。防止同一个单号对应的apiversion重复插入token todo 这里是否还需要判断
        else if (checkDuplicateAuth(approvalNo, tokenManagerVos.get(0).getApiVersionId()) && !approvalNo.equals(ApiCommonConstant.DEFAULT_APPROVAL_NO)) {
            return SaveTokenEnum.SUCCESS;
        } else {
            try {
                apiServiceTokenManagerDao.insertList(tokenManagerVos);
            } catch (Exception e) {
                logger.error("Batch save token to db failed", e);
                throw new ApiServiceTokenException(800002, e.getMessage());

            }
        }

        return SaveTokenEnum.SUCCESS;
    }

    private boolean checkDuplicateAuth(String approvalNo, Long apiServiceVersionID) {
        if (apiServiceTokenManagerDao.queryApprovalNo(approvalNo, apiServiceVersionID) > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<TokenManagerVo> genTokenRecord(ApprovalVo approvalVo) {
        List<TokenManagerVo> tokenManagerVoList = new ArrayList<>();
        Arrays.stream(approvalVo.getApplyUser().split(",")).forEach(tempUser -> {
            TokenManagerVo tmpToken = new TokenManagerVo();
            tmpToken.setApiId(approvalVo.getApiId());
            tmpToken.setApplyTime(new Date());
//            tmpToken.setDuration(ApiServiceConfiguration.API_TOKEN_DURATION.getValue());
            tmpToken.setDuration(approvalVo.getDuration());
            tmpToken.setReason("approval token auth");
            tmpToken.setStatus(1);
            tmpToken.setIpWhitelist("");
            tmpToken.setCaller("scripts");
            tmpToken.setUser(tempUser);
            tmpToken.setPublisher(approvalVo.getCreator());
            tmpToken.setApiVersionId(approvalVo.getApiVersionId());

            ApiServiceToken apiServiceToken = new ApiServiceToken();
            apiServiceToken.setApplyUser(tempUser);
            apiServiceToken.setPublisher(approvalVo.getCreator()); //todo creator
            apiServiceToken.setApplyTime(tmpToken.getApplyTime());
            apiServiceToken.setApiServiceId(approvalVo.getApiId());

            tmpToken.setToken(JwtManager.createToken(tempUser, apiServiceToken, tmpToken.getDuration()));
            //审批单号
            tmpToken.setApplySource(approvalVo.getApprovalNo());
            tokenManagerVoList.add(tmpToken);
        });

        mergeHistoryToken(tokenManagerVoList,approvalVo);

        return tokenManagerVoList;
    }



    /**
     * 合并历史授权用户
     * 将上一版本中已授权但在本次审批申请中未包含的用户，自动合并到当前审批中
     * 增量授权场景：保留历史用户的配置信息（IP 白名单、访问限制等）
     *
     * @param tokenManagerVoList 当前审批申请的用户 token 列表
     * @param approvalVo 当前审批信息
     */
    private void mergeHistoryToken(List<TokenManagerVo> tokenManagerVoList, ApprovalVo approvalVo) {

        // 获取上一次审批完成的信息（第二新的审批通过版本）
        ApprovalVo historyApprovalVo = approvalService.getSecondApproval(approvalVo.getApiId());
        if (historyApprovalVo == null) {
            logger.info("api [{},{}] not find history approval",approvalVo.getApiId(),approvalVo.getApprovalName());
            // 没有历史审批记录，无需合并
            return;
        }

        // 提取当前申请的用户列表，用于去重判断
        Set<String> currentUserSet = tokenManagerVoList.stream()
                .map(TokenManagerVo::getUser)
                .collect(Collectors.toSet());

        // 查询上一次审批中所有有效的 token 记录
        List<TokenManagerVo> historyTokenList = apiServiceTokenManagerDao
                .queryByVersionIdWithValidStatus(historyApprovalVo.getApiVersionId(),
                        historyApprovalVo.getApprovalNo(),
                        ApiCommonConstant.API_ENABLE_STATUS);

        logger.info("history token size is {}", historyTokenList.size());
        logger.info("current user is {}",StringUtils.join(currentUserSet,","));
        // 遍历历史 token，找出需要合并的用户（历史中有但本次申请中没有）
        List<TokenManagerVo> tokensToAdd = new ArrayList<>();
        for (TokenManagerVo historyToken : historyTokenList) {
            logger.info("api id is [{},{}], user is {}",historyToken.getApiId(),
                    historyToken.getApiVersionId(),historyToken.getUser());
            // 如果该用户已经在当前申请中，跳过
            if (currentUserSet.contains(historyToken.getUser())) {
                continue;
            }

            // 创建新的 token，复制历史 token 的配置信息
            TokenManagerVo newToken = copyHistoryToken(historyToken);
            // 更新为新版本的 API 版本 ID 和审批单号
            newToken.setApiVersionId(approvalVo.getApiVersionId());
            newToken.setApplySource(approvalVo.getApprovalNo());
            newToken.setStatus(1); // 设置为有效状态

            tokensToAdd.add(newToken);
        }

        logger.info("tokensToAdd size is {}", tokensToAdd.size());
        // 将需要合并的 token 添加到列表中（避免在遍历时修改原列表）
        tokenManagerVoList.addAll(tokensToAdd);
    }


    /**
     * 复制历史 token 的配置信息
     * 保留用户之前设置的 IP 白名单、访问限制等个性化配置
     *
     * @param historyToken 历史 token 配置
     * @return 新的 token 对象，复用历史配置
     */
    private TokenManagerVo copyHistoryToken(TokenManagerVo historyToken) {
        TokenManagerVo newToken = new TokenManagerVo();
        // 复制历史 token 的所有属性（除 ID 外）
        BeanUtils.copyProperties(historyToken, newToken);
        newToken.setId(null); // 清空 ID，以便插入新记录
        newToken.setApplyTime(historyToken.getApplyTime() == null ?
                new Date(): historyToken.getApplyTime()); // 如果申请时间空，则更新为当前时间
        return newToken;
    }



    @Override
    public void  updateTokenStatusBeforeVersionId(ApprovalVo approvalVo){
        apiServiceTokenManagerDao.updateTokenStatusBeforeVersionId(approvalVo.getApiVersionId(), approvalVo.getApiId(), ApiCommonConstant.API_DISABLE_STATUS);
    }


}

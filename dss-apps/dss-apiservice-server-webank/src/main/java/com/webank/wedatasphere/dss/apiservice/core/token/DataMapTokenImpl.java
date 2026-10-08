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

    /**
     * 批量保存 Token 到数据库
     * 
     * 业务逻辑：
     * 1. 校验 Token 非空（JWT 字符串）
     * 2. 防止重复授权：检查同一 API 版本 + 审批单号是否已存在 Token
     * 3. 批量插入 Token 记录
     * 
     * 重复授权检查说明：
     * - 避免用户多次调用 approvalRefresh 接口导致重复插入 Token
     * - DEFAULT_APPROVAL_NO（"0001"）是特殊审批单号，用于 API 创建者自授权，需要允许重复
     * 
     * @param tokenManagerVos Token 列表
     * @param approvalNo 审批单号
     * @return 保存结果枚举
     * @throws ApiServiceTokenException Token 为空或保存失败时抛出
     */
    @Override
    @Transactional(rollbackFor = ErrorException.class)
    public SaveTokenEnum saveTokensToDb(List<TokenManagerVo> tokenManagerVos, String approvalNo) throws ApiServiceTokenException {
        // 校验 Token 非空
        boolean isEmptyToken = tokenManagerVos.stream().anyMatch(tokenManagerVo -> StringUtils.isEmpty(tokenManagerVo.getToken()));
        if (isEmptyToken) {
            throw new ApiServiceTokenException(800001, "Failed to save to db for Some token is empty");
        }
        // 防止重复授权检查
        // 查询该 api_version 和 approvalNo 是否已有 Token 记录（不是默认审批单号）
        // 如果存在记录，说明已经生成过 Token，直接返回成功，避免重复插入
        else if (checkDuplicateAuth(approvalNo, tokenManagerVos.get(0).getApiVersionId()) && !approvalNo.equals(ApiCommonConstant.DEFAULT_APPROVAL_NO)) {
            logger.warn("审批单号 {} 对应的 API 版本 {} 已存在 Token，跳过重复插入", 
                       approvalNo, tokenManagerVos.get(0).getApiVersionId());
            return SaveTokenEnum.SUCCESS;
        } else {
            try {
                // 批量插入 Token 记录
                apiServiceTokenManagerDao.insertList(tokenManagerVos);
                logger.info("成功保存 {} 个 Token 到数据库，审批单号: {}", tokenManagerVos.size(), approvalNo);
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

    /**
     * 根据审批信息生成 Token 记录
     * 
     * 业务流程：
     * 1. 解析审批申请用户列表（逗号分隔字符串，如 "user1,user2,user3"）
     * 2. 为每个用户创建基础 Token 对象（包含 API ID、版本 ID、发布者等基本信息）
     * 3. 生成 JWT Token 字符串（包含用户信息、有效期等）
     * 4. 调用 mergeHistoryToken 合并历史授权用户（增量授权核心）
     * 5. 返回完整的 Token 列表（包含申请用户 + 历史用户）
     * 
     * 增量授权说明：
     * - 如果本次申请只包含部分用户（如只申请 user3），系统会自动查询历史版本的用户列表
     * - 将历史用户（如 user1、user2）也包含在 Token 列表中
     * - 保留历史用户的配置信息（IP 白名单、访问限制等）
     * 
     * @param approvalVo 审批信息对象
     * @return Token 记录列表（包含所有授权用户：申请用户 + 历史用户）
     */
    @Override
    public List<TokenManagerVo> genTokenRecord(ApprovalVo approvalVo) {
        List<TokenManagerVo> tokenManagerVoList = new ArrayList<>();
        
        // 解析申请用户列表（逗号分隔），为每个用户创建 Token
        // 例如：applyUser = "user1,user2,user3"
        Arrays.stream(approvalVo.getApplyUser().split(",")).forEach(tempUser -> {
            TokenManagerVo tmpToken = new TokenManagerVo();
            // 设置 API 基本信息
            tmpToken.setApiId(approvalVo.getApiId());
            tmpToken.setApplyTime(new Date());
            tmpToken.setDuration(approvalVo.getDuration()); // 授权时长（天）
            tmpToken.setReason("approval token auth");
            tmpToken.setStatus(1); // 有效状态
            tmpToken.setIpWhitelist(""); // IP 白名单（默认为空）
            tmpToken.setCaller("scripts");
            tmpToken.setUser(tempUser); // 申请用户
            tmpToken.setPublisher(approvalVo.getCreator()); // 发布者
            tmpToken.setApiVersionId(approvalVo.getApiVersionId()); // API 版本 ID

            // 构造 JWT Token
            ApiServiceToken apiServiceToken = new ApiServiceToken();
            apiServiceToken.setApplyUser(tempUser);
            apiServiceToken.setPublisher(approvalVo.getCreator());
            apiServiceToken.setApplyTime(tmpToken.getApplyTime());
            apiServiceToken.setApiServiceId(approvalVo.getApiId());

            // 生成 JWT Token 字符串（包含用户信息、有效期等）
            tmpToken.setToken(JwtManager.createToken(tempUser, apiServiceToken, tmpToken.getDuration()));
            // 设置审批单号（用于关联审批记录）
            tmpToken.setApplySource(approvalVo.getApprovalNo());
            tokenManagerVoList.add(tmpToken);
        });

        // 合并历史授权用户（增量授权核心逻辑）
        // 会查询上一版本的历史用户，将不在本次申请中的用户也加入到 Token 列表
        mergeHistoryToken(tokenManagerVoList, approvalVo);

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



    /**
     * 禁用指定版本之前的所有 Token
     * 
     * 业务场景：
     * 当 API V2 审批通过后，需要禁用 V1 的所有 Token，确保只有最新版本的 Token 有效。
     * 
     * 执行逻辑：
     * - 查询该 API（apiId）的所有 Token
     * - 找出 api_version_id 小于当前版本的 Token（即旧版本的 Token）
     * - 将这些 Token 的状态设置为禁用（status=0）
     * 
     * 示例：
     * API V1（version_id=100）有 Token：user1, user2
     * API V2（version_id=200）审批通过后，禁用 V1 的所有 Token
     * 之后只有 V2 生成的 Token 有效
     * 
     * @param approvalVo 当前审批信息（包含 apiId 和 apiVersionId）
     */
    @Override
    public void  updateTokenStatusBeforeVersionId(ApprovalVo approvalVo){
        apiServiceTokenManagerDao.updateTokenStatusBeforeVersionId(
            approvalVo.getApiVersionId(),    // 当前版本 ID
            approvalVo.getApiId(),           // API ID
            ApiCommonConstant.API_DISABLE_STATUS // 禁用状态
        );
        logger.info("已禁用 API {} 版本 {} 之前的所有 Token", approvalVo.getApiId(), approvalVo.getApiVersionId());
    }


}

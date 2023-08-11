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

package com.webank.wedatasphere.dss.apiservice.core.token;

import com.webank.wedatasphere.dss.apiservice.core.bo.ApiServiceToken;
import com.webank.wedatasphere.dss.apiservice.core.constant.ApiCommonConstant;
import com.webank.wedatasphere.dss.apiservice.core.constant.SaveTokenEnum;
import com.webank.wedatasphere.dss.apiservice.core.dao.ApiServiceTokenManagerDao;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiServiceTokenException;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApprovalVo;
import com.webank.wedatasphere.dss.apiservice.core.vo.TokenManagerVo;
import org.apache.commons.lang.StringUtils;
import org.apache.linkis.common.exception.ErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class DataMapTokenImpl implements TokenAuth {

    private static final Logger logger = LoggerFactory.getLogger(DataMapTokenImpl.class);

    @Autowired
    ApiServiceTokenManagerDao apiServiceTokenManagerDao;

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
        return tokenManagerVoList;
    }
}

package com.webank.wedatasphere.dss.apiservice.core.token;

import com.webank.wedatasphere.dss.apiservice.core.constant.SaveTokenEnum;
import com.webank.wedatasphere.dss.apiservice.core.exception.ApiServiceTokenException;
import com.webank.wedatasphere.dss.apiservice.core.vo.ApprovalVo;
import com.webank.wedatasphere.dss.apiservice.core.vo.TokenManagerVo;

import java.util.List;

/**
 * @author allenlliu
 * @version 2.0.0
 * @date 2020/08/14 11:41 AM
 */
public interface TokenAuth {


    /**
     * batch save token to db
     * @param tokenManagerVos
     * @return
     */
    SaveTokenEnum saveTokensToDb(List<TokenManagerVo> tokenManagerVos, String approvalNo) throws ApiServiceTokenException;

    List<TokenManagerVo>  genTokenRecord(ApprovalVo approvalVo);

    void  updateTokenStatusBeforeVersionId(ApprovalVo approvalVo);

}

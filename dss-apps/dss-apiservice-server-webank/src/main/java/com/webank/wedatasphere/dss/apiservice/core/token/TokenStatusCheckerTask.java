package com.webank.wedatasphere.dss.apiservice.core.token;

import com.webank.wedatasphere.dss.apiservice.core.dao.ApiServiceTokenManagerDao;
import com.webank.wedatasphere.dss.apiservice.core.util.DateUtil;
import com.webank.wedatasphere.dss.apiservice.core.vo.TokenManagerVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
@EnableScheduling
public class TokenStatusCheckerTask {
    private static final Logger LOG = LoggerFactory.getLogger(TokenStatusCheckerTask.class);

    @Autowired
    ApiServiceTokenManagerDao atmd;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void doTokenStatusCheckTask() {
        // 查询启用状态的token
        List<TokenManagerVo> tokenManagerVos = atmd.queryTokenByStatus(1);
        if (null != tokenManagerVos) {
            for (TokenManagerVo tmv : tokenManagerVos) {
                Date applyTime = tmv.getApplyTime();
                Date nowTime = Calendar.getInstance().getTime();
                Calendar cal = Calendar.getInstance();
                cal.setTime(applyTime);
                cal.add(Calendar.DATE, tmv.getDuration().intValue());
                Date endTime = cal.getTime();
                if (endTime.compareTo(nowTime) < 0) {
                    LOG.warn("token id:" + tmv.getId() + " 已经过期！");
                    atmd.disableTokenStatus(tmv.getId());
                }
            }
        }
    }

}

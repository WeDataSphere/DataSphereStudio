package com.webank.wedatasphere.dss.common.server.mybatis.condition;

import org.apache.linkis.common.conf.CommonVars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

@Component
public class DPMDataSourceCondition  implements Condition {

    private static final Logger logger = LoggerFactory.getLogger(DPMDataSourceCondition.class);

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        boolean match = CommonVars.apply("wds.linkis.dpm.bean.default", true).getValue();
        logger.info("DPMDataSourceCondition match : " + match);
        return match;
    }
}

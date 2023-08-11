package com.webank.wedatasphere.dss.detection.server.config;

import com.webank.wedatasphere.dss.detection.server.method.CustomDetectionMethod;
import com.webank.wedatasphere.dss.detection.server.method.DetectionMethod;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DetectionBeanConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public DetectionMethod getDetectionMethod(){
        return new CustomDetectionMethod();
    }
}

package com.webank.wedatasphere.dss.framework.workspace.conf;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Author: xlinliu
 * Date: 2024/5/9
 */
@Configuration
@EnableAsync
public class WorkspaceSpringConf {
    @Bean
    public ScheduledThreadPoolExecutor dssEcReleaseScheduledExecutor() {
        return new ScheduledThreadPoolExecutor(30,
                new ThreadFactoryBuilder().setNameFormat("Dss-Ec-Release-Spring-Scheduler-Thread-%d").build());
    }
}

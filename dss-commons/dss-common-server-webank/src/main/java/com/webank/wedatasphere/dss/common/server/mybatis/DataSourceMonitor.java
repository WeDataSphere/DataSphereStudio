package com.webank.wedatasphere.dss.common.server.mybatis;

import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DataSourceMonitor {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceMonitor.class);

    @Autowired
    private DataSource dataSource;

    @Scheduled(fixedRateString = "${datasource.monitor.fixed-rate:3600000}")  // 默认每小时执行一次
    public void printDataSourceInfo() {
        if (dataSource instanceof DruidDataSource) {
            DruidDataSource druidDataSource = (DruidDataSource) dataSource;
            // 构建连接池信息字符串
            StringBuilder poolInfo = new StringBuilder("Druid Pool Information: \n");
            poolInfo.append("Active Connections: ").append(druidDataSource.getActiveCount()).append("\n");
            poolInfo.append("Polling Count Connections: ").append(druidDataSource.getPoolingCount()).append("\n");
            poolInfo.append("Max Active Connections: ").append(druidDataSource.getMaxActive()).append("\n");
            poolInfo.append("Min Idle Connections: ").append(druidDataSource.getMinIdle()).append("\n");
            poolInfo.append("Max Wait Time: ").append(druidDataSource.getMaxWait()).append("\n");
            poolInfo.append("Time Between Eviction Runs: ").append(druidDataSource.getTimeBetweenEvictionRunsMillis()).append("\n");
            poolInfo.append("Min Evictable Idle Time: ").append(druidDataSource.getMinEvictableIdleTimeMillis()).append("\n");
            poolInfo.append("Validation Query: ").append(druidDataSource.getValidationQuery()).append("\n");
            poolInfo.append("Test While Idle: ").append(druidDataSource.isTestWhileIdle()).append("\n");
            poolInfo.append("Test On Borrow: ").append(druidDataSource.isTestOnBorrow()).append("\n");
            poolInfo.append("Test On Return: ").append(druidDataSource.isTestOnReturn()).append("\n");
            poolInfo.append("Pool Prepared Statements: ").append(druidDataSource.isPoolPreparedStatements()).append("\n");
            poolInfo.append("Remove Abandoned: ").append(druidDataSource.isRemoveAbandoned()).append("\n");
            poolInfo.append("Remove Abandoned Timeout: ").append(druidDataSource.getRemoveAbandonedTimeout());

            // 一次性打印连接池信息
            logger.info(poolInfo.toString());
        } else {
            logger.warn("The DataSource is not an instance of DruidDataSource, cannot print pool information.");
        }
    }
}


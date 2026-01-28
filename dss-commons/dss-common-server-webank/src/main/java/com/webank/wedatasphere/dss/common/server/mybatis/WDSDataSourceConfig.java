package com.webank.wedatasphere.dss.common.server.mybatis;


import bsp.encrypt.EncryptUtil;
import com.webank.wedatasphere.dss.common.server.mybatis.condition.DPMDataSourceCondition;
import com.webank.wedatasphere.dss.common.server.mybatis.conf.WDSMybatisConfiguration;
import org.apache.linkis.mybatis.DataSourceUtils;
import org.apache.linkis.mybatis.conf.MybatisConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConfigurationProperties
public class WDSDataSourceConfig {

  private static final Logger logger = LoggerFactory.getLogger(WDSDataSourceConfig.class);

  @Bean(name = "dataSource", destroyMethod = "close")
  @Conditional(DPMDataSourceCondition.class)
  public DataSource dataSource() {
    String encryptPassword = MybatisConfiguration.BDP_SERVER_MYBATIS_DATASOURCE_PASSWORD.getValue();
    String priKey = WDSMybatisConfiguration.LINKIS_MYSQL_PRIV_KEY.getValue();
    String password = null;
    if (WDSMybatisConfiguration.LINKIS_MYSQL_IS_ENCRYPT.getValue()) {
      try {
        password = EncryptUtil.decrypt(priKey, encryptPassword);
      } catch (Exception e) {
        logger.error("failed to decrypt password for {}", encryptPassword, e);
        System.exit(-2);
      }
    } else {
      password = encryptPassword;
    }
    logger.info("default DPMDataSource inited.");
    return DataSourceUtils.buildDataSource(null, null, password);
  }
}

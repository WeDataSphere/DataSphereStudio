package com.webank.wedatasphere.dss.common.server.mybatis.conf;

import org.apache.linkis.common.conf.CommonVars;

public class WDSMybatisConfiguration {

  public static final CommonVars<Boolean> LINKIS_MYSQL_IS_ENCRYPT =
      CommonVars.apply("wds.linkis.mysql.is.encrypt", true);

  public static final CommonVars<String> LINKIS_MYSQL_PUB_KEY =
      CommonVars.apply(
          "wds.linkis.mysql.pub.key",
          "abc");

  public static final CommonVars<String> LINKIS_MYSQL_PRIV_KEY =
      CommonVars.apply(
          "wds.linkis.mysql.pri.key",
          "abc");


  public static final CommonVars<Boolean> LINKIS_HIVE_IS_ENCRYPT =
      CommonVars.apply("linkis.hive.is.encrypt", false);

  public static final CommonVars<String> LINKIS_HIVE_PRIV_KEY =
      CommonVars.apply(
          "linkis.hive.pri.key", "");
}

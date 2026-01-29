package com.webank.wedatasphere.dss.datamap.conf

import org.apache.linkis.common.conf.CommonVars

object DataMapConnConf {
  // 客户端和服务器建立连接的超时时间
  val HTTP_SOCKET_TIMEOUT = CommonVars.apply("wds.linkis.metadata.http.socket.timeout", 20000).getValue
  // 从连接池获取连接的超时时间
  val HTTP_CONN_TIMEOUT = CommonVars.apply("wds.linkis.metadata.http.connection.timeout", 20000).getValue
  // 连接建立后，request没有回应的超时时间
  val HTTP_CONN_REQUEST_TIMEOUT = CommonVars.apply("wds.linkis.metadata.http.connection.request.time", 20000).getValue

  // 集群类型
  val CLUSTER_TYPE = CommonVars.apply("wds.linkis.datamap.cluster.type", "BDAP").getValue
  // 环境uat和prod
  val DATAMAP_ENV = CommonVars.apply("wds.linkis.datamap.env", "prod").getValue
  //dms的系统用户
  val DATAMAP_SYSTEM_USERNAME=CommonVars.apply("wds.linkis.datamap.system.username", "dms_sys_5425_reader").getValue
  // datamap认证的需要的appid
  val DATAMAP_APPID = CommonVars.apply("wds.linkis.datamap.appid", "490aa89528a0298a2c3b21de889ed919").getValue
  // datamap认证需要的token信息
  val DATAMAP_TOKEN = CommonVars.apply("wds.linkis.datamap.token", "dataMapToken").getValue
  // datamap服务已做拆分，此处连接的实际是datashapis服务
  val DATAMAP_IP_ADDRESS = CommonVars.apply("wds.linkis.metadata.datamap.ip", "127.0.0.1").getValue
  val DATAMAP_IP_PORT = CommonVars.apply("wds.linkis.metadata.datamap.ip.port", 8001).getValue
  val DATAMAP_REQUEST_BASE_PATH = CommonVars.apply("wds.linkis.metadata.datamap.request.base.path", "/api/v1/isolate/metadata-service").getValue

  val DATAMAP_REQUEST_SCHEMAINFO_METHOD = CommonVars.apply("wds.linkis.metadata.datamap.request.schemainfo.method", "GET").getValue
  val DATAMAP_REQUEST_SCHEMAINFO_PATH = CommonVars.apply("wds.linkis.metadata.datamap.request.schemainfo.path", "/databases/detailByDbName").getValue
  val DATAMAP_REQUEST_SCHEMAINFO_PARAM = CommonVars.apply("wds.linkis.metadata.datamap.request.schemainfo.param", "{}").getValue

  val DATAMAP_REQUEST_TABLESMETADATA_METHOD = CommonVars.apply("wds.linkis.metadata.datamap.request.tablesmetadata.method", "GET").getValue
  val DATAMAP_REQUEST_TABLESMETADATA_PATH = CommonVars.apply("wds.linkis.metadata.datamap.request.tablesmetadata.path", "/datasets/tableDetails").getValue
  val DATAMAP_REQUEST_TABLESMETADATA_PARAM = CommonVars.apply("wds.linkis.metadata.datamap.request.tablesmetadata.param", "{}").getValue


  val DATAMAP_REQUEST_TABALESNAME_METHOD = CommonVars.apply("wds.linkis.metadata.datamap.request.tablesname.method", "GET").getValue
  val DATAMAP_REQUEST_TABALESNAME_PATH = CommonVars.apply("wds.linkis.metadata.datamap.request.tablesname.path", "/datasets/tableNames").getValue
  val DATAMAP_REQUEST_TABALESNAME_PARAM = CommonVars.apply("wds.linkis.metadata.datamap.request.tablesname.param", "{}").getValue


  val TRANSFEROR_CLASS = CommonVars.apply("wds.dss.datapipe.tables.owner.transferor.class", "com.webank.wedatasphere.dss.datamap.datamap.transferor.ITSMTransferor").getValue
  val ITSM_REQUEST_URL =CommonVars.apply("wds.dss.datapipe.itsm.request.url", "https://localhost/itsm/request/insertRequestAuth.any").getValue
  val DB_CLUSTER_NAME = CommonVars.apply("wds.dss.datapipe.db.cluster.name", "HDP-DG-BDAP-MAIN").getValue
  val DB_ENV = CommonVars.apply("wds.dss.datapipe.db.env.name", "test").getValue
  val ITSM_ENV = CommonVars.apply("wds.dss.datapipe.itsm.env.name", "test").getValue
  val ITSM_IMPL_MANAGER = CommonVars.apply("wds.dss.datapipe.itsm.impl.manager", "huyangchen").getValue
  val ITSM_APP_ID = CommonVars.apply("wds.dss.datapipe.itsm.app.id", "504").getValue
  val ITSM_AP_KEY = CommonVars.apply("wds.dss.datapipe.itsm.app.key", "").getValue
  val ITSM_USER_ID = CommonVars.apply("wds.dss.datapipe.itsm.user.id", "burdezhang").getValue
  val ITSM_FORM_ID = CommonVars.apply("wds.dss.datapipe.itsm.form.id", "10002686").getValue
  val ITSM_FORM_VERSION = CommonVars.apply("wds.dss.datapipe.itsm.form.version", "3").getValue
  // val DB_RESTRICTION = CommonVars.apply("wds.dss.datapipe.db.restriction", "work, work_f, work_safe").getValue
  val DB_RESTRICTION = CommonVars.apply("wds.dss.datapipe.db.restriction", "work, bak").getValue


  val NEBULA_REQUEST_BASE_PATH = CommonVars.apply("wds.linkis.metadata.nebula.request.base.path", "/api/v1/isolate/metadata-tools/nebula/metadata").getValue
  val NEBULA_REQUEST_SPACE_METHOD = CommonVars.apply("wds.linkis.metadata.nebula.request.space.method", "GET").getValue
  val NEBULA_REQUEST_SPACE_PATH = CommonVars.apply("wds.linkis.metadata.nebula.request.space.path", "/space").getValue
  val NEBULA_REQUEST_SPACE_PARAM = CommonVars.apply("wds.linkis.metadata.nebula.request.space.param", "{}").getValue
  val NEBULA_REQUEST_TAGS_METHOD = CommonVars.apply("wds.linkis.metadata.nebula.request.tags.method", "GET").getValue
  val NEBULA_REQUEST_TAGS_PATH = CommonVars.apply("wds.linkis.metadata.nebula.request.tags.path", "/tags").getValue
  val NEBULA_REQUEST_TAGS_PARAM = CommonVars.apply("wds.linkis.metadata.nebula.request.tags.param", "{}").getValue
  val NEBULA_REQUEST_TAG_PROP_METHOD = CommonVars.apply("wds.linkis.metadata.nebula.request.tag.prop.method", "GET").getValue
  val NEBULA_REQUEST_TAG_PROP_PATH = CommonVars.apply("wds.linkis.metadata.nebula.request.tag.prop.path", "/tag-prop").getValue
  val NEBULA_REQUEST_TAG_PROP_PARAM = CommonVars.apply("wds.linkis.metadata.nebula.request.tag.prop.param", "{}").getValue
  val NEBULA_REQUEST_EDGES_METHOD = CommonVars.apply("wds.linkis.metadata.nebula.request.edges.method", "GET").getValue
  val NEBULA_REQUEST_EDGES_PATH = CommonVars.apply("wds.linkis.metadata.nebula.request.edges.path", "/edges").getValue
  val NEBULA_REQUEST_EDGES_PARAM = CommonVars.apply("wds.linkis.metadata.nebula.request.edges.param", "{}").getValue
  val NEBULA_REQUEST_EDGE_PROP_METHOD = CommonVars.apply("wds.linkis.metadata.nebula.request.edge.prop.method", "GET").getValue
  val NEBULA_REQUEST_EDGE_PROP_PATH = CommonVars.apply("wds.linkis.metadata.nebula.request.edge.prop.path", "/edge-prop").getValue
  val NEBULA_REQUEST_EDGE_PROP_PARAM = CommonVars.apply("wds.linkis.metadata.nebula.request.edge.prop.param", "{}").getValue
  val NEBULA_REQUEST_TAG_INDEX_METHOD = CommonVars.apply("wds.linkis.metadata.nebula.request.tag.index.method", "GET").getValue
  val NEBULA_REQUEST_TAG_INDEX_PATH = CommonVars.apply("wds.linkis.metadata.nebula.request.tag.index.path", "/tag-index").getValue
  val NEBULA_REQUEST_TAG_INDEX_PARAM = CommonVars.apply("wds.linkis.metadata.nebula.request.tag.index.param", "{}").getValue
  val NEBULA_REQUEST_EDGE_INDEX_METHOD = CommonVars.apply("wds.linkis.metadata.nebula.request.edge.index.method", "GET").getValue
  val NEBULA_REQUEST_EDGE_INDEX_PATH = CommonVars.apply("wds.linkis.metadata.nebula.request.edge.index.path", "/edge-index").getValue
  val NEBULA_REQUEST_EDGE_INDEX_PARAM = CommonVars.apply("wds.linkis.metadata.nebula.request.edge.index.param", "{}").getValue
  val NEBULA_REQUEST_PERMISSION_METHOD = CommonVars.apply("wds.linkis.metadata.nebula.request.permission.method", "GET").getValue
  val NEBULA_REQUEST_PERMISSION_PATH = CommonVars.apply("wds.linkis.metadata.nebula.request.permission.path", "/delayed/permission").getValue
  val NEBULA_REQUEST_PERMISSION_PARAM = CommonVars.apply("wds.linkis.metadata.nebula.request.permission.param", "{}").getValue
  val DMS_REQUEST_CODE_META_METHOD = CommonVars.apply("wds.linkis.metadata.dms.request.code.meta.method", "POST")
    .getValue
  val DMS_REQUEST_CODE_META_PATH = CommonVars.apply("wds.linkis.metadata.dms.request.code.meta.path",
    "/lineage/sql").getValue
  val DMS_REQUEST_ENTERPRISE_SECURE_USER_LIST_METHOD =
    CommonVars.apply("wds.linkis.metadata.dms.request.enterprise.secure.user.list.method", "GET")
      .getValue
  val DMS_REQUEST_ENTERPRISE_SECURE_USER_LIST_PATH =
    CommonVars.apply("wds.linkis.metadata.dms.request.enterprise.secure.user.list.path",
      "/mask/enterprise/secure/usage").getValue
  val DMS_REQUEST_USER_NEED_SCAN_METHOD = CommonVars.apply("wds.linkis.metadata.dms.request.user.need.scan.method",
      "GET")
    .getValue
  val DMS_REQUEST_USER_NEED_SCAN_PATH = CommonVars.apply("wds.linkis.metadata.dms.request.user.need.scan.path",
    "/mask/enterprise/secure/user/scan").getValue

  val DMS_REQUEST_DEDUCT_SENSITIVE_DATA_QUOTA_METHOD =
    CommonVars.apply("wds.linkis.metadata.dms.request.deduct.sensitive.data.quota.method", "POST")
    .getValue
  val DMS_REQUEST_DEDUCT_SENSITIVE_DATA_QUOTA_PATH =
    CommonVars.apply("wds.linkis.metadata.dms.request.deduct.sensitive.data.quota.path",
    "/mask/enterprise/secure/usage").getValue
  val DMS_REQUEST_VALIDATE_TABLE_METHOD =
    CommonVars.apply("wds.linkis.metadata.dms.request.validate.table.method", "GET")
      .getValue
  val DMS_REQUEST_VALIDATE_TABLE_PATH =
    CommonVars.apply("wds.linkis.metadata.dms.request.validate.table.path",
      "/datasets/details").getValue

  val DMS_REQUEST_TABLE_MASK_METHOD =
    CommonVars.apply("wds.linkis.metadata.dms.request.table.mask.method", "POST")
      .getValue
  val DMS_REQUEST_TABLE_MASK_INFO_PATH =
    CommonVars.apply("wds.linkis.metadata.dms.request.table.mask.info.path",
      "/datamap/detailDatasetMaskInfo").getValue
  val DMS_REQUEST_BDP_TABLE_DETAIL_PATH =
    CommonVars.apply("wds.linkis.metadata.dms.request.bdp.table.detail.path",
      "/datamap/bdpTableDetail").getValue
  val DMS_REQUEST_MASK_COLUMN_LIST_PATH =
    CommonVars.apply("wds.linkis.metadata.dms.request.mask.column.list.path",
      "/datamap/columnMaskInfoList").getValue
}

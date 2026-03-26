SET @@autocommit=0;
START TRANSACTION;

CREATE TABLE `dss_error_code_solution` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `error_desc` varchar(1024) COLLATE utf8_bin DEFAULT NULL COMMENT '错误内容信息',
  `app_name` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'appconn name',
  `uri` varchar(512) COLLATE utf8_bin DEFAULT NULL COMMENT '请求uri',
  `match_content` varchar(1024) COLLATE utf8_bin DEFAULT NULL COMMENT '匹配内容，可以是正则或错误码等方式',
  `match_type` int(11) DEFAULT NULL COMMENT '匹配方式，REGEX或ERRORCODE等',
  `solution` varchar(1024) COLLATE utf8_bin DEFAULT NULL COMMENT '解决方案json体，包含url等信息',
  `update_by` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `is_skipped` int(11) DEFAULT NULL COMMENT '是否跳过该错误，前端不需要展示上报按钮',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dss_error_code_report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `uri` varchar(512) COLLATE utf8_bin DEFAULT NULL COMMENT '请求uri',
  `response_body` text COLLATE utf8_bin COMMENT '响应体',
  `request_headers` text COLLATE utf8_bin COMMENT '请求头',
  `request_body` text COLLATE utf8_bin COMMENT '请求体',
  `query_params` text COLLATE utf8_bin COMMENT '请求参数',
  `error_code` varchar(50) COLLATE utf8_bin DEFAULT NULL COMMENT '错误码',
  `error_desc` varchar(1024) COLLATE utf8_bin DEFAULT NULL COMMENT '错误描述',
  `report_by` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT '上报者',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4;

-- 修正datachecker类名
update dss_appconn set class_name="com.webank.wedatasphere.dss.appconn.datachecker.DataCheckerAppConn" where appconn_name="datachecker";
-- 修正dss_workflow表的project_id
update dss_workflow w  set w.project_id  = (select ov.project_id from dss_orchestrator_version_info ov  where app_id = w.id) where w.project_id is null;




COMMIT;
SET @@autocommit=1;
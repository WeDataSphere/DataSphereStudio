CREATE TABLE `dss_dataset_scan_record` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `path` text COLLATE utf8mb4_bin NOT NULL COMMENT '结果集路径',
  `task_id` varchar(128) COLLATE utf8mb4_bin NOT NULL COMMENT 'linkis任务id',
  `row_size` bigint(11) NOT NULL COMMENT '结果集行数',
  `has_sensitive_info` tinyint(1) DEFAULT NULL COMMENT '是否有敏感',
  `scan_info` longtext COLLATE utf8mb4_bin COMMENT '扫描的结果字段信息',
  `read_history` text COLLATE utf8mb4_bin COMMENT '已经看过结果集的用户、场景（查看、下载、导出等），是一个json',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='结果集敏感信息扫描记录';

CREATE TABLE `dss_dataset_user_usage_cache` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL,
  `usage_quota` bigint(11) NOT NULL COMMENT '流量',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='敏感信息流量,dms缓存,实现dms降级';


alter table dss_workflow_node_content_to_ui add PARTITION(
    PARTITION eventsenderWTSS VALUES IN ('wtss.eventchecker.sender'),
    PARTITION eventreceiverWTSS VALUES IN ('wtss.eventchecker.receiver')
);



UPDATE dss_workflow_node_ui
	SET default_value=NULL
	WHERE `key`='wds.linkis.rm.yarnqueue';
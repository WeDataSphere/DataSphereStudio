CREATE TABLE `dss_project_orchestrator_white` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `project_name` varchar(255) NOT NULL COMMENT '项目名称',
  `orchestrator_id`   bigint(20) COMMENT '编排ID',
  `orchestrator_name` varchar(255) NOT NULL COMMENT '编排名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_by` varchar(255) NOT NULL COMMENT '创建者',
  `update_by` varchar(255)  NOT NULL COMMENT '更新者',
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ;



ALTER TABLE dss_workflow_node_content ADD INDEX idx_orchestrator_id (orchestrator_id);

ALTER TABLE dss_workflow_relation ADD INDEX idx_flow_id (flow_id);

ALTER TABLE dss_error_code_report ADD INDEX idx_report_by_time(report_by,create_time);



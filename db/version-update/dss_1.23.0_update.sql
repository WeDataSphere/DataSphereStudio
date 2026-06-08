CREATE TABLE `dss_workflow_tenant_global_variable_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `request_id` varchar(64) NOT NULL COMMENT '接口请求批次Id',
  `root_flow_id` bigint(20) NOT NULL COMMENT '根工作流Id',
  `flow_id` bigint(20) NOT NULL COMMENT '工作流Id',
  `flow_name` varchar(128) DEFAULT NULL COMMENT '工作流名称',
  `workspace_name` varchar(128) DEFAULT NULL COMMENT '工作空间名称',
  `project_name` varchar(128) DEFAULT NULL COMMENT '项目名称',
  `tenant` varchar(255) NOT NULL COMMENT 'tenant全局变量值',
  `bml_version` varchar(64) DEFAULT NULL COMMENT '保存后的BML版本',
  `operate_user` varchar(64) NOT NULL COMMENT '操作用户',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_tenant_variable_record_request_id` (`request_id`),
  KEY `idx_tenant_variable_record_root_flow_id` (`root_flow_id`),
  KEY `idx_tenant_variable_record_flow_id` (`flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='工作流tenant全局变量修改记录表';

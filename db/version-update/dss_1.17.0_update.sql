create table `dss_send_ims_task` (
   `id` bigint(20) NOT NULL AUTO_INCREMENT,
   `job_id` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '任务ID',
   `alter_title` text COLLATE utf8mb4_bin COMMENT 'ims标题信息',
   `alter_info` longtext COLLATE utf8mb4_bin COMMENT 'ims内容信息',
  `create_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='发送ims任务信息';

ALTER TABLE dss_workflow_node_content ADD INDEX idx_index (node_key,orchestrator_id,flow_id);
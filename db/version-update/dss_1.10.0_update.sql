CREATE TABLE `dss_workspace_default_template` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `template_id` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '参数模板的uuid',
  `workspace_id` bigint(20) DEFAULT NULL COMMENT '项目id',
  `create_user` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` varchar(100) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='工作空间模板关联表';


alter table dss_workspace add enabled_flow_keywords_check tinyint DEFAULT '0' COMMENT '是否开启工作流关键字校验,1、启用 0、禁用';

alter table dss_workspace add is_default_reference tinyint  DEFAULT '0' COMMENT '是否默认引用资源参数模板,1、是 0、否';

alter table dss_orchestrator_info add not_contains_keywords_node longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin COMMENT '不包含关键字的节点信息';
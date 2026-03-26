-- 工作流引用模板表
CREATE TABLE `dss_ec_config_template_workflow`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `template_id` varchar(64) DEFAULT NULL COMMENT '参数模板的uuid',
    `project_id`  bigint(20) DEFAULT NULL COMMENT '项目id',
    `orchestrator_id` bigint(20) DEFAULT NULL COMMENT '编排id',
    `flow_id` bigint(20) DEFAULT NULL COMMENT '工作流id',
    `create_time` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='工作流应用模板表';

-- 工作流默认模板表
CREATE TABLE `dss_workflow_default_template`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `template_id` varchar(64) DEFAULT NULL COMMENT '参数模板的uuid',
    `project_id` bigint(20) DEFAULT NULL COMMENT '项目id',
    `orchestrator_id` bigint(20) DEFAULT NULL COMMENT '编排id',
    `create_user` varchar(100) DEFAULT NULL COMMENT '创建人',
    `create_time` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_user` varchar(100) DEFAULT NULL COMMENT '更新人',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    key `idx_workspace_project` (`project_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='工作流默认模板表';

-- 公告内容表修改，添加创建用户和创建时间
ALTER TABLE `dss_notice` ADD create_user varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '创建用户名';
ALTER TABLE `dss_notice` ADD create_time datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
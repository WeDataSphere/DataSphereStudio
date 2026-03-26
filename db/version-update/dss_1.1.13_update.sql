SET @@autocommit = 0;
START TRANSACTION;


-- 参数模板表
CREATE TABLE `dss_ec_config_template`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `template_id`   varchar(64) NOT NULL UNIQUE COMMENT '参数模板的uuid',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `name`   varchar(64) NOT NULL UNIQUE COMMENT '参数模板名',
    `description`  varchar(256) DEFAULT NULL COMMENT '模板描述',
    `engine_type`   varchar(128) NOT NULL COMMENT '引擎类型',
    `permission_type`   tinyint(1) NOT NULL COMMENT '可见范围类型，0全部可见，1指定用户可见',
    `creator`   varchar(64) NOT NULL COMMENT '创建人',
    `create_time` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modifier`   varchar(64) NOT NULL COMMENT '修改人',
    `modify_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_workspace_id` (`workspace_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='参数模板表';

-- 参数模板可见用户表
CREATE TABLE `dss_ec_config_template_user`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_name`   varchar(64) DEFAULT NULL COMMENT '用户名',
    `template_id`   varchar(64) NOT NULL COMMENT '参数模板的uuid',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `create_time` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_uuid` (`user_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='参数模板可见用户表';

-- 模板应用规则表
CREATE TABLE `dss_ec_config_template_apply_rule`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `rule_id`   varchar(64) NOT NULL UNIQUE COMMENT '规则id',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `rule_type`   tinyint(1) NOT NULL COMMENT '规则类型，0为临时规则，1指定用户可见',
    `template_id`   varchar(64) NOT NULL  COMMENT '关联的参数模板id',
    `template_name`   varchar(64) NOT NULL  COMMENT '关联的参数模板名',
    `engine_type`   varchar(128) NOT NULL COMMENT '引擎类型',
    `engine_name` varchar(128) NOT NULL COMMENT '引擎名（带版本号）',
    `permission_type`   tinyint(1) NOT NULL COMMENT '覆盖范围，0为全部工作空间用户，1指定用户，2为新用户',
    `application`  varchar(128) DEFAULT NULL COMMENT '应用类型',
    `status`   tinyint(1) DEFAULT 0 COMMENT '执行状态：0未执行 1执行成功  2执行失败 3部分失败',
    `creator`   varchar(64) NOT NULL COMMENT '创建人',
    `create_time` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `execute_user`   varchar(64) DEFAULT NULL COMMENT '执行人',
    `execute_time`  datetime    DEFAULT NULL COMMENT '最近执行时间',
    PRIMARY KEY (`id`),
    KEY `idx_workspace_id` (`workspace_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='模板应用规则表';

-- 模板应用规则覆盖用户表
CREATE TABLE `dss_ec_config_template_apply_rule_user`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_name`   varchar(64) DEFAULT NULL COMMENT '用户名',
    `rule_id`   varchar(64) NOT NULL  COMMENT '规则的uuid',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `create_time` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_uuid` (`user_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='模板应用规则覆盖用户表';

-- 模板应用规则执行记录表
CREATE TABLE `dss_ec_config_template_apply_rule_execute_record`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_name`   varchar(64) DEFAULT NULL COMMENT '用户名',
    `rule_id`   varchar(64) NOT NULL COMMENT '规则id',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `template_name`   varchar(64) NOT NULL  COMMENT '关联的参数模板名',
    `engine_type`   varchar(128) NOT NULL  COMMENT '引擎类型',
    `application`  varchar(128) DEFAULT NULL COMMENT '应用类型',
    `status`   tinyint(1) DEFAULT 0 COMMENT '执行状态：0未执行 1执行成功  2执行失败',
    `execute_user`   varchar(64) NOT NULL COMMENT '执行人',
    `execute_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '最近执行时间',
    PRIMARY KEY (`id`),
    KEY `idx_workspace_id` (`workspace_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='模板应用规则执行记录表';

-- 修改组件名字段长度由16字符至24字符
ALTER TABLE dss_workflow_node MODIFY COLUMN name varchar(24) NULL;


COMMIT;
SET @@autocommit = 1;
SET @@autocommit = 0;
START TRANSACTION;
-- EC自动释放规则配置表
CREATE TABLE `dss_ec_release_strategy`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `strategy_id`   varchar(64) NOT NULL UNIQUE COMMENT '规则id',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `name`   varchar(64) NOT NULL COMMENT '规则名',
    `description`  varchar(128) DEFAULT NULL COMMENT '规则描述',
    `queue`   varchar(128) NOT NULL UNIQUE COMMENT '关联队列',
    `trigger_condition_conf`   varchar(1024) NOT NULL COMMENT '触发条件(json)',
    `terminate_condition_conf`   varchar(1024) NOT NULL COMMENT '终止条件(json)',
    `ims_conf`   varchar(2048) NOT NULL COMMENT '告警设置(json)',
    `status`   int(1) DEFAULT 0 COMMENT '规则状态：0禁用 1开启',
    `creator`   varchar(64) NOT NULL COMMENT '创建人',
    `create_time` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modifier`   varchar(64) NOT NULL COMMENT '修改人',
    `modify_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `execute_instance`   varchar(128) DEFAULT NULL COMMENT '最近处理该规则的服务实例',
    `execute_time`   datetime    DEFAULT NULL COMMENT '最近处理该规则的时间起点',
    PRIMARY KEY (`id`),
    KEY `idx_strategy_id` (`strategy_id`),
    KEY `idx_workspace_id` (`workspace_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='EC自动释放规则配置';

-- 工作空间关联的队列表
CREATE TABLE `dss_queue_in_workspace`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `queue`   varchar(128) NOT NULL UNIQUE COMMENT '队列名',
    `apply_user`   varchar(64) NOT NULL COMMENT '申请人',
    `approve_id`   varchar(64) NOT NULL COMMENT '申请单号',
    `create_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='工作空间关联的队列';

-- EC释放通知发送记录表
CREATE TABLE `dss_ec_release_ims_record`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `record_id`   varchar(64) NOT NULL COMMENT '发送记录id',
    `strategy_id`   varchar(64) NOT NULL COMMENT '释放规则id',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `content`   varchar(1024) NOT NULL COMMENT '发送内容',
    `status`   int(1) DEFAULT 0 COMMENT '通知状态：0未发送 1已发送  2发送失败',
    `execute_instance`   varchar(128) NOT NULL COMMENT '负责发送的服务实例',
    `create_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_record_id` (`record_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='EC释放通知发送记录';


-- 请求释放EC历史
CREATE TABLE `dss_ec_kill_history`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `strategy_id`   varchar(64) NOT NULL COMMENT '释放规则id',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `instance`   varchar(128) NOT NULL COMMENT '释放的EC实例名',
    `engine_type`   varchar(64) NOT NULL COMMENT 'EC类型',
    `queue`   varchar(128) NOT NULL COMMENT '队列名',
    `driver_core`   int(11) DEFAULT 0 COMMENT '本地释放核数',
    `driver_memory`   bigint(11) DEFAULT 0 COMMENT '本地释放内存，单位Byte',
    `yarn_core`   int(11) DEFAULT 0 COMMENT 'yarn释放核数',
    `yarn_memory`   bigint(11) DEFAULT 0 COMMENT 'yarn释放内存，单位Byte',
    `unlock_duration`   bigint(11) DEFAULT 0 COMMENT 'EC空闲时长,单位秒',
    `owner`    varchar(64) NOT NULL COMMENT 'EC创建者',
    `killer`    varchar(64) NOT NULL COMMENT 'EC释放触发者',
    `ec_start_time`  varchar(64)   NOT NULL COMMENT 'EC创建的时间',
    `kill_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '请求killEC的时间',
    `create_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `execute_instance`   varchar(128) DEFAULT NULL COMMENT '负责发送的服务实例',
    PRIMARY KEY (`id`),
    KEY `idx_workspace_id` (`workspace_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='请求释放EC历史';


-- 表dss_orchestrator_job_info添加instance_name,status和error_msg字段
ALTER TABLE `dss_orchestrator_job_info` ADD `instance_name` varchar(128) DEFAULT NULL COMMENT '执行任务的实例';
ALTER TABLE `dss_orchestrator_job_info` ADD `status` varchar(128) DEFAULT NULL COMMENT '转换任务状态';
ALTER TABLE `dss_orchestrator_job_info` ADD `error_msg` varchar(2048) DEFAULT NULL COMMENT '转换任务异常信息';
ALTER TABLE `dss_orchestrator_job_info` CHANGE `updated_time` `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE `dss_orchestrator_job_info` MODIFY `job_id` varchar(64) DEFAULT NULL COMMENT '任务id';
-- 表dss_release_task添加instance_name字段
ALTER TABLE `dss_release_task` ADD `instance_name` varchar(128) DEFAULT NULL COMMENT '执行任务的实例' AFTER `status`;
-- 表dss_workflow_task添加instance_name字段
ALTER TABLE `dss_workflow_task` ADD `instance_name` varchar(128) DEFAULT NULL COMMENT '执行任务的实例' AFTER `status`;
-- 表dss_orchestrator_copy_info添加instance_name字段
ALTER TABLE `dss_orchestrator_copy_info` ADD `instance_name` varchar(128) DEFAULT NULL COMMENT '执行任务的实例' AFTER `status`;
-- 表dss_project_copy_task添加instance_name和error_orc字段
ALTER TABLE `dss_project_copy_task` ADD `instance_name` varchar(128) DEFAULT NULL COMMENT '执行任务的实例' AFTER `status`;
ALTER TABLE `dss_project_copy_task` ADD `error_orc` varchar(2048) DEFAULT '' COMMENT '拷贝异常编排' AFTER `error_msg`;
ALTER TABLE `dss_project_copy_task` MODIFY `error_msg` text COMMENT '拷贝异常信息';
-- 表dss_project_operate_record添加instance_name字段
ALTER TABLE `dss_project_operate_record` ADD `instance_name` varchar(128) DEFAULT NULL COMMENT '执行任务的实例' AFTER `status`;
-- 表dss_workspace_user_role添加update_user和update_time字段
ALTER TABLE `dss_workspace_user_role` add `update_user` varchar(32) DEFAULT NULL COMMENT '更新人';
ALTER TABLE `dss_workspace_user_role` add `update_time` datetime DEFAULT NULL COMMENT '更新时间';

DELETE FROM dss_release_note_content;
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
    VALUES ('feature1', '【优化】项目复制完成但有工作流复制出现异常增加错误信息提示','_book/版本动态与公告/v1.1.12.html#1', 1, 1, now());
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
    VALUES ('feature2', '【优化】错误提示优化','_book/版本动态与公告/v1.1.12.html#2', 1, 1, now());
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
    VALUES ('feature3', '【新增】工作空间管理员配置规则自动释放空闲引擎','_book/版本动态与公告/v1.1.12.html#3', 1, 1, now());
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
    VALUES ('feature4', '【功能拓展】工作流节点上下游依赖高亮展示','_book/版本动态与公告/v1.1.12.html#4', 1, 1, now());
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
    VALUES ('feature5', '【新增】库表信息页批量操作增加全选','_book/版本动态与公告/v1.1.12.html#5', 1, 1, now());

COMMIT;
SET @@autocommit = 1;
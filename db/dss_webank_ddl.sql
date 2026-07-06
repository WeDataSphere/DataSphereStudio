SET @@autocommit=0;
START TRANSACTION;



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

CREATE TABLE `dss_itsm` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `itsm_id` VARCHAR(50) NOT NULL,
  `content` LONGTEXT NOT NULL,
  `status` VARCHAR(50) DEFAULT NULL,
  `columns` LONGTEXT,
  PRIMARY KEY (`id`)
) ENGINE=INNODB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ;

CREATE TABLE `dss_orchestrator_schedule_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `orchestrator_id` bigint(20) NOT NULL,
  `project_name` varchar(1024) COLLATE utf8_bin NOT NULL,
  `schedule_user` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  `schedule_time` varchar(4096) COLLATE utf8_bin DEFAULT NULL,
  `alarm_level` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `alarm_user_emails` varchar(4096) COLLATE utf8_bin DEFAULT NULL,
  `last_update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `active_flag` VARCHAR(10)  DEFAULT 'true'  COMMENT '调度标示：true-已启动；false-已禁用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ;

CREATE TABLE `dss_orchestrator_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `orchestrator_id` bigint(20) NOT NULL,
  `project_id` bigint(20) NOT NULL,
  `workspace_id` int(10) NOT NULL DEFAULT '0',
  `username` varchar(100) COLLATE utf8_bin NOT NULL,
  `priv` tinyint(5) NOT NULL DEFAULT '0',
  `last_update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ;

CREATE TABLE `dss_release_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) NOT NULL,
  `orchestrator_version_id` bigint(20) NOT NULL,
  `orchestrator_id` bigint(20) NOT NULL,
  `release_user` varchar(128) NOT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `status` varchar(64) DEFAULT 'init',
  `instance_name` varchar(128) DEFAULT NULL COMMENT '执行任务的实例',
  `error_msg` TEXT DEFAULT NULL COMMENT '发布错误信息',
  `comment` varchar(500) DEFAULT NULL COMMENT '发布描述',
  `log_msg` varchar(255) DEFAULT NULL COMMENT '日志信息或日志路径',
  `bak` varchar(255) DEFAULT NULL COMMENT '备用字段',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=605 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin  ROW_FORMAT=COMPACT;

CREATE TABLE `dss_error_code_solution` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `error_desc` varchar(1024) COLLATE utf8_bin DEFAULT NULL COMMENT '错误内容信息',
  `app_name` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'appconn name',
  `uri` varchar(512) COLLATE utf8_bin DEFAULT NULL COMMENT '请求uri',
  `match_content` varchar(1024) COLLATE utf8_bin DEFAULT NULL COMMENT '匹配内容，可以是正则或错误码等方式',
  `match_type` int(11) DEFAULT NULL COMMENT '匹配方式，1:REGEX或2:ERRORCODE等',
  `solution` varchar(1024) COLLATE utf8_bin DEFAULT NULL COMMENT '解决方案json体，包含url等信息',
  `update_by` varchar(128) COLLATE utf8_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ;

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
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin ;

CREATE TABLE `dss_workspace_associate_departments` (
   `id` bigint(20) NOT NULL AUTO_INCREMENT,
   `workspace_id` bigint(20) DEFAULT NULL,
   `departments` text DEFAULT NULL COMMENT '关联的部门-科室列表，逗号分割，若部门后不接科室则代表关联整个部门',
   `role_ids` varchar(128) DEFAULT NULL COMMENT '角色id列表，逗号分割',
   `create_time` datetime DEFAULT NULL,
   `update_time` datetime DEFAULT NULL,
   `create_by` varchar(128) DEFAULT NULL,
   `update_by` varchar(128) DEFAULT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COLLATE=utf8mb4_bin COMMENT='空间自动加入用户绑定的部门科室信息';

CREATE TABLE `dss_streamis_proxy_user`
(
    `id`              int(11)  NOT NULL AUTO_INCREMENT,
    `user_name`       varchar(64)       DEFAULT NULL COMMENT  '实名用户名',
    `proxy_user_name` varchar(64)       DEFAULT NULL COMMENT  '代理用户名',
    `create_by`       varchar(64)       DEFAULT NULL COMMENT '创建者',
    `create_time`     datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4  COLLATE=utf8mb4_bin COMMENT='流式应用代理用户表';
-- 版本发布时的releaseNote信息
CREATE TABLE `dss_release_note_content`
(
    `id`           int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`         varchar(200) DEFAULT NULL COMMENT '名称',
    `title`        varchar(200) DEFAULT NULL COMMENT '标题',
    `url`          varchar(300) DEFAULT NULL COMMENT 'url',
    `url_type`     int(1)       DEFAULT '1' COMMENT 'url类型: 0-内部系统，1-外部系统；默认是外部',
    `release_type` int(1)       DEFAULT '0' COMMENT '发布形式: 0-作为dss整体发布，1-单独发布scriptis；默认是dss',
    `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET=utf8mb4  COLLATE=utf8mb4_bin COMMENT ='releaseNote表';

-- 用户访问行为统计表，初期只有登录行为统计
CREATE TABLE `dss_user_access_audit`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_name`   varchar(64) DEFAULT NULL COMMENT '用户名',
    `login_count` BIGINT      DEFAULT 0 COMMENT '登录次数',
    `first_login` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '第一次登录时间',
    `last_login`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '上一次登录时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_name` (`user_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin  COMMENT ='用户访问行为次数统计';

-- 首页公告表
CREATE TABLE `dss_notice` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `content` text CHARACTER SET utf8mb4 COMMENT '公告内容',
    `start_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
    `end_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '失效时间',
    `create_user` varchar(255) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT '创建用户名',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='首页公告内容';

-- EC自动释放规则配置表
CREATE TABLE `dss_ec_release_strategy`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `strategy_id`   varchar(64) NOT NULL UNIQUE COMMENT '规则id',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `name`   varchar(64) NOT NULL COMMENT '规则名',
    `description`  varchar(128) DEFAULT NULL COMMENT '规则描述',
    `queue`   varchar(128) NOT NULL COMMENT '关联队列',
    `cross_cluster`   tinyint(1) DEFAULT 0 COMMENT '是否跨集群,0为否，1为是',
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
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT ='EC自动释放规则配置';

-- 工作空间关联的队列表
CREATE TABLE `dss_queue_in_workspace`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `queue`   varchar(128) NOT NULL  COMMENT '队列名',
    `cross_cluster`   tinyint(1) DEFAULT 0 COMMENT '是否跨集群,0为否，1为是',
    `apply_user`   varchar(64) NOT NULL COMMENT '申请人',
    `approve_id`   varchar(64) NOT NULL COMMENT '申请单号',
    `create_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT ='工作空间关联的队列';

-- EC释放通知发送记录表
CREATE TABLE `dss_ec_release_ims_record`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `record_id`   varchar(64) NOT NULL COMMENT '发送记录id',
    `strategy_id`   varchar(64) NOT NULL COMMENT '释放规则id',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `content`   text NOT NULL COMMENT '发送内容',
    `status`   int(1) DEFAULT 0 COMMENT '通知状态：0未发送 1已发送  2发送失败',
    `execute_instance`   varchar(128) NOT NULL COMMENT '负责发送的服务实例',
    `create_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_record_id` (`record_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT ='EC释放通知发送记录';


-- 请求释放EC历史
CREATE TABLE `dss_ec_kill_history`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
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
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT ='请求释放EC历史';

-- 参数模板表
CREATE TABLE `dss_ec_config_template`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `template_id`   varchar(64) NOT NULL UNIQUE COMMENT '参数模板的uuid',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `name`   varchar(128) NOT NULL UNIQUE COMMENT '参数模板名',
    `description`  varchar(256) DEFAULT NULL COMMENT '模板描述',
    `engine_type`   varchar(128) NOT NULL COMMENT '引擎类型',
    `permission_type`   tinyint(1) NOT NULL COMMENT '可见范围类型，0全部可见，1指定用户可见',
    `creator`   varchar(64) NOT NULL COMMENT '创建人',
    `create_time` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modifier`   varchar(64) NOT NULL COMMENT '修改人',
    `modify_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `default_for_ai_sql`  tinyint(1) DEFAULT 0 COMMENT '是否为aisql默认模板，0否，1是',
    PRIMARY KEY (`id`),
    KEY `idx_workspace_id` (`workspace_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT ='参数模板表';

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
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT ='参数模板可见用户表';

-- 模板应用规则表
CREATE TABLE `dss_ec_config_template_apply_rule`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `rule_id`   varchar(64) NOT NULL UNIQUE COMMENT '规则id',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `rule_type`   tinyint(1) NOT NULL COMMENT '规则类型，0为临时规则，1指定用户可见',
    `template_id`   varchar(64) NOT NULL  COMMENT '关联的参数模板id',
    `template_name`   varchar(128) NOT NULL  COMMENT '关联的参数模板名',
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
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT ='模板应用规则表';

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
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT ='模板应用规则覆盖用户表';

-- 模板应用规则执行记录表
CREATE TABLE `dss_ec_config_template_apply_rule_execute_record`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_name`   varchar(64) DEFAULT NULL COMMENT '用户名',
    `rule_id`   varchar(64) NOT NULL COMMENT '规则id',
    `workspace_id`   bigint(20) NOT NULL COMMENT '工作空间id',
    `template_name`   varchar(128) NOT NULL  COMMENT '关联的参数模板名',
    `engine_type`   varchar(128) NOT NULL  COMMENT '引擎类型',
    `application`  varchar(128) DEFAULT NULL COMMENT '应用类型',
    `status`   tinyint(1) DEFAULT 0 COMMENT '执行状态：0未执行 1执行成功  2执行失败',
    `execute_user`   varchar(64) NOT NULL COMMENT '执行人',
    `execute_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '最近执行时间',
    PRIMARY KEY (`id`),
    KEY `idx_workspace_id` (`workspace_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT ='模板应用规则执行记录表';

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
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT ='工作流应用模板表';

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
  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT ='工作流默认模板表';

CREATE TABLE `dss_ec_config_template_apply_rule_department` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `department_name` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '部门名称',
  `rule_id` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '规则的uuid',
  `workspace_id` bigint(20) NOT NULL COMMENT '工作空间id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_template_uuid` (`department_name`)
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='模板应用规则覆盖部门表';

-- 发送ims任务信息表
CREATE TABLE `dss_send_ims_task` (
   `id` bigint(20) NOT NULL AUTO_INCREMENT,
   `job_id` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '任务ID',
   `alter_title` text COLLATE utf8mb4_bin COMMENT 'ims标题信息',
   `alter_info` longtext COLLATE utf8mb4_bin COMMENT 'ims内容信息',
   `create_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `update_time` timestamp DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='发送ims任务信息';

-- azkaban调取系统消息队列表
CREATE TABLE `event_queue` (
  `msg_id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '消息ID号',
  `sender` varchar(45) NOT NULL COMMENT '消息发送者',
  `send_time` datetime NOT NULL COMMENT '消息发送时间',
  `topic` varchar(45) NOT NULL COMMENT '消息主题',
  `msg_name` varchar(45) NOT NULL COMMENT '消息名称',
  `msg` varchar(250) DEFAULT NULL COMMENT '消息内容',
  `send_ip` varchar(45) NOT NULL,
  `run_date` VARCHAR(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '信号的run_date',
  PRIMARY KEY (`msg_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21068 DEFAULT CHARSET=utf8mb4 COMMENT='azkaban调取系统消息队列表';


CREATE TABLE `event_status` (
  `receiver` varchar(45) NOT NULL COMMENT '消息接收者',
  `receive_time` datetime NOT NULL COMMENT '消息接收时间',
  `topic` varchar(45) NOT NULL COMMENT '消息主题',
  `msg_name` varchar(45) NOT NULL COMMENT '消息名称',
  `msg_id` int(11) NOT NULL COMMENT '消息的最大消费id',
  PRIMARY KEY (`receiver`,`topic`,`msg_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息消费状态表';


COMMIT;
SET @@autocommit=1;
SET @@autocommit=0;
START TRANSACTION;

CREATE TABLE `dss_project_operate_record`
(
    `id`                  bigint(20)   NOT NULL AUTO_INCREMENT,
    `record_id`           varchar(64)  NOT NULL,
    `workspace_id`        bigint(20)   NOT NULL COMMENT '空间id',
    `project_id`          bigint(20)   NOT NULL COMMENT '项目id',
    `operate_type`        int(11)      NOT NULL COMMENT '操作类型',
    `status`              int(11)      NOT NULL COMMENT '操作状态',
    `instance_name`     VARCHAR(128) DEFAULT NULL COMMENT '执行任务的实例',
    `content`             longtext DEFAULT NULL COMMENT '操作内容详情',
    `result_resource_uri` text     DEFAULT NULL COMMENT '操作结果资源的uri，是一个json',
    `creator`             varchar(100) NOT NULL,
    `create_time`         datetime     NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_record_id` (`record_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目操作记录表';

CREATE TABLE `dss_streamis_proxy_user`
(
    `id`              int(11)  NOT NULL AUTO_INCREMENT,
    `user_name`       varchar(64)       DEFAULT NULL COMMENT  '实名用户名',
    `proxy_user_name` varchar(64)       DEFAULT NULL COMMENT  '代理用户名',
    `create_by`       varchar(64)       DEFAULT NULL COMMENT '创建者',
    `create_time`     datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT='流式应用代理用户表';

COMMIT;
SET @@autocommit=1;
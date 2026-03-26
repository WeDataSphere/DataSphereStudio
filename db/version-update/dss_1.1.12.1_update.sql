SET @@autocommit = 0;
START TRANSACTION;

alter table dss_apiservice_approval add column `duration` bigint(20) NOT NULL COMMENT '授权时间，单位天',
 add column `background_desc` text NOT NULL COMMENT '背景描述',
 add column `importance` int(11) NOT NULL COMMENT '业务重要度',
 add column `attention_user` varchar(255) NOT NULL COMMENT '关注人';

alter table dss_apiservice_api_version add column `execute_user` varchar(255) NOT NULL COMMENT '代理执行用户';
alter table dss_apiservice_api_version add column `description` text  NOT NULL COMMENT '描述';
alter table dss_apiservice_api_version add column `comment` text NOT NULL COMMENT '备注';

CREATE TABLE `dss_apiservice_job`
(
    `id`          bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `submit_user`   varchar(64) DEFAULT NULL COMMENT '提交用户',
    `proxy_user`   varchar(64) DEFAULT NULL COMMENT '代理用户',
    `task_id`   varchar(256) DEFAULT NULL COMMENT '任务id',
    `exec_id`   varchar(256) DEFAULT NULL COMMENT '执行job id',
    `user`   varchar(64) DEFAULT NULL COMMENT '用户',
    `create_time`  datetime  DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='数据服务发布表';

COMMIT;
SET @@autocommit = 1;
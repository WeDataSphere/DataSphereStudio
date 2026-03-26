-- 增加 工作空间关联git表，存储git token password等信息
CREATE TABLE `dss_workspace_associate_git` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
   `workspace_id` bigint(20) DEFAULT NULL,
   `git_user`  varchar(64)  DEFAULT NULL COMMENT  'git登录用户名',
   `git_password`  VARCHAR(255)  DEFAULT NULL COMMENT  'git登录密码，用于跳转',
   `git_token`  varchar(255) COMMENT  '用户配置的git token',
   `git_url` varchar(255),
   `create_time` datetime DEFAULT NULL,
   `update_time` datetime DEFAULT NULL,
   `create_by` varchar(128) DEFAULT NULL,
   `update_by` varchar(128) DEFAULT NULL,
   `git_user_id` varchar(20) DEFAULT NULL,
   `type` varchar(32) DEFAULT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='工作空间绑定的git信息';

CREATE TABLE `dss_orchestrator_submit_job_info` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `orchestrator_id` bigint(20) NOT NULL,
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `instance_name` varchar(128) DEFAULT NULL COMMENT '提交任务的实例',
    `status` varchar(128) DEFAULT NULL COMMENT '提交任务状态',
    `error_msg` varchar(2048) DEFAULT NULL COMMENT '提交任务异常信息',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='dss_orchestrator_submit_job_info表';

CREATE TABLE `dss_project_associate_git` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
   `git_project_id` varchar(20) DEFAULT NULL,
   `project_name`  varchar(64)  DEFAULT NULL COMMENT  '项目名',
   `workspace_id` bigint(20) DEFAULT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='项目绑定的project的git信息';

alter table dss_apiservice_param ADD  max_length int(8) NULL COMMENT '最大长度';

-- 增加 工作流提交状态字段
ALTER TABLE dss_orchestrator_info ADD status VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

-- 增加 commit_id用于记录各个版本发布时的commit_id
ALTER TABLE dss_orchestrator_version_info ADD commit_id varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

-- 增加 project 接入git标识
ALTER TABLE dss_project ADD associate_git TINYINT DEFAULT '0' COMMENT '0:未接入git，1:已接入git';
ALTER TABLE dss_project ADD data_source_list_json TEXT  CHARACTER SET utf8mb4 COLLATE utf8mb4_bin COMMENT '项目数据源配置，json格式';


SET @@autocommit=0;
START TRANSACTION;

CREATE TABLE `dss_project_copy_task` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `workspace_id` BIGINT(20) COMMENT '空间ID',
  `source_project_id` BIGINT(20) COMMENT '(源)复制工程ID',
  `source_project_name` VARCHAR(200) COMMENT '(源)复制工程名称',
  `copy_project_id` BIGINT(20) COMMENT '复制工程ID',
  `copy_project_name` VARCHAR(200) COMMENT '复制工程名称',
  `surplus_count` INT(3) COMMENT '剩余复制数量',
  `sum_count` INT(3) COMMENT '总数',
  `status` INT(1) COMMENT '状态 0:初始化，1：复制中，2：复制成功',
  `instance_name` VARCHAR(128) DEFAULT NULL COMMENT '执行任务的实例',
  `create_by` VARCHAR(200) COMMENT '创建人',
  `create_time` datetime COMMENT '创建时间',
  `update_time` datetime COMMENT '上个复制时间',
  `error_msg` text COMMENT '失败原因',
  `error_orc` VARCHAR(2048) DEFAULT '' COMMENT '拷贝异常编排',
  PRIMARY KEY (`id`)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='复制工程任务表';

ALTER TABLE `dss_workflow_execute_info`
   ADD COLUMN `status` INT(1) NULL COMMENT '状态，0：失败 1：成功，' AFTER `task_id`,
   ADD COLUMN `version` VARCHAR(200) NULL COMMENT '工作流bml版本号' AFTER `flow_id`,
   ADD COLUMN `updatetime` DATETIME NULL COMMENT '更新时间' AFTER `running_jobs`,
   CHANGE `task_id` `task_id` BIGINT(20) NOT NULL COMMENT '任务id';

--需要先添加一条记录，先删除原来spark节点内存校验的关联关系
INSERT INTO `dss_workflow_node_ui_validate` (`validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) VALUES ('Regex', '^[0-9.]*g{0,1}$', 'Spark内存设置如2g', 'Drive memory size, default value: 2', 'blur');


INSERT  INTO `dss_dictionary`(`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`)
 VALUES (0,'workflow_check_switch','workflowCheckSwitch','workflowCheckSwitch','workflow_check_switch',0,NULL,NULL,NULL,NULL,0,NULL,1,'发布校验工作流开关，1：开,0:关','SYSTEM',NOW(),NULL,NOW());


 CREATE TABLE `dss_orchestrator_job_info` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `job_id` varchar(64) DEFAULT NULL COMMENT 'job ID',
    `conversion_job_json` varchar(1024) DEFAULT NULL COMMENT 'job信息',
    `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
    `instance_name` varchar(128) DEFAULT NULL COMMENT '执行任务的实例',
    `status` varchar(128) DEFAULT NULL COMMENT '转换任务状态',
    `error_msg` varchar(2048) DEFAULT NULL COMMENT '转换任务异常信息',
    PRIMARY KEY (`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='dss_orchestrator_job_info表';


-- 用于数据服务的代码搜索的脚本，不是dss数据库使用
--  select *
--  from
--  (select * from  `stacyyan_ind`.`linkis_ps_job_history_detail` A left join  `stacyyan_ind`.`linkis_ps_job_history_group_history` B on A.job_history_id=B.id ) C
--  where
--  C.execution_content like '%${code_search}%' and C.submit_user='${dss_api_submit_user}'

COMMIT;
SET @@autocommit=1;

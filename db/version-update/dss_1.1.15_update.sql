-- 新增引擎内存
INSERT  INTO `dss_workflow_node_ui`
(`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`)
values
('wds.linkis.engineconn.java.driver.memory','hive引擎内存，默认值：1G','hive driver memory, default：1G','wds.linkis.engineconn.java.driver.memory','wds-linkis-engineconn.java.driver.memory','Input',0,NULL,'1G',0,NULL,0,1,1,0,'runtime');

-- 建立hive关联关系
INSERT INTO `dss_workflow_node_to_ui` (`workflow_node_id`, `ui_id`)
VALUES (
    (SELECT id FROM `dss_workflow_node` WHERE name = 'hql'),
    (SELECT id FROM `dss_workflow_node_ui` WHERE `key` = 'wds.linkis.engineconn.java.driver.memory')
);

-- 增加校验
insert into `dss_workflow_node_ui_validate` (`validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`)
values('Regex', '^([1-9]|10|[1-9])(g|G){0,1}$', '设置范围为[1,10],设置超出限制', 'hive memory limit 1,10', 'blur');

insert into `dss_workflow_node_ui_validate` (`validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`)
values('Regex', '^((source.type|check.object).\\w+=(\\w(\\.)*(\\{(.+?)\\})*)+[;\n]*)+$', '请正确填写多源配置', 'params config error, please make sure!', 'blur');

insert into `dss_workflow_node_ui_validate` (`validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`)
values('Function', 'validateJobDesc', 'check.object配置重复,请检查', 'exist check.object repeat please check!', 'blur');

-- 关联校验
INSERT INTO `dss_workflow_node_ui_to_validate` (`ui_id`, `validate_id`)
VALUES (
    (SELECT id FROM `dss_workflow_node_ui` WHERE `key` = 'wds.linkis.engineconn.java.driver.memory'),
    (SELECT id FROM `dss_workflow_node_ui_validate` WHERE error_msg = '设置范围为[1,10],设置超出限制' AND error_msg_en = 'hive memory limit 1,10')
);

INSERT INTO `dss_workflow_node_ui_to_validate` (`ui_id`, `validate_id`)
VALUES (
    (SELECT id FROM `dss_workflow_node_ui` WHERE `key` = 'job.desc'),
    (SELECT id FROM `dss_workflow_node_ui_validate` WHERE error_msg = '请正确填写多源配置' AND error_msg_en = 'params config error, please make sure!')
);

INSERT INTO `dss_workflow_node_ui_to_validate` (`ui_id`, `validate_id`)
VALUES (
    (SELECT id FROM `dss_workflow_node_ui` WHERE `key` = 'job.desc'),
    (SELECT id FROM `dss_workflow_node_ui_validate` WHERE error_msg = 'check.object配置重复,请检查' AND error_msg_en = 'exist check.object repeat please check!')
);

-- job.desc描述文案修改
update
	`dss_workflow_node_ui`
set
	`description` = '请正确填写多源配置，数据源编号从01开始，按行分割。如：\nsource.type.01=hivedb\ncheck.object.01=db.tb{ds=${run_date}}\ncheck.object.02=db2.tb{ds=${run_date}}\nsource.type可不填，系统将根据库名自动识别数据来源，如：\ncheck.object.01=db.tb{ds=${run_date}}\ncheck.object.02=db2.tb{ds=${run_date}}'
where
	`key` = 'job.desc';

-- 工作流节点选择引用模板，隐藏参数
-- spark、pyspark、scala、hive节点
update
    `dss_workflow_node_ui`
set
    `condition` = "!${params.configuration.startup['ec.conf.templateId']}"
where
    `key` in ('spark.executor.memory','spark.executor.cores','spark.executor.instances','wds.linkis.engineconn.java.driver.memory','spark.conf','spark.driver.memory');

-- spark.executor.cores参数描述修改
update
    `dss_workflow_node_ui`
set
   `description` = "执行器核心个数，默认值：2", description_en="Number of cores per executor, default value: 2"
where
   `key` = 'spark.executor.cores';

CREATE TABLE `dss_user_limit`
(
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
`limit_name` varchar(64) NOT NULL COMMENT '限制项名称',
`value` varchar(128) NOT NULL COMMENT '限制项value',
`user_name` varchar(1024) DEFAULT NULL COMMENT '限制用户',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
PRIMARY KEY (`id`),
key `idx_limit_name` (`limit_name`)
) ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='dss用户限制表';

CREATE TABLE `dss_ec_config_template_apply_rule_department` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `department_name` varchar(64) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '部门名称',
  `rule_id` varchar(64) COLLATE utf8mb4_bin NOT NULL COMMENT '规则的uuid',
  `workspace_id` bigint(20) NOT NULL COMMENT '工作空间id',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_template_uuid` (`department_name`)
) ENGINE=InnoDB CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='模板应用规则覆盖部门表';



SET @@autocommit=0;
START TRANSACTION;

ALTER TABLE dss_orchestrator_info ADD is_default_reference tinyint(1) NULL COMMENT '是否默认引用资源参数模板';

COMMIT;
SET @@autocommit=1;


-- 更新query_params字段类型为text
ALTER TABLE dss_apiservice_access_info MODIFY COLUMN query_params mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

-- 回退  query_params字段类型为varchar(512)
-- ALTER TABLE dss_apiservice_access_info MODIFY COLUMN query_params varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin;

-- 更新content字段类型为text
ALTER TABLE dss_ec_release_ims_record MODIFY COLUMN content text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;

-- 回退 content字段类型为varchar(1024)
-- ALTER TABLE dss_ec_release_ims_record MODIFY COLUMN content varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;



INSERT INTO dss_workflow_node_ui (`key`,description,description_en,lable_name,lable_name_en,ui_type,required,value,default_value,is_hidden,`condition`,is_advanced,`order`,node_menu_type,is_base_info,`position`) VALUES
    ('auto.disabled','默认为false，若禁用，工作流执行和调度会跳过该节点','default false','是否禁用节点','auto.disabled','Select',1,'["true","false"]',NULL,0,NULL,0,1,1,0,'special');

insert into dss_workflow_node_ui_to_validate (ui_id,validate_id)  values (
                                                                             (select id  from `dss_workflow_node_ui` WHERE `key` = 'auto.disabled' limit 1),
                                                                             (select id  from dss_workflow_node_ui_validate  where  validate_type='None' limit 1)
                                                                         );

INSERT INTO dss_workflow_node_to_ui (workflow_node_id, ui_id)
SELECT id, (SELECT id FROM `dss_workflow_node_ui` WHERE `key` = 'auto.disabled' limit 1) FROM dss_workflow_node;


-- 更新datachecker job.desc验证信息
update dss_workflow_node_ui_validate set validate_range = 'validateJobDesc',validate_type = 'Function' where error_msg ='请正确填写多源配置' and error_msg_en = 'params config error, please make sure!';

update dss_workflow_node_ui_validate set validate_range = 'validateJobDescDuplication',validate_type = 'Function',error_msg ='check.object.xx或source.type.xx重复,请检查' where error_msg_en in ('exist check.object.xx please check!','exist check.object repeat please check!');


-- 取消job.desc 中文限制
delete from  dss_workflow_node_ui_validate where error_msg_en = 'Chinese characters are not allowed';





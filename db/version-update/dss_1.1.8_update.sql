SET @@autocommit=0;
START TRANSACTION;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='空间自动加入用户绑定的部门科室信息';

ALTER TABLE dss_release_task MODIFY COLUMN error_msg TEXT NULL COMMENT '发布错误信息';

ALTER TABLE dss_workspace ADD COLUMN (`admin_permission` tinyint(1) DEFAULT 1 NOT NULL COMMENT '工作空间管理员是否有权限查看该空间下所有项目，1可以，0不可以');
-- 此处只针对企业风险的工作空间做更新
UPDATE dss_workspace set admin_permission = 0 where name = 'BRM_WORKSPACE';


INSERT INTO dss_workflow_node_ui
( `key`, description, description_en, lable_name, lable_name_en, ui_type, required, value, default_value, is_hidden, `condition`, is_advanced, `order`, node_menu_type, is_base_info, `position`)
VALUES('spark.conf', 'spark自定义参数配置输入，例如spark.sql.shuffle.partitions=10。多个参数使用分号分隔。', 'input spark params config, eg: spark.sql.shuffle.partitions=10. Use semi-colon to split multi-params', 'spark.conf', 'spark.conf', 'Text', 0, NULL, "", 0, NULL, 0, 1, 1, 0, 'startup');

select @sparkConfUiId:=id from dss_workflow_node_ui where `key`="spark.conf";
select @sqlNodeId:=id from dss_workflow_node where node_type="linkis.spark.sql";
select @pysparkNodeId:=id from dss_workflow_node where node_type="linkis.spark.py";
select @scalaNodeId:=id from dss_workflow_node where node_type="linkis.spark.scala";

insert into dss_workflow_node_to_ui(`workflow_node_id`,`ui_id`) values(@sqlNodeId, @sparkConfUiId);
insert into dss_workflow_node_to_ui(`workflow_node_id`,`ui_id`) values(@pysparkNodeId, @sparkConfUiId);
insert into dss_workflow_node_to_ui(`workflow_node_id`,`ui_id`) values(@scalaNodeId, @sparkConfUiId);

select @len500valId:=id from dss_workflow_node_ui_validate where error_msg="长度在1到500个字符";

insert into dss_workflow_node_ui_to_validate(`ui_id`,`validate_id`) values(@sparkConfUiId, @len500valId);


COMMIT;
SET @@autocommit=1;
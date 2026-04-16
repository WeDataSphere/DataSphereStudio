insert into `dss_workflow_node` (`name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`, `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`)
values ('branch','scriptis','workflow.branch','0','0','0','1','0','svgs/branch-node.svg');

insert  into `dss_workflow_node_to_group`(`node_id`,`group_id`) values (
(select id from dss_workflow_node where node_type = 'workflow.branch' limit 1),
(select id from dss_workflow_node_group where name_en='Function node' limit 1)
);

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values (
(select id from dss_workflow_node where node_type = 'workflow.branch' limit 1),
(select id from dss_workflow_node_ui where `key` ='title' and node_menu_type = '1' limit 1)
);

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values (
(select id from dss_workflow_node where node_type = 'workflow.branch' limit 1),
(select id from dss_workflow_node_ui where `key` ='desc' and node_menu_type = '1' limit 1)
);

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values (
(select id from dss_workflow_node where node_type = 'workflow.branch' limit 1),
(select id from dss_workflow_node_ui where `key` ='businessTag' limit 1)
);

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values (
(select id from dss_workflow_node where node_type = 'workflow.branch' limit 1),
(select id from dss_workflow_node_ui where `key` ='appTag' limit 1)
);


INSERT  INTO `dss_workflow_node_ui`(`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`)
values ('branch.rules','请填写分支规则，格式如condition.1=amount>100;on.success.1=节点A;on.failure.1=节点B','Please enter branch rules, such as condition.1=amount>100;on.success.1=NodeA;on.failure.1=NodeB','分支规则','Branch rules','Text',1,NULL,NULL,0,NULL,0,2,1,0,'special');

insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (
(select id from dss_workflow_node where node_type = 'workflow.branch' limit 1),
(select id from dss_workflow_node_ui where `key` ='branch.rules' limit 1)
);

insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (
(select id from dss_workflow_node_ui where `key` ='branch.rules' limit 1),
(select id  from dss_workflow_node_ui_validate  where  validate_type='Required' limit 1)
);


alter table dss_workflow_node_content_to_ui add PARTITION(PARTITION `branch` VALUES IN ('workflow.branch'));
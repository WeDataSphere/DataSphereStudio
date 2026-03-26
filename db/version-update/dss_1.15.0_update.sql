-- 添加sparkVersion属性
INSERT INTO `dss_workflow_node_ui` (`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`)
values ('sparkVersion','请选择 Spark版本','Please choose the Spark version','Spark版本','sparkVersion','Select',1,'["2","3"]','2',0,NULL,0,1,1,0,'runtime');

-- 建立sparkVersion属性与节点的关联关系
INSERT INTO `dss_workflow_node_to_ui` (`workflow_node_id`, `ui_id`)
VALUES (
    (SELECT id FROM `dss_workflow_node` WHERE name = 'sql' limit 1),
    (SELECT id FROM `dss_workflow_node_ui` WHERE `key` = 'sparkVersion' limit 1)
);

INSERT INTO `dss_workflow_node_to_ui` (`workflow_node_id`, `ui_id`)
VALUES (
    (SELECT id FROM `dss_workflow_node` WHERE name = 'scala' limit 1),
    (SELECT id FROM `dss_workflow_node_ui` WHERE `key` = 'sparkVersion' limit 1)
);


INSERT INTO `dss_workflow_node_to_ui` (`workflow_node_id`, `ui_id`)
VALUES (
    (SELECT id FROM `dss_workflow_node` WHERE name = 'pyspark' limit 1),
    (SELECT id FROM `dss_workflow_node_ui` WHERE `key` = 'sparkVersion' limit 1)
);


INSERT INTO dss_workflow_node_ui_to_validate (ui_id,validate_id)
values (
     (select id  from `dss_workflow_node_ui` WHERE `key` = 'sparkVersion' limit 1),
     (select id  from dss_workflow_node_ui_validate  where  validate_type='None' limit 1)
);
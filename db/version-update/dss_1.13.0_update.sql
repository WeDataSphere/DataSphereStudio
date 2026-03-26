CREATE TABLE `dss_workspace_starrocks_cluster` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `workspace_id` bigint(20) NOT NULL COMMENT '工作空间id',
  `workspace_name` varchar(255) COLLATE utf8mb4_bin NOT NULL COMMENT '工作空间名',
  `cluster_name` varchar(256) COLLATE utf8mb4_bin NOT NULL COMMENT '集群名',
  `cluster_ip` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '集群ip',
  `http_port` int(11) NOT NULL COMMENT 'http端口',
  `tcp_port` int(11) NOT NULL COMMENT 'tcp端口',
  `is_default_cluster` tinyint(1) DEFAULT NULL COMMENT '是否默认集群，1是，0否',
  `create_user` varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_user` varchar(128) COLLATE utf8mb4_bin DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='工作空间StarRocks集群配置';


alter table dss_proxy_user add column expire_time datetime NOT NULL DEFAULT '2099-12-31 23:59:59' COMMENT '过期时间';


INSERT INTO dss_workflow_node_ui (`key`,description,description_en,lable_name,lable_name_en,ui_type,required,value,default_value,is_hidden,`condition`,is_advanced,`order`,node_menu_type,is_base_info,`position`) VALUES
    ('disableEdit','默认为false,若禁用,节点无法进行编辑','default false','是否禁用节点编辑功能','disableEdit','Select',1,'["true","false"]',NULL,1,NULL,0,1,1,0,'special');

INSERT INTO dss_workflow_node_to_ui (workflow_node_id, ui_id)
SELECT id, (SELECT id FROM `dss_workflow_node_ui` WHERE `key` = 'disableEdit' limit 1) FROM dss_workflow_node;

 alter table  dss_ec_config_template  add column   `default_for_ai_sql`  tinyint(1) DEFAULT 0 COMMENT '是否为aisql默认模板，0否，1是';


INSERT INTO dss_workflow_node (name,appconn_name,node_type,jump_type,support_jump,submit_to_scheduler,enable_copy,should_creation_before_node,icon_path)
VALUES ('starrocks','scriptis','linkis.jdbc.starrocks',2,1,1,1,0,'svgs/starrocks-node.svg');

INSERT INTO dss_workflow_node_to_group (node_id,group_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.jdbc.starrocks') ,(select id from dss_workflow_node_group where name_en = 'Data development'));

INSERT INTO dss_workflow_node_ui (`key`,description,description_en,lable_name,lable_name_en,ui_type,required,value,default_value,is_hidden,`condition`,is_advanced,`order`,node_menu_type,is_base_info,`position`)
VALUES ('executeCluster','请选择执行集群','Please choose execute cluster','选择执行集群','executeCluster','Select',1,'/api/rest_j/v1/dss/framework/workspace/getWorkspaceStarRocksCluster',NULL,0,NULL,0,1,1,0,'runtime');

alter table dss_workflow_node_content_to_ui add PARTITION(PARTITION `starrocks` VALUES IN ('linkis.jdbc.starrocks'));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.jdbc.starrocks'),(select id from dss_workflow_node_ui where `key` = 'title' and node_menu_type = '1' ));
INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.jdbc.starrocks'),(select id from dss_workflow_node_ui where `key` = 'desc' and node_menu_type = '1' ));
INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.jdbc.starrocks'),(select id from dss_workflow_node_ui where `key` = 'businessTag'));
INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.jdbc.starrocks'),(select id from dss_workflow_node_ui where `key` = 'appTag'));
INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.jdbc.starrocks'),(select id from dss_workflow_node_ui where `key` = 'ReuseEngine'));
INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.jdbc.starrocks'),(select id from dss_workflow_node_ui where `key` = 'auto.disabled'));
INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.jdbc.starrocks'),(select id from dss_workflow_node_ui where `key` = 'executeCluster'));
INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.jdbc.starrocks'),(select id from dss_workflow_node_ui where `key` = 'disableEdit'));
INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.jdbc.starrocks'),(select id from dss_workflow_node_ui where `key` = 'wds.linkis.engine.runtime.priority'));

INSERT INTO dss_workflow_node_ui_to_validate (ui_id,validate_id) VALUES
((SELECT  id FROM dss_workflow_node_ui where `key`='executeCluster'), (SELECT id FROM `dss_workflow_node_ui_validate` WHERE validate_type = 'Required'));



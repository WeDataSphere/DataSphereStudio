INSERT INTO dss_workflow_node (name,appconn_name,node_type,jump_type,support_jump,submit_to_scheduler,enable_copy,should_creation_before_node,icon_path)
VALUES ('aisql','scriptis','linkis.ai.sql',2,1,1,1,0,'svgs/ai.svg');

INSERT INTO dss_workflow_node_to_group (node_id,group_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1) ,(select id from dss_workflow_node_group where name_en = 'Data development' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='title' and node_menu_type = '1' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='desc' and node_menu_type = '1' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='businessTag' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='appTag' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='spark.driver.memory' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='spark.executor.memory' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='spark.executor.cores' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='spark.executor.instances' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='wds.linkis.rm.yarnqueue' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='resources' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='ReuseEngine' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='spark.conf' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='auto.disabled' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='wds.linkis.engine.runtime.priority' limit 1));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id) values ((select id from dss_workflow_node where node_type = 'linkis.ai.sql' limit 1),(select id from dss_workflow_node_ui where `key` ='disableEdit' limit 1));

alter table dss_workflow_node_content_to_ui add PARTITION(PARTITION `aisql` VALUES IN ('linkis.ai.sql'));
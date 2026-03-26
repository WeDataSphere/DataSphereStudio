
ALTER TABLE dss_workflow_node_content_to_ui ADD node_type VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin COMMENT '节点类型，用于分区';

ALTER TABLE dss_workflow_node_content_to_ui ADD node_content_type VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin COMMENT '内容类型，整数-NumInterval, 内存-Memory, 字符串-String';

ALTER TABLE dss_workflow_node_content ADD flow_id BIGINT  COMMENT '工作流Id';

ALTER TABLE dss_workflow_node_content_to_ui DROP PRIMARY KEY;

ALTER TABLE dss_workflow_node_content_to_ui ADD PRIMARY KEY (`content_id`, `node_ui_key`, `node_type`);

ALTER TABLE `dss_workflow_node_content_to_ui`
PARTITION BY LIST COLUMNS(`node_type`) (
    PARTITION `python` VALUES IN ('linkis.python.python'),
    PARTITION `pyspark` VALUES IN ('linkis.spark.py'),
    PARTITION `sql` VALUES IN ('linkis.spark.sql'),
    PARTITION `scala` VALUES IN ('linkis.spark.scala'),
    PARTITION `hql` VALUES IN ('linkis.hive.hql'),
	PARTITION `jdbc` VALUES IN ('linkis.jdbc.jdbc'),
	PARTITION `shell` VALUES IN ('linkis.shell.sh'),
	PARTITION `connector` VALUES IN ('linkis.control.empty'),
	PARTITION `subFlow` VALUES IN ('workflow.subflow'),
    PARTITION `datachecker` VALUES IN ('linkis.appconn.datachecker'),
    PARTITION `eventsender` VALUES IN ('linkis.appconn.eventchecker.eventsender'),
    PARTITION `eventreceiver` VALUES IN ('linkis.appconn.eventchecker.eventreceiver'),
    PARTITION `sendemail` VALUES IN ('linkis.appconn.sendemail'),
    PARTITION `display` VALUES IN ('linkis.appconn.visualis.display'),
	PARTITION `dashboard` VALUES IN ('linkis.appconn.visualis.dashboard'),
	PARTITION `widget` VALUES IN ('linkis.appconn.visualis.widget'),
	PARTITION `view` VALUES IN ('linkis.appconn.visualis.view'),
	PARTITION `tableau` VALUES IN ('linkis.appconn.newVisualis.tableau'),
    PARTITION `CheckRules` VALUES IN ('linkis.appconn.qualitis'),
    PARTITION `ShellRules` VALUES IN ('linkis.appconn.qualitis.bash'),
    PARTITION `CheckAlert` VALUES IN ('linkis.appconn.qualitis.checkalert'),
    PARTITION `mlss` VALUES IN ('linkis.appconn.mlss'),
    PARTITION `gpu` VALUES IN ('linkis.appconn.mlflow.gpu'),
    PARTITION `tableauDataRefre` VALUES IN ('linkis.appconn.newVisualis.tableauDataRefre'),
    PARTITION `TableRules` VALUES IN ('linkis.appconn.qualitis.TableRules'),
    PARTITION `mlssv2` VALUES IN ('linkis.appconn.mlssv2'),
    PARTITION `sqoop` VALUES IN ('linkis.appconn.exchangis.sqoop'),
    PARTITION `datax` VALUES IN ('linkis.appconn.exchangis.datax'),
    PARTITION `nebula` VALUES IN ('linkis.nebula.nebula'),
	PARTITION `metabase` VALUES IN ('linkis.appconn.metabase')
);

INSERT INTO dss_workflow_node_ui_validate (validate_type,validate_range,error_msg,error_msg_en,`trigger`) VALUES
    ('Function','validateCheckObject','请正确填写check.object的值,格式为dbname.tablename{ds=partitionname}，不要有空格','params config error, please make sure!','blur');

INSERT INTO dss_workflow_node_ui_to_validate (ui_id ,validate_id ) VALUES
    ((select id from dss_workflow_node_ui where `key`='check.object'),(SELECT id FROM dss_workflow_node_ui_validate where validate_type ='Function' AND validate_range ='validateCheckObject'));

UPDATE dss_workflow_node_ui_validate set error_msg='请严格按照dbname.tablename{ds=partitionname}格式填写，中间不允许有空格' ,validate_range='^\\s*[a-zA-Z]([^.]*\\.[^.]*){1,}\\s*$' where error_msg ='需要检查的数据源dbname.tablename{partition}' ;

INSERT INTO dss_workflow_node (name,appconn_name,node_type,jump_type,support_jump,submit_to_scheduler,enable_copy,should_creation_before_node,icon_path)
VALUES ('nebula','scriptis','linkis.nebula.nebula',2,1,1,1,0,'svgs/nebula-graph.svg');

INSERT INTO dss_workflow_node_to_group (node_id,group_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.nebula.nebula') ,(select id from dss_workflow_node_group where name_en = 'Data development'));

INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.nebula.nebula'),(select id from dss_workflow_node_ui where `key` = 'title' and node_menu_type = '1' ));
INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.nebula.nebula'),(select id from dss_workflow_node_ui where `key` = 'desc' and node_menu_type = '1' ));
INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.nebula.nebula'),(select id from dss_workflow_node_ui where `key` = 'businessTag'));
INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.nebula.nebula'),(select id from dss_workflow_node_ui where `key` = 'appTag'));
INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.nebula.nebula'),(select id from dss_workflow_node_ui where `key` = 'ReuseEngine'));
INSERT INTO dss_workflow_node_to_ui (workflow_node_id,ui_id)
VALUES ((select id from dss_workflow_node where node_type = 'linkis.nebula.nebula'),(select id from dss_workflow_node_ui where `key` = 'auto.disabled'));
-- 清空上个版本的台账数据
DELETE FROM  dss_workflow_node_content_to_ui;

DELETE FROM dss_workflow_node_content;

-- 资源运营看板切换到portal
UPDATE dss_sidebar_content  SET url='http://wds.bdp.webank.com/#/admin/operationManage/yarn' WHERE title ='资源使用数据看板';
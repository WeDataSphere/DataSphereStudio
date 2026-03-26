-- 错误信息链接知识库文档修复
INSERT
	INTO
	dss1_open_dev.dss_error_code_solution (error_desc, app_name, uri, match_content, match_type, solution, update_by, is_skipped)
VALUES ('a', 'dss', '1', '项目名称 (\\S+) 在(\\S+)已被删除', 1, '{"solutionUrl":"_book/知识库/DSS常见问题/DSS工作流相关问题/工作流发布失败，报错：项目不存在.html"}', NULL, NULL)

ALTER TABLE dss_apiservice_access_info ADD task_id varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '任务id';
ALTER TABLE dss_apiservice_access_info ADD task_status varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '任务执行状态';

ALTER TABLE dss_apiservice_approval ADD sensitive_level TINYINT NULL COMMENT '是否涉及一级敏感数据';

update `dss_workflow_node` set icon_path='svgs/datachecker-node.svg' where node_type='linkis.appconn.datachecker' and name = 'datachecker';
update `dss_workflow_node` set icon_path='svgs/python-node.svg' where node_type ='linkis.python.python' and name ='python';
update `dss_workflow_node` set icon_path='svgs/pyspark-node.svg' where node_type='linkis.spark.py' and name ='pyspark';
update `dss_workflow_node` set icon_path='svgs/sql-node.svg' where node_type='linkis.spark.sql' and name ='sql';
update `dss_workflow_node` set icon_path='svgs/Scala-node.svg' where node_type='linkis.spark.scala' and name ='scala';
update `dss_workflow_node` set icon_path='svgs/hql-node.svg' where node_type='linkis.hive.hql' and name ='hql';
update `dss_workflow_node` set icon_path='svgs/connector-node.svg' where node_type='linkis.control.empty' and name ='connector';
update `dss_workflow_node` set icon_path='svgs/sendemail-node.svg' where node_type='linkis.appconn.sendemail' and name ='sendemail';
update `dss_workflow_node` set icon_path='svgs/shell-node.svg' where node_type='linkis.shell.sh' and name ='shell';
update `dss_workflow_node` set icon_path='svgs/subflow-node.svg' where node_type='workflow.subflow' and name ='subFlow';
update `dss_workflow_node` set icon_path='svgs/eventsender-node.svg' where node_type='linkis.appconn.eventchecker.eventsender' and name ='eventsender';
update `dss_workflow_node` set icon_path='svgs/eventchecker-node.svg' where node_type='linkis.appconn.eventchecker.eventreceiver' and name ='eventreceiver';

update `dss_workflow_node_ui` set `position` = 'startup' where `key` = 'wds.linkis.engineconn.java.driver.memory';

-- todo 最好先把dss库备份一份，方便查询原始数据， dss_appconn表的id改为从1001开始，避免太小和之前application_id混淆

-- todo appconn_id测试环境不超过20，生产需要确认
SET @@autocommit=0;
START TRANSACTION;

INSERT INTO dss_appconn
(id, appconn_name, is_user_need_init, `level`, if_iframe, is_external, reference, class_name, appconn_class_path, resource)
VALUES(22, 'scriptis', 0, 1, 0, 0, 'sso', NULL, NULL, '');
INSERT INTO dss_appconn
(id, appconn_name, is_user_need_init, `level`, if_iframe, is_external, reference, class_name, appconn_class_path, resource)
VALUES(23, 'sso', 0, 1, 0, 0, NULL, 'com.webank.wedatasphere.dss.appconn.sso.SSOAppConn', '', '');
INSERT INTO dss_appconn
(id, appconn_name, is_user_need_init, `level`, if_iframe, is_external, reference, class_name, appconn_class_path, resource)
VALUES(24, 'apiservice', 0, 1, 0, 0, 'sso', NULL, NULL, '');
INSERT INTO dss_appconn
(id, appconn_name, is_user_need_init, `level`, if_iframe, is_external, reference, class_name, appconn_class_path, resource)
VALUES(26, 'datawrangler', 0, 1, 0, 1, 'sso', NULL, NULL, '');
INSERT INTO dss_appconn
(id, appconn_name, is_user_need_init, `level`, if_iframe, is_external, reference, class_name, appconn_class_path, resource)
VALUES(27, 'newvisualis', 0, 1, 0, 1, 'sso', NULL, NULL, '');
INSERT INTO dss_appconn
(id, appconn_name, is_user_need_init, `level`, if_iframe, is_external, reference, class_name, appconn_class_path, resource)
VALUES(28, 'visualishub', 0, 1, 0, 1, 'sso', NULL, NULL, '');

-- TODO dss_appconn_instance修改
alter table dss_appconn_instance drop column redirect_url;
alter table dss_appconn_instance change column homepage_url `homepage_uri` varchar(255) DEFAULT NULL COMMENT '主页uri';

INSERT INTO dss_appconn_instance
(id, appconn_id, label, url, enhance_json, homepage_uri)
VALUES(34, 22, 'DEV', '/home', '', '');
INSERT INTO dss_appconn_instance
(id, appconn_id, label, url, enhance_json, homepage_uri)
VALUES(36, 24, 'DEV', '/apiservices', '', '');
INSERT INTO dss_appconn_instance
(id, appconn_id, label, url, enhance_json, homepage_uri)
VALUES(37, 25, 'PROD', '/orchestratorFramework', '', '');
INSERT INTO dss_appconn_instance
(id, appconn_id, label, url, enhance_json, homepage_uri)
VALUES(38, 26, 'DEV', 'http://10.107.116.246:8315/', '', '');
INSERT INTO dss_appconn_instance
(id, appconn_id, label, url, enhance_json, homepage_uri)
VALUES(39, 27, 'DEV', 'http://sit.dss.bdp.weoa.com/', '', 'dss/newVisualis');
INSERT INTO dss_appconn_instance
(id, appconn_id, label, url, enhance_json, homepage_uri)
VALUES(40, 28, 'DEV', 'http://sit.dss.bdp.weoa.com/', '', 'dss/visualishub');


update dss_appconn set id=id+1000;
update dss_appconn_instance set appconn_id=appconn_id+1000;
-- appconn 类名有变动  todo 看生产需不需要更新class_path
update dss_appconn set class_name="com.webank.wedatasphere.dss.appconn.orchestrator.OrchestratorFrameworkAppConn" where appconn_name="orchestrator-framework";
update dss_appconn set class_name="com.webank.wedatasphere.dss.appconn.workflow.WorkflowAppConn" where appconn_name="workflow";
-- todo if_iframe和is_external字段修改，先都设置为0，再逐个update
update dss_appconn set if_iframe =0,is_external =0;
update dss_appconn set if_iframe=1,is_external=1 where appconn_name="schedulis";
update dss_appconn set if_iframe=1,is_external=1 where appconn_name="visualis";
update dss_appconn set if_iframe=1,is_external=0 where appconn_name="apiservice";
update dss_appconn set if_iframe=0,is_external=1 where appconn_name="datawrangler";
update dss_appconn set if_iframe=0,is_external=1 where appconn_name="newvisualis";
update dss_appconn set if_iframe=0,is_external=1 where appconn_name="visualishub";

alter table dss_orchestrator_info add column (`workspace_id` int(11) DEFAULT NULL COMMENT '空间id',
  `orchestrator_mode` varchar(100) DEFAULT NULL COMMENT '编排模式，取得的值是dss_dictionary中的dic_key(parent_key=p_arrangement_mode)',
  `orchestrator_way` varchar(256) DEFAULT NULL COMMENT '编排方式',
  `orchestrator_level` varchar(32) DEFAULT NULL,
  `update_user` varchar(100) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间');

update dss_orchestrator_info oi set workspace_id=(select workspace_id from dss_project_orchestrator po where po.orchestrator_id=oi.id);
update dss_orchestrator_info oi set orchestrator_mode=(select orchestrator_mode from dss_project_orchestrator po where po.orchestrator_id=oi.id);
update dss_orchestrator_info oi set orchestrator_way=(select orchestrator_way from dss_project_orchestrator po where po.orchestrator_id=oi.id);
update dss_orchestrator_info oi set update_user=(select update_user from dss_project_orchestrator po where po.orchestrator_id=oi.id);
update dss_orchestrator_info oi set update_time=(select update_time from dss_project_orchestrator po where po.orchestrator_id=oi.id);

-- dss_orchestrator_ref_orchestration_relation definition
CREATE TABLE `dss_orchestrator_ref_orchestration_relation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `orchestrator_id` bigint(20) NOT NULL,
  `ref_project_id` bigint(20) DEFAULT NULL,
  `ref_orchestration_id` bigint(20) DEFAULT NULL COMMENT '调度系统工作流的id(调用SchedulerAppConn的OrchestrationOperation服务返回的orchestrationId)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT;


ALTER TABLE dss_workflow_node CHANGE icon icon_path longtext CHARACTER SET utf8mb4 COLLATE utf8_general_ci NULL;
-- todo 设置每个节点的jump_type和appconn_name，jump_url要先改为1，否则改变不了类型为int
update dss_workflow_node set jump_url ="1";
ALTER TABLE dss_workflow_node CHANGE jump_url jump_type INT NULL;
update dss_workflow_node set icon_path =concat("icons/",name,".icon");
-- todo 确认生产各个节点support_jump字段是否是正确的
update dss_workflow_node set appconn_name="scriptis",jump_type=2 where name in ('python','pyspark','sql','shell','scala','hql','jdbc','connector','subFlow');
update dss_workflow_node set appconn_name="visualis" where name in ('display','dashboard','widget','view');
update dss_workflow_node set appconn_name="sendemail" where name in ('sendemail');
update dss_workflow_node set appconn_name="eventchecker" where name in ('eventsender','eventreceiver');
update dss_workflow_node set appconn_name="datachecker" where name in ('datachecker');
update dss_workflow_node set jump_type=0 where name in ("eventsender","eventreceiver","datachecker");


RENAME TABLE dss_dictionary to dss_workspace_dictionary;
-- todo 检查下dss_workspace_role是否有存量数据
DROP TABLE IF EXISTS `dss_workspace_role`;
RENAME TABLE dss_role to dss_workspace_role;
RENAME TABLE dss_admin_dept to dss_workspace_admin_dept;
RENAME TABLE dss_download_audit to dss_workspace_download_audit;
RENAME TABLE dss_flow_relation to dss_workflow_relation;
RENAME TABLE dss_flow_edit_lock to dss_workflow_edit_lock;
RENAME TABLE dss_onestop_menu to dss_workspace_menu;
RENAME TABLE dss_menu_role to dss_workspace_menu_role;

RENAME TABLE dss_onestop_user_favorites to dss_workspace_user_favorites_appconn;
-- todo application_id和appconn_id对应关系修改，通过dss_onestop_menu_application的application_id到dss_application表找到组件，待确认生产数据
ALTER TABLE dss_workspace_user_favorites_appconn CHANGE menu_application_id menu_appconn_id int(20) NULL;


-- todo application_id（dss_application表）和appconn_id对应关系修改
RENAME TABLE dss_component_role to dss_workspace_appconn_role;
ALTER TABLE dss_workspace_appconn_role CHANGE component_id appconn_id int(20) NULL;
-- dss_application的linkis组件相当于scriptis
select @linkis_application_id:=id from dss_application where name='linkis';
select @scriptis_appconn_id:=id from dss_appconn where appconn_name='scriptis';
update dss_workspace_appconn_role set appconn_id=@scriptis_appconn_id where appconn_id=@linkis_application_id;

select @workflow_application_id:=id from dss_application where name='workflow';
select @workflow_appconn_id:=id from dss_appconn where appconn_name='workflow';
update dss_workspace_appconn_role set appconn_id=@workflow_appconn_id where appconn_id=@workflow_application_id;

select @visualis_application_id:=id from dss_application where name='visualis';
select @visualis_appconn_id:=id from dss_appconn where appconn_name='visualis';
update dss_workspace_appconn_role set appconn_id=@visualis_appconn_id where appconn_id=@visualis_application_id;

select @schedulis_application_id:=id from dss_application where name='schedulis';
select @schedulis_appconn_id:=id from dss_appconn where appconn_name='schedulis';
update dss_workspace_appconn_role set appconn_id=@schedulis_appconn_id where appconn_id=@schedulis_application_id;

select @qualitis_application_id:=id from dss_application where name='qualitis';
select @qualitis_appconn_id:=id from dss_appconn where appconn_name='qualitis';
update dss_workspace_appconn_role set appconn_id=@qualitis_appconn_id where appconn_id=@qualitis_application_id;

-- todo 确定生产apiservice的name
select @apiservice_application_id:=id from dss_application where name='apiService';
select @apiservice_appconn_id:=id from dss_appconn where appconn_name='apiservice';
update dss_workspace_appconn_role set appconn_id=@apiservice_appconn_id where appconn_id=@apiservice_application_id;

select @datawrangler_application_id:=id from dss_application where name='datawrangler';
select @datawrangler_appconn_id:=id from dss_appconn where appconn_name='datawrangler';
update dss_workspace_appconn_role set appconn_id=@datawrangler_appconn_id where appconn_id=@datawrangler_application_id;

select @newvisualis_application_id:=id from dss_application where name='newVisualis';
select @newvisualis_appconn_id:=id from dss_appconn where appconn_name='newvisualis';
update dss_workspace_appconn_role set appconn_id=@newvisualis_appconn_id where appconn_id=@newvisualis_application_id;

-- todo 确定visualis_hub在dss_application表的name
select @visualishub_application_id:=id from dss_application where name='visualis_hub';
select @visualishub_appconn_id:=id from dss_appconn where appconn_name='visualishub';
update dss_workspace_appconn_role set appconn_id=@visualishub_appconn_id where appconn_id=@visualishub_application_id;


-- 和workspace_menu表可以dml语句插入，todo appconn_id手动修改
RENAME TABLE dss_onestop_menu_application to dss_workspace_menu_appconn;
ALTER TABLE dss_workspace_menu_appconn CHANGE application_id appconn_id int(20) NULL;
ALTER TABLE dss_workspace_menu_appconn CHANGE onestop_menu_id menu_id int(20) NULL;
-- todo 确定title_en是否正确
select @apiservice_appconn_id:=id from dss_appconn where appconn_name='apiservice';
update dss_workspace_menu_appconn set appconn_id=@apiservice_appconn_id where title_en="Data service development";

select @scriptis_appconn_id:=id from dss_appconn where appconn_name='scriptis';
update dss_workspace_menu_appconn set appconn_id=@scriptis_appconn_id where title_en="Scriptis";

select @workflow_appconn_id:=id from dss_appconn where appconn_name='workflow';
update dss_workspace_menu_appconn set appconn_id=@workflow_appconn_id where title_en="workflow";

select @visualis_appconn_id:=id from dss_appconn where appconn_name='visualis';
update dss_workspace_menu_appconn set appconn_id=@visualis_appconn_id where title_en="Visualis";

select @schedulis_appconn_id:=id from dss_appconn where appconn_name='schedulis';
update dss_workspace_menu_appconn set appconn_id=@schedulis_appconn_id where title_en="Schedulis";

select @datawrangler_appconn_id:=id from dss_appconn where appconn_name='datawrangler';
update dss_workspace_menu_appconn set appconn_id=@datawrangler_appconn_id where title_en="datawrangler";

select @newvisualis_appconn_id:=id from dss_appconn where appconn_name='newvisualis';
update dss_workspace_menu_appconn set appconn_id=@newvisualis_appconn_id where title_en="newVisualis";

select @visualishub_appconn_id:=id from dss_appconn where appconn_name='visualishub';
update dss_workspace_menu_appconn set appconn_id=@visualishub_appconn_id where title_en="Visualis-Hub";





COMMIT;
SET @@autocommit=1;
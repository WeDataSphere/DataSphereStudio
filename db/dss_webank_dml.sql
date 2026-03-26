SET @@autocommit=0;
START TRANSACTION;

INSERT INTO dss_workspace_dictionary
(workspace_id, parent_key, dic_name, dic_name_en, dic_key, dic_value, dic_value_en, title, title_en, url, url_type, icon, order_num, remark, checked, create_user, create_time, update_user, update_time)
VALUES(0, 'p_develop_process', '生产中心', 'Production Center', 'pdp_product_center', 'prod', NULL, NULL, NULL, NULL, 0, 'kaifa-icon', 1, '工程开发流程-生产中心', 1, 'SYSTEM', '2020-12-28 17:32:35.0', NULL, '2021-02-22 17:49:02.0');

INSERT INTO dss_workspace_dictionary
(workspace_id, parent_key, dic_name, dic_name_en, dic_key, dic_value, dic_value_en, title, title_en, url, url_type, icon, order_num, remark, checked, create_user, create_time, update_user, update_time)
VALUES(0, 'workflow_check_switch', 'workflowCheckSwitch', 'workflowCheckSwitch', 'workflow_check_switch', '0', NULL, NULL, NULL, NULL, 0, NULL, 1, '发布校验工作流开关，1：开,0:关', 1, 'SYSTEM', '2022-01-12 12:51:49.0', NULL, '2022-06-07 09:22:52.0');

INSERT INTO dss_appconn
(appconn_name, is_user_need_init, `level`, if_iframe, is_external, reference, class_name, appconn_class_path, resource)
VALUES('orchestrator-framework', 0, 1, 0, 0, NULL, 'com.webank.wedatasphere.dss.appconn.orchestrator.OrchestratorFrameworkAppConn', '/appcom/Install/dss/dss-appconns/orchestrator-framework', '');

select @orchestrator_appconnId:=id from `dss_appconn` where `appconn_name` = 'orchestrator-framework';
INSERT INTO dss_appconn_instance
(appconn_id, label, url, enhance_json, homepage_uri)
VALUES(@orchestrator_appconnId, 'DEV', '/orchestratorFramework', '', '');
INSERT INTO dss_appconn_instance
(appconn_id, label, url, enhance_json, homepage_uri)
VALUES(@orchestrator_appconnId, 'PROD', '/orchestratorFramework', '', '');

-- workflow prod env
select @workflow_appconnId:=id from `dss_appconn` where `appconn_name` = 'workflow';
INSERT INTO dss_appconn_instance
(appconn_id, label, url, enhance_json, homepage_uri)
VALUES(@workflow_appconnId, 'PROD', '/workspaceHome', '', '');



COMMIT;
SET @@autocommit=1;
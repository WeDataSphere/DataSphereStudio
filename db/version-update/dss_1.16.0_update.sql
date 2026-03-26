ALTER TABLE dss_apiservice_approval ADD COLUMN product_info TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '产品信息';

ALTER TABLE dss_apiservice_approval ADD COLUMN dev_principals varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '库表负责人';

ALTER TABLE dss_apiservice_api_version ADD COLUMN datasource varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '数据源名称';

UPDATE dss_workflow_node_ui SET `value` = '["linkis.appconn.visualis.display","linkis.appconn.visualis.dashboard","linkis.appconn.newVisualis.tableau","linkis.appconn.mlssv2"]' WHERE `key` = 'content'

ALTER TABLE dss_apiservice_approval MODIFY COLUMN approval_name varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '审批单名称';
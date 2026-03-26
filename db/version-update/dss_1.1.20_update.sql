ALTER TABLE event_queue ADD COLUMN run_date  VARCHAR(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '信号的run_date'  AFTER send_ip;
ALTER TABLE dss_ec_release_strategy ADD COLUMN cross_cluster   tinyint(1) DEFAULT 0 COMMENT '是否跨集群,0为否，1为是'  AFTER queue;
ALTER TABLE dss_queue_in_workspace ADD COLUMN cross_cluster   tinyint(1) DEFAULT 0 COMMENT '是否跨集群,0为否，1为是'  AFTER queue;
-- 新增历史查询字段
ALTER TABLE dss_apiservice_access_info ADD COLUMN query_params TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '新的文本字段';
-- 删除唯一性约束
ALTER TABLE dss_ec_release_strategy DROP INDEX queue;
ALTER TABLE dss_queue_in_workspace DROP INDEX queue;
ALTER TABLE dss_project_orchestrator_white ADD COLUMN reason text  CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL  COMMENT '原因';

ALTER TABLE dss_project_orchestrator_white ADD COLUMN type varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '类型';

ALTER TABLE dss_ec_kill_history ADD INDEX idx_strategy_id_time (strategy_id,create_time);
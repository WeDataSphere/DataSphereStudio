SET @@autocommit=0;
START TRANSACTION;

CREATE TABLE `dss_orchestrator_copy_info` (
        `id` VARCHAR(128) NOT NULL COMMENT '主键',
        `username` VARCHAR(128) DEFAULT NULL COMMENT '用户名',
        `type` VARCHAR(128) DEFAULT NULL COMMENT '编排类别',
        `source_orchestrator_id` INT(20) DEFAULT NULL COMMENT '源编排ID',
        `source_orchestrator_name` VARCHAR(255) DEFAULT NULL COMMENT '源编排名',
        `target_orchestrator_name` VARCHAR(255) DEFAULT NULL COMMENT '目标编排名',
        `source_project_name` VARCHAR(255) DEFAULT NULL COMMENT '源工程名',
        `target_project_name` VARCHAR(255) DEFAULT NULL COMMENT '目标工程名',
        `workspace_id` INT(20) DEFAULT NULL COMMENT '工作空间ID',
        `workflow_node_suffix` VARCHAR(255) DEFAULT NULL COMMENT '目标工作流节点后缀',
        `microserver_name` VARCHAR(128) COMMENT '微服务名',
        `exception_info` VARCHAR(128) COMMENT '异常信息',
        `status` int(1) DEFAULT 0 COMMENT '复制任务最终状态',
        `instance_name`            varchar(128) DEFAULT NULL COMMENT '执行任务的实例',
        `is_copying` int(1) DEFAULT 0 COMMENT '编排是否在被复制',
        `success_node` TEXT COMMENT '复制成功节点',
        `start_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '复制开始时间',
        `end_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '复制结束时间',
        PRIMARY KEY (`id`),
        INDEX index_soi(source_orchestrator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPACT COMMENT='编排复制信息表';

COMMIT;
SET @@autocommit=1;
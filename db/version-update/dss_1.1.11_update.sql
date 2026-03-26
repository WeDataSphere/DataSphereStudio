SET @@autocommit = 0;
START TRANSACTION;

-- 首页公告表
CREATE TABLE `dss_notice`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `content`   text DEFAULT NULL COMMENT '公告内容',
    `start_time` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '生效时间',
    `end_time`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '失效时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE=utf8mb4_bin COMMENT ='首页公告内容';
-- 调整执行器内存大小限制为1-28
update
    dss_workflow_node_ui_to_validate nutv , dss_workflow_node_ui nu , dss_workflow_node_ui_validate nuv
set
    nuv.validate_type='Regex',
    nuv.validate_range='^([1-9]|1[0-9]|2[0-8])(g|G){0,1}$',
    nuv.error_msg='设置范围为[1,28],设置超出限制',
    nuv.error_msg_en='must be between 1 and 28',
    nuv.trigger='blur'
where   nu.id = nutv.ui_id
  AND nutv.validate_id = nuv.id AND nu.key='spark.executor.memory' ;
-- fix 驱动器内存大小设置不能带g
update
    dss_workflow_node_ui_to_validate nutv , dss_workflow_node_ui nu , dss_workflow_node_ui_validate nuv
set
    nuv.validate_type='Regex',
    nuv.validate_range='^([1-9]|1[0-5])(g|G){0,1}$',
    nuv.error_msg='设置范围为[1,15],设置超出限制',
    nuv.error_msg_en='must be between 1 and 15',
    nuv.trigger='blur'
where   nu.id = nutv.ui_id
  AND nutv.validate_id = nuv.id AND nu.key='spark.driver.memory' ;

DELETE FROM dss_release_note_content;
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
    VALUES ('feature1', '【新增】结果集下载支持用户选择分隔符','_book/知识库/用户手册/版本功能介绍/v1.1.10/结果集下载支持选择分隔符.html', 1, 1, now());

COMMIT;
SET @@autocommit = 1;
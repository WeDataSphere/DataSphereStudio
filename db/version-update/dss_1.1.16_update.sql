-- 修改name字段64字符为128字符
ALTER TABLE dss_ec_config_template MODIFY name varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;

ALTER TABLE dss_ec_config_template_apply_rule MODIFY template_name varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;

ALTER TABLE dss_ec_config_template_apply_rule_execute_record MODIFY template_name varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL;

DELETE FROM dss_release_note_content;
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
    VALUES ('feature1', '【优化】调大库表信息页中表名字段的列宽','_book/版本动态与公告/v1.1.16.html#1', 1, 1, now());
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
    VALUES ('feature2', '【优化】Hive部分参数范围调整','_book/版本动态与公告/v1.1.16.html#2', 1, 1, now());
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
    VALUES ('feature3', '【优化】日志优化','_book/版本动态与公告/v1.1.16.html#3', 1, 1, now());
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
    VALUES ('feature4', '【新增】新增错误码','_book/版本动态与公告/v1.1.16.html#4', 1, 1, now());
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
    VALUES ('feature5', '【优化】不同建表方式功能保持统一','_book/版本动态与公告/v1.1.16.html#5', 1, 1, now());
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
    VALUES ('feature6', '【优化】分区信息展示创建时间优化','_book/版本动态与公告/v1.1.16.html#6', 1, 1, now());
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
    VALUES ('feature7', '【新增】支持自动生成rename语句','_book/版本动态与公告/v1.1.16.html#7', 1, 1, now());
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
    VALUES ('feature8', '【优化】优化Scriptis中配置引擎参数功能','_book/版本动态与公告/v1.1.16.html#8', 1, 1, now());
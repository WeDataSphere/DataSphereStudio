SET @@autocommit = 0;
START TRANSACTION;

-- appconn表新增微应用标记字段
ALTER TABLE `dss_appconn` ADD `is_micro_app` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否微应用嵌入';
-- enhance_json字段大小从1024增到到2048
alter  table dss_appconn_instance change `enhance_json` `enhance_json` varchar(2048) DEFAULT NULL COMMENT 'json格式的配置';

-- 邮件节点修改收件人，抄送人，秘密发送人的描述。(注意该修改只针对行内)
UPDATE `dss_workflow_node_ui` SET description = '请填写收件人企业微信英文名，按";"分割' WHERE `key` = 'to';
UPDATE `dss_workflow_node_ui` SET description = '请填写抄送人企业微信英文名，按";"分割' WHERE `key` = 'cc';
UPDATE `dss_workflow_node_ui` SET description = '请填写秘密抄送人企业微信英文名，按";"分割' WHERE `key` = 'bcc';

-- 去除节点描述的中文限制
delete nutv  from dss_workflow_node_ui_to_validate nutv , dss_workflow_node_ui nu , dss_workflow_node_ui_validate nuv
             where   nu.id = nutv.ui_id
             AND nutv.validate_id = nuv.id and nu.lable_name ='节点描述' and nuv.error_msg='此值不能输入中文';

DELETE FROM dss_release_note_content;
INSERT INTO dss_release_note_content (name, title, url, url_type, release_type, create_time)
VALUES ('feature1', '【新增】结果集下载支持用户选择分隔符','_book/知识库/用户手册/版本功能介绍/v1.1.10/结果集下载支持选择分隔符.html', 1, 1, now()),
('feature2', '【优化】错误信息提醒框常驻展示 ','_book/知识库/用户手册/版本功能介绍/v1.1.10/错误信息提示框可常驻展示.html', 1, 1, now()),
('feature3', '【优化】工作流节点名称展示优化','_book/知识库/用户手册/版本功能介绍/v1.1.10/工作流节点名称展示优化.html', 1, 1, now()),
('feature4', '【新增】Scriptis表基本属性增加是否压缩信息','_book/知识库/用户手册/版本功能介绍/v1.1.10/Scriptis表基本属性增加内容展示.html', 1, 1, now()),
('feature5', '【新增】实时资源运营数据展示','_book/知识库/用户手册/版本功能介绍/v1.1.10/实时运营数据看板.html', 1, 1, now()),
('feature6', '【新增】对接dm的自动获取开发负责人接口，提单时自动填充开发负责人字段信息','_book/知识库/用户手册/版本功能介绍/v1.1.10/数据服务对接DM时自动填充开发负责人.html', 1, 0, now())
       ;

COMMIT;
SET @@autocommit = 1;
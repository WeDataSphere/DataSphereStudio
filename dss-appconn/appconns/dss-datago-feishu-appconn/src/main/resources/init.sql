-- TODO 这里只适用于第一次安装时。如果是更新的话dss_appconn表不能先删除再插入，因为其他表如dss_workspace_appconn_role关联了appconn_id(不能变)，需要使用update、alter语句更新
select @datago_feishu_appconn_id:=id from `dss_appconn` where `appconn_name` = 'datagofeishu';
delete from `dss_appconn_instance` where `appconn_id` = @datago_feishu_appconn_id;

delete from dss_appconn where appconn_name = "datagofeishu";
INSERT INTO `dss_appconn` (`appconn_name`, `is_user_need_init`, `level`, `if_iframe`, `is_external`, `reference`, `class_name`, `appconn_class_path`, `resource`)
VALUES ('datagofeishu', 0, 1, 1, 1, NULL, 'com.webank.wedatasphere.dss.appconn.datagofeishu.DataGoFeishuAppConn', 'DSS_INSTALL_HOME_VAL/dss-appconns/datagofeishu', '');

select @datago_feishu_appconn_id:=id from `dss_appconn` where `appconn_name` = 'datagofeishu';

INSERT INTO `dss_appconn_instance` (`appconn_id`, `label`, `url`, `enhance_json`, `homepage_uri`)
VALUES (@datago_feishu_appconn_id, 'DEV', 'datagofeishu', '{"wds.dss.appconn.datago.feishu.api.base.url":"http://DATAGO_HOST:3003","wds.dss.appconn.datago.feishu.api.form.path":"/api/export/form","wds.dss.appconn.datago.feishu.api.task.path":"/api/export/task","wds.dss.appconn.datago.feishu.api.execute.path":"/api/export/execute","wds.dss.appconn.datago.feishu.api.token.header":"Authorization","wds.dss.appconn.datago.feishu.api.token":"","wds.dss.appconn.datago.feishu.optype.supported":"table","wds.dss.appconn.datago.feishu.detect.poll.interval.ms":"30000","wds.dss.appconn.datago.feishu.execute.retry.max":"3","wds.dss.appconn.datago.feishu.execute.retry.interval.ms":"30000","wds.dss.appconn.datago.feishu.max.wait.time.ms":"7200000","wds.dss.appconn.datago.feishu.http.connect.timeout.ms":"10000","wds.dss.appconn.datago.feishu.http.read.timeout.ms":"60000"}', '');

delete from dss_workflow_node where appconn_name = "datagofeishu";
insert into `dss_workflow_node` (`name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`, `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`)
values('datagofeishu','datagofeishu','linkis.appconn.datagofeishu','0','0','1','1','0','svgs/datagofeishu-node.svg');

select @datago_feishu_node_id:=id from `dss_workflow_node` where `node_type` = 'linkis.appconn.datagofeishu';

delete from `dss_workflow_node_to_group` where `node_id`=@datago_feishu_node_id;
delete from `dss_workflow_node_to_ui` where `workflow_node_id`=@datago_feishu_node_id;

-- 查找节点所属组的id
select @datago_feishu_node_group_id:=id from `dss_workflow_node_group` where `name` = '信号节点';

INSERT INTO `dss_workflow_node_to_group`(`node_id`,`group_id`) values (@datago_feishu_node_id, @datago_feishu_node_group_id);

-- 删除并新增节点专属属性UI（按 key 清除，保证重复执行幂等）
delete from `dss_workflow_node_ui` where `key` in ('dmId','notifyUsers','dataTargets','detectPollInterval','executeRetryMax','maxWaitTime') and node_menu_type = 1;

INSERT INTO `dss_workflow_node_ui`
(`key`, description, description_en, lable_name, lable_name_en, ui_type, required, value, default_value, is_hidden, `condition`, is_advanced, `order`, node_menu_type, is_base_info, `position`)
VALUES ('dmId', '请输入DataGo侧DM审批单号', 'DataGo DM approval order ID', 'DM单号', 'DM Order ID', 'Input', 1, NULL, NULL, 0, NULL, 0, 1, 1, 0, 'runtime');

INSERT INTO `dss_workflow_node_ui`
(`key`, description, description_en, lable_name, lable_name_en, ui_type, required, value, default_value, is_hidden, `condition`, is_advanced, `order`, node_menu_type, is_base_info, `position`)
VALUES ('notifyUsers', '飞书通知人，多个使用英文分号分隔，须为DM单检测用户子集', 'Feishu notify users, semicolon separated', '飞书通知人', 'Notify Users', 'Input', 1, NULL, NULL, 0, NULL, 0, 2, 1, 0, 'runtime');

INSERT INTO `dss_workflow_node_ui`
(`key`, description, description_en, lable_name, lable_name_en, ui_type, required, value, default_value, is_hidden, `condition`, is_advanced, `order`, node_menu_type, is_base_info, `position`)
VALUES ('dataTargets', '每行一个外发目标，行间用分号(;)或换行分隔；行内格式：dt.序号=db=库名|table=表名|fields=字段1,字段2|partition={分区值}。无分区写 partition= 或省略该段。示例：dt.01=db=bdap_desensitized|table=customer_export_view|fields=cust_id,cust_name,amount|partition={dt=20260716}', 'One target per line (; or newline); inline: dt.NN=db=..|table=..|fields=..|partition={..}. Use partition= or omit for non-partitioned tables.', '外发目标', 'Data Targets', 'Text', 1, NULL, NULL, 0, NULL, 0, 3, 1, 0, 'runtime');

INSERT INTO `dss_workflow_node_ui`
(`key`, description, description_en, lable_name, lable_name_en, ui_type, required, value, default_value, is_hidden, `condition`, is_advanced, `order`, node_menu_type, is_base_info, `position`)
VALUES ('detectPollInterval', '检测轮询间隔，单位毫秒（建议10~30秒）', 'Detect polling interval in milliseconds', '检测轮询间隔', 'Detect Poll Interval', 'Input', 0, NULL, '30000', 0, NULL, 1, 7, 1, 0, 'runtime');

INSERT INTO `dss_workflow_node_ui`
(`key`, description, description_en, lable_name, lable_name_en, ui_type, required, value, default_value, is_hidden, `condition`, is_advanced, `order`, node_menu_type, is_base_info, `position`)
VALUES ('executeRetryMax', '外发失败最大重试次数', 'Max retry count for export', '外发重试次数', 'Export Retry Max', 'Input', 0, NULL, '3', 0, NULL, 1, 8, 1, 0, 'runtime');

INSERT INTO `dss_workflow_node_ui`
(`key`, description, description_en, lable_name, lable_name_en, ui_type, required, value, default_value, is_hidden, `condition`, is_advanced, `order`, node_menu_type, is_base_info, `position`)
VALUES ('maxWaitTime', '节点最大等待时间，单位毫秒', 'Maximum wait time in milliseconds', '最大等待时间', 'Maximum Wait Time', 'Input', 0, NULL, '7200000', 0, NULL, 1, 9, 1, 0, 'runtime');

-- 外发目标 dataTargets 的前端校验规则（参考 job.desc 的 Function 校验范式）
DELETE FROM dss_workflow_node_ui_validate WHERE validate_range = 'validateDataTargets';
INSERT INTO dss_workflow_node_ui_validate
(validate_type, validate_range, error_msg, error_msg_en, `trigger`)
VALUES ('Function', 'validateDataTargets', '请正确填写外发目标配置', 'Invalid data targets config', 'blur');
SELECT @validate_data_targets := MAX(id)
FROM dss_workflow_node_ui_validate WHERE validate_range = 'validateDataTargets';

-- 考虑表中有的是重复记录，最好加上limit 1
select @ui_node_title:=id from `dss_workflow_node_ui` where `key` = 'title' and node_menu_type = 1 limit 1;
select @ui_node_desc:=id from `dss_workflow_node_ui` where `key` = 'desc' and node_menu_type = 1 limit 1;
select @ui_node_business_tag:=id from `dss_workflow_node_ui` where `key` = 'businessTag' and node_menu_type = 1 limit 1;
select @ui_node_app_tag:=id from `dss_workflow_node_ui` where `key` = 'appTag' and node_menu_type = 1 limit 1;
select @ui_node_reuse_engine:=id from `dss_workflow_node_ui` where `key` = 'ReuseEngine' and node_menu_type = 1 limit 1;
select @ui_dm_id:=id from `dss_workflow_node_ui` where `key` = 'dmId' and node_menu_type = 1 limit 1;
select @ui_notify_users:=id from `dss_workflow_node_ui` where `key` = 'notifyUsers' and node_menu_type = 1 limit 1;
select @ui_data_targets:=id from `dss_workflow_node_ui` where `key` = 'dataTargets' and node_menu_type = 1 limit 1;
select @ui_detect_poll_interval:=id from `dss_workflow_node_ui` where `key` = 'detectPollInterval' and node_menu_type = 1 limit 1;
select @ui_execute_retry_max:=id from `dss_workflow_node_ui` where `key` = 'executeRetryMax' and node_menu_type = 1 limit 1;
select @ui_max_wait_time:=id from `dss_workflow_node_ui` where `key` = 'maxWaitTime' and node_menu_type = 1 limit 1;

DELETE FROM dss_workflow_node_ui_to_validate WHERE ui_id = @ui_data_targets;
INSERT INTO dss_workflow_node_ui_to_validate (ui_id, validate_id)
VALUES (@ui_data_targets, @validate_data_targets);

INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@datago_feishu_node_id, @ui_node_title);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@datago_feishu_node_id, @ui_node_desc);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@datago_feishu_node_id, @ui_node_business_tag);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@datago_feishu_node_id, @ui_node_app_tag);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@datago_feishu_node_id, @ui_node_reuse_engine);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@datago_feishu_node_id, @ui_dm_id);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@datago_feishu_node_id, @ui_notify_users);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@datago_feishu_node_id, @ui_data_targets);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@datago_feishu_node_id, @ui_detect_poll_interval);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@datago_feishu_node_id, @ui_execute_retry_max);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@datago_feishu_node_id, @ui_max_wait_time);

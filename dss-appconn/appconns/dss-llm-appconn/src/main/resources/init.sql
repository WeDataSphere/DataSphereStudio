-- LLM AppConn 数据库初始化脚本
-- 用于在DSS系统中注册LLM节点

-- ======================================================
-- 第一部分：注册AppConn
-- ======================================================

-- 清理旧数据（如果存在）
select @llm_appconnId:=id from `dss_appconn` where `appconn_name` = 'llm';
delete from `dss_appconn_instance` where `appconn_id` = @llm_appconnId;
delete from `dss_appconn` where `appconn_name` = 'llm';

-- 注册LLM AppConn
INSERT INTO `dss_appconn`
(`appconn_name`, `is_user_need_init`, `level`, `if_iframe`, `is_external`,
 `reference`, `class_name`, `appconn_class_path`, `resource`)
VALUES ('llm', 0, 1, 1, 1, NULL,
        'com.webank.wedatasphere.dss.appconn.llm.LLMAppConn',
        'DSS_INSTALL_HOME_VAL/dss-appconns/llm', '');

-- 获取AppConn ID
select @llm_appconnId:=id from `dss_appconn` where `appconn_name` = 'llm';

-- 注册AppConn实例配置
-- 注意：LLM_API_URL 和 LLM_API_KEY 需要在部署时替换为实际值
INSERT INTO `dss_appconn_instance` (`appconn_id`, `label`, `url`, `enhance_json`, `homepage_uri`)
VALUES (@llm_appconnId, 'DEV', 'llm',
        '{"llm.api.url": "LLM_API_URL",
          "llm.api.key": "LLM_API_KEY",
          "llm.max.retries": "3",
          "llm.retry.delay.ms": "1000"}',
        '');

-- ======================================================
-- 第二部分：创建AI节点分组
-- ======================================================

-- 检查是否已存在AI节点分组
select @ai_node_groupId:=id from `dss_workflow_node_group` where `name` = 'AI节点';

-- 如果不存在，则创建AI节点分组
INSERT INTO `dss_workflow_node_group` (`name`, `name_en`, `description`, `order`)
SELECT 'AI节点', 'AI Nodes', 'AI相关节点，包括大模型调用等', 10
WHERE NOT EXISTS (SELECT 1 FROM `dss_workflow_node_group` WHERE `name` = 'AI节点');

-- 重新获取分组ID
select @ai_node_groupId:=id from `dss_workflow_node_group` where `name` = 'AI节点';

-- ======================================================
-- 第三部分：注册工作流节点
-- ======================================================

-- 清理旧节点数据
delete from `dss_workflow_node` where `name` = 'llm' and `appconn_name` = 'llm';

-- 注册LLM工作流节点
INSERT INTO `dss_workflow_node`
(`name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`,
 `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`)
VALUES ('llm', 'llm', 'linkis.appconn.llm.llm', '0', '0', '1', '1', '0', 'icons/llm.icon');

-- 获取节点ID
select @llm_nodeId:=id from `dss_workflow_node` where `node_type` = 'linkis.appconn.llm.llm';

-- 关联节点到AI节点分组
INSERT INTO `dss_workflow_node_to_group`(`node_id`,`group_id`)
VALUES (@llm_nodeId, @ai_node_groupId);

-- ======================================================
-- 第四部分：定义UI配置项
-- ======================================================

-- 清理旧UI配置
delete from `dss_workflow_node_ui` where `key` in (
    'llm.system.prompt', 'llm.user.prompt', 'llm.model', 'llm.temperature',
    'llm.max.tokens', 'llm.mcp.tools', 'llm.save.key'
);

-- 1. 系统提示词配置
INSERT INTO `dss_workflow_node_ui`
(`key`, `description`, `description_en`, `lable_name`, `lable_name_en`,
 `ui_type`, `required`, `value`, `default_value`, `is_hidden`, `condition`,
 `is_advanced`, `order`, `node_menu_type`, `is_base_info`, `position`)
VALUES
('llm.system.prompt', '系统提示词，定义AI助手的角色和行为规范', 'System prompt defining AI assistant role and behavior',
 '系统提示词', 'System Prompt',
 'TextArea', 0, NULL, '你是一个专业的数据分析助手', 0, NULL, 0, 1, 0, 0, 'node');

-- 2. 用户提示词配置
INSERT INTO `dss_workflow_node_ui`
(`key`, `description`, `description_en`, `lable_name`, `lable_name_en`,
 `ui_type`, `required`, `value`, `default_value`, `is_hidden`, `condition`,
 `is_advanced`, `order`, `node_menu_type`, `is_base_info`, `position`)
VALUES
('llm.user.prompt', '用户提示词，描述具体任务需求。上游节点数据会自动追加到此提示词后', 'User prompt describing the task. Upstream data will be appended automatically',
 '用户提示词', 'User Prompt',
 'TextArea', 1, NULL, '', 0, NULL, 0, 2, 0, 0, 'node');

-- 3. 模型选择
INSERT INTO `dss_workflow_node_ui`
(`key`, `description`, `description_en`, `lable_name`, `lable_name_en`,
 `ui_type`, `required`, `value`, `default_value`, `is_hidden`, `condition`,
 `is_advanced`, `order`, `node_menu_type`, `is_base_info`, `position`)
VALUES
('llm.model', 'LLM模型名称', 'LLM model name',
 '模型名称', 'Model',
 'Select', 1, 'gpt-4,gpt-4-turbo,gpt-3.5-turbo,claude-2,claude-3-opus,claude-3-sonnet', 'gpt-3.5-turbo', 0, NULL, 0, 3, 0, 0, 'node');

-- 4. 温度参数
INSERT INTO `dss_workflow_node_ui`
(`key`, `description`, `description_en`, `lable_name`, `lable_name_en`,
 `ui_type`, `required`, `value`, `default_value`, `is_hidden`, `condition`,
 `is_advanced`, `order`, `node_menu_type`, `is_base_info`, `position`)
VALUES
('llm.temperature', '温度参数，控制输出随机性，范围0-2。值越小输出越确定', 'Temperature controls randomness, range 0-2. Lower values are more deterministic',
 '温度参数', 'Temperature',
 'Input', 0, NULL, '0.7', 0, NULL, 1, 4, 0, 0, 'node');

-- 5. 最大Token数
INSERT INTO `dss_workflow_node_ui`
(`key`, `description`, `description_en`, `lable_name`, `lable_name_en`,
 `ui_type`, `required`, `value`, `default_value`, `is_hidden`, `condition`,
 `is_advanced`, `order`, `node_menu_type`, `is_base_info`, `position`)
VALUES
('llm.max.tokens', '最大生成Token数量，控制输出长度', 'Maximum number of tokens to generate',
 '最大Token数', 'Max Tokens',
 'Input', 0, NULL, '2000', 0, NULL, 1, 5, 0, 0, 'node');

-- 6. MCP工具配置
INSERT INTO `dss_workflow_node_ui`
(`key`, `description`, `description_en`, `lable_name`, `lable_name_en`,
 `ui_type`, `required`, `value`, `default_value`, `is_hidden`, `condition`,
 `is_advanced`, `order`, `node_menu_type`, `is_base_info`, `position`)
VALUES
('llm.mcp.tools', 'MCP工具配置，JSON格式。定义LLM可调用的工具函数', 'MCP tools configuration in JSON format',
 'MCP工具配置', 'MCP Tools',
 'TextArea', 0, NULL, '', 0, NULL, 1, 6, 0, 0, 'node');

-- 7. 结果保存键
INSERT INTO `dss_workflow_node_ui`
(`key`, `description`, `description_en`, `lable_name`, `lable_name_en`,
 `ui_type`, `required`, `value`, `default_value`, `is_hidden`, `condition`,
 `is_advanced`, `order`, `node_menu_type`, `is_base_info`, `position`)
VALUES
('llm.save.key', 'LLM响应结果保存到Context的键名，下游节点可通过${键名}引用', 'Key name to save LLM response in Context for downstream nodes',
 '结果保存键', 'Save Key',
 'Input', 0, NULL, 'llm.response', 0, NULL, 1, 7, 0, 0, 'node');

-- ======================================================
-- 第五部分：关联节点到UI配置
-- ======================================================

-- 获取UI配置ID
select @ui_system_prompt:=id from `dss_workflow_node_ui` where `key` = 'llm.system.prompt';
select @ui_user_prompt:=id from `dss_workflow_node_ui` where `key` = 'llm.user.prompt';
select @ui_model:=id from `dss_workflow_node_ui` where `key` = 'llm.model';
select @ui_temperature:=id from `dss_workflow_node_ui` where `key` = 'llm.temperature';
select @ui_max_tokens:=id from `dss_workflow_node_ui` where `key` = 'llm.max.tokens';
select @ui_mcp_tools:=id from `dss_workflow_node_ui` where `key` = 'llm.mcp.tools';
select @ui_save_key:=id from `dss_workflow_node_ui` where `key` = 'llm.save.key';

-- 清理旧关联
delete from `dss_workflow_node_to_ui` where `workflow_node_id` = @llm_nodeId;

-- 关联节点到UI配置
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@llm_nodeId, @ui_system_prompt);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@llm_nodeId, @ui_user_prompt);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@llm_nodeId, @ui_model);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@llm_nodeId, @ui_temperature);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@llm_nodeId, @ui_max_tokens);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@llm_nodeId, @ui_mcp_tools);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@llm_nodeId, @ui_save_key);

-- 添加通用UI配置（节点名和描述）
select @common_ui_node_name:=id from `dss_workflow_node_ui` where `lable_name` = '节点名' limit 1;
select @common_ui_node_desc:=id from `dss_workflow_node_ui` where `lable_name` = '节点描述' limit 1;

-- 如果存在通用配置，则关联
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`)
SELECT @llm_nodeId, @common_ui_node_name
WHERE @common_ui_node_name IS NOT NULL;

INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`)
SELECT @llm_nodeId, @common_ui_node_desc
WHERE @common_ui_node_desc IS NOT NULL;

-- ======================================================
-- 完成提示
-- ======================================================
SELECT 'LLM AppConn 初始化完成！' as message;
SELECT '请确保在dss_appconn_instance表中更新LLM_API_URL和LLM_API_KEY为实际值' as reminder;

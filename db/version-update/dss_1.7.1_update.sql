INSERT INTO copilot_server_prompt_template
(prompt_type, `desc`, icon, title, name, tip, template, example, collection_name, top_n, shield)
VALUES('CodeCorrection', '可请求AI对代码进行语法纠错', NULL, '代码语法纠错', 'markdown', NULL, '请检查并修改如下代码中的语法错误:%s，执行错误信息如下:%s', '请检查并修改如下代码中的语法错误:CREATES TABLE demo_sales_report_work.test (
ordertest bigints
);,执行错误信息如下：您的sql代码可能有语法错误，请检查sql代码', NULL, NULL, 1);

CREATE TABLE IF NOT EXISTS `copilot_server_codecompletion` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '用户名',
  `language` varchar(32) COLLATE utf8mb4_bin NOT NULL COMMENT '脚本类型',
  `prefix` text COLLATE utf8mb4_bin COMMENT '代码补全前缀',
  `suffix` text COLLATE utf8mb4_bin COMMENT '补全后缀',
  `complete_content` text COLLATE utf8mb4_bin COMMENT '补全内容',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `answer_time` datetime DEFAULT NULL COMMENT '回复时间',
  `adopt` tinyint(1) DEFAULT NULL COMMENT '是否采纳，1采纳，0未采纳',
  `complete_times` int(11) DEFAULT 0 COMMENT '补全次数',
  `adopt_time` datetime DEFAULT NULL COMMENT '采纳时间',
  `status` varchar(32) DEFAULT NULL COLLATE utf8mb4_bin COMMENT '补全请求状态',
  `error_message` text DEFAULT NULL COLLATE utf8mb4_bin COMMENT '错误信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO copilot_server_llm_client
(id, route_regex, url, status, name, create_time, update_time, offline_time, config)
VALUES(4, '.*', 'http://172.21.140.49:7001/v1/completions', 'online', 'tabby', '2024-08-13 20:52:00', '2024-08-13 20:55:28', '2024-08-13 20:55:28', '{}');

update copilot_server_prompt_template set example = '生成一个简单的python程序示例，实现使用pandas库来读取一个csv文件，并计算每一列的平均值,在每个用户的交易记录中，我想为他们添加一个顺序号，我应该怎样使用row_number窗口函数实现'
where prompt_type = 'Common'
;


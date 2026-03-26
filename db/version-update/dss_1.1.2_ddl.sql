SET @@autocommit=0;
START TRANSACTION;

CREATE TABLE `dss_admin_dept` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父部门id',
  `ancestors` varchar(50) DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) DEFAULT '' COMMENT '部门名称',
  `order_num` int(4) DEFAULT '0' COMMENT '显示顺序',
  `leader` varchar(20) DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `status` char(1) DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

CREATE TABLE IF NOT EXISTS `dss_guide_group` (
  `id` BIGINT(13) NOT NULL AUTO_INCREMENT,
  `path` VARCHAR(100) NOT NULL COMMENT '页面URL路径',
  `title` VARCHAR(255) DEFAULT NULL COMMENT '标题',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '0:未删除(默认), 1已删除',
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='用户向导页面';

CREATE TABLE IF NOT EXISTS `dss_guide_content` (
  `id` BIGINT(13) NOT NULL AUTO_INCREMENT,
  `group_id` BIGINT(50) NOT NULL COMMENT '所属页面ID',
  `path` VARCHAR(100) NOT NULL COMMENT '所属页面URL路径',
  `title` VARCHAR(255) DEFAULT NULL COMMENT '标题',
  `title_alias` VARCHAR(50) DEFAULT NULL COMMENT '标题简称',
  `seq` VARCHAR(20) DEFAULT NULL COMMENT '序号',
  `type` INT(1) DEFAULT '1' COMMENT '类型: 1-步骤step，2-问题question',
  `content` TEXT DEFAULT NULL COMMENT 'Markdown格式的内容',
  `content_html` MEDIUMTEXT DEFAULT NULL COMMENT 'Markdown内容转化为HTML格式',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '0:未删除(默认), 1已删除',
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='用户向导页面内容详情';

CREATE TABLE `dss_download_audit`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `creator` varchar(255)  COMMENT '创建者',
  `tenant` varchar(255)  COMMENT '租户',
	`path` varchar(255)  COMMENT '文件路径',
	`sql` varchar(3000)  COMMENT '执行sql脚本',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	 PRIMARY KEY (`id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '文件下载审计';

CREATE TABLE IF NOT EXISTS `dss_guide_catalog` (
  `id` BIGINT(13) NOT NULL AUTO_INCREMENT,
  `parent_id` BIGINT(13) NOT NULL COMMENT '父级目录ID，-1代表最顶级目录',
  `title` VARCHAR(255) DEFAULT NULL COMMENT '标题',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `create_by` VARCHAR(255) DEFAULT NULL COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(255) DEFAULT NULL COMMENT '更新者',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` TINYINT(1) DEFAULT '0' COMMENT '0:未删除(默认), 1已删除',
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='用户向导知识库目录';

CREATE TABLE IF NOT EXISTS `dss_guide_chapter` (
  `id` BIGINT(13) NOT NULL AUTO_INCREMENT,
  `catalog_id` BIGINT(13) NOT NULL COMMENT '目录ID',
  `title` VARCHAR(255) DEFAULT NULL COMMENT '标题',
  `title_alias` VARCHAR(50) DEFAULT NULL COMMENT '标题简称',
  `content` TEXT DEFAULT NULL COMMENT 'Markdown格式的内容',
  `content_html` MEDIUMTEXT DEFAULT NULL COMMENT 'Markdown转换为html内容',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '0:未删除(默认), 1已删除',
  PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='用户向导知识库文章';

CREATE TABLE `dss_proxy_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) DEFAULT NULL,
  `proxy_user_name` varchar(64) DEFAULT NULL,
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=214 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `dss_dataapi_config` (
	`id` BIGINT ( 20 ) NOT NULL AUTO_INCREMENT COMMENT '主键',
	`workspace_id` BIGINT ( 20 ) NOT NULL COMMENT '工作空间id',
	`api_name` VARCHAR ( 255 ) NOT NULL COMMENT 'API名称',
	`api_path` VARCHAR ( 255 ) NOT NULL unique COMMENT 'API Path',
	`group_id` BIGINT ( 20 ) NOT NULL COMMENT 'API组id',
	`api_type` VARCHAR ( 20 ) NOT NULL COMMENT 'API类型：GUIDE-向导模式，SQL-脚本模式',
	`protocol` VARCHAR ( 20 ) NOT NULL COMMENT 'Http协议',
	`datasource_id` BIGINT ( 20 ) NOT NULL COMMENT '数据源id',
	`datasource_name` VARCHAR ( 50 ) DEFAULT NULL COMMENT '数据源名称',
	`datasource_type` VARCHAR ( 20 ) DEFAULT NULL COMMENT '数据源类型',
	`sql` text COMMENT 'sql模板',
	`tbl_name` VARCHAR ( 100 ) DEFAULT NULL COMMENT '数据表名称',
    `req_fields` VARCHAR ( 1000 ) CHARACTER SET utf8 DEFAULT NULL COMMENT '请求字段名称',
    `res_fields` VARCHAR ( 1000 ) CHARACTER SET utf8 DEFAULT NULL COMMENT '返回字段名称',
	`order_fields` VARCHAR ( 500 ) DEFAULT NULL COMMENT '排序字段名称及方式',
	`is_test` TINYINT ( 1 ) DEFAULT '0' COMMENT '是否测试成功：0未测试(默认)，1测试成功',
	`status` TINYINT ( 1 ) DEFAULT '0' COMMENT 'API状态：0未发布(默认)，1已发布',
	`previlege` VARCHAR ( 20 ) DEFAULT 'WORKSPACE' COMMENT 'WORKSPACE,PRIVATE',
	`method` VARCHAR ( 20 ) DEFAULT NULL COMMENT 'HTTPS,HTTP',
	`describe` VARCHAR ( 255 ) DEFAULT NULL COMMENT '描述',
	`memory` INT DEFAULT NULL COMMENT '内存大小',
	`req_timeout` INT DEFAULT NULL COMMENT '请求超时时间',
	`label` VARCHAR ( 255 ) DEFAULT NULL COMMENT '标签',
	`create_by` VARCHAR ( 255 ) DEFAULT NULL COMMENT '创建者',
	`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`update_by` VARCHAR ( 255 ) DEFAULT NULL COMMENT '更新者',
	`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
	`is_delete` TINYINT ( 1 ) DEFAULT '0' COMMENT '0:未删除(默认), 1已删除',
	`page_size` int  DEFAULT 0 COMMENT '每页数据大小',

	PRIMARY KEY ( `id` )
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = 'API';

CREATE TABLE `dss_dataapi_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `workspace_id` bigint(20) DEFAULT NULL COMMENT '工作空间id',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `note` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `create_time`  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '0:未删除(默认), 1已删除',
  PRIMARY KEY (`id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT='服务组';

CREATE TABLE `dss_dataapi_auth` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `workspace_id` bigint(20) NOT NULL COMMENT '工作空间ID',
  `caller` varchar(255)  DEFAULT NULL COMMENT '调用者名称',
  `token` varchar(255)  DEFAULT NULL COMMENT 'token字符串',
  `expire` datetime DEFAULT NULL COMMENT 'token过期时间',
  `group_id` bigint(20) DEFAULT NULL COMMENT 'api组',
  `create_by` varchar(255)  DEFAULT NULL COMMENT '创建者',
  `create_time`  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(255)  DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '0:未删除(默认), 1已删除',
  PRIMARY KEY (`id`)
) ENGINE=INNODB  DEFAULT CHARSET=utf8mb4 COMMENT='API认证';

CREATE TABLE `dss_dataapi_call` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `api_id` bigint(11) NOT NULL COMMENT 'API ID',
  `params_value` text COMMENT '调用参数名称和值',
  `status` tinyint(255) DEFAULT NULL COMMENT '执行状态：1成功，2失败，3超时',
  `time_start` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '开始时间',
  `time_end` datetime DEFAULT NULL COMMENT '结束时间',
  `time_length` bigint(20) DEFAULT NULL COMMENT '调用时长',
  `caller` varchar(255) DEFAULT NULL COMMENT '调用者名称',
  PRIMARY KEY (`id`)
) ENGINE=INNODB  DEFAULT CHARSET=utf8mb4 COMMENT='API调用记录'
;
CREATE TABLE `dss_dataapi_datasource` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `workspace_id` bigint(20) DEFAULT NULL COMMENT '工作空间id',
  `name` varchar(255) NOT NULL COMMENT '名称',
  `note` varchar(255) DEFAULT NULL COMMENT '描述',
  `type` varchar(255) NOT NULL COMMENT '数据库类型',
  `url` varchar(255) NOT NULL COMMENT '连接url',
  `username` varchar(255) NOT NULL COMMENT '用户名',
  `pwd` varchar(255) NOT NULL COMMENT '密码',
  `create_by` varchar(255) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(255) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_delete` tinyint(1) DEFAULT '0' COMMENT '0:未删除(默认), 1已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源';


alter table dss_onestop_user_favorites add `type` varchar(20) DEFAULT NULL COMMENT '类型,#区分收藏和盯一盯';

ALTER  TABLE dss_orchestrator_info modify column uuid varchar(100) not null;

alter table dss_workspace add `workspace_type` varchar(20);

alter table dss_user add `dept_id` int(11) DEFAULT NULL;
alter table dss_user add `email` varchar(50) DEFAULT '' COMMENT '用户邮箱';
alter table dss_user add `phonenumber` varchar(11) DEFAULT '' COMMENT '手机号码';
alter table dss_user add `password` varchar(100) DEFAULT '' COMMENT '密码';
alter table dss_user add `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）';
alter table dss_user add `create_by` varchar(64) DEFAULT NULL COMMENT '创建者';
alter table dss_user add `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
alter table dss_user add `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
alter table dss_user add `remark` varchar(500) DEFAULT NULL COMMENT '备注';

ALTER TABLE dss_flow_edit_lock MODIFY COLUMN flow_version varchar(16) NULL;


COMMIT;
SET @@autocommit=1;
SET @@autocommit=0;
START TRANSACTION;
-- 版本发布时的releaseNote信息
CREATE TABLE `dss_release_note_content`
(
    `id`           int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`         varchar(200) DEFAULT NULL COMMENT '名称',
    `title`        varchar(200) DEFAULT NULL COMMENT '标题',
    `url`          varchar(300) DEFAULT NULL COMMENT 'url',
    `url_type`     int(1)       DEFAULT '1' COMMENT 'url类型: 0-内部系统，1-外部系统；默认是外部',
    `release_type` int(1)       DEFAULT '0' COMMENT '发布形式: 0-作为dss整体发布，1-单独发布scriptis；默认是dss',
    `create_time`  datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='releaseNote表';

-- 用户访问行为统计表，初期只有登录行为统计
CREATE TABLE `dss_user_access_audit`
(
    `id`          int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_name`   varchar(64) DEFAULT NULL COMMENT '用户名',
    `login_count` BIGINT      DEFAULT 0 COMMENT '登录次数',
    `first_login` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '第一次登录时间',
    `last_login`  datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '上一次登录时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_name` (`user_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户访问行为次数统计';
-- 更新releaseNote
DELETE  FROM dss_release_note_content;
INSERT INTO dss_release_note_content (name,title,url,url_type,release_type,create_time) VALUES
('feature1','【新增】任务完成时发送通知至企业微信','_book/知识库/用户手册/版本功能介绍/v1.1.9/任务完成时发送通知至企业微信.html',1,1,now()),
('feature2','【新增】工作空间管理员kill空闲引擎','_book/知识库/用户手册/版本功能介绍/v1.1.9/工作空间管理员kill空闲引擎.html',1,0,now()),
('feature3','【功能增强】新编辑器优化','_book/知识库/用户手册/版本功能介绍/v1.1.9/新编辑器优化.html',1,1,now()),
('feature4','【新增】表属主转移提单对接ITSM','_book/知识库/用户手册/版本功能介绍/v1.1.9/表属主转移提单对接ITSM.html',1,1,now()),
('feature5','【新增】数据库备注信息展示','_book/知识库/用户手册/版本功能介绍/v1.1.9/数据库备注信息展示.html',1,1,now()),
('feature6','【新增】库详情数据支持实时删除','_book/知识库/用户手册/版本功能介绍/v1.1.9/库详情数据支持实时删除.html',1,1,now()),
('feature7','【新增】建表向导校验规则优化','_book/知识库/用户手册/版本功能介绍/v1.1.9/建表向导校验规则优化.html',1,1,now()),
('feature8','【新增】版本发布时前端提示用户刷新','_book/知识库/用户手册/版本功能介绍/v1.1.9/版本发布时前端提示用户刷新.html',1,1,now()),
('feature9','【新增】新用户引导优化','_book/知识库/用户手册/版本功能介绍/v1.1.9/新用户引导优化.html',1,1,now()),
('feature10','【功能优化】查看表结构时交互页面优化','_book/知识库/用户手册/版本功能介绍/v1.1.9/查看表结构时交互页面优化.html',1,1,now())
;

COMMIT;
SET @@autocommit=1;
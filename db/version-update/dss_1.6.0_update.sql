ALTER TABLE `dss_project`
    ADD COLUMN `label` varchar(128)  CHARACTER SET utf8mb4 COLLATE utf8mb4_bin COMMENT
    '标签，用于区分不同来源请求创建的项目' AFTER `associate_git`;
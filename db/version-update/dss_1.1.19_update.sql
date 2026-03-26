ALTER TABLE dss_workspace_dictionary ADD checked TINYINT default 0 COMMENT '默认勾选';

UPDATE
    `dss_workspace_dictionary`
SET
    checked = 1
WHERE
    dic_name = '开发中心' AND dic_value = 'dev';

UPDATE
    `dss_workspace_dictionary`
SET
    checked = 1
WHERE
    dic_name = '生产中心' AND dic_value = 'prod';

UPDATE
    `dss_workflow_node_ui_validate`
SET
    validate_range = '^(((check.object).\\w+=[^.\\s;]+(\\.[^.\\s;]+)+[;\\s]*)|((source.type).\\w+=\\w+[;\\s]*))\\+$'
WHERE
    error_msg ='请正确填写多源配置' and error_msg_en = 'params config error, please make sure!';
-- 删除部分appconn引擎类型对应节点的是否复用引擎ui，强制复用引擎
delete
from dss_workflow_node_to_ui
where ui_id = (select id
               from dss_workflow_node_ui dwnu
               where dwnu.key = 'ReuseEngine')
  and workflow_node_id in (select id
                           from dss_workflow_node dwn
                           where dwn.appconn_name in
                                 ('datachecker', 'eventchecker', 'metabase', 'newVisualis', 'sendemail', 'visualis'));

--  hive引擎添加map、reduce任务限制参数
-- 添加 mapreduce.job.running.map.limit 和 mapreduce.job.running.reduce.limit 限制参数至 dss_workflow_node_ui 表中, 填写限制提示信息。
INSERT  INTO `dss_workflow_node_ui` (`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values
  ('mapreduce.job.running.map.limit','请填写map任务数限制','please map task limit','mapreduce.job.running.map.limit','mapreduce.job.running.map.limit','Input',0,NULL,'200000',0,"!${params.configuration.startup['ec.conf.templateId']}",0,1,1,0,'startup');

INSERT INTO `dss_workflow_node_ui` (`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values ('mapreduce.job.running.reduce.limit','请填写reduce任务数限制','please reduce task limit','mapreduce.job.running.reduce.limit','mapreduce.job.running.reduce.limit','Input',0,NULL,'999',0,"!${params.configuration.startup['ec.conf.templateId']}",0,1,1,0,'startup');

-- 添加 dss_workflow_node主键ID和dss_workflow_node_ui主键ID 关联信息至节点ui中间表 dss_workflow_node_to_ui
INSERT INTO `dss_workflow_node_to_ui` (`workflow_node_id`, `ui_id`) VALUES (
    (SELECT id FROM `dss_workflow_node` WHERE name = 'hql'),
    (SELECT id FROM `dss_workflow_node_ui` WHERE `key` = 'mapreduce.job.running.map.limit')
);

INSERT INTO `dss_workflow_node_to_ui` (`workflow_node_id`, `ui_id`) VALUES (
    (SELECT id FROM `dss_workflow_node` WHERE name = 'hql'),
    (SELECT id FROM `dss_workflow_node_ui` WHERE `key` = 'mapreduce.job.running.reduce.limit')
);

-- 添加规则校验
insert into `dss_workflow_node_ui_validate` (`validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`)
values('Regex', '^(1[0-9]{1,5}|200000|[1-9][0-9]{1,4})$', '设置范围为[10,200000],设置超出限制', 'map task limit 10,200000', 'blur');

insert into `dss_workflow_node_ui_validate` (`validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`)
values('Regex', '^([1-9][0-9]{1,2}|999|)$', '设置范围为[10,999],设置超出限制', 'reduce task limit 10,999', 'blur');

-- 关联校验

insert into dss_workflow_node_ui_to_validate (ui_id,validate_id)  values (
  (select id  from `dss_workflow_node_ui` WHERE `key` = 'mapreduce.job.running.map.limit'),
  (select id  from dss_workflow_node_ui_validate  where  error_msg_en = 'map task limit 10,200000')
);

insert into dss_workflow_node_ui_to_validate (ui_id,validate_id)  values (
  (select id  from `dss_workflow_node_ui` WHERE `key` = 'mapreduce.job.running.reduce.limit'),
  (select id  from dss_workflow_node_ui_validate  where  error_msg_en = 'reduce task limit 10,999');


 update  dss_workflow_node_ui_validate set error_msg = '设置范围为[1,10],设置超出限制',error_msg_en = 'must be between 1 and 10'  where id = (select t2.validate_id  from dss_workflow_node_ui t1
 join dss_workflow_node_ui_to_validate t2 on t1.id = t2.ui_id
 where t1.`key`  = 'spark.executor.cores' limit 1);


 update  dss_workflow_node_ui_validate set error_msg = '设置范围为[1,40],设置超出限制',error_msg_en = 'must be between 1 and 40'  where id = (select t2.validate_id  from dss_workflow_node_ui t1
 join dss_workflow_node_ui_to_validate t2 on t1.id = t2.ui_id
 where t1.`key`  = 'spark.executor.instances' limit 1);
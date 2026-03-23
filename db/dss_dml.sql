DELETE FROM dss_appconn;
INSERT INTO `dss_appconn` (`id`, `appconn_name`, `is_user_need_init`, `level`, `if_iframe`, `is_external`, `reference`, `class_name`, `appconn_class_path`, `resource`)
VALUES (1,'sso',0,1,0,0,NULL,"com.webank.wedatasphere.dss.appconn.sso.SSOAppConn",NULL,NULL),
(2,'scriptis',0,1,0,0,"sso",NULL,NULL,NULL),
(3,'workflow',0,1,1,0,NULL,'com.webank.wedatasphere.dss.appconn.workflow.WorkflowAppConn','/appcom/Install/dss/dss-appconns/workflow',NULL),
(4,'apiservice',0,1,0,0,"sso",NULL,NULL,NULL);

DELETE FROM dss_appconn_instance;
select @scriptis_appconn_id:= id from dss_appconn where appconn_name="scriptis";
select @workflow_appconn_id:= id from dss_appconn where appconn_name="workflow";
select @apiservice_appconn_id:= id from dss_appconn where appconn_name="apiservice";
INSERT INTO `dss_appconn_instance` (`id`, `appconn_id`, `label`, `url`, `enhance_json`, `homepage_uri`)
VALUES (2, @scriptis_appconn_id, 'DEV', '/home', '', ''),
(3, @workflow_appconn_id,'DEV','/workspaceHome','',''),
(4, @apiservice_appconn_id, 'DEV', '/apiservices', '', '');

DELETE FROM dss_workspace;
insert into `dss_workspace`(`id`, `name`,`label`,`description`,`create_by`,`create_time`,`department`,`product`,`source`,`last_update_time`,`last_update_user`,`workspace_type`)
values(224, 'bdapWorkspace','','bdapWorkspace','hadoop','2020-07-13 02:39:41','1','bdapWorkspace',NULL,'2020-07-13 02:39:41','hadoop','project');

DELETE FROM dss_user;
INSERT INTO `dss_user` VALUES (100,'hadoop_test','hadoop_test',1,101,1,'','','','0',NULL,'2021-11-17 09:33:45','2021-11-17 09:51:55',NULL),
(215,'hadoop','hadoop',1,101,1,'','','','0',NULL,'2021-11-17 09:43:41','2021-11-17 09:51:49',NULL);

DELETE FROM dss_workspace_dictionary;
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (1,0,'0','绌洪棿寮€鍙戞祦绋?,'Space development process','w_develop_process',NULL,NULL,NULL,NULL,NULL,0,NULL,1,'绌洪棿寮€鍙戞祦绋?,'SYSTEM','2020-12-28 17:32:34',NULL,'2021-02-22 17:46:40');
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (2,0,'w_develop_process','闇€姹?,'Demand','wdp_demand','鍒涘缓鏂扮殑涓氬姟闇€姹傦紝骞跺皢闇€姹傛寚娲剧粰瀵瑰簲璐熻矗浜恒€?,'Create new business requirements and assign them to the corresponding responsible person.','Demo妗堜緥','Demo case',NULL,0,'xuqiu',1,'绌洪棿寮€鍙戞祦绋?闇€姹?,'SYSTEM','2020-12-28 17:32:35',NULL,'2021-02-23 09:38:07',0);
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (3,0,'w_develop_process','璁捐','Design','wdp_design','閽堝鏂扮殑涓氬姟闇€姹傦紝杩涜鏁颁粨瑙勫垝鍜屽簱琛ㄨ璁°€?,'According to the new business requirements, data warehouse planning and database table design are carried out.','Demo妗堜緥','Demo case',NULL,0,'sheji',1,'绌洪棿寮€鍙戞祦绋?璁捐','SYSTEM','2020-12-28 17:32:35',NULL,'2021-02-23 09:38:09',0);
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (4,0,'w_develop_process','寮€鍙?,'Development','wdp_development','閽堝鏂扮殑涓氬姟闇€姹傦紝杩涜鏁颁粨瑙勫垝鍜屽簱琛ㄨ璁°€?,'According to the new business requirements, data warehouse planning and database table design are carried out.','Demo妗堜緥','Demo case',NULL,0,'kaifa',1,'绌洪棿寮€鍙戞祦绋?寮€鍙?,'SYSTEM','2020-12-28 17:32:35',NULL,'2021-02-23 09:38:10',0);
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (5,0,'w_develop_process','璋冭瘯','Debugging','wdp_debug','鍒涘缓鏂扮殑涓氬姟闇€姹傦紝骞跺皢闇€姹傛寚娲剧粰瀵瑰簲璐熻矗浜恒€?,'Create new business requirements and assign them to the corresponding responsible person.','Demo妗堜緥','Demo case',NULL,0,'tiaoshi',1,'绌洪棿寮€鍙戞祦绋?璋冭瘯','SYSTEM','2020-12-28 17:32:35',NULL,'2021-02-23 09:38:11',0);
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (6,0,'w_develop_process','鐢熶骇','Production','wdp_product','鍒涘缓鏂扮殑涓氬姟闇€姹傦紝骞跺皢闇€姹傛寚娲剧粰瀵瑰簲璐熻矗浜恒€?,'Create new business requirements and assign them to the corresponding responsible person.','Demo妗堜緥','Demo case',NULL,0,'shengchan',1,'绌洪棿寮€鍙戞祦绋?鐢熶骇','SYSTEM','2020-12-28 17:32:35',NULL,'2021-02-23 09:38:12',0);
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (7,0,'0','宸ョ▼寮€鍙戞祦绋?,'Engineering development process','p_develop_process',NULL,NULL,NULL,NULL,NULL,0,NULL,1,'宸ョ▼寮€鍙戞祦绋?,'SYSTEM','2020-12-28 17:32:35',NULL,'2021-02-22 17:48:48',0);
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (8,0,'p_develop_process','寮€鍙戜腑蹇?,'Development Center','pdp_development_center','dev',NULL,NULL,NULL,NULL,0,'kaifa-icon',1,'宸ョ▼寮€鍙戞祦绋?寮€鍙戜腑蹇?,'SYSTEM','2020-12-28 17:32:35',NULL,'2021-02-22 17:49:02',1);
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (10,0,'0','宸ョ▼缂栨帓妯″紡','Project layout mode','p_orchestrator_mode',NULL,NULL,NULL,NULL,NULL,0,NULL,1,'宸ョ▼缂栨帓妯″紡','SYSTEM','2020-12-28 17:32:35',NULL,'2021-02-22 17:49:36',0);
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (11,0,'p_orchestrator_mode','宸ヤ綔娴?,'Workflow','pom_work_flow','radio',NULL,NULL,NULL,NULL,0,'gongzuoliu-icon',1,'宸ョ▼缂栨帓妯″紡-宸ヤ綔娴?,'SYSTEM','2020-12-28 17:32:35',NULL,'2021-02-22 17:49:49',0);
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (14,0,'pom_work_flow','DAG','DAG','pom_work_flow_DAG',NULL,NULL,NULL,NULL,NULL,0,NULL,1,'宸ョ▼缂栨帓妯″紡-宸ヤ綔娴?DAG','SYSTEM','2020-12-28 17:32:35',NULL,'2021-02-22 17:50:31',0);
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (16,0,'pom_single_task','Scriptis','Scriptis','pom_single_task_scriptis',NULL,NULL,NULL,NULL,NULL,0,NULL,1,'宸ョ▼缂栨帓妯″紡-鍗曚换鍔?Scriptis','SYSTEM','2020-12-28 17:32:35',NULL,'2021-02-22 17:51:08',0);
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (18,0,'pom_single_task','Qualitis','Qualitis','pom_single_task_qualitis',NULL,NULL,NULL,NULL,NULL,0,NULL,1,'宸ョ▼缂栨帓妯″紡-鍗曚换鍔?Qualitis','SYSTEM','2020-12-28 17:32:35',NULL,'2021-02-22 17:50:53',0);
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (20,0,'pom_consist_orchestrator','Qualitis','Qualitis','pom_consist_orchestrator_qualitis',NULL,NULL,NULL,NULL,NULL,0,NULL,1,'宸ョ▼缂栨帓妯″紡-缁勫悎缂栨帓-Qualitis','SYSTEM','2020-12-28 17:32:35',NULL,'2021-02-22 17:50:57',0);
insert into `dss_workspace_dictionary`(`id`,`workspace_id`,`parent_key`,`dic_name`,`dic_name_en`,`dic_key`,`dic_value`,`dic_value_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`, checked) values (21,0,'pom_consist_orchestrator','Email','Email','pom_consist_orchestrator_email',NULL,NULL,NULL,NULL,NULL,0,NULL,1,'宸ョ▼缂栨帓妯″紡-缁勫悎缂栨帓-Email','SYSTEM','2020-12-28 17:32:35',NULL,'2021-02-22 17:51:22',0);
insert into `dss_workspace_dictionary`(`workspace_id`, `parent_key`, `dic_name`, `dic_name_en`, `dic_key`, `dic_value`, `dic_value_en`, `title`, `title_en`, `url`, `url_type`, `icon`, `order_num`, `remark`, `create_user`, `create_time`, `update_user`, `update_time`, checked) values('0','0','宸ヤ綔绌洪棿榛樿閮ㄩ棬','Space development name','w_workspace_department','10001-閮ㄩ棬涓€;10002-閮ㄩ棬浜?10003-閮ㄩ棬涓?,NULL,NULL,NULL,NULL,'0',NULL,'1','宸ヤ綔绌洪棿榛樿閮ㄩ棬锛屽墠闈㈡槸id鍚庨潰鏄儴闂ㄥ悕绉颁腑闂翠娇鐢ㄢ€?鈥?妯潌鍒嗛殧锛屽涓互鑻辨枃鍒嗗彿鍒嗛殧','SYSTEM','2020-12-28 17:32:34',NULL,'2021-02-22 17:46:40',0);

DELETE FROM dss_sidebar;
insert  into `dss_sidebar`(`id`,`workspace_id`,`name`,`name_en`,`title`,`title_en`,`type`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`) values (2,0,'鑿滃崟','Menu','鑿滃崟','Menu',1,1,NULL,'SYSTEM','2020-12-15 13:21:06',NULL,'2021-02-23 09:45:50');
--insert  into `dss_sidebar`(`id`,`workspace_id`,`name`,`name_en`,`title`,`title_en`,`type`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`) values (3,0,'甯歌闂','Common problem','甯歌闂','Common problem',1,1,NULL,'SYSTEM','2020-12-15 13:21:06',NULL,'2021-02-23 09:46:18');

DELETE FROM dss_sidebar_content;
insert  into `dss_sidebar_content`(`id`,`workspace_id`,`sidebar_id`,`name`,`name_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`) values (3,0,2,NULL,NULL,'宸ヤ綔绌洪棿绠＄悊','Workspace management','/workspaceManagement/productsettings',0,'menuIcon',1,NULL,'SYSTEM','2020-12-15 13:21:07',NULL,'2021-02-23 09:47:49');
insert  into `dss_sidebar_content`(`id`,`workspace_id`,`sidebar_id`,`name`,`name_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`) values (4,0,2,NULL,NULL,'UDF绠＄悊','UDF management','dss/linkis/?noHeader=1&noFooter=1#/urm/udfManagement',1,'menuIcon',1,NULL,'SYSTEM','2020-12-15 13:21:07',NULL,'2021-02-23 09:47:11');
--insert  into `dss_sidebar_content`(`id`,`workspace_id`,`sidebar_id`,`name`,`name_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`) values (5,0,3,NULL,NULL,'璧勬簮閰嶇疆璇存槑',NULL,'http://127.0.0.1:8088/kn/d/38',1,'fi-warn',1,NULL,'SYSTEM','2020-12-15 13:21:07',NULL,'2021-01-12 17:16:52');
--insert  into `dss_sidebar_content`(`id`,`workspace_id`,`sidebar_id`,`name`,`name_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`) values (6,0,3,NULL,NULL,'Spark浣跨敤鎸囧崡','[Discussion on error code 22223]','http://127.0.0.1:8088/kn/d/40',1,'fi-warn',1,NULL,'SYSTEM','2020-12-15 13:21:07',NULL,'2021-02-23 09:48:28');
--insert  into `dss_sidebar_content`(`id`,`workspace_id`,`sidebar_id`,`name`,`name_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`) values (7,0,3,NULL,NULL,'Hive璇硶浠嬬粛',NULL,'http://127.0.0.1:8088/kn/d/34',1,'fi-warn',1,NULL,'SYSTEM','2020-12-15 13:21:07',NULL,'2021-01-12 17:17:00');
--insert  into `dss_sidebar_content`(`id`,`workspace_id`,`sidebar_id`,`name`,`name_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`) values (8,0,3,NULL,NULL,'宸ヤ綔娴佷娇鐢ㄤ粙缁?,NULL,'http://127.0.0.1:8088/kn/d/42',1,'fi-warn',1,NULL,'SYSTEM','2020-12-15 13:21:07',NULL,'2021-01-12 17:17:01');
--insert  into `dss_sidebar_content`(`id`,`workspace_id`,`sidebar_id`,`name`,`name_en`,`title`,`title_en`,`url`,`url_type`,`icon`,`order_num`,`remark`,`create_user`,`create_time`,`update_user`,`update_time`) values (9,0,3,NULL,NULL,'鏁版嵁鏈嶅姟浣跨敤浠嬬粛','Discussion on error code 22223','http://127.0.0.1:8088/kn/d/32',1,'fi-warn',1,NULL,'SYSTEM','2020-12-15 13:21:07',NULL,'2021-02-23 09:48:19');

DELETE FROM dss_workspace_menu;
INSERT INTO `dss_workspace_menu` (`id`, `name`, `title_en`, `title_cn`, `description`, `is_active`, `icon`, `order`, `create_by`, `create_time`, `last_update_time`, `last_update_user`) VALUES('1','鏁版嵁浜ゆ崲','data exchange','鏁版嵁浜ゆ崲','鏁版嵁浜ゆ崲鎻忚堪','1',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `dss_workspace_menu` (`id`, `name`, `title_en`, `title_cn`, `description`, `is_active`, `icon`, `order`, `create_by`, `create_time`, `last_update_time`, `last_update_user`) VALUES('2','鏁版嵁鍒嗘瀽','data analysis','鏁版嵁鍒嗘瀽','鏁版嵁鍒嗘瀽鎻忚堪','1',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `dss_workspace_menu` (`id`, `name`, `title_en`, `title_cn`, `description`, `is_active`, `icon`, `order`, `create_by`, `create_time`, `last_update_time`, `last_update_user`) VALUES('3','鐢熶骇杩愮淮','production operation','鐢熶骇杩愮淮','鐢熶骇杩愮淮鎻忚堪','1',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `dss_workspace_menu` (`id`, `name`, `title_en`, `title_cn`, `description`, `is_active`, `icon`, `order`, `create_by`, `create_time`, `last_update_time`, `last_update_user`) VALUES('4','鏁版嵁璐ㄩ噺','data quality','鏁版嵁璐ㄩ噺','鏁版嵁璐ㄩ噺鎻忚堪','1',NULL,NULL,NULL,NULL,NULL,NULL);
INSERT INTO `dss_workspace_menu` (`id`, `name`, `title_en`, `title_cn`, `description`, `is_active`, `icon`, `order`, `create_by`, `create_time`, `last_update_time`, `last_update_user`) VALUES('5','绠＄悊鍛樺姛鑳?,'administrator function','绠＄悊鍛樺姛鑳?,'绠＄悊鍛樺姛鑳芥弿杩?,'0',NULL,NULL,NULL,NULL,NULL,NULL);
insert into `dss_workspace_menu` (`id`, `name`, `title_en`, `title_cn`, `description`, `is_active`, `icon`, `order`, `create_by`, `create_time`, `last_update_time`, `last_update_user`) values('6','鏁版嵁搴旂敤','data application','鏁版嵁搴旂敤','鏁版嵁搴旂敤鎻忚堪','1',NULL,NULL,NULL,NULL,NULL,NULL);
insert into `dss_workspace_menu` (`id`, `name`, `title_en`, `title_cn`, `description`, `is_active`, `icon`, `order`, `create_by`, `create_time`, `last_update_time`, `last_update_user`) values('7','搴旂敤寮€鍙?,'application development','搴旂敤寮€鍙?,'搴旂敤寮€鍙戞弿杩?,'1',NULL,NULL,NULL,NULL,NULL,NULL);

DELETE FROM dss_workspace_menu_appconn;
INSERT INTO dss_workspace_menu_appconn (appconn_id, menu_id, title_en, title_cn, desc_en, desc_cn, labels_en, labels_cn, is_active, access_button_en, access_button_cn, manual_button_en, manual_button_cn, manual_button_url, icon, `order`, create_by, create_time, last_update_time, last_update_user, image)
VALUES (@scriptis_appconn_id, 2, 'Scriptis', 'Scriptis', 'Scriptis is a one-stop interactive data exploration analysis tool built by WeDataSphere, uses Linkis as the kernel.', 'Scriptis鏄井浼楅摱琛屽井鏁板煙(WeDataSphere)鎵撻€犵殑涓€绔欏紡浜や簰寮忔暟鎹帰绱㈠垎鏋愬伐鍏凤紝浠ヤ换鎰忔ˉ(Linkis)鍋氫负鍐呮牳锛屾彁渚涘绉嶈绠楀瓨鍌ㄥ紩鎿?濡係park銆丠ive銆乀iSpark绛?銆丠ive鏁版嵁搴撶鐞嗗姛鑳姐€佽祫婧?濡俌arn璧勬簮銆佹湇鍔″櫒璧勬簮)绠＄悊銆佸簲鐢ㄧ鐞嗗拰鍚勭鐢ㄦ埛璧勬簮(濡俇DF銆佸彉閲忕瓑)绠＄悊鐨勮兘鍔涖€?, 'scripts development,IDE', '鑴氭湰寮€鍙?IDE', 1, 'enter Scriptis', '杩涘叆Scriptis', 'user manual', '鐢ㄦ埛鎵嬪唽', 'http://127.0.0.1:8088/wiki/scriptis/manual/workspace_cn.html', 'shujukaifa-logo', null, null, null, null, null, 'shujukaifa-icon');
INSERT INTO dss_workspace_menu_appconn (appconn_id, menu_id, title_en, title_cn, desc_en, desc_cn, labels_en, labels_cn, is_active, access_button_en, access_button_cn, manual_button_en, manual_button_cn, manual_button_url, icon, `order`, create_by, create_time, last_update_time, last_update_user, image)
VALUES (@workflow_appconn_id, 2, 'workflow', '宸ヤ綔娴佸紑鍙?, '宸ヤ綔娴佸紑鍙?, '宸ヤ綔娴佸紑鍙?, null, null, 1, 'Enter workflow', '杩涘叆 宸ヤ綔娴佸紑鍙?, null, null, null, null, null, null, null, null, null, null);
INSERT INTO dss_workspace_menu_appconn (appconn_id, menu_id, title_en, title_cn, desc_en, desc_cn, labels_en, labels_cn, is_active, access_button_en, access_button_cn, manual_button_en, manual_button_cn, manual_button_url, icon, `order`, create_by, create_time, last_update_time, last_update_user, image)
VALUES (@apiservice_appconn_id, 7, 'dataService', '鏁版嵁鏈嶅姟', '/dataService', '/dataService', null, null, 1, 'Enter dataService', '杩涘叆 鏁版嵁鏈嶅姟', null, null, null, null, null, null, null, null, null, null);


DELETE FROM dss_workspace_role;
INSERT INTO `dss_workspace_role` (`id`, `workspace_id`, `name`, `front_name`, `update_time`, `description`) VALUES('1','-1','admin','绠＄悊鍛?,'2020-07-13 02:43:35','閫氱敤瑙掕壊绠＄悊鍛?);
INSERT INTO `dss_workspace_role` (`id`, `workspace_id`, `name`, `front_name`, `update_time`, `description`) VALUES('2','-1','maintenance','杩愮淮鐢ㄦ埛','2020-07-13 02:43:35','閫氱敤瑙掕壊杩愮淮鐢ㄦ埛');
INSERT INTO `dss_workspace_role` (`id`, `workspace_id`, `name`, `front_name`, `update_time`, `description`) VALUES('3','-1','developer','寮€鍙戠敤鎴?,'2020-07-13 02:43:35','閫氱敤瑙掕壊寮€鍙戠敤鎴?);
INSERT INTO `dss_workspace_role` (`id`, `workspace_id`, `name`, `front_name`, `update_time`, `description`) VALUES('4','-1','analyser','鍒嗘瀽鐢ㄦ埛','2020-07-13 02:43:36','閫氱敤瑙掕壊鍒嗘瀽鐢ㄦ埛');
INSERT INTO `dss_workspace_role` (`id`, `workspace_id`, `name`, `front_name`, `update_time`, `description`) VALUES('5','-1','operator','杩愯惀鐢ㄦ埛','2020-07-13 02:43:36','閫氱敤瑙掕壊杩愯惀鐢ㄦ埛');
INSERT INTO `dss_workspace_role` (`id`, `workspace_id`, `name`, `front_name`, `update_time`, `description`) VALUES('6','-1','boss','棰嗗','2020-07-13 02:43:36','閫氱敤瑙掕壊棰嗗');
INSERT INTO `dss_workspace_role` (`id`, `workspace_id`, `name`, `front_name`, `update_time`, `description`) VALUES('7','-1','apiUser','鏁版嵁鏈嶅姟鐢ㄦ埛','2020-08-21 11:35:02','閫氱敤瑙掕壊鏁版嵁鏈嶅姟鐢ㄦ埛');

DELETE FROM dss_workspace_user_role;
select @defaultWorkspaceId:=id from dss_workspace where name='bdapWorkspace';
insert  into `dss_workspace_user_role`(`workspace_id`,`username`,`role_id`,`create_time`,`created_by`,`user_id`) values
(@defaultWorkspaceId,'hadoop',1,'2021-09-06 14:39:17','hadoop',0),(@defaultWorkspaceId,'hadoop',2,'2021-09-06 14:39:17','hadoop',0),
(@defaultWorkspaceId,'hadoop',3,'2021-09-06 14:39:17','hadoop',0),(@defaultWorkspaceId,'hadoop',4,'2021-09-06 14:39:17','hadoop',0),
(@defaultWorkspaceId,'hadoop',5,'2021-09-06 14:39:17','hadoop',0),(@defaultWorkspaceId,'hadoop',6,'2021-09-06 14:39:17','hadoop',0),
(@defaultWorkspaceId,'hadoop',7,'2021-09-06 14:39:17','hadoop',0);

DELETE FROM dss_workflow_node;
insert into `dss_workflow_node` (`id`, `name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`, `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`) values('1','python','scriptis','linkis.python.python','2','1','1','1','0','svgs/python.svg');
insert into `dss_workflow_node` (`id`, `name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`, `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`) values('2','pyspark','scriptis','linkis.spark.py','2','1','1','1','0','svgs/pyspark.svg');
insert into `dss_workflow_node` (`id`, `name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`, `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`) values('3','sql','scriptis','linkis.spark.sql','2','1','1','1','0','svgs/sql.svg');
insert into `dss_workflow_node` (`id`, `name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`, `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`) values('4','scala','scriptis','linkis.spark.scala','2','1','1','1','0','svgs/scala.svg');
insert into `dss_workflow_node` (`id`, `name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`, `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`) values('5','hql','scriptis','linkis.hive.hql','2','1','1','1','0','svgs/hql.svg');
insert into `dss_workflow_node` (`id`, `name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`, `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`) values('6','jdbc','scriptis','linkis.jdbc.jdbc','2','1','1','1','0','icons/jdbc.icon');
insert into `dss_workflow_node` (`id`, `name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`, `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`) values('7','shell','scriptis','linkis.shell.sh','2','1','1','1','0','svgs/shell.svg');
insert into `dss_workflow_node` (`id`, `name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`, `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`) values('10','connector','scriptis','linkis.control.empty','2','0','1','1','0','icons/connector.icon');
insert into `dss_workflow_node` (`id`, `name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`, `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`) values('12','subFlow','scriptis','workflow.subflow','2','1','1','0','1','svgs/subflow.svg');
insert into `dss_workflow_node` (`id`, `name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`, `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`) values('13','branch','workflow','workflow.branch','0','0','0','1','0','svgs/subflow.svg');

DELETE FROM dss_workflow_node_group;
insert  into `dss_workflow_node_group`(`id`,`name`,`name_en`,`description`,`order`) values (1,'鏁版嵁浜ゆ崲','Data exchange',NULL,1);
insert  into `dss_workflow_node_group`(`id`,`name`,`name_en`,`description`,`order`) values (2,'鏁版嵁寮€鍙?,'Data development',NULL,2);
insert  into `dss_workflow_node_group`(`id`,`name`,`name_en`,`description`,`order`) values (3,'鏁版嵁璐ㄩ噺','Data Governance',NULL,3);
insert  into `dss_workflow_node_group`(`id`,`name`,`name_en`,`description`,`order`) values (4,'鏁版嵁鍙鍖?,'Data visualization',NULL,4);
insert  into `dss_workflow_node_group`(`id`,`name`,`name_en`,`description`,`order`) values (5,'鏁版嵁杈撳嚭','Data output',NULL,8);
insert  into `dss_workflow_node_group`(`id`,`name`,`name_en`,`description`,`order`) values (6,'淇″彿鑺傜偣','Signal node',NULL,6);
insert  into `dss_workflow_node_group`(`id`,`name`,`name_en`,`description`,`order`) values (7,'鍔熻兘鑺傜偣','Function node',NULL,7);
insert  into `dss_workflow_node_group`(`id`,`name`,`name_en`,`description`,`order`) values (8,'鏈哄櫒瀛︿範','Machine Learning',NULL,5);

DELETE FROM dss_workflow_node_to_group;
select @scriptis_node_groupId:=id from dss_workflow_node_group where name='鏁版嵁寮€鍙?;
select @function_node_groupId:=id from dss_workflow_node_group where name='鍔熻兘鑺傜偣';
insert  into `dss_workflow_node_to_group`(`node_id`,`group_id`) values (1, @scriptis_node_groupId);
insert  into `dss_workflow_node_to_group`(`node_id`,`group_id`) values (2, @scriptis_node_groupId);
insert  into `dss_workflow_node_to_group`(`node_id`,`group_id`) values (3, @scriptis_node_groupId);
insert  into `dss_workflow_node_to_group`(`node_id`,`group_id`) values (4, @scriptis_node_groupId);
insert  into `dss_workflow_node_to_group`(`node_id`,`group_id`) values (5, @scriptis_node_groupId);
insert  into `dss_workflow_node_to_group`(`node_id`,`group_id`) values (6, @scriptis_node_groupId);
insert  into `dss_workflow_node_to_group`(`node_id`,`group_id`) values (7, @scriptis_node_groupId);
insert  into `dss_workflow_node_to_group`(`node_id`,`group_id`) values (10, @function_node_groupId);
insert  into `dss_workflow_node_to_group`(`node_id`,`group_id`) values (12, @function_node_groupId);
insert  into `dss_workflow_node_to_group`(`node_id`,`group_id`) values (13, @function_node_groupId);

DELETE FROM dss_workflow_node_ui;
-- todo msg.topic鍦╮eceiver鍜宻ender浣跨敤浜嗛噸澶峩ey
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (1,'title','璇峰～鍐欒妭鐐瑰悕绉?,'Please enter node name','鑺傜偣鍚?,'Node name','Input',1,NULL,NULL,0,NULL,0,1,1,1,'node');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (3,'desc','璇峰～鍐欒妭鐐规弿杩?,'Please enter the node description','鑺傜偣鎻忚堪','Node description','Text',0,NULL,NULL,0,NULL,0,4,1,1,'node');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (5,'businessTag',NULL,NULL,'涓氬姟鏍囩','businessTag','Tag',0,NULL,NULL,0,NULL,0,2,1,1,'node');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (6,'appTag',NULL,NULL,'搴旂敤鏍囩','appTag','Tag',0,NULL,NULL,0,NULL,0,3,1,1,'node');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (7,'spark.driver.memory','椹卞姩鍣ㄥ唴瀛樺ぇ灏忥紝榛樿鍊硷細2','Driver memory, default value: 2','spark-driver-memory','spark-driver-memory','Input',0,NULL,'2',0,NULL,0,1,1,0,'startup');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (8,'spark.executor.memory','鎵ц鍣ㄥ唴瀛樺ぇ灏忥紝榛樿鍊硷細3','Executor memory, default value: 3','spark-executor-memory','spark-executor-memory','Input',0,NULL,'3',0,NULL,0,1,1,0,'startup');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (9,'spark.executor.cores','鎵ц鍣ㄦ牳蹇冧釜鏁帮紝榛樿鍊硷細1','Number of cores per executor, default value: 1','spark-executor-cores','spark-executor-cores','Input',0,NULL,'2',0,NULL,0,1,1,0,'startup');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (10,'spark.executor.instances','鎵ц鍣ㄤ釜鏁帮紝榛樿鍊硷細2','Number of executors, default value: 2','spark-executor-instances','spark-executor-instances','Input',0,NULL,'2',0,NULL,0,1,1,0,'startup');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (11,'wds.linkis.rm.yarnqueue','鎵ц闃熷垪','Execution queue','wds-linkis-yarnqueue','wds-linkis-yarnqueue','Input',0,NULL,'dws',0,NULL,0,1,1,0,'startup');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (12,'resources',NULL,NULL,'璧勬簮淇℃伅','Resource information','Upload',0,'[]',NULL,0,NULL,0,1,1,0,'node');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (13,'category','璇烽€夋嫨绫诲瀷','Please choose the type','绫诲瀷','Type','Select',1,'[\"node\"]','node',0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (14,'subject','璇峰～鍐欓偖浠舵爣棰?,'Please enter the email subject','閭欢鏍囬','Email Subject','Input',1,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (15,'content','璇烽€夋嫨鎴栬緭鍏ュ彂閫侀」','Please choose or enter the items to send','鍙戦€侀」','Intems to Send','MultiBinding',1,'[\"linkis.appconn.visualis.display\",\"linkis.appconn.visualis.dashboard\"]','[]',0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (16,'to','璇峰～鍐欐敹浠朵汉','Please enter recipients','鏀朵欢浜?,'To','Input',1,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (17,'cc','璇峰～鍐欐妱閫佷汉','Please enter carbon copy recipients','鎶勯€?,'Cc','Input',0,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (18,'bcc','璇峰～鍐欑瀵嗗彂閫佷汉','Please enter blind carbon copy recipients','绉樺瘑鎶勯€?,'Bcc','Input',0,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (19,'itsm','璇峰～鍐欏叧鑱斿鎵瑰崟','Please enter ITSM','鍏宠仈瀹℃壒鍗?,'ITSM','Input',1,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (20,'msg.type','璇锋纭～鍐欐秷鎭被鍨?,'Please enter message type correctly','msg.type','msg.type','Disable',1,NULL,'SEND',0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (21,'msg.topic','娑堟伅涓婚锛屽繀椤讳笌eventreceiver瀹屽叏涓€鑷?,'Message subject must be exactly the same as eventreceiver','msg.topic','msg.topic','Input',1,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (22,'msg.sender','璇锋纭～鍐欏彂閫佽€?,'Please enter the sender correctly','msg.sender','msg.sender','Input',1,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (23,'msg.name','娑堟伅鍚嶇О锛屽繀椤讳笌eventreceiver瀹屽叏涓€鑷?,'The message name must be exactly the same as the eventreceiver','msg.name','msg.name','Input',1,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (24,'msg.body','璇锋纭～鍐欐秷鎭唴瀹?,'Please enter the message content correctly','msg.body','msg.body','Text',0,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (25,'msg.type','璇锋纭～鍐欐秷鎭被鍨?,'Please enter message type correctly','msg.type','msg.type','Disable',1,NULL,'RECEIVE',0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (26,'msg.receiver','璇锋纭～鍐欐秷鎭帴鏀惰€?,'Please enter message recipients correctly','msg.receiver','msg.receiver','Input',1,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (27,'query.frequency','璇峰～鍐欐煡璇㈤鐜囷紝榛樿10娆?,'Please enter query frequency, 10 times by default','query.frequency','query.frequency','Disable',0,NULL,'10',0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (28,'max.receive.hours','璇峰～鍐欑瓑寰呮椂闂达紝榛樿1灏忔椂','Please enter waiting time, 1 hour by default','max.receive.hours','max.receive.hours','Input',0,NULL,'12',0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (29,'msg.savekey','娑堟伅鍏变韩key鍊硷紝榛樿msg.body','The ky of message content, msg.body by default','msg.savekey','msg.savekey','Input',0,NULL,'msg.body',0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (30,'only.receive.today',NULL,NULL,'only.receive.today','only.receive.today','Input',0,NULL,'true',0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (31,'source.type','璇烽€夋嫨鏁版嵁鏉ユ簮','Please choose the data source','source.type','source.type','Select',1,'[\"hivedb\",\"maskdb\"]',NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (32,'check.object','姣斿锛歞b.tb{ds=${run_date}}','Please enter the name of data dependency','check.object','check.object','Input',1,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (33,'max.check.hours',NULL,NULL,'max.check.hours','max.check.hours','Input',0,NULL,'1',0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (34,'job.desc','璇锋纭～鍐欏婧愰厤缃?,'Please enter multi-source configuration correctly','job.desc','job.desc','Text',0,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (35,'filter',NULL,NULL,'杩囨护鏉′欢','Filter','Input',0,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (37,'upStreams','璇烽€夋嫨涓婃父鑺傜偣','Please select upstream node','缁戝畾涓婃父鑺傜偣','Bind front node','Binding',1,'[\"*\"]','empty',0,NULL,0,3,0,1,'node');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (39,'msg.topic','娑堟伅涓婚锛屽繀椤讳笌eventsender瀹屽叏涓€鑷?,'Message subject must be exactly the same as eventsender','msg.topic','msg.topic','Input',1,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (40,'msg.name','娑堟伅鍚嶇О锛屽繀椤讳笌eventsender瀹屽叏涓€鑷?,'The message name must be exactly the same as the eventsender ','msg.name','msg.name','Input',1,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (41,'executeUser','璇峰～鍐欐墽琛岀敤鎴?,'Please enter execute user','鎵ц鐢ㄦ埛','executeUser','Input',1,NULL,NULL,0,NULL,0,1,1,0,'runtime');
insert  into `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (42,'Filter','璇峰～鍐欒繃婊ゆ潯浠?,'Please enter filter','杩囨护鏉′欢','Filter','Input',1,NULL,NULL,0,NULL,0,1,1,0,'runtime');
INSERT  INTO `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (45,'ReuseEngine','璇烽€夋嫨鏄惁澶嶇敤寮曟搸','Please choose to reuse engin or not','鏄惁澶嶇敤寮曟搸','reuse-engine-or-not','Select',1,'[\"true\",\"false\"]','true',0,NULL,0,1,1,0,'startup');
INSERT  INTO `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (46,'wds.linkis.engineconn.java.driver.memory','璇峰～鍐欏紩鎿庡唴瀛?,'please input driver memory','wds.linkis.engineconn.java.driver.memory','wds-linkis-engineconn.java.driver.memory','Input',0,NULL,'1G',0,NULL,0,1,1,0,'runtime');
INSERT  INTO `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (47,'branch.output.mapping','璇峰～鍐欒緭鍑哄彉閲忔槧灏勶紝鏍煎紡濡?amount=total_amount锛屽涓槧灏勮鐢ㄨ嫳鏂囬€楀彿鍒嗛殧','Please enter output variable mappings such as amount=total_amount, and separate multiple mappings with commas','杈撳嚭鍙橀噺鏄犲皠','Branch output mapping','Text',0,NULL,NULL,0,NULL,0,2,1,0,'special');
INSERT  INTO `dss_workflow_node_ui`(`id`,`key`,`description`,`description_en`,`lable_name`,`lable_name_en`,`ui_type`,`required`,`value`,`default_value`,`is_hidden`,`condition`,`is_advanced`,`order`,`node_menu_type`,`is_base_info`,`position`) values (48,'branch.rules','璇峰～鍐欏垎鏀鍒欙紝姣忚涓€鏉★紝鏍煎紡濡?amount>100=鑺傜偣A','Please enter one branch rule per line, such as amount>100=NodeA','鍒嗘敮瑙勫垯','Branch rules','Text',1,NULL,NULL,0,NULL,0,2,1,0,'special');

DELETE FROM dss_workflow_node_to_ui;
select @workflow_node_sql:=id from dss_workflow_node where name='sql';
select @workflow_node_python:=id from dss_workflow_node where name='python';
select @workflow_node_pyspark:=id from dss_workflow_node where name='pyspark';
select @workflow_node_scala:=id from dss_workflow_node where name='scala';
select @workflow_node_hql:=id from dss_workflow_node where name='hql';
select @workflow_node_shell:=id from dss_workflow_node where name='shell';
select @workflow_node_jdbc:=id from dss_workflow_node where name='jdbc';
select @workflow_node_connector:=id from dss_workflow_node where name='connector';

select @workflow_node_branch:=id from dss_workflow_node where name='branch';

select @node_ui_title:=id from dss_workflow_node_ui where `key`='title' limit 1;
select @node_ui_desc:=id from dss_workflow_node_ui where `key`='desc' limit 1;
select @node_ui_businessTag:=id from dss_workflow_node_ui where `key`='businessTag';
select @node_ui_appTag:=id from dss_workflow_node_ui where `key`='appTag';
select @node_ui_spark_driver_memory:=id from dss_workflow_node_ui where `key`='spark.driver.memory';
select @node_ui_spark_executor_memory:=id from dss_workflow_node_ui where `key`='spark.executor.memory';
select @node_ui_spark_executor_cores:=id from dss_workflow_node_ui where `key`='spark.executor.cores';
select @node_ui_spark_executor_instances:=id from dss_workflow_node_ui where `key`='spark.executor.instances';
select @node_ui_wds_linkis_rm_yarnqueue:=id from dss_workflow_node_ui where `key`='wds.linkis.rm.yarnqueue';
select @node_ui_resources:=id from dss_workflow_node_ui where `key`='resources';
select @node_ui_category:=id from dss_workflow_node_ui where `key`='category';
select @node_ui_subject:=id from dss_workflow_node_ui where `key`='subject';
select @node_ui_content:=id from dss_workflow_node_ui where `key`='content';
select @node_ui_to:=id from dss_workflow_node_ui where `key`='to';
select @node_ui_cc:=id from dss_workflow_node_ui where `key`='cc';
select @node_ui_bcc:=id from dss_workflow_node_ui where `key`='bcc';
select @node_ui_itsm:=id from dss_workflow_node_ui where `key`='itsm';
select @node_ui_msg_sender:=id from dss_workflow_node_ui where `key`='msg.sender';
select @node_ui_msg_body:=id from dss_workflow_node_ui where `key`='msg.body';
select @node_ui_msg_receiver:=id from dss_workflow_node_ui where `key`='msg.receiver';
select @node_ui_query_frequency:=id from dss_workflow_node_ui where `key`='query.frequency';
select @node_ui_max_receive_hours:=id from dss_workflow_node_ui where `key`='max.receive.hours';
select @node_ui_msg_savekey:=id from dss_workflow_node_ui where `key`='msg.savekey';
select @node_ui_only_receive_today:=id from dss_workflow_node_ui where `key`='only.receive.today';
select @node_ui_source_type:=id from dss_workflow_node_ui where `key`='source.type';
select @node_ui_check_object:=id from dss_workflow_node_ui where `key`='check.object';
select @node_ui_max_check_hours:=id from dss_workflow_node_ui where `key`='max.check.hours';
select @node_ui_job_desc:=id from dss_workflow_node_ui where `key`='job.desc';
select @node_ui_upStreams:=id from dss_workflow_node_ui where `key`='upStreams';
select @node_ui_executeUser:=id from dss_workflow_node_ui where `key`='executeUser';
select @node_ui_ReuseEngine:=id from dss_workflow_node_ui where `key`='ReuseEngine';
select @node_ui_DriverMemory:=id from dss_workflow_node_ui where `key`='wds.linkis.engineconn.java.driver.memory';
select @node_ui_branch_output_mapping:=id from dss_workflow_node_ui where `key`='branch.output.mapping';
select @node_ui_branch_rules:=id from dss_workflow_node_ui where `key`='branch.rules';

insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_sql,@node_ui_title);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_sql,@node_ui_desc);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_sql,@node_ui_businessTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_sql,@node_ui_appTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_sql,@node_ui_spark_driver_memory);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_sql,@node_ui_spark_executor_memory);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_sql,@node_ui_spark_executor_cores);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_sql,@node_ui_spark_executor_instances);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_sql,@node_ui_wds_linkis_rm_yarnqueue);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_sql,@node_ui_resources);
INSERT  INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@workflow_node_sql,@node_ui_ReuseEngine);
INSERT  INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@workflow_node_sql,@node_ui_branch_output_mapping);

insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_python,@node_ui_title);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_python,@node_ui_desc);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_python,@node_ui_businessTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_python,@node_ui_appTag);
INSERT  INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@workflow_node_python,@node_ui_ReuseEngine);

insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_pyspark,@node_ui_title);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_pyspark,@node_ui_desc);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_pyspark,@node_ui_businessTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_pyspark,@node_ui_appTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_pyspark,@node_ui_spark_driver_memory);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_pyspark,@node_ui_spark_executor_memory);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_pyspark,@node_ui_spark_executor_cores);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_pyspark,@node_ui_spark_executor_instances);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_pyspark,@node_ui_wds_linkis_rm_yarnqueue);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_pyspark,@node_ui_resources);
INSERT  INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@workflow_node_pyspark,@node_ui_ReuseEngine);
INSERT  INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@workflow_node_pyspark,@node_ui_branch_output_mapping);

insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_scala,@node_ui_title);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_scala,@node_ui_desc);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_scala,@node_ui_businessTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_scala,@node_ui_appTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_scala,@node_ui_spark_driver_memory);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_scala,@node_ui_spark_executor_memory);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_scala,@node_ui_spark_executor_cores);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_scala,@node_ui_spark_executor_instances);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_scala,@node_ui_wds_linkis_rm_yarnqueue);
INSERT  INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@workflow_node_scala,@node_ui_ReuseEngine);

insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_hql,@node_ui_title);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_hql,@node_ui_desc);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_hql,@node_ui_businessTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_hql,@node_ui_appTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_hql,@node_ui_wds_linkis_rm_yarnqueue);
INSERT  INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@workflow_node_hql,@node_ui_ReuseEngine);

insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_shell,@node_ui_title);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_shell,@node_ui_desc);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_shell,@node_ui_businessTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_shell,@node_ui_appTag);
INSERT  INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@workflow_node_shell,@node_ui_ReuseEngine);

insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_jdbc,@node_ui_title);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_jdbc,@node_ui_desc);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_jdbc,@node_ui_businessTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_jdbc,@node_ui_appTag);
INSERT  INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@workflow_node_jdbc,@node_ui_ReuseEngine);

insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_connector,@node_ui_title);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_connector,@node_ui_desc);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_connector,@node_ui_businessTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_connector,@node_ui_appTag);
INSERT  INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@workflow_node_connector,@node_ui_ReuseEngine);

insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_subFlow,@node_ui_title);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_subFlow,@node_ui_desc);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_subFlow,@node_ui_businessTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_subFlow,@node_ui_appTag);
INSERT  INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) VALUES (@workflow_node_subFlow,@node_ui_ReuseEngine);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_branch,@node_ui_title);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_branch,@node_ui_desc);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_branch,@node_ui_businessTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_branch,@node_ui_appTag);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_branch,@node_ui_branch_rules);
insert  into `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@workflow_node_hql,@node_ui_DriverMemory);

DELETE FROM dss_workflow_node_ui_validate;
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('7','NumInterval','[1,15]','椹卞姩鍣ㄥ唴瀛樺ぇ灏忥紝榛樿鍊硷細2','Drive memory size, default value: 2','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('8','NumInterval','[3,15]','鎵ц鍣ㄥ唴瀛樺ぇ灏忥紝榛樿鍊硷細3','Actuator memory size, default value: 3','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('9','NumInterval','[1,10]','鎵ц鍣ㄦ牳蹇冧釜鏁帮紝榛樿鍊硷細1','Number of cores per executor, default value : 1','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('10','NumInterval','[1,40]','鎵ц鍣ㄤ釜鏁帮紝榛樿鍊硷細2','Number of per executor, default value : 2','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('13','OFT','[\"node\"]','璇烽€夋嫨绫诲瀷','Please select type','change');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('25','OFT','[\"RECEIVE\"]','','Please select ','change');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('27','NumInterval','[1,1000]','璇峰～鍐欐煡璇㈤鐜囷紝榛樿10娆★紝鑼冨洿锛?-1000','Please fill in the inquiry frequency, default : 10, range is 1 to 1000','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('28','NumInterval','[1,1000]','璇峰～鍐欑瓑寰呮椂闂达紝榛樿1灏忔椂锛岃寖鍥达細1-1000','Please enter waiting time, 1 hour by default, range is 1 to 1000','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('31','OFT','[\"hivedb\",\"maskdb\"]','','Invalid format,example:ProjectName@WFName@jobName','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('32','Regex','^[^\\u4e00-\\u9fa5]+$','姝ゅ€间笉鑳借緭鍏ヤ腑鏂?,'Chinese characters are not allowed','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('40','Regex','^[a-zA-Z]([^.]*\\.[^.]*){1,}$','闇€瑕佹鏌ョ殑鏁版嵁婧恉bname.tablename{partition}','Checked data source dbname.tablename{partition}','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('41','Regex','^[\\S\\n\\s]{0,500}$','闀垮害鍦?鍒?00涓瓧绗?,'The length is between 1 and 500 characters','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('44','Regex','^[a-zA-Z][a-zA-Z0-9_@-]*$','蹇呴』浠ュ瓧姣嶅紑澶达紝涓斿彧鏀寔瀛楁瘝銆佹暟瀛椼€佷笅鍒掔嚎銆丂銆佷腑妯嚎','Started with alphabetic characters, only alphanumeric characters, underscore(_), @ and hyphen(-) are allowed','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('45','Regex','^[a-zA-Z0_9-]([^@]*@[^@]*){2}[a-zA-Z\\d]$','姝ゅ€兼牸寮忛敊璇紝渚嬪锛歅rojectName@WFName@jobName','Invalid format,example:ProjectName@WFName@jobName','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('46','Regex','^[a-zA-Z0_9-]([^_]*_[^_]*){2}[a-zA-Z\\d]$','姝ゅ€兼牸寮忛敊璇紝渚嬪锛歜dp_tac_name','Invalid format,example:bdp_tac_name','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('47','Regex','^.{1,128}$','闀垮害鍦?鍒?28涓瓧绗?,'The length is between 1 and 128 characters','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('48','Regex','^[a-zA-Z][a-zA-Z0-9_-]*$','蹇呴』浠ュ瓧姣嶅紑澶达紝涓斿彧鏀寔瀛楁瘝銆佹暟瀛椼€佷笅鍒掔嚎锛?,'Started with alphabetic characters, only alphanumeric and underscore are allowed!','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('49','Regex','^[a-zA-Z0-9_\\u4e00-\\u9fa5]*$','鍙敮鎸佷腑鏂囥€佸瓧姣嶃€佹暟瀛楀拰涓嬪垝绾匡紒','Only Chinese characters, alphanumeric characters and underscore are allowed in subject!','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('50','Regex','^[a-z][a-zA-Z0-9_.@;]*$','蹇呴』浠ュ瓧姣嶅紑澶达紝涓斿彧鏀寔瀛楁瘝銆佹暟瀛椼€佷笅鍒掔嚎銆丂銆佺偣','Must start with a letter and only letters, numbers, underscores, @, points are supported','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('51','Regex','^[0-9_.]*$','鍙敮鎸佹暟瀛椼€佷笅鍒掔嚎銆佺偣','Only numbers, underscores and dots are supported','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('52','None',NULL,NULL,NULL,'blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('53','OFT','[\"SEND\"]',NULL,NULL,'change');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('54','NumInterval','[1,1000]','璇峰～鍐欑瓑寰呮椂闂达紝榛樿1灏忔椂锛岃寖鍥达細1-1000','Please fill in the waiting time, default 1 hour, range: 1-1000','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('55','Required',NULL,'璇ュ€间笉鑳戒负绌?,'The value cannot be empty\n\n','change');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('56','Function','validatorTitle','鑺傜偣鍚嶄笉鑳藉拰宸ヤ綔娴佸悕涓€鏍?,'The node name cannot be the same as the workflow name',NULL);
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('57','Regex','^[a-zA-Z][a-zA-Z0-9_.-]*$','蹇呴』浠ュ瓧姣嶅紑澶达紝涓斿彧鏀寔瀛楁瘝銆佹暟瀛椼€佷笅鍒掔嚎銆佺偣锛?,'It must start with a letter and only supports letters, numbers, underscores and dots!','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('58','Regex','(.+)@(.+)@(.+)','姝ゆ牸寮忛敊璇紝渚嬪锛歅rojectName@WFName@jobName','Invalid format,example:ProjectName@WFName@jobName','blur');
INSERT INTO `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('59','OFT','["true","false"]','璇峰～鍐欐槸鍚﹀鐢ㄥ紩鎿庯紝false锛氫笉澶嶇敤锛宼rue锛氬鐢?,'Please fill in whether or not to reuse engine, true: reuse, false: not reuse','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('60', 'Regex', '^[0-9.]*g{0,1}$', 'Spark鍐呭瓨璁剧疆濡?g', 'Drive memory size, default value: 2', 'blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('61','Regex','^(.|\s){1,5000}$','闀垮害鍦?鍒?000涓瓧绗?,'The length is between 1 and 5000 characters','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('62','Regex','^.{1,150}$','闀垮害鍦?鍒?50涓瓧绗?,'The length is between 1 and 150 characters','blur');
insert into `dss_workflow_node_ui_validate` (`id`, `validate_type`, `validate_range`, `error_msg`, `error_msg_en`, `trigger`) values('63', 'Regex', '^([1-9]|10|[1-9])(g|G){0,1}$', '璁剧疆鑼冨洿涓篬1,10],璁剧疆瓒呭嚭闄愬埗', 'hive memory limit 1,10', 'blur');

DELETE FROM dss_workflow_node_ui_to_validate;
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_source_type,31);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_source_type,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_check_object,32);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_check_object,40);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_check_object,41);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_check_object,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_max_receive_hours,28);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_query_frequency,27);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_desc,32);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_desc,41);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_msg_sender,44);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_msg_sender,45);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_msg_sender,41);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (25,25);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (25,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_msg_receiver,32);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_msg_receiver,41);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_msg_receiver,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_msg_receiver,58);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (21,32);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (21,41);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (21,46);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (21,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_msg_savekey,32);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_msg_savekey,41);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (23,47);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (23,48);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (23,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_msg_body,32);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_msg_body,41);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_spark_driver_memory,7);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_spark_executor_memory,8);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_spark_executor_cores,9);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_wds_linkis_rm_yarnqueue,41);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_wds_linkis_rm_yarnqueue,57);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_title,48);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_title,62);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_title,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_title,56);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_category,13);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_subject,47);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_to,50);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_cc,50);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_bcc,50);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_itsm,51);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_itsm,47);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_businessTag,52);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_appTag,52);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_resources,52);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (20,53);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (20,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_only_receive_today,52);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_max_check_hours,54);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_job_desc,32);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (35,52);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_msg_sender,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_category,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_to,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_itsm,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_subject,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_upStreams,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (39,32);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (39,41);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (39,46);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (39,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (40,47);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (40,48);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (40,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_content,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_msg_sender,58);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_executeUser,55);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (42,52);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_ReuseEngine,59);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_spark_driver_memory,60);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_spark_executor_memory,60);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_job_desc,61);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_DriverMemory,63);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_branch_output_mapping,61);
insert  into `dss_workflow_node_ui_to_validate`(`ui_id`,`validate_id`) values (@node_ui_branch_rules,61);


DELETE FROM dss_workspace_appconn_role;
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('-1',@scriptis_appconn_id,'1','1',now(),'system');
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('-1',@scriptis_appconn_id,'2','1',now(),'system');
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('-1',@scriptis_appconn_id,'3','1',now(),'system');
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('-1',@scriptis_appconn_id,'4','1',now(),'system');
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('-1',@scriptis_appconn_id,'5','1',now(),'system');
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('-1',@scriptis_appconn_id,'6','1',now(),'system');
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('-1',@scriptis_appconn_id,'7','1',now(),'system');

INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('-1',@workflow_appconn_id,'1','1',now(),'system');
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('-1',@workflow_appconn_id,'2','1',now(),'system');
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('-1',@workflow_appconn_id,'3','1',now(),'system');
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('-1',@workflow_appconn_id,'4','1',now(),'system');
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('-1',@workflow_appconn_id,'5','1',now(),'system');
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('-1',@workflow_appconn_id,'6','1',now(),'system');
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('-1',@workflow_appconn_id,'7','1',now(),'system');

INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('224',@scriptis_appconn_id,'1','1',now(),'system');
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('224',@workflow_appconn_id,'1','1',now(),'system');
INSERT INTO `dss_workspace_appconn_role` (`workspace_id`, `appconn_id`, `role_id`, `priv`, `update_time`, `updateby`) VALUES('224',@apiservice_appconn_id,'1','1',now(),'system');


INSERT INTO `dss_workspace_admin_dept` (`id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES('100','0','0','鍩虹绉戞妧','0','leader01','1888888888','123@qq.com','0','0','admin',now(),'admin',now());






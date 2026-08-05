# language: zh-CN
功能: DataGo飞书多维表格外发AppConn节点

  背景:
    Given 用户已登录DSS系统
    And 用户已进入工作流编辑页面
    And datagofeishu AppConn已部署，节点类型为 linkis.appconn.datagofeishu，节点显示名为 sendfeishu
    And DataGo数据外发服务可达，默认端口3003
    And 实例配置 wds.dss.appconn.datago.feishu.api.base.url 已指向DataGo服务地址

  @NodeCreation @P0
  场景: 拖拽sendfeishu节点到工作流并显示属性面板
    Given 工作流节点分组"数据输出"下存在"DataGo飞书多维表格外发"节点
    When 用户将"DataGo飞书多维表格外发"节点拖拽到工作流画布
    Then 节点在画布上创建成功，节点类型为 linkis.appconn.datagofeishu，节点显示名为 sendfeishu
    And 节点参数面板展示 DM单号、飞书通知人、外发目标三个专属属性
    And DM单号、飞书通知人、外发目标标记为必填
    And 节点参数面板不展示最大等待时间、检测轮询间隔、外发重试次数、外发重试间隔属性

  @NodeCreation @P0
  场景: 填写完整节点参数并保存成功
    Given 工作流画布上存在一个sendfeishu节点
    When 用户在DM单号输入框填写 "DM202607150001"
    And 用户在飞书通知人输入框填写 "alexyang;ryanchen"
    And 用户在外发目标文本域填写 "dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14}"
    And 用户点击保存按钮
    Then 节点参数保存成功
    And 节点属性 dmId 持久化为 "DM202607150001"
    And 节点属性 notifyUsers 持久化为 "alexyang;ryanchen"
    And 节点属性 dataTargets 持久化为 "dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14}"

  @NodeCreation @P0
  场景: DM单号必填校验
    Given 工作流画布上存在一个sendfeishu节点
    When 用户清空DM单号输入框
    And 用户填写合法的飞书通知人与外发目标
    And 用户点击保存按钮
    Then 系统提示"DM单号为必填项"
    And 保存被拒绝，节点参数面板保持打开

  @NodeCreation @P0
  场景: 飞书通知人必填校验
    Given 工作流画布上存在一个sendfeishu节点
    When 用户填写合法的DM单号与外发目标
    And 用户清空飞书通知人输入框
    And 用户点击保存按钮
    Then 系统提示"飞书通知人为必填项"
    And 保存被拒绝

  @NodeCreation @P0
  场景: 外发目标必填校验
    Given 工作流画布上存在一个sendfeishu节点
    When 用户填写合法的DM单号与飞书通知人
    And 用户清空外发目标文本域且未填写旧单值字段
    And 用户点击保存按钮
    Then 系统提示"外发目标为必填项"
    And 保存被拒绝

  @NodeCreation @P1
  场景: 实例级时间参数非正整数校验
    Given 实例级配置 wds.dss.appconn.datago.feishu.max.wait.time.ms 被设为非法值 "0"
    And 工作流画布上存在一个sendfeishu节点
    When 用户填写合法的DM单号、飞书通知人、外发目标
    And 用户提交工作流执行
    Then 节点执行失败，异常码为 82001
    And 错误消息含"参数 ... 必须为正整数"
    And 不调用任何DataGo接口

  @NodeCreation @P2
  场景: maxWaitTime不暴露节点UI仅实例级生效
    Given 实例级配置 wds.dss.appconn.datago.feishu.max.wait.time.ms 为 600（秒）
    And 工作流画布上存在一个sendfeishu节点
    When 用户打开节点参数面板检查专属属性
    And 用户提交工作流执行
    And DataGo检测任务长时间不返回终态
    Then 节点参数面板仅展示 dmId/notifyUsers/dataTargets 三个专属属性，无最大等待时间输入框
    And dss_workflow_node_ui 中 sendfeishu 绑定的 key 不含 maxWaitTime
    And 节点使用实例级 maxWaitTime=600秒
    And 约600秒后节点超时失败，异常码为 82006
    And 日志记录 maxWaitTime=600000ms

  @DataTargets @P1
  场景: 多级分区花括号保护等号与斜杠
    Given DataGo中存在DM单，其表 db_metrics.daily_kpi 审批分区为 "dt=20260716/country=us"
    When 用户配置外发目标为 "dt.01=db=db_metrics|table=daily_kpi|fields=kpi_code,kpi_val|partition={dt=20260716/country=us}"
    And 用户提交工作流执行
    Then 节点解析分区时去除花括号，保留内部 "dt=20260716/country=us"
    And 花括号内的等号与斜杠不被当作分隔符
    And 调用 POST /api/export/task 请求体中 tables[0].partitions 为 ["dt=20260716/country=us"]（每表分区列表，外层无 partitions）
    And DM审批比对通过，节点进入检测阶段

  @DataTargets @P1
  场景: 空分区与省略分区段等价
    Given DataGo中存在DM单，其表 db_audit.t_dim_code 为非分区表
    When 用户配置外发目标为 "dt.01=db=db_audit|table=t_dim_code|fields=code_id,code_name|partition="
    And 用户提交工作流执行
    Then 节点将 partition 解析为 null（非分区表）
    And 调用 POST /api/export/task 请求体中 tables[0] 不含 partitions 字段（非分区表，外层无 partitions）
    And 等价于省略 partition 段的写法 "dt.01=db=db_audit|table=t_dim_code|fields=code_id,code_name"
    And DM审批比对通过

  @DataTargets @P1
  场景: 行间分号与换行混合分隔
    Given DataGo中存在DM单，包含 db_audit.t_event_log 与 db_audit.t_dim_code 两张表
    When 用户在外发目标文本域填写使用分号、换行、空行混合分隔的多行配置
    And 用户提交工作流执行
    Then 节点解析得到2个DataTarget
    And 空行与首尾空白被忽略
    And 调用 POST /api/export/task 请求体中 tables 数组含2个元素，每表内嵌各自 fields（外层无 fields）

  @DataTargets @P1
  场景: 外发目标编号格式错误
    Given 工作流画布上存在一个sendfeishu节点
    When 用户配置外发目标为 "dt.aa=db=db_audit|table=t_event_log|fields=user_id,event,ts"
    And 用户提交工作流执行
    Then 节点执行失败，异常码为 82001
    And 错误消息含"外发目标编号格式错误，需为 dt.序号"
    And 编号必须匹配正则 dt\.\d+

  @DataTargets @P1
  场景: 缺少db/table/fields必填段
    Given 工作流画布上存在一个sendfeishu节点
    When 用户配置外发目标为 "dt.01=table=t_event_log|fields=user_id,event,ts"
    And 用户提交工作流执行
    Then 节点执行失败，异常码为 82001
    And 错误消息含"缺少 db/table/fields 必填段"

  @DataTargets @P1
  场景: 外发目标存在未知字段段
    Given 工作流画布上存在一个sendfeishu节点
    When 用户配置外发目标为 "dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|foo=bar"
    And 用户提交工作流执行
    Then 节点执行失败，异常码为 82001
    And 错误消息含"存在未知字段: foo"
    And 仅允许 db/table/fields/partition 四种段key

  @DataTargets @P1
  场景: 同库同表同分区重复外发目标
    Given 工作流画布上存在一个sendfeishu节点
    When 用户配置外发目标为 "dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14};dt.02=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14}"
    And 用户提交工作流执行
    Then 节点执行失败，异常码为 82001
    And 错误消息含"外发目标 dt.02 db_audit.t_event_log#ds=2026-07-14 重复，请检查"
    And 同一 (db, table, partition) 不允许重复，去重依据为 DataTarget.tableKey()=db.table#partition

  @DataTargets @P1
  场景: 同库同表不同分区允许共存
    Given DataGo中DM单 DM202607150001 存在，表 db_audit.t_event_log 审批列为 user_id,event,ts，审批分区覆盖 ds=2026-07-14 与 ds=2026-07-15
    And 工作流画布上存在一个sendfeishu节点
    When 用户配置外发目标为 "dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14};dt.02=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-15}"
    And 用户提交工作流执行
    Then 节点DSL解析通过，不报82001重复错误
    And 节点解析出2个外发目标，tableKey分别为 db_audit.t_event_log#ds=2026-07-14 与 db_audit.t_event_log#ds=2026-07-15
    And 调用 POST /api/export/task 请求体 tables[] 含2个元素，各自内嵌对应 partition，顶层 partitions 含 ds=2026-07-14 与 ds=2026-07-15

  @DMValidate @P0
  场景: 字段超出DM单审批范围
    Given DataGo中DM单 DM202607150001 存在，表 db_audit.t_event_log 审批列为 user_id,event,ts
    When 用户配置 dmId="DM202607150001" 且 dataTargets 字段为 "user_id,event,ts,phone"
    And 用户提交工作流执行
    Then 节点调用 POST /api/export/form 获取DM单信息成功
    Then 节点执行失败，异常码为 82003
    And 错误消息含"第1个外发目标 字段超出DM单审批范围，越权字段: [phone]"
    And 错误消息列出审批字段与节点字段
    And 不调用 POST /api/export/task 与 POST /api/export/execute 接口

  @DMValidate @P0
  场景: DM单optype为datago拒绝外发
    Given DataGo中DM单 DM202607150004 存在，optype 为 datago（报告外发）
    When 用户配置 dmId="DM202607150004" 并提交工作流执行
    Then 节点调用 POST /api/export/form 返回 optype=datago
    Then 节点执行失败，异常码为 82003
    And 错误消息含"DM单optype=datago非table，请走现有报告外发接口"
    And 不调用任务接口与外发接口

  @DMValidate @P0
  场景: 飞书通知人越权
    Given DataGo中DM单 DM202607150001 的检测用户为 alexyang,ryanchen
    And 表单接口①返回的 form.notifyUsers 含审批允许通知人集合 [alexyang, ryanchen]
    When 用户配置 notifyUsers="alexyang;unauthorized_user" 并提交工作流执行
    Then 节点调用 POST /api/export/form 获取DM单信息成功
    Then 节点执行失败，异常码为 82003
    And 错误消息含"飞书通知人超出DM单授权范围，越权用户: [unauthorized_user]"
    And 不调用任务接口与外发接口

  @DMValidate @P0
  场景: 外发目标库表不在DM单审批范围
    Given DataGo中DM单 DM202607150001 仅含 db_audit.t_event_log 与 db_audit.t_dim_code
    When 用户配置 dataTargets 含 "dt.01=db=db_audit|table=t_not_exist|fields=user_id,event,ts"
    And 用户提交工作流执行
    Then 节点执行失败，异常码为 82003
    And 错误消息含"第1个外发目标库表 db_audit.t_not_exist 不在DM单审批范围内"
    And 不调用任务接口与外发接口

  @DMValidate @P2
  场景: 客户端不校验分区范围分区不一致放行至检测阶段
    Given DataGo中DM单 DM202607150001 的 t_event_log 审批分区为 "ds=2026-07-14"
    When 用户配置 dataTargets 含 "dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-15}"
    And 用户提交工作流执行
    Then 节点调用 POST /api/export/form 获取DM单信息成功
    And 节点不在DM比对阶段因分区不一致失败，不抛 82003
    And 节点通过DM比对进入DETECTING阶段，调用 POST /api/export/task
    And 后续是否成功取决于②③对实际分区的处理

  @DMValidate @P1
  场景: DM单状态为检测不通过
    Given DataGo中DM单 DM202607150005 状态为 detected_fail
    When 用户配置 dmId="DM202607150005" 并提交工作流执行
    Then 节点调用 POST /api/export/form 返回 status=detected_fail
    Then 节点执行失败，异常码为 82005
    And 错误消息含"DM单检测不通过（命中敏感），DataGo已飞书通知"
    And 不调用任务接口与外发接口

  @DMValidate @P1
  场景: DM单状态为检测异常
    Given DataGo中DM单 DM202607150006 状态为 detect_error
    When 用户配置 dmId="DM202607150006" 并提交工作流执行
    Then 节点调用 POST /api/export/form 返回 status=detect_error
    Then 节点执行失败，异常码为 82006
    And 错误消息含"DM单检测异常，DataGo已飞书通知"

  @DMValidate @P1
  场景: DM单状态为已外发
    Given DataGo中DM单 DM202607150007 状态为 exported
    When 用户配置 dmId="DM202607150007" 并提交工作流执行
    Then 节点执行失败，异常码为 82002
    And 错误消息含"DM单已外发，不可重复外发"

  @DMValidate @P1
  场景: DM单不存在
    Given DataGo中不存在DM单 DM202607150099
    When 用户配置 dmId="DM202607150099" 并提交工作流执行
    Then 节点调用 POST /api/export/form 返回 data 为空
    Then 节点执行失败，异常码为 82002
    And 错误消息含"DM单不存在或无审批信息"
    And 不调用任务接口与外发接口

  @DMValidate @P1
  场景: 字段顺序与空白差异归一通过
    Given DataGo中DM单 DM202607150001 的 t_event_log 审批列为 user_id,event,ts
    When 用户配置 dataTargets 字段为 " ts ,event , user_id "（顺序打乱、含空白）
    And 用户提交工作流执行
    Then 节点对字段做 trim、去空、去重、排序归一后为 event,ts,user_id
    And DM单审批字段归一后同为 event,ts,user_id
    And 比对通过，节点进入检测阶段

  @DMValidate @P1
  场景: 飞书通知人顺序空白去重归一通过
    Given DataGo中DM单 DM202607150001 的检测用户为 alexyang,ryanchen
    And 表单接口①返回的 form.notifyUsers 为 [alexyang, ryanchen]
    When 用户配置 notifyUsers=" ryanchen ; alexyang ; alexyang ;  "（含空白、重复、顺序打乱）
    And 用户提交工作流执行
    Then 节点对 notifyUsers 做 trim、去空、去重、排序归一后为 alexyang,ryanchen
    And 是DM单检测用户子集，比对通过
    And 节点进入检测阶段

  @Detect @P0
  场景: 检测不通过detected_fail
    Given DataGo中DM单 DM202607150001 合法，审批比对通过
    And 任务接口首次创建返回 taskId=1024
    And 后续轮询返回 status=detected_fail，resultSummary="命中敏感表 t_sensitive"
    And DataGo已主动飞书通知
    When 用户配置合法节点参数并提交工作流执行
    Then 节点进入DETECTING阶段并获取taskId
    Then 节点执行失败，异常码为 82005
    And 错误消息含"DataGo检测不通过: 命中敏感表 t_sensitive"
    And DSS不调用 POST /api/export/execute 接口
    And DSS不重复发送飞书通知

  @Detect @P0
  场景: 检测异常detect_error
    Given DataGo中DM单合法，审批比对通过
    And 任务接口首次创建返回 taskId
    And 后续轮询返回 status=detect_error（DataGo已自动重试2次仍失败）
    When 用户配置合法节点参数并提交工作流执行
    Then 节点执行失败，异常码为 82006
    And 错误消息含"DataGo检测异常"
    And DSS不调用外发接口
    And DataGo已飞书通知，DSS不重复通知

  @Detect @P0
  场景: 检测轮询超时
    Given DataGo中DM单合法，审批比对通过
    And 任务接口首次创建返回 taskId
    And 后续轮询始终返回 inited 或 detecting（非终态）
    And 实例级配置 wds.dss.appconn.datago.feishu.max.wait.time.ms 为 600（秒）
    When 用户配置合法节点参数并提交工作流执行
    Then 节点在DETECTING阶段持续轮询，状态保持Running
    And 超过600秒（600000ms）后节点失败，异常码为 82006
    And 错误消息含"任务超过最大等待时间"
    And 日志记录超时时 stage=DETECTING

  @Detect @P1
  场景: 检测任务首次创建未返回taskId
    Given DataGo中DM单合法，审批比对通过
    And 任务接口首次创建（taskId=null）返回200，但 data.taskId 为 null
    When 用户配置合法节点参数并提交工作流执行
    Then 节点执行失败，异常码为 82004
    And 错误消息含"DataGo首次检测未返回taskId"
    And 不再进行后续轮询

  @Detect @P1
  场景: 检测任务接口调用失败
    Given DataGo中DM单合法，审批比对通过
    And 任务接口 POST /api/export/task 返回500（DataGo内部错误）
    When 用户配置合法节点参数并提交工作流执行
    Then 节点执行失败，异常码为 82004
    And 错误消息含"DataGo任务接口调用失败"

  @Detect @P1
  场景: 检测任务返回未知状态
    Given DataGo中DM单合法，审批比对通过
    And 任务接口返回 status="unknown_status"（非枚举值）
    When 用户配置合法节点参数并提交工作流执行
    Then 节点执行失败，异常码为 82004
    And 错误消息含"DataGo检测接口返回未知状态: unknown_status"

  @Detect @P1
  场景: 检测任务返回已外发状态直接成功
    Given DataGo中DM单合法，审批比对通过
    And 任务接口首次创建返回 taskId，status=exported（任务已被外发）
    When 用户配置合法节点参数并提交工作流执行
    Then 节点 stage 直接置为 SUCCESS，状态为 Success
    And 不调用 POST /api/export/execute 接口

  @Execution @P0
  场景: 正常外发流程多目标端到端
    Given DataGo中DM单 DM202607150001 存在，optype=table，包含表 db_audit.t_event_log 与 db_audit.t_dim_code
    And DM单检测用户为 alexyang,ryanchen
    And 任务接口对本次创建返回 inited → detecting → detected_pass
    And 执行外发接口返回200，sheets全部success
    When 用户配置 dmId="DM202607150001"
    And 用户配置 notifyUsers="alexyang;ryanchen"
    And 用户配置 dataTargets="dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14};dt.02=db=db_audit|table=t_dim_code|fields=code_id,code_name"
    And 用户提交工作流执行
    Then 节点依次完成审批校验、检测通过、外发成功
    And 节点状态为Success
    And 调用 POST /api/export/task 请求体 tables[] 每表内嵌 fields/partitions(非空才传)/notifyUsers（外层无 fields/partitions）
    And 调用 POST /api/export/execute 请求体中 taskIds 为 [本节点检测taskId]
    And 外发结果记录 bitableUrl 与各sheet表名/行数/状态
    And 飞书通知人 alexyang 与 ryanchen 收到含多维表格URL的飞书消息

  @Execution @P0
  场景: 单目标外发成功记录bitableUrl与sheet行数
    Given DataGo中DM单 DM202607150002 存在，optype=table，包含单表 db_audit.t_dim_code
    And DM单检测用户为 alexyang
    And 任务接口返回 detected_pass
    And 执行外发接口返回200，sheets全部success，bitableName="DM202607150002--1--{时间戳}"
    When 用户配置 dmId="DM202607150002"
    And 用户配置 notifyUsers="alexyang"
    And 用户配置 dataTargets="dt.01=db=db_audit|table=t_dim_code|fields=code_id,code_name"
    And 用户提交工作流执行
    Then 节点状态为Success
    And 外发响应 data.bitableUrl 非空，日志中可查
    And data.sheets 仅1个元素，tableName=t_dim_code，rows=N，status=success
    And data.notifyUsers 为 ["alexyang"]
    And data.exportedAt 为 ISO 8601 时间格式
    And 审计字段含 bitableUrl、sheetsSummary、exportStatus=success

  @Execution @P0
  场景: 外发部分失败重试耗尽
    Given DataGo中DM单合法，检测通过，taskId=1024
    And 实例级配置 wds.dss.appconn.datago.feishu.execute.retry.max 为3
    And 执行外发接口持续返回200，但 sheets 中 t_event_log.status=failed
    When 用户配置含 t_event_log 的多目标dataTargets并提交工作流执行
    Then 节点首次外发200含failed，触发重试（executeRetryCount=1）
    And 第2、3次重试仍失败（executeRetryCount=2、3）
    Then 节点执行失败，异常码为 82008
    And 错误消息含"DataGo外发部分表失败且重试耗尽（已写入不回滚）"
    And 已写入的sheet不回滚，日志记录哪些sheet成功
    And 重试间隔为 executeRetryInterval（默认30秒）

  @Execution @P0
  场景: 飞书不可达502重试耗尽
    Given DataGo中DM单合法，检测通过，taskId=1024
    And 实例级配置 wds.dss.appconn.datago.feishu.execute.retry.max 为3
    And 执行外发接口持续返回502（飞书不可达）
    When 用户配置合法节点参数并提交工作流执行
    Then 节点首次外发502，触发重试（executeRetryCount=1）
    And 第2、3次重试仍返回502
    Then 节点执行失败，异常码为 82007
    And 错误消息含"DataGo外发重试次数耗尽"
    And 日志记录每次重试的 httpCode=502

  @Execution @P0
  场景: 外发超限413不可重试
    Given DataGo中DM单合法，检测通过，taskId=1024
    And 执行外发接口返回413，message="表 t_big_data 超出大小限制(估算 62MB, 上限 50MB)"
    When 用户配置dataTargets指向超大数据量表并提交工作流执行
    Then 节点立即失败（不重试），异常码为 82009
    And 错误消息含"超出大小限制"
    And executeRetryCount 保持为0

  @Execution @P1
  场景: 外发部分失败重试后成功
    Given DataGo中DM单合法，检测通过，taskId=1024
    And 实例级配置 wds.dss.appconn.datago.feishu.execute.retry.max 为3
    And 执行外发接口首次返回200，sheets含1个failed
    And 执行外发接口第2次重试返回200，sheets全部success
    When 用户配置合法节点参数并提交工作流执行
    Then 节点首次部分失败，触发重试（executeRetryCount=1）
    And 第2次重试全部success
    Then 节点状态为Success
    And 日志记录"外发部分表失败，第1次重试"与最终"数据已外发到飞书多维表格"
    And 已写入的sheet不重复写入

  @Execution @P1
  场景: 飞书不可达504重试耗尽
    Given DataGo中DM单合法，检测通过，taskId=1024
    And 实例级配置 wds.dss.appconn.datago.feishu.execute.retry.max 为3
    And 执行外发接口持续返回504（飞书不可达）
    When 用户配置合法节点参数并提交工作流执行
    Then 504与502行为一致，均触发重试
    And 重试耗尽后节点失败，异常码为 82007

  @Execution @P1
  场景: 飞书不可达重试后成功
    Given DataGo中DM单合法，检测通过，taskId=1024
    And 实例级配置 wds.dss.appconn.datago.feishu.execute.retry.max 为3
    And 执行外发接口首次返回502，第2次返回200全部success
    When 用户配置合法节点参数并提交工作流执行
    Then 节点首次502，触发重试（executeRetryCount=1）
    And 第2次重试200全部success
    Then 节点状态为Success
    And 日志记录"飞书服务不可达，第1次重试"与最终"数据已外发到飞书多维表格"

  @Execution @P1
  场景: 外发状态冲突409不可重试
    Given DataGo中DM单合法，检测通过，taskId=1024
    And 执行外发接口返回409，message="存在未检测通过的任务: [taskId=1025 status=detected_fail]"
    When 用户配置合法节点参数并提交工作流执行
    Then 节点立即失败（不重试），异常码为 82009
    And 错误消息含"存在未检测通过的任务"
    And executeRetryCount 保持为0

  @Execution @P1
  场景: 外发内部错误500直接失败
    Given DataGo中DM单合法，检测通过，taskId=1024
    And 执行外发接口返回500（DataGo内部错误）
    When 用户配置合法节点参数并提交工作流执行
    Then 节点失败，异常码为 82007
    And 500不触发重试（仅502/504可重试）
    And 错误消息含"DataGo执行外发失败"

  @Execution @P2
  场景: 外发参数错误400映射82001
    Given DataGo中DM单合法，检测通过
    And 执行外发接口返回400，message="optype 参数为必填项"（模拟DSS侧构造请求异常）
    When 用户配置合法节点参数并提交工作流执行
    Then 节点失败，异常码为 82001（400映射为82001）
    And 不重试

  @Compatibility @P1
  场景: 兼容旧单值字段自动包装为单目标
    Given DataGo中DM单 DM202607150001 合法
    And 工作流画布上存在一个旧版节点，未填 dataTargets
    And 旧节点填写了 dbName=db_audit、tableName=t_event_log、fields=user_id,event,ts、partition=ds=2026-07-14
    When 用户提交工作流执行
    Then 节点检测到 dataTargets 为空，走兼容回退逻辑
    And 节点自动将 dbName/tableName/fields/partition 包装为单元素DataTarget
    And 包装后等价于 "dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14}"
    And 后续流程与正常流程一致，节点成功
    And 旧节点升级后不破坏原有语义

  @Lifecycle @P1
  场景: 节点kill后状态置KILLED
    Given DataGo中DM单合法，审批比对通过
    And 任务接口长时间返回 detecting（非终态）
    And 节点处于DETECTING阶段Running
    When 用户在DSS工作流执行界面点击"杀死"节点
    Then kill 返回 true
    And 节点 stage 置为 Killed，状态置为 Killed
    And 节点不再继续轮询DataGo
    And 日志含"节点已终止，不再轮询DataGo"
    And result 返回 error（非Success）

  @Audit @P1
  场景: 日志审计字段完整性
    Given DataGo中DM单合法，端到端正常外发成功
    When 用户执行正常外发流程
    And 节点状态变为Success
    Then 节点执行日志含 dmId、optype、库名、表名、字段数量、分区、通知人数、taskId、当前阶段、DataGo返回状态
    And 外发完成后日志含多维表格名称、bitableUrl、各sheet表名/行数/状态、实际通知用户、外发完成时间
    And 审计字段包含 executionId、nodeId、user、dmId、optype、taskId、dataTargets、targetCount、notifyUsers、detectStatus、exportStatus、bitableUrl、sheetsSummary、errorMessage
    And 日志每行含时间戳前缀
    And 进度 progress 在 DETECTING 返回0.3，EXPORTING 返回0.8，SUCCESS 返回1.0

  @Audit @P1
  场景: 日志脱敏不含原始数据与凭据
    Given DataGo中DM单合法，正常外发流程执行
    When 用户收集节点执行日志、审计字段与DataGoFeishuClient及DataGoFeishuHttpUtils的INFO级日志
    Then 日志不含 DataGo 返回的原始数据内容（sheet行数据、字段值）
    And 日志不含飞书凭据（FS-* token、Authorization 头值）
    And 飞书通知人仅记录账号标识（如 alexyang），不记录飞书 user_open_id
    And 执行外发响应的sheets仅记录 tableName/rows/status 摘要，不记录行数据
    And HTTP请求日志不含 Authorization 头值，响应体日志截断2000字符

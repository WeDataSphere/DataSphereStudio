# DataGo飞书多维表格外发AppConn 设计文档

| 属性 | 值 |
|------|-----|
| 设计编号 | DES-DSS-1.22.4-003 |
| 关联需求 | REQ-DSS-1.22.4-003 |
| 版本 | dev-1.22.4 |
| 所属模块 | dss-datago-feishu-appconn |
| 参考实现 | dss-datachecker-appconn |
| 关联架构 | 数据外发飞书多维表格-架构设计.md |
| 对接契约 | [数据外发飞书多维表格_接口文档.md](./数据外发飞书多维表格_接口文档.md)（v2.0） |
| 落地代码 | `dss-appconn/appconns/dss-datago-feishu-appconn` |

> 本文依据模块 `dss-datago-feishu-appconn` 的实际代码与 `init.sql` 校准，描述已落地的实现。接口契约以 [接口文档 v2.0](./数据外发飞书多维表格_接口文档.md) 为准；本文与代码、`init.sql` 三者保持一致，凡有出入以代码与 `init.sql` 为准。

## 一、设计概述

本设计在DSS工作流中新增一个类似 `datachecker` 的 AppConn：`datagofeishu`，用于编排BDAP数据外发到飞书多维表格的受控流程。用户开发工作流节点时填写DM单号、飞书通知人、外发目标（多库多表多字段多分区）；节点执行时，DSS通过DataGo提供的通用外发接口获取DM单审批信息，与用户填写的节点参数进行一致性比对（字段校验为子集语义：节点字段须为DM单审批字段的子集），校验通过后才继续执行检测与外发。

节点执行链路分为三个阶段（对齐接口文档 ①②③ 三个接口）：

1. **审批信息校验（① 表单获取）**：根据DM单号请求 `POST /api/export/form` 获取审批单涉及的 `optype` / `tables` / `columns` / `dmUser` / `status`，并与节点参数比对。仅 `optype=table` 时继续，否则节点失败。
2. **检测轮询（② 任务生成/状态查询）**：调用 `POST /api/export/task`。首次请求 `taskId` 传空创建检测任务，DataGo返回唯一 `taskId`（数值型）；后续请求携带该 `taskId` 轮询检测状态，直到 `detected_pass`（通过）/ `detected_fail`（不通过）/ `detect_error`（异常）。
3. **执行外发（③ 执行外发）**：检测通过后调用 `POST /api/export/execute`，传入 `dmId` / `optype=table` / `notifyUsers` / `taskIds`，DataGo **一次性完成** 创建飞书多维表格 + 写 sheet + 向 `notifyUsers` 发送飞书消息，返回多维表格 URL 与各 sheet 结果。

> **说明**：不再设置独立的"发送轮询"和"成功通知"阶段——③ `execute` 为同步一次性操作，外发与飞书通知由 DataGo 内部一并完成；检测不通过（`detected_fail`/`detect_error`）时 DataGo **已主动**经飞书通知用户，DSS 无需重复通知。

任一阶段出现参数不一致、optype 非法、检测失败、外发失败、超时或DataGo接口异常，节点均直接失败并抛出明确异常（异常码 82001~82011，见 4.3）。

## 二、整体架构

### 2.1 模块架构图

```
                         DSS工作流
                            |
                            v
              DataGo飞书多维表格外发节点
                            |
                            v
        DataGoFeishuRefExecutionOperation
          +-----------------+-----------------+
          |                 |                 |
          v                 v                 v
  NodeParams(解析)   DataGoFeishuClient   DataGoFeishuExecutionAction
  DmInfoComparator       (①②③调用)        (状态机/轮询/重试载体)
                            |
                            v
                    DataGo数据外发服务
                    POST /api/export/*
                    ├── ① /form      表单获取
                    ├── ② /task      任务生成/状态查询
                    └── ③ /execute   执行外发(建表+写sheet+飞书消息)
                            |
                            v
                    QZ区weproxy-nginx
                            |
                            v
                    OASF区飞书服务接口
```

### 2.2 AppConn定位

| 项 | 设计 / 实现 |
|----|------|
| AppConn名称 | `datagofeishu`（`dss_appconn.appconn_name`） |
| 节点类型 | `linkis.appconn.datagofeishu`（`dss_workflow_node.node_type`） |
| 工作流节点 DB name | `sendfeishu`（`dss_workflow_node.name`） |
| 节点图标 | `svgs/sendfeishu-node.svg` |
| 节点所属分组 | `数据输出`（`dss_workflow_node_group.name`） |
| AppConn 类型 | `OnlyDevelopmentAppConn`（仅开发流程规范，无 SSO/组织结构规范） |
| 开发规范 | `OnlyExecutionDevelopmentStandard`（仅提供 RefExecution 执行能力） |
| 执行编排器 | `DataGoFeishuRefExecutionOperation`，继承 `LongTermRefExecutionOperation`，混入 `Killable` + `Procedure` |
| 调度方式 | DSS提交节点后异步执行，DSS周期调用 `state()` 获取状态（长周期异步轮询） |
| 外部服务 | DataGo数据外发服务（`POST /api/export/*`，默认端口 3003） |
| 飞书链路 | DataGo经QZ区 `weproxy-nginx` 转发到OASF区飞书服务接口（FS-*鉴权由DataGo内部处理，DSS无需关心） |
| 对接范围 | 本期仅 `optype=table`（飞书多维表格外发）；`optype=datago`（报告外发）走现有 `/api/report/*`，本AppConn不处理 |

### 2.3 调用时序

```
DataGoFeishuRefExecutionOperation.submit(requestRef)
    |
    +--> 1. buildProperties：合并实例级 enhance_json + runtimeMap + variable，变量替换
    +--> 2. NodeParams.from：解析 dmId / notifyUsers / dataTargets(多库多表多字段多分区)
    +--> 3. new DataGoFeishuClient(properties)
    +--> 4. client.queryExportForm(dmId)        +--> ① POST /api/export/form
    |       +--> 获取 optype / tables / columns / dmUser / status（可选 notifyUsers）
    +--> 5. DmInfoComparator.validate(params, form, "table")
    |       +--> optype!=table / 表不在dm单 / 字段超范围 / notifyUsers越权 → 抛异常
    +--> 6. 解析轮询/重试/超时参数（节点级>实例级>默认，秒×1000转毫秒）
    +--> 7. action.stage = DETECTING, state = Running
    +--> return AsyncExecutionResponseRef（maxLoopTime=maxWaitTime, askStatePeriod=轮询/重试间隔较小值）

DataGoFeishuRefExecutionOperation.state(action)
    |
    +--> 终态/超时(>maxWaitTime: DETECTING→82006, EXPORTING→82007)/轮询节流(<nextPollAt) 判定
    +--> stage == DETECTING
    |       +--> taskId 为空: ② POST /api/export/task {dmId,tables,taskId:null} 创建 → 返回 taskId+status（fields/partitions/notifyUsers 内嵌于 tables[]）
    |       +--> taskId 非空: ② POST /api/export/task {...,taskId:xxx} 查询 → 返回 status
    |       +--> status=inited/detecting        → Running(设 nextPollAt, 继续轮询)
    |       +--> status=detected_pass           → stage = EXPORTING, Running
    |       +--> status=detected_fail           → 节点失败 82005(DataGo已通知)
    |       +--> status=detect_error            → 节点失败 82006(DataGo已通知)
    |       +--> status=exported                → stage = SUCCESS, Success
    +--> stage == EXPORTING
            +--> ③ POST /api/export/execute {dmId,optype:"table",notifyUsers,taskIds:[taskId]}
            +--> 502/504(飞书不可达) → 重试(executeRetryCount<executeRetryMax), Running; 耗尽 82007
            +--> 413/409(超限/状态冲突) → 82009(不可重试)
            +--> 403(越权) → 82003 / 400(参数) → 82001 / 500等 → 82007(不可重试)
            +--> 200 全部 sheet success → stage = SUCCESS, Success
            +--> 200 存在 failed sheet   → 重试失败表, 耗尽 82008(已写入不回滚)
```

## 三、详细设计

### 3.1 工作流节点定义

- **节点类型**: `linkis.appconn.datagofeishu`
- **工作流节点 DB name**: `sendfeishu`（`dss_workflow_node.name`，前端节点 Palette 展示名）
- **节点所属分组**: `数据输出`
- **节点图标**: `svgs/sendfeishu-node.svg`
- **jump_type/support_jump/submit_to_scheduler/enable_copy/should_creation_before_node**: `0/0/1/1/0`（不可跳转、可提交调度、可复制、节点前无需创建）

**节点参数**（节点开发面板填写，对应 `dss_workflow_node_ui` 专属属性）：

| 参数(key) | UI 类型 | 必填 | 说明 |
|-----------|---------|:----:|------|
| dmId | Input | 是 | DM审批单号（对应接口 `dmId`） |
| notifyUsers | Input | 是 | 飞书通知人，多个用英文分号分隔（对应接口 `notifyUsers`，须为DM单检测用户子集） |
| dataTargets | Text | 是 | 外发目标（多库多表多字段多分区），行式 DSL，见下方格式 |

> 节点级 UI 仅暴露上述 3 个业务参数。轮询间隔、外发重试次数/间隔、最大等待时间等运行节奏参数**不暴露为节点 UI**，统一走实例级配置（见 4.2）。`maxWaitTime` 虽在代码中以节点级 key 优先读取，但标准 `init.sql` 未将其注册为 UI 属性，故实际取实例级配置。

**`dataTargets` DSL 格式**（参考 `job.desc` 行式范式）：

```
每行一个外发目标，行间用分号(;)或换行分隔；行内各段用 | 分隔。
dt.序号=db=库名|table=表名|fields=字段1,字段2|partition={分区值}
```

- 行首 `dt.序号=` 切出编号（序号正则 `dt\.\d+`，用于去重与报错定位）；行内每段按**首个 `=`** 切 key/value。
- `db` / `table` / `fields` 必填，`fields` 多字段用英文逗号分隔。
- `partition` 段可选：值为 `{..}` 时取花括号内内容（保护分区值本身的 `=` / `/`，如多级分区 `{dt=20260716/country=us}`）；写 `partition=` 或省略该段表示非分区表（解析后 partition=null）。
- 同一 `(db, table, partition)` 不允许重复（`DataTarget.tableKey()` 去重，非分区表 partition 为空）；同表不同分区允许共存，否则节点失败 82001。
- 未知段 key（非 db/table/fields/partition）直接报错 82001。

**示例（多库多表多字段）**：
```
dt.01=db=bdap_desensitized|table=customer_export_view|fields=cust_id,cust_name,amount|partition={dt=20260716};
dt.02=db=bdap_metrics|table=daily_kpi|fields=kpi_code,kpi_val|partition={dt=20260716/country=us};
dt.03=db=bdap_raw|table=dim_code|fields=code_id,code_name
```

**兼容回退**：若节点未填写 `dataTargets`，仍保留旧的 `dbName`/`tableName`/`fields`/`partition` 单值字段语义（`NodeParams.legacySingleTarget`），自动包装为单元素目标，旧节点不破坏。

**设计决策**：

- 节点不允许用户填写飞书多维表格目标地址，实际目标以DM单和DataGo侧审批信息为准。
- 节点参数只作为执行意图表达，真正执行前必须与DataGo返回的DM单信息比对。
- 字段、通知人等集合类参数在比对前统一做 trim、去空、去重和排序，避免因顺序差异导致误判。
- 节点本期仅处理 `optype=table`；若DM单 `optype=datago`，节点直接失败并提示走现有报告外发。
- **单节点支持多库多表多字段多分区**：每个 `dataTargets` 行对应一个外发目标，逐目标与①审批表单比对；②任务接口按 `tables[]` 批量创建一次检测任务；③按 `taskIds` 批量外发为同一多维表格的多个 sheet。

### 3.2 AppConn结构设计

模块路径与实际包结构：

```
dss-appconn/appconns/dss-datago-feishu-appconn
    +-- pom.xml
    +-- src/main/assembly/distribution.xml
    +-- src/main/java/com/webank/wedatasphere/dss/appconn/datagofeishu
    |       +-- DataGoFeishuAppConn.java                 # AppConn 入口（OnlyDevelopmentAppConn）
    |       +-- standard/DataGoFeishuDevelopmentStandard.java  # 开发规范（OnlyExecutionDevelopmentStandard）
    |       +-- service/DataGoFeishuExecutionService.java      # 执行服务，构造 RefExecutionOperation
    |       +-- client/DataGoFeishuClient.java           # ①②③ 接口客户端
    |       +-- conf/DataGoFeishuConfiguration.java      # 配置键常量
    |       +-- entity/NodeParams.java                   # 节点参数模型 + dataTargets DSL 解析
    |       +-- entity/DataTarget.java                   # 单个外发目标
    |       +-- entity/ExportForm.java                   # ① 表单响应
    |       +-- entity/ExportTable.java                  # ① 表单中的库表项
    |       +-- entity/TaskResponse.java                 # ② 任务响应
    |       +-- entity/ExecuteResponse.java              # ③ 外发响应
    |       +-- entity/SheetResult.java                  # ③ 单 sheet 写入结果
    |       +-- entity/DataGoFeishuResponse.java         # 统一响应持有者（httpCode/success/code/message/data）
    |       +-- exception/DataGoFeishuException.java     # 统一异常（errorCode + httpCode）
    |       +-- utils/DataGoFeishuHttpUtils.java         # OkHttp 底层调用 + 统一响应解析 + 日志
    |       +-- utils/DmInfoComparator.java             # ① 表单与节点参数一致性比对
    +-- src/main/scala/com/webank/wedatasphere/dss/appconn/datagofeishu
    |       +-- DataGoFeishuRefExecutionOperation.scala  # 节点执行编排（submit/state/result/kill/progress/log）
    |       +-- DataGoFeishuExecutionAction.scala        # 执行状态载体 + 阶段常量
    +-- src/main/resources
            +-- init.sql                                 # AppConn/实例/节点/UI 初始化（打包进 jar）
            +-- appconn.properties                       # 配置参考模板（pom 排除，不打进 jar）
            +-- log4j2.xml                               # 日志配置（pom 排除，不打进 jar）
```

| 类 | 职责 |
|----|------|
| `DataGoFeishuAppConn` | `AbstractAppConn` + `OnlyDevelopmentAppConn`，`initialize()` 创建 `DataGoFeishuDevelopmentStandard`，`getOrCreateDevelopmentStandard()` 返回之 |
| `DataGoFeishuDevelopmentStandard` | `OnlyExecutionDevelopmentStandard`，`createRefExecutionService()` 创建 `DataGoFeishuExecutionService` |
| `DataGoFeishuExecutionService` | `AbstractRefExecutionService`，`createRefExecutionOperation()` 构造 `DataGoFeishuRefExecutionOperation` |
| `DataGoFeishuRefExecutionOperation` | 节点执行编排：`submit`（参数解析+①表单+比对+初始化）、`state`（按阶段驱动②轮询/③外发+超时/节流）、`result`/`kill`/`progress`/`log`；三级参数优先级回退（`positiveLong`/`positiveInt`） |
| `DataGoFeishuExecutionAction` | `AbstractRefExecutionAction`，跨次 `state()` 状态载体：client/nodeParams/taskId/stage/轮询与重试参数及计数/nextPollAt/startedAt/bitableUrl 等；含 `DataGoFeishuStage` 阶段常量与 `askStatePeriod` |
| `DataGoFeishuClient` | 封装①②③：`queryExportForm` / `createOrQueryTask` / `executeExport`；构造请求体、错误码映射、业务级日志 |
| `DataGoFeishuConfiguration` | 配置键常量（base.url/各接口路径/token/optype/轮询重试超时）及默认值 |
| `NodeParams` | 节点参数模型；`from(Properties)` 解析 dmId/notifyUsers/dataTargets，含 `parseDataTargets` DSL 解析与旧字段兼容 |
| `DataTarget` | 单个外发目标（dbName/tableName/fields/partition），`tableKey()` 去重依据 |
| `ExportForm` / `ExportTable` | ① 表单响应模型（dmId/dmTitle/dmUser/optype/tables/status，及可选 notifyUsers） |
| `TaskResponse` | ② 任务响应模型（taskId:Long/optype/status/resultSummary） |
| `ExecuteResponse` / `SheetResult` | ③ 外发响应模型（bitableName/bitableUrl/sheets[]/notifyUsers/exportedAt） |
| `DataGoFeishuResponse` | 统一响应持有者（httpCode/success/code/message/data:JsonObject），`isHttpOk`/`isBusinessOk` 判定 |
| `DataGoFeishuException` | 统一异常，`errorCode`（82001~82011）+ `httpCode`（用于③重试判定：502/504 可重试） |
| `DataGoFeishuHttpUtils` | OkHttp 底层调用；统一响应解析 `{success,code,message,data}`；`serializeNulls` 保证②`taskId:null`；详细请求/响应日志（响应体截断 2000 字符）；网络/解析异常抛 82011 |
| `DmInfoComparator` | ① 表单与节点参数一致性比对（optype/状态/逐目标库表命中/字段子集/通知人授权） |

### 3.3 DataGo接口设计

> 本节接口契约严格对齐 [接口文档 v2.0](./数据外发飞书多维表格_接口文档.md)。Base URL：`http://{DataGoHost}:{DataGoPort}`（默认端口 `3003`）；`Content-Type: application/json; charset=utf-8`；统一响应结构 `{success, code, message, data}`。请求路径与 token 由实例级 `enhance_json` 配置（见 4.2），客户端 `DataGoFeishuClient` 构造时读取。

#### 3.3.1 ① 外发表单获取 — `POST /api/export/form`

**用途**: 按DM单号查询外发表单数据（库表、字段、分区、用户、optype、检测/外发状态），用于与节点参数比对。

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| dmId | string | 否 | DM审批单号；不传或不存在时返回空数据 |

```json
{ "dmId": "DM202607150001" }
```

**响应字段**（dm单存在，optype=table）:

| 字段 | 类型 | 说明 |
|------|------|------|
| data.dmId | string | dm单号 |
| data.dmTitle | string | dm单标题 |
| data.dmUser | string | 提单人（dm单检测用户；③ 的 notifyUsers 须为其子集） |
| data.optype | string | 外发对象类型（`table` / `datago`） |
| data.tables | array | 库表列表（optype=table 时） |
| data.tables[].dbName | string | 库名 |
| data.tables[].tableName | string | 表名 |
| data.tables[].partition | string | 分区（如 `ds=2026-07-14`） |
| data.tables[].columns | string[] | 授权列 |
| data.status | string | 当前状态（见 3.3.2 状态枚举） |
| data.notifyUsers | string[]? | **可选**：审批允许的通知人集合。若①返回，则客户端据此做 notifyUsers 子集校验；否则该项校验交由③（403）兜底 |

dm单不存在时返回 `{success:true, code:200, message:"无数据", data:null}`，客户端 `queryExportForm` 见 `data==null` 即抛 82002。

> 说明：接口文档 v1.1 的①响应未显式列出 `data.notifyUsers`，`ExportForm` 与 `DmInfoComparator` 将其作为**可选字段**读取——若 DataGo 返回则客户端先做子集校验，未返回则跳过、由③ 403 兜底。

**校验规则**（`DmInfoComparator.validate`）:

- `optype` 必须为 `table`，否则节点失败 82003（提示走现有报告外发）。
- `status` 终态：`detected_fail`→82005、`detect_error`→82006、`exported`→82002（已外发不可重复）；`inited`/`detecting`/`detected_pass` 继续。
- **逐外发目标命中**：`dataTargets` 中每个目标 `(db, table)` 必须命中 `data.tables` 中的某张表；未命中时提示"第 N 个外发目标库表 … 不在DM单审批范围内"（82003）。
- 每个目标的 `fields` 必须是该表 `columns` 的**子集**（允许只外发部分审批字段，不要求完全一致）；节点字段超出审批范围时提示"第 N 个外发目标 … 字段超出DM单审批范围，越权字段: …"（82003）。
- 每个目标的 `partition` 范围校验：`DmInfoComparator.validatePartition` 已实现（节点分区须在审批分区范围内），**当前 `validate()` 未调用它**，分区一致性交由②③检测阶段以实际分区为准。详见 3.4。
- 节点填写的 `notifyUsers`：若①返回 `data.notifyUsers`，则须为其子集，越权报 82003；若①未返回，跳过客户端校验，由③ 403 兜底。

#### 3.3.2 ② 外发任务生成 / 状态查询 — `POST /api/export/task`

**用途**: **同一接口，两种语义**，由 `taskId` 是否为空区分：
- `taskId` 为空/null → **创建**检测任务（optype 继承自dm单），返回 `taskId`
- `taskId` 非空 → **查询**该任务当前状态

**请求体**（接口契约字段，v1.7/v1.8 起 `fields`/`partitions`/`notifyUsers` 全部内嵌于每个 `tables[]` 元素，外层不再有这些参数）:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| dmId | string | 是 | DM审批单号（须在①中有效） |
| tables | array | 是 | 外发库表列表（**每表自带 fields/partitions/notifyUsers**） |
| tables[].dbName | string | 是 | 库名 |
| tables[].tableName | string | 是 | 表名 |
| tables[].fields | string[] | 是 | 该表的外发字段（列名）；**须⊆dm表单该表 `columns`**，否则检测阶段"库表范围检测"失败 |
| tables[].partitions | string[] | 否 | 该表的分区列表（按分区逐批读取/检测，写入同一 sheet）；非分区表不传 |
| tables[].notifyUsers | string[] | 是 | 该表的外发通知用户；**非空**，且**须⊆dm表单该表用户名单**（① 返回的 `tables[].usernames`），否则拒绝 |
| taskId | number \| null | 否 | **空/null=创建；非空=查询** |

**实际发送的请求体**（`DataGoFeishuClient.taskBody` → `buildTables`，由 `dataTargets` 按表合并映射）:

```json
{
  "dmId": "DM202607150001",
  "tables": [
    { "dbName": "db_audit", "tableName": "t_event_log", "fields": ["event","ts","user_id"], "partitions": ["ds=2026-07-14"], "notifyUsers": ["alexyang","ryanchen"] },
    { "dbName": "db_audit", "tableName": "t_dim_code", "fields": ["code_id","code_name"], "notifyUsers": ["alexyang","ryanchen"] }
  ],
  "taskId": null
}
```

> 实现说明：
> - **按 (db, table) 合并**：同一 `(db, table)` 的多个外发目标（如同表不同分区）合并为单一 `tables[]` 元素，`partitions` 为该表分区列表（去重保序），同表多分区写入同一 sheet。
> - 每个 `tables[]` 元素内嵌 `dbName`/`tableName`/`fields`（该表所有目标字段并集，去重保序）/`partitions`（非空才传）/`notifyUsers`（节点通知人，每表一致）。
> - 外层不再有 `fields`/`partitions`/`notifyUsers`（v1.7/v1.8 起全部内嵌于 `tables[]`）。
> - Gson 开启 `serializeNulls`，保证创建请求 `"taskId": null` 符合契约。

**响应字段**:

| 字段 | 类型 | 说明 |
|------|------|------|
| data.taskId | number | 任务ID（= DataGo外发任务主键，后续③/查询均用它） |
| data.optype | string | 外发对象类型（继承自dm单） |
| data.status | string | 任务状态（见下表） |
| data.resultSummary | string | 状态/结果摘要（进度、命中敏感表、失败原因等） |

**任务状态枚举**:

| status 值 | 含义 | 是否终态 | DSS 后续动作 |
|-----------|------|:--------:|-------------|
| `inited` | 已创建，待检测 | 否 | 继续轮询 |
| `detecting` | 检测中 | 否 | 继续轮询 |
| `detected_pass` | 检测通过 | 否 | 可调③外发 |
| `detected_fail` | 检测不通过（命中敏感） | 是 | 流程终止 82005，DataGo已飞书通知 |
| `detect_error` | 检测异常（已自动重试2次仍失败） | 是 | 流程终止 82006，DataGo已飞书通知 |
| `exported` | 已外发 | 是 | 流程完成 |

**调用规则**:

- 第一次调用 `taskId` 传 null，DataGo创建检测任务后返回唯一 `taskId`（数值型），DSS保存于 `action.taskId`；若未返回 taskId 抛 82004。
- 后续轮询必须传入上一次返回的 `taskId`。
- `inited`/`detecting` 设 `nextPollAt` 继续轮询；`detected_pass` 进入外发阶段；`detected_fail`/`detect_error` 节点失败；`exported` 直接成功。
- 未知 status 抛 82004。
- 建议轮询间隔 10~30 秒（检测按表分批，每批 ≤5000 行）。
- **创建幂等**：同一dm单若已有进行中任务，建议先查再决定是否创建，避免重复（依赖DataGo侧创建幂等）。

**错误响应与异常码映射**（`createOrQueryTask`）:

| HTTP code | 含义 | DSS 异常码 |
|:----:|------|:----:|
| 400 | dmId 缺失等参数错误 | 82004（403 时为 82003） |
| 403 | dm 单无效 / optype≠table / 已过期；某表 `notifyUsers` 为空或非 dm 表单该表用户名单子集（越权） | 82003 |
| 404 | 查询模式下 taskId 不存在 | 82004 |
| 500 等 | 服务内部错误 | 82004 |

> 本期②仅支持 optype=table。dm单 optype=datago 时 DataGo 返回 403，映射 82003。

#### 3.3.3 ③ 执行外发 — `POST /api/export/execute`

**用途**: 当dm单对应任务检测通过（`detected_pass`）后，批量外发。optype=table 时 DataGo **一次性完成**：创建飞书多维表格 + 写 sheet + 向 `notifyUsers` 发送飞书消息（含多维表格URL）。

**外发结果**（optype=table）：
- 一个dm单 → 创建一个飞书多维表格，命名 `{dmId}--{表数量}--{时间戳}`
- 每个表 → 一个 sheet；单表过大时分多次请求追加写入同一 sheet
- 外发成功同时向 `notifyUsers` 发送飞书消息

**请求体**（`DataGoFeishuClient.executeBody`）:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| dmId | string | 是 | DM审批单号 |
| optype | string | 是 | 固定 `"table"`（`datago` → 409） |
| notifyUsers | string[] | 是 | 外发结果通知用户；为空报错；且必须是dm单检测用户（dmUser）的子集，否则报错 |
| taskIds | number[] | 否 | 指定导出的任务；本设计传入 `[本节点检测taskId]` |

**请求示例**：
```json
{
  "dmId": "DM202607150001",
  "optype": "table",
  "notifyUsers": ["alexyang", "ryanchen"],
  "taskIds": [1024]
}
```

> 节点传入 `taskIds=[本节点检测taskId]`，仅导出本节点检测通过的任务，避免误导出同dm单其他任务。

**响应字段**（成功，`isBusinessOk` 即 httpOk+success+data非空）:

| 字段 | 类型 | 说明 |
|------|------|------|
| data.dmId | string | dm单号 |
| data.optype | string | `table` |
| data.bitableName | string | 多维表格名称（`{dmId}--{表数量}--{时间戳}`） |
| data.bitableUrl | string | 多维表格访问URL |
| data.sheets | array | 各表 sheet 写入结果 |
| data.sheets[].tableName | string | 表名 |
| data.sheets[].rows | number | 写入行数 |
| data.sheets[].status | string | `success` / `failed` |
| data.notifyUsers | string[] | 实际通知用户 |
| data.exportedAt | string | 外发完成时间（ISO 8601） |

> **部分失败**：若批量中某表失败，**已写入的表不回滚**，`sheets[].status` 标记各表结果，整体仍返回响应并飞书通知；DSS 据 `sheets` 重试失败表。本设计单节点场景下，存在 `failed` sheet 即按重试策略重试，仍失败则节点失败 82008。

**错误响应与异常码映射**（`mapExecuteErrorCode` + `handleExport` 重试判定）:

| HTTP code | message 示例 | 触发 | DSS 异常码 | 可否重试 |
|:----:|-------------|------|:----:|:----:|
| 400 | `optype 参数为必填项` / `notifyUsers 不能为空` | optype缺失/notifyUsers为空 | 82001 | 否 |
| 403 | `notifyUsers 必须是 dm 单检测用户的子集，越权用户: [xxx]` | notifyUsers含dmUser之外账号 | 82003 | 否 |
| 409 | `optype=datago 本期本接口不支持` / `存在未检测通过的任务` | optype=datago / 指定任务未全通过 | 82009 | 否 |
| 413 | `表 t_xxx 超出大小限制(估算 62MB, 上限 50MB)` | 单sheet估算超50MB | 82009 | 否 |
| 502/504 | `飞书服务不可达，请稍后重试` | weproxy/飞书不可达 | 82007 | **是**（重试耗尽仍失败才落 82007） |
| 500 | 服务内部错误 | DataGo内部异常 | 82007 | 否 |
| 网络/解析异常 | 空响应/非JSON/IOException | `DataGoFeishuHttpUtils` 抛 82011，③包装为 82007 | 82007 | 否 |

> 重试语义：仅 HTTP 502/504（飞书不可达）与"200 但存在 failed sheet"两类触发重试，重试上限 `execute.retry.max`（默认 3）；413/409/403/400/500 及网络异常均不重试，直接失败。异常对象保留 `httpCode` 供 `handleExport` 判定可重试性。

### 3.4 DM信息比对设计

**文件**: `utils/DmInfoComparator.java`

**职责**: 对①返回的DM单表单信息和用户填写的节点参数进行一致性比对。`validate(NodeParams, ExportForm, supportedOptype)` 入口，按顺序执行各校验，任一不满足抛 `DataGoFeishuException`。

| 比对项 | 比对规则 | 失败提示 / 异常码 |
|--------|----------|----------|
| DM单存在性 | ①返回 data 非空（`queryExportForm` 已保证，`form==null` 兜底） | 82002 |
| optype | 必须为 `table`（与 `supportedOptype` 一致） | 82003：DM单optype=xx非table，请走现有报告外发 |
| DM单状态 | `detected_fail`/`detect_error`/`exported` 视为不可继续 | 82005 / 82006 / 82002 |
| 库名+表名 | **逐外发目标**：每个 `(db,table)` 必须命中 tables 列表 | 82003：第N个外发目标库表 … 不在DM单审批范围内 |
| 字段名称 | 每个目标 fields 必须是该表 columns 的**子集**（允许只外发部分审批字段） | 82003：第N个外发目标 … 字段超出DM单审批范围，越权字段: … |
| 分区名 | `validatePartition`：节点 partition 须在审批 partition 范围内 | 82003：第N个外发目标 … 分区不在DM单审批范围内 |
| 飞书通知人 | 节点 notifyUsers 须为①返回 `data.notifyUsers` 的子集 | 82003：飞书通知人超出DM单授权范围，越权用户: … |

> **实现现状**：
> - **分区校验**：`validatePartition` 已实现但 `validate()` 当前**未调用**，分区一致性不在客户端强校验，交由②③检测阶段以实际分区为准。如需启用，在 `validate()` 循环内补 `validatePartition(target, table, idx)` 调用即可。
> - **notifyUsers 校验**：客户端依据①可选字段 `data.notifyUsers` 做子集校验；若①未返回该字段则跳过，由③ 403（notifyUsers 须为 dmUser 子集）兜底。即客户端校验"审批允许通知人集合"，服务端③校验"dm 单检测用户 dmUser"，二者目标一致、互为兜底。

**字段规范化**（`normalize`）：trim、去空、去重、排序，用于集合类比对。

### 3.5 长轮询状态机设计

**文件**: `DataGoFeishuExecutionAction.scala`（状态载体 + `DataGoFeishuStage` 阶段常量）、`DataGoFeishuRefExecutionOperation.scala`（状态机驱动）

**执行阶段**（`DataGoFeishuStage`）:

| 阶段常量 | 值 | 含义 | 下一阶段 |
|------|------|------|----------|
| Init | `INIT` | 概念阶段：参数解析与①表单获取/比对（在 `submit` 内完成，不作为持久化阶段） | DETECTING / FAILED |
| Detecting | `DETECTING` | 调用②任务接口（创建+轮询） | EXPORTING / SUCCESS / FAILED |
| Exporting | `EXPORTING` | 调用③执行外发（一次性，含飞书通知） | SUCCESS / FAILED |
| Success | `SUCCESS` | 节点成功 | 结束 |
| Failed | `FAILED` | 节点失败 | 结束 |
| Killed | `KILLED` | 节点被杀死 | 结束 |

> `submit` 完成参数解析+①+比对后直接置 `stage = Detecting`（INIT 不落库）。`state()` 按当前 stage 分发到 `handleDetect` / `handleExport`。

**状态机逻辑**（伪代码，对齐 `state`/`handleDetect`/`handleExport`）:

```scala
state(action):
    if action.state.isCompleted: return action.state
    if now - action.startedAt > action.maxWaitTime:           // 超时
        code = (stage == Detecting) ? 82006 : 82007
        return fail(action, "任务等待超时", code).state
    if now < action.nextPollAt: return action.state           // 轮询节流
    try {
        stage match {
            case Detecting => handleDetect(action)
            case Exporting => handleExport(action)
            case _ => throw 82011("未知执行阶段: " + stage)
        }
    } catch t => fail(action, "执行失败: " + t, t)
    return action.state

handleDetect(action):
    response = client.createOrQueryTask(nodeParams, action.taskId)
    if response.status blank: throw 82004
    if action.taskId == null:
        if response.taskId == null: throw 82004("首次检测未返回taskId")
        action.taskId = response.taskId
    switch normalize(response.status):
        case "inited"|"detecting": nextPollAt = now + detectPollInterval; Running
        case "detected_pass":      stage = Exporting; nextPollAt = 0; Running
        case "detected_fail":      throw 82005(resultSummary)
        case "detect_error":       throw 82006(resultSummary)
        case "exported":           stage = Success; Success
        case other:                throw 82004("未知状态: " + status)

handleExport(action):
    try:
        response = client.executeExport(dmId, notifyUsers, taskId)   // isBusinessOk 否则抛 mapped code
        if response.status in ("export_failed","failed"): throw 82007("DataGo外发失败: status="+status)  // 外发失败终态,不重试
        failedSheets = response.sheets.filter(status != "success")
        if failedSheets.empty:
            stage = Success; Success
        else if executeRetryCount < executeRetryMax:                 // 部分失败重试
            executeRetryCount++; nextPollAt = now + executeRetryInterval; Running
        else: throw 82008("部分表失败且重试耗尽（已写入不回滚）")
    catch e: DataGoFeishuException:
        if e.httpCode in (502,504) and executeRetryCount < executeRetryMax:  // 飞书不可达重试
            executeRetryCount++; nextPollAt = now + executeRetryInterval; Running
        else if e.httpCode in (502,504): throw 82007("外发重试次数耗尽")
        else: throw e                            // 413/409→82009, 403→82003, 400→82001, 500/82011→82007
```

**轮询与节流控制**:

| 机制 | 说明 |
|------|------|
| `nextPollAt` | `inited`/`detecting`/外发重试后设置；`state()` 在未到 `nextPollAt` 时直接返回当前状态，不调 DataGo |
| `askStatePeriod` | `max(1000ms, min(detectPollInterval, executeRetryInterval))`，作为 `AsyncExecutionResponseRef` 的 DSS 询问状态周期 |
| `maxLoopTime` | 取 `maxWaitTime`，作为异步响应最大循环时间 |
| `progress` | Detecting=0.3、Exporting=0.8、Success=1.0、其它=0.0 |
| 超时 | `now - startedAt > maxWaitTime`：DETECTING→82006、EXPORTING→82007 |

**轮询控制配置**（实例级，见 4.2）:

| 配置项 | 默认值 | 单位 | 说明 |
|--------|--------|------|------|
| detect.poll.interval.ms | 30 | 秒 | ②检测状态轮询间隔（建议10~30秒） |
| execute.retry.max | 3 | 次 | ③外发失败最大重试次数 |
| execute.retry.interval.ms | 30 | 秒 | ③外发重试间隔 |
| max.wait.time.ms | 7200 | 秒 | 节点最大等待时间（2小时） |
| http.connect.timeout.ms | 10000 | 毫秒 | DataGo接口连接超时 |
| http.read.timeout.ms | 60000 | 毫秒 | DataGo接口读取超时 |

### 3.6 异常处理设计

| 场景 | 接口表现 | 处理 / 异常码 |
|------|----------|------|
| 节点参数缺失或格式错误 | — | 节点失败，82001 |
| AppConn 配置缺失（base.url 等） | — | 节点失败，82001 |
| ①表单接口调用失败/返回404(dm单不存在) | ①失败/data=null | 节点失败，82002 |
| DM单optype≠table | ①optype=datago | 节点失败，82003（提示走报告外发） |
| DM单信息与节点参数不一致 | ①比对偏差 | 节点失败，82003（提示具体不一致项） |
| ②任务接口调用失败 | ②400/500等 | 节点失败，82004（403→82003） |
| 首次检测未返回taskId | ②data.taskId缺失 | 节点失败，82004 |
| ②返回未知 status | ②status 非法 | 节点失败，82004 |
| 检测不通过（命中敏感） | ②status=detected_fail | 节点失败，82005（DataGo已飞书通知，DSS无需重复通知） |
| 检测异常 | ②status=detect_error | 节点失败，82006（DataGo已飞书通知） |
| 检测轮询超时 | DETECTING 阶段超过 maxWaitTime | 节点失败，82006 |
| 外发阶段超时 | EXPORTING 阶段超过 maxWaitTime | 节点失败，82007 |
| ③外发接口调用失败(飞书不可达) | ③502/504 | 按重试策略重试，耗尽则82007 |
| ③外发接口内部错误/网络异常 | ③500 / 82011 | 节点失败，82007（不重试） |
| ③外发部分表失败 | ③200但sheets含failed | 重试失败表，耗尽则82008（已写入不回滚） |
| ③外发超限/状态冲突/optype不支持 | ③413/409 | 节点失败，82009（不可重试） |
| notifyUsers越权 | ③403 | 节点失败，82003 |
| ③optype/notifyUsers 参数缺失 | ③400 | 节点失败，82001 |
| DataGo 响应空/非JSON/网络异常 | 任意接口 | 82011（③处包装为82007） |
| 未知执行阶段 | — | 82011 |
| 节点被kill | — | stage=Killed，state=Killed，不再轮询DataGo |

### 3.7 日志与审计

**日志记录**:

- `DataGoFeishuHttpUtils` 对每次请求记录「方法 + URL + 请求体」与「HTTP 状态码 + 耗时 + 业务 success/code + 响应体（截断 2000 字符）」，便于全链路排查。
- `DataGoFeishuClient` 记录各接口业务级入口与结果摘要（dmId/optype/status/taskId/bitableUrl/sheetCount 等）。
- `DataGoFeishuRefExecutionOperation` 记录节点初始化、检测创建/轮询、外发成功/重试/失败等关键事件，并通过 `appendLog` 写入节点执行日志（带时间戳）。
- 不记录DataGo返回的原始数据内容；飞书通知人记录账号标识，不记录飞书凭据。

**审计字段**（节点日志/异常信息中可体现）:

| 字段 | 说明 |
|------|------|
| executionId / nodeId / user | DSS执行ID/节点ID/执行用户 |
| dmId / optype | DM单号 / 外发对象类型 |
| taskId | DataGo检测任务ID（数值） |
| dataTargets / targetCount / fieldCount | 外发目标列表 / 数量 / 字段总数 |
| notifyUsers | 飞书通知人 |
| stage / lastRemoteStatus / lastSummary | 当前阶段 / 最近远端状态 / 摘要 |
| bitableUrl / sheetsSummary | 多维表格URL / 各sheet结果摘要 |
| errorMessage / errorCode / httpCode | 失败原因 / 异常码 / HTTP码 |

## 四、数据模型

### 4.1 节点参数模型

```json
{
  "dmId": "DM202607150001",
  "notifyUsers": "alexyang;ryanchen",
  "dataTargets": "dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14};dt.02=db=db_audit|table=t_dim_code|fields=code_id,code_name"
}
```

- `dmId`：String，必填。
- `notifyUsers`：解析为 `List<String>`，按 `[,;]` 切分后 trim、去空、去重、排序。
- `dataTargets`：解析为 `List<DataTarget>`，每个 `DataTarget = { dbName, tableName, fields:List<String>, partition }`；`partition` 为 null 表示非分区表。
- 兼容旧单值字段 `dbName`/`tableName`/`fields`/`partition`（无 `dataTargets` 时包装为单元素目标）。

### 4.2 配置数据模型

实例级配置写入 `dss_appconn_instance.enhance_json`，执行时由 `service.getAppInstance.getConfig` 读取，与 `datachecker` 一致。`appconn.properties` 为参考模板（pom 排除，不打进 jar）。

| 配置键 | CommonVars/常量 | 类型 | 默认值 | 单位 |
|--------|----------------|------|--------|------|
| wds.dss.appconn.datago.feishu.api.base.url | BASE_URL | String | —（如 `http://host:3003`） | — |
| wds.dss.appconn.datago.feishu.api.form.path | FORM_PATH | String | `/api/export/form` | — |
| wds.dss.appconn.datago.feishu.api.task.path | TASK_PATH | String | `/api/export/task` | — |
| wds.dss.appconn.datago.feishu.api.execute.path | EXECUTE_PATH | String | `/api/export/execute` | — |
| wds.dss.appconn.datago.feishu.api.token.header | API_TOKEN_HEADER | String | `Authorization` | — |
| wds.dss.appconn.datago.feishu.api.token | API_TOKEN | String | ``（空，不追加鉴权头） | — |
| wds.dss.appconn.datago.feishu.optype.supported | SUPPORTED_OPTYPE | String | `table` | — |
| wds.dss.appconn.datago.feishu.detect.poll.interval.ms | DETECT_INTERVAL | Long | 30 | 秒 |
| wds.dss.appconn.datago.feishu.execute.retry.max | EXECUTE_RETRY_MAX | Int | 3 | 次 |
| wds.dss.appconn.datago.feishu.execute.retry.interval.ms | EXECUTE_RETRY_INTERVAL | Long | 30 | 秒 |
| wds.dss.appconn.datago.feishu.max.wait.time.ms | MAX_WAIT_TIME | Long | 7200 | 秒 |
| wds.dss.appconn.datago.feishu.http.connect.timeout.ms | CONNECT_TIMEOUT | Long | 10000 | 毫秒 |
| wds.dss.appconn.datago.feishu.http.read.timeout.ms | READ_TIMEOUT | Long | 60000 | 毫秒 |

> 轮询/重试间隔/最大等待时间单位为**秒**（key 名保留 `.ms` 后缀以向后兼容，值为秒）；HTTP 超时仍为毫秒。代码内部按毫秒运算，解析时将秒 ×1000 转毫秒。

**参数优先级**：轮询 / 重试 / 超时类参数按 **节点级 UI 值 > 实例级 `enhance_json` 配置 > 硬编码默认值** 三级回退，由 `DataGoFeishuRefExecutionOperation` 的 `positiveLong` / `positiveInt` 统一承载：先取节点级 `nodeKey`（非空则用），否则取实例级 `configKey`，再否则用 `defaultValue`。秒级参数在赋值给 action（内部毫秒语义）时 ×1000。

| 参数 | 节点级 UI key | 实例级 configKey | 硬编码默认 | 标准UI是否暴露 |
|------|--------------|------------------|-----------|:-----------:|
| 节点最大等待时间 | `maxWaitTime` | `wds.dss.appconn.datago.feishu.max.wait.time.ms` | 7200秒（2小时） | 否（代码读 nodeKey，但 init.sql 未注册 UI，实际走实例级） |
| 检测轮询间隔 | `detectPollInterval` | `wds.dss.appconn.datago.feishu.detect.poll.interval.ms` | 30秒 | 否 |
| 外发重试次数 | `executeRetryMax` | `wds.dss.appconn.datago.feishu.execute.retry.max` | 3 | 否 |
| 外发重试间隔 | `""`（不读节点级） | `wds.dss.appconn.datago.feishu.execute.retry.interval.ms` | 30秒 | 否 |

> 标准 `init.sql` 不将上述任一参数暴露为节点 UI 属性（仅 `dmId`/`notifyUsers`/`dataTargets` 暴露），避免用户随意调整轮询/重试节奏；`executeRetryInterval` 的 `nodeKey` 传空串，明确仅走实例级配置。

### 4.3 异常码分配

| 异常码 | 含义 | 对应接口场景 |
|--------|------|------|
| 82001 | 节点参数缺失或格式错误 / AppConn配置缺失 | dmId/notifyUsers/dataTargets 缺失或 dataTargets DSL 格式错误、base.url 为空、参数非正整数、③400 |
| 82002 | DataGo表单接口(①)调用失败 | ① form 调用异常或返回404(dm单不存在)/data=null、DM单已外发(exported) |
| 82003 | DM单信息与节点参数不一致 | optype≠table、表不在dm单、字段超范围、notifyUsers越权、②③返回403 |
| 82004 | DataGo任务接口(②)调用失败 | ② task 创建/查询调用异常、未返回taskId、未返回有效状态、未知status |
| 82005 | DataGo检测不通过 | ② status=detected_fail（DataGo已通知） |
| 82006 | DataGo检测异常或检测轮询超时 | ② status=detect_error 或 DETECTING 阶段超过 maxWaitTime |
| 82007 | DataGo执行外发接口(③)调用失败 | ③ execute 502/504 重试耗尽、500、网络异常、EXPORTING 阶段超时、③ status=export_failed/failed（外发失败终态） |
| 82008 | DataGo外发部分表失败 | ③ sheets存在failed且重试耗尽（已写入不回滚） |
| 82009 | DataGo外发超限或状态冲突 | ③ 413(超50MB) / 409(未通过/optype不支持) |
| 82011 | DataGo接口通信/解析异常或未知阶段 | 响应空/非JSON/IOException、未知执行阶段 |

## 五、部署与配置

### 5.1 init.sql设计

`init.sql` 位于 `src/main/resources/init.sql`，打包进 jar，安装时执行。它一次性初始化：AppConn 元信息、AppConn 实例（含 `enhance_json` 运行配置）、工作流节点、节点所属分组、节点专属属性 UI、校验规则与绑定关系。脚本按 key 清除后重建，保证重复执行幂等。

> ⚠️ 脚本首行注释指出：**仅适用于第一次安装**。升级安装时不能先删 `dss_appconn` 再插入（`dss_workspace_appconn_role` 等表关联了 `appconn_id`），需改为 `update`/`alter` 方式，保持 `appconn_id` 不变。

实际 `init.sql` 全文：

```sql
-- TODO 这里只适用于第一次安装时。如果是更新的话dss_appconn表不能先删除再插入，因为其他表如dss_workspace_appconn_role关联了appconn_id(不能变)，需要使用update、alter语句更新
select @datago_feishu_appconn_id:=id from `dss_appconn` where `appconn_name` = 'datagofeishu';
delete from `dss_appconn_instance` where `appconn_id` = @datago_feishu_appconn_id;

delete from dss_appconn where appconn_name = "datagofeishu";
INSERT INTO `dss_appconn` (`appconn_name`, `is_user_need_init`, `level`, `if_iframe`, `is_external`, `reference`, `class_name`, `appconn_class_path`, `resource`)
VALUES ('datagofeishu', 0, 1, 1, 1, NULL, 'com.webank.wedatasphere.dss.appconn.datagofeishu.DataGoFeishuAppConn', 'DSS_INSTALL_HOME_VAL/dss-appconns/datagofeishu', '');

select @datago_feishu_appconn_id:=id from `dss_appconn` where `appconn_name` = 'datagofeishu';

INSERT INTO `dss_appconn_instance` (`appconn_id`, `label`, `url`, `enhance_json`, `homepage_uri`)
VALUES (@datago_feishu_appconn_id, 'DEV', 'datagofeishu', '{"wds.dss.appconn.datago.feishu.api.base.url":"http://DATAGO_HOST:3003","wds.dss.appconn.datago.feishu.api.form.path":"/api/export/form","wds.dss.appconn.datago.feishu.api.task.path":"/api/export/task","wds.dss.appconn.datago.feishu.api.execute.path":"/api/export/execute","wds.dss.appconn.datago.feishu.api.token.header":"Authorization","wds.dss.appconn.datago.feishu.api.token":"","wds.dss.appconn.datago.feishu.optype.supported":"table","wds.dss.appconn.datago.feishu.detect.poll.interval.ms":"30","wds.dss.appconn.datago.feishu.execute.retry.max":"3","wds.dss.appconn.datago.feishu.execute.retry.interval.ms":"30","wds.dss.appconn.datago.feishu.max.wait.time.ms":"7200","wds.dss.appconn.datago.feishu.http.connect.timeout.ms":"10000","wds.dss.appconn.datago.feishu.http.read.timeout.ms":"60000"}', '');

delete from dss_workflow_node where appconn_name = "datagofeishu";
insert into `dss_workflow_node` (`name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`, `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`)
values('sendfeishu','datagofeishu','linkis.appconn.datagofeishu','0','0','1','1','0','svgs/sendfeishu-node.svg');

select @datago_feishu_node_id:=id from `dss_workflow_node` where `node_type` = 'linkis.appconn.datagofeishu';

delete from `dss_workflow_node_to_group` where `node_id`=@datago_feishu_node_id;
delete from `dss_workflow_node_to_ui` where `workflow_node_id`=@datago_feishu_node_id;

-- 查找节点所属组的id
select @datago_feishu_node_group_id:=id from `dss_workflow_node_group` where `name` = '数据输出';

INSERT INTO `dss_workflow_node_to_group`(`node_id`,`group_id`) values (@datago_feishu_node_id, @datago_feishu_node_group_id);

-- 删除并新增节点专属属性UI（按 key 清除，保证重复执行幂等）
delete from `dss_workflow_node_ui` where `key` in ('dmId','notifyUsers','dataTargets') and node_menu_type = 1;

INSERT INTO `dss_workflow_node_ui`
(`key`, description, description_en, lable_name, lable_name_en, ui_type, required, value, default_value, is_hidden, `condition`, is_advanced, `order`, node_menu_type, is_base_info, `position`)
VALUES ('dmId', '请输入DataGo侧DM审批单号', 'DataGo DM approval order ID', 'DM单号', 'DM Order ID', 'Input', 1, NULL, NULL, 0, NULL, 0, 1, 1, 0, 'runtime');

INSERT INTO `dss_workflow_node_ui`
(`key`, description, description_en, lable_name, lable_name_en, ui_type, required, value, default_value, is_hidden, `condition`, is_advanced, `order`, node_menu_type, is_base_info, `position`)
VALUES ('notifyUsers', '飞书通知人，多个使用英文分号分隔，须为DM单检测用户子集', 'Feishu notify users, semicolon separated', '飞书通知人', 'Notify Users', 'Input', 1, NULL, NULL, 0, NULL, 0, 2, 1, 0, 'runtime');

INSERT INTO `dss_workflow_node_ui`
(`key`, description, description_en, lable_name, lable_name_en, ui_type, required, value, default_value, is_hidden, `condition`, is_advanced, `order`, node_menu_type, is_base_info, `position`)
VALUES ('dataTargets', '每行一个外发目标，行间用分号(;)或换行分隔；行内格式：dt.序号=db=库名|table=表名|fields=字段1,字段2|partition={分区值}。无分区写 partition= 或省略该段。示例：dt.01=db=bdap_desensitized|table=customer_export_view|fields=cust_id,cust_name,amount|partition={dt=20260716}', 'One target per line (; or newline); inline: dt.NN=db=..|table=..|fields=..|partition={..}. Use partition= or omit for non-partitioned tables.', '外发目标', 'Data Targets', 'Text', 1, NULL, NULL, 0, NULL, 0, 3, 1, 0, 'runtime');

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
select @ui_auto_disable:=id from dss_workflow_node_ui where `key` = 'auto.disabled' limit 1;
select @ui_engine_runtime_priority:=id from dss_workflow_node_ui where `key` = 'wds.linkis.engine.runtime.priority' limit 1;

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
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@datago_feishu_node_id, @ui_auto_disable);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`,`ui_id`) values (@datago_feishu_node_id, @ui_engine_runtime_priority);

select @validate_required_id:=id from dss_workflow_node_ui_validate where validate_type  = 'Required' limit 1;

INSERT INTO dss_workflow_node_ui_to_validate (ui_id, validate_id) VALUES (@ui_dm_id, @validate_required_id);

INSERT INTO dss_workflow_node_ui_to_validate (ui_id, validate_id) VALUES (@ui_notify_users, @validate_required_id);
```

**要点说明**:

- **AppConn 元信息**：`appconn_name='datagofeishu'`，`class_name=com.webank.wedatasphere.dss.appconn.datagofeishu.DataGoFeishuAppConn`，`appconn_class_path=DSS_INSTALL_HOME_VAL/dss-appconns/datagofeishu`，`level=1`，`if_iframe=1`，`is_external=1`。
- **实例配置 `enhance_json`**：含 base.url（`http://DATAGO_HOST:3003`，部署时替换）、三个接口路径、token 配置、optype.supported、轮询/重试/超时参数。执行时由 `service.getAppInstance.getConfig` 读取。
- **工作流节点**：`name='sendfeishu'`，`appconn_name='datagofeishu'`，`node_type='linkis.appconn.datagofeishu'`，`icon_path='svgs/sendfeishu-node.svg'`，归入 `数据输出` 分组。
- **专属属性 UI**：`dmId`(Input, order=1)/`notifyUsers`(Input, order=2)/`dataTargets`(Text, order=3)，均 `node_menu_type=1`、`position='runtime'`、必填、中英双语 label 与 description。
- **校验规则**：`dataTargets` 绑定 `Function`/`validateDataTargets`（blur 触发）；`dmId`、`notifyUsers` 绑定 `Required` 校验。
- **节点-UI 绑定**：公共属性（title/desc/businessTag/appTag/ReuseEngine）+ 专属（dmId/notifyUsers/dataTargets）+ 运行控制（auto.disabled / wds.linkis.engine.runtime.priority）。
- **幂等**：按 `appconn_name`、`node_type`、UI `key` 清除后重建，重复执行无副作用。
- **升级注意**：见脚本首行 TODO，升级时勿删 `dss_appconn`，改用 update/alter 保持 `appconn_id`。

### 5.2 appconn.properties配置

`appconn.properties` 为配置参考模板（pom.xml `<resource><excludes>` 排除 `*.properties`，不打进 jar），运行时配置以 `dss_appconn_instance.enhance_json` 为准。两者保持一致：

```properties
# DataGo飞书多维表格外发接口地址（含端口，默认3003）
wds.dss.appconn.datago.feishu.api.base.url=http://DATAGO_HOST:DATAGO_PORT
# 通用外发接口路径（对齐接口文档 v2.0）
wds.dss.appconn.datago.feishu.api.form.path=/api/export/form
wds.dss.appconn.datago.feishu.api.task.path=/api/export/task
wds.dss.appconn.datago.feishu.api.execute.path=/api/export/execute
# 服务间调用鉴权（内网默认网络层隔离，按需配置）
wds.dss.appconn.datago.feishu.api.token.header=Authorization
wds.dss.appconn.datago.feishu.api.token=
# 本期仅处理 optype=table，datago 走现有报告外发 /api/report/*
wds.dss.appconn.datago.feishu.optype.supported=table
# 轮询与重试配置（detect/retry.interval/max.wait.time 单位为秒，key 名保留 .ms 后缀以向后兼容；http 超时为毫秒）
wds.dss.appconn.datago.feishu.detect.poll.interval.ms=30
wds.dss.appconn.datago.feishu.execute.retry.max=3
wds.dss.appconn.datago.feishu.execute.retry.interval.ms=30
wds.dss.appconn.datago.feishu.max.wait.time.ms=7200
wds.dss.appconn.datago.feishu.http.connect.timeout.ms=10000
wds.dss.appconn.datago.feishu.http.read.timeout.ms=60000
```

### 5.3 工作流节点配置

DSS工作流节点参数面板暴露的输入项（对齐 `init.sql` 的 `dss_workflow_node_ui`）：

| UI标签(lable_name) | 参数名(key) | 控件类型(ui_type) | 校验 |
|--------|--------|----------|------|
| DM单号 | dmId | Input | Required |
| 飞书通知人 | notifyUsers | Input | Required，分号分隔，须为DM单检测用户子集 |
| 外发目标 | dataTargets | Text | Required + `validateDataTargets`（dt.序号=db=..\|table=..\|fields=..\|partition={..}，行间分号/换行分隔） |

## 六、执行示例

### 6.1 正常流程

```
1. 用户配置节点：
   dmId=DM202607150001
   notifyUsers=alexyang;ryanchen
   dataTargets=dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14};dt.02=db=db_audit|table=t_dim_code|fields=code_id,code_name

2. DSS调① POST /api/export/form {dmId}，拿到 optype=table、tables、columns、dmUser=alexyang、status。
3. DSS比对节点参数与表单：optype=table、逐目标(dt.01/dt.02)表命中、fields与各表columns一致、notifyUsers⊆dmUser，全部通过。
4. DSS首次调② POST /api/export/task：
   {dmId, tables:[{db_audit.t_event_log,fields:[event,ts,user_id],partitions:[ds=2026-07-14],notifyUsers},{db_audit.t_dim_code,fields:[code_id,code_name],notifyUsers}], taskId:null}
5. DataGo返回 taskId=1024、status=inited。
6. DSS携带 taskId=1024 继续调②轮询，DataGo返回 status=detecting。
7. DataGo返回 status=detected_pass，DSS进入外发阶段。
8. DSS调③ POST /api/export/execute {dmId,optype:"table",notifyUsers,taskIds:[1024]}。
9. DataGo一次性完成：创建多维表格 + 写sheet + 向notifyUsers发飞书消息，返回 bitableUrl、sheets。
10. sheets 全部 success，节点执行成功（飞书通知已由③完成）。
```

### 6.2 参数不一致流程

```
1. 用户填写 dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts,phone。
2. ①返回DM单该表审批列为 user_id,event,ts。
3. DSS比对发现第1个外发目标 dt.01 字段超出DM单审批范围（越权字段: phone）。
4. 节点直接失败(82003)，不调②③。
```

### 6.3 optype非table流程

```
1. 用户填写 dmId=DM202607150002。
2. ①返回 optype=datago（报告外发）。
3. DSS比对发现 optype≠table。
4. 节点直接失败(82003)，提示该dm单为报告外发，请走现有 /api/report/*。
```

### 6.4 检测失败流程

```
1. DSS首次调②获取 taskId=1024。
2. DSS携带 taskId 轮询②。
3. DataGo返回 status=detected_fail，resultSummary="命中敏感表 t_xxx"。
4. 节点执行失败(82005)，DataGo已主动飞书通知，DSS不重复通知，不调③。
```

### 6.5 外发部分失败/重试流程

```
1. 检测通过后，DSS调③ execute。
2. DataGo返回 200，但 sheets 中 t_event_log.status=failed（飞书写入失败）。
3. DSS按重试策略再次调③ execute（taskIds 仍为 [1024]）。
4. 重试后成功 → 节点成功；重试耗尽仍失败 → 节点失败(82008)，已写入不回滚。
```

### 6.6 飞书不可达重试流程

```
1. 检测通过后，DSS调③ execute。
2. DataGo返回 502/504（飞书服务不可达）。
3. DSS按 executeRetryInterval 间隔重试（上限 executeRetryMax=3）。
4. 重试期间内成功 → 节点成功；重试耗尽仍 502/504 → 节点失败(82007)。
```

## 七、安全考虑

| 安全项 | 措施 |
|--------|------|
| 审批一致性 | 节点参数必须与①返回的DM单表单信息一致（`DmInfoComparator`） |
| optype控制 | 仅 optype=table 走本AppConn；optype=datago 拒绝并提示走报告外发 |
| 数据外发控制 | 检测通过（detected_pass）前不调③ execute |
| taskId控制 | 首次②传 taskId:null 创建，后续②与③均使用DataGo返回的唯一数值 taskId |
| notifyUsers控制 | 客户端按①可选 notifyUsers 子集校验，服务端③按 dmUser 子集校验（403越权） |
| 凭据控制 | DSS不持有飞书凭据，FS-*鉴权与飞书访问由DataGo经QZ区weproxy-nginx完成 |
| 服务间鉴权 | 内网网络层隔离；可选 token 经 `api.token`/`api.token.header` 追加鉴权头（空则不追加） |
| 日志脱敏 | 不打印原始数据内容，不打印凭据；响应体日志截断 2000 字符 |
| 失败感知 | detected_fail/detect_error 时 DataGo 已主动飞书通知，DSS 无需重复通知 |
| 失败处理 | 参数偏差、optype非法、检测失败、外发失败均导致节点失败 |

## 八、性能考虑

| 场景 | 影响 | 优化措施 |
|------|------|----------|
| 检测轮询 | 检测耗时较长时节点长期运行 | `LongTermRefExecutionOperation` 异步轮询；`nextPollAt` 节流；间隔10~30秒 |
| 外发执行 | 大数据量写入飞书耗时较长 | ③由DataGo分批写入同一sheet，DSS只发起一次调用并按需重试 |
| DataGo接口超时 | 可能导致节点失败 | 配置连接超时、读取超时和最大等待时间 |
| 飞书不可达 | ③返回502/504 | 按重试策略重试（execute.retry.max），413/409/500不重试 |
| 部分表失败 | ③部分sheet failed | 重试失败表，已写入不回滚，避免重复外发 |
| 多节点并发 | DataGo压力增加 | DSS侧控制轮询间隔，DataGo侧控制任务并发与检测批次（每批5000行） |
| 节点状态询问频率 | DSS 周期调 state() | `askStatePeriod` 取轮询/重试间隔较小值（下限1s），避免空转 |

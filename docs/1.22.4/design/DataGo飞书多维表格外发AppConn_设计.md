# DataGo飞书多维表格外发AppConn 设计文档

| 属性 | 值 |
|------|-----|
| 设计编号 | DES-DSS-1.23.0-003 |
| 关联需求 | REQ-DSS-1.23.0-003 |
| 版本 | dev-1.23.0 |
| 所属模块 | dss-datagofeishu-appconn |
| 参考实现 | dss-datachecker-appconn |
| 关联架构 | 数据外发飞书多维表格-架构设计.md |
| 对接契约 | [数据外发飞书多维表格_接口文档.md](./数据外发飞书多维表格_接口文档.md)（v1.1） |

## 一、设计概述

本设计在DSS工作流中新增一个类似 `datachecker` 的 AppConn：`datagofeishu`，用于编排BDAP数据外发到飞书多维表格的受控流程。用户开发工作流节点时填写DM单号、飞书通知人、库名、表名、字段名称和分区名；节点执行时，DSS通过DataGo提供的通用外发接口获取DM单审批信息，与用户填写的节点参数进行一致性比对，只有完全匹配后才继续执行检测与外发。

节点执行链路分为三个阶段（对齐接口文档 ①②③ 三个接口）：

1. **审批信息校验（① 表单获取）**：根据DM单号请求 `POST /api/export/form` 获取审批单涉及的 `optype` / `tables` / `columns` / `dmUser` / `status`，并与节点参数比对。仅 `optype=table` 时继续，否则节点失败。
2. **检测轮询（② 任务生成/状态查询）**：调用 `POST /api/export/task`。首次请求 `taskId` 传空创建检测任务，DataGo返回唯一 `taskId`（数值型）；后续请求携带该 `taskId` 轮询检测状态，直到 `detected_pass`（通过）/ `detected_fail`（不通过）/ `detect_error`（异常）。
3. **执行外发（③ 执行外发）**：检测通过后调用 `POST /api/export/execute`，传入 `dmId` / `optype=table` / `notifyUsers` / `taskIds`，DataGo **一次性完成** 创建飞书多维表格 + 写 sheet + 向 `notifyUsers` 发送飞书消息，返回多维表格 URL 与各 sheet 结果。

> **说明**：与旧设计相比，不再设置独立的"发送轮询"和"成功通知"阶段——③ `execute` 为同步一次性操作，外发与飞书通知由 DataGo 内部一并完成；检测不通过（`detected_fail`/`detect_error`）时 DataGo **已主动**经飞书通知用户，DSS 无需重复通知。

任一阶段出现参数不一致、optype 非法、检测失败、外发失败、超时或DataGo接口异常，节点均直接失败并抛出明确异常。

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
  NodeParamParser   DataGoFeishuClient   DataGoFeishuAction
          |                 |                 |
          v                 v                 v
  节点参数解析       DataGo接口调用       长轮询状态维护
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

| 项 | 设计 |
|----|------|
| AppConn名称 | `datagofeishu` |
| 节点类型 | `linkis.appconn.datagofeishu` |
| 节点名称 | DataGo飞书多维表格外发 |
| 实现模式 | 类似 `dss-datachecker-appconn`，实现 `LongTermRefExecutionOperation` |
| 调度方式 | DSS提交节点后异步执行，DSS周期调用 `state()` 获取状态 |
| 外部服务 | DataGo数据外发服务（`POST /api/export/*`，默认端口 3003） |
| 飞书链路 | DataGo经QZ区 `weproxy-nginx` 转发到OASF区飞书服务接口（FS-*鉴权由DataGo内部处理，DSS无需关心） |
| 对接范围 | 本期仅 `optype=table`（飞书多维表格外发）；`optype=datago`（报告外发）走现有 `/api/report/*`，本AppConn不处理 |

### 2.3 调用时序

```
DataGoFeishuRefExecutionOperation.submit(requestRef)
    |
    +--> 1. 解析节点参数
    |       dmId / notifyUsers / dataTargets(多库多表多字段多分区)
    |
    +--> 2. DataGoFeishuClient.queryExportForm(dmId)
    |       +--> ① POST /api/export/form
    |       +--> 获取 optype / tables / columns / dmUser / status
    |
    +--> 3. DmInfoComparator.compare(nodeParams, formData)
    |       +--> optype!=table / 表不在dm单 / 字段超范围 / notifyUsers越权 → 抛异常
    |
    +--> 4. 初始化DataGoFeishuAction
    |       +--> stage = DETECTING
    |       +--> taskId = null
    |
    +--> return AsyncExecutionResponseRef

DataGoFeishuRefExecutionOperation.state(action)
    |
    +--> stage == DETECTING
    |       +--> taskId 为空: ② POST /api/export/task {taskId:null} 创建 → 返回 taskId+status
    |       +--> taskId 非空: ② POST /api/export/task {taskId:xxx} 查询 → 返回 status
    |       +--> status=inited/detecting        → Running(继续轮询)
    |       +--> status=detected_pass           → stage = EXPORTING, Running
    |       +--> status=detected_fail/detect_error → 节点失败(DataGo已通知)
    |
    +--> stage == EXPORTING
            +--> ③ POST /api/export/execute {dmId,optype:"table",notifyUsers,taskIds:[taskId]}
            +--> 502/504(飞书不可达) → 按重试策略重试, Running
            +--> 413/409(超限/状态冲突) → 节点失败
            +--> 200 全部 sheet success → stage = SUCCESS, Success
            +--> 200 存在 failed sheet   → 重试失败表, 仍失败则节点失败(部分失败,已写入不回滚)
```

## 三、详细设计

### 3.1 工作流节点定义

**节点类型**: `linkis.appconn.datagofeishu`

**节点名称**: DataGo飞书多维表格外发

**节点参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dmId | String | 是 | DM审批单号（对应接口 `dmId`） |
| notifyUsers | String | 是 | 飞书通知人，多个用英文分号分隔（对应接口 `notifyUsers`，须为 dm 单 `dmUser` 子集） |
| dataTargets | Text | 是 | 外发目标（多库多表多字段多分区），行式 DSL，见下方格式 |
| detectPollInterval | Int | 否 | 检测轮询间隔，默认使用系统配置 |
| executeRetryMax | Int | 否 | 外发失败最大重试次数，默认使用系统配置 |
| maxWaitTime | Int | 否 | 最大等待时间，默认使用系统配置 |

**`dataTargets` DSL 格式**（参考 `job.desc` 行式范式）：

```
每行一个外发目标，行间用分号(;)或换行分隔；行内各段用 | 分隔。
dt.序号=db=库名|table=表名|fields=字段1,字段2|partition={分区值}
```

- 行首 `dt.序号=` 切出编号（序号用于去重与报错定位）；行内每段按**首个 `=`** 切 key/value。
- `db` / `table` / `fields` 必填，`fields` 多字段用英文逗号分隔。
- `partition` 段可选：值为 `{..}` 时取花括号内内容（保护分区值本身的 `=` / `/`，如多级分区 `{dt=20260716/country=us}`）；写 `partition=` 或省略该段表示非分区表。
- 同一 `(db, table)` 不允许重复，否则节点失败。

**示例（多库多表多字段）**：
```
dt.01=db=bdap_desensitized|table=customer_export_view|fields=cust_id,cust_name,amount|partition={dt=20260716};
dt.02=db=bdap_metrics|table=daily_kpi|fields=kpi_code,kpi_val|partition={dt=20260716/country=us};
dt.03=db=bdap_raw|table=dim_code|fields=code_id,code_name
```

**兼容回退**：若节点未填写 `dataTargets`，仍保留旧的 `dbName`/`tableName`/`fields`/`partition` 单值字段语义，自动包装为单元素目标，旧节点不破坏。

**设计决策**:


- 节点不允许用户填写飞书多维表格目标地址，实际目标以DM单和DataGo侧审批信息为准。
- 节点参数只作为执行意图表达，真正执行前必须与DataGo返回的DM单信息比对。
- 字段、通知人等集合类参数在比对前统一做 trim、去空、去重和排序，避免因顺序差异导致误判。
- 节点本期仅处理 `optype=table`；若DM单 `optype=datago`，节点直接失败并提示走现有报告外发。
- **单节点支持多库多表多字段多分区**：每个 `dataTargets` 行对应一个外发目标，逐目标与①审批表单比对，②任务接口按 `tables[]` 批量创建一次检测任务，③按 `taskIds` 批量外发为同一多维表格的多个 sheet。

### 3.2 AppConn结构设计

新增模块建议路径：

```
dss-appconn/appconns/dss-datagofeishu-appconn
    |
    +-- pom.xml
    +-- src/main/java/com/webank/wedatasphere/dss/appconn/datagofeishu
    |       +-- DataGoFeishuAppConn.java
    |       +-- DataGoFeishu.java
    |       +-- standard/DataGoFeishuDevelopmentStandard.java
    |       +-- client/DataGoFeishuClient.java
    |       +-- entity/NodeParams.java
    |       +-- entity/ExportForm.java
    |       +-- entity/ExportTable.java
    |       +-- entity/TaskResponse.java
    |       +-- entity/ExecuteResponse.java
    |       +-- entity/SheetResult.java
    |       +-- utils/DataGoFeishuHttpUtils.java
    |
    +-- src/main/scala/com/webank/wedatasphere/dss/appconn/datagofeishu
    |       +-- DataGoFeishuRefExecutionOperation.scala
    |       +-- DataGoFeishuExecutionAction.scala
    |
    +-- src/main/resources
            +-- init.sql
            +-- appconn.properties
            +-- log4j2.xml
```

| 类 | 职责 |
|----|------|
| `DataGoFeishuAppConn` | 注册DevelopmentIntegrationStandard |
| `DataGoFeishuDevelopmentStandard` | 提供RefExecutionOperation |
| `DataGoFeishuRefExecutionOperation` | 节点执行编排，负责submit/state/result/kill/progress/log |
| `DataGoFeishuExecutionAction` | 保存执行状态、taskId、阶段、轮询次数、外发重试次数、异常信息 |
| `DataGoFeishuClient` | 封装DataGo接口调用（queryExportForm / createOrQueryTask / executeExport） |
| `NodeParams` | 节点参数模型 |
| `ExportForm` / `ExportTable` | ① 表单响应模型（dmId/dmTitle/dmUser/optype/tables/status） |
| `TaskResponse` | ② 任务响应模型（taskId/optype/status/resultSummary） |
| `ExecuteResponse` / `SheetResult` | ③ 外发响应模型（bitableName/bitableUrl/sheets[]/notifyUsers/exportedAt） |
| `DataGoFeishuHttpUtils` | HTTP请求、统一响应解析、超时和重试 |

### 3.3 DataGo接口设计

> 本节接口契约严格对齐 [接口文档 v1.1](./数据外发飞书多维表格_接口文档.md)。Base URL：`http://{DataGoHost}:{DataGoPort}`（默认端口 `3003`）；`Content-Type: application/json; charset=utf-8`；统一响应结构 `{success, code, message, data}`。

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

dm单不存在时返回 `{success:true, code:200, message:"无数据", data:null}`。

**校验规则**:

- `optype` 必须为 `table`，否则节点失败（提示走现有报告外发）。
- `status` 为终态失败（`detected_fail`/`detect_error`）时节点直接失败；`exported` 时提示已外发。
- **逐外发目标命中**：`dataTargets` 中每个目标 `(db, table)` 必须命中 `data.tables` 中的某张表；未命中时提示"第 N 个外发目标库表 … 不在DM单审批范围内"。
- 每个目标的 `fields` 必须与该表 `columns` 完全一致（默认；接口支持子集）。
- 每个目标的 `partition` 必须在该表分区范围内（若dm单限定分区）。
- 节点填写的 `notifyUsers` 必须是 `dmUser` 的子集。

#### 3.3.2 ② 外发任务生成 / 状态查询 — `POST /api/export/task`

**用途**: **同一接口，两种语义**，由 `taskId` 是否为空区分：
- `taskId` 为空/null → **创建**检测任务（optype 继承自dm单），返回 `taskId`
- `taskId` 非空 → **查询**该任务当前状态

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| dmId | string | 是 | DM审批单号（须在①中有效） |
| tables | array | 是 | 外发库表列表（每表内嵌其字段/分区，由节点 `dataTargets` 逐行映射） |
| tables[].dbName | string | 是 | 库名 |
| tables[].tableName | string | 是 | 表名 |
| tables[].fields | string[] | 是 | 该表检测/外发字段（列名，与①该表 `columns` 对应） |
| tables[].partition | string | 否 | 分区（非分区表不传） |
| taskId | number \| null | 否 | **空/null=创建；非空=查询** |

**创建请求示例**（taskId 传空，多表）：
```json
{
  "dmId": "DM202607150001",
  "tables": [
    { "dbName": "db_audit", "tableName": "t_event_log", "fields": ["user_id", "event", "ts"], "partition": "ds=2026-07-14" },
    { "dbName": "db_audit", "tableName": "t_dim_code", "fields": ["code_id", "code_name"] }
  ],
  "taskId": null
}
```

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
| `detected_fail` | 检测不通过（命中敏感） | 是 | 流程终止，DataGo已飞书通知 |
| `detect_error` | 检测异常（已自动重试2次仍失败） | 是 | 流程终止，DataGo已飞书通知 |
| `exported` | 已外发 | 是 | 流程完成 |

**调用规则**:

- 第一次调用 `taskId` 传 null，DataGo创建检测任务后返回唯一 `taskId`（数值型），DSS保存。
- 后续轮询必须传入上一次返回的 `taskId`。
- `inited`/`detecting` 继续轮询；`detected_pass` 进入外发阶段；`detected_fail`/`detect_error` 节点失败。
- 建议轮询间隔 10~30 秒（检测按表分批，每批 ≤5000 行）。
- **创建幂等**：同一dm单若已有进行中任务，建议先查再决定是否创建，避免重复（依赖DataGo侧创建幂等）。

**错误响应**:

| code | message 示例 | 触发 |
|:----:|-------------|------|
| 400 | `dmId 参数为必填项` | dmId 缺失 |
| 403 | `dm 单无效或非 table 类型` | dm单不在权限表 / optype≠table / 已过期 |
| 404 | `任务不存在: taskId=9999` | 查询模式下 taskId 不存在 |

> 本期②仅支持 optype=table。dm单 optype=datago 时返回 403。

#### 3.3.3 ③ 执行外发 — `POST /api/export/execute`

**用途**: 当dm单对应任务检测通过（`detected_pass`）后，批量外发。optype=table 时 DataGo **一次性完成**：创建飞书多维表格 + 写 sheet + 向 `notifyUsers` 发送飞书消息（含多维表格URL）。

**外发结果**（optype=table）：
- 一个dm单 → 创建一个飞书多维表格，命名 `{dmId}--{表数量}--{时间戳}`
- 每个表 → 一个 sheet；单表过大时分多次请求追加写入同一 sheet
- 外发成功同时向 `notifyUsers` 发送飞书消息

**请求体**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|:----:|------|
| dmId | string | 是 | DM审批单号 |
| optype | string | 是 | 外发对象类型；**本期仅 `table`**（`datago` → 409） |
| notifyUsers | string[] | 是 | 外发结果通知用户；为空报错；且必须是dm单检测用户（dmUser）的子集，否则报错 |
| taskIds | number[] | 否 | 指定导出的任务；不传则导出该dm单下所有 `detected_pass` 任务 |

**请求示例**：
```json
{
  "dmId": "DM202607150001",
  "optype": "table",
  "notifyUsers": ["alexyang", "ryanchen"],
  "taskIds": [1024]
}
```

> 本设计中节点传入 `taskIds=[本节点检测taskId]`，仅导出本节点检测通过的那张表，避免误导出同dm单其他任务。

**响应字段**（成功）:

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

> **部分失败**：若批量中某表失败，**已写入的表不回滚**，`sheets[].status` 标记各表结果，整体仍返回响应并飞书通知；DSS可据 `sheets` 重试失败表。本设计单节点单表场景下，该 sheet 失败即按重试策略重试，仍失败则节点失败。

**错误响应**:

| code | message 示例 | 触发 | DSS处理 |
|:----:|-------------|------|---------|
| 400 | `optype 参数为必填项` / `notifyUsers 不能为空` | optype缺失/notifyUsers为空 | 节点失败(82001) |
| 403 | `notifyUsers 必须是 dm 单检测用户的子集，越权用户: [xxx]` | notifyUsers含dmUser之外账号 | 节点失败(82003) |
| 409 | `optype=datago 本期本接口不支持，请走 /api/report/*` | optype=datago | 节点失败(82009) |
| 409 | `存在未检测通过的任务: [taskId=1025 status=detected_fail]` | 指定任务未全通过 | 节点失败(82009) |
| 413 | `表 t_xxx 超出大小限制(估算 62MB, 上限 50MB)` | 单sheet估算超50MB | 节点失败(82009)，不可重试 |
| 502/504 | `飞书服务不可达，请稍后重试` | weproxy/飞书不可达 | 按重试策略重试(82007) |
| 500 | 服务内部错误 | DataGo内部异常 | 节点失败(82007) |

### 3.4 DM信息比对设计

**文件**: `DmInfoComparator.java`（新增）

**职责**: 对①返回的DM单表单信息和用户填写的节点参数进行一致性比对。

| 比对项 | 比对规则 | 失败提示 |
|--------|----------|----------|
| DM单存在性 | ①返回 data 非空 | DM单不存在或无审批信息 |
| optype | 必须为 `table` | DM单optype非table，请走现有报告外发 |
| DM单状态 | status 非 `detected_fail`/`detect_error`/`exported` 终态失败 | DM单检测未通过/已异常/已外发 |
| 库名+表名 | 节点(dbName,tableName) 必须命中 tables 列表 | 节点库表与DM单不一致 |
| 字段名称 | 节点 fields 集合必须与该表 columns 一致（默认完全一致） | 节点字段与DM单审批字段不一致 |
| 分区名 | 节点 partition 必须在该表分区范围内 | 节点分区不在DM单审批范围内 |
| 飞书通知人 | 节点 notifyUsers 必须是 dmUser 的子集 | 飞书通知人超出DM单授权范围 |

**字段规范化**:

```scala
normalizeList(value)
    .split(",|;")
    .map(_.trim)
    .filter(_.nonEmpty)
    .distinct
    .sorted
```

### 3.5 长轮询状态机设计

**文件**: `DataGoFeishuExecutionAction.scala`（新增）

**执行阶段**:

| 阶段 | 含义 | 下一阶段 |
|------|------|----------|
| INIT | 参数解析与①表单获取/比对 | DETECTING / FAILED |
| DETECTING | 调用②任务接口（创建+轮询） | EXPORTING / FAILED |
| EXPORTING | 调用③执行外发（一次性，含飞书通知） | SUCCESS / FAILED |
| SUCCESS | 节点成功 | 结束 |
| FAILED | 节点失败 | 结束 |
| KILLED | 节点被杀死 | 结束 |

**状态机逻辑**:

```scala
state(action):
    if action.stage == DETECTING:
        if action.taskId is null:
            // 创建检测任务
            response = client.createOrQueryTask(
              dmId, tables, fields, partitions, taskId=null)
            action.taskId = response.taskId   // number
        else:
            // 查询检测状态
            response = client.createOrQueryTask(
              dmId, tables, fields, partitions, taskId=action.taskId)
        switch response.status:
            case "inited" | "detecting":
                return Running                  // 继续轮询
            case "detected_pass":
                action.stage = EXPORTING
                return Running
            case "detected_fail":
                fail(82005, response.resultSummary)   // DataGo已通知
            case "detect_error":
                fail(82006, response.resultSummary)   // DataGo已通知
            case "exported":
                action.stage = SUCCESS
                return Success

    if action.stage == EXPORTING:
        response = client.executeExport(
          dmId, optype="table", notifyUsers, taskIds=[action.taskId])
        // execute 同步返回最终结果
        if response.code in (502, 504):        // 飞书不可达,可重试
            if action.executeRetry < executeRetryMax:
                action.executeRetry += 1
                return Running                  // 下次轮询重试
            else:
                fail(82007, "外发重试次数耗尽")
        if response.code == 413 or 409:        // 超限/状态冲突,不可重试
            fail(82009, response.message)
        if response.code == 200:
            if any sheet.status == "failed":   // 部分失败
                if action.executeRetry < executeRetryMax:
                    action.executeRetry += 1
                    return Running              // 重试失败表
                else:
                    fail(82008, "部分表外发失败,已写入不回滚")
            else:
                action.bitableUrl = response.bitableUrl
                action.stage = SUCCESS
                return Success
        else:                                  // 500等其他错误
            fail(82007, response.message)
```

**轮询控制**:

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| detect.poll.interval.ms | 30000 | ②检测状态轮询间隔（接口建议10~30秒） |
| execute.retry.max | 3 | ③外发失败最大重试次数 |
| execute.retry.interval.ms | 30000 | ③外发重试间隔 |
| max.wait.time.ms | 7200000 | 节点最大等待时间 |
| http.connect.timeout.ms | 10000 | DataGo接口连接超时 |
| http.read.timeout.ms | 60000 | DataGo接口读取超时 |

### 3.6 异常处理设计

| 场景 | 接口表现 | 处理 / 异常码 |
|------|----------|------|
| 节点参数缺失或格式错误 | — | 节点失败，82001 |
| ①表单接口调用失败/返回404(dm单不存在) | ①失败/404 | 节点失败，82002 |
| DM单optype≠table | ①optype=datago | 节点失败，82003（提示走报告外发） |
| DM单信息与节点参数不一致 | ①比对偏差 | 节点失败，82003（提示具体不一致字段） |
| ②任务接口调用失败 | ②400/500等 | 节点失败，82004 |
| 首次检测未返回taskId | ②data.taskId缺失 | 节点失败，82004（提示DataGo未返回任务ID） |
| 检测不通过（命中敏感） | ②status=detected_fail | 节点失败，82005（DataGo已飞书通知，DSS无需重复通知） |
| 检测异常 | ②status=detect_error | 节点失败，82006（DataGo已飞书通知） |
| 检测轮询超时 | ②长时间非终态 | 节点失败，82006（提示检测超时） |
| ③外发接口调用失败(飞书不可达) | ③502/504 | 按重试策略重试，耗尽则82007 |
| ③外发接口内部错误 | ③500 | 节点失败，82007 |
| ③外发部分表失败 | ③200但sheets含failed | 重试失败表，耗尽则82008（已写入不回滚） |
| ③外发超限/状态冲突/optype不支持 | ③413/409 | 节点失败，82009（不可重试） |
| notifyUsers越权 | ③403 | 节点失败，82003 |
| 节点被kill | — | 设置状态为KILLED，不再继续轮询DataGo |

### 3.7 日志与审计

**日志记录**:

- 记录dmId、optype、库名、表名、字段数量、分区、通知人数、taskId、当前阶段和DataGo返回状态。
- 记录③返回的 bitableName / bitableUrl / sheets 各表行数与状态。
- 不记录DataGo返回的原始数据内容。
- 飞书通知人可记录账号标识，但不记录飞书凭据。

**审计字段**:

| 字段 | 说明 |
|------|------|
| executionId | DSS执行ID |
| nodeId | DSS节点ID |
| user | 执行用户 |
| dmId | DM单号 |
| optype | 外发对象类型 |
| taskId | DataGo检测任务ID（数值） |
| dbName/tableName | 库表 |
| fields | 字段列表 |
| partition | 分区 |
| notifyUsers | 飞书通知人 |
| detectStatus | 检测状态 |
| exportStatus | 外发状态 |
| bitableUrl | 多维表格URL |
| sheetsSummary | 各sheet结果摘要 |
| errorMessage | 失败原因 |

## 四、数据模型

### 4.1 节点参数模型

```json
{
  "dmId": "DM202607150001",
  "notifyUsers": "alexyang;ryanchen",
  "dbName": "db_audit",
  "tableName": "t_event_log",
  "fields": "user_id,event,ts",
  "partition": "ds=2026-07-14",
  "detectPollInterval": 30000,
  "executeRetryMax": 3,
  "maxWaitTime": 7200000
}
```

### 4.2 配置数据模型

| 配置键 | CommonVars变量 | 类型 | 默认值 |
|--------|----------------|------|--------|
| wds.dss.appconn.datago.feishu.api.base.url | DATAGO_FEISHU_API_BASE_URL | String | ""（如 `http://host:3003`） |
| wds.dss.appconn.datago.feishu.detect.poll.interval.ms | DETECT_POLL_INTERVAL_MS | Int | 30000 |
| wds.dss.appconn.datago.feishu.execute.retry.max | EXECUTE_RETRY_MAX | Int | 3 |
| wds.dss.appconn.datago.feishu.execute.retry.interval.ms | EXECUTE_RETRY_INTERVAL_MS | Int | 30000 |
| wds.dss.appconn.datago.feishu.max.wait.time.ms | MAX_WAIT_TIME_MS | Int | 7200000 |
| wds.dss.appconn.datago.feishu.http.connect.timeout.ms | HTTP_CONNECT_TIMEOUT_MS | Int | 10000 |
| wds.dss.appconn.datago.feishu.http.read.timeout.ms | HTTP_READ_TIMEOUT_MS | Int | 60000 |

### 4.3 异常码分配

| 异常码 | 含义 | 对应接口场景 |
|--------|------|------|
| 82001 | 节点参数缺失或格式错误 | dmId/notifyUsers/dbName等缺失或格式错误 |
| 82002 | DataGo表单接口(①)调用失败 | ① form 调用异常或返回404(dm单不存在) |
| 82003 | DM单信息与节点参数不一致 | optype≠table、表不在dm单、字段超范围、notifyUsers越权 |
| 82004 | DataGo任务接口(②)调用失败 | ② task 创建/查询调用异常或未返回taskId |
| 82005 | DataGo检测不通过 | ② status=detected_fail（DataGo已通知） |
| 82006 | DataGo检测异常或轮询超时 | ② status=detect_error 或超过maxWaitTime |
| 82007 | DataGo执行外发接口(③)调用失败 | ③ execute 调用异常(502/504/500)且重试耗尽 |
| 82008 | DataGo外发部分表失败 | ③ sheets存在failed且重试耗尽（已写入不回滚） |
| 82009 | DataGo外发超限或状态冲突 | ③ 413(超50MB) / 409(未通过/optype不支持) |

## 五、部署与配置

### 5.1 init.sql设计

参考 `dss-datachecker-appconn` 的初始化方式，`init.sql` 需要同时初始化 AppConn 元信息、AppConn 实例、工作流节点、节点所属分组、节点属性 UI，以及节点和属性 UI 的绑定关系。节点运行依赖的DataGo地址（含端口3003）、轮询间隔、超时时间等作为实例级配置写入 `dss_appconn_instance.enhance_json`，节点开发时需要填写的DM单号、飞书通知人、库名、表名、字段名称、分区名等属性写入 `dss_workflow_node_ui` 并绑定到 `dss_workflow_node_to_ui`。

```sql
-- 1. 初始化AppConn元信息
select @datago_feishu_appconnId:=id
from `dss_appconn`
where `appconn_name` = 'datagofeishu';

delete from `dss_appconn_instance`
where `appconn_id` = @datago_feishu_appconnId;

delete from `dss_appconn`
where `appconn_name` = 'datagofeishu';

INSERT INTO `dss_appconn`
(`appconn_name`, `is_user_need_init`, `level`, `if_iframe`, `is_external`, `reference`, `class_name`, `appconn_class_path`, `resource`)
VALUES
('datagofeishu', 0, 1, 1, 1, NULL,
 'com.webank.wedatasphere.dss.appconn.datagofeishu.DataGoFeishuAppConn',
 'DSS_INSTALL_HOME_VAL/dss-appconns/datagofeishu', '');

select @datago_feishu_appconnId:=id
from `dss_appconn`
where `appconn_name` = 'datagofeishu';

-- 2. 初始化AppConn实例配置
INSERT INTO `dss_appconn_instance`
(`appconn_id`, `label`, `url`, `enhance_json`, `homepage_uri`)
VALUES
(@datago_feishu_appconnId,
 'DEV',
 'datagofeishu',
 '{
   "wds.dss.appconn.datago.feishu.api.base.url":"DATAGO_FEISHU_API_BASE_URL",
   "wds.dss.appconn.datago.feishu.detect.poll.interval.ms":"30000",
   "wds.dss.appconn.datago.feishu.execute.retry.max":"3",
   "wds.dss.appconn.datago.feishu.execute.retry.interval.ms":"30000",
   "wds.dss.appconn.datago.feishu.max.wait.time.ms":"7200000",
   "wds.dss.appconn.datago.feishu.http.connect.timeout.ms":"10000",
   "wds.dss.appconn.datago.feishu.http.read.timeout.ms":"60000"
 }',
 '');

-- 3. 初始化工作流节点定义
delete from `dss_workflow_node`
where `appconn_name` = 'datagofeishu';

INSERT INTO `dss_workflow_node`
(`name`, `appconn_name`, `node_type`, `jump_type`, `support_jump`, `submit_to_scheduler`, `enable_copy`, `should_creation_before_node`, `icon_path`)
VALUES
('datagofeishu', 'datagofeishu', 'linkis.appconn.datagofeishu', '0', '0', '1', '1', '0', 'icons/datagofeishu.icon');

select @datago_feishu_nodeId:=id
from `dss_workflow_node`
where `node_type` = 'linkis.appconn.datagofeishu';

-- 4. 绑定节点所属分组
delete from `dss_workflow_node_to_group`
where `node_id` = @datago_feishu_nodeId;

select @datago_feishu_node_groupId:=id
from `dss_workflow_node_group`
where `name` = '信号节点';

INSERT INTO `dss_workflow_node_to_group`
(`node_id`, `group_id`)
VALUES
(@datago_feishu_nodeId, @datago_feishu_node_groupId);

-- 5. 初始化节点属性UI
-- 公共属性：节点名、节点描述、业务标签、应用标签、是否复用引擎复用已有定义
select @ui_node_name:=id from `dss_workflow_node_ui` where `lable_name` = '节点名' limit 1;
select @ui_node_desc:=id from `dss_workflow_node_ui` where `lable_name` = '节点描述' limit 1;
select @ui_business_tag:=id from `dss_workflow_node_ui` where `lable_name` = '业务标签' limit 1;
select @ui_app_tag:=id from `dss_workflow_node_ui` where `lable_name` = '应用标签' limit 1;
select @ui_reuse_engine:=id from `dss_workflow_node_ui` where `lable_name` = '是否复用引擎' limit 1;

-- DataGo飞书多维表格外发节点专属属性
delete from `dss_workflow_node_ui`
where `lable_name` in (
  'DM单号',
  '飞书通知人',
  '库名',
  '表名',
  '字段名称',
  '分区名',
  '检测轮询间隔',
  '外发重试次数',
  '最大等待时间'
);

INSERT INTO `dss_workflow_node_ui`
(`lable_name`, `ui_type`, `required`, `key`, `value`, `description`)
VALUES
('DM单号', 'input', 1, 'dmId', '', 'DataGo侧DM审批单号');

INSERT INTO `dss_workflow_node_ui`
(`lable_name`, `ui_type`, `required`, `key`, `value`, `description`)
VALUES
('飞书通知人', 'input', 1, 'notifyUsers', '', '飞书通知人，多个接收人使用英文分号分隔，须为DM单检测用户子集');

INSERT INTO `dss_workflow_node_ui`
(`lable_name`, `ui_type`, `required`, `key`, `value`, `description`)
VALUES
('库名', 'input', 1, 'dbName', '', '需要外发数据所在库名');

INSERT INTO `dss_workflow_node_ui`
(`lable_name`, `ui_type`, `required`, `key`, `value`, `description`)
VALUES
('表名', 'input', 1, 'tableName', '', '需要外发数据所在表名');

INSERT INTO `dss_workflow_node_ui`
(`lable_name`, `ui_type`, `required`, `key`, `value`, `description`)
VALUES
('字段名称', 'textarea', 1, 'fields', '', '需要外发的字段名称，多个字段使用英文逗号分隔');

INSERT INTO `dss_workflow_node_ui`
(`lable_name`, `ui_type`, `required`, `key`, `value`, `description`)
VALUES
('分区名', 'input', 0, 'partition', '', '需要外发数据所在分区名');

INSERT INTO `dss_workflow_node_ui`
(`lable_name`, `ui_type`, `required`, `key`, `value`, `description`)
VALUES
('检测轮询间隔', 'input', 0, 'detectPollInterval', '', '检测状态轮询间隔，单位毫秒，未填写时使用实例配置（建议10~30秒）');

INSERT INTO `dss_workflow_node_ui`
(`lable_name`, `ui_type`, `required`, `key`, `value`, `description`)
VALUES
('外发重试次数', 'input', 0, 'executeRetryMax', '', '外发失败最大重试次数，未填写时使用实例配置');

INSERT INTO `dss_workflow_node_ui`
(`lable_name`, `ui_type`, `required`, `key`, `value`, `description`)
VALUES
('最大等待时间', 'input', 0, 'maxWaitTime', '', '节点最大等待时间，单位毫秒，未填写时使用实例配置');

select @ui_dm_id:=id from `dss_workflow_node_ui` where `key` = 'dmId' limit 1;
select @ui_notify_users:=id from `dss_workflow_node_ui` where `key` = 'notifyUsers' limit 1;
select @ui_db_name:=id from `dss_workflow_node_ui` where `key` = 'dbName' limit 1;
select @ui_table_name:=id from `dss_workflow_node_ui` where `key` = 'tableName' limit 1;
select @ui_fields:=id from `dss_workflow_node_ui` where `key` = 'fields' limit 1;
select @ui_partition:=id from `dss_workflow_node_ui` where `key` = 'partition' limit 1;
select @ui_detect_poll_interval:=id from `dss_workflow_node_ui` where `key` = 'detectPollInterval' limit 1;
select @ui_execute_retry_max:=id from `dss_workflow_node_ui` where `key` = 'executeRetryMax' limit 1;
select @ui_max_wait_time:=id from `dss_workflow_node_ui` where `key` = 'maxWaitTime' limit 1;

-- 6. 绑定节点与属性UI
delete from `dss_workflow_node_to_ui`
where `workflow_node_id` = @datago_feishu_nodeId;

INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`, `ui_id`) VALUES (@datago_feishu_nodeId, @ui_node_name);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`, `ui_id`) VALUES (@datago_feishu_nodeId, @ui_node_desc);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`, `ui_id`) VALUES (@datago_feishu_nodeId, @ui_business_tag);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`, `ui_id`) VALUES (@datago_feishu_nodeId, @ui_app_tag);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`, `ui_id`) VALUES (@datago_feishu_nodeId, @ui_reuse_engine);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`, `ui_id`) VALUES (@datago_feishu_nodeId, @ui_dm_id);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`, `ui_id`) VALUES (@datago_feishu_nodeId, @ui_notify_users);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`, `ui_id`) VALUES (@datago_feishu_nodeId, @ui_db_name);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`, `ui_id`) VALUES (@datago_feishu_nodeId, @ui_table_name);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`, `ui_id`) VALUES (@datago_feishu_nodeId, @ui_fields);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`, `ui_id`) VALUES (@datago_feishu_nodeId, @ui_partition);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`, `ui_id`) VALUES (@datago_feishu_nodeId, @ui_detect_poll_interval);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`, `ui_id`) VALUES (@datago_feishu_nodeId, @ui_execute_retry_max);
INSERT INTO `dss_workflow_node_to_ui`(`workflow_node_id`, `ui_id`) VALUES (@datago_feishu_nodeId, @ui_max_wait_time);
```

**说明**:

- `dss_appconn_instance.enhance_json` 保存实例级运行配置，执行时由 `this.service.getAppInstance.getConfig` 读取，与 `datachecker` 的配置读取方式保持一致。`api.base.url` 需填写 DataGo 地址（含默认端口3003，如 `http://datago-host:3003`）。
- `dss_workflow_node_ui` 中的专属属性用于工作流节点开发页面展示，用户填写后进入 `runtimeMap` 或 `job.desc`，由 `DataGoFeishuRefExecutionOperation` 解析。
- 上述 `dss_workflow_node_ui` 字段名按设计表达，实际落库时需以当前版本表结构为准。如果当前表结构使用 `label_name`、`input_type`、`id` 等不同字段名，实施时按现有DDL做字段映射。
- 公共属性复用已有UI定义，专属属性由本AppConn初始化脚本创建并绑定。
- 若生产环境为升级安装，不能直接删除 `dss_appconn` 导致 `appconn_id` 变化，应改为 `update/insert ignore` 或版本升级脚本方式处理。
### 5.2 appconn.properties配置

```properties
# DataGo飞书多维表格外发接口地址（含端口，默认3003）
wds.dss.appconn.datago.feishu.api.base.url=http://datago-host:3003

# 轮询与超时配置
wds.dss.appconn.datago.feishu.detect.poll.interval.ms=30000
wds.dss.appconn.datago.feishu.execute.retry.max=3
wds.dss.appconn.datago.feishu.execute.retry.interval.ms=30000
wds.dss.appconn.datago.feishu.max.wait.time.ms=7200000
wds.dss.appconn.datago.feishu.http.connect.timeout.ms=10000
wds.dss.appconn.datago.feishu.http.read.timeout.ms=60000
```

### 5.3 工作流节点配置

在DSS工作流节点参数面板中新增以下输入项：

| UI标签 | 参数名 | 控件类型 | 校验 |
|--------|--------|----------|------|
| DM单号 | dmId | 文本框 | 必填 |
| 飞书通知人 | notifyUsers | 文本框 | 必填，分号分隔，须为DM单检测用户子集 |
| 库名 | dbName | 文本框 | 必填 |
| 表名 | tableName | 文本框 | 必填 |
| 字段名称 | fields | 文本域 | 必填，逗号分隔 |
| 分区名 | partition | 文本框 | 选填 |

## 六、执行示例

### 6.1 正常流程

```
1. 用户配置节点：
   dmId=DM202607150001
   notifyUsers=alexyang;ryanchen
   dbName=db_audit
   tableName=t_event_log
   fields=user_id,event,ts
   partition=ds=2026-07-14

2. DSS调① POST /api/export/form {dmId}，拿到 optype=table、tables、columns、dmUser=alexyang、status。
3. DSS比对节点参数与表单：optype=table、表命中、fields与columns一致、notifyUsers⊆dmUser，全部通过。
4. DSS首次调② POST /api/export/task {dmId,tables,fields,partitions,taskId:null}。
5. DataGo返回 taskId=1024、status=inited。
6. DSS携带 taskId=1024 继续调②轮询，DataGo返回 status=detecting。
7. DataGo返回 status=detected_pass，DSS进入外发阶段。
8. DSS调③ POST /api/export/execute {dmId,optype:"table",notifyUsers,taskIds:[1024]}。
9. DataGo一次性完成：创建多维表格 + 写sheet + 向notifyUsers发飞书消息，返回 bitableUrl、sheets。
10. sheets 全部 success，节点执行成功（飞书通知已由③完成）。
```

### 6.2 参数不一致流程

```
1. 用户填写 fields=user_id,event,ts,phone。
2. ①返回DM单该表审批列为 user_id,event,ts。
3. DSS比对发现节点字段超出DM单审批范围。
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

## 七、安全考虑

| 安全项 | 措施 |
|--------|------|
| 审批一致性 | 节点参数必须与①返回的DM单表单信息一致 |
| optype控制 | 仅 optype=table 走本AppConn；optype=datago 拒绝并提示走报告外发 |
| 数据外发控制 | 检测通过（detected_pass）前不调③ execute |
| taskId控制 | 首次②传 taskId:null 创建，后续②与③均使用DataGo返回的唯一数值 taskId |
| notifyUsers控制 | notifyUsers 必须是 dm 单检测用户（dmUser）的子集，越权报403 |
| 凭据控制 | DSS不持有飞书凭据，FS-*鉴权与飞书访问由DataGo经QZ区weproxy-nginx完成 |
| 日志脱敏 | 不打印原始数据内容，不打印凭据 |
| 失败感知 | detected_fail/detect_error 时 DataGo 已主动飞书通知，DSS 无需重复通知 |
| 失败处理 | 参数偏差、optype非法、检测失败、外发失败均导致节点失败 |

## 八、性能考虑

| 场景 | 影响 | 优化措施 |
|------|------|----------|
| 检测轮询 | 检测耗时较长时节点长期运行 | 使用LongTermRefExecutionOperation异步轮询；间隔10~30秒 |
| 外发执行 | 大数据量写入飞书耗时较长 | ③由DataGo分批写入同一sheet，DSS只发起一次调用并按需重试 |
| DataGo接口超时 | 可能导致节点失败 | 配置连接超时、读取超时和最大等待时间 |
| 飞书不可达 | ③返回502/504 | 按重试策略重试（execute.retry.max），413/409不重试 |
| 部分表失败 | ③部分sheet failed | 重试失败表，已写入不回滚，避免重复外发 |
| 多节点并发 | DataGo压力增加 | DSS侧控制轮询间隔，DataGo侧控制任务并发与检测批次（每批5000行） |

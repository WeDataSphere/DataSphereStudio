# sendfeishu 节点 测试用例

## 一、概述

### 1.1 被测对象

本文档针对 DSS 工作流中的 **sendfeishu 节点**（DataGo 飞书多维表格外发节点）编写测试用例。该节点作为编排方接入 DataGo 通用外发接口（`POST /api/export/*`，默认端口 3003），将"DM 审批校验 → 检测 → 外发"封装为一个工作流节点，纳入 DSS 工作流统一调度。

节点执行三阶段状态机：`INIT(参数解析+①表单校验) → DETECTING(②任务创建/轮询) → EXPORTING(③执行外发) → SUCCESS`，任一阶段失败即终止。

### 1.2 命名与标识说明（重要）

AppConn 英文标识为 `datagofeishu`，节点 Palette 显示名（`dss_workflow_node.name`）为 `sendfeishu`。各维度实际取值如下，测试与排查均以此为准：

| 维度 | 值 | 备注 |
|------|-----|------|
| appconn_name | `datagofeishu` | `dss_appconn.appconn_name` |
| node_type | `linkis.appconn.datagofeishu` | `dss_workflow_node.node_type` |
| 节点显示名（DB name） | `sendfeishu` | `dss_workflow_node.name`，节点 Palette 显示名 |
| 节点所属分组 | `数据输出` | `dss_workflow_node_group.name` |
| 节点图标 | `svgs/sendfeishu-node.svg` | `dss_workflow_node.icon_path` |
| 配置键前缀 | `wds.dss.appconn.datago.feishu.*` | key 名保留 `.ms` 后缀但值为秒 |
| Maven 模块 | `dss-datago-feishu-appconn` | |
| 包名 | `com.webank.wedatasphere.dss.appconn.datagofeishu` | |
| 入口类 | `com.webank.wedatasphere.dss.appconn.datagofeishu.DataGoFeishuAppConn` | |
| 节点中文显示名 | DataGo飞书多维表格外发 | DataGo 是外部系统名，保留不改 |
| DataGo 外部系统 | DataGo | 不改 |

> ⚠️ 说明：**不存在 `datagofeishu → sendfeishu` 的整体重命名**。`sendfeishu` 仅作为节点 Palette 显示名与图标文件名出现；AppConn 名称、节点类型、配置键前缀、模块目录、包名、入口类均为 `datagofeishu`/`datago.feishu`。本文涉及的需求编号（AC/FR/US）与异常码（82001~82009，另含防御性 82011）均沿用原需求体系。

### 1.3 当前代码状态基线

测试用例以**当前代码与 `init.sql` 实际状态**为准：

- 节点 UI 专属属性仅：`dmId` / `notifyUsers` / `dataTargets`（均必填）；**`maxWaitTime` 不暴露为节点 UI 属性**
- `detectPollInterval` / `executeRetryMax` / `executeRetryInterval` / `maxWaitTime` 均不暴露为节点 UI，仅走实例级 `enhance_json` 配置（代码 `positiveLong`/`positiveInt` 的 nodeKey 传空串或不注册 UI，故实际取实例级）
- 时间参数（`detectPollInterval` / `executeRetryInterval` / `maxWaitTime`）单位为**秒**，代码内部 ×1000 转毫秒
- 配置键 key 名保留 `.ms` 后缀但值为秒；HTTP 超时（connect/read）仍为毫秒
- 参数优先级（代码实现）：节点级 UI 值 > 实例级 `enhance_json` 配置 > 硬编码默认值；当前 UI 未暴露上述节奏参数，故均回落至实例级/默认
- DM 比对阶段：`DmInfoComparator.validate()` 校验 optype/状态/逐目标库表命中/字段子集/通知人授权；**分区范围校验 `validatePartition` 已实现但当前未调用**，分区一致性交由 ②③检测阶段以实际分区为准
- ② 任务接口请求体（v1.7/v1.8 起）：`fields`/`partitions`/`notifyUsers` 全部内嵌于每个 `tables[]` 元素，外层不再有这些参数；`partitions` 为每表 `string[]`（同 db/table 多分区合并为同一元素，写入同一 sheet）；每个 `tables[]` 内嵌 `dbName`/`tableName`/`fields`/`partitions`(非空才传)/`notifyUsers`

### 1.4 外部依赖

| 依赖 | 说明 |
|------|------|
| DataGo 数据外发服务 | 默认端口 3003，提供 `POST /api/export/form`、`POST /api/export/task`、`POST /api/export/execute` 三个接口 |
| 飞书链路 | DataGo 经 QZ 区 weproxy-nginx 转发到 OASF 区飞书服务（FS-* 鉴权由 DataGo 内部处理，DSS 无需关心） |
| DM 审批单 | DataGo 侧审批单，确定外发范围（库表、字段、分区、optype、检测用户） |

---

## 二、测试环境与前置

### 2.1 环境要求

| 项 | 要求 |
|----|------|
| DSS 版本 | dev-1.22.4（datagofeishu AppConn 已部署） |
| JDK | 1.8 |
| Spring Boot | 2.7.18 |
| DataGo 服务 | 可达，默认端口 3003，三个接口联调可用 |
| 数据库 | `dss_appconn` / `dss_appconn_instance` / `dss_workflow_node` / `dss_workflow_node_ui` 已执行 init.sql |
| 实例配置 | `enhance_json` 中 `wds.dss.appconn.datago.feishu.api.base.url` 已配置为 `http://{DataGoHost}:3003` |

### 2.2 DM 单准备要求

为覆盖各场景，需在 DataGo 侧准备以下 DM 单：

| DM 单号 | optype | 状态 | 库表 | 字段 | 分区 | 检测用户 | 用途 |
|---------|--------|------|------|------|------|----------|------|
| DM202607150001 | table | detected_pass（可外发） | db_audit.t_event_log | user_id,event,ts | ds=2026-07-14 | alexyang,ryanchen | 正常外发 |
| DM202607150002 | table | detected_pass | db_audit.t_dim_code | code_id,code_name | （非分区） | alexyang | 单目标非分区表 |
| DM202607150003 | table | detected_pass | db_audit.t_event_log, db_audit.t_dim_code | 见上 | 见上 | alexyang,ryanchen | 多库多表 |
| DM202607150004 | datago | — | — | — | — | — | optype 非法 |
| DM202607150005 | table | detected_fail | db_audit.t_sensitive | phone,idcard | — | alexyang | DM 单已检测不通过 |
| DM202607150006 | table | detect_error | db_audit.t_event_log | user_id,event,ts | ds=2026-07-14 | alexyang | DM 单已检测异常 |
| DM202607150007 | table | exported | db_audit.t_event_log | user_id,event,ts | ds=2026-07-14 | alexyang | DM 单已外发 |
| DM202607150099 | — | — | — | — | — | — | 不存在的 DM 单 |

> ② 任务接口与 ③ 执行外发接口的返回状态由测试时 DataGo 侧 mock 或构造实际数据控制（命中敏感表、构造超 50MB 表、模拟飞书 502 等）。

### 2.3 代理用户与权限

- 执行用户：`v_sunpengwang`（DSS 登录用户）
- DM 单检测用户（dmUser）：`alexyang`、`ryanchen`（飞书通知人必须是其子集）
- 越权测试用户：`unauthorized_user`（不在任何 DM 单检测用户范围内）

### 2.4 公共节点参数模板

```
dmId = DM202607150001
notifyUsers = alexyang;ryanchen
dataTargets = dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14}
（节点 UI 仅上述 3 个专属属性；maxWaitTime 等节奏参数走实例级配置，默认 7200 秒即 2 小时）
```

---

## 三、用例清单汇总表

| 用例ID | 分类 | 标题 | 优先级 | 关联 AC | 异常码 |
|--------|------|------|:------:|:-------:|:------:|
| TC-SF-001 | 正常流程 | 正常外发流程-多目标端到端 | P0 | AC-1, AC-6, AC-11 | — |
| TC-SF-002 | 正常流程 | 单目标外发成功记录 bitableUrl 与 sheet 行数 | P0 | AC-6 | — |
| TC-SF-003 | 参数校验 | 节点参数 dmId 缺失 | P0 | — | 82001 |
| TC-SF-004 | 参数校验 | 节点参数 notifyUsers 缺失 | P0 | — | 82001 |
| TC-SF-005 | 参数校验 | 节点参数 dataTargets 缺失 | P0 | — | 82001 |
| TC-SF-006 | 参数校验 | 实例级时间参数非正整数 | P2 | — | 82001 |
| TC-SF-007 | 参数校验 | maxWaitTime 不暴露节点 UI 仅实例级生效 | P2 | FR-7.3 | — |
| TC-SF-008 | DSL 解析 | 多级分区 `{dt=.../country=...}` 花括号保护 | P1 | FR-5 | — |
| TC-SF-009 | DSL 解析 | 空分区 `partition=` 与省略 partition 段 | P1 | FR-5 | — |
| TC-SF-010 | DSL 解析 | 行间分号与换行混合分隔 | P2 | FR-5 | — |
| TC-SF-011 | DSL 解析 | 编号格式错误（非 dt.NN） | P1 | FR-5 | 82001 |
| TC-SF-012 | DSL 解析 | 缺 db/table/fields 必填段 | P1 | FR-5 | 82001 |
| TC-SF-013 | DSL 解析 | 未知字段段 | P2 | FR-5 | 82001 |
| TC-SF-014 | DSL 解析 | 同 (db,table,partition) 重复 | P1 | AC-15 | 82001 |
| TC-SF-014b | DSL 解析 | 同表不同分区允许共存 | P1 | AC-15 / FR-5.2 / FR-9 | — |
| TC-SF-015 | DM 比对 | 字段超范围 | P0 | AC-2 | 82003 |
| TC-SF-016 | DM 比对 | optype=datago | P0 | AC-3 | 82003 |
| TC-SF-017 | DM 比对 | notifyUsers 越权 | P0 | AC-10 | 82003 |
| TC-SF-018 | DM 比对 | 库表不在 DM 单审批范围 | P0 | FR-2.5 | 82003 |
| TC-SF-019 | DM 比对 | 客户端不校验分区范围-分区不一致放行至②③ | P2 | FR-2.7 | — |
| TC-SF-020 | DM 比对 | 字段顺序与空白差异归一通过 | P2 | FR-2.9 | — |
| TC-SF-021 | DM 比对 | notifyUsers 顺序/空白/去重归一通过 | P2 | FR-2.9 | — |
| TC-SF-022 | DM 比对 | DM 单状态 detected_fail | P1 | FR-2.4 | 82005 |
| TC-SF-023 | DM 比对 | DM 单状态 detect_error | P1 | FR-2.4 | 82006 |
| TC-SF-024 | DM 比对 | DM 单状态 exported | P1 | FR-2.4 | 82002 |
| TC-SF-025 | DM 比对 | 分区校验未启用-DM 单未限定分区节点指定分区通过 | P2 | FR-2.7 | — |
| TC-SF-026 | DM 比对 | 分区校验未启用-节点未指定分区DM 单限定分区通过 | P2 | FR-2.7 | — |
| TC-SF-027 | DM 比对 | ① 未返回审批通知人集合-越权交由 ③ 兜底 | P2 | FR-2.8 | 82003 |
| TC-SF-028 | 检测阶段 | 检测不通过 detected_fail | P0 | AC-4 | 82005 |
| TC-SF-029 | 检测阶段 | 检测异常 detect_error | P0 | AC-5 | 82006 |
| TC-SF-030 | 检测阶段 | 检测轮询超时 | P0 | AC-5 | 82006 |
| TC-SF-031 | 检测阶段 | ② 首次创建未返回 taskId | P1 | FR-3.6 | 82004 |
| TC-SF-032 | 检测阶段 | ② 任务接口调用失败 | P1 | FR-3.1 | 82004 |
| TC-SF-033 | 检测阶段 | ② 任务接口 403 dm 单无效 | P2 | — | 82003 |
| TC-SF-034 | 检测阶段 | ② 返回未知状态 | P2 | — | 82004 |
| TC-SF-035 | 检测阶段 | ② 返回 exported 直接成功 | P2 | — | — |
| TC-SF-036 | 外发阶段 | 外发部分失败重试耗尽 | P0 | AC-7 | 82008 |
| TC-SF-037 | 外发阶段 | 外发部分失败重试后成功 | P1 | AC-7 | — |
| TC-SF-038 | 外发阶段 | 飞书不可达 502 重试耗尽 | P0 | AC-8 | 82007 |
| TC-SF-039 | 外发阶段 | 飞书不可达 504 重试耗尽 | P1 | AC-8 | 82007 |
| TC-SF-040 | 外发阶段 | 飞书不可达重试后成功 | P1 | AC-8 | — |
| TC-SF-041 | 外发阶段 | 超限 413 不可重试 | P0 | AC-9 | 82009 |
| TC-SF-042 | 外发阶段 | 状态冲突 409 不可重试 | P1 | FR-4.5 | 82009 |
| TC-SF-043 | 外发阶段 | 外发 500 内部错误 | P1 | FR-4.7 | 82007 |
| TC-SF-044 | 外发阶段 | 外发 400 参数错误 | P2 | — | 82001 |
| TC-SF-045 | 外发阶段 | 外发阶段 maxWaitTime 超时 | P2 | — | 82007 |
| TC-SF-046 | 异常码 | ① 表单接口 dm 单不存在 | P1 | FR-2.2 | 82002 |
| TC-SF-047 | 异常码 | ① 表单接口调用异常 | P1 | — | 82002 |
| TC-SF-048 | 兼容 | 兼容旧单值字段 | P1 | AC-12 | — |
| TC-SF-049 | 生命周期 | 节点 kill 状态置 KILLED | P1 | AC-13 | — |
| TC-SF-050 | 日志审计 | 日志审计字段完整性 | P1 | AC-14 | — |
| TC-SF-051 | 日志审计 | 日志脱敏-不含原始数据与凭据 | P1 | AC-14 | — |

**合计：51 个用例**（P0: 15，P1: 23，P2: 13）

---

## 四、用例详情

### 4.1 正常流程

#### TC-SF-001：正常外发流程-多目标端到端

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-001 |
| 分类 | 正常流程 |
| 优先级 | P0 |
| 关联 | AC-1 / AC-6 / AC-11 / FR-1 / FR-2 / FR-3 / FR-4 / FR-5 / US-1 / US-9 |

**前置条件**：
1. DataGo 服务可达，端口 3003
2. DM 单 `DM202607150001` 存在，optype=table，status=detected_pass，包含表 `db_audit.t_event_log`（fields=user_id,event,ts，partition=ds=2026-07-14）与 `db_audit.t_dim_code`（fields=code_id,code_name，非分区）
3. dmUser=alexyang,ryanchen
4. ② 任务接口对本次创建将返回 inited → detecting → detected_pass
5. ③ 执行外发接口将返回 200，sheets 全部 success

**测试步骤**：
1. 在 DSS 工作流"数据输出"分组拖拽 sendfeishu（DataGo飞书多维表格外发）节点，配置：
   - dmId = `DM202607150001`
   - notifyUsers = `alexyang;ryanchen`
   - dataTargets = `dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14};dt.02=db=db_audit|table=t_dim_code|fields=code_id,code_name`
2. 保存节点，提交工作流执行
3. 观察节点状态变化：Running → Running → Success
4. 查看节点日志与审计字段

**预期结果**：
1. 节点先调 ① `POST /api/export/form` 获取 DM 单信息，optype=table 校验通过
2. 逐目标比对：dt.01/dt.02 库表命中、字段一致（子集）、notifyUsers⊆dmUser，全部通过（分区范围客户端不校验，交②③）
3. 节点进入 DETECTING 阶段，首次调 ② `POST /api/export/task`（taskId=null）创建任务，请求体 `tables[]` 每表内嵌 `fields`/`partitions`(非空才传)/`notifyUsers`（外层无 fields/partitions），DataGo 返回 taskId（数值型，如 1024）
4. 后续周期调 ② 携带 taskId 轮询，状态由 inited/detecting 最终变为 detected_pass
5. 节点进入 EXPORTING 阶段，调 ③ `POST /api/export/execute`（optype=table，notifyUsers=[alexyang,ryanchen]，taskIds=[1024]）
6. ③ 返回 200，sheets 全部 success，节点状态 Success
7. 日志记录 bitableUrl、各 sheet 表名/行数/状态、实际通知用户、外发完成时间
8. 飞书通知人 alexyang、ryanchen 收到飞书消息（含多维表格 URL）

---

#### TC-SF-002：单目标外发成功记录 bitableUrl 与 sheet 行数

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-002 |
| 分类 | 正常流程 |
| 优先级 | P0 |
| 关联 | AC-6 / FR-4.3 / FR-8.2 / US-1 |

**前置条件**：
1. DM 单 `DM202607150002` 存在，optype=table，status=detected_pass，包含单表 `db_audit.t_dim_code`（fields=code_id,code_name，非分区）
2. dmUser=alexyang
3. ② 返回 detected_pass，③ 返回 200 全部 success

**测试步骤**：
1. 配置 sendfeishu 节点：
   - dmId = `DM202607150002`
   - notifyUsers = `alexyang`
   - dataTargets = `dt.01=db=db_audit|table=t_dim_code|fields=code_id,code_name`
2. 提交执行，等待节点 Success
3. 查询节点 result 与日志

**预期结果**：
1. 节点成功，ExecutionResponseRef 为 success
2. ③ 返回的 `data.bitableName` 形如 `DM202607150002--1--{时间戳}`
3. ③ 返回的 `data.bitableUrl` 非空，日志中可查
4. `data.sheets` 仅 1 个元素：tableName=t_dim_code，rows=N（实际行数），status=success
5. `data.notifyUsers` = ["alexyang"]，`data.exportedAt` 为 ISO 8601 时间
6. 审计字段含 bitableUrl、sheetsSummary、exportStatus=success

---

### 4.2 参数校验

#### TC-SF-003：节点参数 dmId 缺失

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-003 |
| 分类 | 参数校验 |
| 优先级 | P0 |
| 关联 | FR-1.3 / FR-9 / AC-2（参数校验类） |
| 异常码 | 82001 |

**前置条件**：DataGo 服务可达

**测试步骤**：
1. 配置 sendfeishu 节点，dmId 留空，notifyUsers 与 dataTargets 填写合法值
2. 提交执行

**预期结果**：
1. 节点在 INIT 阶段（参数解析）即失败
2. 异常码 82001，错误消息含"节点参数 dmId 不能为空"
3. 不调用任何 DataGo 接口

---

#### TC-SF-004：节点参数 notifyUsers 缺失

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-004 |
| 分类 | 参数校验 |
| 优先级 | P0 |
| 关联 | FR-1.3 / FR-9 |
| 异常码 | 82001 |

**前置条件**：DataGo 服务可达

**测试步骤**：
1. 配置 sendfeishu 节点，notifyUsers 留空，dmId 与 dataTargets 填写合法值
2. 提交执行

**预期结果**：
1. 节点 INIT 阶段失败，异常码 82001
2. 错误消息含"节点参数 notifyUsers 不能为空"或"飞书通知人不能为空"
3. 不调用任何 DataGo 接口

---

#### TC-SF-005：节点参数 dataTargets 缺失

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-005 |
| 分类 | 参数校验 |
| 优先级 | P0 |
| 关联 | FR-1.3 / FR-9 |
| 异常码 | 82001 |

**前置条件**：DataGo 服务可达

**测试步骤**：
1. 配置 sendfeishu 节点，dataTargets 留空，且未填写旧单值字段（dbName/tableName/fields）
2. dmId 与 notifyUsers 填写合法值
3. 提交执行

**预期结果**：
1. 节点 INIT 阶段失败，异常码 82001
2. 错误消息含"外发目标不能为空"或"节点参数 ... 不能为空"
3. 不调用任何 DataGo 接口

---

#### TC-SF-006：实例级时间参数非正整数

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-006 |
| 分类 | 参数校验 |
| 优先级 | P2 |
| 关联 | FR-1.3 / FR-9 / 设计 4.2 |
| 异常码 | 82001 |

**前置条件**：DataGo 服务可达；节点 UI 不暴露节奏参数，故通过实例级 `enhance_json` 构造非法值

**测试步骤**：
1. 将实例级配置之一设为非正整数，例如 `wds.dss.appconn.datago.feishu.max.wait.time.ms=0`（或 `detect.poll.interval.ms=-1`、`execute.retry.interval.ms=abc`）
2. 其余节点参数合法，提交执行

**预期结果**：
1. 节点 INIT 阶段失败，异常码 82001
2. 错误消息含"参数 ... 必须为正整数"（或非负整数，按代码实现）
3. 不进入 DETECTING 阶段

---

#### TC-SF-007：maxWaitTime 不暴露节点 UI 仅实例级生效

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-007 |
| 分类 | 参数校验 |
| 优先级 | P2 |
| 关联 | FR-7.3 / 设计 4.2 参数优先级 |

**前置条件**：
1. 实例级配置 `wds.dss.appconn.datago.feishu.max.wait.time.ms=7200`（秒）
2. DataGo 服务可达，DM 单合法
3. init.sql 已执行，节点 UI 定义已注册

**测试步骤**：
1. 打开 sendfeishu 节点参数面板，确认仅展示 `dmId`/`notifyUsers`/`dataTargets` 三个专属属性，**无 `maxWaitTime` 输入框**
2. 查询 `dss_workflow_node_ui`，确认 sendfeishu 节点未绑定 `maxWaitTime` key
3. 将实例级 `max.wait.time.ms` 改为 `600`（秒），构造 ② 检测长期不返回终态，提交执行
4. 观察节点是否在约 600 秒后超时失败

**预期结果**：
1. 节点 UI 不展示 `maxWaitTime`（节奏参数统一走实例级配置）
2. `dss_workflow_node_ui` 中 sendfeishu 绑定的 key 不含 `maxWaitTime`/`detectPollInterval`/`executeRetryMax`/`executeRetryInterval`
3. 节点使用实例级 maxWaitTime=600 秒，约 600 秒后超时失败，异常码 82006（检测阶段超时）
4. 日志含"任务超过最大等待时间"，maxWaitTime=600000ms

---

### 4.3 dataTargets DSL 解析（FR-5 / 设计 3.1）

#### TC-SF-008：多级分区 `{dt=.../country=...}` 花括号保护

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-008 |
| 分类 | DSL 解析 |
| 优先级 | P1 |
| 关联 | FR-5.1 / 设计 3.1 |

**前置条件**：DataGo 服务可达，DM 单含多级分区表 `db_metrics.daily_kpi`（fields=kpi_code,kpi_val，partition=dt=20260716/country=us）

**测试步骤**：
1. 配置 dataTargets：
   ```
   dt.01=db=db_metrics|table=daily_kpi|fields=kpi_code,kpi_val|partition={dt=20260716/country=us}
   ```
2. 提交执行，检查 ② 请求体中 tables[0].partitions 字段值

**预期结果**：
1. 节点解析 partition 时去除花括号，保留内部 `dt=20260716/country=us`
2. 花括号内的 `=` 与 `/` 不被当作分隔符，完整传递给 ②
3. ② 请求体 `tables[0].partitions = ["dt=20260716/country=us"]`（每表分区列表，外层无 partitions）
4. DM 比对通过（分区范围客户端不校验；字段为审批列子集）

---

#### TC-SF-009：空分区 `partition=` 与省略 partition 段

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-009 |
| 分类 | DSL 解析 |
| 优先级 | P1 |
| 关联 | FR-5.1 / 设计 3.1 |

**前置条件**：DataGo 服务可达，DM 单含非分区表 `db_audit.t_dim_code`（fields=code_id,code_name，无 partition）

**测试步骤**：
1. 场景 A：配置 dataTargets = `dt.01=db=db_audit|table=t_dim_code|fields=code_id,code_name|partition=`
2. 场景 B：配置 dataTargets = `dt.01=db=db_audit|table=t_dim_code|fields=code_id,code_name`（省略 partition 段）
3. 分别提交执行，检查 ② 请求体

**预期结果**：
1. 两种写法等价：partition 解析为 null（非分区表）
2. ② 请求体 `tables[0]` 不含 partitions 字段（非分区表，外层无 partitions）
3. DM 比对通过（字段为审批列子集）

---

#### TC-SF-010：行间分号与换行混合分隔

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-010 |
| 分类 | DSL 解析 |
| 优先级 | P2 |
| 关联 | FR-5.1 / 设计 3.1 |

**前置条件**：DataGo 服务可达，DM 单含 `db_audit.t_event_log` 与 `db_audit.t_dim_code`

**测试步骤**：
1. 场景 A：dataTargets 行间用分号分隔
   ```
   dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts;dt.02=db=db_audit|table=t_dim_code|fields=code_id,code_name
   ```
2. 场景 B：dataTargets 行间用换行分隔（textarea 多行输入）
3. 场景 C：分号与换行混合（含空行、首尾空白）
4. 分别提交执行

**预期结果**：
1. 三种写法均解析为 2 个 DataTarget
2. 分号、`\r`、`\n` 均作为行分隔符；空行与首尾空白被忽略
3. ② 请求体 tables 数组含 2 个元素，每表内嵌各自 `fields`（外层无 fields）

---

#### TC-SF-011：编号格式错误（非 dt.NN）

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-011 |
| 分类 | DSL 解析 |
| 优先级 | P1 |
| 关联 | FR-5.1 / FR-9 |
| 异常码 | 82001 |

**前置条件**：DataGo 服务可达

**测试步骤**：
1. 场景 A：dataTargets = `dt.aa=db=db_audit|table=t_event_log|fields=user_id,event,ts`
2. 场景 B：dataTargets = `dt=db=db_audit|table=t_event_log|fields=user_id,event,ts`
3. 场景 C：dataTargets = `db=db_audit|table=t_event_log|fields=user_id,event,ts`（无 dt. 前缀）
4. 分别提交执行

**预期结果**：
1. 三种场景均 INIT 阶段失败，异常码 82001
2. 错误消息含"外发目标编号格式错误，需为 dt.序号"或"外发目标格式错误，需以 dt.序号= 开头"
3. 编号必须匹配正则 `dt\.\d+`

---

#### TC-SF-012：缺 db/table/fields 必填段

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-012 |
| 分类 | DSL 解析 |
| 优先级 | P1 |
| 关联 | FR-5.1 / FR-9 |
| 异常码 | 82001 |

**前置条件**：DataGo 服务可达

**测试步骤**：
1. 场景 A：缺 db —— `dt.01=table=t_event_log|fields=user_id,event,ts`
2. 场景 B：缺 table —— `dt.01=db=db_audit|fields=user_id,event,ts`
3. 场景 C：缺 fields —— `dt.01=db=db_audit|table=t_event_log`
4. 场景 D：fields 为空 —— `dt.01=db=db_audit|table=t_event_log|fields=`
5. 分别提交执行

**预期结果**：
1. 四种场景均 INIT 阶段失败，异常码 82001
2. 错误消息含"缺少 db/table/fields 必填段"

---

#### TC-SF-013：未知字段段

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-013 |
| 分类 | DSL 解析 |
| 优先级 | P2 |
| 关联 | FR-5.1 / FR-9 |
| 异常码 | 82001 |

**前置条件**：DataGo 服务可达

**测试步骤**：
1. 配置 dataTargets = `dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|foo=bar`
2. 提交执行

**预期结果**：
1. INIT 阶段失败，异常码 82001
2. 错误消息含"存在未知字段: foo"
3. 仅允许 db/table/fields/partition 四种段 key

---

#### TC-SF-014：同 (db,table,partition) 重复

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-014 |
| 分类 | DSL 解析 |
| 优先级 | P1 |
| 关联 | AC-15 / FR-5.2 / FR-9 |
| 异常码 | 82001 |

**前置条件**：DataGo 服务可达

**测试步骤**：
1. 配置 dataTargets（同一 db/table/partition 完全重复）：
   ```
   dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14};
   dt.02=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14}
   ```
2. 提交执行

**预期结果**：
1. INIT 阶段失败，异常码 82001
2. 错误消息含"外发目标 dt.02 db_audit.t_event_log#ds=2026-07-14 重复，请检查"
3. 同一 (db, table, partition) 不允许重复；去重依据为 `DataTarget.tableKey()` = `db.table#partition`（非分区表 partition 段为空）

---

#### TC-SF-014b：同表不同分区允许共存

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-014b |
| 分类 | DSL 解析 |
| 优先级 | P1 |
| 关联 | AC-15 / FR-5.2 / FR-9 |
| 异常码 | — |

**前置条件**：DataGo 服务可达，DM 单 DM202607150001 含表 `db_audit.t_event_log`（fields=user_id,event,ts，审批分区覆盖 ds=2026-07-14 与 ds=2026-07-15），status=detected_pass，dmUser 含 notifyUsers

**测试步骤**：
1. 配置 dataTargets（同 db/table、不同 partition）：
   ```
   dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14};
   dt.02=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-15}
   ```
2. 提交执行

**预期结果**：
1. DSL 解析通过，不报 82001 重复错误（tableKey 分别为 `db_audit.t_event_log#ds=2026-07-14` 与 `db_audit.t_event_log#ds=2026-07-15`，互不重复）
2. 节点解析出 2 个外发目标，进入 DM 比对阶段（两目标按 db/table 命中同一审批表，字段为审批列子集，比对通过）
3. 节点进入 DETECTING 阶段，② 请求体 `tables[]` 含 1 个元素（同 db/table 的两目标按表合并，同表多分区写入同一 sheet），该元素 `partitions` 含 ds=2026-07-14 与 ds=2026-07-15、`fields` 为该表字段；外层无 partitions/fields

---

### 4.4 DM 信息比对（FR-2 / 设计 3.4）

#### TC-SF-015：字段超范围

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-015 |
| 分类 | DM 比对 |
| 优先级 | P0 |
| 关联 | AC-2 / FR-2.6 / FR-9 / US-2 |
| 异常码 | 82003 |

**前置条件**：
1. DM 单 `DM202607150001` 存在，表 `db_audit.t_event_log` 审批列为 user_id,event,ts
2. DataGo 服务可达

**测试步骤**：
1. 配置 sendfeishu 节点：
   - dmId = `DM202607150001`
   - notifyUsers = `alexyang`
   - dataTargets = `dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts,phone|partition={ds=2026-07-14}`
   （fields 多了 phone，超出审批范围）
2. 提交执行

**预期结果**：
1. ① 表单接口正常返回
2. 节点在 DM 比对阶段失败，异常码 82003
3. 错误消息含"第1个外发目标 ... 字段超出DM单审批范围，越权字段: [phone]"，并列出审批字段
4. 不调 ②③ 接口

---

#### TC-SF-016：optype=datago

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-016 |
| 分类 | DM 比对 |
| 优先级 | P0 |
| 关联 | AC-3 / FR-2.3 / FR-9 / US-3 |
| 异常码 | 82003 |

**前置条件**：DM 单 `DM202607150004` 存在，optype=datago（报告外发）

**测试步骤**：
1. 配置 sendfeishu 节点：
   - dmId = `DM202607150004`
   - notifyUsers = `alexyang`
   - dataTargets = `dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts`
2. 提交执行

**预期结果**：
1. ① 返回 optype=datago
2. 节点在 DM 比对阶段失败，异常码 82003
3. 错误消息含"DM单optype=datago非table，请走现有报告外发接口"
4. 不调 ②③ 接口

---

#### TC-SF-017：notifyUsers 越权

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-017 |
| 分类 | DM 比对 |
| 优先级 | P0 |
| 关联 | AC-10 / FR-2.8 / FR-9 / US-8 |
| 异常码 | 82003 |

**前置条件**：
1. DM 单 `DM202607150001` 存在，dmUser=alexyang,ryanchen
2. ① 返回的 form.notifyUsers 含审批允许通知人集合 [alexyang, ryanchen]
3. DataGo 服务可达

**测试步骤**：
1. 配置 sendfeishu 节点：
   - dmId = `DM202607150001`
   - notifyUsers = `alexyang;unauthorized_user`
   - dataTargets = `dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14}`
2. 提交执行

**预期结果**：
1. ① 表单接口正常返回
2. 节点在 DM 比对阶段失败，异常码 82003
3. 错误消息含"飞书通知人超出DM单授权范围，越权用户: [unauthorized_user]"
4. 不调 ②③ 接口
5. 若 ① 未返回审批通知人集合，则越权由 ③ 403 兜底（见 TC-SF-027）

---

#### TC-SF-018：库表不在 DM 单审批范围

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-018 |
| 分类 | DM 比对 |
| 优先级 | P0 |
| 关联 | FR-2.5 / FR-9 |
| 异常码 | 82003 |

**前置条件**：DM 单 `DM202607150001` 仅含 db_audit.t_event_log 与 db_audit.t_dim_code

**测试步骤**：
1. 配置 dataTargets = `dt.01=db=db_audit|table=t_not_exist|fields=user_id,event,ts`
2. dmId 与 notifyUsers 合法，提交执行

**预期结果**：
1. ① 表单接口正常返回
2. 节点在 DM 比对阶段失败，异常码 82003
3. 错误消息含"第1个外发目标库表 db_audit.t_not_exist 不在DM单审批范围内"
4. 不调 ②③ 接口

---

#### TC-SF-019：客户端不校验分区范围-分区不一致放行至②③

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-019 |
| 分类 | DM 比对 |
| 优先级 | P2 |
| 关联 | FR-2.7 / 设计 3.4 |
| 异常码 | — |

> **代码现状**：`DmInfoComparator.validatePartition` 已实现但 `validate()` 当前**未调用**，分区范围不在客户端强校验，分区一致性交由 ②③检测阶段以实际分区为准。本用例验证该"放行"行为。

**前置条件**：DM 单 `DM202607150001` 的 t_event_log 审批分区为 `ds=2026-07-14`，① 比对其他项均通过

**测试步骤**：
1. 配置 dataTargets = `dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-15}`
   （分区与审批分区不一致）
2. dmId 与 notifyUsers 合法，提交执行
3. 观察节点是否在 DM 比对阶段失败，是否继续进入 ②

**预期结果**：
1. ① 表单接口正常返回
2. 节点**不**在 DM 比对阶段因分区不一致失败（不抛 82003）
3. 节点通过 DM 比对，进入 DETECTING 阶段调 ②（分区以节点填写值传递）
4. 后续是否成功取决于 ②③ 对实际分区的处理（如 ②③ 拒绝则以 ②③ 的错误码失败）

---

#### TC-SF-020：字段顺序与空白差异归一通过

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-020 |
| 分类 | DM 比对 |
| 优先级 | P2 |
| 关联 | FR-2.9 / 设计 3.4 字段规范化 |

**前置条件**：DM 单 `DM202607150001` 的 t_event_log 审批列为 [user_id, event, ts]

**测试步骤**：
1. 配置 dataTargets = `dt.01=db=db_audit|table=t_event_log|fields= ts ,event , user_id |partition={ds=2026-07-14}`
   （字段顺序打乱、含空白）
2. 提交执行

**预期结果**：
1. 节点对 fields 做 trim、去空、去重、排序归一后为 [event, ts, user_id]
2. DM 单 columns 归一后同为 [event, ts, user_id]
3. 比对通过，节点进入 DETECTING 阶段
4. 不因顺序或空白差异误判

---

#### TC-SF-021：notifyUsers 顺序/空白/去重归一通过

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-021 |
| 分类 | DM 比对 |
| 优先级 | P2 |
| 关联 | FR-2.9 / FR-2.8 |

**前置条件**：DM 单 `DM202607150001` 的 dmUser=alexyang,ryanchen，① 返回 form.notifyUsers=[alexyang,ryanchen]

**测试步骤**：
1. 配置 notifyUsers = ` ryanchen ; alexyang ; alexyang ;  `
   （含空白、重复、顺序打乱）
2. 提交执行

**预期结果**：
1. 节点对 notifyUsers 做 trim、去空、去重、排序归一后为 [alexyang, ryanchen]
2. 是 dmUser 子集，比对通过
3. 节点进入 DETECTING 阶段

---

#### TC-SF-022：DM 单状态 detected_fail

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-022 |
| 分类 | DM 比对 |
| 优先级 | P1 |
| 关联 | FR-2.4 / FR-9 |
| 异常码 | 82005 |

**前置条件**：DM 单 `DM202607150005` 状态为 detected_fail（DM 单层面已检测不通过）

**测试步骤**：
1. 配置 dmId = `DM202607150005`，其余参数与 DM 单一致
2. 提交执行

**预期结果**：
1. ① 返回 status=detected_fail
2. 节点在 DM 比对阶段（状态校验）失败，异常码 82005
3. 错误消息含"DM单检测不通过（命中敏感），DataGo已飞书通知"
4. 不调 ②③ 接口

---

#### TC-SF-023：DM 单状态 detect_error

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-023 |
| 分类 | DM 比对 |
| 优先级 | P1 |
| 关联 | FR-2.4 / FR-9 |
| 异常码 | 82006 |

**前置条件**：DM 单 `DM202607150006` 状态为 detect_error

**测试步骤**：
1. 配置 dmId = `DM202607150006`，其余参数与 DM 单一致
2. 提交执行

**预期结果**：
1. ① 返回 status=detect_error
2. 节点在 DM 比对阶段失败，异常码 82006
3. 错误消息含"DM单检测异常，DataGo已飞书通知"
4. 不调 ②③ 接口

---

#### TC-SF-024：DM 单状态 exported

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-024 |
| 分类 | DM 比对 |
| 优先级 | P1 |
| 关联 | FR-2.4 / FR-9 |
| 异常码 | 82002 |

**前置条件**：DM 单 `DM202607150007` 状态为 exported（已外发）

**测试步骤**：
1. 配置 dmId = `DM202607150007`，其余参数与 DM 单一致
2. 提交执行

**预期结果**：
1. ① 返回 status=exported
2. 节点在 DM 比对阶段失败，异常码 82002
3. 错误消息含"DM单已外发，不可重复外发"
4. 不调 ②③ 接口

---

#### TC-SF-025：分区校验未启用-DM 单未限定分区节点指定分区通过

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-025 |
| 分类 | DM 比对 |
| 优先级 | P2 |
| 关联 | FR-2.7 / 设计 3.4 |

**前置条件**：DM 单 `DM202607150002` 的 t_dim_code 为非分区表（table.partition 为空）

**测试步骤**：
1. 配置 dataTargets = `dt.01=db=db_audit|table=t_dim_code|fields=code_id,code_name|partition={ds=2026-07-14}`
   （节点指定分区，但 DM 单未限定）
2. 提交执行

**预期结果**：
1. 客户端未启用分区范围校验（`validatePartition` 未调用），不因分区差异失败
2. 比对通过（字段为审批列子集），节点进入 DETECTING 阶段；分区一致性以 ②③检测阶段实际分区为准

---

#### TC-SF-026：分区校验未启用-节点未指定分区DM 单限定分区通过

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-026 |
| 分类 | DM 比对 |
| 优先级 | P2 |
| 关联 | FR-2.7 / 设计 3.4 |

**前置条件**：DM 单 `DM202607150001` 的 t_event_log 限定分区 `ds=2026-07-14`

**测试步骤**：
1. 配置 dataTargets = `dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts`
   （节点未指定分区，DM 单限定分区）
2. 提交执行

**预期结果**：
1. 客户端未启用分区范围校验（`validatePartition` 未调用），节点未指定分区时不强制校验
2. 比对通过（字段为审批列子集），节点进入 DETECTING 阶段；分区以 ②③检测阶段 DM 单分区为准

---

#### TC-SF-027：① 未返回审批通知人集合-越权交由 ③ 兜底

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-027 |
| 分类 | DM 比对 |
| 优先级 | P2 |
| 关联 | FR-2.8 / 设计 3.4 validateNotifyUsers |
| 异常码 | 82003 |

**前置条件**：
1. DM 单 `DM202607150001` 的 ① 返回 form.notifyUsers 为空（未返回审批允许通知人集合）
2. ② 检测将返回 detected_pass
3. ③ 执行外发对越权 notifyUsers 返回 403

**测试步骤**：
1. 配置 notifyUsers = `alexyang;unauthorized_user`（含越权用户）
2. 提交执行，观察 ① 比对阶段是否放行，③ 是否兜底

**预期结果**：
1. ① 比对阶段：form.notifyUsers 为空，跳过子集校验，放行
2. 节点进入 DETECTING → ② 检测通过 → EXPORTING
3. ③ 执行外发返回 403，错误消息含"notifyUsers 必须是 dm 单检测用户的子集，越权用户: [unauthorized_user]"
4. 节点失败，异常码 82003（③ 403 映射为 82003）

---

### 4.5 检测阶段（FR-3 / 设计 3.5）

#### TC-SF-028：检测不通过 detected_fail

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-028 |
| 分类 | 检测阶段 |
| 优先级 | P0 |
| 关联 | AC-4 / FR-3.3 / FR-9 / US-4 |
| 异常码 | 82005 |

**前置条件**：
1. DM 单 `DM202607150001` 合法，① 比对通过
2. ② 首次创建返回 taskId=1024，后续轮询返回 status=detected_fail，resultSummary="命中敏感表 t_sensitive"
3. DataGo 已主动飞书通知

**测试步骤**：
1. 配置合法节点参数，提交执行
2. 观察节点状态变化：Running（DETECTING）→ Failed
3. 检查 DSS 是否调 ③ 接口

**预期结果**：
1. ② 首次创建任务返回 taskId
2. 后续轮询返回 detected_fail
3. 节点失败，异常码 82005
4. 错误消息含"DataGo检测不通过: 命中敏感表 t_sensitive"
5. DSS 不调 ③ 接口（检测不通过不外发）
6. DSS 不重复发飞书通知（DataGo 已通知）

---

#### TC-SF-029：检测异常 detect_error

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-029 |
| 分类 | 检测阶段 |
| 优先级 | P0 |
| 关联 | AC-5 / FR-3.3 / FR-9 |
| 异常码 | 82006 |

**前置条件**：
1. DM 单合法，① 比对通过
2. ② 首次创建返回 taskId，后续轮询返回 status=detect_error（DataGo 已自动重试 2 次仍失败）

**测试步骤**：
1. 配置合法节点参数，提交执行
2. 观察节点状态变化

**预期结果**：
1. 节点失败，异常码 82006
2. 错误消息含"DataGo检测异常"
3. DSS 不调 ③ 接口
4. DataGo 已飞书通知，DSS 不重复通知

---

#### TC-SF-030：检测轮询超时

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-030 |
| 分类 | 检测阶段 |
| 优先级 | P0 |
| 关联 | AC-5 / FR-3.5 / FR-9 |
| 异常码 | 82006 |

**前置条件**：
1. DM 单合法，① 比对通过
2. ② 首次创建返回 taskId，后续轮询始终返回 inited/detecting（非终态）
3. 实例级 `wds.dss.appconn.datago.feishu.max.wait.time.ms=600`（秒，缩短超时时间）

**测试步骤**：
1. 将实例级 max.wait.time.ms 设为 600，配置合法节点参数，提交执行
2. 等待超过 600 秒（10 分钟），观察节点状态

**预期结果**：
1. 节点在 DETECTING 阶段持续轮询，状态保持 Running
2. 超过 600 秒（600000ms）后节点失败，异常码 82006
3. 错误消息含"任务超过最大等待时间"或"DataGo飞书多维表格外发任务等待超时"
4. 日志记录 startedAt、maxWaitTime、超时时的 stage=DETECTING

---

#### TC-SF-031：② 首次创建未返回 taskId

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-031 |
| 分类 | 检测阶段 |
| 优先级 | P1 |
| 关联 | FR-3.6 / FR-9 |
| 异常码 | 82004 |

**前置条件**：
1. DM 单合法，① 比对通过
2. ② 首次创建（taskId=null）返回 200，但 data.taskId 为 null

**测试步骤**：
1. 配置合法节点参数，提交执行
2. 观察节点首次进入 DETECTING 后的行为

**预期结果**：
1. 节点首次调 ② 创建任务，响应中 taskId 为 null
2. 节点失败，异常码 82004
3. 错误消息含"DataGo首次检测未返回taskId"
4. 不再进行后续轮询

---

#### TC-SF-032：② 任务接口调用失败

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-032 |
| 分类 | 检测阶段 |
| 优先级 | P1 |
| 关联 | FR-3.1 / FR-9 |
| 异常码 | 82004 |

**前置条件**：
1. DM 单合法，① 比对通过
2. ② 接口返回 500（DataGo 内部错误）

**测试步骤**：
1. 配置合法节点参数，提交执行
2. 观察节点 DETECTING 阶段行为

**预期结果**：
1. ② 接口返回 500
2. 节点失败，异常码 82004
3. 错误消息含"DataGo任务接口调用失败"

---

#### TC-SF-033：② 任务接口 403 dm 单无效

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-033 |
| 分类 | 检测阶段 |
| 优先级 | P2 |
| 关联 | 设计 3.3.2 错误响应 / FR-9 |
| 异常码 | 82003 |

**前置条件**：
1. ① 比对通过（DM 单初始状态合法）
2. ② 接口返回 403，message="dm 单无效或非 table 类型"（DM 单在 ① 之后被撤销或过期）

**测试步骤**：
1. 配置合法节点参数，提交执行
2. 观察节点 DETECTING 阶段行为

**预期结果**：
1. ② 接口返回 403
2. 节点失败，异常码 82003（403 映射为 82003）
3. 错误消息含"dm 单无效或非 table 类型"

---

#### TC-SF-034：② 返回未知状态

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-034 |
| 分类 | 检测阶段 |
| 优先级 | P2 |
| 关联 | FR-3.3 / FR-9 |
| 异常码 | 82004 |

**前置条件**：
1. DM 单合法，① 比对通过
2. ② 返回 status="unknown_status"（非枚举值）

**测试步骤**：
1. 配置合法节点参数，提交执行

**预期结果**：
1. ② 返回未知状态
2. 节点失败，异常码 82004
3. 错误消息含"DataGo检测接口返回未知状态: unknown_status"

---

#### TC-SF-035：② 返回 exported 直接成功

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-035 |
| 分类 | 检测阶段 |
| 优先级 | P2 |
| 关联 | 设计 3.5 状态机 |

**前置条件**：
1. DM 单合法，① 比对通过
2. ② 首次创建返回 taskId，status=exported（任务已被外发，极端边界）

**测试步骤**：
1. 配置合法节点参数，提交执行

**预期结果**：
1. ② 返回 status=exported
2. 节点 stage 直接置为 SUCCESS，状态 Success
3. 不调 ③ 接口（任务已外发）

---

### 4.6 外发阶段（FR-4 / 设计 3.5）

#### TC-SF-036：外发部分失败重试耗尽

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-036 |
| 分类 | 外发阶段 |
| 优先级 | P0 |
| 关联 | AC-7 / FR-4.6 / FR-9 / US-5 |
| 异常码 | 82008 |

**前置条件**：
1. DM 单合法，② 检测通过（detected_pass），taskId=1024
2. 实例级 `wds.dss.appconn.datago.feishu.execute.retry.max=3`
3. ③ 执行外发返回 200，但 sheets 中 t_event_log.status=failed（飞书写入失败）
4. 后续重试（共 3 次）均返回 200 且 t_event_log.status=failed

**测试步骤**：
1. 配置合法节点参数（多目标含 t_event_log），提交执行
2. 等待检测通过进入 EXPORTING 阶段
3. 观察 ③ 重试行为与最终状态

**预期结果**：
1. ③ 首次返回 200，sheets 含 failed，节点重试（executeRetryCount=1）
2. 第 2、3 次重试仍失败（executeRetryCount=2、3）
3. 重试耗尽后节点失败，异常码 82008
4. 错误消息含"DataGo外发部分表失败且重试耗尽（已写入不回滚）"
5. 已写入的 sheet 不回滚（日志记录哪些 sheet 成功）
6. 重试间隔为 executeRetryInterval（默认 30 秒）

---

#### TC-SF-037：外发部分失败重试后成功

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-037 |
| 分类 | 外发阶段 |
| 优先级 | P1 |
| 关联 | AC-7 / FR-4.6 / US-5 |

**前置条件**：
1. DM 单合法，② 检测通过，taskId=1024
2. 实例级 `execute.retry.max=3`
3. ③ 首次返回 200，sheets 含 1 个 failed
4. ③ 第 2 次重试返回 200，sheets 全部 success

**测试步骤**：
1. 配置合法节点参数，提交执行
2. 观察 ③ 重试后状态

**预期结果**：
1. ③ 首次部分失败，节点重试（executeRetryCount=1）
2. 第 2 次重试全部 success，节点成功，状态 Success
3. 日志记录"外发部分表失败，第1次重试"与最终"数据已外发到飞书多维表格"
4. 已写入的 sheet 不重复写入

---

#### TC-SF-038：飞书不可达 502 重试耗尽

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-038 |
| 分类 | 外发阶段 |
| 优先级 | P0 |
| 关联 | AC-8 / FR-4.4 / FR-9 / US-6 |
| 异常码 | 82007 |

**前置条件**：
1. DM 单合法，② 检测通过，taskId=1024
2. 实例级 `execute.retry.max=3`
3. ③ 执行外发持续返回 502（飞书不可达）

**测试步骤**：
1. 配置合法节点参数，提交执行
2. 观察 ③ 重试行为与最终状态

**预期结果**：
1. ③ 首次返回 502，节点重试（executeRetryCount=1）
2. 第 2、3 次重试仍返回 502
3. 重试耗尽后节点失败，异常码 82007
4. 错误消息含"DataGo外发重试次数耗尽"
5. 日志记录每次重试的 httpCode=502

---

#### TC-SF-039：飞书不可达 504 重试耗尽

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-039 |
| 分类 | 外发阶段 |
| 优先级 | P1 |
| 关联 | AC-8 / FR-4.4 / FR-9 |
| 异常码 | 82007 |

**前置条件**：同 TC-SF-038，但 ③ 返回 504

**测试步骤**：同 TC-SF-038

**预期结果**：
1. 504 与 502 行为一致，均触发重试
2. 重试耗尽后节点失败，异常码 82007

---

#### TC-SF-040：飞书不可达重试后成功

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-040 |
| 分类 | 外发阶段 |
| 优先级 | P1 |
| 关联 | AC-8 / FR-4.4 / US-6 |

**前置条件**：
1. DM 单合法，② 检测通过，taskId=1024
2. 实例级 `execute.retry.max=3`
3. ③ 首次返回 502，第 2 次返回 200 全部 success

**测试步骤**：
1. 配置合法节点参数，提交执行

**预期结果**：
1. ③ 首次 502，节点重试（executeRetryCount=1）
2. 第 2 次重试 200 全部 success，节点成功，状态 Success
3. 日志记录"飞书服务不可达，第1次重试"与最终"数据已外发到飞书多维表格"

---

#### TC-SF-041：超限 413 不可重试

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-041 |
| 分类 | 外发阶段 |
| 优先级 | P0 |
| 关联 | AC-9 / FR-4.5 / FR-9 / US-7 |
| 异常码 | 82009 |

**前置条件**：
1. DM 单合法，② 检测通过，taskId=1024
2. ③ 执行外发返回 413，message="表 t_big_data 超出大小限制(估算 62MB, 上限 50MB)"

**测试步骤**：
1. 配置节点，dataTargets 指向超大数据量表，提交执行
2. 观察 ③ 是否重试

**预期结果**：
1. ③ 返回 413
2. 节点立即失败（不重试），异常码 82009
3. 错误消息含"超出大小限制"或"DataGo执行外发失败"
4. executeRetryCount 保持 0

---

#### TC-SF-042：状态冲突 409 不可重试

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-042 |
| 分类 | 外发阶段 |
| 优先级 | P1 |
| 关联 | FR-4.5 / FR-9 / 设计 3.3.3 |
| 异常码 | 82009 |

**前置条件**：
1. DM 单合法，② 检测通过，taskId=1024
2. ③ 执行外发返回 409，message="存在未检测通过的任务: [taskId=1025 status=detected_fail]"
   （或 optype=datago 场景 409）

**测试步骤**：
1. 配置合法节点参数，提交执行

**预期结果**：
1. ③ 返回 409
2. 节点立即失败（不重试），异常码 82009
3. 错误消息含"存在未检测通过的任务"或"optype=datago 本期本接口不支持"
4. executeRetryCount 保持 0

---

#### TC-SF-043：外发 500 内部错误

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-043 |
| 分类 | 外发阶段 |
| 优先级 | P1 |
| 关联 | FR-4.7 / FR-9 / 设计 3.6 |
| 异常码 | 82007 |

**前置条件**：
1. DM 单合法，② 检测通过，taskId=1024
2. ③ 执行外发返回 500（DataGo 内部错误）

**测试步骤**：
1. 配置合法节点参数，提交执行

**预期结果**：
1. ③ 返回 500
2. 节点失败，异常码 82007
3. 500 不触发重试（仅 502/504 可重试，500 直接失败）
4. 错误消息含"DataGo执行外发失败"

---

#### TC-SF-044：外发 400 参数错误

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-044 |
| 分类 | 外发阶段 |
| 优先级 | P2 |
| 关联 | FR-9 / 设计 3.3.3 |
| 异常码 | 82001 |

**前置条件**：
1. DM 单合法，② 检测通过
2. ③ 执行外发返回 400，message="optype 参数为必填项"（模拟 DSS 侧构造请求异常）

**测试步骤**：
1. 配置合法节点参数，提交执行

**预期结果**：
1. ③ 返回 400
2. 节点失败，异常码 82001（400 映射为 82001）
3. 不重试

---

#### TC-SF-045：外发阶段 maxWaitTime 超时

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-045 |
| 分类 | 外发阶段 |
| 优先级 | P2 |
| 关联 | FR-3.5 / 设计 3.5 超时判定 |
| 异常码 | 82007 |

**前置条件**：
1. DM 单合法，② 检测通过进入 EXPORTING
2. 实例级 `wds.dss.appconn.datago.feishu.max.wait.time.ms=600`（秒）
3. ③ 持续返回 502（飞书长期不可达），重试间隔 30 秒
4. 检测阶段耗时约 500 秒，外发阶段重试至 600 秒超时

**测试步骤**：
1. 将实例级 max.wait.time.ms 设为 600，配置合法节点参数，提交执行
2. 观察超时判定

**预期结果**：
1. 节点在 EXPORTING 阶段重试 ③
2. 超过 600 秒后节点失败
3. 异常码 82007（外发阶段超时映射为 82007，区别于检测阶段 82006）
4. 错误消息含"任务超过最大等待时间"

---

### 4.7 异常码 - 表单阶段

#### TC-SF-046：① 表单接口 dm 单不存在

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-046 |
| 分类 | 异常码 |
| 优先级 | P1 |
| 关联 | FR-2.2 / FR-9 / 设计 3.6 |
| 异常码 | 82002 |

**前置条件**：DM 单 `DM202607150099` 不存在

**测试步骤**：
1. 配置 dmId = `DM202607150099`，提交执行

**预期结果**：
1. ① 返回 404 或 data:null
2. 节点失败，异常码 82002
3. 错误消息含"DM单不存在或无审批信息"
4. 不调 ②③ 接口

---

#### TC-SF-047：① 表单接口调用异常

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-047 |
| 分类 | 异常码 |
| 优先级 | P1 |
| 关联 | FR-9 / 设计 3.6 |
| 异常码 | 82002 |

**前置条件**：DataGo 服务不可达（网络中断或服务宕机）

**测试步骤**：
1. 配置合法 dmId，提交执行
2. 观察 ① 接口调用异常时的行为

**预期结果**：
1. ① HTTP 调用抛出异常（ConnectException/SocketTimeoutException）；`DataGoFeishuHttpUtils` 内部抛 82011，被 `queryExportForm` 捕获并包装为 82002
2. 节点失败，异常码 82002
3. 错误消息含"DataGo表单接口调用失败"

---

### 4.8 兼容（FR-6）

#### TC-SF-048：兼容旧单值字段

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-048 |
| 分类 | 兼容 |
| 优先级 | P1 |
| 关联 | AC-12 / FR-6.1 / FR-6.2 |

**前置条件**：
1. DM 单 `DM202607150001` 合法
2. 存在旧版节点配置（未填 dataTargets，仅填 dbName/tableName/fields/partition）

**测试步骤**：
1. 配置 sendfeishu 节点（模拟旧节点升级）：
   - dmId = `DM202607150001`
   - notifyUsers = `alexyang`
   - dataTargets 留空
   - dbName = `db_audit`
   - tableName = `t_event_log`
   - fields = `user_id,event,ts`
   - partition = `ds=2026-07-14`
2. 提交执行

**预期结果**：
1. 节点检测到 dataTargets 为空，走兼容回退逻辑
2. 自动将 dbName/tableName/fields/partition 包装为单元素 DataTarget
3. 包装后等价于 `dt.01=db=db_audit|table=t_event_log|fields=user_id,event,ts|partition={ds=2026-07-14}`
4. 后续流程与正常流程一致，节点成功
5. 旧节点升级后不破坏原有语义

---

### 4.9 生命周期

#### TC-SF-049：节点 kill 状态置 KILLED

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-049 |
| 分类 | 生命周期 |
| 优先级 | P1 |
| 关联 | AC-13 / 设计 3.5 状态机 |

**前置条件**：
1. DM 单合法，② 检测长时间返回 detecting（非终态）
2. 节点处于 DETECTING 阶段 Running

**测试步骤**：
1. 配置合法节点参数，提交执行
2. 等待节点进入 DETECTING 轮询状态
3. 在 DSS 工作流执行界面点击"杀死"节点
4. 观察 kill 后节点状态与后续轮询行为

**预期结果**：
1. kill 返回 true
2. 节点 stage 置为 Killed，状态置为 Killed
3. 节点不再继续轮询 DataGo（nextPollAt 不再生效）
4. 日志含"节点已终止，不再轮询DataGo"
5. result 返回 error（非 Success）

---

### 4.10 日志审计（FR-8）

#### TC-SF-050：日志审计字段完整性

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-050 |
| 分类 | 日志审计 |
| 优先级 | P1 |
| 关联 | AC-14 / FR-8.1 / FR-8.2 / FR-8.4 |

**前置条件**：DM 单合法，端到端正常外发成功（同 TC-SF-001 场景）

**测试步骤**：
1. 执行 TC-SF-001 正常外发流程
2. 节点 Success 后，收集节点执行日志与审计字段
3. 逐项核对审计字段

**预期结果**：
1. 日志含 dmId、optype、库名、表名、字段数量、分区、通知人数、taskId、当前阶段、DataGo 返回状态
2. 外发完成后日志含多维表格名称、bitableUrl、各 sheet 表名/行数/状态、实际通知用户、外发完成时间
3. 审计字段至少包含：executionId、nodeId、user、dmId、optype、taskId、dataTargets、targetCount、notifyUsers、detectStatus、exportStatus、bitableUrl、sheetsSummary、errorMessage
4. 日志含时间戳前缀（如 "2026-07-20 10:30:00 ..."）
5. 进度 progress 在 DETECTING 返回 0.3，EXPORTING 返回 0.8，SUCCESS 返回 1.0

---

#### TC-SF-051：日志脱敏-不含原始数据与凭据

| 项 | 内容 |
|----|------|
| 用例ID | TC-SF-051 |
| 分类 | 日志审计 |
| 优先级 | P1 |
| 关联 | AC-14 / FR-8.3 / 6.2 安全 |

**前置条件**：DM 单合法，正常外发流程执行（含失败重试场景更佳）

**测试步骤**：
1. 执行正常外发流程（或部分失败重试场景）
2. 收集节点执行日志、审计字段、`DataGoFeishuClient` 与 `DataGoFeishuHttpUtils` 的 INFO 级日志
3. 检查是否含敏感信息

**预期结果**：
1. 日志不含 DataGo 返回的原始数据内容（sheet 行数据、字段值）
2. 日志不含飞书凭据（FS-* token、Authorization 头值）
3. 飞书通知人仅记录账号标识（如 alexyang），不记录飞书 user_open_id 等凭据
4. ③ 返回的 sheets 仅记录 tableName/rows/status 摘要，不记录行数据
5. HTTP 请求日志不含 Authorization 头值；响应体日志截断 2000 字符

---

## 五、异常码覆盖矩阵

异常码 82001~82009 与测试用例的对应关系如下（✓ 表示该用例覆盖此异常码）。

| 异常码 | 含义 | 覆盖用例 | 覆盖数 |
|:------:|------|----------|:------:|
| 82001 | 节点参数缺失或格式错误 | TC-SF-003, TC-SF-004, TC-SF-005, TC-SF-006, TC-SF-011, TC-SF-012, TC-SF-013, TC-SF-014, TC-SF-044 | 9 |
| 82002 | 审批表单获取失败 | TC-SF-024, TC-SF-046, TC-SF-047 | 3 |
| 82003 | DM 单信息与节点参数不一致 | TC-SF-015, TC-SF-016, TC-SF-017, TC-SF-018, TC-SF-027, TC-SF-033 | 6 |
| 82004 | 检测任务接口调用失败 | TC-SF-031, TC-SF-032, TC-SF-034 | 3 |
| 82005 | 检测不通过 | TC-SF-022, TC-SF-028 | 2 |
| 82006 | 检测异常或轮询超时 | TC-SF-023, TC-SF-029, TC-SF-030 | 3 |
| 82007 | 执行外发接口调用失败 | TC-SF-038, TC-SF-039, TC-SF-043, TC-SF-045 | 4 |
| 82008 | 外发部分表失败 | TC-SF-036 | 1 |
| 82009 | 外发超限或状态冲突 | TC-SF-041, TC-SF-042 | 2 |
| 82011 | 防御性异常码（未知执行阶段/通信解析） | —（不可达，见说明） | 0 |

**异常码覆盖统计**：
- 82001~82009 全部 9 个业务异常码均被覆盖
- 共 33 个用例涉及异常码验证（其余 18 个为正常流程 / DSL 解析 / 参数归一 / 兼容 / 生命周期 / 日志审计类）
- 82008（部分失败重试耗尽）与 82009（超限/状态冲突）为不可重试终态，各由核心 P0 用例覆盖

**82011 说明**：`82011` 为防御性异常码，来源有二：
1. `DataGoFeishuHttpUtils` 在网络异常（IOException）/ 响应空 / 响应非 JSON 时抛出，但被 `DataGoFeishuClient` 各方法捕获并分别包装为 82002（①）/ 82004（②）/ 82007（③），故**对外不直接暴露**，由 TC-SF-047（① 网络异常→82002）等覆盖其等价路径；
2. `state()` 在 `action.stage` 非已知阶段时抛出，属内部状态保护，正常黑盒不可达，无独立用例。

### 5.1 异常码触发路径速查

| 异常码 | 触发阶段 | 触发条件 | HTTP 码 |
|:------:|----------|----------|:-------:|
| 82001 | INIT（参数解析） | dmId/notifyUsers/dataTargets 缺失或 DSL 格式错误；实例级时间参数非正整数；AppConn 配置 base.url 缺失 | — |
| 82001 | EXPORTING | ③ 返回 400（optype 缺失等参数错误） | 400 |
| 82002 | INIT（表单获取） | ① 接口调用异常或 DM 单不存在（data:null / 404） | 404 / 异常 |
| 82002 | INIT（比对） | DM 单状态为 exported（已外发） | — |
| 82003 | INIT（比对） | optype≠table、表不在 DM 单、字段超范围、notifyUsers 越权（① 返回审批通知人集合时） | — |
| 82003 | DETECTING | ② 返回 403（dm 单无效/非 table 类型） | 403 |
| 82003 | EXPORTING | ③ 返回 403（notifyUsers 越权，① 未兜底时） | 403 |
| 82004 | DETECTING | ② 接口调用异常、未返回 taskId、返回未知状态 | 400/500 等 |
| 82005 | INIT（比对） | DM 单状态 detected_fail | — |
| 82005 | DETECTING | ② 返回 status=detected_fail | — |
| 82006 | INIT（比对） | DM 单状态 detect_error | — |
| 82006 | DETECTING | ② 返回 status=detect_error；或 DETECTING 阶段超过 maxWaitTime | — |
| 82007 | EXPORTING | ③ 返回 502/504 重试耗尽；③ 返回 500；③ 网络异常；EXPORTING 阶段超时 | 502/504/500 |
| 82008 | EXPORTING | ③ 返回 200 但 sheets 含 failed，重试耗尽 | 200 |
| 82009 | EXPORTING | ③ 返回 413（超 50MB）/ 409（状态冲突/optype 不支持） | 413/409 |
| 82011 | 任意（内部） | 网络/解析异常（被包装为 82002/82004/82007）；未知执行阶段（不可达） | — |

> 注：分区范围不一致当前**不**在客户端 DM 比对阶段触发 82003（`validatePartition` 未调用），交由 ②③检测阶段处理（见 TC-SF-019）。

---

## 六、附：测试用例与需求追溯

### 6.1 验收标准（AC）覆盖

| AC | 描述 | 覆盖用例 |
|:--:|------|----------|
| AC-1 | 正常外发流程 | TC-SF-001 |
| AC-2 | 字段超范围（82003） | TC-SF-015 |
| AC-3 | optype=datago（82003） | TC-SF-016 |
| AC-4 | 检测不通过 detected_fail（82005） | TC-SF-028（+ TC-SF-022 DM 单层面） |
| AC-5 | 检测异常或超时（82006） | TC-SF-029, TC-SF-030（+ TC-SF-023 DM 单层面） |
| AC-6 | 外发成功 | TC-SF-001, TC-SF-002 |
| AC-7 | 外发部分失败重试（82008） | TC-SF-036, TC-SF-037 |
| AC-8 | 飞书不可达重试（82007） | TC-SF-038, TC-SF-039, TC-SF-040 |
| AC-9 | 超限不可重试（82009） | TC-SF-041 |
| AC-10 | notifyUsers 越权（82003） | TC-SF-017 |
| AC-11 | 多库多表 | TC-SF-001（隐含于 TC-SF-001） |
| AC-12 | 兼容旧节点 | TC-SF-048 |
| AC-13 | 节点 kill | TC-SF-049 |
| AC-14 | 日志审计 | TC-SF-050, TC-SF-051 |
| AC-15 | 同 (db,table,partition) 重复；同表不同分区允许 | TC-SF-014, TC-SF-014b |

**AC 覆盖率：15/15 = 100%**

### 6.2 功能需求（FR）覆盖

| FR | 描述 | 覆盖用例 |
|:--:|------|----------|
| FR-1 | 节点定义与参数 | TC-SF-001~TC-SF-007 |
| FR-2 | 审批信息校验 | TC-SF-015~TC-SF-027 |
| FR-3 | 检测任务与轮询 | TC-SF-028~TC-SF-035 |
| FR-4 | 执行外发 | TC-SF-036~TC-SF-045 |
| FR-5 | 多库多表多字段多分区 | TC-SF-001, TC-SF-008~TC-SF-014, TC-SF-014b |
| FR-6 | 兼容回退 | TC-SF-048 |
| FR-7 | 节点参数 UI 与实例配置 | TC-SF-007（节奏参数不暴露 UI，仅实例级；三级优先级逻辑由代码保证） |
| FR-8 | 日志与审计 | TC-SF-050, TC-SF-051 |
| FR-9 | 失败语义与异常码 | 见异常码覆盖矩阵（82001~82009 全覆盖） |

### 6.3 用户故事（US）覆盖

| US | 描述 | 覆盖用例 |
|:--:|------|----------|
| US-1 | 正常外发 | TC-SF-001, TC-SF-002 |
| US-2 | 参数不一致 | TC-SF-015 |
| US-3 | optype 非法 | TC-SF-016 |
| US-4 | 检测不通过 | TC-SF-028 |
| US-5 | 外发部分失败/重试 | TC-SF-036, TC-SF-037 |
| US-6 | 飞书不可达 | TC-SF-038, TC-SF-040 |
| US-7 | 超限不可重试 | TC-SF-041 |
| US-8 | 通知人越权 | TC-SF-017 |
| US-9 | 多库多表 | TC-SF-001 |

---

## 七、文档信息

| 属性 | 值 |
|------|-----|
| 文档版本 | v1.1 |
| 创建日期 | 2026-07-20 |
| 更新日期 | 2026-07-21 |
| 关联需求 | REQ-DSS-1.22.4-003（数据外发飞书多维表格_需求.md） |
| 关联设计 | DES-DSS-1.22.4-003（DataGo飞书多维表格外发AppConn_设计.md） |
| 被测模块 | dss-datago-feishu-appconn |
| 被测节点 | linkis.appconn.datagofeishu（节点显示名 sendfeishu，DataGo飞书多维表格外发） |
| 用例总数 | 51（P0: 15，P1: 23，P2: 13） |
| AC 覆盖率 | 15/15 = 100% |
| 异常码覆盖率 | 9/9 = 100%（82001~82009；82011 为防御性不可达） |

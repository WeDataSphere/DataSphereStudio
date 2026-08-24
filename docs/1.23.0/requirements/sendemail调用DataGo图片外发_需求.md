# sendemail 调用 DataGo 数据外发图片消息 需求文档

| 属性 | 值 |
|------|-----|
| 需求编号 | REQ-DSS-1.23.0-002 |
| 需求名称 | sendemail 节点飞书投递改走 DataGo 数据外发图片消息接口 |
| 需求类型 | 重构（REFACTOR） |
| 优先级 | P1 |
| 状态 | 开发中 |
| 版本 | dev-1.23.0 |
| 所属模块 | dss-sendemail-appconn |
| 关联接口 | [数据外发图片消息_接口文档.md](../design/数据外发图片消息_接口文档.md) |
| 关联设计 | [sendemail调用DataGo图片外发_设计.md](../design/sendemail调用DataGo图片外发_设计.md) |
| 替代需求 | [sendemail飞书发送_需求.md](sendemail飞书发送_需求.md)（fass-core 直发） |

---

## 一、功能背景

### 1.1 当前痛点

sendemail 节点现有飞书投递（1.23.0 已开发）直连 fass-core 发模板消息：

- **无敏感数据检测**：报表图片直接发飞书，存在敏感数据外泄风险。
- **无图片持久化审计**：图片不落 HDFS，无法事后回溯。
- **治理分散**：飞书投递未纳入 DataGo 统一外发治理通道，与 DataGo 多维表格外发的合规管线割裂。
- **认证复杂**：FS-* 签名认证维护成本高，且与 DataGo 服务间鉴权方式不一致。

### 1.2 期望价值

- **合规外发**：复用 DataGo 外发治理管线（HDFS 持久化 + Qwen VL OCR + 大乔敏感检测），敏感数据命中阻断。
- **统一鉴权**：改用页面登录 session-token（配置）+ `dss_user_name`（loginUser=工作流 `executeUser`，空则 `submitUser`，来自 runtime 上下文），由 DataGo `SessionTokenService.validate` 校验，与 DataGo 多维表格外发一致；不再用 DSS 固定 Token + IP 白名单。
- **审计可溯**：图片存 HDFS 入库，长期保留可回溯。
- **降低维护成本**：删除 fass-core 直发的 FS-* 签名、templateCode、img_key 模板参数等逻辑，飞书投递收口到 DataGo。

---

## 二、功能概述

### 2.1 一句话描述

将 sendemail 节点的飞书投递链路净替换为调用 DataGo 数据外发图片消息接口（`/api/outbound/send` type=image + `/api/outbound/task`），提交文本（邮件主题）+ 图片附件 + 接收人，DataGo 受理后异步完成敏感检测与飞书图片消息投递，sendemail 同步阻塞轮询终态。

### 2.2 目标用户

- DSS 工作流开发者（配置 sendemail 节点参数）
- DSS 工作流运维人员（配置 DataGo 外发参数）
- 数据分析人员（接收工作流执行结果的飞书用户）

---

## 三、功能需求

### 3.1 核心功能 P0

| 编号 | 功能项 | 描述 |
|:----:|-------|------|
| F-P0-01 | 飞书外发开关 | 沿用 `sendFeishu` 节点参数（不变），true 时执行飞书外发 |
| F-P0-02 | 飞书接收者配置 | 沿用 `feishuTo` 节点参数（不变），分号分隔工号 |
| F-P0-03 | 文本消息内容 | 以邮件 subject 作为 `text` 提交，空则兜底 "DSS Email Notification"（满足接口 text 必填非空） |
| F-P0-04 | 图片附件外发 | 仅图片附件（PngAttachment/image/*/.png）提交为 `images`，File/Base64 双模式取 File，逐张 ≤10MB |
| F-P0-05 | 受理接口调用 | 调 ① `POST /api/outbound/send`（multipart，source=dss,type=image），取 taskId |
| F-P0-06 | 轮询终态 | 调 ② `POST /api/outbound/task` 轮询，exported 成功 / detected_fail/detect_error/export_failed 失败 |
| F-P0-07 | 失败处理 | 飞书外发失败时 sendemail 节点标记失败；终态失败 DataGo 已通知接收人，不重发 |

### 3.2 增强功能 P1

| 编号 | 功能项 | 描述 |
|:----:|-------|------|
| F-P1-01 | 配置校验 | 进入外发流程时校验 base.url/session.token/source/path 非空，轮询/重试参数为正 |
| F-P1-02 | 502/504 重试 | 受理①与轮询②遇 502/504（上游不可达）退避重试，次数/间隔可配 |
| F-P1-03 | 轮询超时保护 | **每批**阻塞轮询超 max.wait（默认 1800s=30min/批，每批独立 deadline）仍非终态则失败 |
| F-P1-04 | HTTP 超时可配 | 连接/读取超时可配 |
| F-P1-05 | recipients 过滤感知 | DataGo 拒 `v_` 前缀外包与 `hadoop`/`hduser` 等系统用户前缀，过滤后为空返回 400；sendemail 不预过滤，400 透传为受理失败（81002） |
| F-P1-06 | loginUser 来源 | `dss_user_name`=工作流 `executeUser`，空则 `submitUser`，从 runtimeMap 解析后透传（= HDFS 上传归属用户）；不落配置 |
| F-P1-07 | 多图按张数分批 | DataGo `/api/outbound/send` 单请求 part 数上限 20（超 500）；按 `image.batch.maxcount`（默认 10）分批，每批一次受理+轮询；0=不分批 |
| F-P1-08 | 失败原因透传 | 节点报错带 `原因：<异常 getMessage>`；81002/81003 desc 带 DataGo 原始响应体；81004 desc 带 `resultSummary` |

### 3.3 功能不包含

| 编号 | 不包含项 | 说明 |
|:----:|---------|------|
| N-01 | fass-core 直发保留 | 净替换，删除直发代码，不留 direct 模式开关 |
| N-02 | 飞书群消息 | 仅按接收人工号发个人消息（DataGo 内部投递） |
| N-03 | 仅飞书不邮件 | 飞书外发依赖邮件发送成功（不变） |
| N-04 | datago-feishu-appconn 联动 | 落点全在 sendemail，不引入/不修改 datago-feishu-appconn |
| N-05 | 敏感检测实现 | 检测由 DataGo 内部完成，sendemail 透明 |

---

## 四、输入输出

### 4.1 输入

**配置项输入**（appconn.properties）：

| 配置项 | 类型 | 默认值 | 说明 |
|-------|------|--------|------|
| wds.dss.appconn.datago.outbound.api.base.url | String | "" | DataGo 基础地址（直连含端口；DSS 前置/UAT 带 `/cui` 前缀） |
| wds.dss.appconn.datago.outbound.session.token | String | "" | 页面登录 session-token（Authorization: Bearer） |
| wds.dss.appconn.datago.outbound.source | String | "dss" | 任务来源 |
| wds.dss.appconn.datago.outbound.channel | String | "feishu" | 渠道 |
| wds.dss.appconn.datago.outbound.send.path | String | "/api/outbound/send" | ① 受理路径 |
| wds.dss.appconn.datago.outbound.task.path | String | "/api/outbound/task" | ② 轮询路径 |
| wds.dss.appconn.datago.outbound.poll.interval | Int | 10 | 轮询间隔（秒） |
| wds.dss.appconn.datago.outbound.max.wait | Int | 1800 | 每批轮询超时（秒），1800=30min/批 |
| wds.dss.appconn.datago.outbound.retry.max | Int | 3 | 502/504 重试次数 |
| wds.dss.appconn.datago.outbound.retry.interval | Int | 30 | 重试间隔（秒） |
| wds.dss.appconn.datago.outbound.image.maxsize | Int | 10485760 | 单图字节上限 |
| wds.dss.appconn.datago.outbound.image.batch.maxcount | Int | 10 | 按张数分批，每批≤10（DataGo part 上限 20）；0=不分批 |
| wds.dss.appconn.datago.outbound.http.connect.timeout | Int | 10000 | 连接超时（毫秒） |
| wds.dss.appconn.datago.outbound.http.read.timeout | Int | 60000 | 读取超时（毫秒） |

**节点参数输入**（runtimeMap）：

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| sendFeishu | Boolean/String | false | 是否发送飞书 |
| feishuTo | String | "" | 飞书接收人工号列表，分号分隔 |
| executeUser | String | "" | 工作流执行人；作为 `dss_user_name`（loginUser）首选 |
| submitUser | String | "" | 工作流提交人；executeUser 为空时回退为 `dss_user_name` |

> `dss_user_name`（loginUser）= `executeUser`，空则 `submitUser`，从 runtimeMap 解析；不落配置。

### 4.2 输出

**飞书投递**：DataGo 检测通过后以飞书真图片消息投递到 feishuTo 指定接收人（DataGo 内部完成）。

**执行结果**：
- 外发成功（exported）：sendemail 节点返回成功
- 外发失败（终态失败/受理失败/轮询失败/超时）：节点返回失败，错误消息"飞书发送失败！"

---

## 五、业务规则

### 5.1 执行流程

```
sendemail 节点执行
  |
  +--> Step 1: 邮件发送（不变）
  |      +--> 失败 → 返回错误，不执行飞书外发
  |      +--> 成功 → 继续
  |
  +--> Step 2: 飞书外发判断
         +--> sendFeishu != true → 跳过，返回成功
         +--> feishuTo 空 → 跳过，返回成功
         +--> sendFeishu=true 且 feishuTo 非空 → 执行外发
                +--> DataGoOutboundConfig.validate()
                +--> text = subject 兜底
                +--> recipients = feishuTo → JSON 数组
                +--> 图片附件准备（≤10MB 校验）
                +--> ① submitImage → taskId（502/504 重试）
                +--> ② 轮询终态（502/504 重试，max.wait 超时保护）
                |       exported → 成功
                |       detected_fail/error/export_failed → 失败（DataGo 已通知）
                |       pending/detecting/pass → 继续轮询
                +--> 失败 → 返回错误
                +--> 成功 → 返回成功
```

### 5.2 关键业务规则

| 编号 | 规则 | 说明 |
|:----:|------|------|
| BR-01 | 飞书外发可选 | sendFeishu 默认 false，不影响邮件发送 |
| BR-02 | 飞书外发在邮件之后 | 邮件失败不走飞书 |
| BR-03 | text 必填非空 | subject 兜底保证 text 非空（接口硬约束） |
| BR-04 | 图片可选 | images 0~N 张，非图片附件不外发（与现状一致） |
| BR-05 | 终态失败不重发 | DataGo 已通知接收人，sendemail 仅标记节点失败 |
| BR-06 | 同步阻塞轮询 | 在 execute 线程内轮询至终态或超时 |
| BR-07 | recipients 过滤在 DataGo 侧 | 不预过滤 `v_`/系统用户前缀；过滤后为空 DataGo 返回 400，sendemail 透传为受理失败 |
| BR-08 | 鉴权用 session-token | 请求带 `Authorization: Bearer <session-token>` + `Cookie: dss_user_name=<loginUser>`；loginUser=`executeUser`（空则 `submitUser`）；token 无效/过期/无 username -> 401 |
| BR-09 | 多图按张数分批 | 单请求 part 数 >20 DataGo 返回 500；按 `image.batch.maxcount`（默认 10）分批，每批 ≤10 张 < 20；每批一次受理+轮询，每批独立 30min 超时；fail-fast |

---

## 六、验收标准

| 编号 | 验收标准 | 验证方式 |
|:----:|---------|---------|
| AC-01 | sendFeishu=false 时仅发邮件，不调 DataGo | 执行节点，日志无 DataGo 调用 |
| AC-02 | sendFeishu=true 且 feishuTo 有值时邮件+飞书均发 | 执行节点，验证飞书收到图片消息 |
| AC-03 | feishuTo 空时仅发邮件 | sendFeishu=true 不设 feishuTo，验证仅发邮件 |
| AC-04 | 外发失败时节点失败 | 配置无效 session-token，验证节点失败 |
| AC-05 | 图片 >10MB 拒绝 | 构造超大图片，验证抛 81006 |
| AC-06 | 多接收人（分号分隔） | feishuTo="u1;u2"，验证两人收到 |
| AC-07 | base.url/session.token 空时抛配置异常 | sendFeishu=true 但 session-token 空，验证抛 IllegalArgumentException |
| AC-08 | 502/504 自动重试 | 模拟上游不可达，验证按配置重试 |
| AC-09 | 轮询超时失败 | max.wait 内未终态，验证抛 81005 |
| AC-10 | 终态失败标记节点失败 | detected_fail 终态，验证节点失败且不重发 |
| AC-11 | recipients 过滤后为空被拒 | feishuTo 全为 `v_`/系统用户前缀，DataGo 返回 400，sendemail 标记节点失败（81002） |
| AC-12 | loginUser 取 executeUser/submitUser | runtimeMap 有 executeUser 时以其为 dss_user_name；executeUser 空则取 submitUser；日志可见 loginUser |
| AC-13 | 多图按张数分批 | 25 张图（batch.maxcount=10）→ 3 批（10/10/5），每批 ≤10 part < 20，无 500；收件人收 3 条消息，全部图片投递 |
| AC-14 | 失败原因可见 | 受理/轮询/终态失败时节点报错带 `原因：...`（含 DataGo 原始响应体 / resultSummary） |

---

## 七、影响范围

### 7.1 代码变更

| 文件 | 变更类型 | 说明 |
|------|:-------:|------|
| outbound/OutboundTaskStatus.scala | 新增 | 状态枚举 |
| outbound/DataGoOutboundConfig.scala | 新增 | 配置读取/校验 |
| outbound/DataGoOutboundClient.scala | 新增 | ①② 接口客户端 |
| outbound/DataGoImageSender.scala | 新增 | 外发编排 |
| SendEmailAppConnConfiguration.scala | 修改 | 删 FEISHU_*，增 DATAGO_OUTBOUND_* |
| SendEmailRefExecutionOperation.scala | 修改 | 调用改 DataGoImageSender |
| appconn.properties | 修改 | 配置项替换 |
| feishu/FeishuClient.scala | 删除 | fass-core 直发 |
| feishu/FeishuMessageSender.scala | 删除 | fass-core 直发编排 |
| feishu/FeishuConfig.scala | 删除 | fass-core 配置 |
| FeishuMessageSenderTest.java | 删除 | 旧单测（由 outbound 测试替代） |
| outbound/DataGoImageSenderTest.java | 新增 | 新单测 |

### 7.2 兼容性影响

| 影响项 | 影响程度 | 说明 |
|-------|:-------:|------|
| 邮件发送 | 无 | 飞书外发为独立 Step 2 |
| 节点参数 | 无 | sendFeishu/feishuTo 语义不变 |
| 现有工作流 | 无 | sendFeishu 默认 false |
| Email 接口 | 无 | feishuTo 字段不变 |
| 配置文件 | **需升级** | feishu.app.* 删除，替换为 datago.outbound.*；鉴权 `token` 改 `session.token`；`dss_user_name` 不落配置，取 runtime 的 executeUser/submitUser |

### 7.3 依赖项

| 依赖 | 类型 | 说明 |
|------|------|------|
| DataGo outbound 通道 | 外部依赖 | 需支持 type=image + source=dss，且 recipients 过滤规则生效 |
| DataGo 鉴权 | 外部依赖 | 页面登录 session-token + dss_user_name（`SessionTokenService.validate`；loginUser=工作流 executeUser/submitUser） |
| 网络连通性 | 基础设施 | DSS 可访问 DataGo（直连 3003；DSS 前置/UAT 走 `/cui` 前缀） |

---

## 八、风险与约束

| 编号 | 风险/约束 | 等级 | 应对措施 |
|:----:|---------|:----:|---------|
| R-01 | 接口 v0.3 待联调 | 中 | session-token 来源/TTL、401 语义、img_key 细节待 DataGo 确认，不影响实现 |
| R-02 | 同步阻塞轮询占用线程 | 中 | max.wait 默认 1800s=30min/批上限（每批独立），超时即失败 |
| R-03 | DataGo 不可达 | 中 | 502/504 退避重试，耗尽标记节点失败 |
| R-04 | 图片超大 | 低 | ≤10MB 前置校验，超限抛 81006 |
| R-05 | OCR/检测耗时不确定 | 中 | 轮询超时兜底，通常 <1min |
| R-06 | session-token 过期 | 中 | 长期/调度执行需长效 token 或刷新机制，待与 DataGo 确认 |

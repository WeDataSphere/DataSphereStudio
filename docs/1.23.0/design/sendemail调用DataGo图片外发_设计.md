# sendemail 调用 DataGo 数据外发图片消息 设计文档

| 属性 | 值 |
|------|-----|
| 设计编号 | DES-DSS-1.23.0-002 |
| 关联需求 | REQ-DSS-1.23.0-002 |
| 版本 | dev-1.23.0 |
| 所属模块 | dss-sendemail-appconn |
| 关联接口 | [数据外发图片消息_接口文档.md](./数据外发图片消息_接口文档.md) |
| 替代设计 | [sendemail飞书发送_设计.md](./sendemail飞书发送_设计.md)（fass-core 直发，本期净替换） |

---

## 一、设计概述

本设计将 sendemail appconn 的飞书投递链路从「直连 fass-core 发模板消息」**净替换**为「调用 DataGo 数据外发图片消息接口」：

- **受理**：`POST /api/outbound/send`（`multipart/form-data`，`type=image`），提交文本 + 图片 + 接收人。
- **轮询**：`POST /api/outbound/task`，按 `taskId` 轮询直到终态。
- **投递**：DataGo 内部完成 HDFS 持久化 → Qwen VL OCR → 大乔敏感检测 → fass-core 图片消息投递，对 sendemail 透明。

替换后 sendemail 获得敏感数据检测、图片 HDFS 审计、统一外发治理能力；删除原 fass-core 直发的 FS-* 签名、templateCode、img_key 模板参数等逻辑，飞书投递统一收口到 DataGo 治理通道。

**落点约束**：实现全部在 `dss-sendemail-appconn` 内，不引入、不修改 `dss-datago-feishu-appconn`。

### 1.1 与旧设计的关系

| 维度 | 旧设计（fass-core 直发） | 新设计（DataGo 外发） |
|------|--------------------------|----------------------|
| 投递通道 | 直连 fass-core `/feishu/external/access/*` | DataGo `/api/outbound/send` + `/api/outbound/task` |
| 鉴权 | FS-AppId/Nonce/Timestamp/Signature/Source 签名 | `Authorization: Bearer <session-token>` + `Cookie: dss_user_name=<loginUser>`（session-token 配置注入；loginUser=工作流 executeUser，空则取 submitUser，来自 runtime 上下文） |
| 内容组装 | sendemail 拼 templateCode + params(imgKeys) | sendemail 提交 text+images，DataGo 组装 `DATAGO_NOTIFY` |
| 敏感检测 | 无 | Qwen VL OCR + 大乔检测（DataGo 内部） |
| 图片持久化 | 无 | HDFS 入库审计（DataGo 内部） |
| 同步性 | 同步逐接收人 sendMessage | 受理后同步阻塞轮询终态 |
| 失败通知 | sendemail 抛异常 | 终态失败 DataGo 已通知接收人，sendemail 仅标记节点失败 |

---

## 二、整体架构

### 2.1 模块架构图

```
                         SendEmailRefExecutionOperation
                         (执行入口，编排邮件 + 飞书外发)
                         /                          \
                        /                            \
        Step 1: 邮件发送                     Step 2: 飞书外发（DataGo）
        emailSender.send()                   DataGoImageSender.send(email)
                       |                            |
                       |                    +-------+--------+
                       |                    |                |
                       |             DataGoOutboundConfig  DataGoOutboundClient
                       |             (配置读取/校验)      (① 受理 + ② 轮询)
                       |                              /    |    \
                       |                             /     |     \
                       |                    multipart上传  JSON轮询  502/504重试
                       |
          Email 接口（feishuTo 字段，不变）
               |
          AbstractEmail（实现 feishuTo，不变）
               ^
               |
     AbstractEmailGenerator
     （从 runtimeMap 读取 feishuTo，不变）
```

### 2.2 调用时序

```
工作流引擎
    |
    v
SendEmailRefExecutionOperation.execute(requestRef)
    |
    +--> emailGenerator.generateEmail(requestRef)   // 生成邮件（含 feishuTo）
    +--> emailSender.send(email)                    // Step 1: 邮件发送
    |
    +--> 从 runtimeMap 读取 sendFeishu
    +--> if (sendFeishu == true
    |       && email.getFeishuTo != null
    |       && email.getFeishuTo.trim.nonEmpty)
    |       |
    |       +--> DataGoImageSender.send(email)      // Step 2: 飞书外发
    |               |
    |               +--> DataGoOutboundConfig.validate()
    |               +--> text = subject 非空兜底
    |               +--> recipients = feishuTo 分号分隔 → JSON 数组
    |               +--> 图片附件准备（File/Base64 双模式 + ≤10MB 校验）
    |               +--> DataGoOutboundClient.submitImage()        // ① 受理（含 502/504 重试）
    |               +--> loop: DataGoOutboundClient.queryTask()    // ② 轮询（含 502/504 重试）
    |                       exported               → 成功
    |                       detected_fail/error/export_failed → 抛 81004
    |                       pending/detecting/pass → sleep 继续
    |                       超时                   → 抛 81005
    |
    +--> return ExecutionResponseRef
```

---

## 三、详细设计

### 3.1 配置项设计

**文件**：`SendEmailAppConnConfiguration.scala`

**删除旧配置项**（fass-core 直发）：

| 配置键 | 说明 |
|--------|------|
| `wds.dss.appconn.feishu.app.id` | 删除 |
| `wds.dss.appconn.feishu.app.token` | 删除 |
| `wds.dss.appconn.feishu.source` | 删除 |
| `wds.dss.appconn.feishu.template.code` | 删除 |
| `wds.dss.appconn.feishu.api.base.url` | 删除 |

**新增配置项**（DataGo 外发）：

| 配置键 | CommonVars 变量 | 类型 | 默认值 | 说明 |
|--------|------------------|------|--------|------|
| `wds.dss.appconn.datago.outbound.api.base.url` | DATAGO_OUTBOUND_API_BASE_URL | String | "" | DataGo 基础地址（含端口，默认 3003；DSS 前置/UAT 需加 `/cui` 前缀，如 `http://uat.dss.bdap.weoa.com/cui`） |
| `wds.dss.appconn.datago.outbound.session.token` | DATAGO_OUTBOUND_SESSION_TOKEN | String | "" | 页面登录 session-token（`Authorization: Bearer <session-token>`，由 DataGo `SessionTokenService.validate` 校验） |
| `wds.dss.appconn.datago.outbound.source` | DATAGO_OUTBOUND_SOURCE | String | "dss" | 任务来源，固定 dss |
| `wds.dss.appconn.datago.outbound.channel` | DATAGO_OUTBOUND_CHANNEL | String | "feishu" | 渠道，本期仅飞书 |
| `wds.dss.appconn.datago.outbound.send.path` | DATAGO_OUTBOUND_SEND_PATH | String | "/api/outbound/send" | ① 受理路径 |
| `wds.dss.appconn.datago.outbound.task.path` | DATAGO_OUTBOUND_TASK_PATH | String | "/api/outbound/task" | ② 轮询路径 |
| `wds.dss.appconn.datago.outbound.poll.interval` | DATAGO_OUTBOUND_POLL_INTERVAL | Int | 10 | 轮询间隔（秒） |
| `wds.dss.appconn.datago.outbound.max.wait` | DATAGO_OUTBOUND_MAX_WAIT | Int | 1800 | **每批**轮询超时上限（秒）；1800=30min/批 |
| `wds.dss.appconn.datago.outbound.retry.max` | DATAGO_OUTBOUND_RETRY_MAX | Int | 3 | 502/504 重试次数 |
| `wds.dss.appconn.datago.outbound.retry.interval` | DATAGO_OUTBOUND_RETRY_INTERVAL | Int | 30 | 重试退避间隔（秒） |
| `wds.dss.appconn.datago.outbound.image.maxsize` | DATAGO_OUTBOUND_IMAGE_MAXSIZE | Int | 10485760 | 单图字节上限（10MB，接口硬约束） |
| `wds.dss.appconn.datago.outbound.image.batch.maxcount` | DATAGO_OUTBOUND_IMAGE_BATCH_MAXCOUNT | Int | 10 | 按张数分批，每批图片数上限（DataGo multipart part 上限 20，默认 10 留余量；0=不分批） |
| `wds.dss.appconn.datago.outbound.http.connect.timeout` | DATAGO_OUTBOUND_HTTP_CONNECT_TIMEOUT | Int | 10000 | 连接超时（毫秒） |
| `wds.dss.appconn.datago.outbound.http.read.timeout` | DATAGO_OUTBOUND_HTTP_READ_TIMEOUT | Int | 60000 | 读取超时（毫秒） |

**设计决策**：
- 连接配置通过 CommonVars 管理，与现有邮件配置风格一致。
- 是否发送飞书仍由 sendemail 节点参数 `sendFeishu` 控制（不变）。
- 轮询/重试/超时参数化，便于不同环境调优。
- 鉴权改用页面登录 session-token + `dss_user_name`（接口文档 v0.2，去 DSS 固定 Token + IP 白名单）；sendemail 为服务端执行，session-token 由配置项注入，`dss_user_name`（loginUser）取工作流 runtime 的 `executeUser`（空则 `submitUser`，见 3.6）。**session-token 存在 TTL**，长期/调度执行需长效 token 或刷新机制（见九、待联调）。

### 3.2 DataGoOutboundConfig 配置校验

**文件**：`outbound/DataGoOutboundConfig.scala`（新增）

**职责**：读取和校验 DataGo 外发配置。

```scala
object DataGoOutboundConfig extends Logging {
  def getApiBaseUrl: String
  def getSessionToken: String      // 页面登录 session-token
  def getSource: String
  def getChannel: String
  def getSendPath: String
  def getTaskPath: String
  def getPollInterval: Int       // 秒
  def getMaxWait: Int            // 秒
  def getRetryMax: Int
  def getRetryInterval: Int      // 秒
  def getImageMaxSize: Int       // 字节
  def getConnectTimeout: Int     // 毫秒
  def getReadTimeout: Int        // 毫秒

  def validate(): Unit = {
    // base.url 非空、session.token 非空、source 非空、path 非空
    // poll.interval/max.wait/retry 正数校验
  }
}
```

**校验逻辑**：
- 仅当节点 `sendFeishu=true` 且 `feishuTo` 非空、实际进入外发流程时调用 `validate()`。
- 校验失败抛 `IllegalArgumentException`（快速失败，不做静默降级）。

### 3.3 OutboundTaskStatus 状态枚举

**文件**：`outbound/OutboundTaskStatus.scala`（新增）

```scala
object OutboundTaskStatus {
  // 非终态
  val PENDING = "pending"
  val DETECTING = "detecting"
  val DETECTED_PASS = "detected_pass"
  // 终态
  val EXPORTED = "exported"                 // 成功
  val DETECTED_FAIL = "detected_fail"       // 命中敏感
  val DETECT_ERROR = "detect_error"         // 处理异常
  val EXPORT_FAILED = "export_failed"       // 投递失败

  def isTerminal(status: String): Boolean
  def isSuccess(status: String): Boolean
  def isFailedTerminal(status: String): Boolean
}
```

### 3.4 DataGoOutboundClient 接口客户端

**文件**：`outbound/DataGoOutboundClient.scala`（新增）

**职责**：封装 DataGo 外发接口 ①② 调用。

#### 3.4.1 鉴权

所有请求（①②）附加两个 Header：

- `Authorization: Bearer <session-token>` —— session-token 来自配置项 `wds.dss.appconn.datago.outbound.session.token`。
- `Cookie: dss_user_name=<loginUser>` —— loginUser 取自工作流 runtime 的 `executeUser`（空则 `submitUser`），由 `SendEmailRefExecutionOperation` 从 runtimeMap 解析后透传；同时作为图片 HDFS 上传归属用户（审计可溯到实际执行人）。

> 等价形式：亦可用 `Session-Token` 头单独传页面登录 token（接口文档 2.1）。本设计统一采用 `Authorization: Bearer` + `Cookie: dss_user_name` 形式（与 sendemail_image.txt 请求样例一致）。token 无效/过期/无 username -> 401。该鉴权仅对 `source=dss` 生效。

#### 3.4.2 ① 受理 — submitImage

```
submitImage(text, recipientsJson, title, images: Array[File], loginUser: String): Long
    |
    +--> POST {baseUrl}/api/outbound/send  (multipart/form-data; charset=utf-8)
    |    Header: Authorization: Bearer <session-token>
    |            Cookie: dss_user_name=<loginUser>
    |    Fields: source=dss, type=image, channel=feishu,
    |            text={text}, recipients={recipientsJson}, title={title},
    |            images={file1}, images={file2}, ...  (一图一段)
    |
    +--> 解析响应 {success, code, message, data:{taskId, status, imageCount}}
    +--> 成功 --> 返回 data.taskId
    +--> 失败 --> 抛 EmailSendFailedException(81002)
    |            （含 recipients 过滤后为空等 400、401、413、500）
    +--> 502/504 --> 退避重试（retry.max/retry.interval），耗尽抛 81002
```

> **recipients 过滤（DataGo 侧，权威）**：DataGo 不接受 `v_` 前缀用户（外包）与系统用户（`hadoop`/`hduser` 等前缀）；过滤后为空返回 400。sendemail 仅做 trim + 去空，**不预过滤**（避免与 DataGo 规则重复漂移），过滤结果以 DataGo 响应为准。

**multipart 构造**：手写 boundary，文本字段（source/type/channel/text/recipients/title）以 form 字段提交，`images` 字段对每张图片重复一段（`Content-Disposition: form-data; name="images"; filename="xxx"`）。参照旧 `FeishuClient.uploadFile` 的 multipart 范式。

#### 3.4.3 ② 轮询 — queryTask

```
queryTask(taskId: Long, loginUser: String): TaskQueryResult  // 返回 (status, resultSummary)
    |
    +--> POST {baseUrl}/api/outbound/task  (application/json; charset=utf-8)
    |    Header: Authorization: Bearer <session-token>
    |            Cookie: dss_user_name=<loginUser>
    |            Content-Type: application/json; charset=utf-8
    |    Body: {"taskId":N,"source":"dss"}
    |
    +--> 解析响应 {success, code, message, data:{taskId, status, resultSummary, ...}}
    +--> 成功 --> 返回 TaskQueryResult(data.status, data.resultSummary)
    +--> 失败 --> 抛 EmailSendFailedException(81003)（desc 带原始响应体）
    +--> 502/504 --> 退避重试，耗尽抛 81003
```

> **resultSummary**：失败终态时承载原因（如「命中敏感数据」「OCR 失败」）。`pollUntilTerminal` 终态时日志带 `resultSummary`，81004 desc 也带，便于定位。

#### 3.4.4 HTTP 工具方法

```scala
// 通用 POST（multipart / json 两种 contentType）
private def sendPost(url, body bytes, contentType, isMultipart): String
// 读取 HTTP 响应（区分成功流 2xx / 错误流）
private def readResponse(connection): String
// 轻量 JSON 字段提取（不依赖 JSON 库）
private def getFieldFromJson(json, field): String
// JSON 字符串转义
def escapeJson(value: String): String
```

**设计决策**：
- 不引入外部 JSON 库（Gson/Jackson），使用正则提取 JSON 字段，避免依赖冲突（与旧 FeishuClient 一致）。
- HTTP 使用 JDK 原生 `HttpURLConnection`，不引入 OkHttp/HttpClient（模块 pom 仅有 httpclient，但旧实现选 JDK 原生，保持一致）。
- 响应读取区分成功流（2xx）和错误流（非 2xx）。

### 3.5 DataGoImageSender 外发编排

**文件**：`outbound/DataGoImageSender.scala`（新增）

**职责**：编排飞书外发流程。

```
send(email: Email, loginUser: String): Unit      // loginUser=executeUser(空则submitUser)，由调用方传入
    |
    +--> 1. 校验 feishuTo（null/空/纯空格 → 跳过返回）
    +--> 2. DataGoOutboundConfig.validate()
    +--> 3. text = subject 非空兜底（null/blank → "DSS Email Notification"）
    +--> 4. recipients = feishuTo.split(";").map(trim).filter(nonEmpty)
    |       空集合 → warn 跳过返回
    |       → JSON 数组字符串 ["u1","u2"]
    |       （不预过滤 v_/系统用户，过滤在 DataGo 侧，见 3.4.2）
    +--> 5. 图片附件准备
    |       images = attachments.filter(isImageAttachment).map(prepareImageFile)
    |       prepareImageFile: File 直接用 / Base64 解码写临时文件；逐张校验 ≤ image.maxsize
    |       失败 → 抛 81006；finally 清理临时文件
    +--> 6. 按张数分批：batches = images.grouped(image.batch.maxcount)
    |       （默认 10/批；0=不分批）
    |       原因：DataGo /api/outbound/send 单请求 multipart part 数上限 20，超过返回 500
    |       → 每批 ≤ batch.maxcount 张图（< 20，留余量）
    +--> 7. 逐批：taskId = DataGoOutboundClient.submitImage(text, recipients, title=subject, 该批, loginUser)  // ①
    |       轮询终态（每批独立 deadline = now + max.wait*1000，默认 1800s=30min/批）
    |       exported            → 该批成功
    |       detected_fail / detect_error / export_failed → 抛 81004（fail-fast，后续批不再发）
    |       pending / detecting / detected_pass → sleep(poll.interval*1000)
    |       now > deadline      → 抛 81005
    +--> 8. 全部 exported → 成功（每批 = 一条飞书消息，收件人收到 ⌈N/batch.maxcount⌉ 条）
```

**图片附件准备双模式**：

| 模式 | 触发条件 | 说明 |
|------|---------|------|
| File 直接上传 | `attachment.getFile` 非空且文件存在 | 性能优，无需解码 |
| Base64 临时文件 | `getFile` 为空或文件不存在 | Base64 解码写临时文件，finally 块清理 |

**图片判定**（复用旧逻辑）：
```scala
attachment.isInstanceOf[PngAttachment] ||
  Option(attachment.getMediaType).exists(_.toLowerCase.startsWith("image/")) ||
  Option(attachment.getName).exists(_.toLowerCase.endsWith(".png"))
```

**异常码设计**：

| 异常码 | 含义 | 触发场景 |
|:------:|------|---------|
| 81001 | 配置缺失 | base.url/session.token 未配置（desc 含具体缺哪项 + 配置键） |
| 81002 | 受理失败 | ① 非 2xx 或业务失败（400/401/413/500，重试耗尽）；**desc 带 DataGo 原始响应体** |
| 81003 | 轮询失败 | ② 非 2xx 或业务失败（重试耗尽）；**desc 带 DataGo 原始响应体** |
| 81004 | 终态非 exported | detected_fail/detect_error/export_failed（fail-fast，后续批不再发；desc 带 `resultSummary` 失败原因） |
| 81005 | 轮询超时 | 超 max.wait 仍非终态（每批独立 deadline） |
| 81006 | 图片附件准备失败 | >10MB / 格式不符 / Base64 解码失败（带 contentPrefix 预览） |
| 81007 | 响应解析失败 | 响应非 JSON / 缺字段 / 无 body |

> 节点报错统一带 `原因：<异常 getMessage>`（`putErrorMsg`），即上述任一异常的 desc 都会在工作流节点错误里显示。

### 3.6 SendEmailRefExecutionOperation 集成

**文件**：`SendEmailRefExecutionOperation.scala`

**变更**：import 从 `FeishuMessageSender` 改为 `DataGoImageSender`，`execute` 中 Step 2 从 runtimeMap 解析 loginUser（`executeUser`，空则 `submitUser`）并调用 `DataGoImageSender.send(email, loginUser)`。逻辑结构不变（sendFeishu 判断、tryCatch 包装、失败 putErrorMsg）。

```scala
// Step 2: 发送到飞书（DataGo 外发，可选）
val runtimeMap = requestRef.getExecutionRequestRefContext.getRuntimeMap
val sendFeishu = Option(runtimeMap.get("sendFeishu")).exists(_.toString.equalsIgnoreCase("true"))
if (sendFeishu && email.getFeishuTo != null && email.getFeishuTo.trim.nonEmpty) {
  // dss_user_name = 工作流 executeUser，空则 submitUser（loginUser = DataGo HDFS 上传归属用户）
  val executeUser = Option(runtimeMap.get("executeUser")).map(_.toString).filter(_.nonEmpty).getOrElse("")
  val submitUser = Option(runtimeMap.get("submitUser")).map(_.toString).filter(_.nonEmpty).getOrElse("")
  val loginUser = if (executeUser.nonEmpty) executeUser else submitUser
  logger.info(s"Feishu sending is selected and feishuTo is configured: ${email.getFeishuTo}, loginUser: ${loginUser}")
  Utils.tryCatch {
    DataGoImageSender.send(email, loginUser)
    logger.info("Feishu sending completed successfully.")
  } { t =>
    return putErrorMsg("飞书发送失败！", t)
  }
} else if (sendFeishu) {
  logger.warn("Feishu sending is selected but feishuTo is empty, skip Feishu sending.")
}
```

**关键设计决策**：
- 飞书外发在邮件发送之后执行（邮件失败直接 return，不走飞书）。
- 飞书外发条件：`sendFeishu=true` AND `feishuTo` 非空（不变）。
- 飞书外发失败时节点标记失败（不静默忽略）。
- 终态失败（81004）DataGo 已通知接收人，sendemail 仅标记节点失败，不重发飞书。

### 3.7 不变项

| 组件 | 说明 |
|------|------|
| `Email.java` getFeishuTo/setFeishuTo | 接口不变 |
| `AbstractEmail.scala` feishuTo 字段 | 实现不变 |
| `AbstractEmailGenerator.scala` 读取 feishuTo | 逻辑不变 |
| 邮件发送链路 | 完全不变 |
| `sendFeishu`/`feishuTo` 节点参数语义 | 不变（触发条件、分号分隔接收者） |

---

## 四、删除清单

以下文件在本期净替换中删除：

| 文件 | 原职责 |
|------|--------|
| `feishu/FeishuClient.scala` | fass-core 文件上传 + 模板消息发送（FS-* 签名） |
| `feishu/FeishuMessageSender.scala` | fass-core 直发编排 |
| `feishu/FeishuConfig.scala` | fass-core 连接配置校验 |
| `src/test/.../feishu/FeishuMessageSenderTest.java` | 旧直发单测（由 outbound 包测试替代） |

**引用同步**：
- `SendEmailRefExecutionOperation.scala`：import 与调用改为 `DataGoImageSender`。
- `SendEmailAppConnConfiguration.scala`：删除 5 个 `FEISHU_*` 配置项。
- `appconn.properties`：删除 feishu.app.* 注释项，新增 datago.outbound.* 注释项。

---

## 五、数据模型

### 5.1 配置数据模型

| 配置键 | CommonVars 变量 | 类型 | 默认值 |
|-------|----------------|------|--------|
| wds.dss.appconn.datago.outbound.api.base.url | DATAGO_OUTBOUND_API_BASE_URL | String | "" |
| wds.dss.appconn.datago.outbound.session.token | DATAGO_OUTBOUND_SESSION_TOKEN | String | "" |
| wds.dss.appconn.datago.outbound.source | DATAGO_OUTBOUND_SOURCE | String | "dss" |
| wds.dss.appconn.datago.outbound.channel | DATAGO_OUTBOUND_CHANNEL | String | "feishu" |
| wds.dss.appconn.datago.outbound.send.path | DATAGO_OUTBOUND_SEND_PATH | String | "/api/outbound/send" |
| wds.dss.appconn.datago.outbound.task.path | DATAGO_OUTBOUND_TASK_PATH | String | "/api/outbound/task" |
| wds.dss.appconn.datago.outbound.poll.interval | DATAGO_OUTBOUND_POLL_INTERVAL | Int | 10 |
| wds.dss.appconn.datago.outbound.max.wait | DATAGO_OUTBOUND_MAX_WAIT | Int | 1800 |
| wds.dss.appconn.datago.outbound.retry.max | DATAGO_OUTBOUND_RETRY_MAX | Int | 3 |
| wds.dss.appconn.datago.outbound.retry.interval | DATAGO_OUTBOUND_RETRY_INTERVAL | Int | 30 |
| wds.dss.appconn.datago.outbound.image.maxsize | DATAGO_OUTBOUND_IMAGE_MAXSIZE | Int | 10485760 |
| wds.dss.appconn.datago.outbound.image.batch.maxcount | DATAGO_OUTBOUND_IMAGE_BATCH_MAXCOUNT | Int | 10 |
| wds.dss.appconn.datago.outbound.http.connect.timeout | DATAGO_OUTBOUND_HTTP_CONNECT_TIMEOUT | Int | 10000 |
| wds.dss.appconn.datago.outbound.http.read.timeout | DATAGO_OUTBOUND_HTTP_READ_TIMEOUT | Int | 60000 |

### 5.2 异常码分配

| 异常码范围 | 用途 |
|-----------|------|
| 81001 | 配置缺失 |
| 81002 | 受理失败 |
| 81003 | 轮询失败 |
| 81004 | 终态非 exported |
| 81005 | 轮询超时 |
| 81006 | 图片附件准备失败 |
| 81007 | 响应解析失败 |

> 与旧直发 8000x、datago-feishu-appconn 8200x 区分。

---

## 六、部署与配置

### 6.1 DataGo 侧准备

1. DataGo 开通 `source=dss` 的 session-token 鉴权（`SessionTokenService.validate` 校验页面登录 session-token）；loginUser 取请求 `Cookie: dss_user_name`（DSS 侧为工作流 executeUser/submitUser），同时作为图片 HDFS 上传归属用户；接口文档 v0.2 已去 DSS 固定 Token + IP 白名单。
2. 确认 DataGo outbound 通道已支持 `type=image` + `source=dss`，且 recipients 过滤规则（拒 `v_` 前缀外包、`hadoop`/`hduser` 等系统用户前缀）生效。
3. 确认 DSS 服务器可访问 DataGo（直连默认端口 3003；DSS 前置/UAT 走 `/cui` 前缀，如 `http://uat.dss.bdap.weoa.com/cui`）。

### 6.2 DSS 配置步骤

在 `appconn.properties` 中添加：

```properties
# DataGo 数据外发图片消息配置（是否发送飞书由 sendemail 节点参数 sendFeishu 控制）
# 直连 DataGo 用 http://DATAGO_HOST:3003；DSS 前置/UAT 需带 /cui 前缀
wds.dss.appconn.datago.outbound.api.base.url=http://uat.dss.bdap.weoa.com/cui
wds.dss.appconn.datago.outbound.session.token=xxxxxxxxxxxxxxxx
wds.dss.appconn.datago.outbound.source=dss
wds.dss.appconn.datago.outbound.channel=feishu
# wds.dss.appconn.datago.outbound.send.path=/api/outbound/send
# wds.dss.appconn.datago.outbound.task.path=/api/outbound/task
# wds.dss.appconn.datago.outbound.poll.interval=10
# wds.dss.appconn.datago.outbound.max.wait=1800   # 每批轮询超时（秒），1800=30min/批
# wds.dss.appconn.datago.outbound.retry.max=3
# wds.dss.appconn.datago.outbound.retry.interval=30
# wds.dss.appconn.datago.outbound.image.maxsize=10485760
# wds.dss.appconn.datago.outbound.image.batch.maxcount=10   # 按张数分批，每批≤10（DataGo part 上限 20）；0=不分批
# wds.dss.appconn.datago.outbound.http.connect.timeout=10000
# wds.dss.appconn.datago.outbound.http.read.timeout=60000
```

**升级提示**：
- 原 `wds.dss.appconn.feishu.app.*` 配置项已删除，需替换为上述 `datago.outbound.*`。
- 鉴权由「DSS 固定 Token + IP 白名单」改为「页面登录 session-token + `dss_user_name` cookie」：原 `wds.dss.appconn.datago.outbound.token` 改为 `session.token`；`dss_user_name`（loginUser）不再配置，取工作流 runtime 的 `executeUser`（空则 `submitUser`）。
- `session.token` 为页面登录 session-token，**存在 TTL**，需定期更新或与 DataGo 确认长效 token/刷新机制（见九）。

### 6.3 sendemail 节点配置

节点参数不变：

```
sendFeishu=true
feishuTo=zhangsan;lisi
```

---

## 七、性能考虑

| 场景 | 性能影响 | 优化措施 |
|------|---------|---------|
| 同步阻塞轮询 | 占用执行线程，OCR+检测+投递通常 <1min | max.wait 默认 1800s=30min/批，每批独立 deadline，超时即失败 |
| 多图片上传 | 与图片大小成正比 | 流式上传，逐张 ≤10MB 校验前置 |
| 图片数 >20 张 | DataGo 单请求 part 上限 20，超过 500 | 按 `image.batch.maxcount`（默认 10）分批，每批一次受理+轮询；N 张→⌈N/10⌉ 批，最坏 ⌈N/10⌉×30min |
| 502/504 重试 | 退避等待 | 可配次数/间隔，默认 3 次/30s |
| 多接收人 | DataGo 内部逐接收人投递 | sendemail 每批受理一次，不逐人调用 |

---

## 八、安全考虑

| 安全项 | 措施 |
|-------|------|
| 凭据存储 | session-token 配置文件明文存储，与邮件密码同等保护级别，需限制配置文件访问权限（`dss_user_name` 不落配置，取自 runtime） |
| 鉴权 | 页面登录 session-token（配置）+ `dss_user_name` cookie（loginUser=工作流 executeUser/submitUser）；token 无效/过期/无 username -> 401 |
| loginUser 归属 | loginUser 同时作为图片 HDFS 上传归属用户（不再用接收人首位），审计可溯到实际操作人 |
| 敏感数据 | DataGo 内部 Qwen VL OCR + 大乔检测，命中阻断（sendemail 透明） |
| 图片审计 | DataGo 内部 HDFS 二进制直传持久化（v0.3，去 base64），可回溯 |
| 网络安全 | 支持 HTTPS（base.url 配置） |

---

## 九、待联调确认

接口文档 v0.3 / sendemail_image.txt 请求样例对齐后的待确认项（不影响 sendemail 实现，仅影响部署配置）：

1. **session-token 来源与生命周期**：sendemail 为服务端执行，session-token 由配置注入（`dss_user_name`=executeUser/submitUser 取自 runtime）；session-token 存在 TTL，长期/调度执行需长效 token 或刷新机制，待与 DataGo 确认。
2. 401 鉴权失败消息（`无效的 session-token 鉴权`）的语义确认（token 无效/过期/无 username）。
3. fass-core img_key 字段名（DataGo 内部，对 sendemail 透明）。
4. `/cui` DSS 前置/UAT 前缀生效确认（请求样例 `http://uat.dss.bdap.weoa.com/cui/api/outbound/...`）。
5. 502/504 退避重试的次数/间隔建议值。
6. recipients 过滤规则（`v_` 前缀外包、`hadoop`/`hduser` 系统用户前缀）以 DataGo 实际生效为准。

---

## 十、变更记录

| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 | 2026-08-06 | 净替换 sendemail 飞书投递为 DataGo 数据外发图片消息接口；删除 fass-core 直发；新增 outbound 包 |
| v1.1 | 2026-08-14 | 对齐接口文档 v0.3 与 sendemail_image.txt 样例：鉴权改页面登录 session-token + `Cookie: dss_user_name`（去 DSS 固定 Token + IP 白名单）；配置项 `token` 改 `session.token` 并新增 `dss.user.name`；base.url 标注 `/cui` 前缀；明确 DataGo recipients 过滤（v_/系统用户）；② queryTask 补 Cookie 头 |
| v1.2 | 2026-08-17 | `dss_user_name`（loginUser）不再配置项注入，改取工作流 runtime 的 `executeUser`（空则 `submitUser`）：删除 `wds.dss.appconn.datago.outbound.dss.user.name` 配置项与 `getDssUserName`；`send`/`submitImage`/`queryTask` 透传 `loginUser`；`SendEmailRefExecutionOperation` 从 runtimeMap 解析 loginUser |
| v1.3 | 2026-08-17 | 图片 Base64 鲁棒解码：剥离 commons-codec 分块 CRLF 与 `data:...;base64,` 数据 URI 前缀，标准→MIME 解码回退，失败带 `contentPrefix` 预览；图片分 Content-Type 改按扩展名（`image/png` 等，对齐 curl）；①/② 请求参数与响应全量 INFO 日志（不截断） |
| v1.4 | 2026-08-18 | 多图按张数分批：DataGo `/api/outbound/send` 单请求 multipart part 数上限 20（超 500），新增 `image.batch.maxcount`（默认 10，0=不分批）按批 `submitImage→pollUntilTerminal`；`max.wait` 默认 120→1800（30min/批，每批独立 deadline）；81002/81003 desc 改带 DataGo 原始响应体；`putErrorMsg` 统一带 `原因：<getMessage>` |

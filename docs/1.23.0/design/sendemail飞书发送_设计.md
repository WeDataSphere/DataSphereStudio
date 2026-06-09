# sendemail节点飞书发送功能 设计文档

| 属性 | 值 |
|------|-----|
| 设计编号 | DES-DSS-1.23.0-001 |
| 关联需求 | REQ-DSS-1.23.0-001 |
| 版本 | dev-1.23.0 |
| 所属模块 | dss-sendemail-appconn |

---

## 一、设计概述

本设计在DSS工作流sendemail节点现有邮件发送功能的基础上，新增飞书消息发送能力。采用"后置增强"模式，在邮件发送成功后，根据节点参数和飞书接收者决定是否执行飞书发送，对现有代码侵入最小。

---

## 二、整体架构

### 2.1 模块架构图

```
                         SendEmailRefExecutionOperation
                         (执行入口，编排邮件+飞书发送)
                         /                          \
                        /                            \
        Step 1: 邮件发送                     Step 2: 飞书发送（新增）
        emailSender.send()                   FeishuMessageSender.send()
                       |                            |
                       |                    +-------+--------+
                       |                    |                |
                       |             FeishuConfig      FeishuClient
                       |             (配置校验)      (API客户端)
                       |                              /    |    \
                       |                             /     |     \
                       |                    Token管理  文件上传  消息发送
                       |
          Email接口（新增feishuTo字段）
               |
          AbstractEmail（实现feishuTo）
               ^
               |
     AbstractEmailGenerator
     （从runtimeMap读取feishuTo）
```

### 2.2 调用时序

```
工作流引擎
    |
    v
SendEmailRefExecutionOperation.execute(requestRef)
    |
    +--> sendEmailAppConnHooks.preGenerate()
    +--> emailGenerator.generateEmail(requestRef)
    |       |
    |       +--> AbstractEmailGenerator.generateEmailInfo()
    |       |       |
    |       |       +--> 从runtimeMap读取feishuTo --> email.setFeishuTo()
    |       |
    |       +--> generateEmailContent()
    |
    +--> sendEmailAppConnHooks.preSend()
    +--> emailSender.send(email)                    <-- Step 1: 邮件发送
    |
    +--> 从runtimeMap读取sendFeishu
    +--> if (sendFeishu == true
    |       && email.getFeishuTo != null
    |       && email.getFeishuTo.trim.nonEmpty)
    |       |
    |       +--> FeishuMessageSender.send(email)     <-- Step 2: 飞书发送
    |               |
    |               +--> FeishuConfig.validate()     <-- 配置校验
    |               +--> 解析feishuTo（分号分隔）
    |               +--> FeishuClient.sendTextMessage()  (主题通知)
    |               +--> uploadAttachment() * N           (附件上传)
    |               |       +--> FeishuClient.uploadFile()
    |               +--> FeishuClient.sendFileMessage() * N * M  (文件消息)
    |
    +--> return ExecutionResponseRef
```

---

## 三、详细设计

### 3.1 Email接口扩展

**文件**: `Email.java`

**变更**: 新增feishuTo字段存取方法

```java
public interface Email {
    // ... 原有方法 ...

    /**
     * 获取飞书接收者open_id列表（分号分隔）
     */
    String getFeishuTo();

    /**
     * 设置飞书接收者open_id列表
     * @param feishuTo 分号分隔的open_id字符串
     */
    void setFeishuTo(String feishuTo);
}
```

**设计决策**:
- feishuTo与to/cc/bcc字段对齐，采用分号分隔的字符串格式
- 不使用独立的FeishuEmail子接口，避免接口膨胀
- 默认值为null（AbstractEmail中），运行时由AbstractEmailGenerator从runtimeMap读取并设置为空字符串

### 3.2 AbstractEmail实现

**文件**: `AbstractEmail.scala`

**变更**: 实现feishuTo字段

```scala
class AbstractEmail extends Email {
  // ... 原有字段 ...
  private var feishuTo: String = _

  override def getFeishuTo: String = feishuTo
  override def setFeishuTo(feishuTo: String): Unit = this.feishuTo = feishuTo
}
```

### 3.3 配置项设计

**文件**: `SendEmailAppConnConfiguration.scala`

**新增系统连接配置项**:

| 配置键 | 类型 | 默认值 | 说明 |
|-------|------|-------|------|
| wds.dss.appconn.feishu.app.id | String | "" | 飞书应用App ID |
| wds.dss.appconn.feishu.app.secret | String | "" | 飞书应用App Secret |
| wds.dss.appconn.feishu.api.base.url | String | https://open.feishu.cn/open-apis | 飞书API基础地址 |

```scala
val FEISHU_APP_ID = CommonVars("wds.dss.appconn.feishu.app.id", "")
val FEISHU_APP_SECRET = CommonVars("wds.dss.appconn.feishu.app.secret", "")
val FEISHU_API_BASE_URL = CommonVars("wds.dss.appconn.feishu.api.base.url", "https://open.feishu.cn/open-apis")
```

**设计决策**:
- 连接配置项通过CommonVars管理，与现有邮件配置风格一致
- 是否发送飞书不放在配置文件中，由sendemail节点参数 `sendFeishu` 控制
- 支持自定义api.base.url，便于代理部署场景

### 3.4 FeishuConfig配置校验

**文件**: `FeishuConfig.scala`（新增）

**职责**: 读取和校验飞书配置

```scala
object FeishuConfig extends Logging {
  def getAppId: String = SendEmailAppConnConfiguration.FEISHU_APP_ID.getValue
  def getAppSecret: String = SendEmailAppConnConfiguration.FEISHU_APP_SECRET.getValue
  def getApiBaseUrl: String = SendEmailAppConnConfiguration.FEISHU_API_BASE_URL.getValue

  def validate(): Unit = {
    if (getAppId.isEmpty)
      throw new IllegalArgumentException("Feishu app.id is not configured.")
    if (getAppSecret.isEmpty)
      throw new IllegalArgumentException("Feishu app.secret is not configured.")
  }
}
```

**校验逻辑**:
- FeishuConfig只负责连接配置读取和校验，不负责判断节点是否发送飞书
- 仅当节点sendFeishu=true且feishuTo非空、实际进入飞书发送流程时调用validate()
- 校验失败抛出IllegalArgumentException（快速失败，不做静默降级）
- 不校验apiBaseUrl（默认值可用，自定义地址由用户自行保证正确性）

### 3.5 FeishuClient飞书API客户端

**文件**: `FeishuClient.scala`（新增）

**职责**: 封装飞书开放平台API调用

#### 3.5.1 Tenant Token管理

```
getTenantAccessToken()
    |
    +--> 缓存未过期? --> 返回缓存token
    |
    +--> 缓存过期 --> refreshTenantToken()
                          |
                          +--> POST /auth/v3/tenant_access_token/internal
                          |    Body: {"app_id":"xxx","app_secret":"xxx"}
                          |
                          +--> 成功 --> 缓存token，设置过期时间（expire - 5分钟）
                          +--> 失败 --> 抛出EmailSendFailedException(80002)
```

**缓存策略**:
- 使用synchronized保证线程安全
- 提前5分钟（TOKEN_REFRESH_MARGIN = 300000ms）刷新，避免token在使用中过期
- 缓存字段: `tenantToken: String`, `tokenExpireTime: Long`

#### 3.5.2 文件上传

```
uploadFile(file: File, fileName: String): String
    |
    +--> POST /im/v1/files (multipart/form-data)
    |    Header: Authorization: Bearer {token}
    |    Fields: file_type=stream, file_name={fileName}, file={fileBytes}
    |
    +--> 成功 --> 返回 file_key
    +--> 失败 --> 抛出EmailSendFailedException(80003)
```

#### 3.5.3 消息发送

```
sendTextMessage(receiveId, receiveIdType, text): Unit
    |
    +--> POST /im/v1/messages?receive_id_type={receiveIdType}
    |    Header: Authorization: Bearer {token}
    |    Body: {"receive_id":"xxx","msg_type":"text","content":"{\"text\":\"xxx\"}"}
    |
    +--> 特殊字符转义: 双引号 -> \"，换行 -> \n
    +--> 成功 --> 无返回值
    +--> 失败 --> 抛出EmailSendFailedException(80004)

sendFileMessage(receiveId, receiveIdType, fileKey): Unit
    |
    +--> POST /im/v1/messages?receive_id_type={receiveIdType}
    |    Header: Authorization: Bearer {token}
    |    Body: {"receive_id":"xxx","msg_type":"file","content":"{\"file_key\":\"xxx\"}"}
    |
    +--> 成功 --> 无返回值
    +--> 失败 --> 抛出EmailSendFailedException(80004)
```

#### 3.5.4 HTTP工具方法

```scala
// 通用POST请求
private def sendPostRequest(urlStr, body, contentType, authHeader): String

// 读取HTTP响应（区分成功/失败流）
private def readResponse(connection): String

// 轻量JSON字段提取（不依赖JSON库）
private def getFieldFromJson(json, field): String
```

**设计决策**:
- 不引入外部JSON库（如Gson/Jackson），使用正则提取JSON字段，避免依赖冲突
- HTTP使用JDK原生HttpURLConnection，不引入HttpClient/OkHttp
- 响应读取区分成功流（2xx）和错误流（非2xx）

### 3.6 FeishuMessageSender消息发送编排

**文件**: `FeishuMessageSender.scala`（新增）

**职责**: 编排飞书消息发送流程

```
send(email: Email): Unit
    |
    +--> 1. 校验feishuTo（null/空/纯空格 -> 跳过）
    +--> 2. FeishuConfig.validate()（配置校验）
    +--> 3. 解析接收者: feishuTo.split(";").map(_.trim).filter(_.nonEmpty)
    +--> 4. 发送主题文本消息
    |       for each receiver:
    |           FeishuClient.sendTextMessage(receiver, "open_id", "[DSS邮件通知] " + subject)
    |           失败 --> EmailSendFailedException(80006)
    |
    +--> 5. 发送附件文件消息
            for each attachment:
                fileKey = uploadAttachment(attachment)
                for each receiver:
                    FeishuClient.sendFileMessage(receiver, "open_id", fileKey)
                    失败 --> EmailSendFailedException(80008)

uploadAttachment(attachment: Attachment): String
    |
    +--> attachment.getFile != null && file.exists()?
    |       YES --> FeishuClient.uploadFile(file, fileName)
    |       NO  --> Base64解码 -> 写入临时文件 -> uploadFile -> 删除临时文件
```

**附件上传双模式设计**:

| 模式 | 触发条件 | 说明 |
|------|---------|------|
| File直接上传 | attachment.getFile()非null且文件存在 | 性能优，无需解码重编码 |
| Base64临时文件上传 | attachment.getFile()为null或文件不存在 | 兼容性保证，创建临时文件后上传，finally块清理 |

**异常码设计**:

| 异常码 | 含义 | 触发场景 |
|:------:|------|---------|
| 80002 | 飞书Token获取失败 | appId/appSecret错误、网络不通 |
| 80003 | 飞书文件上传失败 | 文件损坏、网络中断 |
| 80004 | 飞书消息发送失败 | 无效open_id、接收者不存在 |
| 80005 | 飞书HTTP响应无body | 服务端异常 |
| 80006 | 飞书主题消息发送失败 | sendTextMessage异常 |
| 80007 | 飞书附件上传失败 | uploadAttachment异常 |
| 80008 | 飞书文件消息发送失败 | sendFileMessage异常 |

### 3.7 SendEmailRefExecutionOperation集成

**文件**: `SendEmailRefExecutionOperation.scala`

**变更**: execute方法中新增飞书发送逻辑

```scala
override def execute(requestRef): ExecutionResponseRef = {
  val email = ... // 生成邮件（原有逻辑）

  // Step 1: 发送邮件
  Utils.tryCatch {
    emailSender.send(email)
  } { t =>
    return putErrorMsg("发送邮件失败！", t)
  }

  // Step 2: 发送到飞书（可选）
  val runtimeMap = requestRef.getExecutionRequestRefContext.getRuntimeMap
  val sendFeishu = Option(runtimeMap.get("sendFeishu")).exists(_.toString.equalsIgnoreCase("true"))
  if (sendFeishu && email.getFeishuTo != null && email.getFeishuTo.trim.nonEmpty) {
    logger.info(s"Feishu sending is selected and feishuTo is configured: ${email.getFeishuTo}")
    Utils.tryCatch {
      FeishuMessageSender.send(email)
      logger.info("Feishu sending completed successfully.")
    } { t =>
      return putErrorMsg("飞书发送失败！", t)
    }
  }

  new ExecutionResponseRefBuilder().success()
}
```

**关键设计决策**:
- 飞书发送在邮件发送之后执行（邮件失败直接return，不走飞书）
- 飞书发送条件：sendFeishu=true AND feishuTo非null AND feishuTo.trim非空
- 飞书发送失败时，节点标记为失败（不静默忽略）
- 使用Utils.tryCatch包装，保持与邮件发送一致的异常处理风格

### 3.8 AbstractEmailGenerator参数读取

**文件**: `AbstractEmailGenerator.scala`

**变更**: generateEmailInfo方法中读取feishuTo

```scala
val feishuTo = if (runtimeMap.get("feishuTo") != null) runtimeMap.get("feishuTo").toString else ""
email.setFeishuTo(feishuTo)
```

**设计决策**:
- 与to/cc/bcc字段读取方式对齐
- runtimeMap中无feishuTo时默认为空字符串（非null），避免后续null判断复杂性

---

## 四、飞书开放平台集成设计

### 4.1 飞书应用配置要求

| 配置项 | 值/要求 |
|-------|--------|
| 应用类型 | 自建应用 |
| 权限 - 消息 | `im:message:send_as_bot`（获取与发送单聊、群组消息） |
| 权限 - 文件 | `im:resource`（上传文件和图片） |
| 事件订阅 | 无需配置（非事件回调模式） |
| 机器人 | 需启用机器人能力 |

### 4.2 飞书API调用清单

| API | 方法 | 用途 | 限流 |
|-----|------|------|------|
| /auth/v3/tenant_access_token/internal | POST | 获取Tenant Access Token | 无特殊限制 |
| /im/v1/files | POST(multipart) | 上传文件 | 50次/分钟 |
| /im/v1/messages | POST | 发送消息 | 100次/分钟 |

### 4.3 接收者标识类型

当前固定使用 `open_id` 类型。飞书支持的接收者标识类型：

| 类型 | 说明 | 当前支持 |
|------|------|:-------:|
| open_id | 用户在应用内的唯一标识 | 支持 |
| user_id | 用户在企业内的唯一标识 | 不支持 |
| chat_id | 群聊ID | 不支持 |

---

## 五、数据模型

### 5.1 Email接口字段变更

| 字段 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| feishuTo | String | null（AbstractEmail中）/ ""（Generator设置后） | 飞书接收者open_id列表，分号分隔 |

### 5.2 配置数据模型

| 配置键 | CommonVars变量 | 类型 | 默认值 |
|-------|---------------|------|-------|
| wds.dss.appconn.feishu.app.id | FEISHU_APP_ID | String | "" |
| wds.dss.appconn.feishu.app.secret | FEISHU_APP_SECRET | String | "" |
| wds.dss.appconn.feishu.api.base.url | FEISHU_API_BASE_URL | String | https://open.feishu.cn/open-apis |

### 5.3 异常码分配

| 异常码范围 | 用途 |
|-----------|------|
| 80001 | （保留） |
| 80002 | 飞书Token获取失败 |
| 80003 | 飞书文件上传失败 |
| 80004 | 飞书消息发送失败 |
| 80005 | 飞书HTTP响应异常 |
| 80006 | 飞书主题消息发送失败 |
| 80007 | 飞书附件上传失败 |
| 80008 | 飞书文件消息发送失败 |

---

## 六、部署与配置

### 6.1 飞书应用创建步骤

1. 登录飞书开放平台（https://open.feishu.cn）
2. 创建自建应用，获取App ID和App Secret
3. 开通权限：`im:message:send_as_bot`、`im:resource`
4. 启用机器人能力
5. 发布应用

### 6.2 DSS配置步骤

在 `appconn.properties` 中添加以下配置：

```properties
# 飞书连接配置（是否发送飞书由sendemail节点参数sendFeishu控制）
wds.dss.appconn.feishu.app.id=cli_xxxxxxxxxxxx
wds.dss.appconn.feishu.app.secret=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
# 自定义API地址（可选，默认为飞书官方地址）
# wds.dss.appconn.feishu.api.base.url=https://open.feishu.cn/open-apis
```

### 6.3 sendemail节点配置

在工作流sendemail节点的参数中，新增sendFeishu和feishuTo字段：

```
sendFeishu=true
feishuTo=ou_xxxxxxxxxxxxxxxx;ou_yyyyyyyyyyyyyyyy
```

---

## 七、性能考虑

| 场景 | 性能影响 | 优化措施 |
|------|---------|---------|
| Token缓存 | 首次获取约200ms，后续缓存命中0ms | 缓存+提前5分钟刷新 |
| 文件上传 | 与附件大小成正比 | 无特殊优化，使用流式上传 |
| 多接收者 | 消息数 = 接收者数 * (1 + 附件数) | 逐个发送，无批量API |
| 多附件 | 附件数 * (上传 + 接收者数 * 发送) | File直接上传模式避免Base64编解码 |

---

## 八、安全考虑

| 安全项 | 措施 |
|-------|------|
| App Secret存储 | 配置文件明文存储，与邮件密码同等保护级别，需限制配置文件访问权限 |
| Token安全 | 内存缓存，不持久化到磁盘 |
| 接收者验证 | 依赖飞书平台验证open_id有效性，无效ID返回错误 |
| 网络安全 | 支持HTTPS（默认），支持自定义代理地址 |



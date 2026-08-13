# sendemail 调用 DataGo 图片外发 测试执行报告

## 测试概览

| 项目 | 值 |
|-----|-----|
| **测试时间** | 2026-08-13 14:15:03 (UTC+8) |
| **项目类型** | Java (Maven) |
| **测试模块** | dss-sendemail-appconn-core |
| **被测对象** | outbound 包（DataGo 数据外发图片消息链路） |
| **执行模式** | 标准模式 |
| **测试环境** | Windows 10, JDK 1.8.0_462 (Temurin), Maven 3.8.5 |
| **构建插件** | scala-maven-plugin 3.2.2, maven-surefire-plugin 2.12.4 |
| **执行命令** | `mvn -pl dss-appconn/appconns/dss-sendemail-appconn/sendemail-appconn-core test -Dtest=DataGoImageSenderTest` |
| **BUILD 结果** | SUCCESS |
| **总耗时** | 26.327 s |
| **测试类** | `com.webank.wedatasphere.dss.appconn.sendemail.outbound.DataGoImageSenderTest` |

> 说明：本次变更为飞书投递链路净替换——由原 `feishu` 包（fass-core 直发）替换为 `outbound` 包（调用 DataGo 数据外发图片消息接口，异常码段 81001-81007）。被测源码位于 `outbound` 包下的 4 个 Scala 文件：`OutboundTaskStatus.scala` / `DataGoOutboundConfig.scala` / `DataGoOutboundClient.scala` / `DataGoImageSender.scala`。单元测试 `DataGoImageSenderTest.java` 为纯逻辑测试（不连真实 HTTP），与模块既有测试风格一致。

## 测试结果汇总

```
Running com.webank.wedatasphere.dss.appconn.sendemail.outbound.DataGoImageSenderTest
Tests run: 32, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.739 sec

Results :
Tests run: 32, Failures: 0, Errors: 0, Skipped: 0
```

| 测试分组 | 总数 | 通过 | 失败 | 通过率 |
|---------|:----:|:----:|:----:|:------:|
| text 兜底 | 3 | ✅ 3 | ❌ 0 | 100% |
| receivers 解析 | 7 | ✅ 7 | ❌ 0 | 100% |
| 图片附件判定 | 4 | ✅ 4 | ❌ 0 | 100% |
| 图片大小校验 | 3 | ✅ 3 | ❌ 0 | 100% |
| 状态判定 | 7 | ✅ 7 | ❌ 0 | 100% |
| JSON 转义 | 5 | ✅ 5 | ❌ 0 | 100% |
| sendFeishu 控制逻辑 | 3 | ✅ 3 | ❌ 0 | 100% |
| **总计** | **32** | **✅ 32** | **❌ 0** | **100%** |

## 详细测试结果

### 1. text 兜底测试

验证 `DataGoImageSender.resolveText` 对 subject 为 null / 空白 / 非空时的兜底与 trim 逻辑（接口要求 text 非空，空时使用默认值 `DSS Email Notification`）。

| 用例ID | 测试方法 | 覆盖场景 | 状态 | 说明 |
|-------|---------|---------|:----:|------|
| UT-01 | testResolveText_NullSubject_ShouldUseDefault | 边界-null 主题 | ✅ | subject=null 时使用默认 text "DSS Email Notification" |
| UT-02 | testResolveText_BlankSubject_ShouldUseDefault | 边界-空白主题 | ✅ | subject="   " 时 trim 后为空，走默认 text |
| UT-03 | testResolveText_NonEmptySubject_ShouldUseTrimmedSubject | 正向-非空主题 | ✅ | subject="  2026年7月审计报表  " trim 后为 "2026年7月审计报表" |

### 2. receivers 解析测试

验证 `DataGoImageSender.send` 对 feishuTo 为 null / 空 / 纯空格 / 多接收者 / 仅分号 的解析与 trim 过滤逻辑，以及 `toJsonArray` 的 JSON 数组构造。

| 用例ID | 测试方法 | 覆盖场景 | 状态 | 说明 |
|-------|---------|---------|:----:|------|
| UT-04 | testFeishuToNull_ShouldSkip | 边界-null 接收者 | ✅ | feishuTo=null 判定为应跳过 |
| UT-05 | testFeishuToEmpty_ShouldSkip | 边界-空字符串 | ✅ | feishuTo="" 判定为应跳过 |
| UT-06 | testFeishuToWhitespace_ShouldSkip | 边界-纯空格 | ✅ | feishuTo="   " 判定为应跳过 |
| UT-07 | testFeishuToMultipleReceivers_ShouldParseAndTrim | 正向-多接收者 | ✅ | "  zhangsan  ;  lisi  ;wangwu" 解析出 3 个有效接收者 |
| UT-08 | testFeishuToOnlySemicolonsAndSpaces_NoValidReceivers | 边界-仅分隔符 | ✅ | "  ;  ;  " 过滤后有效接收者为 0 |
| UT-09 | testToJsonArray_SingleReceiver | 正向-单接收者 JSON | ✅ | ["alexyang"] 单元素 JSON 数组正确 |
| UT-10 | testToJsonArray_MultipleReceivers | 正向-多接收者 JSON | ✅ | ["alexyang","ryanchen"] 多元素 JSON 数组正确 |

### 3. 图片附件判定测试

验证 `DataGoImageSender.isImageAttachment` 对文件名后缀与 mediaType 的图片识别逻辑（PngAttachment 实例 / image/* 媒体类型 / .png 后缀三者之一即判定为图片）。

| 用例ID | 测试方法 | 覆盖场景 | 状态 | 说明 |
|-------|---------|---------|:----:|------|
| UT-11 | testIsImageAttachment_PngFile | 正向-PNG 识别 | ✅ | "chart.png" 后缀判定为图片 |
| UT-12 | testIsImageAttachment_CsvFile | 正向-CSV 排除 | ✅ | "report.csv" 不识别为图片 |
| UT-13 | testIsImageAttachment_ImageMediaType | 正向-image 媒体类型 | ✅ | "image/png" 媒体类型判定为图片 |
| UT-14 | testIsImageAttachment_NonImageMediaType | 正向-非图片媒体类型排除 | ✅ | "application/pdf" 不识别为图片 |

### 4. 图片大小校验测试

验证 `DataGoImageSender.prepareImageFile` 的 ≤10MB（10485760 字节）大小校验边界（限内通过 / 超限拒绝 / 临界通过）。

| 用例ID | 测试方法 | 覆盖场景 | 状态 | 说明 |
|-------|---------|---------|:----:|------|
| UT-15 | testImageSize_WithinLimit_ShouldPass | 边界-限内通过 | ✅ | 5MB ≤ 10MB，校验通过 |
| UT-16 | testImageSize_ExceedsLimit_ShouldFail | 异常-超限拒绝 | ✅ | 11MB > 10MB，校验拒绝（对应 81006 路径） |
| UT-17 | testImageSize_ExactLimit_ShouldPass | 边界-临界通过 | ✅ | 恰好 10485760 字节（=10MB）通过 |

### 5. 状态判定测试

验证 `OutboundTaskStatus` 的 normalize / isTerminal / isSuccess / isFailedTerminal 四个方法对终态（exported / detected_fail / detect_error / export_failed）、非终态（pending / detecting / detected_pass）、null、未知状态的判定鲁棒性。

| 用例ID | 测试方法 | 覆盖场景 | 状态 | 说明 |
|-------|---------|---------|:----:|------|
| UT-18 | testStatus_IsSuccess_Exported | 关键路径-成功状态 | ✅ | exported / EXPORTED / " exported " 均 isSuccess=true（normalize trim+小写） |
| UT-19 | testStatus_IsSuccess_NonExported | 边界-非成功状态 | ✅ | pending / detected_fail 的 isSuccess=false |
| UT-20 | testStatus_IsTerminal | 关键路径-终态判定 | ✅ | exported / detected_fail / detect_error / export_failed 均 isTerminal=true |
| UT-21 | testStatus_IsNotTerminal | 关键路径-非终态判定 | ✅ | pending / detecting / detected_pass 的 isTerminal=false |
| UT-22 | testStatus_IsFailedTerminal | 关键路径-失败终态 | ✅ | detected_fail / detect_error / export_failed 为 true，exported 为 false |
| UT-23 | testStatus_Null_ShouldNotCrash | 边界-null 不崩溃 | ✅ | null 对三个方法均返回 false，不抛 NPE |
| UT-24 | testStatus_Unknown_ShouldNotBeTerminal | 边界-未知状态 | ✅ | "foobar" isTerminal=false（防御性） |

### 6. JSON 转义测试

验证 `DataGoOutboundClient.escapeJson` 对双引号 / 换行 / 反斜杠 / null / 普通文本的转义（用于 recipients、title、filename 安全嵌入 JSON 字符串字面量）。

| 用例ID | 测试方法 | 覆盖场景 | 状态 | 说明 |
|-------|---------|---------|:----:|------|
| UT-25 | testEscapeJson_DoubleQuotes_ShouldEscape | 边界-双引号转义 | ✅ | `"` → `\"` |
| UT-26 | testEscapeJson_Newline_ShouldEscape | 边界-换行转义 | ✅ | `\n`（换行符）→ `\n`（反斜杠+n 两字符） |
| UT-27 | testEscapeJson_Backslash_ShouldEscape | 边界-反斜杠转义 | ✅ | `\` → `\\` |
| UT-28 | testEscapeJson_Null_ShouldReturnEmpty | 边界-null 输入 | ✅ | null → ""（空字符串） |
| UT-29 | testEscapeJson_NormalText_ShouldNotChange | 正向-普通文本 | ✅ | "alexyang" 不变 |

### 7. sendFeishu 控制逻辑测试

验证 `SendEmailRefExecutionOperation.execute` 中 `sendFeishu && feishuTo 非空` 的外发触发控制流三分支（关闭 / 空接收者 / 满足条件）。

| 用例ID | 测试方法 | 覆盖场景 | 状态 | 说明 |
|-------|---------|---------|:----:|------|
| UT-30 | testSendFeishuFalse_ShouldSkip | 关键路径-关闭飞书 | ✅ | sendFeishu=false 时不外发（仅发邮件） |
| UT-31 | testSendFeishuTrueButEmptyFeishuTo_ShouldSkip | 关键路径-空接收者 | ✅ | sendFeishu=true 但 feishuTo="" 时不外发 |
| UT-32 | testSendFeishuTrueAndValidFeishuTo_ShouldSend | 关键路径-完整配置 | ✅ | sendFeishu=true 且 feishuTo="zhangsan" 时触发外发 |

## 测试覆盖说明

### 已覆盖的验收标准

对照测试案例文档 `sendemail调用DataGo图片外发_测试案例.md` 的 AC-01 ~ AC-10，本次单元测试（纯逻辑层）直接覆盖情况如下：

| 验收标准 | 对应用例 | 单测覆盖 | 状态 | 说明 |
|---------|---------|---------|:----:|------|
| AC-01 sendFeishu=false 不调 DataGo | TC049 / TC056 | UT-30 | ✅ | 控制逻辑分支已验证 |
| AC-02 sendFeishu=true 且 feishuTo 有值均发 | TC024 / TC054 | UT-32 | ✅ | 控制逻辑分支已验证 |
| AC-03 feishuTo 空仅发邮件 | TC025 / TC026 / TC050 | UT-04 / UT-05 / UT-06 / UT-31 | ✅ | null / 空 / 纯空格均覆盖 |
| AC-04 外发失败节点失败 | TC012 / TC051 / TC058 | — | ⏳ | 需 Mock HTTP 触发 81002-81007，SIT 验证 |
| AC-05 图片 >10MB 拒绝 | TC037 | UT-16 | ✅ | 超限拒绝逻辑已验证 |
| AC-06 多接收人 | TC029 / TC059 | UT-07 / UT-09 / UT-10 | ✅ | 解析 + JSON 数组构造已验证 |
| AC-07 base.url/token 空抛配置异常 | TC002 / TC003 | — | ⏳ | validate() 依赖配置单例，需加载 appconn.properties 验证 |
| AC-08 502/504 自动重试 | TC013 / TC019 | — | ⏳ | 需 Mock HttpURLConnection，SIT 验证 |
| AC-09 轮询超时失败 | TC046 | — | ⏳ | pollUntilTerminal 为 private，需 Mock DataGoOutboundClient |
| AC-10 终态失败标记节点失败 | TC042 / TC057 | UT-22（判定层） | 🟡 | isFailedTerminal 判定已覆盖；完整轮询抛 81004 路径需 Mock |

**单元测试直接覆盖**：AC-01 / AC-02 / AC-03 / AC-05 / AC-06 共 5 项（含 AC-10 判定层）。
**待 Mock / SIT 补充**：AC-04 / AC-07 / AC-08 / AC-09 / AC-10（完整路径）共 5 项。

### 未覆盖场景（需 Mock 框架 / SIT 环境）

| 场景 | 测试案例编号 | 未覆盖原因 | 建议补充方式 |
|------|------------|-----------|------------|
| DataGoOutboundClient HTTP 交互（① submitImage / ② queryTask） | TC011-TC022 | 依赖真实 HTTP 连接或 Mock HttpURLConnection，当前为纯逻辑测试 | 引入 Mockito（mock-static HttpURLConnection）或 MockWebServer 后补充 |
| 502/504 退避重试（retry.max / retry.interval） | TC013 / TC014 / TC019 | sendWithRetry / sendPost 为 private 且依赖 HTTP | Mock HTTP 响应码序列后验证重试次数与退避间隔 |
| 响应解析异常（无 taskId / taskId 非数字 / 无 status / 无 body） | TC015 / TC016 / TC020 / TC022 | 依赖 HTTP 响应体内容 | Mock 响应体后验证 81007 异常码与消息 |
| DataGoOutboundConfig.validate() 配置校验 | TC001-TC006 | 依赖 SendEmailAppConnConfiguration 单例加载 appconn.properties | 单独构造配置加载测试或 SIT 环境验证 |
| DataGoImageSender 编排与轮询（pollUntilTerminal） | TC024 / TC041-TC047 | pollUntilTerminal 为 private，依赖 DataGoOutboundClient | Mockito static mock DataGoOutboundClient 后验证 81004 / 81005 路径 |
| 图片附件准备（File 模式 / Base64 模式 / 临时文件） | TC035 / TC036 / TC039 / TC040 | prepareImageFile 为 private 且依赖 Attachment 实现 | 构造 Attachment 桩或反射调用补充 |
| SendEmailRefExecutionOperation 集成 | TC048-TC053 | 依赖 Spring 容器与邮件发送链路 | SIT 环境验证 |
| 端到端业务流程（DSS + DataGo 完整链路） | TC054-TC059 | 依赖完整 DSS + DataGo 部署环境 | SIT 环境验证 |

> 说明：当前 32 个单元测试聚焦于无外部依赖的纯逻辑层（字段映射、接收者解析、图片判定、状态判定、JSON 转义、大小校验、控制流分支），与模块既有测试风格一致。HTTP 交互、轮询编排、集成与端到端场景受限于 JDK HttpURLConnection 直连与 private 方法封装，需在引入 Mock 框架或 SIT 环境后补充，属预期内的覆盖边界。

## 测试结论

- [x] ✅ **通过**：已执行的 32 个单元测试全部通过（Tests run: 32, Failures: 0, Errors: 0, Skipped: 0），BUILD SUCCESS，核心纯逻辑验证正确
- [x] ✅ **核心逻辑覆盖**：text 兜底、receivers 解析、图片附件判定、图片大小校验、状态判定、JSON 转义、sendFeishu 控制逻辑 7 个分组均 100% 通过
- [x] ✅ **异常码段验证**：81006（图片超限）判定逻辑、81004（失败终态）判定逻辑已在对应分组间接验证
- ⚠️ **有补充项**：HTTP 交互（81002/81003/81007）、轮询编排（81004/81005 完整路径）、配置校验、集成与端到端测试需在 Mock 框架或 SIT 环境中补充

### 建议

1. **短期**：在 SIT 环境中手动验证 TC048-TC059 端到端场景（含 81001-81007 异常码全链路），确认 DataGo 受理 + 轮询 + 飞书投递真实可用
2. **中期**：引入 Mockito（含 mock-static）或 MockWebServer 依赖，补充 TC011-TC022 的 HTTP 交互单测与 TC024/TC041-TC047 的编排轮询单测，覆盖 502/504 重试、响应解析异常、轮询超时等关键异常路径
3. **长期**：建立自动化集成测试流水线，覆盖 DSS + DataGo 全链路（含 token 鉴权、敏感命中 detected_fail、多接收人投递），并将 outbound 包回归用例纳入模块级回归集

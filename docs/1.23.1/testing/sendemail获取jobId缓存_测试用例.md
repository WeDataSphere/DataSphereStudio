# sendemail 获取 jobId 缓存 测试案例

## 1. 概述

### 1.1 测试目标

本文档针对 sendemail appconn `EmailCSHelper.getJobIds` 执行内缓存优化（提交 ccdfadfc6，2 文件 +39/-3），基于代码实现生成覆盖缓存命中、首次加载、生命周期清理、并发与边界的测试用例。

### 1.2 测试范围

| 模块 | 文件路径 | 变更类型 |
|------|---------|---------|
| EmailCSHelper | `sendemail-appconn-core/.../cs/EmailCSHelper.scala` | MODIFIED（getJobIds 缓存读写、loadJobIds 抽出、clearJobIdsCache 新增） |
| SendEmailRefExecutionOperation | `sendemail-appconn-core/.../SendEmailRefExecutionOperation.scala` | MODIFIED（execute 以 tryFinally 包装并清理缓存） |

> 受益调用方（未改动、需回归）：`MultiContentEmailGenerator.generateEmailContent`、`EmailCSHelper.getJobTypes`。

### 1.3 需求属性识别

**识别的属性**：后端开发（性能优化，纯 Scala 后端变更，无接口/配置/DB 变化）

**测试用例生成策略**：
- 侧重单元测试（缓存语义、副本防御、清理、并发），依赖 Mock `ExecutionRequestRefContext`/runtimeMap
- SIT 验证多上游节点工作流的端到端行为一致性与日志特征

### 1.4 项目测试框架摘要

```
测试框架: JUnit 4.12
构建工具: Maven（scala-maven-plugin 3.2.2 + maven-surefire-plugin 2.12.4）
单测风格: 纯逻辑测试（不连真实 CS/HTTP），与模块既有 DataGoImageSenderTest 风格一致
```

### 1.5 关键实现要素

| 要素 | 值 |
|------|-----|
| 缓存 key | `__dss_sendemail_job_ids_cache__` |
| 缓存 value | `JobIdsCache(jobIds: Array[Long])`（私有 case class，类型匹配识别） |
| 锁 | `runtimeMap.synchronized`（读写/清理同锁） |
| 副本 | 写入存 clone、命中返回 clone；未命中返回加载结果本身 |
| 清理 | `execute` → `Utils.tryFinally(执行体)(clearJobIdsCache)`，成功/失败/异常均执行 |
| 日志特征 | 命中：`From sendemail execution cache to get Job IDs`；首次：`From cs to getJob ids` / `Job IDs is` |
| 保留异常 | `EmailSendFailedException(80003, "empty result set is not allowed")`（content 空 / jobIds 空） |

---

## 2. 代码变更分析结果

### 2.1 新增/修改方法详情

#### EmailCSHelper.scala

| 方法 | 签名 | 说明 |
|------|------|------|
| getJobIds | `def getJobIds(refContext): Array[Long]`（改造） | 锁内查缓存：命中返回 clone + 日志；未命中 loadJobIds 后写缓存（存 clone）并返回加载结果 |
| loadJobIds | `private def loadJobIds(refContext): Array[Long]`（新增，原逻辑抽出） | contextID 解析 → content 节点列表 → 每节点 getNodeNameByNodeID + getLinkisJobData → jobIds；空则 80003 |
| clearJobIdsCache | `private[sendemail] def clearJobIdsCache(refContext): Unit`（新增） | null 安全 + 同锁 remove 缓存 key |

#### SendEmailRefExecutionOperation.scala

| 方法 | 签名 | 说明 |
|------|------|------|
| execute | `override def execute(requestRef): ExecutionResponseRef`（改造） | `Utils.tryFinally(executeWithJobIdsCache(requestRef))(clearJobIdsCache)` |
| executeWithJobIdsCache | `private def ...`（新增，原 execute 体抽出） | 逻辑逐行不变（hooks→生成→解析→发送→可选 DataGo 外发） |

### 2.2 关键路径分析

| 路径 | 条件 | 预期结果 |
|------|------|---------|
| 路径1：缓存命中 | 同执行内第二次 getJobIds | 返回 clone，无 CS 调用，日志 From sendemail execution cache |
| 路径2：缓存未命中 | 执行内首次 getJobIds | loadJobIds 走 CS，写缓存，返回加载结果 |
| 路径3：执行成功清理 | execute 正常返回（含 putErrorMsg 错误响应） | finally 清理缓存 |
| 路径4：执行异常清理 | 执行体抛异常 | 异常传播前 finally 清理 |
| 路径5：加载失败不写缓存 | content 空 / jobIds 空 → 80003 | 异常冒泡，runtimeMap 无缓存 |
| 路径6：非缓存类型容错 | key 处已存在非 JobIdsCache 值 | 按未命中处理，加载后覆盖 |
| 路径7：并发首次加载 | 多线程同时未命中 | 一个线程加载，其余等锁命中 |

---

## 3. 测试用例

### 3.1 缓存命中与首次加载（EmailCSHelper）

#### TC001：执行内首次获取走 CS 原加载链并写缓存

**前置条件**：runtimeMap 含 content=["node1","node2"]，CS 侧 nodeName/jobId 查询可用（Mock）

**测试步骤**：
1. 构造 Mock refContext（含 runtimeMap）
2. 首次调用 `EmailCSHelper.getJobIds(refContext)`

**预期结果**：
- 返回与升级前一致的 jobIds（顺序 = content 顺序）
- 日志输出 `From cs to getJob ids` 与 `Job IDs is`
- runtimeMap 中写入 key `__dss_sendemail_job_ids_cache__`，value 为 JobIdsCache（内容为返回值的副本）

**优先级**：P0
**覆盖场景**：关键路径 - 首次加载

---

#### TC002：执行内第二次获取命中缓存

**前置条件**：TC001 已完成（缓存已写入）

**测试步骤**：再次调用 `EmailCSHelper.getJobIds(refContext)`

**预期结果**：
- 返回与首次相同的 jobIds（值与顺序）
- 日志输出 `From sendemail execution cache to get Job IDs`
- 不再触发 CS 查询（getNodeNameByNodeID/getLinkisJobData 调用次数不增加）

**优先级**：P0
**覆盖场景**：关键路径 - 缓存命中（核心收益）

---

#### TC003：多次获取仅一次 CS 加载

**前置条件**：同 TC001，循环调用 getJobIds 共 K 次（K≥3）

**预期结果**：CS 加载链（`From cs to getJob ids` 日志）仅出现 1 次，其余 K-1 次均命中缓存

**优先级**：P0
**覆盖场景**：关键路径 - 重复获取去重

---

#### TC004：命中缓存返回数组为副本

**前置条件**：缓存已写入（jobIds=[1,2,3]）

**测试步骤**：
1. 调用 getJobIds 获取副本 A，对其排序为 [3,2,1] 并追加元素 4
2. 再次调用 getJobIds 获取副本 B

**预期结果**：B 仍为 [1,2,3]（原始顺序与长度），缓存未被 A 的修改污染

**优先级**：P0
**覆盖场景**：边界场景 - 副本防御（返回侧）

---

#### TC005：写缓存存副本（加载结果修改不影响缓存）

**前置条件**：首次加载返回数组 R（未命中分支返回加载结果本身）

**测试步骤**：对 R 排序/增删后再次 getJobIds

**预期结果**：再次获取返回原始顺序与长度（缓存写入时已 clone，R 的修改不影响缓存）

**优先级**：P1
**覆盖场景**：边界场景 - 副本防御（写入侧）

---

#### TC006：缓存 key 已存在非 JobIdsCache 类型值时安全覆盖

**前置条件**：runtimeMap 预置 `__dss_sendemail_job_ids_cache__` = 普通字符串（模拟外部污染）

**测试步骤**：调用 getJobIds

**预期结果**：不抛 ClassCastException；按未命中走 CS 加载并以 JobIdsCache 覆盖旧值

**优先级**：P2
**覆盖场景**：边界场景 - 类型容错

---

### 3.2 异常与不写缓存（loadJobIds 原语义）

#### TC007：content 为空列表时抛 80003 且不写缓存

**前置条件**：runtimeMap content="[]"

**测试步骤**：调用 getJobIds

**预期结果**：
- 抛出 `EmailSendFailedException(80003)`，消息为 `empty result set is not allowed`
- runtimeMap 中无缓存 key（加载失败不缓存）

**优先级**：P0
**覆盖场景**：异常场景 - 空结果集（原行为保留）

---

#### TC008：CS 返回 jobIds 为空时抛 80003 且不写缓存

**前置条件**：content 非空但所有节点 jobId 查询结果为空

**预期结果**：同 TC007（80003、无缓存写入）

**优先级**：P1
**覆盖场景**：异常场景 - 空 jobIds（原行为保留）

---

#### TC009：加载失败后同执行内再次调用可重试加载

**前置条件**：首次 getJobIds 因 CS 异常失败（未写缓存）

**测试步骤**：CS 恢复后（Mock 返回正常）再次调用 getJobIds

**预期结果**：再次走 CS 加载链并成功写缓存（失败结果未被缓存）

**优先级**：P1
**覆盖场景**：关键路径 - 失败不缓存可重试

---

### 3.3 生命周期清理（clearJobIdsCache + execute 包装）

#### TC010：clearJobIdsCache 移除缓存

**前置条件**：缓存已写入

**测试步骤**：调用 `EmailCSHelper.clearJobIdsCache(refContext)`

**预期结果**：runtimeMap 中 key 被移除；再次 getJobIds 重新走 CS 加载

**优先级**：P0
**覆盖场景**：关键路径 - 清理生效

---

#### TC011：clearJobIdsCache 空安全

**前置条件**：分别构造 refContext=null、runtimeMap=null

**测试步骤**：调用 `clearJobIdsCache`

**预期结果**：静默返回，不抛 NPE

**优先级**：P1
**覆盖场景**：边界场景 - 空上下文

---

#### TC012：execute 正常成功后缓存被清理

**前置条件**：sendemail 节点执行成功（邮件发送成功，sendFeishu=false 或外发成功）

**测试步骤**：执行节点，观察 execute 返回后 runtimeMap

**预期结果**：`__dss_sendemail_job_ids_cache__` 不存在（tryFinally 成功路径清理）

**优先级**：P0
**覆盖场景**：关键路径 - 成功清理

---

#### TC013：execute 失败（putErrorMsg 路径）后缓存被清理

**前置条件**：邮件发送失败（SMTP 异常）或 DataGo 外发失败，execute 返回错误响应

**预期结果**：错误响应返回前 finally 已执行，缓存被移除

**优先级**：P0
**覆盖场景**：关键路径 - 失败清理

---

#### TC014：execute 执行体抛异常时缓存被清理

**前置条件**：构造执行体抛出未捕获异常（如 hooks 抛 RuntimeException）

**预期结果**：异常向外传播，且传播前 finally 清理缓存

**优先级**：P1
**覆盖场景**：异常场景 - 异常清理

---

### 3.4 并发测试

#### TC015：并发首次获取仅一次 CS 加载

**前置条件**：缓存未写入；起 N（≥8）个线程同时调用 getJobIds（CountDownLatch 对齐起点）

**测试步骤**：并发调用并收集各线程结果

**预期结果**：
- CS 加载链仅执行 1 次（`From cs to getJob ids` 日志仅一条）
- 各线程返回的 jobIds 内容一致
- 无异常、无脏数据

**优先级**：P0
**覆盖场景**：关键路径 - 并发单次加载

---

#### TC016：getJobIds 与 clearJobIdsCache 并发无半态

**前置条件**：缓存已写入；线程 A 循环 getJobIds，线程 B 循环 clearJobIdsCache

**预期结果**：任一时刻 getJobIds 要么拿到完整缓存副本、要么走完整加载，无部分写入状态；最终无异常

**优先级**：P2
**覆盖场景**：边界场景 - 读写竞争

---

### 3.5 端到端（SIT）验证

#### TC017：多上游节点工作流执行 - 日志特征与内容一致

**前置条件**：工作流含 ≥2 个上游节点（产出图片结果集）+ sendemail 节点（category=node）

**测试步骤**：
1. 执行工作流
2. 查看 sendemail 节点日志与收件邮箱

**预期结果**：
- 日志中 `From cs to getJob ids` 至多 1 次；若执行内多次获取，出现 `From sendemail execution cache to get Job IDs`
- 邮件附件（图片）与升级前版本一致
- 节点状态成功

**优先级**：P0
**覆盖场景**：正向场景 - 端到端收益与一致性

---

#### TC018：存量工作流回归 - 行为与升级前一致

**前置条件**：升级前已验证的存量 sendemail 工作流（含 sendFeishu=true 的 DataGo 外发节点）

**测试步骤**：升级后原样执行存量工作流

**预期结果**：邮件/飞书投递行为、节点状态、异常表现与升级前完全一致（缓存对调用方透明）

**优先级**：P0
**覆盖场景**：兼容性 - 存量零影响

---

## 4. 测试用例统计

### 4.1 按优先级分布

| 优先级 | 数量 | 占比 |
|:------:|:----:|:----:|
| P0 | 9 | 50% |
| P1 | 6 | 33% |
| P2 | 3 | 17% |
| **总计** | **18** | **100%** |

### 4.2 按模块分布

| 模块 | 测试用例数 |
|------|:--------:|
| EmailCSHelper 缓存语义（命中/加载/副本/容错） | 6 |
| EmailCSHelper 异常与不写缓存 | 3 |
| 生命周期清理（clearJobIdsCache + execute） | 5 |
| 并发 | 2 |
| 端到端（SIT） | 2 |
| **总计** | **18** |

### 4.3 验收标准覆盖检查

| 验收标准 | 覆盖用例 | 状态 |
|---------|---------|:----:|
| AC-01 重复获取命中缓存 | TC002, TC003, TC017 | OK |
| AC-02 首次加载结果与升级前一致 | TC001, TC017, TC018 | OK |
| AC-03 执行成功后缓存清理 | TC010, TC012 | OK |
| AC-04 执行失败后缓存清理 | TC013, TC014 | OK |
| AC-05 返回副本不被污染 | TC004, TC005 | OK |
| AC-06 并发获取线程安全 | TC015, TC016 | OK |
| AC-07 空结果集行为不变 | TC007, TC008, TC009 | OK |

**覆盖率**：7/7 验收标准（100%）

---

## 5. 自动化测试说明

### 5.1 现状与最终安排

本变更不配套单元测试（按团队安排，本功能随版本回归验证）。提交仅含 2 个主源码文件，本节留存单测可行性结论供后续如需补测时参考。

### 5.2 单测可行性结论（2026-09-15 复核修正）

早版本曾认为 TC004/TC005/TC010/TC015 可"预置缓存绕开 CS 链"，经代码与试跑复核**不成立**，修正如下：

| 用例组 | 可行性 | 原因 |
|-------|--------|------|
| TC004/TC005/TC010/TC015 副本/清理/并发 | **无法绕开 CS 链** | 缓存 value 为 `JobIdsCache`（`EmailCSHelper` object 私有 case class），测试侧无法构造该类型预置缓存；只能经真实 `loadJobIds` 写入 |
| TC001-TC003/TC006-TC009 加载与命中 | 需 mock-static | `loadJobIds` 走 `ContextServiceUtils`/`LinkisJobDataServiceImpl` 静态链；且首步 `getContextIDStrByMap` 会真实反序列化 contextID（非纯本地读 key），空结果集用例也需构造合法 ContextID 或 Mock 该静态方法 |
| TC011 清理空安全 | 高（纯本地） | 仅依赖 refContext/runtimeMap 桩 |
| TC012-TC014 execute 包装 | 需 SIT | 需实例化 Operation 或真实节点执行 |

### 5.3 验证路径（最终）

1. **回归验证**（团队执行）：TC017/TC018 —— 多上游节点 sendemail 工作流日志特征与内容一致性、存量工作流（含 sendFeishu=true 的 DataGo 外发节点）行为不变
2. **SIT**：TC012-TC014（节点成功/失败/异常后缓存清理核查，可结合节点日志与 runtimeMap 检查）
3. 如后续需补单测：引入 Mockito mock-static（CS 静态链）后方可覆盖 TC001-TC010/TC015

---

## 6. 测试结论

- 用例已产出（18 条，P0 9 条），验收标准 7/7 全覆盖。
- 不配套单元测试（按团队安排随版本回归验证）；SIT 验证待版本提测后执行。
- 重点回归方向：多上游节点 sendemail 工作流、存量 DataGo 外发工作流（确认缓存对既有链路透明）。

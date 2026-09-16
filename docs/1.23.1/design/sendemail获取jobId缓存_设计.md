# sendemail 获取 jobId 缓存 设计文档

| 属性 | 值 |
|------|-----|
| 设计编号 | DES-DSS-1.23.1-001 |
| 关联需求 | REQ-DSS-1.23.1-001 |
| 版本 | dev-1.23.1 |
| 所属模块 | dss-sendemail-appconn（sendemail-appconn-core） |
| 关联提交 | ccdfadfc6（`EmailCSHelper.scala`、`SendEmailRefExecutionOperation.scala`，+39/-3） |

---

## 一、设计概述

在 sendemail 节点**单次执行内**为 `EmailCSHelper.getJobIds` 增加执行级缓存：

- **缓存载体**：执行请求上下文的 runtimeMap（`ExecutionRequestRefContext.getRuntimeMap`），key 为内部标识 `__dss_sendemail_job_ids_cache__`，value 为私有样例类 `JobIdsCache(jobIds: Array[Long])`。
- **命中路径**：返回缓存数组的 `clone()` 副本，日志 `From sendemail execution cache to get Job IDs ...`。
- **未命中路径**：走原 CS 加载链（原 `getJobIds` 逻辑原样抽为私有方法 `loadJobIds`），成功后写入缓存（同样存副本）。
- **清理点**：`SendEmailRefExecutionOperation.execute` 以 `Utils.tryFinally` 包装执行体，finally 中调用 `EmailCSHelper.clearJobIdsCache`，成功/失败/异常路径均清理。

**设计原则**：纯缓存层叠加，加载逻辑逐行保留（含 80003 异常语义），对调用方（`MultiContentEmailGenerator`、`getJobTypes`）零侵入、零感知。

### 1.1 优化前后的差异

| 维度 | 优化前 | 优化后 |
|------|--------|--------|
| 一次执行内第 N 次获取 jobIds | 每次完整走 CS：contextID 解析 + 每上游节点 2 次远程查询 | 首次走 CS 并缓存，后续内存读 |
| CS 查询次数（M 个上游节点、执行内获取 K 次） | K × (1 + 2M) 次远程调用 | 1 × (1 + 2M) 次 |
| 缓存生命周期 | 无缓存 | 单次 execute（懒加载 → tryFinally 清理） |
| 行为语义 | — | 不变（jobIds 内容/顺序、80003 异常） |

---

## 二、整体架构

### 2.1 调用关系

```
SendEmailRefExecutionOperation.execute(requestRef)
    |
    +-- Utils.tryFinally( executeWithJobIdsCache(requestRef) )   // 原执行体，逻辑不变
    |                   { EmailCSHelper.clearJobIdsCache(ctx) }  // finally：成功/失败/异常均清理
    |
    +-- executeWithJobIdsCache:
          +-- hooks.preGenerate
          +-- emailGenerator.generateEmail ──> MultiContentEmailGenerator.generateEmailContent
          |                                          |
          |                                          +-- EmailCSHelper.getJobIds(refContext)  ──┐
          +-- emailContentParsers / Generators                                                  |
          +-- hooks.preSend                                                                      |
          +-- emailSender.send(email)                                                            |
          +-- (可选) DataGoImageSender.send                                                       |
                                                                                                 |
    EmailCSHelper.getJobIds:                                                                     |
        runtimeMap.synchronized {                                                                |
          命中 JobIdsCache --> 返回 clone（日志 From sendemail execution cache ...）               |
          未命中 ──── loadJobIds(refContext)  <------------------------------------------------┘
                         +-- getContextIDStrByMap(runtimeMap)          // CS
                         +-- runtimeMap["content"] --> nodeIDs
                         +-- nodeID → getNodeNameByNodeID(contextIDStr, nodeID)   // CS，每节点 1 次
                         +-- nodeName → LinkisJobDataServiceImpl.getLinkisJobData // CS，每节点 1 次
                         +-- .map(_.getJobID) --> jobIds（空则 80003）
                       写入缓存 JobIdsCache(jobIds.clone())
        }
```

### 2.2 缓存数据结构

```scala
object EmailCSHelper extends Logging {

  private val JOB_IDS_CACHE_KEY = "__dss_sendemail_job_ids_cache__"

  private case class JobIdsCache(jobIds: Array[Long])
}
```

- key 带双下划线前后缀，与业务参数（content/sendFeishu/feishuTo 等）区隔，标识内部缓存。
- value 用私有 case class 包装而非裸数组：类型匹配（`case cache: JobIdsCache`）可精确识别"本缓存写入的值"，避免 runtimeMap 中同 key 的其他类型值被误当缓存使用。

---

## 三、详细设计

### 3.1 EmailCSHelper.getJobIds（缓存读写）

```scala
def getJobIds(refContext: ExecutionRequestRefContext): Array[Long] = {
  val runtimeMap = refContext.getRuntimeMap
  runtimeMap.synchronized {
    runtimeMap.get(JOB_IDS_CACHE_KEY) match {
      case cache: JobIdsCache =>
        val cachedJobIds = cache.jobIds.clone()
        info(s"From sendemail execution cache to get Job IDs ${cachedJobIds.toList}.")
        cachedJobIds
      case _ =>
        val jobIds = loadJobIds(refContext)
        runtimeMap.put(JOB_IDS_CACHE_KEY, JobIdsCache(jobIds.clone()))
        jobIds
    }
  }
}
```

**设计决策**：

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 锁对象 | `runtimeMap.synchronized` | 锁与缓存载体同对象，保证"检查→加载→写入"原子性；无需引入独立锁字段，且与 `clearJobIdsCache` 天然互斥 |
| 加载失败不写缓存 | `loadJobIds` 抛异常（80003 等）时直接冒泡，不执行 put | 失败结果（异常）不缓存；同执行内下次调用可重试加载 |
| 副本进出 | 存 `jobIds.clone()`、取 `cache.jobIds.clone()` | `Array[Long]` 可变，防止调用方排序/增删污染缓存；返回值与首次加载结果内容一致 |
| 首次返回原数组 | 未命中分支返回 `jobIds`（加载结果本身），缓存存副本 | 调用方拿到与缓存隔离的引用；命中分支返回缓存副本，两者防御等级一致 |
| 非缓存值容错 | `match { case cache: JobIdsCache => ...; case _ => 加载 }` | runtimeMap 中该 key 若被外部写入非 JobIdsCache 值，按未命中处理并覆盖，不抛 ClassCastException |

### 3.2 EmailCSHelper.loadJobIds（原逻辑抽出）

原 `getJobIds` 方法体原样迁移，**仅变量来源从 `refContext.getRuntimeMap` 改为局部 `runtimeMap`，无任何逻辑改动**：

1. `ContextServiceUtils.getContextIDStrByMap(runtimeMap)` 解析 contextIDStr；
2. `runtimeMap.get("content")` 解析上游节点 ID 列表（String JSON / List 双形态兼容）；空列表抛 `EmailSendFailedException(80003, "empty result set is not allowed")`；
3. 每节点 `getNodeNameByNodeID`（节点 ID→节点名，CS 查询，null 节点名仅记日志继续）；
4. 每节点构造 `CommonContextKey`（PUBLIC/DATA，key=`NODE_PREFIX + nodeName + JOB_ID`），`LinkisJobDataServiceImpl.getLinkisJobData` 取 jobId（CS 查询）；
5. 结果空数组同样抛 80003。

### 3.3 EmailCSHelper.clearJobIdsCache（清理）

```scala
private[sendemail] def clearJobIdsCache(refContext: ExecutionRequestRefContext): Unit = {
  if (refContext != null && refContext.getRuntimeMap != null) {
    val runtimeMap = refContext.getRuntimeMap
    runtimeMap.synchronized {
      runtimeMap.remove(JOB_IDS_CACHE_KEY)
    }
  }
}
```

- 可见性收窄到 `private[sendemail]`，仅供执行入口清理，不暴露为通用 API。
- refContext / runtimeMap 为 null 时静默返回（清理动作不产生新异常）。
- 与 `getJobIds` 同锁，不存在"清理与读取竞争导致半态"。

### 3.4 SendEmailRefExecutionOperation.execute（执行体包装）

```scala
override def execute(requestRef: RefExecutionRequestRef.RefExecutionRequestRefImpl): ExecutionResponseRef =
  Utils.tryFinally(executeWithJobIdsCache(requestRef)) {
    EmailCSHelper.clearJobIdsCache(requestRef.getExecutionRequestRefContext)
  }

private def executeWithJobIdsCache(requestRef: RefExecutionRequestRef.RefExecutionRequestRefImpl): ExecutionResponseRef = {
  // 原 execute 方法体，逐行不变：
  // hooks.preGenerate → emailGenerator.generateEmail → parsers/generators → hooks.preSend
  // → emailSender.send →（可选）DataGoImageSender.send → ExecutionResponseRef
  // 各失败分支仍走 putErrorMsg 返回错误响应
}
```

**设计决策**：

| 决策点 | 选择 | 理由 |
|--------|------|------|
| 清理时机 | execute 的 finally | 缓存生命周期严格 = 单次节点执行；无论成功、失败（putErrorMsg 返回 error 响应）、执行体抛异常，均清理 |
| tryFinally 而非手动 try/catch | Linkis `Utils.tryFinally` | 与模块既有风格一致（`MultiContentEmailGenerator` 中已使用），避免遗漏 return 路径 |
| 懒加载而非 execute 进入时预加载 | 首次 getJobIds 触发 | 该节点执行不必然需要 jobIds（如 category 非 node 的场景）；不引入额外前置失败点 |
| putErrorMsg 失败路径也清理 | tryFinally 覆盖所有 return | 失败响应也是正常返回，同样经过 finally |

---

## 四、数据模型

无持久化数据、无配置项变更。唯一新增运行时数据：

| 数据 | 位置 | 类型 | 生命周期 |
|------|------|------|---------|
| `__dss_sendemail_job_ids_cache__` | runtimeMap（执行请求上下文） | `JobIdsCache(Array[Long])` | 懒加载写入 → execute finally 移除 |

---

## 五、并发与边界分析

### 5.1 并发场景

| 场景 | 行为 |
|------|------|
| 同执行内多线程同时首次 getJobIds | runtimeMap 锁互斥：一个线程加载并写入，其余等锁后命中缓存，仅触发一次 CS 加载链 |
| getJobIds 执行中触发 clearJobIdsCache | 同锁互斥：清理等待读取完成（或反之），无半态 |
| 首次加载在锁内做 CS 远程调用 | 持锁时长≈一次原 getJobIds 时长；等锁线程与升级前"各自重复查 CS"相比，总时延下降（1 次远程 vs K 次远程） |

### 5.2 边界场景

| 边界 | 行为 |
|------|------|
| content 为空 / jobIds 为空 | loadJobIds 抛 80003，**不写缓存**；同执行内再次调用会重新尝试加载 |
| CS 加载抛非业务异常 | 同上，异常冒泡（被 execute 的 tryCatch 转为错误响应），finally 清理（无缓存可清，等价于空操作） |
| runtimeMap 中该 key 已被外部写入其他类型值 | `case _` 按未命中处理，加载后覆盖 |
| 调用方修改返回数组 | clone 副本，缓存不受影响 |
| 重试/上下文复用场景 | 上次执行已清理，本次重新加载，无脏缓存 |

---

## 六、部署与配置

- **无配置变更**：不新增/删除/修改任何 appconn.properties 配置项。
- **无 DB 变更**、**无接口变更**。
- 发布方式：随 sendemail-appconn 常规发版部署；回滚直接回退包（无状态残留）。

---

## 七、性能考虑

| 场景 | 优化前 | 优化后 |
|------|--------|--------|
| 单次执行获取 K 次、M 个上游节点 | K×(1+2M) 次 CS 远程调用 | 1×(1+2M) 次 + (K-1) 次内存读 |
| CS 抖动期间 | K 次暴露于故障窗口 | 1 次暴露，后续命中不再依赖 CS |
| 内存开销 | — | 单次执行内多存一份 Array[Long] 副本（节点 ID 量级，KB 级以下），finally 清理后释放 |

---

## 八、安全考虑

| 安全项 | 说明 |
|-------|------|
| 数据隔离 | 缓存内容为 jobId 数组（非用户敏感数据），生命周期单次执行，无跨用户共享 |
| key 命名 | 双下划线内部前缀，避免与业务参数冲突或被页面参数注入覆盖（同 key 非 JobIdsCache 类型值会被安全覆盖） |
| 锁范围 | 仅 runtimeMap 实例级同步，无全局锁、无死锁环（临界区内不再获取其他锁） |

---

## 九、测试建议

配套测试用例见 [sendemail获取jobId缓存_测试用例.md](../testing/sendemail获取jobId缓存_测试用例.md)：

- 单测（建议新增 `EmailCSHelperTest`）：命中/未命中、副本防御、清理空安全、并发单次加载、80003 不写缓存——需 Mock `ExecutionRequestRefContext`/runtimeMap。
- SIT：以多上游节点工作流执行 sendemail 节点，核对日志中 CS 加载仅一次、邮件内容与升级前一致。

---

## 十、变更记录

| 版本 | 日期 | 变更 |
|------|------|------|
| v1.0 | 2026-09-14 | 初版（随提交 ccdfadfc6）：getJobIds 增加 runtimeMap 执行内缓存（synchronized + clone），原 CS 加载链抽为 loadJobIds，新增 clearJobIdsCache；execute 以 tryFinally 包装并在结束时清理 |

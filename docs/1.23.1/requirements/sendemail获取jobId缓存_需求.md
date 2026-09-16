# sendemail 获取 jobId 缓存 需求文档

| 属性 | 值 |
|------|-----|
| 需求编号 | REQ-DSS-1.23.1-001 |
| 需求名称 | sendemail 节点执行内缓存 CS 获取的 jobIds |
| 需求类型 | 优化（OPTIMIZE） |
| 优先级 | P2 |
| 状态 | 已提交开发（ccdfadfc6，2026-09-14），待测试 |
| 版本 | dev-1.23.1 |
| 所属模块 | dss-sendemail-appconn |
| 关联设计 | [sendemail获取jobId缓存_设计.md](../design/sendemail获取jobId缓存_设计.md) |
| 关联提交 | ccdfadfc6 `feat: sendemail节点发送获取cs内容使用缓存`（2 文件，+39/-3） |

---

## 一、功能背景

### 1.1 当前痛点

sendemail 节点执行时需从 CS（ContextService）获取上游节点的 Linkis jobIds（`EmailCSHelper.getJobIds`），用于拉取上游节点结果集生成邮件内容（图片/HTML 附件等）：

- **单次调用查询链长**：每次调用 `getJobIds` = 1 次 contextID 解析（`ContextServiceUtils.getContextIDStrByMap`）+ **每个上游节点 2 次 CS 远程查询**（`getNodeNameByNodeID` 节点 ID→节点名 + `LinkisJobDataServiceImpl.getLinkisJobData` 节点名→jobId）。上游节点越多（content 列表越长），CS 压力与时延线性放大。
- **一次执行内重复查询**：jobIds 在同一次节点执行的有效期内不变，但此前每次调用都完整重查 CS。邮件内容生成（`MultiContentEmailGenerator`）、`EmailCSHelper.getJobTypes` 等入口均会触发 `getJobIds`，同一执行内存在重复加载。
- **CS 故障放大**：CS 短暂抖动时，重复查询使 sendemail 节点失败概率成倍增加。

### 1.2 期望价值

- **降低 CS 查询次数**：同一次节点执行内 jobIds 仅首次从 CS 加载，后续命中执行内缓存。
- **降低节点时延**：省去重复的 CS 远程往返，上游节点多时收益明显。
- **行为语义不变**：仅优化查询路径，邮件内容、异常行为（含 80003 空结果集异常）与升级前完全一致。

---

## 二、功能概述

### 2.1 一句话描述

在 sendemail 节点单次执行内，将 `EmailCSHelper.getJobIds` 从 CS 加载的 jobIds 缓存于 runtimeMap（key `__dss_sendemail_job_ids_cache__`），后续获取直接返回缓存副本；节点执行结束（成功/失败）由 `SendEmailRefExecutionOperation.execute` 以 tryFinally 统一清理，跨执行不残留。

### 2.2 目标用户

- DSS 工作流开发者/数据分析人员（sendemail 节点执行更快、更稳）
- 平台运维人员（CS 服务查询压力下降）

---

## 三、功能需求

### 3.1 核心功能 P0

| 编号 | 功能项 | 描述 |
|:----:|-------|------|
| F-P0-01 | 执行内缓存命中 | 同一次节点执行内第二次及以后调用 `getJobIds` 命中缓存，直接返回副本，日志输出 `From sendemail execution cache to get Job IDs ...` |
| F-P0-02 | 首次加载并写缓存 | 缓存未命中时走原 CS 加载链（抽为 `loadJobIds`，逻辑不变），加载成功后写入缓存（存副本） |
| F-P0-03 | 执行结束清理 | `execute` 以 `Utils.tryFinally` 包装执行体，节点执行结束（成功/失败/异常）调用 `clearJobIdsCache` 移除缓存，跨执行不残留 |

### 3.2 增强功能 P1

| 编号 | 功能项 | 描述 |
|:----:|-------|------|
| F-P1-01 | 线程安全 | 缓存读写以 `runtimeMap.synchronized` 保护，同一执行内并发调用仅一个线程加载，其余等锁后命中缓存 |
| F-P1-02 | 副本防御 | 缓存写入与返回均为 `clone()` 副本，调用方对返回数组排序/增删不污染缓存 |
| F-P1-03 | 清理空安全 | `clearJobIdsCache` 对 refContext/runtimeMap 为 null 的场景不抛异常 |

### 3.3 功能不包含

| 编号 | 不包含项 | 说明 |
|:----:|---------|------|
| N-01 | 跨执行缓存 | 缓存生命周期严格限定单次节点执行，不做进程级/工作流级缓存 |
| N-02 | jobIds 语义变化 | 加载逻辑（content 解析、CS 查询链、80003 异常）保持不变，仅增加缓存层 |
| N-03 | 其他 CS 查询缓存 | `fetchLinkisJobResultSetPaths`、`getResultSetReader` 等其他 CS/结果集调用不在本期范围 |

---

## 四、输入输出

### 4.1 输入

无新增配置项、无节点参数变化。输入仍为执行请求上下文：

| 输入 | 来源 | 说明 |
|------|------|------|
| runtimeMap["content"] | 工作流执行上下文 | 上游节点 ID 列表（JSON 数组字符串或 List），首次加载时解析 |
| contextIDStr | runtimeMap | CS 上下文 ID，首次加载时解析 |
| runtimeMap 自身 | `ExecutionRequestRefContext.getRuntimeMap` | 同时作为缓存载体（key `__dss_sendemail_job_ids_cache__`）与锁对象 |

### 4.2 输出

| 输出 | 说明 |
|------|------|
| jobIds: Array[Long] | 首次为 CS 加载结果；后续为缓存副本（内容一致） |
| 日志 | 命中：`From sendemail execution cache to get Job IDs ...`；首次加载沿用原 `From cs to getJob ids ...` / `Job IDs is ...` |

---

## 五、业务规则

| 编号 | 规则 | 说明 |
|:----:|------|------|
| BR-01 | 缓存生命周期=单次执行 | execute 进入时生效（懒加载），execute 返回/抛异常时清理（tryFinally） |
| BR-02 | 首次加载逻辑不变 | `loadJobIds` 与原 `getJobIds` 逻辑逐行一致：content 为空抛 `EmailSendFailedException(80003, "empty result set is not allowed")`；jobIds 为空同样抛 80003 |
| BR-03 | 副本进出 | 写缓存存 `clone()`，读缓存返回 `clone()`，防御外部修改 |
| BR-04 | 同锁互斥 | 缓存读写与清理均在 `runtimeMap.synchronized` 块内，以 runtimeMap 为锁对象（与缓存载体同对象，保证可见性与互斥） |
| BR-05 | 清理无条件执行 | 成功、失败（putErrorMsg 返回错误响应）、异常（执行体抛出）路径均清理；refContext/runtimeMap 为 null 时静默跳过 |

---

## 六、验收标准

| 编号 | 验收标准 | 验证方式 |
|:----:|---------|---------|
| AC-01 | 同一执行内重复获取命中缓存 | SIT 执行 sendemail 节点，日志出现且仅出现一次 `From cs to getJob ids`，其后出现 `From sendemail execution cache to get Job IDs` |
| AC-02 | 首次加载结果与升级前一致 | 多上游节点场景，邮件附件内容（图片/HTML）与升级前版本一致 |
| AC-03 | 执行成功后缓存清理 | 节点成功后 runtimeMap 中无 `__dss_sendemail_job_ids_cache__` |
| AC-04 | 执行失败后缓存同样清理 | 构造发送失败（如 SMTP/DataGo 异常），节点失败后缓存仍被清理 |
| AC-05 | 返回副本不被污染 | 单测：对 getJobIds 返回数组排序/追加后，再次 getJobIds 仍返回原始顺序与长度 |
| AC-06 | 并发获取线程安全 | 单测：多线程并发首次 getJobIds，仅触发一次 loadJobIds，各线程结果一致 |
| AC-07 | 空结果集行为不变 | content 为空/CS 返回空 jobIds 时仍抛 80003，且异常后缓存清理（未写入） |

---

## 七、影响范围

### 7.1 代码变更

| 文件 | 变更类型 | 说明 |
|------|:-------:|------|
| `cs/EmailCSHelper.scala` | 修改 | 新增缓存 key/`JobIdsCache`；`getJobIds` 增加缓存读写（synchronized + clone）；原 CS 加载链抽为私有 `loadJobIds`；新增 `clearJobIdsCache` |
| `SendEmailRefExecutionOperation.scala` | 修改 | `execute` 改为 tryFinally 包装（原执行体抽为 `executeWithJobIdsCache`，逻辑不变），finally 中调用 `clearJobIdsCache` |

### 7.2 兼容性影响

| 影响项 | 影响程度 | 说明 |
|-------|:-------:|------|
| 邮件内容生成 | 无 | jobIds 内容与顺序不变 |
| 节点参数/配置文件 | 无 | 无新增配置、无参数变化 |
| 异常行为 | 无 | 80003 等异常码与触发条件不变 |
| 其他调用方 | 无 | `MultiContentEmailGenerator`、`getJobTypes` 调用方式不变，自动受益于缓存 |

### 7.3 依赖项

| 依赖 | 类型 | 说明 |
|------|------|------|
| CS（ContextService） | 外部依赖 | 首次加载仍依赖 CS 可用；缓存命中路径不再依赖 |

---

## 八、风险与约束

| 编号 | 风险/约束 | 等级 | 应对措施 |
|:----:|---------|:----:|---------|
| R-01 | runtimeMap 中残留缓存 key，被序列化/透传到下游 | 低 | key 带双下划线内部前缀；execute tryFinally 无条件清理；即使残留，后续执行首次 getJobIds 会覆盖 |
| R-02 | runtimeMap 作为锁对象：首次加载的 CS 远程调用在锁内执行，同执行内其他线程等锁期间会被 CS 时延阻塞；且与框架侧其他以 runtimeMap 为锁的同步块互斥 | 低 | 属"单次加载、其余命中缓存"的既有代价，sendemail 执行内并发度低，CS 单次加载原本也要串行等待；锁持有时间≈一次原有 getJobIds 时长，无放大 |
| R-03 | 缓存掩盖执行中途 CS 数据变化 | 低 | jobIds 在单次执行内本为不可变事实，语义上可安全缓存 |

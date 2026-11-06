# 信号节点状态查询超时永久Running修复 设计文档

| 属性 | 值 |
|------|-----|
| 设计编号 | DES-DSS-1.26.0-FIX-001 |
| 关联需求 | REQ-DSS-1.26.0-FIX-001 |
| 设计类型 | Bug修复（FIX） |
| 优先级 | P1 |
| 版本 | dev-1.26.0 |
| 所属模块 | dss-orchestrator/dss-workflow/dss-flow-execution-server（核心修复）；dss-linkis-node-execution（配置调优分析，不改代码）；dss-eventchecker-appconn（关联分析，不改代码） |
| 创建日期 | 2026-11-02 |

---

## 一、设计概述

### 1.1 Bug 摘要

基于需求文档 REQ-DSS-1.26.0-FIX-001 的根因分析，本 Bug 的核心事实链（行号均已在源码核实）：

1. **异常分支"返回 true 但不翻转状态"（P0 根因）**：`DefaultNodeRunner.isLinkisJobCompleted`（`DefaultNodeRunner.scala:69-95`）中，`case t:Throwable => logger.error(...); true`（line 93-94）在 entrance 状态查询 HTTP 超时（SocketTimeoutException 等非 ErrorException）时返回 true，poller 据此将 runner 移出 `nodeRunnerQueue`（`NodeExecutionStatusPoller.scala:34-38`），但该分支内**未调用 transitionState**，节点状态停留在 Running，永久无人再查询——工作流永久挂起。
2. **未知状态串同样触发（P0 同根因第二路径）**：`NodeExecutionState.withName(...)`（line 80）对 DSS 枚举外的状态串（如 Linkis 返回的 `Submitting` 等）抛 `NoSuchElementException`，被 `case t:Throwable` 捕获，走同样的错误路径。
3. **无超时兜底（P1 放大器）**：`NodeExecutionState.Timeout` 枚举（`NodeExecutionState.scala:24`）已定义且 `isCompleted` 视其为终态（line 32-35），但全工程无任何代码路径将节点置为 Timeout；DSS 侧节点理论可无限 Running。
4. **readTimeout 过长（P1 放大器）**：`wds.linkis.flow.connection.timeout` 默认 300000ms（`LinkisJobExecutionConfiguration.java:57`），同时用作 connectionTimeout 与 readTimeout（`LinkisUjesClientUtils.java:40/44/57/59`）；entrance Full GC 期间每次轮询挂满 5 分钟，poller 线程池（默认 20 线程，`FlowExecutionEntranceConfiguration.scala:26`）可能全部长挂。
5. **设计阶段新发现（必须随 P1 一并修复）**：`FlowEntranceJob.onStatusChanged`（`FlowEntranceJob.scala:96-140`）对节点状态 match 中**没有 `Timeout` 分支**（落入 `case _` 空分支）——若只启用 Timeout 枚举翻转而不补该分支，节点虽翻为 Timeout 终态，但**不会从 `runningNodes` 迁移到 `failedNodes`**（`FlowContext.changedNodeState`，`FlowContext.scala:55-64`），`isFlowCompleted`（`FlowEntranceJob.scala:265`）永远为 false，工作流仍无法完成。**P1 启用 Timeout 必须同步修改 FlowEntranceJob**（详见 4.3）。

### 1.2 修复目标

| 目标 | 验收点 | 对应 AC |
|-----|-------|:----:|
| entrance 短暂抖动（< 阈值）自愈 | 查询异常按暂时性故障处理，继续轮询；entrance 恢复后下一轮询周期内正常翻转终态 | AC-01 |
| entrance 长期不可用（≥ 阈值）收敛 | 连续失败达阈值 → kill Linkis 任务 + 节点置 Failed + error 告警，工作流正常结束可重跑 | AC-02 |
| 未知状态串不中断轮询 | 未知状态映射为 Running（非终态）+ warn 日志（含原始状态串），不抛异常 | AC-03 |
| 正常路径行为不变 | entrance 健康时提交/轮询/翻转（Succeed/Failed/Cancelled）与修复前逐字节一致 | AC-04/AC-05 |
| 节点级超时兜底（P1） | 超时节点翻 Timeout，且 FlowEntranceJob 正确迁移节点并结束工作流 | AC-02/AC-04 |
| 配置兼容 | 不新增强制配置；新配置有默认值；既有配置语义不变 | AC-06 |

### 1.3 设计原则

1. **最小侵入**：不修改 `NodeRunner.isLinkisJobCompleted` 接口签名（`NodeRunner.scala:56`），不重构 poller 队列模型（需求 N-02），修复内聚在 `DefaultNodeRunner` 实现内部 + `FlowExecutionEntranceConfiguration` 配置定义 + `FlowEntranceJob` 补一个 match 分支。
2. **正常路径零变更**：entrance 健康时的执行路径（interval 检查 → 终态检查 → getState → withName → transitionState → true）控制流与返回值与修复前完全一致；状态映射采用"枚举快路径优先"，正常状态串行为不变。
3. **保守默认**：超时兜底默认关闭（普通节点 -1），失败升级阈值默认 30 分钟（容忍约 6 次 300s 超时连续失败，区分抖动与真不可用）；所有新阈值可配置、可关闭。
4. **收敛方向保守**：任何兜底路径最终都翻转到既有终态（Failed/Timeout），复用现有 `transitionState → NodeRunnerListener.onStatusChanged → FlowEntranceJob` 失败处理链，不引入新的状态语义分支以外的机制。

### 1.4 紧急程度

P1：本 Bug 造成工作流永久挂起（非数据损坏），entrance 抖动期间同批多工作流批量触发，夜间调度批次整体延迟需人工恢复。P0 修复点（异常分支 + 状态映射）当前迭代必须完成；P1（超时兜底 + FlowEntranceJob Timeout 分支）与 P0 同迭代交付（二者耦合，见 1.1 第 5 条）；配置调优与运维治理随发布配套执行。

---

## 二、方案选型

### 2.1 P0：异常分支修复策略

#### 2.1.1 候选方案对比矩阵

| 候选 | 描述 | 误杀风险 | 恢复能力 | 复杂度 | 缺点 | 推荐度 |
|:----:|------|:------:|:------:|:----:|------|:----:|
| **A** | 只改 catch 返回 false：`case t:Throwable => false`，永远继续轮询 | 无 | 短期抖动可自愈；**长期不可用时节点无限 Running + poller 线程被拖死，Bug 未消除只是换了形态** | 极低 | 把"永久挂起"变成"永久轮询"，entrance 长期宕机时 20 个 poller 线程全部挂在 300s 超时上，拖垮所有工作流的状态轮询；无升级告警 | ⭐⭐ |
| B | 失败计数器阈值：连续失败 N 次后 kill + Failed | 与时长脱钩：300s 超时下 N=6 即 30 分钟，但若 readTimeout 调小到 60s，N=6 只需 6 分钟，entrance 一次较长 Full GC（10 分钟级）即被误杀 | 抖动可自愈 | 低 | 计数语义依赖单次失败时长，配置调优（P1-02）会隐式改变其真实时长语义，运维易踩坑 | ⭐⭐⭐ |
| **C** | **连续失败时长阈值**：记录 `lastStatusSuccessTime`（最近一次成功获取状态的时刻），`now - lastStatusSuccessTime >= 阈值` 才 kill + Failed；任一次成功查询即刷新计时 | 低：以真实时长度量，与单次超时配置解耦；30 分钟阈值下 entrance 分钟级 Full GC 完全容忍 | 抖动自愈 + 长期不可用收敛到 Failed（可重跑） | 低（新增 1 字段 + 1 判定方法） | 计时状态在 runner 实例内，服务重启后重新计数（重启本身中断工作流，既有行为，可接受，见 R-03） | ⭐⭐⭐⭐⭐ |
| D | 节点级总时长兜底单独使用：仅靠 `startTime + maxRunningTime` 翻 Timeout，不改 catch 分支 | 高：阈值要照顾最慢合法任务（多小时 Scriptis），只能配很大（如 24h）；entrance 抖动挂起的节点要等满全量时长才收敛 | 极慢收敛（小时~天级） | 低 | 不解决根因：`case t:Throwable => true` 仍在第一次异常时就移出轮询队列——**根本不会走到超时判定**（判定点在轮询路径内），方案 D 单独使用对本案无效 | ⭐ |

#### 2.1.2 选型决策

**推荐方案：C（连续失败时长阈值）作为 P0 主线，并叠加"未知状态映射兜底"消除第二触发路径；D 作为 P1 独立增强（非 P0 替代品）。**

选型理由：

1. **精确命中根因语义**：根因是"查询暂时性故障被当作任务已完成"。C 的语义是——故障期间继续轮询（对应"暂时性"），仅当持续超过阈值才升级为任务失败（对应"真不可用"），与 Why5 得出的正确异常处理策略一一对应。
2. **误杀风险最低且稳定**：时长阈值不随单次超时配置变化。默认 30 分钟的依据：entrance Full GC 恢复通常分钟级；现状 readTimeout 300s/次，30 分钟容忍约 6 次连续失败；P1 调优为 60s 后容忍约 30 次。无论哪种超时配置，30 分钟都能明确区分"抖动"与"真不可用"。B 的计数阈值在 readTimeout 调小后会隐式缩短真实时长，与本修复的 P1 配置调优互相打架。
3. **彻底消除 D 的死角**：D 的判定点若放在轮询路径内，catch 返回 true 移出队列后判定根本不会执行；若放在 catch 内基于 startTime，则阈值只能按"任务最大合法时长"配置（天级），对"entrance 抖 5 分钟挂起"的场景收敛过慢。C 的计时基于"最后成功时刻"，与任务自身时长解耦。
4. **改动内聚**：C 只在 `DefaultNodeRunner` 新增 1 个字段、1 个判定方法、改写 catch 分支，不触碰接口签名与 poller（需求 7.1 明确 poller 可不改动）。
5. **并发安全天然成立**：`nodeRunnerQueue` 是单队列 take-put 模型（`NodeExecutionStatusPoller.scala:32-35`），同一 runner 同一时刻仅被一个 poller 线程持有，`lastStatusSuccessTime` 读写天然串行，无需 volatile/锁（需求 5.4 候选A 并发疑虑的结论）。

#### 2.1.3 不选 A/B/D 的具体原因

- **不选 A**：只是把"永久 Running 无人查"变成"永久 Running 被无限查"，且 entrance 长期不可用时 poller 线程池耗尽，影响面从单个工作流扩大到全部工作流，属于更差的失败模式。
- **不选 B**：计数语义被 readTimeout 隐式缩放（300s→60s 后同样 N=6 的真实时长从 30 分钟缩到 6 分钟），与 P1-02 配置调优存在耦合陷阱；且"连续失败"在 interval 检查（3s 间隔）与线程调度下计数口径易碎。
- **不选 D 单独使用**：见矩阵，catch 返回 true 导致轮询停止后超时判定不可达，对本 Bug 无效。D 仅在 C 落地后作为独立兜底（覆盖"entrance 一直正常返回 Running 但任务实际僵死"的另一类异常）。

### 2.2 P0：未知状态串处理策略

| 候选 | 描述 | 优点 | 缺点 | 推荐度 |
|:----:|------|------|------|:----:|
| A | tryCatch 包裹 withName，异常时返回 Running + warn | 改动最小（1 行） | 无法翻译 Linkis 语义近似的终态（如 `Cancelled` 大小写变体、`Shutdown`），终态被误判为 Running 会一直轮询到超时升级才收敛 | ⭐⭐⭐ |
| **B** | **显式映射函数**：枚举快路径（withName 原样保留）+ 已知 Linkis 状态别名映射表 + 未知串兜底 Running + warn | 正常路径零开销零变更；已知别名（含终态）正确翻译；未知串不中断；映射表可单测 | 需维护 DSS↔Linkis 映射表，Linkis 新增状态需跟进（R-05） | ⭐⭐⭐⭐⭐ |

**决策：选 B**。理由：`isLinkisJobCompleted` 的正确性依赖终态识别，若 `Cancelled`/`Timeout` 等 Linkis 终态因拼写差异被兜底成 Running，节点会错误地继续轮询并最终被 30 分钟阈值误杀为 Failed——语义失真。显式映射表让绝大多数状态走确定性翻译，仅真正未知的状态才降级。映射表见 3.3.3。

### 2.3 P1：节点级超时兜底与信号节点差异化

| 候选 | 描述 | 优点 | 缺点 | 推荐度 |
|:----:|------|------|------|:----:|
| A | 不做超时兜底，仅靠 C 的失败阈值 | 零改动 | 覆盖不了"entrance 一直正常返回 Running 但任务实际僵死"（如 entrance 侧调度器异常但 HTTP 正常）的场景 | ⭐⭐ |
| **B** | 全局统一 `max.running.time`，默认开启（如 24h） | 配置简单 | 必须迁就最慢合法任务，默认值只能很大，对信号节点（合法等待 1h）收敛过慢；一旦有用户跑了 25h 的合法任务即被误杀 | ⭐⭐⭐ |
| **C** | 全局配置默认关闭（-1）+ 信号节点独立配置默认开启（2h），基于 `FlowExecutionUtils.isSignalNode`（已有方法首次接线） | 普通节点默认零回归风险；信号节点（本次故障节点类型）默认获得兜底；差异化阈值语义清晰 | 新增 2 个配置项 | ⭐⭐⭐⭐⭐ |

**决策：选 C**。信号节点默认 2 小时的取舍分析：

- entrance 侧信号等待自身有 `max.receive.hours`（默认 1 小时，`EventChecker.java:68-70`）兜底，到点任务会以超时错误结束（`ListenerEventBusRefExecutionScheduler.scala:71-75`），DSS 下轮查询即查到 Failed 正常翻转——**正常情况下 DSS 侧兜底不应触发**；
- DSS 侧兜底只需覆盖"entrance 侧兜底也失效"的极端场景，取 `2 × max.receive.hours = 2 小时`留足余量，避免与 entrance 侧 maxLoopTime 竞态误杀仍在合法等待的节点（缓解 R-02）；
- 不逐节点解析 `max.receive.hours`（该参数在 AppConn 提交后才进入 runtimeMap，DSS runner 侧解析需耦合 eventchecker 内部参数，且其他 AppConn 亦有类似参数），若用户将某 ER 节点的 `max.receive.hours` 配置超过 2 小时，需运维同步调大 `wds.dss.flow.node.max.running.time.signal`，写入部署文档；
- 普通节点默认 -1 关闭：避免对多小时合法 Scriptis 任务造成误杀回归，交由部署按需开启。

### 2.4 P1：`wds.linkis.flow.connection.timeout` 调优结论

现状与约束（已核实）：

- `LINKIS_CONNECTION_TIMEOUT`（`LinkisJobExecutionConfiguration.java:57`，默认 300000）在 `LinkisUjesClientUtils.java:40/44`（0.x 路径）与 `57/59`（1.x 路径）**同时用作 connectionTimeout 与 readTimeout**；
- UJESClient 按 linkisUrl 缓存全局共享（`LinkisNodeExecutionImpl.java:73-96` clientMap），**提交任务（submit/execute）与状态查询（getJobInfo）、日志、结果集拉取共用同一 client 与同一超时配置**，无法只对状态查询生效。

利弊分析：

| 维度 | 调小至 60000 的影响 |
|-----|------------------|
| 状态查询（轻接口，毫秒级返回） | 正面：Full GC 期间单次挂起从 300s 降至 60s，poller 线程周转快约 5 倍；30 分钟失败阈值内可容纳约 30 次重试（现状 6 次），抖动自愈概率大幅提高 |
| 任务提交（submit，异步返回 taskID，正常秒级） | 风险点：大工作流提交（复杂节点 JSON、超长 SQL/AI SQL 代码）请求体大，若 gateway/entrance 同时高负载，60s readTimeout 可能中断提交。但 submit 响应仅为 taskID 轻响应，内网传输耗时不构成主要矛盾；极端场景下提交快速失败后 `run()` 的 catch 会 transitionState(Failed)（`DefaultNodeRunner.scala:120-124`），fail-fast 优于挂 5 分钟，行为正确且可重跑 |
| 日志/结果集拉取（getLog/openLog/resultSet） | 大结果集分页拉取单次响应可能较大，60s 余量需灰度验证（openLog 已有独立 10 次重试，`LinkisNodeExecutionImpl.java:166-227`，可容忍个别超时） |
| 语义影响 | 该配置全局共享，改的是"所有 UJES HTTP 操作"而非仅状态查询 |

**结论与建议**：

1. **不改代码默认值**（保持 300000）：避免存量部署升级后隐式改变大任务提交语义，符合需求 6.1"既有配置语义不变"；
2. **部署侧调整**：SIT/生产灰度环境在 DSS flow-execution-server 的 properties 配置中设 `wds.linkis.flow.connection.timeout=60000`，先验证大任务提交与大结果集拉取，再全量；
3. **本次不拆分 connection/read 两个配置**（YAGNI）：拆分需改 `LinkisUjesClientUtils` 两条构建路径与 `LinkisJobExecutionConfiguration`，引入新配置；在 C 方案（时长阈值）落地后，拆分的边际收益有限，留待后续有真实诉求再做；
4. **代码默认值与部署值的组合安全性**：C 的时长阈值以真实时长度量，60s 或 300s 超时配置下 30 分钟阈值语义不变（见 2.1.2 理由 2），调优不与 P0 修复耦合。

---

## 三、详细设计

### 3.1 整体修复流程

```
┌────────────────────────────────────────────────────────────────────┐
│            isLinkisJobCompleted 修复后决策流（每轮轮询）             │
└────────────────────────────────────────────────────────────────────┘

NodeExecutionStatusPoller.run()  (每5秒调度, NodeExecutionStatusPoller.scala:30-39)
  └── runner.isLinkisJobCompleted  (DefaultNodeRunner.scala)
        ├── ① interval 检查: 距上次查询 < 3000ms → return false（不变）
        ├── ② isCompleted(getStatus)？已终态 → return true（不变）
        ├── ③【P1新增】节点级最大运行时长检查（见 3.5）
        │     └── 超限 → kill Linkis 任务 + transitionState(Timeout) → return true
        ├── ④ getState 查询 entrance（HTTP，不变）
        │     ├── 成功 → 刷新 lastStatusSuccessTime = now 【P0新增】
        │     │     └── ⑤ 状态映射（枚举快路径 + 别名映射兜底）【P0改造】
        │     │           ├── 已知终态 → onStatusChanged + transitionState → true（不变）
        │     │           └── 未知串 → Running + warn → false【兜底】
        │     └── 抛异常 ↓
        └── ⑥ 异常分支【P0核心改造】
              ├── ErrorException → warn → false（继续轮询，不变）
              └── 其他 Throwable → error 日志
                    └── now - lastStatusSuccessTime >= STATE_FETCH_FAIL_TIMEOUT(默认30min)？
                          ├── 是 → tryAndWarn(kill) + transitionState(Failed)
                          │        + error 告警（节点名/失败时长/异常摘要）→ true
                          └── 否 → warn（连续失败时长/阈值剩余）→ false（继续轮询）
```

### 3.2 时序图：entrance 抖动期间的状态轮询（修复后）

```mermaid
sequenceDiagram
    autonumber
    participant P as NodeExecutionStatusPoller<br/>(20线程, 5s调度)
    participant R as DefaultNodeRunner
    participant L as LinkisNodeExecutionImpl.getState
    participant E as Linkis entrance
    participant F as FlowEntranceJob<br/>(NodeRunnerListener)

    Note over R: 启动时 lastStatusSuccessTime = startTime

    rect rgb(235, 245, 255)
    Note over P,E: 阶段一：entrance 短暂抖动（< 30分钟阈值）
    P->>R: isLinkisJobCompleted()
    R->>R: interval≥3000ms 且节点未终态
    R->>R: 【P1】startTime+maxRunningTime 超限？否
    R->>L: getState(linkisJob)
    L->>E: HTTP GET 状态接口
    E-->>L: 超时无响应（Full GC）
    L-->>R: 抛 SocketTimeoutException
    R->>R: error日志；now-lastStatusSuccessTime < 30min
    R-->>P: false（继续轮询）✅ 不再无条件返回true
    P->>P: nodeRunnerQueue.put(runner) 放回队列
    Note over E: entrance 恢复
    P->>R: 下一轮 isLinkisJobCompleted()
    R->>L: getState(linkisJob)
    L->>E: HTTP GET 状态接口
    E-->>L: 返回 "Succeed"
    L-->>R: "Succeed"
    R->>R: lastStatusSuccessTime = now（刷新计时）
    R->>R: withName("Succeed") 枚举快路径命中终态
    R->>F: listener.onStatusChanged(Running, Succeed, job)
    F->>F: runningNodes→succeedNodes; 调度下游; tryCompleted
    R->>R: transitionState(Succeed)
    R-->>P: true
    end

    rect rgb(255, 240, 240)
    Note over P,E: 阶段二：entrance 持续不可用（≥ 30分钟阈值）
    P->>R: isLinkisJobCompleted()（第N轮失败）
    R->>L: getState(linkisJob)
    L-->>R: 抛 SocketTimeoutException
    R->>R: now-lastStatusSuccessTime ≥ 30min 触发升级
    R->>L: Utils.tryAndWarn { cancel-kill(linkisJob) }
    Note over L: kill 已完成任务幂等（见4.7），失败仅warn不阻断
    R->>F: transitionState(Failed) → onStatusChanged(Running, Failed, node)
    F->>F: printLog ERROR；runningNodes→failedNodes；kill()结束工作流
    R->>R: error 告警日志（节点名/失败时长/异常摘要）
    R-->>P: true（终态，移出队列）
    end
```

#### 关键节点说明表

| 节点 | 处理逻辑 | 输入/输出 | 异常处理 |
|:----:|---------|----------|---------|
| ① interval 检查 | 距上次查询 < `NODE_STATUS_INTERVAL`(3000ms) 直接返回 false（既有，不变） | 输入: lastGetStatusTime<br>输出: false | 无 |
| ② 已终态检查 | `isCompleted(getStatus)` 为 true 直接返回 true（既有，不变） | 输入: 节点当前状态<br>输出: true | 无 |
| ③ 超时兜底检查【P1】 | `startTime + 生效的 maxRunningTime <= now` 且非终态 → kill + 翻 Timeout | 输入: startTime, maxRunningTime<br>输出: Timeout 终态 | 配置 -1 跳过；kill 失败仅 warn，仍翻 Timeout |
| ④ getState 查询 | UJES Client HTTP 查询 entrance 任务状态（既有，不变） | 输入: linkisJob<br>输出: 状态字符串 | 异常交由⑥处理 |
| ⑤ 状态映射【P0】 | 枚举快路径 `withName` → 别名映射表 → 未知兜底 Running | 输入: 状态串<br>输出: NodeExecutionState | NoSuchElementException 被捕获走兜底，不外抛 |
| ⑥ 异常分支【P0核心】 | ErrorException → false；其他 Throwable → 连续失败时长判定 | 输入: lastStatusSuccessTime, 阈值<br>输出: false 或 Failed 终态+true | kill 抛异常 tryAndWarn 吞掉，仍翻 Failed |

#### 技术难点说明表

| 难点 | 问题描述 | 解决方案 | 决策理由 |
|-----|---------|---------|---------|
| 计时基准的选择 | 用"首次失败时刻"还是"最后成功时刻"起算 | 用 `lastStatusSuccessTime`（成功即刷新），而非记录首次失败时间戳 | 成功刷新语义天然处理"失败-成功-失败"交错场景；若记首败时刻则一次成功后仍需清零，状态机更复杂且易错 |
| lastStatusSuccessTime 的可见性 | poller 20 线程是否会并发读写该字段 | 不加 volatile/锁 | `LinkedBlockingQueue` take-put 模型保证同一 runner 同一时刻仅被一个 poller 线程持有（单消费者语义），读写天然串行；加锁反而引入不必要开销 |
| 升级 kill 与 entrance 已完成的冲突 | 30 分钟失败期间 entrance 可能已恢复且任务已 Succeed | kill 前 tryAndWarn 兜底；kill 一个已完成的 Linkis 任务幂等（4.7）；接受"误置 Failed 但任务实际成功"的最差结果 | 30 分钟不可用后工作流状态必须收敛（可重跑）；现状是永久挂起，收敛语义严格更优 |
| 未知状态与终态识别的平衡 | 全部兜底 Running 会把终态变体误判为非终态 | 枚举快路径 + 别名映射表（含 Linkis 终态别名），仅真正未知串才 Running | 终态串拼写变体（Cancelled/Killed 等）确定性翻译，避免终态节点被 30 分钟阈值误杀 |
| Timeout 状态的工作流联动 | `FlowEntranceJob.onStatusChanged` 无 Timeout 分支，翻 Timeout 后节点滞留 runningNodes | 同步补充 `case NodeExecutionState.Timeout` 分支：迁移 runningNodes→failedNodes + 打印日志 + kill 工作流（对齐 Failed 分支行为） | 不补则 P1 修复产生新形态挂起（设计新发现，见 1.1 第 5 条） |
| Scala 2.11 兼容 | 新代码不得使用高版本 API | 仅用 String#equalsIgnoreCase、Option、tryAndWarn 等 JDK8/Scala 2.11.12 基础 API | 项目约束（需求 6.1） |

#### 边界与约束说明

- **前置条件**：
  - P0：无（不配置任何新项即按默认 30 分钟阈值生效）
  - P1 超时兜底：普通节点默认关闭（-1）；信号节点默认 2 小时生效，依赖 `wds.dss.flow.signal.nodes`（既有配置，默认 `linkis.appconn.eventchecker.eventreceiver`）识别节点类型（取 `node.getNodeType` 比对，`FlowJobNodeParser.scala:62` 已将 nodeType 放入 LINKIS_TYPE）
- **后置保证**：
  - 任一 AppConn 节点从进入轮询队列起，最迟在 max(30 分钟失败阈值, 生效的 maxRunningTime) 内收敛到终态
  - 不再存在"返回 true 但状态为 Running"的路径组合
- **副作用说明**：
  - entrance 真不可用超过阈值时在跑工作流会批量翻 Failed（R-01，可重跑；运维 SOP：计划内长维护前暂停工作流）
  - kill 幂等性分析见 4.7；kill 请求本身在 entrance 不可用时大概率也超时失败，仅 warn，不阻断置 Failed
- **回滚约束**：
  - 回滚代码后恢复旧行为（永久 Running）；无数据库/配置结构变更，回滚仅需替换 jar 包
  - 部署侧若已调 `wds.linkis.flow.connection.timeout=60000`，回滚 DSS 代码时该值仍有效且兼容（配置属 Linkis 客户端通用配置）

### 3.3 核心代码改动（P0）

#### 3.3.1 FlowExecutionEntranceConfiguration.scala —— 新增配置

**文件**：`dss-orchestrator/orchestrators/dss-workflow/dss-flow-execution-server/src/main/scala/com/webank/wedatasphere/dss/flow/execution/entrance/conf/FlowExecutionEntranceConfiguration.scala`

**改动位置**：line 55（`SIGNAL_NODES`）之后追加

```scala
  val SIGNAL_NODES = CommonVars("wds.dss.flow.signal.nodes", "linkis.appconn.eventchecker.eventreceiver")  // 既有 line 55

  // ===== 以下为本次新增 =====

  // P0：连续状态获取失败升级阈值（毫秒）。自最近一次成功获取到 entrance 状态起算，
  // 持续失败达到该时长后 kill Linkis 任务并将节点置为 Failed。
  // 默认 30 分钟：entrance Full GC 恢复通常为分钟级；readTimeout=300s 时约容忍 6 次连续失败，
  // readTimeout 调优为 60s 后约容忍 30 次，可区分"抖动"与"真不可用"。<=0 视为无限等待（不建议）。
  val NODE_STATE_FETCH_FAIL_TIMEOUT = CommonVars("wds.dss.flow.node.state.fetch.fail.timeout", 30 * 60 * 1000L)

  // P1：节点级最大运行时长兜底（毫秒），-1 表示关闭。
  // 从节点 startTime 起算，非终态且超过该时长时节点翻转为 Timeout 终态。
  val NODE_MAX_RUNNING_TIME = CommonVars("wds.dss.flow.node.max.running.time", -1L)

  // P1：信号节点（EventReceiver 等）最大运行时长兜底（毫秒），-1 表示关闭。
  // 未显式配置 NODE_MAX_RUNNING_TIME 的信号节点按本值兜底；默认 2 小时 = 2 × entrance 侧
  // max.receive.hours（默认 1 小时，EventChecker.java:68-70），留足余量避免与 entrance 侧
  // maxLoopTime 兜底竞态误杀仍在合法等待的节点。
  val SIGNAL_NODE_MAX_RUNNING_TIME = CommonVars("wds.dss.flow.node.max.running.time.signal", 2 * 60 * 60 * 1000L)
```

**注意**：`CommonVars` 泛型使用 `Long`（Scala 2.11 下 `30 * 60 * 1000L` 为 Long 字面量，与 `CommonVars("...", 1800000L)` 等价）。

#### 3.3.2 DefaultNodeRunner.scala —— 核心方法重写

**文件**：`dss-orchestrator/orchestrators/dss-workflow/dss-flow-execution-server/src/main/scala/com/webank/wedatasphere/dss/flow/execution/entrance/node/DefaultNodeRunner.scala`

**改动一**：新增字段（line 52 `lastGetStatusTime` 之后）

```scala
  private var lastGetStatusTime: Long = 0   // 既有 line 52

  // ===== 以下为本次新增 =====

  /**
    * 最近一次成功获取到 entrance 状态的时刻。
    * 成功查询（无论终态与否）即刷新；节点启动时初始化为 startTime。
    * 用于"连续状态获取失败时长"判定（P0），及运行时长兜底的基础计时。
    * 并发说明：nodeRunnerQueue 为单队列 take-put 模型，同一 runner 同一时刻
    * 仅被一个 poller 线程持有，本字段读写天然串行，无需同步。
    */
  private var lastStatusSuccessTime: Long = _
```

**改动二**：`run()` 中初始化计时（line 111 `setStartTime` 之后追加一行）

```scala
      //set start time
      this.setStartTime(System.currentTimeMillis())
      this.lastStatusSuccessTime = this.startTime   // ← 新增：失败计时的起点
```

**改动三**：`isLinkisJobCompleted` 完整方法（替换 line 69-95）

```scala
  override def isLinkisJobCompleted: Boolean = Utils.tryCatch{

    val interval = System.currentTimeMillis() - lastGetStatusTime

    if ( interval < FlowExecutionEntranceConfiguration.NODE_STATUS_INTERVAL.getValue){
      return false
    }

    lastGetStatusTime = System.currentTimeMillis()

    if(NodeExecutionState.isCompleted(getStatus)) return true

    // P1: 节点级最大运行时长兜底（默认关闭；信号节点默认 2 小时），超限翻 Timeout 终态
    if (isNodeRunningTimeout()) {
      return transitionToTimeout()
    }

    val toState = NodeExecutionState.fromLinkisStateName(
      LinkisNodeExecutionImpl.getLinkisNodeExecution.getState(this.linkisJob))
    // 成功获取到状态（无论终态与否），刷新连续失败计时基准
    this.lastStatusSuccessTime = System.currentTimeMillis()
    if (NodeExecutionState.isCompleted(toState)) {
      val listener = LinkisNodeExecutionImpl.getLinkisNodeExecution.asInstanceOf[LinkisExecutionListener]
      listener.onStatusChanged(getStatus.toString, toState.toString, this.linkisJob)
      this.transitionState(toState)
      info(s"Finished to execute node of ${this.node.getName}")
      true
    } else {
      false
    }
  }{
    case e:ErrorException =>
      logger.warn(s"failed to get ${this.node.getName} state", e)
      false
    case t :Throwable =>
      logger.error(s"failed to get ${this.node.getName} state", t)
      // REQ-DSS-1.26.0-FIX-001: 查询异常属于暂时性故障，默认继续轮询；
      // 仅当连续失败时长超过阈值才升级为任务失败（kill + Failed），消除"返回 true 但不翻转状态"的语义裂缝
      escalateIfStateFetchTimeout(t)
  }
```

**改动四**：新增私有方法（`isLinkisJobCompleted` 之后）

```scala
  /**
    * P1: 节点级最大运行时长兜底判定。
    * 普通节点读 NODE_MAX_RUNNING_TIME（默认 -1 关闭）；
    * 信号节点（FlowExecutionUtils.isSignalNode 按节点 nodeType 识别）未显式配置普通项时
    * 按 SIGNAL_NODE_MAX_RUNNING_TIME 兜底（默认 2 小时）。
    */
  private def isNodeRunningTimeout(): Boolean = {
    val nodeType = if (this.node != null) this.node.getNodeType else ""
    var maxRunningTime = FlowExecutionEntranceConfiguration.NODE_MAX_RUNNING_TIME.getValue
    if (maxRunningTime <= 0 && FlowExecutionUtils.isSignalNode(nodeType)) {
      maxRunningTime = FlowExecutionEntranceConfiguration.SIGNAL_NODE_MAX_RUNNING_TIME.getValue
    }
    maxRunningTime > 0 && this.startTime > 0 &&
      System.currentTimeMillis() - this.startTime >= maxRunningTime
  }

  /**
    * P1: 运行超时翻转。先尝试 kill Linkis 任务（失败仅告警），再翻 Timeout 终态。
    * Timeout 属于终态（NodeExecutionState.isCompleted），FlowEntranceJob 的 Timeout 分支
    * 会迁移节点并结束工作流。
    */
  private def transitionToTimeout(): Boolean = {
    error(s"Node ${this.node.getName} exceeded max running time " +
      s"(${FlowExecutionEntranceConfiguration.NODE_MAX_RUNNING_TIME.getValue}ms configured, " +
      s"signal default applied if applicable), started at $startTime, will be turned to Timeout.")
    killLinkisJobQuietly()
    this.transitionState(NodeExecutionState.Timeout)
    true
  }

  /**
    * P0: 连续状态获取失败升级判定。
    * 自 lastStatusSuccessTime（最近一次成功获取状态的时刻）起算，
    * 持续失败达到 NODE_STATE_FETCH_FAIL_TIMEOUT（默认 30 分钟）时：
    * kill Linkis 任务（尽力而为）→ 节点置 Failed → error 告警 → 返回 true 结束轮询；
    * 未超阈值返回 false 继续轮询（附带连续失败时长 warn，便于运维提前介入）。
    */
  private def escalateIfStateFetchTimeout(t: Throwable): Boolean = {
    val failTimeout = FlowExecutionEntranceConfiguration.NODE_STATE_FETCH_FAIL_TIMEOUT.getValue
    val failedDuration = System.currentTimeMillis() - this.lastStatusSuccessTime
    if (failTimeout > 0 && failedDuration >= failTimeout) {
      error(s"Node ${this.node.getName} has failed to fetch Linkis job state for " +
        s"${failedDuration / 1000}s (threshold ${failTimeout / 1000}s since last success at " +
        s"$lastStatusSuccessTime). Escalate to Failed. Last exception: ${t.getMessage}", t)
      killLinkisJobQuietly()
      this.transitionState(NodeExecutionState.Failed)
      true
    } else {
      warn(s"Node ${this.node.getName} failed to fetch Linkis job state, " +
        s"continuous failure ${failedDuration / 1000}s (threshold ${failTimeout / 1000}s), " +
        s"keep polling. Cause: ${t.getMessage}")
      false
    }
  }

  /**
    * 尽力而为地 kill Linkis 任务：
    * 1) 复用 DefaultNodeRunner.cancel 的 kill 链路（LinkisNodeExecutionImpl.cancel → UJESClient.kill）；
    * 2) kill 一个已完成的任务是幂等的（设计文档 4.7），entrance 不可用时 kill 大概率失败，
    *    失败仅记录 warn，不阻断节点状态翻转。
    */
  private def killLinkisJobQuietly(): Unit = Utils.tryAndWarn {
    if (this.linkisJob != null && this.linkisJob.getJobExecuteResult != null) {
      LinkisNodeExecutionImpl.getLinkisNodeExecution.cancel(this.linkisJob)
    }
  }
```

**改动五**：新增 import（line 21-31 区域追加）

```scala
import com.webank.wedatasphere.dss.flow.execution.entrance.utils.FlowExecutionUtils
```

**改动要点汇总**（Before/After 对照见 3.3.5）：

1. `case t:Throwable` 分支不再无条件返回 true，改为调用 `escalateIfStateFetchTimeout(t)`；
2. 成功获取状态后刷新 `lastStatusSuccessTime`（放在 `fromLinkisStateName` 返回之后，getState 抛异常时不会误刷新）；
3. `NodeExecutionState.withName` 直调改为 `NodeExecutionState.fromLinkisStateName`（见 3.3.3）；
4. 升级/超时路径的 kill 复用 `LinkisNodeExecutionImpl.cancel`（与 `DefaultNodeRunner.cancel` line 130-137 同链路，但**不走 cancel() 方法本身**——避免置 Cancelled 状态与 `canceled` 标志位副作用，我们的目标终态是 Failed/Timeout）；
5. `run()` 中初始化 `lastStatusSuccessTime = startTime`。

#### 3.3.3 NodeExecutionState.scala —— 状态映射函数

**文件**：`dss-orchestrator/orchestrators/dss-workflow/dss-flow-execution-server/src/main/scala/com/webank/wedatasphere/dss/flow/execution/entrance/node/NodeExecutionState.scala`

**改动**：object 内追加映射函数（line 39 `isFailed` 之后）

```scala
  /**
    * REQ-DSS-1.26.0-FIX-001: Linkis 任务状态名 → NodeExecutionState 安全映射。
    *
    * 三级策略：
    * 1. 枚举快路径：withName 直接命中（Running/Succeed/Failed/Cancelled/Scheduled 等，正常路径零开销零变更）；
    * 2. 别名映射：Linkis 侧与 DSS 枚举拼写不一致的已知状态（见 LINKIS_STATE_ALIASES）；
    * 3. 未知兜底：返回 Running（非终态）继续轮询并 warn，不抛 NoSuchElementException。
    *
    * 注意：null/空串同样兜底 Running——entrance 极端场景下可能返回空状态串。
    */
  def fromLinkisStateName(stateName: String): NodeExecutionState = {
    val trimmed = if (stateName == null) "" else stateName.trim
    if (trimmed.isEmpty) {
      return Running
    }
    // 1. 枚举快路径（保留原 withName 语义，正常状态串行为与修复前完全一致）
    values.find(_.toString.equalsIgnoreCase(trimmed)) match {
      case Some(state) => return state
      case _ =>
    }
    // 2. 已知 Linkis 状态别名
    LINKIS_STATE_ALIASES.get(trimmed.toLowerCase) match {
      case Some(state) => state
      case _ =>
        // 3. 未知状态兜底：视为运行中，不中断轮询
        org.apache.linkis.common.utils.Logging.getLogger(getClass)
          .warn(s"Unknown Linkis job state [$trimmed], mapped to Running by default. " +
            s"Please check Linkis version compatibility.")
        Running
    }
  }

  private val LINKIS_STATE_ALIASES: scala.collection.mutable.Map[String, NodeExecutionState] = {
    val m = scala.collection.mutable.Map[String, NodeExecutionState]()
    // Linkis 1.x JobStatus 常见值（org.apache.linkis.governance.common.entity.job.JobStatus / 旧 ExecutionNodeStatus）
    // ---- 提交/排队中间态 → 映射为 Running（DSS 无对应等待枚举，且非终态语义一致）
    m("submitting") = Running
    m("queued") = Running
    m("scheduled") = Scheduled        // 需求提到的 Linkis "Sched" 类中间态
    m("sched") = Scheduled
    m("wait_for_retry") = WaitForRetry
    m("retry") = WaitForRetry
    m("init") = Inited
    m("idle") = Running
    // ---- 成功终态
    m("success") = Succeed
    m("succeeded") = Succeed
    m("succeed") = Succeed            // 冗余保护（大小写无关命中）
    // ---- 失败终态
    m("failed") = Failed
    m("failure") = Failed
    m("error") = Failed
    m("failed_for_resultsize") = Failed   // Linkis: 结果集超限失败
    // ---- 取消终态
    m("cancelled") = Cancelled
    m("canceled") = Cancelled
    m("cancel") = Cancelled
    m("killed") = Cancelled
    m("kill") = Cancelled
    m("shutdown") = Cancelled
    m("stopped") = Cancelled
    // ---- 超时终态
    m("timeout") = Timeout
    m("time_out") = Timeout
    m("scheduled_timeout") = Timeout     // Linkis: 排队超时（Sched+Timeout 组合态）
    m("queue_full_timeout") = Timeout    // Linkis: 队列满超时
    m
  }
```

> 实现 note：`getLogger` 实际建议改为传入 logger 或在 `DefaultNodeRunner` 调用侧打日志（枚举 object 混入 `org.apache.linkis.common.utils.Logging` trait 更简洁：`object NodeExecutionState extends Enumeration with Logging`，然后用 `logger.warn`）。编码阶段按"object 混入 Logging trait"落地，避免反射式 getLogger。

**完整映射表**（设计定稿，编码与单测按此覆盖）：

| Linkis 状态串（不区分大小写） | 映射到 NodeExecutionState | 是否终态 | 说明 |
|------------------------------|--------------------------|:------:|------|
| `Inited` / `init` | Inited | 否 | 初始化 |
| `Scheduled` / `scheduled` / `sched` | Scheduled | 否 | 排队/已调度（Linkis "Sched" 类中间态） |
| `WaitForRetry` / `wait_for_retry` / `retry` | WaitForRetry | 否 | 等待重试 |
| `Running` / `submitting` / `queued` / `idle` | Running | 否 | 运行中；提交中/排队中在 DSS 侧统一视为运行中（节点已提交） |
| `Succeed` / `success` / `succeeded` / `succeed` | Succeed | 是 | 成功 |
| `Failed` / `failed` / `failure` / `error` / `failed_for_resultsize` | Failed | 是 | 失败（含结果集超限失败） |
| `Cancelled` / `cancelled` / `canceled` / `cancel` / `killed` / `kill` / `shutdown` / `stopped` | Cancelled | 是 | 取消/被杀/停止 |
| `Timeout` / `timeout` / `time_out` / `scheduled_timeout` / `queue_full_timeout` | Timeout | 是 | 各类超时（含 Sched+Timeout 组合态） |
| `Skipped` | Skipped | 是 | 跳过（DSS 内部态，防御性保留） |
| （空串 / null） | Running | 否 | entrance 异常返回兜底 |
| **其他任何未知串** | Running + warn 日志 | 否 | 兜底：不中断轮询（对应需求 F-P0-03） |

映射表维护约定：Linkis 升级新增任务状态时，需同步检查 `org.apache.linkis.governance.common.entity.job.JobStatus` 枚举并补充映射（写入 R-05 缓解措施与部署文档）。

#### 3.3.4 FlowEntranceJob.scala —— Timeout 分支补充（P1 必备）

**文件**：`dss-orchestrator/orchestrators/dss-workflow/dss-flow-execution-server/src/main/scala/com/webank/wedatasphere/dss/flow/execution/entrance/job/FlowEntranceJob.scala`

**改动位置**：`onStatusChanged`（line 96-140），在 `case NodeExecutionState.Scheduled` 分支（line 133-136）之后、`case _` 之前插入：

```scala
      case NodeExecutionState.Timeout =>
        printLog(s"node($nodeName) exceeded max running time, turned to Timeout", "ERROR")
        if (NodeExecutionState.isRunning(fromState))
          FlowContext.changedNodeState(this.getFlowContext.getRunningNodes, this.getFlowContext.getFailedNodes, node, "node execute timeout")
        this.kill()
```

**理由**（设计新发现的闭环）：现状 match 无 Timeout 分支，翻 Timeout 后节点滞留 `runningNodes`（`FlowContextImpl.scala:29`），`isFlowCompleted`（`FlowEntranceJob.scala:265`）恒 false → 工作流仍然挂起。该分支行为对齐 Failed 分支（line 99-104）：迁移 runningNodes→failedNodes + kill 工作流（超时节点视作执行失败，工作流以失败结束、可重跑）。

#### 3.3.5 Before/After 对照（isLinkisJobCompleted 关键差异）

```scala
// ===== 问题代码（BEFORE，DefaultNodeRunner.scala:80/91-94）=====
    val toState = NodeExecutionState.withName(LinkisNodeExecutionImpl.getLinkisNodeExecution.getState(this.linkisJob))  // ← 未知状态抛 NoSuchElementException
    // ...（终态翻转逻辑，不变）
  }{
    case e:ErrorException => logger.warn(s"failed to get ${this.node.getName} state", e)
      false
    case t :Throwable => logger.error(s"failed to get ${this.node.getName} state", t)
      true    // ← Bug 点：返回 true 但未 transitionState，poller 停止轮询，节点永久 Running

// ===== 修复代码（AFTER，关键差异）=====
    val toState = NodeExecutionState.fromLinkisStateName(   // ← 枚举快路径 + 别名 + 未知兜底 Running
      LinkisNodeExecutionImpl.getLinkisNodeExecution.getState(this.linkisJob))
    this.lastStatusSuccessTime = System.currentTimeMillis() // ← 成功查询刷新失败计时基准
    // ...（终态翻转逻辑，保持不变）
  }{
    case e:ErrorException => logger.warn(s"failed to get ${this.node.getName} state", e)
      false                                                 // ← 行为不变：继续轮询
    case t :Throwable => logger.error(s"failed to get ${this.node.getName} state", t)
      escalateIfStateFetchTimeout(t)                        // ← 未超阈值 false 继续轮询；
                                                            //    超阈值 kill + Failed + true 收敛
```

### 3.4 新增配置清单

| 配置 key | 默认值 | 单位 | 优先级 | 说明 |
|---------|-------|------|:----:|------|
| `wds.dss.flow.node.state.fetch.fail.timeout` | `1800000`（30 分钟） | ms | P0 | 连续状态获取失败升级阈值：自最近一次成功获取 entrance 状态起算，持续失败达到该时长 → kill Linkis 任务 + 节点置 Failed + error 告警。`<=0` 视为永不升级（不建议，恢复旧行为） |
| `wds.dss.flow.node.max.running.time` | `-1`（关闭） | ms | P1 | 节点级最大运行时长兜底：非终态节点从 startTime 起超过该时长 → kill + 置 Timeout。默认关闭以避免误杀长任务 |
| `wds.dss.flow.node.max.running.time.signal` | `7200000`（2 小时） | ms | P1 | 信号节点专属兜底：当上一配置为 -1 且节点为信号节点（`wds.dss.flow.signal.nodes` 匹配 nodeType）时生效。默认 = 2 × entrance 侧 `max.receive.hours`（1h），覆盖 entrance 侧兜底也失效的极端场景 |
| `wds.linkis.flow.connection.timeout`（既有） | 代码默认 `300000` 不变 | ms | P1 部署项 | **部署侧建议**灰度调整为 `60000`（见 2.4 分析）；不拆分 connection/read 两配置 |

### 3.5 修改文件清单

| # | 文件 | 改动类型 | 内容 |
|:-:|-----|:------:|------|
| 1 | `.../conf/FlowExecutionEntranceConfiguration.scala` | 新增 | 3 个配置项定义（3.3.1） |
| 2 | `.../node/DefaultNodeRunner.scala` | 核心修改 | 新增字段 `lastStatusSuccessTime`；重写 `isLinkisJobCompleted`（69-95）；新增 4 个私有方法；`run()` 初始化计时；新增 import |
| 3 | `.../node/NodeExecutionState.scala` | 新增 | `fromLinkisStateName` 映射函数 + `LINKIS_STATE_ALIASES` 映射表（object 混入 Logging） |
| 4 | `.../job/FlowEntranceJob.scala` | 新增分支 | `onStatusChanged` 补 `case NodeExecutionState.Timeout`（P1 必备） |
| 5 | `.../execution/NodeExecutionStatusPoller.scala` | **不改动** | 通过返回值语义修复间接生效（需求 N-02；"status is completed" 日志在语义修复后不再误导——true 必然伴随终态） |
| 6 | `.../utils/FlowExecutionUtils.scala` | **不改动** | `isSignalNode`（74-76）首次获得调用方（DefaultNodeRunner），方法本体不动 |
| 7 | `.../utils/LinkisUjesClientUtils.java` | **不改动** | 不拆分超时配置（2.4 结论 3） |
| 8 | dss-eventchecker-appconn 全部 | **不改动** | 仅关联分析对象（需求 N-01/N-04） |

---

## 四、关键设计决策

### 4.1 决策一：连续失败时长阈值（方案C）而非失败计数器或纯返回 false

见 2.1。核心：时长语义与单次超时配置（readTimeout）解耦，30 分钟阈值在 300s 或 60s 超时配置下都稳定表达"抖动 vs 真不可用"的边界；计数器（方案B）会被 P1-02 配置调优隐式缩放真实时长，纯 false（方案A）在 entrance 长期宕机时拖死 poller 线程池，方案D 单独使用因轮询已停止而不可达。

### 4.2 决策二：计时基准为 lastStatusSuccessTime 而非首次失败时刻

`escalateIfStateFetchTimeout` 以"最近一次成功获取状态的时刻"起算，任一次成功查询即自动重置计时。对比"记录首次失败时间戳 + 成功时清零"方案：
- 成功刷新发生在成功路径主线上（一行赋值），无独立清零逻辑，状态机更简单；
- 语义上"连续失败时长"的连续性由 lastStatusSuccessTime 直接定义，无边界歧义（如成功清零与失败记录的竞态窗口）；
- 初始化为 startTime（节点启动时刻），保证节点从提交起就处于计时保护之下（若提交后 entrance 立即不可用，30 分钟后同样收敛为 Failed 而非永久 Running）。

### 4.3 决策三：状态映射采用"枚举快路径 + 别名表 + 未知兜底 Running"

见 2.2 与 3.3.3。关键取舍：
1. **快路径保证正常路径零变更**：`values.find(_.toString.equalsIgnoreCase(trimmed))` 先行，entrance 健康时返回的状态串（Running/Succeed/Failed/Cancelled 等，DSS 与 Linkis 1.x JobStatus 主名一致）全部走快路径，与原 `withName` 行为等价（equalsIgnoreCase 比逐字 withName 更宽容，属安全性增强而非行为变更）；
2. **终态别名必须确定性翻译**：`success/killed/timeout/scheduled_timeout` 等若被兜底为 Running，终态节点会被持续轮询并最终被失败阈值误杀为 Failed——把"成功任务"标成"失败"，语义失真不可接受；
3. **未知兜底 Running 而非 Failed**：未知的中间态（Linkis 未来新增）大概率非终态，兜底 Running + 持续 warn 让系统有时间被发现与修复（R-05），误杀代价（Failed）大于多轮询代价。

### 4.4 决策四：节点级超时默认关闭 + 信号节点差异化默认开启

见 2.3。普通节点默认 -1 的原因：DSS 工作流存在多小时合法任务（大 SQL/Spark 作业），任何全局默认阈值都有误杀面，兜底能力交由部署按需开启；信号节点（本次故障类型）合法等待时长有上界（entrance 侧 max.receive.hours 默认 1h + maxLoopTime 强制结束），DSS 侧 2h 兜底不会误杀合法等待，默认开启收益（本次故障类型直接获得保护）大于风险。

### 4.5 决策五：升级 kill 不复用 cancel() 方法，另立 killLinkisJobQuietly

`DefaultNodeRunner.cancel`（line 130-137）会置 `canceled = true` 并 `transitionState(Cancelled)`——语义是"用户主动取消"。升级场景的目标终态是 Failed（P0）/Timeout（P1），直接复用 cancel 会：(1) 状态被 Cancelled 覆盖，与 transitionState(Failed/Timeout) 产生双翻转（后一次生效但触发两次 listener 回调）；(2) `canceled` 标志位被污染。故抽取 kill 动作本身（`LinkisNodeExecutionImpl.cancel(this.linkisJob)`，与 cancel() 内部同一调用）到 `killLinkisJobQuietly`，状态翻转由升级路径独立控制。两处翻终态（transitionState(Failed) 与 kill 触发的 listener 回调）之间可能产生一次多余的 onStatusChanged，但 `transitionState` 有 `if (getStatus == toState) return` 幂等保护（`NodeRunner.scala:77`），且 FlowEntranceJob 的 Failed 分支会 kill 整工作流（`FlowEntranceJob.scala:99-104`），kill 后 `killNodes` 对各 runner 调 cancel 时同样有 canceled 幂等保护（line 130-131），无重复 kill 风险。

### 4.6 决策六：`lastStatusSuccessTime` 不做持久化

计时状态在 runner 实例内存中，flow-execution-server 重启即丢失（重新从新 startTime 计数）。理由（对应 R-03）：服务重启本身就会中断工作流执行（既有行为，工作流引擎不恢复跨进程轮询），丢失的计时随节点重新提交而重置，语义自洽；持久化需引入存储介质与恢复逻辑，收益仅覆盖"重启后 entrance 仍不可用"的窄场景，且重启后新计时同样会在 30 分钟后收敛。不做。

### 4.7 决策七：kill 一个可能已完成的 Linkis 任务——幂等性分析

升级路径 kill 时，entrance 可能已恢复且任务实际已 Succeed（30 分钟失败窗口的尾巴）。kill 是否安全：

| 场景 | kill 调用行为 | 结果 |
|-----|-------------|------|
| 任务已 Succeed（终态） | entrance 对已完成任务的 kill 请求返回成功或"任务不存在/已完成"类响应；Linkis entrance 对终态任务的 kill 是空操作（任务已从执行队列移除） | 无副作用；DSS 侧节点已置 Failed（查不到最终真实状态），工作流失败结束，可重跑 |
| 任务仍在运行 | kill 正常终止任务 | 符合预期（entrance 已 30 分钟不可达，任务状态不可知，终止重跑是保守正确动作） |
| entrance 仍不可用 | kill 请求本身超时/失败 | `killLinkisJobQuietly` 的 tryAndWarn 吞掉异常仅 warn；节点照常置 Failed，收敛不受影响 |
| 任务 ID 已过期清理 | entrance 返回任务不存在 | 同场景 1，无副作用 |

结论：**kill 已完成的 Linkis 任务是幂等的/无害的**——entrance 侧终态任务不因重复 kill 改变状态或产生副作用；最差结果是"任务实际成功但节点被置 Failed"，工作流以失败结束、可重跑，相比现状（永久挂起）是严格的语义改善。kill 失败不阻断状态翻转（tryAndWarn），保证收敛不依赖 kill 成功。

### 4.8 决策八：不重构 poller，不修改 NodeRunner 接口签名

需求 N-02 与 7.1 约束。修复通过 `isLinkisJobCompleted` 的返回值语义（true ⇔ 节点已到终态）实现：
- 修复前：true 可能意味着"完成"或"查询异常"（Bug 根源）；
- 修复后：true 当且仅当 `getStatus` 为终态（已终态短路 / 终态翻转 / Failed 升级 / Timeout 超限四种路径均先保证状态翻转再返回 true）。
poller 的 "status is completed" 日志（`NodeExecutionStatusPoller.scala:37`）随之语义正确，无需改动，误导性日志问题（需求 6.4）自动消解。

---

## 五、兼容性影响

### 5.1 正常路径不受影响（逐路径核对）

| 路径 | 修复前 | 修复后 | 结论 |
|-----|-------|-------|:----:|
| interval 短路（<3000ms → false） | line 71-75 | 逐字保留 | 一致 |
| 节点已终态（isCompleted → true） | line 79 | 逐字保留 | 一致 |
| 查询成功 + 终态翻转（onStatusChanged → transitionState → info → true） | line 80-86 | 控制流保留；`withName` 换为 `fromLinkisStateName`（快路径对合法状态串等价命中）+ 一行刷新时间戳 | 行为一致 |
| 查询成功 + 非终态（→ false） | line 87-89 | 同上，行为一致 | 一致 |
| ErrorException（warn → false） | line 91-92 | 逐字保留 | 一致 |
| 查询异常（Throwable） | line 93-94：error → **true**（Bug） | error → 时长判定（false 或 Failed+true） | **唯一行为变更点，即修复目标** |
| EmptyJob 提交直翻 Succeed | line 112-116 | 不改动 | 一致 |
| 用户取消（cancel → Cancelled） | line 130-137 | 不改动 | 一致 |
| BranchNodeRunner（isLinkisJobCompleted 恒为 isCompleted(status)，line 64） | — | 不改动，不受影响 | 一致 |
| run() 提交失败（catch → transitionState(Failed)） | line 120-124 | 不改动（仅上方加一行计时初始化） | 一致 |

### 5.2 现有配置语义不变

| 配置 | 修复前 | 修复后 | 结论 |
|-----|-------|-------|:----:|
| `wds.linkis.flow.connection.timeout`（默认 300000） | connection/read 双用 | 完全不动（部署侧可调 60000，属既有配置用法） | 一致 |
| `wds.dds.flow.node.status.poller.*`（线程 20/调度 5s/间隔 3000ms） | — | 完全不动 | 一致 |
| `wds.dss.flow.signal.nodes`（默认 eventreceiver） | 已定义未使用 | **首次被调用**（识别信号节点用），配置含义与取值不变 | 一致（新调用方） |
| `wds.dss.flow.skip.nodes` 等 | — | 不动 | 一致 |

### 5.3 新增配置的兼容性

- 3 个新配置全部有默认值，不配置即生效（P0 阈值 30min / P1 普通节点关闭 / 信号节点 2h）；
- 不新增数据库表、不改表结构、不改 `assembly/config/config.sh` 必填项；
- 需求 AC-06 场景（零配置升级）行为：比修复前多了 30 分钟失败收敛与信号节点 2h 超时兜底，正常任务不受影响。

### 5.4 接口/类签名兼容

| 项 | 变更 | 兼容性 |
|---|------|:----:|
| `NodeRunner.isLinkisJobCompleted` 抽象方法签名 | 不变（实现内部增强） | ✅ |
| `DefaultNodeRunner` 公有方法集合 | 不新增公有方法（新增均为 private） | ✅ |
| `NodeExecutionState` 枚举值集合 | 不变（Timeout 为既有值，首次被赋值使用） | ✅ |
| `NodeExecutionState` object 新增成员 | `fromLinkisStateName` + `LINKIS_STATE_ALIASES`（新增，不覆盖既有方法） | ✅ |
| `FlowEntranceJob.onStatusChanged` | 新增 Timeout match 分支（不改既有分支） | ✅ |
| 对 NodeRunnerListener/FlowEntranceJob 的调用方式 | transitionState → onStatusChanged 链路复用，无新事件类型 | ✅ |

### 5.5 运行环境兼容

- JDK 8 / Scala 2.11.12：新代码仅使用 String#equalsIgnoreCase、Option/mutable.Map、Utils.tryAndWarn、System.currentTimeMillis，无高版本 API；
- Linkis 1.18.3-wds 依赖不动；UJES Client 行为不动（仅部署侧可调超时配置值）。

---

## 六、测试策略

### 6.1 单元测试覆盖点

| 测试类 | 测试方法 | 覆盖场景 | 验收标准 |
|-------|---------|---------|---------|
| `NodeExecutionStateTest`（新增） | `testFromLinkisStateName_EnumFastPath` | 枚举内状态串（Running/Succeed/Failed/Cancelled/Scheduled，含大小写变体 running/SUCCEED） | 返回对应枚举值，与 withName 结果一致 |
| `NodeExecutionStateTest` | `testFromLinkisStateName_Aliases` | 全部别名表条目（success/killed/timeout/scheduled_timeout/submitting/queued/wait_for_retry/failed_for_resultsize 等，见 3.3.3 映射表） | 逐条断言映射结果；终态别名的 isCompleted 为 true |
| `NodeExecutionStateTest` | `testFromLinkisStateName_UnknownFallback` | 未知串（如 "Sched"+"Unknown"）、空串、null | 均返回 Running，不抛异常；warn 日志含原始串（未知串场景） |
| `DefaultNodeRunnerTest`（新增/扩展） | `testIsLinkisJobCompleted_ShortGlitch_KeepPolling` | mock getState 第 1-2 次抛 SocketTimeoutException，第 3 次返回 Succeed；阈值设 30min | 前两次返回 false 且状态仍 Running；warn 日志含连续失败时长；第三次返回 true 且状态翻 Succeed |
| `DefaultNodeRunnerTest` | `testIsLinkisJobCompleted_EscalateToFailed` | mock getState 持续抛异常；`lastStatusSuccessTime` 手工回拨 31 分钟（或阈值调小加速） | 达阈值那轮返回 true；状态翻 Failed；killLinkisJobQuietly 被调用（cancel 链路）；error 日志含失败时长与异常摘要 |
| `DefaultNodeRunnerTest` | `testIsLinkisJobCompleted_ErrorException_KeepPolling` | mock getState 抛 ErrorException | 返回 false，状态不变（行为与修复前一致） |
| `DefaultNodeRunnerTest` | `testIsLinkisJobCompleted_SuccessRefreshesTimer` | 失败→成功（Running）→失败 交替，成功后失败时长从 0 重算 | 成功查询后 lastStatusSuccessTime 被刷新（可通过反射或日志断言）；不误升级 |
| `DefaultNodeRunnerTest` | `testIsNodeRunningTimeout_DisabledByDefault` | 普通节点默认 -1，运行超久 | 不触发 Timeout，行为与修复前一致 |
| `DefaultNodeRunnerTest` | `testIsNodeRunningTimeout_SignalNodeDefault` | nodeType=`linkis.appconn.eventchecker.eventreceiver`，startTime 回拨 2.5h | 触发：kill 调用 + 状态翻 Timeout + 返回 true |
| `DefaultNodeRunnerTest` | `testIsNodeRunningTimeout_SignalNodeWithinWindow` | 同上但回拨 1.5h（entrance 侧 max.receive.hours 内） | 不触发 |
| `DefaultNodeRunnerTest` | `testKillLinkisJobQuietly_FailureNotBlocking` | mock LinkisNodeExecutionImpl.cancel 抛异常 | 异常被吞（warn），transitionState(Failed) 正常执行 |
| `DefaultNodeRunnerTest` | `testRun_InitializesLastStatusSuccessTime` | 正常 run() 一次 | lastStatusSuccessTime == startTime |

> 单测可行性说明：`LinkisNodeExecutionImpl.getLinkisNodeExecution` 为静态单例（`LinkisNodeExecutionImpl.java:69-71`），单测需通过可注入手段（测试子类覆盖/反射替换/mockito-inline 静态 mock，按模块既有测试基建选定）替换 getState/cancel 行为；`linkisJob.getJobExecuteResult == null` 时 killLinkisJobQuietly 应跳过（已含 null 判断，与 cancel() line 133 同口径）。

### 6.2 集成测试场景

| 编号 | 场景 | 前置 | 步骤 | 预期 | AC |
|:----:|-----|-----|-----|-----|:--:|
| IT-01 | 短暂抖动自愈 | 工作流B含 ER 节点（msg.after.send=false）Running；A 的 Sender 信号已写 event_queue | tc 对 entrance 注入 5 分钟网络延迟（> readTimeout）；恢复；等 1-2 轮询周期 | 抖动期节点保持 Running 且在队列中被轮询（warn 周期输出）；恢复后 ER 下一周期翻 Succeed，下游调度；全程无"true 但 Running"组合 | AC-01 |
| IT-02 | 长期不可用收敛 | 同上；测试配置 `wds.dss.flow.node.state.fetch.fail.timeout=120000`（2 分钟加速） | entrance 压力持续超过 2 分钟；观察节点/日志/Linkis 任务 | 达阈值时节点 kill + 翻 Failed；error 告警（节点名/时长/摘要）；工作流失败结束可重跑；runner 移出队列不重复处理 | AC-02 |
| IT-03 | 未知状态串 | 测试桩使 getState 返回 "Sched"（未知）；随后恢复 "Running"→"Succeed" | 观察 2+ 轮询周期 | 未知串映射 Running 继续轮询不抛 NoSuchElementException；warn 含原串；恢复后按真实终态翻转 | AC-03 |
| IT-04 | 正常任务回归 | entrance 健康；含 Scriptis 节点的工作流 | 分别执行：成功脚本 / 抛错脚本 / 运行中人工取消 | 三场景分别翻 Succeed/Failed/Cancelled，runner 正常移出队列，工作流正常推进/结束——与修复前行为一致 | AC-04 |
| IT-05 | EmptyJob 与 Sender 回归 | 含空节点与 EventSender 的工作流 | 正常执行 | EmptyJob 提交即 Succeed（112-116 路径不变）；Sender 发送成功翻 Succeed；均不进异常分支 | AC-05 |
| IT-06 | 零配置升级 | 不加任何新配置启动 | 跑含信号节点工作流全流程 | 新配置按默认生效不报错；`wds.linkis.flow.connection.timeout` 不配时语义同修复前 | AC-06 |
| IT-07 | 信号节点超时兜底 | ER 节点 entrance 侧 maxLoopTime 被禁用/失效模拟（或测试桩使 getState 恒返回 Running）；不配 max.running.time | 等待超过 2 小时（或临时调小 signal 配置加速） | 节点翻 Timeout；FlowEntranceJob 打印 timeout 日志；runningNodes→failedNodes；工作流失败结束（不挂起） | AC-02 变体 |
| IT-08 | 普通节点超时兜底开关 | 配 `wds.dss.flow.node.max.running.time=60000`；普通节点 | 正常执行一个 2 分钟任务 | 60s 时节点被 kill + 翻 Timeout，工作流失败——验证开关生效与 FlowEntranceJob Timeout 分支 | AC-02 变体 |
| IT-09 | 配置调优验证（P1-02） | 灰度环境 `wds.linkis.flow.connection.timeout=60000` | 提交大 SQL 任务 + 大结果集节点 + 正常信号工作流 | 提交/日志/结果拉取不因 60s 超时异常失败；状态轮询故障期间线程占用明显缩短 | R-04 |
| IT-10 | kill 重跑应急（P2） | 已卡死 ER 节点（修复前版本构造或拔掉修复），msg.after.send 取 false/true 两组 | kill 卡死节点→重跑工作流B | false 组：游标续读，旧信号可被接收翻 Success；true 组：窗口起点为新实例启动时间，旧信号不消费，需人工重发后翻 Success | AC-07 |

### 6.3 回归测试范围

| 范围 | 测试要点 |
|-----|---------|
| AppConn 节点状态轮询 | Scriptis/Visualis/Qualitis/_sendemail 等各类型 AppConn 节点的提交-轮询-翻转全链路（同一 isLinkisJobCompleted 路径） |
| 终态翻转四路径 | 已终态短路 / 查询到终态 / 提交失败 catch / 用户取消（各路径返回值与状态一致性） |
| 信号节点跨工作流依赖 | Sender→event_queue→Receiver 正常收发（entrance 健康）流程 |
| 工作流失败传导 | 节点 Failed → FlowEntranceJob.kill → killNodes 级联取消（99-104 既有链路，升级路径复用） |
| EmptyJob/BranchNodeRunner | 空节点直翻、分支节点评估（均不走受改路径，防误伤） |
| 服务重启 | 重启后新提交工作流计时正常（lastStatusSuccessTime=startTime 初始化） |

---

## 七、上线与回滚方案

### 7.1 发布策略

| 项 | 方案 |
|---|------|
| 发布类型 | 常规迭代发布（P1）；若线上挂起事故持续发酵，可裁剪为仅含 P0 改动的 Hot Fix 先行 |
| 构建验证 | `mvn clean package -pl dss-orchestrator/orchestrators/dss-workflow/dss-flow-execution-server -am -DskipTests` |
| 灰度阶段 | SIT（IT-01~IT-08 全过，阈值调小加速验证）→ 预生产（IT-09 配置调优验证 2 天，含大任务提交）→ 生产灰度单节点 2 天（观察告警日志与终态分布）→ 全量 |
| 发布时间窗 | 建议白天业务低峰发布，便于首日观察升级告警是否误触发 |
| 配置下发 | 生产初始建议：P0 阈值保持默认 30min；`wds.linkis.flow.connection.timeout=60000` 灰度验证后再全量；P1 两个超时配置先默认（普通关/信号 2h） |

### 7.2 回滚方案

**回滚触发条件**：
- 正常节点出现批量误翻 Failed/Timeout（升级/超时误杀，日志关键字 "Escalate to Failed" / "exceeded max running time" 异常高频）；
- 正常路径翻转行为异常（Succeed/Failed/Cancelled 回归失败）；
- 编译/类加载异常（NodeExecutionState object 混入 Logging 引发的初始化问题）。

**回滚步骤**：

| 步骤 | 操作 | 验证点 |
|:---:|------|-------|
| 1 | 值班 SRE 依据告警（升级误杀日志高频）评估确认 | 确认需回滚 |
| 2 | 发布平台回滚 flow-execution-server 到上一版本包，重启 | 服务启动正常 |
| 3 | 功能回归：正常工作流执行、终态翻转 | 与修复前行为一致 |
| 4 | 配置回收：若已下发 60000 超时配置，评估是否恢复 300000 | 状态查询/提交正常 |

**回滚代码路径**：仅替换 jar 包；无数据库变更；新配置项残留无副作用（旧代码不读取）。

**止血替代**（回滚期间若 entrance 再抖动）：沿用人工 SOP——识别卡死节点 kill 重跑（见 8.2）。

### 7.3 监控方案

| 监控项 | 方式 | 告警阈值/动作 |
|-------|------|-------------|
| 连续失败进行中（提前介入窗口） | 日志关键字 "continuous failure"（warn） | 单节点持续出现 > 10 分钟 → 值班群提醒 |
| 升级失败收敛 | 日志关键字 "Escalate to Failed"（error） | 触发即告警；生产首周若 > N 次/天（N 按工作流规模定）需复核阈值 |
| 超时兜底触发 | 日志关键字 "exceeded max running time"（error） | 触发即告警，复核是否误杀 |
| 未知状态串 | 日志关键字 "Unknown Linkis job state"（warn） | 出现即提醒检查 Linkis 版本兼容（R-05） |
| 节点终态分布 | "Finished to execute node of"（info） vs 升级/超时 error 比例 | 升级+超时占比异常升高 → 复核 entrance 健康 |
| entrance 健康（诱因侧） | Linkis 侧 GC/负载监控 | Full GC > 1 次/小时或负载 > 阈值 → 联动 Linkis 运维（D-01） |

---

## 八、运维治理建议（P2，非代码改动）

### 8.1 entrance 侧容量治理（诱因消除，Linkis 侧工单跟进）

| 措施 | 说明 | 归属 |
|-----|------|------|
| entrance 隔离 | 将 DSS 工作流调度流量与交互式（Scriptis 即席查询）流量入口隔离，避免交互高峰拖垮调度状态查询 | Linkis 运维 |
| entrance 扩容 | 状态查询 QPS 随 DSS 工作流并发线性增长（每节点每 3s 一次），按峰值工作流节点数评估 entrance 实例数 | Linkis 运维 |
| JVM 调优 | entrance 进程切换 G1（`-XX:+UseG1GC`），合理设置 MaxHeapSize 与_pause 时间目标，降低 Full GC 停顿频次与时长（分钟级 STW 是本次故障直接诱因） | Linkis 运维 |
| 网关链路 | 排查 gateway→entrance 转发超时配置，与 DSS 侧 readTimeout 联动（网关超时应小于客户端 readTimeout，避免客户端先行超时产生噪声失败） | Linkis 运维 |

### 8.2 应急 SOP：已卡死 Running 节点的 kill 重跑

> 适用：存量已卡死节点（修复上线前产生）的清理恢复。

**kill 重跑安全性分析**（依据 `AbstractEventCheckReceiver.java` 已核实代码）：

| 场景 | 机制 | kill 重跑结论 |
|-----|------|-------------|
| `msg.after.send=false`（全窗口消费） | 查询窗口起点为 `allStartTime`（10000 年，`AbstractEventCheckReceiver.java:48/196`）；消费游标持久化于 `event_status` 表 msg_id（`getOffset` line 127-154）；游标推进用 `INSERT ... ON DUPLICATE KEY UPDATE` 且仅当 msg_id 未被其他消费者推进时更新（`updateMsgOffset` line 81-122，line 93 SQL 的 CASE 条件） | **安全**：重跑后从上次游标继续，`event_queue` 消息不丢不重，旧信号可被正常接收，ER 节点翻 Success |
| `msg.after.send=true`（仅消费启动后新信号） | 窗口起点为 `nowStartTime` = receiver 实例进程启动时间（line 50 `initReceiverTimes`，line 188/194 `createExecuteType` 引用） | **陷阱**：kill 重跑后新实例的 nowStartTime 晚于旧信号写入时间，**旧信号落在窗口外不会被消费**；必须人工重发信号（或业务确认放弃该信号）后 ER 才翻 Success |

**SOP 步骤**：

1. 定位卡死节点：DSS 前端工作流实例页筛选 Running 超长节点，或日志检索该节点最后一次 "failed to get ... state"；
2. 确认 entrance 侧该任务实际状态（Linkis 管理台按 taskID 查询）：若已 Succeed 而 DSS 仍 Running，属本 Bug 存量 victim；
3. kill 卡死的工作流实例（DSS 页面 Kill，走 `FlowEntranceJob.kill → killNodes → runner.cancel`）；
4. 检查 ER 节点 `msg.after.send` 参数：
   - false：直接重跑工作流，ER 按游标续读旧信号，预期翻 Success；
   - true：先通知上游业务方重发信号（写入 event_queue），再重跑工作流；
5. 重跑后观察 ER 节点 1-2 个 query.frequency（60s）周期内的状态翻转与 `event_status` 游标推进日志（"The last record id was"）；
6. 修复版本上线后，存量卡死节点不会再新增；上述 SOP 保留用于存量清理。

### 8.3 阈值运维约定

- entrance 计划内维护（预期 > 30 分钟）：提前暂停在跑与待调度工作流，或临时调大 `wds.dss.flow.node.state.fetch.fail.timeout`（如维护时长 + 30 分钟缓冲），维护完成后恢复（R-01）；
- 用户配置超大 `max.receive.hours`（> 2 小时）的 ER 节点：需同步调大 `wds.dss.flow.node.max.running.time.signal`，避免 DSS 侧先于 entrance 侧误杀（R-02）；
- Linkis 版本升级：按 3.3.3 映射表维护约定检查新增任务状态并补充别名（R-05）。

---

## 九、风险评估与缓解

| 编号 | 风险项 | 等级 | 影响 | 概率 | 应对措施 |
|:----:|-------|:----:|-----|:----:|---------|
| R-01 | 计划内 entrance 长维护（>30min）导致在跑工作流批量翻 Failed | 中 | 工作流批量失败（可重跑） | 中 | 阈值可配置；运维约定 8.3（维护前暂停/临时调大）；Failed 可重跑，损失可控 |
| R-02 | 升级 kill 与 entrance 侧 maxLoopTime 竞态：DSS 先 kill 时 entrance 任务仍在合法等待 | 低 | 信号等待任务被提前终止 | 低 | 信号节点独立 2h 阈值（≈2×max.receive.hours），与 P0 失败阈值（30min）互不干扰；kill 前 error 日志留痕 |
| R-03 | 失败计时状态在 runner 内存，服务重启丢失重新计数 | 低 | 重启后首个 30min 窗口内不可用不升级 | 低 | 重启本身中断工作流（既有行为），重置语义自洽（4.6）；不做持久化 |
| R-04 | readTimeout 调小（300s→60s）后极端慢操作被误判超时 | 中 | 大任务提交/大结果集拉取偶发失败（有重试） | 中 | 仅部署侧调整不改默认；IT-09 灰度验证大任务提交与结果拉取；可配置回退 300000 |
| R-05 | 未知状态映射 Running 掩盖 Linkis 新增失败态 | 低 | 新失败态节点延迟发现（最终被 30min/2h 兜底收敛） | 低 | warn 日志持续输出原串（监控项已配置）；映射表维护约定 8.3 |
| R-06 | 升级 kill 与 entrance 实际已完成的竞态 | 低 | 任务实际成功但节点置 Failed（可重跑） | 低 | kill 幂等性分析（4.7）：entrance 对终态任务 kill 空操作；收敛语义严格优于永久挂起 |
| R-07 | `FlowEntranceJob` Timeout 分支遗漏导致新形态挂起（P1 上线残缺） | 高 | 超时节点翻 Timeout 但工作流仍不完成 | —（设计已闭环） | **本设计已将 3.3.4 列为 P1 必备改动**；测试 IT-07/IT-08 专项验证工作流可完成；上线检查清单强制核对 |
| R-08 | NodeExecutionState object 混入 Logging trait 引发初始化顺序问题 | 低 | 类加载异常 | 低 | 编码阶段验证；回滚触发条件含此项；必要时改为调用方打日志 |
| R-09 | 大量节点同时达 30min 阈值造成 kill 风暴（entrance 恢复瞬间） | 低 | entrance 恢复期负载毛刺 | 低 | kill 前 error 日志；阈值可按环境差异化；毛刺量级 = 卡住节点数，一次性 |
| D-01 | entrance 容量治理属 Linkis 侧范畴，DSS 无法单方面消除诱因 | 高 | 抖动仍会发生（但 DSS 侧行为正确收敛） | 中 | 本设计保证收敛（P0/P1）；容量治理以 8.1 工单推进，不承诺消除 |
| D-02 | `msg.after.send=true` 场景 kill 重跑依赖人工重发信号 | 中 | 应急恢复需业务配合 | 中 | SOP 8.2 明确两种场景差异；写入用户文档 |

---

## 十、附录

### 10.1 关键代码位置索引（修复前，行号已核实）

| 文件 | 行号 | 说明 |
|------|:----:|------|
| `.../node/DefaultNodeRunner.scala` | 69-95 | `isLinkisJobCompleted` 全量实现（核心修复对象） |
| 同上 | 73-75 | interval 检查（NODE_STATUS_INTERVAL 默认 3000ms） |
| 同上 | 80 | `NodeExecutionState.withName(...)` 直调（未知状态抛 NoSuchElementException 风险点） |
| 同上 | 84 | 正常终态翻转 transitionState（true 路径唯一合法翻转点） |
| 同上 | 91-92 | ErrorException → false（正确示范，保留） |
| 同上 | 93-94 | **Bug 点**：Throwable → true 且不 transitionState |
| 同上 | 101-127 | run()：提交入口（line 111 setStartTime，118 runJob） |
| 同上 | 112-116 | EmptyJob 直翻 Succeed（不受影响路径） |
| 同上 | 120-124 | 提交失败 catch → transitionState(Failed) |
| 同上 | 130-137 | cancel()：kill 链路（升级 kill 复用其内部调用而非方法本身） |
| 同上 | 48/52 | startTime / lastGetStatusTime 字段（新字段 lastStatusSuccessTime 加在其后） |
| `.../execution/NodeExecutionStatusPoller.scala` | 30-39 | poller（take-判断-put 回，不改动） |
| 同上 | 34-38 | false 才 put 回；true 仅打日志 |
| `.../execution/DefaultFlowExecution.scala` | 38/81-86 | nodeRunnerQueue 定义与 poller 调度（5s，线程上限 20） |
| `.../conf/FlowExecutionEntranceConfiguration.scala` | 26/28/31 | poller 线程 20 / 调度 5s / 间隔 3000ms |
| 同上 | 55 | SIGNAL_NODES（新配置项追加于此之后） |
| `.../node/NodeExecutionState.scala` | 24 | Timeout 枚举值（首次启用） |
| 同上 | 32-35 | isCompleted（Timeout 属终态，启用即就绪） |
| `.../job/FlowEntranceJob.scala` | 96-140 | onStatusChanged（**无 Timeout 分支，3.3.4 补齐**） |
| 同上 | 99-104 | Failed 分支（Timeout 分支行为对齐对象） |
| 同上 | 265/267-273 | isFlowCompleted / tryCompleted（Timeout 不迁移节点则永不完成） |
| 同上 | 249-255/275-281 | kill() / killNodes（失败传导链路，复用） |
| `.../FlowContext.scala` | 55-64 | changedNodeState（节点在 map 间迁移） |
| `.../FlowContextImpl.scala` | 29/35 | runningNodes / failedNodes |
| `.../utils/FlowExecutionUtils.scala` | 74-76 | isSignalNode（首次接线） |
| `.../job/parser/FlowJobNodeParser.scala` | 62 | nodeType → LINKIS_TYPE（isSignalNode 入参来源：node.getNodeType） |
| `.../node/BranchNodeRunner.scala` | 64 | isLinkisJobCompleted 恒 isCompleted(status)（不受影响） |
| `.../node/NodeRunner.scala` | 56/76-81 | 接口签名（不变）/ transitionState 幂等保护 |
| `dss-linkis-node-execution/.../conf/LinkisJobExecutionConfiguration.java` | 57 | LINKIS_CONNECTION_TIMEOUT 默认 300000（不改） |
| `dss-linkis-node-execution/.../utils/LinkisUjesClientUtils.java` | 40/44（0.x）/ 57/59（1.x） | connection/read 同源（不拆分） |
| `dss-linkis-node-execution/.../execution/impl/LinkisNodeExecutionImpl.java` | 125-127 | getState（HTTP 查询入口，不改） |
| 同上 | 73-96 | clientMap：client 按 linkisUrl 全局共享（超时配置全局影响面依据） |
| 同上 | 292-298 | cancel → UJESClient.kill（killLinkisJobQuietly 复用） |
| dss-eventchecker-appconn `.../entity/EventChecker.java` | 35/68-76 | max.receive.hours（默认 1h）/ query.frequency（最小 60s） |
| dss-eventchecker-appconn `.../execution/EventCheckerRefExecutionOperation.scala` | 146-153 | createAsyncResponseRef 绑定 maxLoopTime/queryFrequency |
| dss-standard `.../scheduler/ListenerEventBusRefExecutionScheduler.scala` | 71-75 | entrance 侧 maxLoopTime 超时兜底（对照证据） |
| dss-eventchecker-appconn `.../service/AbstractEventCheckReceiver.java`（Java） | 45-51 | initReceiverTimes：nowStartTime=进程启动时间 |
| 同上 | 81-122 | updateMsgOffset：游标推进（ON DUPLICATE KEY UPDATE） |
| 同上 | 127-154 | getOffset：读 event_status 游标 |
| 同上 | 179-203 | createExecuteType：msg.after.send 决定窗口起点 |

### 10.2 需求追溯矩阵

| 需求编号 | 需求描述 | 设计章节 | 验收 AC |
|:------:|---------|---------|:----:|
| F-P0-01 | 异常分支不再无条件返回 true | 3.3.2（改动三/四）、4.1、4.8 | AC-01 |
| F-P0-02 | 连续失败升级机制（kill+Failed+告警） | 3.3.2（escalateIfStateFetchTimeout/killLinkisJobQuietly）、3.3.1、4.2、4.5、4.7 | AC-02 |
| F-P0-03 | 未知状态名映射兜底 | 3.3.3、4.3 | AC-03 |
| F-P0-04 | 失败期间行为可观测 | 3.3.2（warn 周期日志）、7.3 | AC-01 |
| F-P1-01 | 节点级最大运行时长兜底（Timeout 启用） | 3.3.2（isNodeRunningTimeout/transitionToTimeout）、3.3.4、4.4 | AC-02 |
| F-P1-02 | connection.timeout 调优 | 2.4、3.4、IT-09 | R-04 |
| F-P1-03 | 信号节点差异化治理（isSignalNode 接线） | 2.3、3.3.2、4.4 | AC-02 |
| N-01/N-02 | 不改 entrance 侧/不重构 poller | 3.5 清单 5/8 | - |
| N-04/N-05 | 不改 eventchecker 表结构/不承诺消除 entrance 故障 | 8.1/8.2（仅文档）、9（D-01） | AC-07 |
| 6.4 | 误导性完成日志修正 | 4.8（返回值语义修复后自动消解） | - |

**验收 AC 清单**：

| AC | 验收标准 | 对应测试 |
|:--:|---------|:-------:|
| AC-01 | 短暂抖动（<阈值）：继续轮询 + warn 周期日志；恢复后下周期正常翻转 | IT-01 + 单测 |
| AC-02 | 长期不可用（≥阈值）：kill + Failed + error 告警，工作流失败结束可重跑 | IT-02/07/08 |
| AC-03 | 未知状态映射 Running + warn 原串，不抛异常，恢复后正常翻转 | IT-03 + 单测 |
| AC-04 | 正常 Succeed/Failed/Cancelled 三路径回归一致 | IT-04 |
| AC-05 | EmptyJob/EventSender 回归不受影响 | IT-05 |
| AC-06 | 零配置升级，新配置默认生效，既有配置语义不变 | IT-06 |
| AC-07 | kill 重跑：false 组游标续读安全；true 组需人工重发（写入 SOP） | IT-10 |

### 10.3 预估工时

| 任务 | 工时（人天） |
|-----|------------|
| DefaultNodeRunner 异常分支重构 + 失败升级 + kill 链路（F-P0-01/02/04） | 1.0 |
| NodeExecutionState 状态映射函数 + 别名表（F-P0-03） | 0.5 |
| 配置项定义 + P1 超时兜底 + FlowEntranceJob Timeout 分支 + isSignalNode 接线（F-P1-01/03） | 1.0 |
| 单元测试（12 个用例，含静态单例 mock 基建） | 1.0 |
| 集成与回归测试（IT-01~IT-10，含 entrance 压力注入与配置调优灰度） | 1.5 |
| kill 重跑应急 SOP 验证与文档（AC-07） | 0.5 |
| 文档与评审 | 0.3 |
| Linkis 侧容量治理协调（边界外，8.1 工单） | 0.5 |
| **合计** | **6.3** |

---

**文档版本**：v1.0
**创建日期**：2026-11-02
**设计负责人**：待定
**需求关联**：[信号节点状态查询超时永久Running修复_需求](../requirements/信号节点状态查询超时永久Running修复_需求.md)

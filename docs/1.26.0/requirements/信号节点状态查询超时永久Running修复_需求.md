# 信号节点状态查询超时永久Running修复 需求文档

| 属性 | 值 |
|------|-----|
| 需求编号 | REQ-DSS-1.26.0-FIX-001 |
| 需求名称 | 信号节点（EventReceiver）状态查询超时导致节点永久Running、工作流永久挂起修复 |
| 需求类型 | Bug修复（FIX） |
| 优先级 | P1 |
| 状态 | 待设计 |
| 版本 | dev-1.26.0（pom.xml 为 1.26.0-SNAPSHOT，Linkis 1.18.3-wds） |
| 所属模块 | dss-orchestrator/dss-workflow/dss-flow-execution-server（核心修复点）；dss-orchestrator/dss-workflow/dss-linkis-node-execution（HTTP 客户端配置）；dss-appconn/appconns/dss-eventchecker-appconn（关联分析，一般不改动） |
| 创建日期 | 2026-11-02 |

---

## 一、需求背景

### 1.1 业务场景

DSS 工作流编排支持**信号节点**（EventChecker，含信号发送方 EventSender 与信号接收方 EventReceiver），用于实现跨工作流的事件依赖：上游工作流的 EventSender 节点执行时将信号写入 Linkis 侧 `event_queue` 表，下游工作流的 EventReceiver 节点持续轮询 `event_queue` 表等待信号到达，收到信号后翻转 Success，从而驱动下游工作流继续执行。

信号节点作为 AppConn 类型任务提交到 Linkis entrance 执行，DSS flow-execution-server 侧通过 `NodeExecutionStatusPoller` 周期性调用 entrance 的状态查询接口获取节点终态。**该状态查询链路依赖 entrance 服务的可用性**：当 entrance 所在机器负载过高或发生 Full GC 时，状态查询 HTTP 请求会超时。

正常情况下，一次查询超时只应导致"本轮查询失败、下一轮重试"；但当前实现中（见第四章根因分析），**任意一次非 ErrorException 的查询异常都会使 DSS 侧停止轮询该节点且不翻转节点状态**，导致节点永久停留在 Running、整个工作流永久挂起。

### 1.2 问题来源

线上实际故障：Linkis entrance 机器负载高/Full GC，导致 DSS ER 信号节点（EventReceiver，EventSender 对应的接收方）调用 entrance 查询状态超时。EventSender 节点已发送成功（信号已写入 `event_queue` 表），但 ER 节点因 entrance 机器问题未接收到信号判定，节点状态一直 Running，不会翻转，**整个工作流永久挂起**，需人工干预才能恢复。

### 1.3 信号节点执行链路（修复前置知识）

**链路一：任务提交与入口查询（DSS flow-execution-server → Linkis entrance）**

1. DSS flow-execution-server 的 `DefaultNodeRunner.run()`（`dss-orchestrator/orchestrators/dss-workflow/dss-flow-execution-server/src/main/scala/com/webank/wedatasphere/dss/flow/execution/entrance/node/DefaultNodeRunner.scala:101-127`）将 ER 节点构建为 AppConn 任务并调用 `LinkisNodeExecutionImpl.getLinkisNodeExecution.runJob(this.linkisJob)`（line 118）提交到 Linkis entrance
2. 任务提交后，节点被放入 `nodeRunnerQueue`（`DefaultFlowExecution.scala:81`），由 `NodeExecutionStatusPoller`（`NodeExecutionStatusPoller.scala:26-41`）以固定频率轮询（`NODE_STATUS_POLLER_THREAD_SIZE` 默认 20 个线程，`NODE_STATUS_POLLER_SCHEDULER_TIME` 默认 5 秒，`FlowExecutionEntranceConfiguration.scala:26-28`；单节点查询间隔 `NODE_STATUS_INTERVAL` 默认 3000ms，line 31）
3. `NodeExecutionStatusPoller.run()`（line 30-39）每轮从队列取出 runner，调用 `runner.isLinkisJobCompleted`；**返回 false 则放回队列继续轮询，返回 true 则移出队列不再轮询**
4. `isLinkisJobCompleted`（`DefaultNodeRunner.scala:69-95`）内部通过 `LinkisNodeExecutionImpl.getLinkisNodeExecution.getState(this.linkisJob)`（line 80）经 UJES Client HTTP 调用 entrance 查询任务状态，查到终态才调 `transitionState` 翻转节点状态

**链路二：信号接收逻辑（entrance 进程内，dss-eventchecker-appconn）**

1. `EventCheckerRefExecutionOperation.submit`（`dss-appconn/appconns/dss-eventchecker-appconn/src/main/scala/com/webank/wedatasphere/dss/appconn/eventchecker/execution/EventCheckerRefExecutionOperation.scala:71-99`）在提交时同步执行一次 `ec.run()`（line 92），RECEIVE 类型节点会立即查询一次 `event_queue` 表（`EventChecker.receiveMsg`，`entity/EventChecker.java:135-161`），查到信号则置 Success，没查到则置 Running（line 157）
2. 未收到信号的任务交由 entrance 进程内 `ListenerEventBusRefExecutionScheduler`（`dss-standard/development-standard/development-process-standard-execution/src/main/scala/com/webank/wedatasphere/dss/standard/app/development/listener/scheduler/ListenerEventBusRefExecutionScheduler.scala:62-101`）按 `query.frequency`（默认 60000ms，最小 60000ms，`EventChecker.java:71-75`）周期轮询 DB；超过 `max.receive.hours`（默认 1 小时）会以超时错误结束（`ListenerEventBusRefExecutionScheduler.scala:71-75`）
3. 也就是说，**entrance 进程内的信号等待自身有 maxLoopTime 兜底**，会正常翻终态；问题出在 DSS 侧"查询 entrance 终态"这一环

### 1.4 当前问题对业务的影响

| 影响维度 | 具体表现 |
|---------|---------|
| 工作流可用性 | ER 节点永久 Running，下游所有节点永不调度，整个工作流永久挂起，只能人工 kill 后重跑 |
| 触发面广 | 所有 AppConn 类型节点（不只信号节点）共用 `DefaultNodeRunner.isLinkisJobCompleted` 轮询路径，任一节点遇到一次非 ErrorException 查询异常即触发 |
| 故障放大 | entrance 负载高/Full GC 是集群级现象，同一时间会有**多个在跑工作流的多个节点**同时进入永久 Running，形成批量挂起 |
| 数据一致性 | 信号本身不丢失（`event_queue` 表已持久化），但 kill 重跑后 `msg.after.send=true` 的旧信号不会被重新消费（见 3.2 节），存在业务语义陷阱 |
| 运维成本 | 需人工识别卡死节点、kill、评估信号重发，夜间无人值守时段损失更大 |

### 1.5 问题等级判定

| 维度 | 评估 |
|-----|------|
| 影响范围 | 所有包含 AppConn 节点（含信号节点）的工作流执行；entrance 抖动期间批量触发 |
| 紧急程度 | P1（非数据损坏类故障，但造成工作流永久挂起，影响生产调度及时性） |
| 数据风险 | 不产生脏数据；信号消息不丢失；但 kill 重跑语义需注意 `msg.after.send` 窗口 |
| 修复优先级 | P0：`isLinkisJobCompleted` 异常分支修复（根因）；P1：超时兜底与配置调优；P2：运维治理 |

---

## 二、问题现象与复现路径

### 2.1 问题速览

| 维度 | 内容 |
|-----|------|
| **问题概述** | Linkis entrance 负载高/Full GC 时，DSS 侧节点状态查询 HTTP 超时，`DefaultNodeRunner.isLinkisJobCompleted` 的 Throwable 异常分支返回 true 但不翻转节点状态，poller 将节点移出轮询队列，节点永久 Running，工作流永久挂起 |
| **紧急程度** | P1 |
| **影响范围** | 所有 AppConn 类型节点（信号节点 EventReceiver 最典型）的状态轮询路径；entrance 抖动期间批量触发 |
| **问题模块** | dss-flow-execution-server（DefaultNodeRunner/NodeExecutionStatusPoller）、dss-linkis-node-execution（UJES Client 超时配置） |
| **复现概率** | entrance 负载高/Full GC 期间高概率（一次任意非 ErrorException 异常即触发） |
| **发现环境** | 生产环境 |

### 2.2 复现步骤

**复现路径一：模拟 entrance 状态查询超时（推荐，可控复现）**

| 步骤 | 操作 | 预期结果 | 实际结果 |
|:----:|------|---------|---------|
| 1 | 创建含 EventSender（工作流A）与 EventReceiver（工作流B）的跨工作流信号依赖，EventReceiver 的 `msg.after.send` 设为 false，正常启动两个工作流 | B 的 ER 节点 Running，持续轮询 `event_queue` 表 | 正常 |
| 2 | A 的 EventSender 执行成功，信号写入 `event_queue` 表（`EventChecker.java:114` sendMsg 成功置 Success） | B 的 ER 节点在下一个 `query.frequency`（60s）内查到信号，entrance 侧任务翻 Success | 正常 |
| 3 | 在 B 的 ER 节点 Running 期间，对 entrance 施加压力使其状态查询接口超时（如 `tc` 注入网络延迟超过 readTimeout、或制造 entrance Full GC） | DSS 侧本轮查询失败，下一轮重试；entrance 恢复后查询成功，节点翻 Success | ❌ DSS 侧抛 SocketTimeoutException，`isLinkisJobCompleted` 走 `case t:Throwable => true`（`DefaultNodeRunner.scala:93-94`）返回 true 但**未调用 transitionState**；poller 将 runner 移出 `nodeRunnerQueue`（`NodeExecutionStatusPoller.scala:36-38`），不再轮询 |
| 4 | 恢复 entrance 压力，等待足够长时间 | entrance 恢复后 DSS 重新查询，节点最终翻 Success | ❌ 节点状态永久停留 Running，工作流B永久挂起；即使 entrance 侧信号任务实际已翻 Success，DSS 侧也无人再查 |

**复现路径二：模拟未知状态名（同类根因的另一触发方式）**

| 步骤 | 操作 | 预期结果 | 实际结果 |
|:----:|------|---------|---------|
| 1 | 构造 entrance 返回 DSS `NodeExecutionState` 枚举中不存在的状态串（如 Linkis 新增状态映射缺失） | 未知状态按非终态处理（视作 Running 继续轮询），或明确报错 | ❌ `NodeExecutionState.withName(...)`（`DefaultNodeRunner.scala:80`）抛 NoSuchElementException，走 `case t:Throwable => true`，同样触发永久 Running |

> 说明：路径二证明该 Bug 的触发条件是"任意一次非 ErrorException 异常"，HTTP 超时只是线上最高频的触发方式（连接 reset、DNS 抖动等同样触发）。

### 2.3 故障调用链

> 以下为**修复前**的故障调用链。行号对应修复前代码位置（修复后行号可能变化，详见附录 10.1 索引表）。

```
NodeExecutionStatusPoller.run()  (NodeExecutionStatusPoller.scala:30-39, 每5秒调度)
  └── runner.isLinkisJobCompleted  (DefaultNodeRunner.scala:69-95)
        ├── interval 检查: 距上次查询 < 3000ms 直接 return false  (line 71-75)
        ├── NodeExecutionState.isCompleted(getStatus)? → 已终态 return true  (line 79)
        └── LinkisNodeExecutionImpl.getLinkisNodeExecution.getState(this.linkisJob)  (line 80)
              └── UJES Client HTTP GET entrance 状态接口
                    (LinkisNodeExecutionImpl.java:125-127)
                    └── readTimeout = wds.linkis.flow.connection.timeout 默认 300000ms
                        (LinkisJobExecutionConfiguration.java:57, LinkisUjesClientUtils.java:44/59)
                          ↓ entrance 负载高/Full GC → 无响应
                抛 SocketTimeoutException / HttpClientException（非 ErrorException）
        ↓ Utils.tryCatch 捕获
  case t:Throwable => logger.error(...); true   (DefaultNodeRunner.scala:93-94)  ← Bug 点
        ❌ 返回 true 但节点 status 仍为 Running，未调用 transitionState
  ↓
NodeExecutionStatusPoller: isLinkisJobCompleted == true
  → 不再 nodeRunnerQueue.put(runner)  (NodeExecutionStatusPoller.scala:34-35)
  → runner 移出轮询队列，仅打日志 "status is completed"  (line 36-38)  ← 误导性日志（实际未完成）
        ↓
节点永久 Running，无任何组件再查询该节点状态 → 工作流永久挂起
```

**关键证据链**：

| 环节 | 代码位置 | 事实 |
|-----|---------|------|
| 异常分支返回 true | `DefaultNodeRunner.scala:93-94` | `case t:Throwable => logger.error(...); true`，无 transitionState |
| ErrorException 分支返回 false | `DefaultNodeRunner.scala:91-92` | `case e:ErrorException => logger.warn(...); false`，继续轮询（正确示范） |
| true 即停止轮询 | `NodeExecutionStatusPoller.scala:34-38` | `if(!runner.isLinkisJobCompleted) put(runner) else 仅打日志` |
| 超时来源 | `LinkisJobExecutionConfiguration.java:57` | `LINKIS_CONNECTION_TIMEOUT = CommonVars.apply("wds.linkis.flow.connection.timeout", 300000)`，同时用作 connectionTimeout 与 readTimeout（`LinkisUjesClientUtils.java:40/44/57/59`） |
| ErrorException 语义 | Linkis SDK | ErrorException 从**响应 JSON** 解析而来；HTTP 超时**根本没有响应体**，不可能抛 ErrorException，必走 Throwable 分支 |

### 2.4 影响范围

- **直接影响**：
  - 信号节点（EventReceiver/EventSender）在 entrance 抖动期间提交或等待时，节点永久 Running
  - 所有走 AppConn 执行链路的节点（Scriptis、Visualis、Qualitis 等第三方 AppConn 任务）同样共用该轮询路径，均受影响
- **间接影响**：
  - 工作流下游节点永不调度，跨工作流信号依赖链整体停滞
  - 夜间调度批次整体延迟，需人工逐个工作流 kill + 重跑
- **不受影响**：
  - EmptyJob 类型节点（`DefaultNodeRunner.scala:112-116`，提交时直接置 Succeed，不走轮询）
  - entrance 正常期间的节点状态查询与翻转

---

## 三、根因分析

### 3.1 核心问题：isLinkisJobCompleted 异常分支"返回 true 但不翻转状态"（5Why）

| 层级 | 问题 | 答案 |
|:----:|------|------|
| Why 1 | 为什么 ER 节点会永久 Running？ | DSS 侧 `NodeExecutionStatusPoller` 停止了对该节点的轮询（runner 已移出 `nodeRunnerQueue`），但节点状态从未被 `transitionState` 翻转为终态 |
| Why 2 | 为什么停止轮询却没翻终态？ | `isLinkisJobCompleted` 的 `case t:Throwable` 分支（`DefaultNodeRunner.scala:93-94`）返回了 true（语义为"已完成"），poller 据此移出队列；但该分支内**只打日志、不调 transitionState**，节点 status 仍是 Running |
| Why 3 | 为什么状态查询会抛 Throwable？ | entrance 负载高/Full GC 时状态查询 HTTP 请求超过 readTimeout（默认 300s，`wds.linkis.flow.connection.timeout`），UJES Client 抛 SocketTimeoutException；超时没有响应体，不可能解析为 ErrorException，必走 Throwable 分支 |
| Why 4 | 为什么异常处理写成"返回 true"？ | 推测原始意图是"查询抛未知异常时避免无限重试/避免阻塞 poller 线程"，用 true 让 poller 放弃该 runner；但 true 的语义在 poller 侧是"任务已完成"，与节点状态机脱节——返回 true 却没有配套的状态翻转与失败告警，形成语义裂缝 |
| Why 5（根本原因） | 为什么缺少对"状态获取持续失败"的治理设计？ | **异常处理策略缺失**：把"查询基础设施异常"与"任务本身结束"混为一谈。正确的语义应当是——查询异常属于**暂时性故障**，默认应继续轮询；只有持续失败超过阈值才升级为任务失败（kill + 置 Failed + 告警）。同时节点执行缺少超时兜底机制（`NodeExecutionState.Timeout` 枚举已定义但全工程无人使用），导致任何"停止轮询"路径都没有最终状态收敛的保证 |

### 3.2 次要问题（放大器）

**次要问题 1：HTTP readTimeout 过长（300 秒）**

- `wds.linkis.flow.connection.timeout` 默认 300000ms（`LinkisJobExecutionConfiguration.java:57`），且**同时用作 connectionTimeout 与 readTimeout**（`LinkisUjesClientUtils.java:40/44/57/59`）
- 状态查询是轻量接口，正常毫秒级返回；entrance Full GC 期间每次轮询要挂满 5 分钟才失败，期间占用 poller 线程（默认 20 个，`NODE_STATUS_POLLER_THREAD_SIZE`），极端情况下 20 个线程全部挂在超时查询上，拖慢所有工作流的状态轮询
- `getClientConfig1_X`（Linkis 1.x 路径，line 50-70）虽配置了 retryHandler，但 `getClientConfig`（0.x 兼容路径，line 37-48）`retryEnabled(false)`；且重试也只针对连接类异常，不解决 readTimeout 挂线程问题

**次要问题 2：DSS 侧节点无超时兜底**

- `NodeExecutionState.Timeout` 枚举值已定义（`node/NodeExecutionState.scala:24`），且 `isCompleted` 会将其视为终态（line 32-35），但**全工程没有任何代码路径将节点置为 Timeout**（grep 证实仅枚举定义处出现）
- 即：无论 entrance 侧任务是否已因 `max.receive.hours` 超时结束（`ListenerEventBusRefExecutionScheduler.scala:71-75` 有 maxLoopTime 兜底），DSS 侧只要查询环节断掉，就永远无法收敛

**次要问题 3：信号节点识别工具已定义但未接线**

- `FlowExecutionUtils.isSignalNode`（`utils/FlowExecutionUtils.scala:74-76`，读取 `wds.dss.flow.signal.nodes` 默认 `linkis.appconn.eventchecker.eventreceiver`，配置定义于 `FlowExecutionEntranceConfiguration.scala:55`）已提供信号节点识别能力，但**无任何调用方**（grep 证实仅 scaladoc 生成物引用）
- 导致 DSS 侧无法对信号节点做差异化治理（如对齐 entrance 侧 max.receive.hours 的超时上限）

### 3.3 kill 重跑的数据语义分析（应急方案前置评估）

对已卡死的 ER 节点，kill 后重跑是否安全取决于信号消费窗口（`AbstractEventCheckReceiver.java`，注意该文件为 **Java** 实现，位于 `dss-appconn/appconns/dss-eventchecker-appconn/src/main/java/com/webank/wedatasphere/dss/appconn/eventchecker/service/AbstractEventCheckReceiver.java`）：

| 机制 | 代码位置 | 语义 |
|-----|---------|------|
| 消费游标 | `getOffset`（line 127-154）：读 `event_status` 表 `msg_id` | 重跑后从上次消费位置继续，不重复消费 |
| 游标推进 | `updateMsgOffset`（line 81-122）：`INSERT ... ON DUPLICATE KEY UPDATE`，仅当 `msg_id` 未被其他消费者推进时才更新 | 信号消息不丢、不重（在窗口内） |
| 查询窗口起点 | `createExecuteType`（line 179-203）：`msg.after.send=true` 时窗口起点为 `nowStartTime`（line 50，receiver 进程启动时间） | **kill 重跑后旧信号不会被消费**——新实例的 `nowStartTime` 晚于旧信号写入时间，需人工重发信号 |

结论：kill 重跑对 `msg.after.send=false` 的场景安全（全时间窗口查询）；对 `msg.after.send=true` 的场景，旧信号落在窗口外，需重发信号才能让 ER 节点翻 Success。

### 3.4 根因确认汇总

- **直接原因**：`DefaultNodeRunner.isLinkisJobCompleted` 的 `case t:Throwable` 分支（line 93-94）在 entrance 状态查询超时等异常时返回 true 但不翻转节点状态，poller 据此停止轮询，节点永久 Running
- **根本原因**：异常处理策略缺失——"查询暂时性故障"被误当作"任务已完成"处理；且缺少持续失败升级机制与节点超时兜底，任何轮询中断都无法收敛到终态
- **相关代码**：详见附录 10.1 关键代码位置索引

---

## 四、期望行为

### 4.1 修复后行为总览

| 场景 | 修复前行为 | 期望行为（修复后） |
|-----|----------|------------------|
| entrance 短暂抖动（Full GC 后恢复，持续 < 阈值） | 一次查询超时 → 节点永久 Running | 查询异常按"暂时性故障"处理，继续轮询；entrance 恢复后查询成功，节点正常翻 Success/Failed |
| entrance 长期不可用（持续 > 阈值，默认 30 分钟） | 节点永久 Running，工作流永久挂起 | 连续状态获取失败超过阈值 → kill Linkis 任务 + 节点置 Failed + 输出告警日志，工作流正常结束（可重跑） |
| entrance 返回 DSS 未知的状态串 | `withName` 抛 NoSuchElementException → 走 Throwable 分支 → 永久 Running | 未知状态映射为 Running（非终态）继续轮询，并输出告警日志；不抛异常 |
| 正常节点状态翻转（entrance 健康） | 查到终态 → transitionState → true | 行为完全不变（兼容性要求） |
| 节点运行时长超过上限（P1） | 无任何兜底，理论可无限 Running | 节点级最大运行时长兜底，超限置 Timeout/Failed 并告警（信号节点可对齐 max.receive.hours） |
| 状态查询 HTTP 超时配置（P1） | readTimeout=300s，Full GC 期间每次轮询挂 5 分钟 | readTimeout 调优至 60s（`wds.linkis.flow.connection.timeout=60000`），快速失败快速重试 |

### 4.2 向后兼容性要求

- **正常路径不变**：entrance 健康时的状态查询与翻转逻辑（`DefaultNodeRunner.scala:79-89`）保持不变，正常节点的翻转行为与修复前完全一致
- **现有配置项语义不变**：`wds.linkis.flow.connection.timeout`、`wds.dds.flow.node.status.poller.*` 等既有配置的含义与默认值不变（60000 为部署侧建议值，非代码默认值变更，是否调整默认值由设计阶段决定）
- **新增配置可选**：新增的失败阈值/超时兜底配置均给出合理默认值，不配置也生效，不强制修改既有部署
- **ErrorException 分支语义不变**：`case e:ErrorException => false`（line 91-92）继续轮询的行为保留
- **接口签名不变**：`NodeRunner.isLinkisJobCompleted` 等接口签名不变更（实现内部增强），`NodeExecutionStatusPoller` 调用方无需改造

---

## 五、功能性需求

### 5.1 核心功能 P0

| 编号 | 功能项 | 描述 |
|:----:|-------|------|
| F-P0-01 | 异常分支不再无条件返回 true | `DefaultNodeRunner.isLinkisJobCompleted` 的 `case t:Throwable` 分支（`DefaultNodeRunner.scala:93-94`）重构：查询异常时记录失败时间戳与连续失败时长，默认返回 false 继续轮询；不再出现"返回 true 但节点状态为 Running"的语义裂缝 |
| F-P0-02 | 连续失败升级机制 | 引入"连续状态获取失败时长"阈值，建议新配置 `wds.dss.flow.node.state.fetch.fail.timeout`（默认 30 分钟，单位毫秒）：持续失败 < 阈值 → 继续轮询并周期性 warn 日志；≥ 阈值 → kill Linkis 任务（复用 `DefaultNodeRunner.cancel` 的 kill 链路，line 130-137）+ 节点置 Failed + error 级告警日志（含节点名、flowExecId、失败时长、最后异常摘要），返回 true 结束轮询 |
| F-P0-03 | 未知状态名映射兜底 | `NodeExecutionState.withName(...)`（line 80）遇未知状态串抛 NoSuchElementException 的风险消除：增加状态名映射兜底（tryCatch 或映射函数），未知状态映射为 Running（非终态）继续轮询，并输出 warn 告警日志（含原始状态串），不中断轮询 |
| F-P0-04 | 失败期间行为可观测 | 连续失败期间每轮（或按周期）输出 warn 日志：节点名、已连续失败时长/次数、阈值剩余时间，便于运维在告警触发前介入 |

### 5.2 增强功能 P1

| 编号 | 功能项 | 描述 |
|:----:|-------|------|
| F-P1-01 | 节点级最大运行时长兜底 | 启用 `NodeExecutionState.Timeout`（`NodeExecutionState.scala:24`，当前全工程未使用）：节点 Running 时长超过上限（新配置，建议 `wds.dss.flow.node.max.running.time`，默认值设计阶段定）时置 Timeout/Failed 并告警；信号节点可复用 `FlowExecutionUtils.isSignalNode`（`FlowExecutionUtils.scala:74-76`）识别后对齐 `max.receive.hours` 语义 |
| F-P1-02 | 状态查询超时配置调优 | 部署侧将 `wds.linkis.flow.connection.timeout` 调整为 60000（60s）：状态查询为轻接口，快速失败避免 poller 线程（默认 20 个）长时间挂起；是否同时拆分 connectionTimeout/readTimeout 为两个配置由设计阶段决定 |
| F-P1-03 | 信号节点差异化治理（可选） | 将已定义未使用的 `FlowExecutionUtils.isSignalNode` 接入超时兜底逻辑，信号节点的最大运行时长与 entrance 侧 `max.receive.hours`（`EventChecker.java:35,68-70`）联动，避免 DSS 侧先于 entrance 侧误杀等待信号的节点 |

### 5.3 功能不包含

| 编号 | 不包含项 | 说明 |
|:----:|---------|------|
| N-01 | 不修改 entrance 侧信号轮询逻辑 | `ListenerEventBusRefExecutionScheduler`（有 maxLoopTime 兜底）与 `EventCheckerRefExecutionOperation` 本身工作正常，不改动 |
| N-02 | 不重构 NodeExecutionStatusPoller 的队列模型 | 现有"take-判断-put回"模型保留，仅通过 isLinkisJobCompleted 返回值语义修复达成目的，不引入优先级队列等新机制 |
| N-03 | 不引入外部告警系统对接 | 告警以 error/warn 日志形式输出，对接公司告警平台由运维侧日志采集完成（非代码改动） |
| N-04 | 不修改 event_queue/event_status 表结构与消费逻辑 | 信号持久化与游标机制（`AbstractEventCheckReceiver`）不动；kill 重跑语义陷阱通过文档与运维 SOP 治理 |
| N-05 | 不承诺消除 entrance 负载高/Full GC 本身 | entrance 容量治理（隔离/扩容/JVM 调优）属 Linkis 侧运维范畴，本需求只保证 entrance 抖动时 DSS 侧行为正确收敛 |

### 5.4 修复方案候选（由设计阶段决定）

> 以下为候选方案方向，需求阶段仅列出，不在本文档做技术选型。

| 候选 | 方向 | 优点 | 缺点 |
|:----:|------|------|------|
| 候选A（P0 主线） | Throwable 分支改返回 false + DefaultNodeRunner 内记录 `lastStateFetchFailTime`，超阈值才 kill+Failed+true | 改动集中在单文件，状态收敛逻辑内聚在 runner 自身 | 需在 runner 上新增失败计时字段，注意并发可见性（poller 多线程可能同时取同一 runner？实际单队列 take 不会） |
| 候选B（P0 备选） | 抛弃 tryCatch 返回值模型，isLinkisJobCompleted 改为显式三态（Completed/Continue/Fatal）返回枚举 | 语义最清晰，杜绝 true/false 歧义 | 改动 NodeRunner 接口签名，影响面扩大到接口与所有实现 |
| 候选C（withName 兜底） | 独立的状态映射函数（含 unknown→Running + warn），替换 withName 直调 | 单点修复，可单测覆盖 | 需维护 DSS↔Linkis 状态映射表，Linkis 新增状态需跟进 |
| 候选D（P1 超时兜底） | 在 isLinkisJobCompleted 内基于 `startTime`（line 48/111/149-151 已有）判断节点级最大运行时长 | 复用现有 startTime 字段，改动小 | 超长合法任务（如多小时 Scriptis）需配置豁免，默认值需谨慎 |
| 候选E（配置调优） | 仅调 `wds.linkis.flow.connection.timeout=60000` 不改代码 | 零代码风险，立即可做 | 只缩短单次挂起时长，不解决"返回true不翻转"根因，必须与候选A/C 组合 |

---

## 六、非功能性需求

### 6.1 兼容性

| 影响项 | 要求 |
|-------|------|
| JDK/Scala 版本 | 保持 JDK 8 / Scala 2.11.12 兼容，不使用高版本 API |
| Linkis 依赖 | 不调整 Linkis `1.18.3-wds` 依赖版本；UJES Client 行为不修改，仅调整配置值 |
| 现有部署 | 不新增数据库表、不修改表结构；新增配置项有默认值，不强制修改 `assembly/config/config.sh` |
| 既有配置语义 | `wds.linkis.flow.connection.timeout` 等既有配置含义不变；60000 为部署建议值 |
| 正常执行链路 | entrance 健康时节点提交、轮询、翻转行为与修复前一致 |

### 6.2 安全性

| 安全项 | 要求 |
|:-------|------|
| kill 权限 | F-P0-02 的 kill 复用 `DefaultNodeRunner.cancel` 现有链路（`LinkisNodeExecutionImpl.cancel`），不新增越权操作 |
| 告警内容 | 日志不输出 token、代理用户密码等敏感信息，异常摘要可含堆栈首行 |
| 配置安全 | 新增配置项不含敏感信息，无需加密 |

### 6.3 性能与资源占用

| 指标 | 修复前 | 修复后目标 |
|-----|-------|----------|
| entrance Full GC 期间单次状态查询挂起 | 最长 300s（readTimeout 默认值） | 最长 60s（部署侧调优后） |
| 连续失败下的轮询收敛时间 | 永不收敛（永久 Running） | ≤ 阈值（默认 30 分钟）内收敛到 Failed + 告警 |
| entrance 短暂抖动（<阈值）恢复后的翻转延迟 | 不翻转（Bug） | 恢复后下一个轮询周期（秒级）内正常翻转 |
| poller 线程占用 | Full GC 期间 20 个线程可能全部长挂 | 快速失败快速重试，线程占用时长大幅缩短 |

### 6.4 可观测性

| 可观测项 | 要求 |
|---------|------|
| 连续失败 warn 日志 | 含节点名、flowExecId、连续失败时长、阈值剩余时间，周期输出 |
| 升级失败 error 日志 | 含节点名、flowExecId、失败时长、最后异常摘要、kill 结果，error 级别（供日志告警采集） |
| 未知状态 warn 日志 | 含节点名、原始状态串，映射为 Running 继续轮询 |
| 现有日志修正 | 修复后 poller 的 "status is completed" 日志（`NodeExecutionStatusPoller.scala:37`）语义应与实际终态一致，不再对未完成节点打印误导性完成日志 |

---

## 七、影响面分析

### 7.1 受影响调用方/模块梳理

| 调用方/模块 | 涉及功能 | 本次影响 |
|------------|---------|---------|
| `NodeExecutionStatusPoller` | 所有 AppConn 节点状态轮询 | ✅ 通过 isLinkisJobCompleted 返回值语义修复间接生效，poller 本身可不改动 |
| `DefaultNodeRunner` | 节点提交/状态查询/翻转/取消 | ✅ 核心修复对象（异常分支、失败计时、状态映射兜底） |
| `LinkisNodeExecutionImpl.getState` | UJES Client 状态查询（`LinkisNodeExecutionImpl.java:125-127`） | ❌ 不修改；超时由配置调优解决 |
| `LinkisUjesClientUtils` | Client 配置构建（`LinkisUjesClientUtils.java:37-70`） | ⚠️ 是否拆分 connection/read 超时配置由设计阶段决定 |
| 信号节点（EventReceiver/EventSender） | 跨工作流事件依赖 | ✅ 受益方；P1 可选差异化超时治理涉及 `FlowExecutionUtils.isSignalNode` 接线 |
| `NodeExecutionState.Timeout` | 枚举已定义未使用（`NodeExecutionState.scala:24`） | ⚠️ P1 启用，注意启用后 isCompleted 语义已就绪（line 32-35 视为终态） |
| 正常节点执行（Scriptis/Visualis/Qualitis 等 AppConn 任务） | 提交与状态翻转 | ✅ 同样受益（同一轮询路径）；需回归验证不破坏正常翻转 |

### 7.2 配置变更

| 变更项 | 说明 | 归属 |
|-------|------|------|
| `wds.dss.flow.node.state.fetch.fail.timeout`（新增） | 连续状态获取失败升级阈值，建议默认 1800000（30 分钟） | DSS 代码（新配置，默认值设计阶段定稿） |
| `wds.dss.flow.node.max.running.time`（新增，P1） | 节点级最大运行时长兜底，默认值设计阶段定 | DSS 代码 |
| `wds.linkis.flow.connection.timeout`（既有） | 部署侧建议从 300000 调整为 60000；是否调整代码默认值由设计阶段决定 | 部署配置（`conf/` 下 properties）/可选代码默认值 |
| `wds.dss.flow.signal.nodes`（既有） | 已定义（默认 `linkis.appconn.eventchecker.eventreceiver`），P1 接线使用，含义不变 | DSS 代码（接线调用方） |

### 7.3 代码影响范围

| 模块 | 文件 | 改造类型 |
|-----|------|---------|
| dss-flow-execution-server | `node/DefaultNodeRunner.scala` | 核心修改：重构 `isLinkisJobCompleted` 异常分支（line 69-95）；新增连续失败计时字段与升级逻辑；withName 状态映射兜底 |
| dss-flow-execution-server | `conf/FlowExecutionEntranceConfiguration.scala` | 新增失败阈值/最大运行时长配置项定义 |
| dss-flow-execution-server | `node/NodeExecutionState.scala` | P1：启用 Timeout（枚举已就绪，可能需补充辅助方法） |
| dss-flow-execution-server | `utils/FlowExecutionUtils.scala` | P1 可选：isSignalNode 接入调用方 |
| dss-flow-execution-server | `execution/NodeExecutionStatusPoller.scala` | 一般不改动（通过返回值语义修复间接生效）；可能修正误导性完成日志 |
| dss-linkis-node-execution | `utils/LinkisUjesClientUtils.java` | 可选：拆分连接/读超时配置（设计阶段决定） |
| dss-eventchecker-appconn | 不改动 | 仅关联分析（kill 重跑语义、max.receive.hours 对齐参考） |

---

## 八、验收标准（三段式）

### 8.1 entrance 短暂抖动恢复后节点正常翻转（P0 核心）

| 编号 | 验证阶段 | 验收条件 |
|:----:|:--------:|---------|
| AC-01 | 前置条件 | 工作流B含 EventReceiver 节点（`msg.after.send=false`），已提交运行、状态 Running；工作流A的 EventSender 已成功写入信号到 `event_queue` 表 |
| AC-01 | 操作步骤 | 1) 对 entrance 注入压力使状态查询超时（如网络延迟 > readTimeout 或制造 Full GC），持续时长小于失败阈值（如 5 分钟 < 默认 30 分钟）；2) 观察期间 DSS 日志；3) 恢复 entrance；4) 等待 1-2 个轮询周期 |
| AC-01 | 预期结果 | 1) 抖动期间节点保持 Running 且仍在 `nodeRunnerQueue` 中被轮询（连续失败 warn 日志周期输出，含失败时长）；2) entrance 恢复后，ER 节点在下一个轮询周期内查询到 entrance 侧终态（Success），节点翻 Succeed，下游节点正常调度；3) 不出现"返回 true 但状态 Running"的日志组合 |

### 8.2 entrance 长期不可用时节点收敛到 Failed 并告警（P0 核心）

| 编号 | 验证阶段 | 验收条件 |
|:----:|:--------:|---------|
| AC-02 | 前置条件 | 同 AC-01，但 entrance 压力持续时长将超过失败阈值（默认 30 分钟，测试可调小 `wds.dss.flow.node.state.fetch.fail.timeout` 加速验证） |
| AC-02 | 操作步骤 | 1) 持续保持 entrance 状态查询超时直至超过阈值；2) 观察节点状态、DSS 日志与 Linkis 任务状态 |
| AC-02 | 预期结果 | 1) 连续失败时长达到阈值的时刻，节点被 kill（Linkis 侧任务转 Cancelled/Killed）且节点状态翻 Failed（终态）；2) error 级告警日志输出（含节点名、flowExecId、失败时长、异常摘要）；3) 工作流正常结束（Failed，可重跑），不再永久挂起；4) 该 runner 移出轮询队列后不再被重复处理 |

### 8.3 未知状态名不中断轮询（P0）

| 编号 | 验证阶段 | 验收条件 |
|:----:|:--------:|---------|
| AC-03 | 前置条件 | 构造/模拟 entrance 返回 DSS `NodeExecutionState` 不存在的状态串（可通过测试桩或临时改造返回值） |
| AC-03 | 操作步骤 | 1) 使 getState 返回未知状态串；2) 观察后续轮询与日志；3) 恢复正常状态串 |
| AC-03 | 预期结果 | 1) 未知状态被映射为 Running（非终态），节点继续轮询，不抛 NoSuchElementException 终断；2) warn 日志输出原始状态串；3) 恢复正常状态后节点按真实终态翻转 |

### 8.4 正常节点状态翻转回归（P0）

| 编号 | 验证阶段 | 验收条件 |
|:----:|:--------:|---------|
| AC-04 | 前置条件 | entrance 健康，创建含普通节点（如 Scriptis 脚本节点）的工作流并执行 |
| AC-04 | 操作步骤 | 1) 正常执行成功场景；2) 执行失败场景（如脚本抛错）；3) 人工取消场景 |
| AC-04 | 预期结果 | 1) 成功场景节点翻 Succeed，行为与修复前一致；2) 失败场景节点翻 Failed；3) 取消场景节点翻 Cancelled；4) 三种终态下 runner 均正常移出轮询队列，工作流正常推进/结束 |

### 8.5 EmptyJob 与信号发送节点回归（P0）

| 编号 | 验证阶段 | 验收条件 |
|:----:|:--------:|---------|
| AC-05 | 前置条件 | 含空节点（EmptyJob）与 EventSender 节点的工作流 |
| AC-05 | 操作步骤 | 执行工作流，观察空节点与 EventSender 的状态翻转 |
| AC-05 | 预期结果 | 1) EmptyJob 节点提交时直接翻 Succeed（`DefaultNodeRunner.scala:112-116` 路径不变）；2) EventSender 发送成功后正常翻 Succeed；3) 两类节点均不进入异常分支 |

### 8.6 既有配置兼容性验证（P1）

| 编号 | 验证阶段 | 验收条件 |
|:----:|:--------:|---------|
| AC-06 | 前置条件 | 使用修复前相同的部署配置（不新增任何新配置项）启动服务 |
| AC-06 | 操作步骤 | 执行含信号节点的工作流全流程 |
| AC-06 | 预期结果 | 1) 新配置项按默认值生效，不配置不报错；2) `wds.linkis.flow.connection.timeout` 不配置时语义与修复前一致；3) 全流程行为正常 |

### 8.7 kill 重跑应急方案验证（P2，运维配套）

| 编号 | 验证阶段 | 验收条件 |
|:----:|:--------:|---------|
| AC-07 | 前置条件 | 存在一个因本 Bug 已卡死 Running 的 ER 节点（或用修复前版本构造），`msg.after.send` 分别取 false/true 两组场景 |
| AC-07 | 操作步骤 | 1) 对卡死节点执行 kill；2) 重新运行工作流B；3) 观察信号消费情况 |
| AC-07 | 预期结果 | 1) `msg.after.send=false`：重跑后 ER 节点按 `event_status` 游标（`AbstractEventCheckReceiver.java:127-154`）继续消费，旧信号可被接收，节点翻 Success；2) `msg.after.send=true`：窗口起点为新实例启动时间（line 50/188/194），旧信号不被消费，需人工重发信号后 ER 节点才翻 Success（该行为写入运维 SOP） |

---

## 九、风险与依赖

| 编号 | 风险/依赖 | 等级 | 应对措施 |
|:----:|---------|:----:|---------|
| R-01 | 阈值（默认 30 分钟）与业务容忍度不匹配：entrance 计划内长时间维护（如 >30 分钟）会导致在跑工作流批量翻 Failed | 中 | 阈值可配置；运维在 entrance 计划维护前主动暂停/提前告警工作流；Failed 可重跑，损失可控 |
| R-02 | kill 升级时机与 entrance 侧 maxLoopTime（`max.receive.hours` 默认 1 小时）竞态：DSS 先 kill 时 entrance 侧任务实际仍在合法等待 | 低 | 信号节点 P1 可选差异化治理（F-P1-03）对齐 max.receive.hours；kill 前 error 日志留痕可追溯 |
| R-03 | 连续失败计时的状态存储在 runner 实例字段，服务重启后计时丢失（重启后重新计数） | 低 | 重启本身会中断工作流执行（既有行为），计时重置可接受；设计阶段可评估是否持久化 |
| R-04 | readTimeout 调小（300s→60s）后，极端慢查询（大工作流状态聚合）可能被误判超时 | 中 | 状态查询为轻接口，60s 余量充足；灰度环境先验证；阈值仍可配置回退 |
| R-05 | 未知状态映射为 Running 可能掩盖 Linkis 新状态（如新失败态）导致延迟发现 | 低 | warn 日志持续输出原始状态串；映射表在设计文档中明示，Linkis 升级时检查 |
| D-01 | 依赖 Linkis entrance 侧状态查询接口稳定性；entrance 容量治理（隔离/扩容/JVM 调优）属 Linkis 运维范畴，DSS 无法单方面消除诱因 | 高 | DSS 侧保证行为正确收敛（本需求）；容量治理以运维工单/Linkis 侧 issue 跟进，不承诺消除 |
| D-02 | kill 重跑的 `msg.after.send=true` 信号重发依赖业务方配合（人工重发） | 中 | 写入运维 SOP 与用户文档，明确两种场景的处置差异 |

---

## 十、附录

### 10.1 关键代码位置索引

| 文件 | 行号 | 说明 |
|------|:----:|------|
| `dss-orchestrator/orchestrators/dss-workflow/dss-flow-execution-server/src/main/scala/com/webank/wedatasphere/dss/flow/execution/entrance/node/DefaultNodeRunner.scala` | 69-95 | `isLinkisJobCompleted` 全量实现（核心修复对象） |
| 同上 | 73-75 | 单节点查询间隔检查（`NODE_STATUS_INTERVAL` 默认 3000ms） |
| 同上 | 80 | `NodeExecutionState.withName(...)` 直调（未知状态抛 NoSuchElementException 的风险点） |
| 同上 | 84 | 正常终态翻转 `transitionState(toState)`（true 路径唯一合法翻转点） |
| 同上 | 91-92 | `case e:ErrorException => false`（正确示范：Linkis 业务异常继续轮询） |
| 同上 | 93-94 | **Bug 点**：`case t:Throwable => true`（返回 true 但不 transitionState） |
| 同上 | 101-127 | `run()`：节点提交入口（line 118 提交 Linkis 任务） |
| 同上 | 112-116 | EmptyJob 直接置 Succeed（不走轮询，不受影响路径） |
| 同上 | 130-137 | `cancel()`：kill 链路（F-P0-02 升级 kill 可复用） |
| 同上 | 48/111/149-151 | `startTime` 字段与赋值（P1 节点级超时兜底可复用） |
| `dss-orchestrator/.../execution/NodeExecutionStatusPoller.scala` | 26-41 | poller 实现（take-判断-put回 模型） |
| 同上 | 34-35 | `isLinkisJobCompleted == false` 才 put 回队列 |
| 同上 | 36-38 | true 分支仅打日志（含误导性 "status is completed"） |
| `dss-orchestrator/.../execution/DefaultFlowExecution.scala` | 38 | `nodeRunnerQueue` 定义 |
| 同上 | 81-86 | 节点入队与 poller 调度（`scheduleAtFixedRate`，默认 5 秒） |
| `dss-orchestrator/.../conf/FlowExecutionEntranceConfiguration.scala` | 26-28 | poller 线程数（默认 20）与调度周期（默认 5s） |
| 同上 | 31 | `NODE_STATUS_INTERVAL`（`wds.dds.flow.node.status.poller.interval.time`，默认 3000ms） |
| 同上 | 55 | `SIGNAL_NODES`（`wds.dss.flow.signal.nodes`，默认 `linkis.appconn.eventchecker.eventreceiver`） |
| `dss-orchestrator/.../node/NodeExecutionState.scala` | 24 | `Timeout` 枚举值（已定义全工程未使用） |
| 同上 | 32-35 | `isCompleted`（Timeout 属终态，启用即就绪） |
| `dss-orchestrator/.../utils/FlowExecutionUtils.scala` | 74-76 | `isSignalNode`（已定义无调用方） |
| `dss-orchestrator/orchestrators/dss-workflow/dss-linkis-node-execution/src/main/java/com/webank/wedatasphere/dss/linkis/node/execution/conf/LinkisJobExecutionConfiguration.java` | 57 | `LINKIS_CONNECTION_TIMEOUT`（`wds.linkis.flow.connection.timeout`，默认 300000ms） |
| `dss-orchestrator/.../utils/LinkisUjesClientUtils.java` | 40/44 | 0.x 路径：connectionTimeout/readTimeout 同源配置，`retryEnabled(false)` |
| 同上 | 57/59 | 1.x 路径：connectionTimeout/readTimeout 同源配置 |
| `dss-orchestrator/.../execution/impl/LinkisNodeExecutionImpl.java` | 125-127 | `getState`：UJES Client HTTP 查询入口 |
| 同上 | 103-122 | `runJob`：任务提交入口 |
| `dss-appconn/appconns/dss-eventchecker-appconn/src/main/scala/com/webank/wedatasphere/dss/appconn/eventchecker/execution/EventCheckerRefExecutionOperation.scala` | 71-99 | `submit`：提交时同步执行一次信号查询（line 92 `ec.run()`） |
| 同上 | 101-121 | `state`：轮询时查询信号（RECEIVE 类型） |
| 同上 | 146-153 | `createAsyncResponseRef`：绑定 `maxWaitTime`（max.receive.hours）与 `queryFrequency` |
| `dss-standard/development-standard/development-process-standard-execution/src/main/scala/com/webank/wedatasphere/dss/standard/app/development/listener/scheduler/ListenerEventBusRefExecutionScheduler.scala` | 62-101 | entrance 进程内信号轮询调度器 |
| 同上 | 71-75 | maxLoopTime 超时兜底（entrance 侧有、DSS 侧无的对照证据） |
| 同上 | 76-89 | queryFrequency 周期轮询与终态处理 |
| `dss-appconn/appconns/dss-eventchecker-appconn/src/main/java/com/webank/wedatasphere/dss/appconn/eventchecker/entity/EventChecker.java` | 35/37 | `max.receive.hours` / `query.frequency` 配置键定义 |
| 同上 | 64-76 | 构造器：maxWaitTime（小时→ms）与 queryFrequency（最小 60000ms）解析 |
| 同上 | 78-133 | `run()`：SEND 置 Success / RECEIVE 未查到置 Running（line 157） |
| `dss-appconn/.../service/AbstractEventCheckReceiver.java`（Java 文件） | 45-51 | `initReceiverTimes`：`nowStartTime` 为进程启动时间 |
| 同上 | 81-122 | `updateMsgOffset`：游标推进（ON DUPLICATE KEY UPDATE 防覆盖） |
| 同上 | 127-154 | `getOffset`：读取 `event_status` 游标 |
| 同上 | 179-203 | `createExecuteType`：`msg.after.send` 决定查询窗口起点（true→nowStartTime） |

> 注：`AbstractEventCheckReceiver` 与 `AbstractEventCheck` 为 **Java** 实现（`src/main/java/.../service/` 目录），非 Scala；`EventChecker` 实体同为 Java（`entity/EventChecker.java`）。

### 10.2 相关接口/模块

| 接口/模块 | 说明 | 本次是否修改 |
|---------|------|:----------:|
| `NodeRunner.isLinkisJobCompleted` | 节点状态轮询判断（`DefaultNodeRunner.scala:69-95`） | ✅ 修复对象（实现内部增强） |
| `NodeExecutionStatusPoller.run` | 轮询调度（`NodeExecutionStatusPoller.scala:30-39`） | ⚠️ 一般不改动（间接生效），可能修正日志 |
| `LinkisNodeExecution.getState` | entrance 状态查询（`LinkisNodeExecutionImpl.java:125-127`） | ❌ 不修改 |
| `LinkisNodeExecution.runJob` | 任务提交（`LinkisNodeExecutionImpl.java:103-122`） | ❌ 不修改 |
| `DefaultNodeRunner.cancel` | 节点 kill（`DefaultNodeRunner.scala:130-137`） | ⚠️ 复用（升级 kill 链路） |
| `EventCheckerRefExecutionOperation.submit/state` | 信号提交与轮询（Scala，eventchecker-appconn） | ❌ 不修改 |
| `ListenerEventBusRefExecutionScheduler` | entrance 进程内调度器 | ❌ 不修改 |
| `AbstractEventCheckReceiver.getOffset/updateMsgOffset` | 信号消费游标（Java） | ❌ 不修改（应急方案依赖分析对象） |
| `FlowExecutionUtils.isSignalNode` | 信号节点识别（`FlowExecutionUtils.scala:74-76`） | ⚠️ P1 可选接线 |

### 10.3 预估工时

| 任务 | 工时（人天） |
|-----|------------|
| `isLinkisJobCompleted` 异常分支重构 + 连续失败计时与升级机制（F-P0-01/02/04） | 1.0 |
| withName 状态映射兜底（F-P0-03） | 0.5 |
| 新增配置项定义与默认值评估 | 0.3 |
| P1 节点级超时兜底 + Timeout 启用 + isSignalNode 接线（F-P1-01/03） | 1.0 |
| 超时配置调优验证（F-P1-02，含灰度） | 0.5 |
| 单元测试（异常分支/阈值/状态映射） | 1.0 |
| 集成与回归测试（AC-01~AC-06 场景，含 entrance 压力注入） | 1.5 |
| kill 重跑应急方案验证与运维 SOP（AC-07） | 0.5 |
| 文档更新 | 0.3 |
| Linkis 侧容量治理协调（边界外） | 0.5 |
| **合计** | **7.1** |

---

**文档版本**：v1.0
**创建日期**：2026-11-02
**需求负责人**：待定
**优先级**：P1（当前迭代完成）

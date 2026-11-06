# 信号节点状态查询超时永久Running修复 测试用例

## 1. 文档信息

| 属性 | 值 |
|------|-----|
| 需求编号 | REQ-DSS-1.26.0-FIX-001 |
| 关联需求文档 | docs/1.26.0/requirements/信号节点状态查询超时永久Running修复_需求.md（AC-01~AC-07） |
| 关联设计文档 | docs/1.26.0/design/信号节点状态查询超时永久Running修复_设计.md（单测 12 项 + 集成场景 IT-01~IT-10） |
| 关联Feature | docs/1.26.0/features/信号节点状态查询超时永久Running修复.feature |
| 版本 | dev-1.26.0（Linkis 1.18.3-wds） |
| 被测模块 | dss-orchestrator/dss-workflow/dss-flow-execution-server（DefaultNodeRunner / NodeExecutionState / FlowEntranceJob / FlowExecutionEntranceConfiguration） |
| 创建日期 | 2026-11-02 |

## 2. 测试范围

- **P0 核心修复**：`DefaultNodeRunner.isLinkisJobCompleted` 异常分支（原 `case t:Throwable => true`）重构后的行为——短暂抖动继续轮询、长期不可用（连续失败 ≥ `wds.dss.flow.node.state.fetch.fail.timeout`，默认 30 分钟）升级 kill + 置 Failed + error 告警
- **P0 状态映射**：`NodeExecutionState.fromLinkisStateName` 三级映射（枚举快路径 / 别名表 / 未知兜底 Running + warn）
- **P1 超时兜底**：节点级最大运行时长（`wds.dss.flow.node.max.running.time` 默认 -1 关闭；信号节点独立默认 2h）；`FlowEntranceJob.onStatusChanged` 补 Timeout 分支（R-07 高风险项）
- **回归验证**：正常节点全生命周期翻转、ErrorException 路径、EmptyJob/EventSender、多节点 DAG、零配置升级兼容、服务重启
- **应急 SOP 与信号链路**：kill 重跑两组 `msg.after.send` 场景、Sender→event_queue→Receiver 端到端链路

**不在范围**（对应需求 N-01~N-05）：entrance 侧信号轮询逻辑、NodeExecutionStatusPoller 队列模型、外部告警系统对接、event_queue/event_status 表结构、entrance 容量治理本身。

## 3. 通用前置条件

1. DSS flow-execution-server 已部署含本次修复的 dev-1.26.0-SNAPSHOT 版本，Linkis entrance/Gateway/EM 正常运行
2. 工作流A含 EventSender 节点、工作流B含 EventReceiver 节点（nodeType=`linkis.appconn.eventchecker.eventreceiver`），B 依赖 A 的信号，ER 节点 `msg.after.send=false`（另注用例除外）
3. entrance 故障注入手段可用：`tc` 网络延迟注入（仅对 DSS→entrance 状态查询流量）、或测试桩替换 `LinkisNodeExecutionImpl.getState` 返回值/抛异常
4. 关键配置基线（未注明时按默认）：
   - `wds.dss.flow.node.state.fetch.fail.timeout=1800000`（30 分钟，P0 阈值）
   - `wds.dss.flow.node.max.running.time=-1`（普通节点关闭）
   - `wds.dss.flow.node.max.running.time.signal=7200000`（信号节点 2 小时）
   - `wds.linkis.flow.connection.timeout`（默认 300000；注入延迟须大于该值才触发查询超时）
   - `wds.dds.flow.node.status.poller.interval.time=3000`（单节点查询间隔）
5. DSS 日志（flow-execution-server）与 Linkis 管理台（按 taskID 查任务状态）可观测；可查询 `event_queue`/`event_status` 表

**关键日志关键字**（断言用，源自设计 7.3）：

| 关键字 | 级别 | 含义 |
|-------|------|------|
| `failed to get ... state` | error/warn | 状态查询失败 |
| `continuous failure` | warn | 连续失败进行中（含失败时长、阈值剩余） |
| `Escalate to Failed` | error | 连续失败达阈值升级 |
| `exceeded max running time` | error | 节点运行超时兜底触发 |
| `Unknown Linkis job state` | warn | 未知状态串兜底 |
| `Finished to execute node of` | info | 节点正常完成翻转 |
| `status is completed` | info | poller 移出队列日志（修复后 true 必伴随终态） |

**加速验证说明**：30 分钟阈值类用例可将 `wds.dss.flow.node.state.fetch.fail.timeout` 等比调小（如 120000=2 分钟，注入 110s/130s 验证边界），结论按等比时长外推；P1 信号节点 2h 兜底可临时调小 `wds.dss.flow.node.max.running.time.signal` 加速。

## 4. 测试用例

### 4.1 P0 状态查询异常治理（核心修复）

#### TC-ERFIX-001【AIGC】entrance短暂抖动（小于30分钟）后ER节点继续轮询并正常翻转Success

| 属性 | 值 |
|------|-----|
| 优先级 | P0 |
| 测试类型 | 功能测试 |
| 关联AC | AC-01 |
| 关联设计 | IT-01 / F-P0-01 / F-P0-04 |
| 执行方式 | 集成（tc 注入或 entrance 制造 Full GC） |

**前置条件**：
1. 工作流A的 EventSender 已执行成功，信号已写入 `event_queue` 表（可 SQL 查询确认）
2. 工作流B的 ER 节点已提交、DSS 侧状态 Running，runner 位于 `nodeRunnerQueue` 中被轮询
3. 失败阈值保持默认 1800000ms（30 分钟）

**测试步骤**：
1. 通过 `tc` 对 DSS→entrance 方向的状态查询流量注入网络延迟（大于 `wds.linkis.flow.connection.timeout`，如 400s），持续 5 分钟（远小于 30 分钟阈值）
2. 期间每隔约 1 分钟抓取一次 flow-execution-server 日志，检索 `failed to get`、`continuous failure` 关键字
3. 期间在工作流B实例页面观察 ER 节点状态
4. 5 分钟后移除延迟注入，恢复 entrance 正常
5. 等待 1~2 个轮询周期（poller 调度 5s + 单节点间隔 3s）

**预期结果**：
1. 抖动期间每轮查询失败仅输出 error/warn 日志，`isLinkisJobCompleted` 返回 false，节点保持 Running 且 runner 仍保留在 `nodeRunnerQueue` 中被继续轮询（不存在"移出队列"行为）
2. warn 日志周期输出连续失败信息，含节点名、已连续失败时长、阈值剩余时间（`continuous failure ... threshold ...`）
3. 不出现"返回 true 但节点状态为 Running"的日志组合（无 `status is completed` 与 Running 并存）
4. entrance 恢复后，ER 节点在下一个轮询周期内查询到 entrance 侧终态 Success，节点翻转为 Succeed，输出 `Finished to execute node of <节点名>`
5. 工作流B下游节点正常调度执行，工作流最终成功结束

---

#### TC-ERFIX-002【AIGC】连续失败期间warn日志周期输出失败时长与阈值剩余时间

| 属性 | 值 |
|------|-----|
| 优先级 | P1 |
| 测试类型 | 功能测试 |
| 关联AC | AC-01 |
| 关联设计 | F-P0-04 / 设计 6.1 单测 warn 断言 |
| 执行方式 | 集成（可配合阈值调小加速） |

**前置条件**：
1. ER 节点已提交且状态 Running，轮询正常（最近一次成功查询时间即 `lastStatusSuccessTime` 已初始化）
2. 日志采集正常，可按时间过滤

**测试步骤**：
1. 注入状态查询失败（延迟注入或测试桩使 getState 抛 SocketTimeoutException），持续约 10 分钟（或将阈值调小至 15 分钟等比缩短）
2. 收集期间全部 `continuous failure` warn 日志
3. 校验日志中"连续失败时长"字段随时间的递增关系
4. 恢复 entrance，确认下一次成功查询后不再输出该 warn

**预期结果**：
1. 失败期间每轮查询（间隔约 3s+）均输出 warn 日志，内容含：节点名、连续失败时长（秒）、阈值总时长、剩余时间、最后异常摘要（Cause: SocketTimeoutException...）
2. 日志中的失败时长随时间单调递增（如 60s→120s→...→600s），与注入起点吻合，无跳变或归零
3. 成功查询恢复后 warn 停止输出
4. 阈值剩余时间 = 阈值 - 已失败时长，数值正确

---

#### TC-ERFIX-003【AIGC】失败-成功-失败交错时连续失败计时自动重置

| 属性 | 值 |
|------|-----|
| 优先级 | P1 |
| 测试类型 | 功能测试 |
| 关联AC | AC-01 |
| 关联设计 | 设计 6.1 `testIsLinkisJobCompleted_SuccessRefreshesTimer` / 决策 4.2 |
| 执行方式 | 单元测试优先（mock getState 序列）；集成可用间歇注入模拟 |

**前置条件**：
1. 单测环境：可注入 mock 的 `LinkisNodeExecutionImpl.getState` 行为序列
2. 阈值配置为测试值（如 300000ms）

**测试步骤**：
1. 使 getState 按序返回：抛异常（第1-2轮）→ 返回 "Running"（第3轮，非终态）→ 抛异常（第4轮起持续）
2. 第4轮失败后读取 warn 日志中的连续失败时长
3. 持续失败至第3轮成功后再过满阈值时长，观察是否升级
4. 校验 `lastStatusSuccessTime` 刷新时机（成功返回 Running 也应刷新）

**预期结果**：
1. 第3轮返回 Running（非终态）属于"成功获取状态"，`lastStatusSuccessTime` 被刷新为当前时刻
2. 第4轮失败的 warn 日志中连续失败时长从 0 附近重新起算（非延续第1-2轮的累计值）
3. 只有自最近一次成功查询起连续失败满阈值才升级为 Failed；交错场景（每次失败时长均小于阈值且中间穿插成功）永不升级
4. 节点状态在整个交错期间保持 Running，最终恢复成功后正常翻终态

---

#### TC-ERFIX-004【AIGC】entrance长期不可用连续失败达30分钟阈值时节点kill并翻Failed告警

| 属性 | 值 |
|------|-----|
| 优先级 | P0 |
| 测试类型 | 异常测试 |
| 关联AC | AC-02 |
| 关联设计 | IT-02 / F-P0-02 / 设计 3.3.2 `escalateIfStateFetchTimeout` |
| 执行方式 | 集成（阈值调小加速：`wds.dss.flow.node.state.fetch.fail.timeout=120000`） |

**前置条件**：
1. ER 节点已提交且状态 Running（工作流B有下游节点待调度）
2. 测试配置 `wds.dss.flow.node.state.fetch.fail.timeout=120000`（2 分钟，加速验证）
3. Linkis 管理台可查询该节点 taskID

**测试步骤**：
1. 注入状态查询持续失败（延迟注入或桩），保持超过 2 分钟不恢复
2. 观察达到 2 分钟阈值时刻的节点行为：DSS 节点状态、Linkis 任务状态、error 告警日志
3. 检查工作流B整体状态与 runner 轮询情况
4. 重新运行工作流B验证可重跑

**预期结果**：
1. 连续失败时长达到阈值的轮次：系统对节点对应 Linkis 任务执行 kill（Linkis 侧任务转 Cancelled/Killed，管理台可查），节点状态翻转为 Failed（终态）
2. error 级告警日志输出（`Escalate to Failed`），含节点名、失败时长（约 120s）、阈值、最后异常摘要
3. 该 runner 被移出 `nodeRunnerQueue` 后不再被重复处理，无重复 kill/重复翻转
4. 工作流B正常结束为 Failed 状态（不再永久挂起），下游节点不执行；工作流B可重新运行
5. 升级翻转走 `transitionState(Failed) → FlowEntranceJob.onStatusChanged` 既有失败链路（runningNodes→failedNodes，工作流 kill 结束）

---

#### TC-ERFIX-005【AIGC】阈值边界-失败29分钟后恢复不误杀节点正常翻转

| 属性 | 值 |
|------|-----|
| 优先级 | P0 |
| 测试类型 | 异常测试（边界） |
| 关联AC | AC-01 / AC-02 |
| 关联设计 | 阈值边界（决策 4.2 计时基准） |
| 执行方式 | 集成（等比加速：阈值 120000，注入 110s 后恢复） |

**前置条件**：
1. ER 节点 Running，entrance 侧信号任务实际仍在等待或已 Success
2. 阈值配置 120000ms（等比代表 30 分钟场景的 29 分钟）
3. 已知该节点信号在 `event_queue` 中存在（或 entrance 侧任务即将翻 Success）

**测试步骤**：
1. 注入状态查询失败，精确控制在阈值前恢复（如阈值 120s，注入 110s 后移除）
2. 恢复后观察下一轮查询与节点状态
3. 校验恢复时刻前最后一轮 warn 日志中的失败时长与剩余时间

**预期结果**：
1. 失败时长（110s）< 阈值（120s），全程不触发升级：无 `Escalate to Failed`、无 kill、节点不被置 Failed
2. 恢复后下一轮查询成功，`lastStatusSuccessTime` 刷新，连续失败计时清零
3. entrance 侧信号任务若已 Success，ER 节点立即翻 Succeed，工作流正常推进
4. 未达阈值前的最后一轮 warn 日志显示剩余时间约 10s，数值正确

---

#### TC-ERFIX-006【AIGC】阈值边界-失败31分钟超过阈值正确升级Failed

| 属性 | 值 |
|------|-----|
| 优先级 | P0 |
| 测试类型 | 异常测试（边界） |
| 关联AC | AC-02 |
| 关联设计 | IT-02 边界（决策 4.2：`failedDuration >= failTimeout` 判定） |
| 执行方式 | 集成（等比加速：阈值 120000，注入 130s） |

**前置条件**：
1. 同 TC-ERFIX-004（阈值 120000ms 加速）
2. 注入手段可持续超过阈值时长

**测试步骤**：
1. 注入状态查询持续失败 130s（超出阈值 10s，等比代表 31 分钟）
2. 观察跨过阈值时刻（第 120s 后的第一个轮询轮次）的行为
3. 校验升级时机：失败时长刚 ≥ 阈值的那一轮即升级，而非下一轮或更晚

**预期结果**：
1. 失败时长达到 120s 后的第一个查询轮次即触发升级：kill Linkis 任务 + 节点置 Failed + error 告警，返回 true 结束轮询
2. 告警日志中失败时长 ≈ 120~123s（阈值 + 一个轮询间隔内的误差），不出现明显延迟升级
3. 升级后节点不再被轮询，工作流结束为 Failed，可重跑
4. 不出现"失败时长未达阈值即升级"（误杀）或"超阈值仍不升级"（漏杀）两个方向的偏差

---

#### TC-ERFIX-007【AIGC】升级时kill请求失败不阻断节点置Failed

| 属性 | 值 |
|------|-----|
| 优先级 | P1 |
| 测试类型 | 异常测试 |
| 关联AC | AC-02 |
| 关联设计 | 设计 3.3.2 `killLinkisJobQuietly`（tryAndWarn）/ 4.7 场景3 / 6.1 `testKillLinkisJobQuietly_FailureNotBlocking` |
| 执行方式 | 单元测试优先（mock cancel 抛异常）；集成可用持续注入模拟 |

**前置条件**：
1. 单测环境：mock `LinkisNodeExecutionImpl.cancel` 抛异常（如 SocketTimeoutException）
2. 节点已连续失败达到阈值

**测试步骤**：
1. 使状态查询连续失败达阈值，同时保持 kill 链路不可用（cancel 调用同样超时/抛异常——entrance 仍不可用时 kill 必然失败）
2. 观察升级流程是否继续执行
3. 校验日志中 kill 失败的记录方式

**预期结果**：
1. kill 抛出的异常被 `tryAndWarn` 吞掉，仅记录 warn 日志，不向上抛出
2. `transitionState(Failed)` 正常执行，节点照常翻转为 Failed 终态，轮询结束——状态收敛不依赖 kill 成功
3. error 告警日志（`Escalate to Failed`）仍正常输出
4. Linkis 侧任务可能仍在运行（kill 失败），但 DSS 侧工作流已正确结束为 Failed；无节点滞留 Running

---

#### TC-ERFIX-008【AIGC】升级kill与entrance侧任务实际已完成的幂等竞态

| 属性 | 值 |
|------|-----|
| 优先级 | P2 |
| 测试类型 | 异常测试 |
| 关联AC | AC-02 |
| 关联设计 | 设计 4.7（kill 幂等性分析）/ R-06 |
| 执行方式 | 单元测试/测试桩（精确构造竞态窗口） |

**前置条件**：
1. 构造场景：状态查询持续失败达阈值，但 entrance 侧该任务实际已翻 Success（如仅对状态查询接口注入故障、或桩上设定"查询恒抛异常 + 任务实际已 Succeed + kill 可达"）
2. Linkis 管理台可查该 taskID 真实状态

**测试步骤**：
1. 维持查询失败满阈值，触发升级 kill（此时 entrance 侧任务已 Success）
2. 查询 Linkis 侧该任务状态是否因 kill 改变
3. 确认 DSS 侧节点终态与工作流状态

**预期结果**：
1. kill 对已完成的 Linkis 任务是幂等空操作：entrance 侧任务状态保持 Success 不变，不产生副作用或错误级联
2. DSS 侧节点被置 Failed（查不到真实状态，保守收敛），error 日志留痕可追溯
3. 工作流以失败结束、可重跑——相比修复前永久挂起是严格改善（接受"任务实际成功但节点置 Failed"的最差结果）
4. kill 与 `transitionState(Failed)` 不产生状态双翻转冲突（transitionState 幂等保护生效）

---

#### TC-ERFIX-009【AIGC】ErrorException业务错误路径仍返回false继续轮询不升级

| 属性 | 值 |
|------|-----|
| 优先级 | P0 |
| 测试类型 | 回归测试 |
| 关联AC | AC-04 |
| 关联设计 | 设计 5.1（ErrorException 分支逐字保留）/ 6.1 `testIsLinkisJobCompleted_ErrorException_KeepPolling` |
| 执行方式 | 单元测试（mock getState 抛 ErrorException）+ 集成回归 |

**前置条件**：
1. 可构造 `Linkis` 返回业务错误响应（ErrorException 从响应 JSON 解析而来，区别于 HTTP 超时）
2. 节点状态 Running

**测试步骤**：
1. 使 getState 抛 ErrorException（Linkis 业务错误，如任务不存在类响应）
2. 观察 `isLinkisJobCompleted` 返回值与节点状态
3. 连续多次 ErrorException 后恢复正常，观察是否误触升级

**预期结果**：
1. ErrorException 分支行为与修复前完全一致：仅输出 warn 日志（`failed to get ... state`），返回 false 继续轮询
2. 连续 ErrorException 期间不进入升级判定（ErrorException 分支先于 Throwable 分支匹配，不参与失败计时升级路径的语义保持），节点不翻 Failed、不被 kill
3. 恢复后节点按真实状态正常翻转
4. 该路径为修复明确保留项（设计 5.1 兼容性核对表），回归不得引入行为变化

---

### 4.2 P0 状态映射兜底

#### TC-ERFIX-010【AIGC】Linkis已知状态串经枚举快路径正确映射且大小写宽容

| 属性 | 值 |
|------|-----|
| 优先级 | P0 |
| 测试类型 | 接口测试 |
| 关联AC | AC-03 / AC-04 |
| 关联设计 | 设计 3.3.3 映射表 / 6.1 `testFromLinkisStateName_EnumFastPath` |
| 执行方式 | 单元测试（`NodeExecutionStateTest`） |

**前置条件**：
1. 单测环境可直调 `NodeExecutionState.fromLinkisStateName`
2. 设计 3.3.3 完整映射表作为断言依据

**测试步骤**：
1. 逐一传入 DSS 枚举内的状态串：`Inited`、`Scheduled`、`WaitForRetry`、`Running`、`Succeed`、`Failed`、`Cancelled`、`Skipped`（及前后带空格变体）
2. 传入大小写变体：`running`、`SUCCEED`、`failed`、`CANCELLED`
3. 断言各返回值的枚举类型与 `isCompleted` 结果

**预期结果**：
1. 枚举内状态串全部命中快路径返回对应枚举值，结果与原 `withName` 一致（正常路径零行为变更）
2. 大小写变体经 `equalsIgnoreCase` 命中同一枚举值（如 `succeed`→Succeed），属安全性增强不引入行为差异
3. 终态判断正确：Succeed/Failed/Cancelled/Timeout/Skipped 的 `isCompleted`=true；Inited/Scheduled/WaitForRetry/Running 的 `isCompleted`=false
4. 无任何已知状态串落入未知兜底分支（不输出 `Unknown Linkis job state` warn）

---

#### TC-ERFIX-011【AIGC】entrance返回未知状态串时映射Running继续轮询并warn原始串

| 属性 | 值 |
|------|-----|
| 优先级 | P0 |
| 测试类型 | 接口测试（异常） |
| 关联AC | AC-03 |
| 关联设计 | IT-03 / F-P0-03 / 6.1 `testFromLinkisStateName_UnknownFallback` |
| 执行方式 | 单元测试 + 集成（测试桩使 getState 返回未知串） |

**前置条件**：
1. 测试桩可控制 getState 返回值序列：`SomeNewLinkisState`（未知串）→ `Running` → `Succeed`
2. ER 节点状态 Running

**测试步骤**：
1. 使 getState 返回未知状态串 `SomeNewLinkisState`，持续 2 个以上轮询周期
2. 观察是否抛 NoSuchElementException、节点轮询是否中断
3. 校验 warn 日志内容
4. 桩切换为返回 `Running` 再返回 `Succeed`，观察节点翻转

**预期结果**：
1. 未知串被映射为 Running（非终态），`isLinkisJobCompleted` 返回 false，节点继续留在轮询队列，全程无 NoSuchElementException 中断
2. warn 日志输出 `Unknown Linkis job state [SomeNewLinkisState], mapped to Running by default`，含原始状态串
3. 恢复返回正常状态串后，节点按真实终态正常翻转（Succeed），下游正常调度
4. 未知串轮询期间不参与失败升级计时（属成功获取状态），节点不因未知串被误杀

---

#### TC-ERFIX-012【AIGC】终态别名success/killed/timeout等确定性翻译为对应终态

| 属性 | 值 |
|------|-----|
| 优先级 | P0 |
| 测试类型 | 接口测试 |
| 关联AC | AC-03 |
| 关联设计 | 设计 3.3.3 `LINKIS_STATE_ALIASES` / 6.1 `testFromLinkisStateName_Aliases` / 决策 4.3 |
| 执行方式 | 单元测试（逐条断言映射表）+ 抽样集成 |

**前置条件**：
单测环境可直调 `fromLinkisStateName`，按设计 3.3.3 映射表准备全部别名条目。

**测试步骤**：
1. 逐条传入成功终态别名：`success`、`succeeded`、`succeed`
2. 逐条传入失败终态别名：`failed`、`failure`、`error`、`failed_for_resultsize`
3. 逐条传入取消终态别名：`cancelled`、`canceled`、`cancel`、`killed`、`kill`、`shutdown`、`stopped`
4. 逐条传入超时终态别名：`timeout`、`time_out`、`scheduled_timeout`、`queue_full_timeout`
5. 逐条传入中间态别名：`submitting`、`queued`、`idle`、`sched`、`scheduled`、`wait_for_retry`、`retry`、`init`
6. 集成抽样：测试桩使 getState 依次返回 `success`、`killed`、`timeout`，观察节点翻转

**预期结果**：
1. 成功别名组全部映射 Succeed（isCompleted=true）；集成抽样中 `success` 使节点翻 Succeed
2. 失败别名组全部映射 Failed；`failed_for_resultsize`（Linkis 结果集超限失败）同样映射 Failed
3. 取消别名组全部映射 Cancelled；集成抽样中 `killed` 使节点翻 Cancelled（而非被兜底为 Running 遭 30 分钟阈值误杀）
4. 超时别名组全部映射 Timeout；`scheduled_timeout`（排队超时）映射 Timeout 终态
5. 中间态别名正确映射（submitting/queued/idle→Running，sched→Scheduled，retry→WaitForRetry，init→Inited），均非终态
6. 全部别名命中映射表，不输出 `Unknown Linkis job state` warn

---

#### TC-ERFIX-013【AIGC】空串与null状态串兜底Running不抛空指针

| 属性 | 值 |
|------|-----|
| 优先级 | P1 |
| 测试类型 | 接口测试（异常） |
| 关联AC | AC-03 |
| 关联设计 | 设计 3.3.3（null/空串兜底说明）/ 6.1 `testFromLinkisStateName_UnknownFallback` |
| 执行方式 | 单元测试 |

**前置条件**：
单测环境可直调 `fromLinkisStateName`，可传入 null 与空串。

**测试步骤**：
1. 传入 `null` 调用 `fromLinkisStateName`
2. 传入空串 `""`
3. 传入纯空白串 `"   "`（trim 后为空）
4. 断言返回值与异常情况

**预期结果**：
1. null 不抛 NullPointerException，返回 Running
2. 空串与纯空白串返回 Running（非终态）
3. 三种输入均不中断轮询、不参与异常分支（视为成功获取状态但状态未知）
4. 空串/null 场景不输出未知状态 warn（区别于未知串场景，设计为静默兜底）或按实现输出口径一致即可，以不抛异常、不中断为硬性要求

---

### 4.3 P1 节点级最大运行时长兜底与配置

#### TC-ERFIX-014【AIGC】普通节点max.running.time默认-1时超时兜底不生效

| 属性 | 值 |
|------|-----|
| 优先级 | P0 |
| 测试类型 | 回归测试 |
| 关联AC | AC-04 / AC-06 |
| 关联设计 | 设计 4.4（默认关闭理由）/ 6.1 `testIsNodeRunningTimeout_DisabledByDefault` |
| 执行方式 | 单元测试 + 集成回归 |

**前置条件**：
1. 不配置 `wds.dss.flow.node.max.running.time`（默认 -1）
2. 普通节点（如 Scriptis 脚本节点，nodeType 非信号节点）

**测试步骤**：
1. 单测：普通节点 startTime 回拨超长时间（如 25 小时），调用超时判定
2. 集成：执行一个运行时长超过历史常规阈值的普通节点任务（或单测等价验证），观察是否出现 `exceeded max running time` 日志与 Timeout 翻转

**预期结果**：
1. 默认 -1 时 `isNodeRunningTimeout` 恒返回 false，不触发超时兜底
2. 长时间运行的普通节点不被 kill、不翻 Timeout，行为与修复前完全一致（零回归，多小时合法任务不受影响）
3. 无 `exceeded max running time` 日志输出

---

#### TC-ERFIX-015【AIGC】配置max.running.time后普通节点超时翻Timeout并结束工作流

| 属性 | 值 |
|------|-----|
| 优先级 | P1 |
| 测试类型 | 配置测试 |
| 关联AC | AC-02（变体） |
| 关联设计 | IT-08 / F-P1-01 |
| 执行方式 | 集成（配置 `wds.dss.flow.node.max.running.time=60000`） |

**前置条件**：
1. 配置 `wds.dss.flow.node.max.running.time=60000`（60 秒）并重启生效
2. 工作流含一个正常执行约 2 分钟的普通节点（如 sleep 型脚本）
3. entrance 健康（状态查询正常，排除 P0 路径干扰）

**测试步骤**：
1. 启动工作流，节点进入 Running
2. 观察 60 秒时刻节点的状态翻转、kill 行为与日志
3. 观察工作流整体状态与 FlowEntranceJob 日志

**预期结果**：
1. 节点运行满 60s 的下一个轮询轮次触发兜底：先尝试 kill Linkis 任务（失败仅 warn），再 `transitionState(Timeout)` 翻 Timeout 终态，返回 true 结束轮询
2. error 日志输出 `exceeded max running time`，含节点名、配置时长、startTime
3. FlowEntranceJob 输出 `node(...) exceeded max running time, turned to Timeout`（ERROR 级）
4. 节点从 runningNodes 迁移到 failedNodes，工作流以失败结束（不挂起），可重跑
5. 未配置时（默认 -1）同场景不触发——与 TC-ERFIX-014 互为开关验证

---

#### TC-ERFIX-016【AIGC】信号节点默认2小时超时兜底生效翻Timeout

| 属性 | 值 |
|------|-----|
| 优先级 | P1 |
| 测试类型 | 功能测试 |
| 关联AC | AC-02（变体） |
| 关联设计 | IT-07 / F-P1-01 / F-P1-03（isSignalNode 接线）/ 6.1 `testIsNodeRunningTimeout_SignalNodeDefault` |
| 执行方式 | 集成（临时调小 `wds.dss.flow.node.max.running.time.signal` 加速，如 120000） |

**前置条件**：
1. 不配置 `wds.dss.flow.node.max.running.time`（保持 -1），使信号节点走独立默认配置
2. 测试加速：临时配置 `wds.dss.flow.node.max.running.time.signal=120000`（2 分钟，等比代表默认 2 小时）
3. ER 节点（nodeType=`linkis.appconn.eventchecker.eventreceiver`）持续 Running 等待信号：构造 entrance 侧 maxLoopTime 兜底失效场景（如测试桩使 getState 恒返回 Running、entrance 侧信号任务不翻终态），且信号不写入

**测试步骤**：
1. 提交工作流B，ER 节点进入 Running 并持续轮询（状态查询本身正常，恒返回 Running）
2. 观察超过信号节点最大运行时长（加速值 2 分钟）后的行为
3. 校验节点终态、工作流状态、日志关键字

**预期结果**：
1. 运行时长达到加速阈值的轮询轮次：kill Linkis 任务 + 节点翻 Timeout 终态
2. error 日志输出 `exceeded max running time`（信号节点独立默认生效；生产默认 2h = 2×entrance 侧 max.receive.hours）
3. FlowEntranceJob Timeout 分支生效：runningNodes→failedNodes，工作流失败结束，不挂起
4. 信号节点识别依赖 `wds.dss.flow.signal.nodes`（默认 eventreceiver）与 nodeType 比对——`FlowExecutionUtils.isSignalNode` 首次接线生效
5. 验证默认关闭的普通节点不受该信号配置影响（同工作流中普通节点无超时翻转）

---

#### TC-ERFIX-017【AIGC】信号节点合法等待窗口内（1.5小时）不被超时兜底误杀

| 属性 | 值 |
|------|-----|
| 优先级 | P1 |
| 测试类型 | 功能测试（边界） |
| 关联AC | AC-02（变体） |
| 关联设计 | 设计 4.4 / 2.3（2×max.receive.hours 余量）/ 6.1 `testIsNodeRunningTimeout_SignalNodeWithinWindow` / R-02 |
| 执行方式 | 单元测试 + 集成等比加速 |

**前置条件**：
1. ER 节点 Running，entrance 侧任务处于合法等待期（max.receive.hours 默认 1 小时内）
2. 单测：startTime 回拨 1.5 小时（小于 2 小时信号默认阈值）

**测试步骤**：
1. 单测：nodeType=eventreceiver、startTime 回拨 1.5h，调用 `isNodeRunningTimeout`
2. 集成等比：信号阈值加速为 120s，ER 节点在 90s（窗口内）时收到信号
3. 观察窗口内节点是否触发 Timeout

**预期结果**：
1. 1.5h < 2h，超时判定返回 false，不触发 kill、不翻 Timeout
2. 集成场景：90s 时信号到达，ER 节点正常翻 Succeed（合法等待不被误杀，验证与 entrance 侧 maxLoopTime 的竞态余量）
3. 仅当超过 2h（entrance 侧兜底也失效的极端场景）才触发兜底——DSS 侧兜底晚于 entrance 侧 max.receive.hours（1h）触发

---

#### TC-ERFIX-018【AIGC】Timeout状态节点触发FlowEntranceJob分支迁移节点并结束工作流

| 属性 | 值 |
|------|-----|
| 优先级 | P0 |
| 测试类型 | 功能测试 |
| 关联AC | AC-02（变体） |
| 关联设计 | 设计 3.3.4（FlowEntranceJob Timeout 分支）/ R-07（高风险：分支遗漏导致新形态挂起） |
| 执行方式 | 集成（同 TC-ERFIX-015/016 任一触发手段） |

**前置条件**：
1. 工作流B结构：ER 节点 → 下游节点（至少 1 个），另有 1 个已成功的上游节点
2. 通过 TC-ERFIX-015 或 TC-ERFIX-016 手段使某节点翻 Timeout

**测试步骤**：
1. 触发节点翻 Timeout
2. 检查 FlowContext 中节点所在集合：runningNodes / failedNodes / succeedNodes
3. 校验 `isFlowCompleted` 与工作流实例最终状态
4. 校验下游节点状态与 kill 级联行为

**预期结果**：
1. FlowEntranceJob `onStatusChanged` 的 Timeout 分支被命中：输出 ERROR 日志（`exceeded max running time, turned to Timeout`）
2. 超时节点从 `runningNodes` 迁移到 `failedNodes`（`FlowContext.changedNodeState`），不滞留 runningNodes
3. `isFlowCompleted` 最终为 true，工作流实例正常结束为失败状态——不出现"节点已 Timeout 但工作流仍 Running"的新形态挂起（R-07 专项验证）
4. 工作流 kill 级联：下游节点被取消/跳过，不执行
5. Timeout 分支行为与 Failed 分支对齐（迁移 + 日志 + kill 工作流）

---

### 4.4 回归验证

#### TC-ERFIX-019【AIGC】正常节点全生命周期Succeed/Failed/Cancelled三路径翻转回归

| 属性 | 值 |
|------|-----|
| 优先级 | P0 |
| 测试类型 | 回归测试 |
| 关联AC | AC-04 |
| 关联设计 | IT-04 / 设计 5.1（正常路径逐路径核对） |
| 执行方式 | 集成回归 |

**前置条件**：
1. entrance 健康，无任何故障注入
2. 工作流含 Scriptis 脚本节点（SQL/Spark 均可）

**测试步骤**：
1. 场景一：执行正常成功的脚本（如 `select 1`），观察节点全生命周期状态序列
2. 场景二：执行抛错脚本（如 `select * from not_exist_table`），观察翻转
3. 场景三：节点运行中（如长 SQL）对工作流执行人工取消，观察翻转
4. 三个场景均检查 runner 移出队列与工作流推进/结束

**预期结果**：
1. 成功场景：状态序列 Scheduled→Running→Succeed，翻转走 `onStatusChanged + transitionState` 既有路径，输出 `Finished to execute node of`，行为与修复前一致
2. 失败场景：节点翻 Failed，工作流失败结束，下游不执行
3. 取消场景：节点翻 Cancelled（`cancel()` 链路不改动），runner 移出队列
4. 三种终态下 runner 均正常移出轮询队列，工作流正常推进/结束；poller 的 `status is completed` 日志与实际终态一致
5. 全程无 `continuous failure`/`Unknown Linkis job state`/`Escalate to Failed` 等新增路径日志——确认正常路径零变更

---

#### TC-ERFIX-020【AIGC】EmptyJob空节点与EventSender节点回归不受影响

| 属性 | 值 |
|------|-----|
| 优先级 | P1 |
| 测试类型 | 回归测试 |
| 关联AC | AC-05 |
| 关联设计 | IT-05 / 设计 5.1（EmptyJob 路径不改动） |
| 执行方式 | 集成回归 |

**前置条件**：
工作流A含 EmptyJob 空节点与 EventSender 信号发送节点。

**测试步骤**：
1. 执行工作流A
2. 观察 EmptyJob 节点提交时刻的状态翻转
3. 观察 EventSender 发送成功后的状态翻转与 `event_queue` 表写入
4. 检查两类节点是否进入任何异常分支

**预期结果**：
1. EmptyJob 提交时直接翻 Succeed（`DefaultNodeRunner.run()` 112-116 路径不变，不进入轮询）
2. EventSender 信号成功写入 `event_queue` 后正常翻 Succeed
3. 两类节点均不进入 Throwable/ErrorException 异常分支，无新增路径日志
4. 工作流A正常成功结束

---

#### TC-ERFIX-021【AIGC】多节点DAG工作流正常编排回归

| 属性 | 值 |
|------|-----|
| 优先级 | P1 |
| 测试类型 | 回归测试 |
| 关联AC | AC-04 |
| 关联设计 | 设计 6.3 回归范围（AppConn 节点轮询全链路、失败传导） |
| 执行方式 | 集成回归 |

**前置条件**：
1. entrance 健康
2. 工作流含多类型节点 DAG：上游脚本节点 → 中游 2 个并行节点（不同类型 AppConn，如 Scriptis SQL + Spark）→ 下游汇总节点，总节点数 ≥ 5

**测试步骤**：
1. 正常执行工作流，观察各节点调度顺序、并行分支与状态翻转
2. 变体：使中游某节点执行失败，观察失败传导（FlowEntranceJob.kill → killNodes 级联取消）
3. 多节点同时在跑期间观察 poller 轮询吞吐（20 线程、3s 间隔）是否正常

**预期结果**：
1. 正常场景：DAG 拓扑顺序调度正确，并行节点各自独立翻转 Succeed，下游在全部上游完成后执行，工作流成功结束
2. 失败场景：失败节点翻 Failed，其余在跑节点被级联取消翻 Cancelled，工作流失败结束（失败传导链路复用，行为与修复前一致）
3. 多节点并发轮询正常，无节点漏轮询/重复翻转
4. 本次修改（DefaultNodeRunner/FlowEntranceJob）未破坏既有编排语义

---

#### TC-ERFIX-022【AIGC】零配置升级兼容-不配置新增配置项服务行为正常

| 属性 | 值 |
|------|-----|
| 优先级 | P1 |
| 测试类型 | 配置测试 |
| 关联AC | AC-06 |
| 关联设计 | IT-06 / 设计 5.3（新增配置兼容性） |
| 执行方式 | 集成 |

**前置条件**：
1. 使用修复前相同的部署配置启动服务（不配置任何本次新增配置项）
2. `wds.linkis.flow.connection.timeout` 不配置（走代码默认 300000）

**测试步骤**：
1. 零新增配置启动 flow-execution-server，检查启动日志无配置缺失报错
2. 执行含信号节点的工作流全流程（Sender→Receiver→下游）
3. 抽查配置默认值生效情况（日志或配置读取接口）：fail.timeout=1800000、max.running.time=-1、signal=7200000
4. 校验 `wds.linkis.flow.connection.timeout` 不配置时语义与修复前一致

**预期结果**：
1. 服务启动正常，新配置项按默认值生效，不配置不报错、不强制修改部署
2. 含信号节点工作流全流程行为正常（相较修复前仅增加 30 分钟失败收敛与信号节点 2h 兜底两项保护，正常任务无感知）
3. 既有配置语义全部不变（connection.timeout/poller 系列/signal.nodes）
4. 不新增数据库表、不改表结构

---

#### TC-ERFIX-023【AIGC】服务重启后失败计时从新startTime初始化

| 属性 | 值 |
|------|-----|
| 优先级 | P2 |
| 测试类型 | 回归测试 |
| 关联AC | AC-06（变体） |
| 关联设计 | 设计 4.6（不持久化决策）/ 6.1 `testRun_InitializesLastStatusSuccessTime` / R-03 |
| 执行方式 | 单元测试 + 集成 |

**前置条件**：
1. 单测：可执行一次完整 `run()` 后检查字段
2. 集成：可重启 flow-execution-server

**测试步骤**：
1. 单测：正常执行 `run()`，校验 `lastStatusSuccessTime == startTime`
2. 集成：重启服务后重新提交工作流，若 entrance 处于持续不可用状态（注入），观察升级发生时刻
3. 对比重启前后工作流行为语义

**预期结果**：
1. 节点提交时 `lastStatusSuccessTime` 初始化为 startTime（提交即处于失败计时保护下）
2. 重启后计时重新从新 startTime 起算（重启前累计的失败时长丢失并重置）——重启后 entrance 持续不可用时，自新提交起 30 分钟后才升级，而非立即
3. 重启本身中断工作流为既有行为，计时重置语义自洽（R-03 接受项），无异常状态残留

---

### 4.5 应急 SOP 与信号端到端

#### TC-ERFIX-024【AIGC】信号收发端到端链路Sender发送Receiver接收下游调度

| 属性 | 值 |
|------|-----|
| 优先级 | P0 |
| 测试类型 | 功能测试（E2E） |
| 关联AC | AC-01 / AC-05 |
| 关联设计 | 设计 6.3（信号节点跨工作流依赖回归） |
| 执行方式 | 集成（entrance 健康，全链路无注入） |

**前置条件**：
1. entrance 健康，无任何故障注入
2. 工作流A：EventSender（msgKey=test_signal_001）→ 记录节点
3. 工作流B：EventReceiver（msgKey=test_signal_001，msg.after.send=false）→ 下游执行节点

**测试步骤**：
1. 启动工作流B，ER 节点进入 Running 等待信号
2. 启动工作流A，EventSender 执行发送
3. 查询 `event_queue` 表确认信号写入（msgKey=test_signal_001）
4. 观察工作流B的 ER 节点在 1~2 个 query.frequency（60s）周期内的翻转
5. 观察下游节点调度与两个工作流的最终状态

**预期结果**：
1. Sender 节点执行成功翻 Succeed，信号持久化到 `event_queue`
2. ER 节点在信号到达后 1 个轮询周期（60s 级）内翻 Succeed，`event_status` 游标推进（日志 `The last record id was ...`）
3. 工作流B下游节点正常调度执行，B 成功结束；A 成功结束
4. 正常信号链路不因本次改动（DefaultNodeRunner/NodeExecutionState/FlowEntranceJob）受任何影响

---

#### TC-ERFIX-025【AIGC】应急SOP-msg.after.send为false时kill重跑旧信号可被消费

| 属性 | 值 |
|------|-----|
| 优先级 | P1 |
| 测试类型 | 回归测试（应急流程） |
| 关联AC | AC-07 |
| 关联设计 | IT-10 / 设计 8.2 SOP / 需求 3.3（kill 重跑语义） |
| 执行方式 | 集成（用修复前版本构造卡死节点，或桩模拟） |

**前置条件**：
1. 存在一个因本 Bug 已卡死 Running 的 ER 节点（用修复前版本构造，或测试桩拔掉修复逻辑模拟），其 `msg.after.send=false`
2. 该信号已写入 `event_queue`，`event_status` 表游标未推进过该 msg_id
3. 修复版本已部署

**测试步骤**：
1. 在 DSS 前端对卡死的工作流B实例执行 Kill（走 `FlowEntranceJob.kill → killNodes → runner.cancel`）
2. 重新运行工作流B
3. 观察 ER 节点 1~2 个 query.frequency（60s）周期内的状态与 `event_status` 游标推进日志

**预期结果**：
1. kill 后卡死工作流实例终止（存量卡死节点可被清理）
2. 重跑后新 ER 实例按 `event_status` 游标继续消费（`AbstractEventCheckReceiver.getOffset`），查询窗口起点为全窗口（allStartTime）
3. 旧信号（kill 前写入的）被正常接收，ER 节点翻 Success，工作流B继续执行——信号不丢不重
4. 游标推进日志正常输出，无重复消费（`updateMsgOffset` 的 ON DUPLICATE KEY UPDATE 保护）

---

#### TC-ERFIX-026【AIGC】应急SOP-msg.after.send为true时kill重跑旧信号不被消费需重发

| 属性 | 值 |
|------|-----|
| 优先级 | P1 |
| 测试类型 | 回归测试（应急流程） |
| 关联AC | AC-07 |
| 关联设计 | IT-10 / 设计 8.2 SOP / R-02（D-02） |
| 执行方式 | 集成（同 TC-ERFIX-025 构造） |

**前置条件**：
1. 存在卡死 Running 的 ER 节点（同 TC-ERFIX-025 构造），其 `msg.after.send=true`
2. 旧信号已写入 `event_queue`（写入时间早于重跑时刻）

**测试步骤**：
1. Kill 卡死的工作流B实例
2. 重新运行工作流B，不重发信号
3. 观察 ER 节点状态（等待数个 query.frequency 周期，确认旧信号不被消费）
4. 由上游业务方人工重发信号（同 msgKey 再写入 `event_queue`）
5. 再次观察 ER 节点翻转

**预期结果**：
1. 重跑后新 ER 实例的查询窗口起点为新实例进程启动时间（nowStartTime），旧信号落在窗口外不被消费
2. 不重发信号时 ER 节点持续 Running 等待（该边界行为符合代码语义，非 Bug；须写入运维 SOP 提示）
3. 人工重发信号后，ER 节点在 1 个 query.frequency 周期内翻 Success，工作流继续
4. 验证结论固化为运维 SOP：msg.after.send=true 的 kill 重跑必须先通知上游重发信号

## 5. 用例统计

### 5.1 优先级分布

| 优先级 | 数量 | 用例 |
|:------:|:----:|------|
| P0 | 12 | TC-ERFIX-001/004/005/006/009/010/011/012/014/018/019/024 |
| P1 | 12 | TC-ERFIX-002/003/007/013/015/016/017/020/021/022/025/026 |
| P2 | 2 | TC-ERFIX-008/023 |
| **合计** | **26** | — |

### 5.2 测试类型分布

| 测试类型 | 数量 | 用例 |
|---------|:----:|------|
| 功能测试 | 9 | TC-ERFIX-001/002/003/016/017/018/024（+E2E） |
| 异常测试 | 6 | TC-ERFIX-004/005/006/007/008 |
| 接口测试 | 4 | TC-ERFIX-010/011/012/013 |
| 配置测试 | 2 | TC-ERFIX-015/022 |
| 回归测试 | 9 | TC-ERFIX-009/014/019/020/021/023/025/026（+兼容） |

> 注：部分用例兼具多重属性（如 TC-ERFIX-005/006 为异常+边界），按主属性归类统计。

### 5.3 Wemind 分类分布

| Wemind 分类 | 数量 | 用例 |
|------------|:----:|------|
| 功能案例 | 7 | TC-ERFIX-001/002/003/016/017/018/024 |
| 接口案例 | 4 | TC-ERFIX-010/011/012/013 |
| 异常案例 | 6 | TC-ERFIX-004/005/006/007/008/015 |
| 回归案例 | 9 | TC-ERFIX-009/014/019/020/021/022/023/025/026 |

## 6. 验收标准覆盖检查

| AC | 验收标准摘要 | 覆盖用例 | 覆盖状态 |
|:--:|------------|---------|:-------:|
| AC-01 | 短暂抖动（<阈值）继续轮询 + warn 周期日志；恢复后下周期正常翻转 | TC-ERFIX-001/002/003/005/024 | ✅ |
| AC-02 | 长期不可用（≥阈值）kill + Failed + error 告警，工作流失败结束可重跑 | TC-ERFIX-004/006/007/008/015/016/017/018 | ✅ |
| AC-03 | 未知状态映射 Running + warn 原串，不抛异常，恢复后正常翻转 | TC-ERFIX-010/011/012/013 | ✅ |
| AC-04 | 正常 Succeed/Failed/Cancelled 三路径回归一致 | TC-ERFIX-009/019/021（+014） | ✅ |
| AC-05 | EmptyJob/EventSender 回归不受影响 | TC-ERFIX-020/024 | ✅ |
| AC-06 | 零配置升级，新配置默认生效，既有配置语义不变 | TC-ERFIX-022/023（+014） | ✅ |
| AC-07 | kill 重跑：false 组游标续读安全；true 组需人工重发（写入 SOP） | TC-ERFIX-025/026 | ✅ |

**覆盖率**：7/7 验收标准（100%）。

## 7. Feature 覆盖率统计

| Feature Scenario | 标签 | 对应用例 | 状态 |
|------------------|------|---------|:----:|
| 复现Bug - entrance状态查询超时后节点永久Running | @skip | TC-ERFIX-001/004 反向验证（修复后行为对立面） | ✅ 反向覆盖 |
| 复现Bug - 任意非ErrorException异常均触发永久Running（Outline 4 例） | @skip | TC-ERFIX-001（SocketTimeout/Http/Connect）、TC-ERFIX-011（NoSuchElement）反向覆盖 | ✅ 反向覆盖 |
| 修复后 - entrance短暂Full GC恢复后节点继续轮询并正常翻转 | @critical @smoke | TC-ERFIX-001 | ✅ |
| 修复后 - 连续失败期间失败计时正确累积与重置 | @bugfix | TC-ERFIX-002/003 | ✅ |
| 修复后 - entrance长期不可用超过阈值时节点翻Failed并告警 | @critical | TC-ERFIX-004/006 | ✅ |
| 修复后 - entrance返回未知状态串时映射为Running继续轮询 | @bugfix | TC-ERFIX-011 | ✅ |
| 回归 - entrance健康时普通节点状态翻转正常 | @regression @critical | TC-ERFIX-019 | ✅ |
| 回归 - 节点执行失败与人工取消场景正常 | @regression @critical | TC-ERFIX-019 | ✅ |
| 回归 - EmptyJob节点与EventSender节点不受影响 | @regression | TC-ERFIX-020 | ✅ |
| 回归 - 不配置任何新增配置项时服务行为兼容 | @regression | TC-ERFIX-022 | ✅ |
| 应急 - msg.after.send为false时kill重跑后旧信号可被消费 | @sop | TC-ERFIX-025 | ✅ |
| 应急 - msg.after.send为true时kill重跑后旧信号不被消费需重发 | @sop | TC-ERFIX-026 | ✅ |

**Feature 覆盖率**：10/10 可执行 Scenario（100%）；2 个 @skip 复现场景由修复后反向行为覆盖。设计文档 12 个单测点与 IT-01~IT-10 全部映射到上述用例（IT-09 配置调优属部署灰度项，由 TC-ERFIX-022 的 connection.timeout 语义校验部分覆盖，全量灰度验证随发布执行）。

## 8. 回归关注点（执行提示）

1. **正常路径零变更是硬约束**：TC-ERFIX-019/021/024 任何失败均为发布阻塞项
2. **R-07 专项**：TC-ERFIX-018 必须执行——FlowEntranceJob Timeout 分支遗漏会导致"翻 Timeout 但工作流仍挂起"的新形态问题
3. **日志关键字监控**：生产首周关注 `Escalate to Failed` 与 `exceeded max running time` 触发频次，异常升高需复核阈值（设计 7.3）
4. **加速验证等比外推**：所有阈值类用例支持等比调小加速，但发布前至少一轮按生产默认值（30min/2h）抽查关键路径
5. **回滚验证**：若发生误杀（正常节点批量翻 Failed/Timeout），按设计 7.2 回滚流程执行，本测试集 TC-ERFIX-014/019/021 可作为回滚后快速回归集

---

**文档版本**：v1.0
**创建日期**：2026-11-02
**用例总数**：26（P0×12 / P1×12 / P2×2）

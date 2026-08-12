# EC释放最小空闲时长门槛 测试执行报告

## 测试概览

| 项目 | 值 |
|-----|-----|
| **报告日期** | 2026-08-11 |
| **需求名称** | EC释放最小空闲时长门槛 |
| **需求编号** | REQ-DSS-1.23.0-002（本地变更，不入 DPMS） |
| **所属模块** | dss-framework-workspace-server-webank（EC 引擎释放） |
| **后端改动** | `ECInstanceReleaseExecuteTask.processStrategy` 候选 stream 新增 `.filter(unlockDuration >= minIdleMs)` 门槛（L194-201） |
| **前端改动** | `enginekill/index.vue` 表单项 + 卡片展示 + 数据流 4 处 |
| **DB 改动** | `dss_ec_release_strategy` 新增 `min_idle_minutes INT NOT NULL DEFAULT 0` |
| **测试环境** | 代码审查 + 设计推导（SIT 环境验证待执行） |

---

## 测试结果汇总

| 测试类型 | 用例总数 | 代码审查✅ | 待 SIT 验证⏳ | 待补单测⏳ |
|---------|:----:|:----:|:----:|:----:|
| 后端·候选门槛过滤 | 7 | 7 | — | 7 |
| 后端·持久化与默认值 | 3 | 3 | 3 | — |
| 前端·规则配置 | 5 | — | 5 | — |
| 回归补充 | 3 | — | 3 | — |
| **总计** | **18** | **10** | **11** | **7** |

> **说明**：本期未实际运行单元测试与 SIT 功能测试。上表"代码审查✅"指通过逐行走查源码 + 设计推导验证逻辑正确性，**不等同于已执行的自动化测试通过**。"待 SIT/单测"为后续需真实执行的验证项。如实标注，不编造执行结果。

---

## 详细测试结果（代码审查维度）

### 1. 后端·候选门槛过滤（逻辑走查）

| 用例ID | 覆盖场景 | 审查结论 | 代码依据 |
|-------|---------|:-------:|---------|
| TC01 | 门槛为0时候选集合与改动前一致 | ✅ | `minIdleMs <= 0` 时 `.filter` 第二参数恒真（L200），等价原行为 |
| TC02 | N=5空闲4min引擎不进候选池 | ✅ | `unlockDuration=240000 >= 300000` 为 false，被滤除 |
| TC03 | N=5空闲6min引擎仍在候选池 | ✅ | `unlockDuration=360000 >= 300000` 为 true，保留 |
| TC04 | 短空闲引擎不进入 kill 优先队列 | ✅ | filter 在 `ECKillingPriorityQueueFactory.getUserFairInstance`（L203）之前，短空闲引擎不进队列 |
| TC05 | 触发条件不满足时门槛无副作用 | ✅ | filter 位于 `if(satisfy(...triggerCondition...))`（L184）分支内，不触发则不执行 |
| TC06 | 分钟正确换算为毫秒 | ✅ | `minIdleMinutes * 60_000L`（L195），与 `unlockDuration`（毫秒）同量纲 |
| TC07 | 未知空闲时长引擎被保守剔除 | ✅ | `lastUnlockTimestamp` 缺失时 `convertECInstance2ECKillHistoryRecord` 置 0，`0 >= minIdleMs` 为 false 被滤除 |

### 2. 后端·持久化与默认值（代码审查 + 待集成）

| 用例ID | 覆盖场景 | 审查结论 | 待验证 |
|-------|---------|:-------:|:------|
| TC08 | 保存→读取往返值一致 | ✅ | 待 SIT：saveEcReleaseStrategy → getEcReleaseStrategyList 往返 |
| TC09 | DB NOT NULL DEFAULT 0，存量行回填 0 | ✅ | 待 SIT：执行 `dss_1.23.0_update.sql` 后查列定义与存量行 |
| TC10 | bean/DO 字段为 int 默认 0 | ✅ | 基本类型 JVM 默认即 0，类型级保证 |

### 3. 前端·规则配置（待 SIT）

| 用例ID | 覆盖场景 | 状态 |
|-------|---------|:----:|
| TC11 | 新增规则输入框默认显示 0 | ⏳ 待 SIT |
| TC12 | 输入框最小值 0，不可负 | ⏳ 待 SIT |
| TC13 | 卡片正确展示（0→不限 / N→N分钟） | ⏳ 待 SIT |
| TC14 | 编辑规则回填 DB 值 | ⏳ 待 SIT |
| TC15 | 新增/重置/回填三处默认 0 | ⏳ 待 SIT |

### 4. 回归补充（待 SIT）

| 用例ID | 覆盖场景 | 状态 |
|-------|---------|:----:|
| REG-1 | N=0 规则升级前后 kill 名单完全一致 | ⏳ 待 SIT |
| REG-2 | N=5 与 N=0 规则并存于不同队列互不干扰 | ⏳ 待 SIT |
| REG-3 | 跨集群（crossCluster=true）规则下门槛生效 | ⏳ 待 SIT |

---

## 验收标准覆盖情况

| 验收标准 | 覆盖用例 | 状态 |
|---------|---------|:----:|
| AC1.x 规则配置字段（默认0/非负/持久化/展示/回填） | TC08-TC15 | ⏳ 待 SIT 验证（前端 5 项 + 持久化往返） |
| AC2.1 门槛=0时候选集合与改动前一致 | TC01 | ✅ 代码审查 |
| AC2.2 空闲<N分钟引擎不在 kill 名单 | TC02, TC04, TC07 | ✅ 代码审查 |
| AC2.3 空闲≥N分钟引擎仍可被 kill | TC03 | ✅ 代码审查 |
| AC2.4 门槛过滤在优先队列之前 | TC04 | ✅ 代码审查 |
| AC2.5 触发条件不满足时门槛无副作用 | TC05 | ✅ 代码审查 |
| AC3.1 后端字段 int 无 null | TC10 | ✅ 代码审查 |
| AC3.2 DB NOT NULL DEFAULT 0 | TC09 | ✅ 代码审查 + 待 SIT 执行 DDL |
| AC3.3 前端三处默认 0 | TC15 | ⏳ 待 SIT |

---

## 未覆盖场景与原因

| 场景 | 原因 | 建议 |
|------|------|------|
| 后端单元测试（TC01-TC07 自动化） | `processStrategy` 为 `private` 方法且依赖 4 个 `@Autowired`（ResourceManageClient/Mapper/KillHistoryService/NotificationService），门槛 filter 为 inline stream 无法隔离；模块当前无 `src/test` 测试基线 | 重构门槛过滤为 package-private 静态方法后补单测（见下） |
| 前端功能测试（TC11-TC15） | 需 SIT 环境 + 规则编辑页 UI 交互 | SIT 部署后用前端动态测试执行 |
| 端到端 kill 流程（REG-1/2/3） | 依赖真实 Linkis 引擎实例与队列资源 | SIT 环境构造多空闲引擎验证 |

---

## 测试结论

- [x] ✅ **代码审查通过**：门槛过滤逻辑（位置、单位换算、默认0兼容、未知空闲保守剔除、与 LONG_TIME_THRESHOLD 正交）经逐行走查确认正确
- ⚠️ **待真实执行**：前端功能（5）、持久化往返（1）、回归端到端（3）需 SIT 环境部署后验证
- ⚠️ **自动化单测待补**：后端 7 项因可测性限制暂以代码审查代替，建议重构后补单测

### 改进建议

1. **短期（本期可选）**：将门槛过滤逻辑抽离为 package-private 静态方法，例如：
   ```java
   static boolean passIdleThreshold(ECKillHistoryRecord r, long minIdleMs) {
       return minIdleMs <= 0 || (r.getUnlockDuration() != null && r.getUnlockDuration() >= minIdleMs);
   }
   ```
   抽离后可对其写纯函数单测，覆盖 TC01/TC02/TC03/TC07 四个边界，无需 mock 整条调用链。
2. **中期**：SIT 部署 1.23.0 后，执行前端 TC11-TC15 与回归 REG-1/2/3，回填本报告真实结果。
3. **长期**：为 EC 释放模块建立 `src/test` 基线，逐步覆盖 `processStrategy` 全流程（需 Mockito mock Linkis 客户端）。

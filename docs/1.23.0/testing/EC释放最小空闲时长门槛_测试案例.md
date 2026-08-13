# 测试用例：EC 释放规则新增"最小空闲时长"门槛

> 需求名称：EC释放最小空闲时长门槛 | 类型：功能增强（ENHANCE）
> 设计文档：`docs/1.23.0/design/EC释放最小空闲时长门槛_设计.md`
> 本地变更（不入 DPMS），用例用于开发自测与回归。Feature 见 `docs/1.23.0/features/EC释放最小空闲时长门槛.feature`。

---

## 用例总览

| ID | 模块 | 标题 | 类型 | 关联 AC |
|----|------|------|------|---------|
| TC01 | 后端·兼容 | 默认 0 时候选集合与改动前一致 | 单元 | AC2.1 |
| TC02 | 后端·门槛 | N=5，空闲 4min 引擎不进候选池 | 单元 | AC2.2 |
| TC03 | 后端·门槛 | N=5，空闲 6min 引擎仍在候选池 | 单元 | AC2.3 |
| TC04 | 后端·门槛 | 短空闲引擎不进入 kill 优先队列 | 单元 | AC2.4 |
| TC05 | 后端·前置 | 触发条件不满足时门槛无副作用 | 集成 | AC2.5 |
| TC06 | 后端·单位 | 分钟正确换算为毫秒 | 单元 | AC2.2/2.3 |
| TC07 | 后端·未知空闲 | lastUnlockTimestamp 缺失的引擎被滤掉 | 单元 | AC2.2 |
| TC08 | 持久化 | 保存→读取往返值一致 | 集成 | AC1.3 |
| TC09 | DB·约束 | NOT NULL DEFAULT 0，存量行回填 0 | SQL | AC3.2 |
| TC10 | 类型·默认 | bean/DO 字段为 int，new 出来即 0 | 单元 | AC3.1 |
| TC11 | 前端·表单 | 新增规则输入框默认显示 0 | 功能 | AC1.1 |
| TC12 | 前端·约束 | 输入框最小值 0，不可负 | 功能 | AC1.2 |
| TC13 | 前端·展示 | 卡片正确展示（0→不限 / N→N分钟） | 功能 | AC1.4 |
| TC14 | 前端·回填 | 编辑规则回填 DB 值 | 功能 | AC1.5 |
| TC15 | 前端·默认 | 新增/重置/回填三处默认 0 | 功能 | AC3.3 |

---

## 后端用例

### TC01 默认 0 时候选集合与改动前一致
- **前置**：某规则 `minIdleMinutes=0`，队列空闲引擎集合 S（含各种空闲时长）。
- **步骤**：`processStrategy` 执行候选筛选。
- **预期**：候选集合 == S（门槛 `.filter` 恒真，未被剔除任何引擎）。
- **关联**：AC2.1。

### TC02 N=5，空闲 4min 引擎不进候选池
- **前置**：规则 `minIdleMinutes=5`；引擎 E 空闲 4*60*1000 ms，状态 Unlock，属该队列。
- **步骤**：`processStrategy` 筛选。
- **预期**：E 不在候选集合（`unlockDuration(240000) >= minIdleMs(300000)` 为 false）。
- **关联**：AC2.2。

### TC03 N=5，空闲 6min 引擎仍在候选池
- **前置**：规则 `minIdleMinutes=5`；引擎 E 空闲 6*60*1000 ms。
- **预期**：E 在候选集合（`360000 >= 300000` 为 true）。
- **关联**：AC2.3。

### TC04 短空闲引擎不进入 kill 优先队列
- **前置**：规则 `minIdleMinutes=5`；候选含 1 台空闲 6min + 1 台空闲 1min。
- **步骤**：触发条件成立，进入 while kill 循环。
- **预期**：只有 6min 引擎进入 `UserFairECKillingPriorityQueue`；1min 引擎未被 poll、未被加入 `ecInstanceKillRequestList`。
- **关联**：AC2.4。

### TC05 触发条件不满足时门槛无副作用
- **前置**：规则 `minIdleMinutes=5`；队列用量未达触发高水位。
- **预期**：`processStrategy` 不进入候选筛选分支（门槛 `.filter` 根本不执行），无引擎被 kill。
- **关联**：AC2.5。

### TC06 分钟正确换算为毫秒
- **前置**：规则 `minIdleMinutes=5`。
- **预期**：`minIdleMs = 5 * 60_000 = 300_000`，日志输出 `min idle duration threshold for killing: 300000 ms`。
- **关联**：AC2.2/2.3。

### TC07 lastUnlockTimestamp 缺失的引擎被滤掉
- **前置**：规则 `minIdleMinutes=5`；引擎 E 的 `lastUnlockTimestamp` 为 null/0。
- **预期**：`convertECInstance2ECKillHistoryRecord` 将其 `unlockDuration` 置 0；门槛 `0 >= 300000` false → E 被滤掉（保守：不知空闲多久的不杀）。
- **关联**：AC2.2。

### TC08 保存→读取往返值一致
- **前置**：新建/编辑规则填 `minIdleMinutes=10`。
- **步骤**：`saveEcReleaseStrategy` → 入库 → `getEcReleaseStrategyList`/`getStrategyByStrategyId` 读回。
- **预期**：读回的 `minIdleMinutes == 10`。
- **关联**：AC1.3。

### TC09 DB NOT NULL DEFAULT 0，存量行回填 0
- **步骤**：执行 `dss_1.23.0_update.sql` 的 ALTER。
- **预期**：① 列定义为 `NOT NULL DEFAULT 0`；② 存量所有行 `min_idle_minutes = 0`；③ `INSERT` 不带该列时自动写 0；④ 显式插 NULL 报错（违反 NOT NULL）。
- **关联**：AC3.2。

### TC10 bean/DO 字段为 int，new 出来即 0
- **步骤**：`new ECReleaseStrategy()` / `new ECReleaseStrategyDO()` 后读 `getMinIdleMinutes()`。
- **预期**：返回 0（基本类型默认），无 NPE 可能。
- **关联**：AC3.1。

---

## 前端用例（功能）

### TC11 新增规则输入框默认显示 0
- **步骤**：规则列表 → 点"新增" → 打开弹窗。
- **预期**："最小空闲时长(分钟)"输入框显示 0。
- **关联**：AC1.1。

### TC12 输入框最小值 0，不可负
- **步骤**：尝试输入 -1 / 按下调箭头。
- **预期**：值钳制在 0（`InputNumber :min="0"`）。
- **关联**：AC1.2。

### TC13 卡片正确展示
- **步骤**：规则 A（minIdleMinutes=0）、规则 B（minIdleMinutes=8）保存后查看列表。
- **预期**：A 显示"最小空闲时长(分钟)：不限"；B 显示"最小空闲时长(分钟)：8分钟"。
- **关联**：AC1.4。

### TC14 编辑规则回填 DB 值
- **步骤**：打开规则 B（DB 中 min_idle_minutes=8）的编辑弹窗。
- **预期**：输入框显示 8。
- **关联**：AC1.5。

### TC15 新增/重置/回填三处默认 0
- **核查点**：`data().formData.minIdleMinutes=0`、`add()` 重置 `=0`、`edit()` 回填 `item.minIdleMinutes || 0`。
- **预期**：任一路径下，未配置时字段恒为 0。
- **关联**：AC3.3。

---

## 回归补充（建议）

- **REG-1**：N=0 的规则在升级前后，对同一队列同一时刻的 kill 名单完全一致（端到端兼容）。
- **REG-2**：N=5 规则与 N=0 规则并存于不同队列，互不干扰。
- **REG-3**：跨集群（crossCluster=true）规则下门槛同样生效。

---

## 备注

- 后端单测可对 `processStrategy` 的候选筛选段做切片测试（mock `fetchECInstance` 返回带不同 `unlockDuration` 的记录），无需真实 Linkis。
- 前端功能用例可用 `frontend-dynamic-tester` 在规则编辑页执行（参考 `docs/regression-test/` 既有 Feature 模式）。

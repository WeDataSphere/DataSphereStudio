# 设计方案：EC 释放规则新增"最小空闲时长"门槛

> 需求名称：EC释放最小空闲时长门槛 | 类型：功能增强（ENHANCE）
> 需求文档：`docs/1.23.0/requirements/EC释放最小空闲时长门槛_需求.md`
> 技术栈：JDK8 / Spring Boot 2.7.18 / MyBatis / Vue.js + iView / MySQL

---

## 一、设计概述

### 1.1 改造范围

| 维度 | 结论 |
|-----|------|
| 改动端 | 后端 + 前端 + DB |
| 后端文件 | 4 个（bean / DO / Mapper.xml / 执行器） |
| DB | 1 列新增（建表 DDL + 升级脚本） |
| 前端文件 | 3 个（enginekill/index.vue + zh.json + en.json） |
| 是否破坏存量 | 否（默认 0 = 不限） |
| 是否修竞态 | 否（范围外，见 §6） |

### 1.2 设计原则

1. **最小侵入**：复用现有候选筛选 stream 的 post-filter 模式（与 `queueName` 过滤同位置）。
2. **默认兼容**：0 = 不限时门槛恒真，老规则零影响。
3. **类型固化**：用基本类型 `int` + DB `NOT NULL DEFAULT 0`，从类型层保证默认 0、杜绝 null。

---

## 二、数据模型变更

### 2.1 新增字段

| 层 | 字段 | 类型 | 默认 |
|----|------|------|------|
| bean `ECReleaseStrategy` | `minIdleMinutes` | `int` | 0 |
| DO `ECReleaseStrategyDO` | `minIdleMinutes` | `int` | 0 |
| DB `dss_ec_release_strategy` | `min_idle_minutes` | `INT(11) NOT NULL DEFAULT 0` | 0 |

> **类型选择**：用 `int` 而非 `Integer`。基本类型 JVM 默认即 0、不可为 null，配合 DB `NOT NULL`，实现"默认 0"的类型级保证，对齐同类字段 `status`/`crossCluster`。`ECReleaseStrategy.toDO/fromDO` 内部用 `BeanUtils.copyProperties`，同名字段自动贯通，无需特殊处理。

### 2.2 DDL

**基础建表**（`db/dss_webank_ddl.sql`，`cross_cluster` 之后）：
```sql
`min_idle_minutes`   int(11) NOT NULL DEFAULT 0 COMMENT '最小空闲时长(分钟),引擎空闲达到该值才允许被回收,0为不限',
```

**升级脚本**（`db/version-update/dss_1.23.0_update.sql`，新建）：
```sql
ALTER TABLE dss_ec_release_strategy
  ADD COLUMN min_idle_minutes int(11) NOT NULL DEFAULT 0
  COMMENT '最小空闲时长(分钟),引擎空闲达到该值才允许被回收,0为不限'
  AFTER cross_cluster;
```
> MySQL 对 `ADD COLUMN ... NOT NULL DEFAULT 0` 会把存量行回填为 0，兼容已有规则。

### 2.3 Mapper（`WebankDSSWorkspaceECReleaseStrategyMapper.xml`）

三处贯通：

**(a) 字段片段**（供所有 SELECT 使用）：
```xml
<sql id="ec_release_strategy_field">
    strategy_id, workspace_id, name, description, queue, cross_cluster,
    min_idle_minutes,            <!-- 新增 -->
    trigger_condition_conf, terminate_condition_conf, ims_conf, status,
    creator, create_time, modifier, modify_time, execute_instance, execute_time
</sql>
```

**(b) insertStrategy VALUES**（位置与字段片段对齐，17 列 ↔ 17 值）：
```xml
#{queue},
#{crossCluster},
#{minIdleMinutes,jdbcType=INTEGER},   <!-- 新增 -->
#{triggerConditionConf},
```

**(c) updateStrategy `<set>`**：
```xml
<if test="minIdleMinutes != null">
    min_idle_minutes = #{minIdleMinutes},
</if>
```
> 注：字段为基本类型 `int`，OGNL `minIdleMinutes != null` 恒真，UPDATE 总会带上该列（符合预期——始终持久化当前值）。

---

## 三、核心算法：候选门槛过滤

### 3.1 改造点

`ECInstanceReleaseExecuteTask.processStrategy(...)`，触发条件成立后的候选 stream。

**改造前**：
```java
List<ECKillHistoryRecord> ecInstanceList = resourceManageClient.fetchECInstance(ecInstanceRequest, operator).stream()
    .filter(e -> e.getUseResource() != null && e.getUseResource().getYarn() != null
              && queueName.equals(e.getUseResource().getYarn().getQueueName()))
    .map(e -> ECKillHistoryRecord.convertECInstance2ECKillHistoryRecord(e, strategy.getWorkspaceId(), strategy.getStrategyId(), operator))
    .collect(Collectors.toList());
```

**改造后**：
```java
// 空闲时长门槛：0 = 不限（默认）
int minIdleMinutes = strategy.getMinIdleMinutes();
long minIdleMs = minIdleMinutes <= 0 ? 0L : minIdleMinutes * 60_000L;
LOGGER.info("min idle duration threshold for killing: {} ms", minIdleMs);
List<ECKillHistoryRecord> ecInstanceList = resourceManageClient.fetchECInstance(ecInstanceRequest, operator).stream()
    .filter(e -> e.getUseResource() != null && e.getUseResource().getYarn() != null
              && queueName.equals(e.getUseResource().getYarn().getQueueName()))
    .map(e -> ECKillHistoryRecord.convertECInstance2ECKillHistoryRecord(e, strategy.getWorkspaceId(), strategy.getStrategyId(), operator))
    .filter(r -> minIdleMs <= 0 || (r.getUnlockDuration() != null && r.getUnlockDuration() >= minIdleMs))  // 新增门槛
    .collect(Collectors.toList());
```

### 3.2 关键设计点

| 点 | 说明 |
|----|------|
| 过滤在 `.map` 之后 | `unlockDuration` 在 `convertECInstance2ECKillHistoryRecord`（L137-142）内由 `lastUnlockTimestamp` 计算，`.map` 前的 `ECInstance` 上无此字段 |
| 过滤在优先队列之前 | 短空闲引擎不进入 `UserFairECKillingPriorityQueue`，不参与排序、不占名额 |
| `minIdleMs <= 0` 恒真分支 | 0 = 不限时直接放行，等价原行为 |
| `unlockDuration` 为 null/0 的处理 | `lastUnlockTimestamp` 缺失时 `convertECInstance2ECKillHistoryRecord` 置 0；N>0 时这些引擎被 `>= minIdleMs` 滤掉（"不知空闲多久的不杀"，保守正确） |

### 3.3 单位换算

- DB 存分钟（用户友好）。
- `minIdleMinutes * 60_000L` → 毫秒。
- 与 `ECKillHistoryRecord.unlockDuration`（毫秒，见 bean L61）同量纲比较。

---

## 四、前端改造（`enginekill/index.vue`）

### 4.1 表单项（Modal 内，"关联队列"之后、"触发条件"之前）

```html
<FormItem :label="$t('message.enginelist.ruleform.minIdleMinutes')" prop="minIdleMinutes">
  <InputNumber v-model="formData.minIdleMinutes" :min="0" :max="10080" :step="1"
    style="width:150px" :placeholder="$t('message.enginelist.ruleform.minIdleMinutesTip')" />
  <span style="margin-left:8px;color:#888;font-size:12px;">
    {{$t('message.enginelist.ruleform.minIdleMinutesTip')}}
  </span>
</FormItem>
```

### 4.2 卡片展示（"是否跨集群"行之后）

```html
<div class="row-item">
  {{$t('message.enginelist.ruleform.minIdleMinutes')}}：{{ item.minIdleMinutes > 0
    ? item.minIdleMinutes + $t('message.enginelist.ruleform.minuteUnit')
    : $t('message.enginelist.ruleform.unlimited') }}
</div>
```

### 4.3 数据流（4 处）

| 位置 | 代码 |
|------|------|
| `data().formData` 默认 | `minIdleMinutes: 0` |
| `add()` 重置 | `minIdleMinutes: 0` |
| `saveRule()` 入参 | `minIdleMinutes: this.formData.minIdleMinutes` |
| `edit()` 回填 | `minIdleMinutes: item.minIdleMinutes \|\| 0`（老规则兜底 0） |

### 4.4 i18n（zh.json + en.json，ruleform 段各加 4 key）

| key | zh | en |
|-----|----|----|
| `minIdleMinutes` | 最小空闲时长(分钟) | Min idle time (min) |
| `minIdleMinutesTip` | 引擎空闲达到该时长才允许被回收，0表示不限 | An engine can be killed only after being idle for this long; 0 means no limit |
| `unlimited` | 不限 | Unlimited |
| `minuteUnit` | 分钟 | min |

---

## 五、与 `LONG_TIME_THRESHOLD` 的分层关系

两者职责正交，叠加成完整策略：

```
候选池(门槛过滤后) ──┐
                    ├──► UserFairECKillingPriorityQueue ──► 按 LONG_TIME_THRESHOLD(5min) 分组排序 ──► 逐个 kill
   [资格层]                                                       [排序层]
   minIdleMinutes                                            LONG_TIME_THRESHOLD
   "能否被杀"                                                "先杀谁"
```

- **N ≤ 5min**：N 比阈值严，入池前挡掉一批，剩余按 5min 分组排序。
- **N > 5min**：入池引擎空闲都 > N ≥ 5min，优先队列的 shortTimeList 恒空，只在阶段2运转；`LONG_TIME_THRESHOLD` 不冲突、仅冗余。
- **N = 0**：门槛恒真，`LONG_TIME_THRESHOLD` 按原状排序，老行为零变化。

---

## 六、竞态边界（本任务不修，仅说明）

### 6.1 已识别竞态
fetch 时引擎 Unlock → fetch 与 batchKillECInstance 之间，引擎被新任务锁成 Busy → DSS 仍按名字 kill → 任务失败（TOCTOU）。DSS 侧 fetch 返回快照、kill 只传 instance 名，中间无重校验。

### 6.2 本门槛的作用（概率性缓解，非修复）
- 长空闲引擎近期需求稀疏，kill 窗口内被复用概率较低 → 统计上降低误杀。
- **但在触发条件成立（队列繁忙）时最不靠谱**：任务到达率最高，门槛效力最弱。

### 6.3 真正修复（独立后续工作，不在本任务）
- Linkis 侧 `enginekillAsyn` 做 compare-and-set："仅当仍 Unlock 才杀"。
- 或 DSS kill 前重 fetch 按状态过滤（治标，缩窗）。
- 或 kill 请求体加 `expectedStatus=Unlock`（语义最清晰）。

---

## 七、改动文件清单

| 文件 | 改动 |
|------|------|
| `ECReleaseStrategy.java` | + 字段 `int minIdleMinutes` + getter/setter |
| `ECReleaseStrategyDO.java` | + 字段 `int minIdleMinutes` + getter/setter |
| `WebankDSSWorkspaceECReleaseStrategyMapper.xml` | 字段片段 + insert + update 三处 |
| `ECInstanceReleaseExecuteTask.java` | processStrategy 候选 stream 加 `.filter` 门槛 |
| `db/dss_webank_ddl.sql` | 建表加 `min_idle_minutes` 列 |
| `db/version-update/dss_1.23.0_update.sql` | 新建，ALTER TABLE 加列 |
| `web/.../enginekill/index.vue` | 表单项 + 卡片展示 + 数据流 4 处 |
| `web/.../i18n/zh.json`、`en.json` | 各 +4 key |

## 八、验证

- 后端编译：`mvn -o -q compile -pl dss-framework/dss-framework-workspace-server-webank -am` → **exit=0**（含 `int` 改型后二次编译）。
- 残留检查：`Integer minIdleMinutes` 全仓 0 处。
- i18n JSON：`JSON.parse` 校验通过。
- Mapper 对齐：字段片段 17 列 ↔ INSERT VALUES 17 个，逐一对应。

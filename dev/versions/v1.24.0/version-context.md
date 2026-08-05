# DSS v1.24.0 版本上下文

> **创建时间**：2026-07-07
> **操作人**：burdezhang(张旗)
> **版本语义**：A（正式开版）— 1.24.0 作为新版本，bump pom、在 DPMS 新建发布计划与子系统版本、把 1.23.0 未完结项结转到 1.24.0
> **版本索引**：`dev/versions/versions.json`

---

## 一、版本基线

| 维度 | 值 | 说明 |
|------|----|------|
| 版本号 | `1.24.0`（目标） | pom 当前仍为 `1.23.0-SNAPSHOT`，待 bump |
| 分支 | `dev-1.24.0` | 当前 HEAD = `3a26a2f7ae8b366a32f9f031cb94ee9d25d00700` |
| 主分支 | `master` | HEAD = `f4d00d1064cd016b13d1db406c0d446d78f8d64c` |
| 源分支 | `dev-1.23.0` | dev-1.24.0 从 dev-1.23.0 当前 HEAD 拉出 |
| 独有提交（vs dev-1.23.0） | **0** | dev-1.24.0 == dev-1.23.0，零差异 |
| master..dev-1.24.0 | 4902（历史累积） | 非 1.24.0 独有 |
| 本地 vs origin/dev-1.24.0 | 同步 | 本地已推送远端 |
| 工作树 | 仅未跟踪文件 | 无已修改跟踪文件 |

### pom 版本现状（漂移）

- 根 `pom.xml`：`<version>1.23.0-SNAPSHOT</version>` + `<dss.version>1.23.0-SNAPSHOT</dss.version>`
- 全项目 **105 个 pom.xml** 含 `1.23.0-SNAPSHOT` 字面量（含 `<parent><version>` 硬编码引用）
- linkis.version / scala.version / spring.version / spring.boot.version 均为第三方版本，**不随 dss 版本走**，bump 时不涉及

---

## 二、纳入项清单（carried，从 v1.23.0 基线带入）

> 这 9 项已在 dev-1.23.0 合入，因 dev-1.24.0 == dev-1.23.0 HEAD，代码上也存在于 dev-1.24.0。
> **DPMS story 归属待定**：若已在 1.23.0 发布计划登记，则无需在 1.24.0 重复登记；若未登记，需决策归属。

| # | taskId | 主题 | 类型 | 关键 commit | DPMS story |
|---|--------|------|------|------------|-----------|
| 1 | sendemail-feishu-option | sendemail节点新增是否发送飞书选项 | feature | `1e471b7f2` `91d88ef92` `3725f5f1c` `0b14e72e9` `686d6bf20` `7a4f953f5` `211c1799e` `0f602e773` `3a26a2f7a` | 待确认 |
| 2 | user-exists-check-api | 新增接口：判断用户是否在DSS中存在 | feature | `873af6b83` | 待确认 |
| 3 | log4j-upgrade-2.25.4 | log4j版本升级到2.25.4 | dependency_governance | `7d49a2feb` | 待确认（#ignore#） |
| 4 | project-workflow-whitelist-api | 新增接口：判断项目/工作流是否在白名单 | feature | `63f2c4cb2` `2c9f3ef09` | 待确认 |
| 5 | fix-branch-node-skip-downstream | 修复：分支节点上游全跳过后下游不执行 | bugfix | `54f8c3353` | 待确认 |
| 6 | fix-batch-edit-unlock-workflow | 修复：批量编辑抛异常后解锁工作流 | bugfix | `eb8ceba75` | 待确认 |
| 7 | optimize-email-pdf-attachment-name | 优化：邮件pdf附件名称 | optimization | `72cfd66bf` | 待确认 |
| 8 | fix-itsm-field-adaptation | 修复：ITSM接口字段适配 | bugfix | `153408e03` | 待确认 |
| 9 | batch-edit-tableau-viewid | 批量编辑tableau节点视图ID属性 | feature | `76bc582d9` `f72300c82` | 待确认 |

---

## 三、结转项（carryover，1.24.0 待重做）

| # | taskId | 主题 | 原 commit | Revert commit | 说明 |
|---|--------|------|-----------|---------------|------|
| 1 | fix-empty-workflow-npe | 修复空工作流NP异常 | `8d46547ee` | `9e77c8b3a` | 1.23.0 被显式 Revert，**修复未生效**；若用户线上遇到空工作流仍会触发 NPE。需在 1.24.0 重新拉起需求/设计/实现。 |

---

## 四、已排除主题（1.23.0 多轮 Revert，未进入纳入项）

| 主题 | Revert commits | 1.24.0 是否重做 |
|------|----------------|----------------|
| 新增接口用于修改工作流全局变量(updateGlobalVariables) | `05eddc79a` `ec3045c87` `f81bc852e` | 待用户决策 |
| 工作流修改tenant变量 | `c7674b9a5` `611a3977c` `71f46f775` `932b8b38a`(Merge Revert) | 待用户决策 |

---

## 五、阻塞项（DPMS 写操作全部阻塞）

### DPMS 实体（2026-07-07 已创建）

| 实体 | ID | 详情 |
|------|----|------|
| 产品 (productId) | 100199 | BDP_DSS / DataSphereStudio |
| 发布计划 | **177587** | DSS1.24.0，type=1 常规版本，[详情页](http://dpms.weoa.com/index.html#/product/100199/release/detail/177587) |
| 发布计划日期 | — | 提测 2026-07-21 / UAT 2026-08-04 / 投产 2026-08-25 / 结束 2026-08-29 |
| 子系统版本 DSS-IDE | subSystemId=5425, versionRecordId=263559 | 意书，1.24.0，[详情](http://scm.et.weoa.com/#/subsystem-version/detail/5425/263559) |
| 子系统版本 DSS-WFS | subSystemId=5433, versionRecordId=263560 | 工作流前端服务，1.24.0，[详情](http://scm.et.weoa.com/#/subsystem-version/detail/5433/263560) |
| 运维负责人 (opsUserName) | falinhe | — |
| 测试负责人 (testUserName) | stacyyan | — |

### DPMS 对齐发现（2026-07-07 只读查询）

- DSS1.23.0 发布计划 (id=175845, status=3) 有 7 个 story，但 git 已合入的 9 项功能中**仅 1 项**（用户存在性接口 story id=513702）已登记，8 项缺口
- DPMS 1.23.0 有 6 个已登记但 git 未实现的需求（内存泄漏/代理用户续期/离职用户共享目录/异常弹窗/Hadoop配置/Tableau推飞书）
- 空工作流 NP 修复（结转项）未登记为 story 或 bug

### 当前阻塞项：无

productId / releasePlanId / 子系统版本均已就绪，后续 story 登记/关联/结转可执行（待用户授权）。

---

## 六、待办清单

| 序号 | 动作 | 类型 | 状态 |
|------|------|------|------|
| T1 | DPMS 只读对齐（productId/subSystem 确认） | 只读 | ✅ done（productId=100199，1.23.0 id=175845） |
| T2 | 版本语义决策 | 决策 | ✅ 已决（A） |
| T3 | pom bump 1.23.0-SNAPSHOT → 1.24.0-SNAPSHOT | 本地代码 | ✅ done（105 文件，未 commit） |
| T4 | 初始化本地版本资产 | 本地文件 | ✅ done（versions.json + 本文件） |
| T5 | DPMS 创建 1.24.0 发布计划 + 子系统版本 | DPMS 写 | ✅ done（releasePlanId=177587，DSS-IDE/DSS-WFS 已建） |
| T6 | 结转 fix-empty-workflow-npe 到 1.24.0 | DPMS 写 + 代码 | 待授权（releasePlanId 已就绪） |
| T7 | 提交 sendemail 飞书测试源 | git commit | 用户暂缓 |
| T8-A | 1.23.0 的 8 项未登记已合入功能补登记 | DPMS 写 | 待决策（归属 1.23.0 / 1.24.0 / 免登记） |
| T8-B | 1.23.0 的 6 项未实现需求结转到 1.24.0 | DPMS 写 | 待决策（结转 / 保留 / 部分结转） |
| T9 | pom bump + 本地资产 git 合并提交 | git commit | 待用户指示 |

---

## 七、版本状态

- **status**: `development`
- **started**: `false`（尚未启动需求执行流程）
- **dpmsReady**: `true`（发布计划 177587 + 子系统版本 DSS-IDE/DSS-WFS 已创建）
- **pomBumped**: `true`（1.24.0-SNAPSHOT，105 文件，未 commit）
- **overallProgress**: 约 95%（基线 9 项已合入 + pom bump + DPMS 实体创建；剩 story 登记/结转/封板）
- **nextAction**: 等待用户决策 T8-A（8 项未登记功能归属）/ T8-B（6 项未实现需求结转）/ T6（空工作流NP登记）/ T9（pom bump 合并提交）

---

**本文件由 `/dev-flow version` 管理路径生成。T1/T3/T4/T5 已完成，DPMS 实体就绪。后续写操作待用户授权。**

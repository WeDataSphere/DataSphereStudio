# DSS 1.23.0 版本进度报告

> **生成方式**：本地只读探查（git log / git show / 文件系统）
> **生成时间**：2026-07-07
> **分支**：`dev-1.23.0`
> **操作人**：burdezhang(张旗)
> **执行范围声明**：本报告由 `/dev-flow version` 只读路径生成，**未调用任何 DPMS 写入 MCP**，未创建/关联/登记任何 DPMS 实体。DPMS 侧对齐状态需后续提供 `productId` 后通过只读查询补全。

---

## 一、版本基线

| 维度 | 状态 | 说明 |
|------|------|------|
| 项目版本号 | `1.23.0-SNAPSHOT` | 根 `pom.xml` 已对齐 1.23.0 迭代 |
| 当前分支 | `dev-1.23.0` | 与版本号一致，开发分支 |
| 主分支 | `master` | 用于 PR 的默认目标分支 |
| 相对 master 领先提交数 | 约 4901 | 含历史累积，非 1.23.0 独有；1.23.0 独有提交见下文纳入项清单 |
| 本地版本管理目录 | 首次建立 | 本次新建 `dev/versions/v1.23.0/` |
| 本地版本配置 `versions.json` | 不存在 | 尚未初始化版本配置登记 |
| DPMS 发布计划 | 未知 | 需提供 `productId` 后通过 `mcp__sdp__get-release-plans` 只读查询确认 |
| DPMS 子系统版本 | 未知 | 同上，需 `productId` + `subSystemId` |

### 技术栈对齐（来自根 pom.xml）

- JDK 8
- Scala 2.11.12
- Spring Boot 2.7.18 / Spring 5.3.34
- Linkis 1.18.3-wds
- Maven 构建

---

## 二、纳入项清单（从 git log 提炼，按落地状态排序）

> 落地状态图标说明：✅已合入 dev-1.23.0 / ⛔已 Revert / ⚠️部分或需关注

### 2.1 ✅ 已合入的有效功能主题（9 项）

| # | 功能主题 | 关键 commit hash | 模块归属 | 状态 |
|---|---------|-----------------|---------|------|
| 1 | **sendemail 节点新增"是否发送飞书"选项** | `1e471b7f2` `211c1799e` `91d88ef92` `3725f5f1c` `0b14e72e9` `686d6bf20` `7a4f953f5` | `dss-appconn/appconns/dss-sendemail-appconn/sendemail-appconn-core/`（新增 `feishu/` 包：FeishuClient / FeishuConfig / FeishuMessageSender；改 Email/AbstractEmail/AbstractEmailGenerator/SendEmailRefExecutionOperation）+ `db/dss_dml.sql` + `appconn.properties` + `init.sql` | ✅ 多次迭代已合入，含 Pace+ 流水线自动部署 commit `211c1799e` |
| 2 | **新增接口：判断用户是否在 DSS 中存在** | `873af6b83` | `dss-framework/dss-framework-workspace-server/.../restful/DSSWorkspaceUserRestful.java` | ✅ 已合入 |
| 3 | **log4j 版本升级到 2.25.4** | `7d49a2feb` | 根 `pom.xml` + `dss-framework/dss-framework-project-server-webank/pom.xml` | ✅ 已合入（提交备注含 `#ignore#`，属依赖治理） |
| 4 | **新增接口：判断项目/工作流是否在白名单** | 代码 `63f2c4cb2`；文档 `2c9f3ef09` | 代码：`dss-framework/dss-framework-orchestrator-server/.../DSSFrameworkOrchestratorRestful.java` + `dss-workflow/dss-workflow-server/.../dao/ProjectOrchestratorWhiteMapper(.java/.xml)` + `service/ProjectOrchestratorWhiteService(Impl)`；文档：`docs/1.23.0/` 下白名单接口设计/需求/feature/测试案例/wemind 导入 | ✅ 已合入 |
| 5 | **修复：分支节点造成的 bug（上游节点都被跳过后下游节点不执行）** | `54f8c3353` | `dss-orchestrator/.../resolver/FlowDependencyResolverImpl.scala` | ✅ 已合入 |
| 6 | **修复：批量编辑抛异常后解锁工作流** | `eb8ceba75` | `dss-workflow/.../service/impl/DSSFlowServiceImpl.java` | ✅ 已合入 |
| 7 | **优化：邮件 pdf 附件名称** | `72cfd66bf` | `dss-appconn/appconns/dss-sendemail-appconn/sendemail-appconn-core/.../parser/PictureEmailContentParser.scala` | ✅ 已合入 |
| 8 | **修复：ITSM 接口字段适配** | `153408e03` | `dss-apps/dss-scriptis-server/.../bean/ItsmRequest.java` + `dss-framework/dss-framework-workspace-server/.../bean/itsm/ItsmRequest.java` | ✅ 已合入 |
| 9 | **批量编辑 tableau 节点视图 ID 属性** | `76bc582d9` `f72300c82` | `dss-workflow/.../entity/request/EditFlowRequest.java` + `dss-workflow/.../service/impl/DSSFlowServiceImpl.java` | ✅ 已合入（两次提交） |

### 2.2 ⛔ 已 Revert 的功能主题（不计入纳入项，但需关注回退是否彻底）

| # | 功能主题 | 原始 commit | Revert commit | 模块归属 | 状态 |
|---|---------|------------|--------------|---------|------|
| R1 | ~~新增接口用于修改工作流全局变量（updateGlobalVariables）~~ | `d8bb2996a` `5968f05d2` `3ae5a8ec2` `6262e675f` `f6887deeb` `fc623db6d` | `05eddc79a` `ec3045c87` `f81bc852e` | 工作流全局变量修改接口 | ⛔ 经多轮 Revert 回退，最终未进入 1.23.0 纳入项 |
| R2 | ~~工作流修改 tenant 变量~~ | `aa4ab497c` `0d7e6b07e` `fb1819c92` `c9b016d06` | `c7674b9a5` `611a3977c` `71f46f775` `932b8b38a`（Revert Merge） | 工作流 tenant 变量修改 | ⛔ 经多轮 Revert 回退（含一次 Merge Revert），最终未进入 1.23.0 纳入项 |
| R3 | ~~修复：空工作流 NP 异常~~ | `8d46547ee` | `9e77c8b3a` | `dss-workflow/.../service/impl/DSSFlowServiceImpl.java` | ⛔ 已被显式 Revert，**该修复未生效**，需确认是否计划在 1.23.0 重新提交 |

### 2.3 其他近期但非 1.23.0 主题的提交（参考）

| commit | 主题 | 说明 |
|--------|------|------|
| `dfcd9d7e3` | 添加 1.22.0 版本文档 | 历史版本文档，非 1.23.0 纳入项 |
| `d31c99a56` `b76b4588f` | upgrade mvn version | 构建版本号治理 |
| `178156520` | 升级 linkis 版本 1.18.3 | 依赖升级，已包含在基线 |

---

## 三、工作树与文档资产状态

### 3.1 当前未提交改动

```
 M dss-appconn/appconns/dss-sendemail-appconn/sendemail-appconn-core/pom.xml   # 新增 junit 4.12 test 依赖
?? dss-appconn/appconns/dss-sendemail-appconn/sendemail-appconn-core/src/test/  # 新增测试源码目录
?? docs/1.23.0/testing/reports/                                                 # 测试报告目录
?? docs/regression-test/                                                        # 回归测试资产
?? docs/project-analysis/  docs/project-knowledge/                             # 项目分析/知识库
?? .claude/  .claude.zip  .mcp.json  memory/  tests/  node_modules/  package.json  current_cookies.json
```

**说明**：`sendemail-appconn-core/pom.xml` 的改动是为 sendemail 飞书功能补充测试依赖（junit 4.12），与主题 #1 配套，但尚未提交。

### 3.2 `docs/1.23.0/` 已沉淀的文档资产

```
docs/1.23.0/
├── design/        sendemail飞书发送_设计.md
├── features/      sendemail飞书发送.feature
├── requirements/  sendemail飞书发送_需求.md
└── testing/
    ├── sendemail飞书发送_测试案例.md
    ├── reports/sendemail飞书发送_测试执行报告.md
    └── wemind/sendemail飞书发送_wemind导入.json
```

**观察**：当前 `docs/1.23.0/` 下只对 **sendemail 飞书发送** 这一个主题沉淀了完整的需求/设计/feature/测试资产。其余 8 个已合入主题（白名单接口、用户存在性判断、分支节点 bug 修复、ITSM 适配等）**未在 `docs/1.23.0/` 下建立对应文档资产**（白名单接口在 commit `2c9f3ef09` 中曾新增文档，但当前工作树 `docs/1.23.0/` 下未检出，可能已清理或路径不同，建议复核）。

---

## 四、DPMS 对齐缺口

> ⚠️ 本节为缺口标注，**未执行任何 DPMS 查询**。以下所有"DPMS 侧状态"均为"未知"，需提供 `productId` 后通过只读查询确认。

| 缺口项 | 当前已知 | 待补全方式 |
|--------|---------|-----------|
| DPMS 是否存在 1.23.0 发布计划 | 未知 | 提供 `productId` → 调用 `mcp__sdp__get-release-plans(name="1.23.0")` 只读查询 |
| DPMS 是否存在 1.23.0 子系统版本 | 未知 | 提供 `productId` + `subSystemId` → 调用 `mcp__dpms__get_sub_system_version_list` 只读查询 |
| 9 项已合入功能是否已登记为 DPMS 系统需求（story） | 未知 | 提供 `productId` → 调用 `mcp__sdp__get-storys(name="<主题>")` 只读查询 |

### 4.1 待补登记的候选 story（DPMS 侧可能未登记）

基于 git log 已落地但 `docs/1.23.0/` 缺文档资产的 8 个主题，若 DPMS 侧同样未登记，则为候选补登记项：

1. 新增接口：判断用户是否在 DSS 中存在（`873af6b83`）
2. 新增接口：判断项目/工作流是否在白名单（`63f2c4cb2`）
3. log4j 版本升级到 2.25.4（`7d49a2feb`）
4. 修复：分支节点 bug——上游节点都被跳过后下游节点不执行（`54f8c3353`）
5. 修复：批量编辑抛异常后解锁工作流（`eb8ceba75`）
6. 优化：邮件 pdf 附件名称（`72cfd66bf`）
7. 修复：ITSM 接口字段适配（`153408e03`）
8. 批量编辑 tableau 节点视图 ID 属性（`76bc582d9` `f72300c82`）

> 这些是否需要登记为正式 story，取决于团队对"依赖治理/bugfix 是否纳入版本纳入项"的口径，需你决策。

---

## 五、风险与建议

### 5.1 风险

| 风险 | 影响 | 建议 |
|------|------|------|
| **R3 空工作流 NP 异常修复被 Revert** | 该 bugfix（`8d46547ee`）被 `9e77c8b3a` 显式回退，**修复未生效**；若用户线上遇到空工作流仍会触发 NPE | 确认是否计划在 1.23.0 重新提交修复；若是，需重新拉起该 bugfix 的需求/设计/实现 |
| **R1/R2 tenant 变量与全局变量修改接口被多轮回退** | 涉及 13+ 个 commit 的反复，最终未进入纳入项；存在 Merge Revert（`932b8b38a`）可能遗留部分代码的风险 | 建议在版本封板前做一次代码检索（grep `updateGlobalVariables` / `tenant`），确认工作树中无残留半成品代码 |
| **本地版本目录缺失** | 无 `dev/versions/versions.json`，无法追踪版本纳入项与 story 对应关系 | 本次已建立 `dev/versions/v1.23.0/`，建议后续在授权后初始化 `versions.json` 登记纳入项 |
| **文档资产覆盖不全** | 9 个已合入主题中仅 1 个（sendemail 飞书）有完整文档资产，其余 8 个缺需求/设计/测试文档 | 评估是否需要为其余主题补文档，或按团队规范认定"bugfix/小优化免文档" |
| **未提交改动** | `sendemail-appconn-core/pom.xml` + `src/test/` 未提交，是 sendemail 飞书功能的测试依赖配套 | 建议尽快提交，避免遗漏 |

### 5.2 下一步可选动作（均需你明确授权后才执行）

| 动作 | 说明 | 所需授权 |
|------|------|---------|
| **A. 只读补全 DPMS 状态** | 提供 `productId` 后，调用 `get-release-plans` / `get-storys` / `get_sub_system_version_list` 只读查询，补全第四节"DPMS 对齐缺口" | 仅需提供 `productId`（只读，无写入） |
| **B. 初始化本地版本配置** | 创建 `dev/versions/versions.json`，登记 1.23.0 的 9 个纳入项 + 3 个 Revert 项 | 本地文件操作，无需 DPMS 授权 |
| **C. DPMS 写入操作（创建/关联/登记）** | 在 DPMS 新建发布计划、子系统版本、补登记 story 等 | **outward-facing 不可逆操作，需逐条明确授权**，并需要 `productId` / `subSystemId` / `optUserName` / 时间窗 / 测试负责人等参数 |

---

## 六、附录：探查命令记录

```bash
# 分支与版本
git log --oneline -80 dev-1.23.0
git rev-list --count master..dev-1.23.0    # => 4901
grep -E "<version>|<artifactId>" pom.xml | head

# 关键 commit 文件归属
git show --stat --pretty=format:"%h %s" <hash>

# 工作树状态
git status --short
git diff dss-appconn/appconns/dss-sendemail-appconn/sendemail-appconn-core/pom.xml

# 文档资产
find docs/1.23.0 -type f
```

---

**报告结束。** 本报告为只读快照，不包含任何 DPMS 写入动作。如需进入 A/B/C 任一动作，请明确指示并提供所需参数。

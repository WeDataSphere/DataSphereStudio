# REQ-01 新增AI用户切换 — 测试用例澄清纪要

> 澄清时间：2026-11-12 | 需求：REQ-01（ENHANCE/P2）| 设计文档版本：v1.1
> 澄清方式：一次成稿（用户确认）；环境策略：dev 冒烟 → uat 验证 → sit 提测（用户 2026-11-12 确认）

## 一、环境与部署策略（三环境递进）

| 环境 | 用途 | 前置条件 | 通过标准 |
|------|------|---------|---------|
| **dev** | 冒烟 | Linkis 部署 ≥ release_v2.1.7（含 aiSwitch 字段）；`wds.linkis.gateway.url.v1` 已配置；`LINKIS_RESOURCE_ADMIN_TOKEN_*` 有效；Linkis 侧 `linkis.ai.user.enable` 可控 | 冒烟用例 S-1~S-4 全通过 |
| **uat** | 验证 | 同 dev + 授权管理流程可用（dss_proxy_user 表可写入测试授权记录） | AC 全量通过 + 基线对拍 |
| **sit** | 提测 | 同 uat + 测试集（testSetId 待 A5 补录后关联） | 全部用例 + 回归 |

**环境开关矩阵**（每个环境需覆盖）：
- `linkis.ai.user.enable = true`：验证豁免链路正向行为
- `linkis.ai.user.enable = false`（默认）：验证降级回退与基线一致性
- Linkis governanceStationAdmin 不可达（停服/网络隔离）：验证 R-001 七情形降级

## 二、测试范围

### 2.1 功能测试（对应需求 AC）

| 编号 | 场景 | 验证点 | AC |
|------|------|--------|:--:|
| F-1 | alexyang 登录 → 切换 alexyang_ai（aiSwitch=true，无授权记录） | 切换成功；代理 cookie 产物（名/值/maxAge/domain/path）与原成功路径一致 | AC2.1/AC2.3 |
| F-2 | alexyang → other_ai（他人派生 _ai，aiSwitch=true，无授权记录） | 拒绝，文案 "have no permission"（与现网一致） | AC2.2 |
| F-3 | hadoop 登录 → 切换 hadoop_ai（aiSwitch=true） | 拒绝（两拒：请求者 hadoop） | AC2.2/R2.1 |
| F-4 | alexyang 登录（当前代理用户为 hadoop 场景）→ 切换 hadoop_ai | 拒绝（两拒：派生源 hadoop） | AC2.2/R2.1 |
| F-5 | 已有授权记录用户 → 切换任意授权目标（aiSwitch=true） | 走原 isExists 成功，行为不变 | AC2.2/R2.2 |
| F-6 | 本人切换（proxyUserName==userName，DS_PROXY_SELF_ENABLE=true） | 原 DS_PROXY_SELF_ENABLE 分支，零变化、零额外调用 | AC2.2 |
| F-7 | aiSwitch=true 下 hadoop 请求 → 不触发 governanceStationAdmin 调用（日志无 query aiSwitch 记录） | 两拒短路在最前（性能与安全次序） | R2.1 |

### 2.2 开关与降级测试

| 编号 | 场景 | 验证点 | AC |
|------|------|--------|:--:|
| D-1 | aiSwitch=false（Linkis 侧开关关闭）→ _ai 目标切换 | 走原 isExists，无授权被拒（与基线一致） | AC3.2 |
| D-2 | Linkis 停服/网络不通 → _ai 目标切换（aiSwitch 环境本应为 true） | 降级走原校验；warn 日志含完整 URL/异常类型/耗时；响应与基线一致 | AC3.1/R1.2 |
| D-3 | Token 配置失效（401/403）→ _ai 目标切换 | 降级走原校验，warn 留痕 | R-004 |
| D-4 | governanceStationAdmin 返回缺 aiSwitch 字段（模拟低版本 Linkis） | 降级返回 false，warn "field missing" | R1.2 |
| D-5 | 超时场景（aiSwitch 查询超时 3000ms） | 降级走原校验；单次请求阻塞 ≤ 超时上限；不重试 | R3.2/R-005 |

### 2.3 回归测试

| 编号 | 场景 | 验证点 | AC |
|------|------|--------|:--:|
| R-1 | 基线对拍：aiSwitch=false 环境下三请求（本人/有授权/无授权）响应逐字段与未升级版本一致 | AC3.3 |
| R-2 | release-server 继承路由三请求（本人/授权切换/无授权拒绝） | R-003 |
| R-3 | getProxyUser/getProxyUserList/add/revokeProxyUser 四接口 | §5.1 |
| R-4 | ITSM 导入代理用户流程（dss-scriptis-server-webank）不受影响 | §2.3 |

### 2.4 边界与安全测试

| 编号 | 场景 | 验证点 |
|------|------|--------|
| B-1 | 当前代理用户 cookie 缺失（未切换过代理）→ 豁免集合退化单元素 | F-1 仍成功（设计 X4） |
| B-2 | 并发多请求同时豁免切换 | 无共享状态，行为一致（E-5） |
| B-3 | aiSwitch 查询并发（豁免路径并发触发） | 无缓存无竞争（R1.1） |
| B-4 | 大小写变体（Hadoop/hadoop/alexyang_AI） | equalsIgnoreCase 两拒生效；_ai 后缀精确全等（大写变体不豁免——按精确匹配设计） |

## 三、造数与账号准备

| 项 | 内容 | 责任 |
|---|------|------|
| 测试账号 | alexyang（普通用户）、hadoop（系统用户，两拒验证）、otheruser（他人派生 _ai 验证） | 测试（stacyyan） |
| 授权记录 | F-5/R-1 需 dss_proxy_user 表预置授权记录；F-1 需确认无 alexyang→alexyang_ai 记录 | 测试（stacyyan） |
| Linkis 开关 | dev/uat/sit 各环境 linkis.ai.user.enable 双向变更权限（热加载验证 E-4） | 运维（falinhe） |
| Token 配置 | D-3 场景需临时改错 Token-Code 值后恢复 | 运维（falinhe）+ 开发（ryanchen） |
| cookie 观测 | 浏览器 DevTools 抓取代理 cookie（dss_user_session_proxy_ticket_id_v1）产物核对 AC2.3 | 测试 |

## 四、风险必测项映射（触点① stabilityRiskTestRequirements 摘要）

| 风险 | 必测要求 | 对应用例 |
|------|---------|---------|
| R-001 依赖 | 超时验证/降级验证/跨系统调用异常 | D-2/D-4/D-5 |
| R-002 越权 | 越权目标拒绝 | F-2/F-3/F-4/B-4 |
| R-003 继承路由 | 路由行为一致性 | R-2 |
| R-004 配置 | 配置边界值/回滚验证 | D-1/D-3 + 部署核对清单 |
| R-005 容量 | 降级路径耗时实测 | D-5（耗时数据留档作容量证据） |

## 五、冒烟清单（dev 环境，部署后首轮执行）

| 编号 | 冒烟项 | 通过标准 |
|------|--------|---------|
| S-1 | DSS 登录 + 打开 Scriptis 设置页（前端不报错） | 页面正常 |
| S-2 | POST setProxyUser 切换 alexyang→alexyang_ai（开关开） | Success + cookie 写入 |
| S-3 | 切回 alexyang（本人切换） | Success |
| S-4 | governanceStationAdmin 直调（验证 aiSwitch 字段读取） | data.aiSwitch=true 实测 |

## 六、待确认项（提测前关闭）

| # | 项 | 状态 |
|---|---|------|
| 1 | testSetId（A5 SIT 测试集创建，待 MCP 会话补录） | ⏳ 待补录 ps-003/004 |
| 2 | dpmsStoryId（A3 story 创建，待补录 ps-001/002） | ⏳ 待补录 |
| 3 | dev/uat/sit 各环境 Linkis 部署版本核对（≥ release_v2.1.7） | ⏳ 运维核对 |
| 4 | 环境间 linkis.ai.user.enable 变更审批流程 | ⏳ 运维确认 |

---
*生成：dev-flow Stage 5.5（主会话内联执行 test-case-clarification-orchestrator，子代理 403 降级）；用例生成（Stage 6）将按本纪要范围产出 BDD Feature。*

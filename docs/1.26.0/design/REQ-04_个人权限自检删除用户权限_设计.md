# 个人权限自检删除用户权限 - 设计文档

| 属性 | 值 |
|------|-----|
| 文档版本 | v1.0 |
| 最后更新 | 2026-11-13 |
| 维护人 | ryanchen |
| 需求类型 | OPTIMIZE |
| 功能属性 | 后端（SQL 脚本交付） |
| 需求文档 | [REQ-04_个人权限自检删除用户权限_需求.md](../requirements/REQ-04_个人权限自检删除用户权限_需求.md) |
| DPMS Story | [559046](http://dpms.weoa.com/#/product/100199/story/detail/559046) |

---

## 执行摘要

交付一个**三段式 SQL 脚本**（待删清单查询 → 事务化删除 → 删后核验），用于按 UM 全行权限检视整改单（ITSM 12570114）删除 DSS-IDE(5425) 子系统下 8 条用户角色授权。脚本以**三元组参数化**（用户名 + workspace_id + role_id）保证删除精确性，事务包裹保证原子性，先查后删保证可核对性。

### 核心设计决策

| 决策点 | 选择 | 理由 |
|-------|------|------|
| 交付形态 | SQL 脚本（非接口调用/自动化） | 用户澄清决策（C-1）；现有 revokeUserRole 接口已接 HPMS 鉴权，脚本路径最短 |
| 删除粒度 | dss_workspace_user_role 行级 | C-2 仅删授权不删角色；不动 dss_workspace_role/dss_workspace_user |
| 安全机制 | 先查后删 + 事务 + 行数校验 + 备份 | 高风险数据操作四重防护 |
| 参数化方式 | 三元组占位符（每条一组） | 避免 IN/宽泛 WHERE 误删 |

---

## 1.1 数据模型与操作对象

**操作表**：`dss_workspace_user_role`（DSS 工作空间用户-角色授权关系表）

关键字段（依 DssWorkspaceUserMapper 既有 SQL 语义）：

| 字段 | 说明 |
|------|------|
| username | 用户名（三元组之一） |
| workspace_id | 工作空间 ID（5425 子系统对应工作空间，执行时确认实际 ID） |
| role_id | 角色 ID（三元组之一） |

**只读参照表**：
- `dss_workspace_role`：角色定义（role_id → 角色名，仅 JOIN 展示，**不修改**）；
- `dss_workspace_user`：用户主记录（**不修改**，UM 整改仅删授权）。

> ⚠️ workspace 维度口径：UM 检视口径为"5425|意书"子系统；DSS 数据模型按 workspace_id 组织。执行前先执行 §1.2 的 W-0 查询确认 5425 对应的 workspace_id 实际值，**禁止假设**。

## 1.2 脚本设计（三段式）

### 段〇：执行前确认（W-0）

```sql
-- W-0: 确认 5425 子系统对应的 workspace 实际 ID（禁止假设，执行前必查）
-- 若库中存在 workspace 维度表/配置，以实际表名为准调整；
-- dss_workspace_user_role 中 distinct workspace_id 亦可辅助定位
SELECT DISTINCT workspace_id FROM dss_workspace_user_role LIMIT 50;
-- 需求方提供 8 条明细后，逐条核对 username 在库中的实际授权行：
SELECT ur.username, ur.workspace_id, ur.role_id, r.role_name, ur.create_time
  FROM dss_workspace_user_role ur
  LEFT JOIN dss_workspace_role r ON ur.role_id = r.id   -- 表/字段名以实际库为准
 WHERE ur.username = ? /* 目标用户名 */;
```

### 段一：待删清单查询（E1，只读）

```sql
-- Q-1: 待删清单核对（8 条，需求方提供明细后逐条代入三元组）
SELECT ur.username, ur.workspace_id, ur.role_id,
       r.role_name /* 以实际字段为准 */, ur.create_time AS granted_time
  FROM dss_workspace_user_role ur
  LEFT JOIN dss_workspace_role r ON ur.role_id = r.id
 WHERE ur.username = ? AND ur.workspace_id = ? AND ur.role_id = ?;
-- 8 条逐个执行（或用 UNION ALL 汇总一屏核对）
```

### 段二：事务化删除（E2）

```sql
-- B-0: 删除前备份（回滚依据，必须执行）
CREATE TABLE IF NOT EXISTS dss_workspace_user_role_bak_20261113 AS
  SELECT ur.* FROM dss_workspace_user_role ur
 WHERE (ur.username, ur.workspace_id, ur.role_id) IN
       ( /* 8 个三元组，逐行 (?,?,?) 列出 */ );

-- T-1: 事务化删除
START TRANSACTION;

DELETE FROM dss_workspace_user_role
 WHERE username = ? AND workspace_id = ? AND role_id = ?;
-- ↑ 8 条 DELETE 逐条执行（三元组精确匹配，禁止合并为宽泛 IN 条件）
-- 每条 DELETE 后检查 affected rows = 1（=0 说明记录不存在，<>1 说明范围异常）

-- T-2: 事务内核验（预期：目标三元组全部 0 行）
SELECT COUNT(*) AS residual
  FROM dss_workspace_user_role
 WHERE (username, workspace_id, role_id) IN
       ( /* 同上 8 个三元组 */ );
-- residual = 0 → COMMIT；residual <> 0 → ROLLBACK 并排查

COMMIT;   -- 或 ROLLBACK;
```

### 段三：删后核验（E3）

```sql
-- V-1: 目标三元组残留核验（预期 8 个三元组均 0 行）
SELECT ur.username, ur.workspace_id, ur.role_id
  FROM dss_workspace_user_role ur
 WHERE ur.username IN ( /* 8 个用户名 */ );
-- 人工比对该批用户剩余授权行：仅剩合法授权（非本次整改范围）→ 通过

-- V-2: 该批用户剩余授权数（对比删除前）
SELECT username, COUNT(*) AS remaining_roles
  FROM dss_workspace_user_role
 WHERE username IN ( /* 8 个用户名 */ )
 GROUP BY username;

-- V-3: 角色本体未受影响核验（需求 C-2：不删角色）
SELECT COUNT(*) FROM dss_workspace_role;   -- 执行前后行数一致
```

## 1.3 执行规范（运维边界）

1. **执行通道**：走 DBA 工单流程在生产库执行，脚本作为工单附件；禁止开发直连生产库执行；
2. **执行顺序**：W-0 → Q-1（核对 8 条与 UM 明细一致）→ B-0（备份）→ T-1/T-2（事务）→ V-1/V-2/V-3（核验）；
3. **中断处理**：任一步与预期不符（行数/残留非 0）→ ROLLBACK → 保留现场 → 通知需求方；
4. **回滚**：从 `dss_workspace_user_role_bak_20261113` 按 username+workspace_id+role_id INSERT 还原；
5. **备份表清理**：整改验收（ITSM 12570114 关单）后 30 天清理备份表。

## 2.1 8 条明细占位

> 待需求方提供（UM 检视结果/ITSM 单截图转文本），格式：`(username, workspace_id, role_id)` × 8。
> 明细到位后填入脚本占位符形成最终执行版。

| # | username | workspace_id | role_id | 核对 |
|---|----------|--------------|---------|:----:|
| 1-8 | （待提供） | （待提供） | （待提供） | ⏳ |

## 2.2 竞品对标

（用户跳过设计向竞品分析，省略。）

---

## 3.1 交付物清单

| # | 交付物 | 路径 |
|---|--------|------|
| 1 | SQL 脚本（三段式+占位符） | `docs/1.26.0/delivery/REQ-04_权限自检删除.sql` |
| 2 | 执行说明（本文档 §1.3） | 本文档 |
| 3 | 测试方案 | `docs/1.26.0/testing/REQ-04_权限自检删除_测试方案.md` |

## 3.2 风险承接（需求风险摘要对照）

| 风险 | 设计承接 |
|------|---------|
| 误删他人授权 | 三元组精确匹配 + 每条 DELETE affected rows=1 校验 |
| 误删角色 | 全脚本无 dss_workspace_role 写操作（V-3 核验行数） |
| 用户失去唯一角色 | V-2 剩余授权数输出，执行前人工评审 |
| 不可逆删除 | B-0 备份表先行 |

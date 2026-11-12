-- ============================================================================
-- REQ-04 个人权限自检删除用户权限 — 三段式执行脚本
-- DPMS Story: 559046  |  ITSM 整改单: 12570114
-- 依据: UM 全行权限检视结果，5425|意书(DSS-IDE) 有 8 条用户角色授权需删除
-- 约束: 仅删除 dss_workspace_user_role 行级授权；不删除角色(dss_workspace_role)、
--       不删除用户主记录(dss_workspace_user)
-- 执行顺序: W-0 → Q-1 → B-0 → T-1 → T-2 → (COMMIT|ROLLBACK) → V-1 → V-2 → V-3
-- 执行通道: DBA 工单流程，本脚本作为工单附件
-- 占位符: 8 个三元组 (username, workspace_id, role_id) 由需求方提供 UM 明细后填入
-- ============================================================================

-- ============================================================================
-- 段〇 W-0: 执行前确认（禁止假设 workspace_id，先查实际值）
-- ============================================================================

-- W-0.1: 库中现存 workspace 分布（辅助确认 5425|意书 对应的 workspace_id 实际值）
SELECT DISTINCT workspace_id, COUNT(*) AS grant_count
  FROM dss_workspace_user_role
 GROUP BY workspace_id
 ORDER BY workspace_id;

-- W-0.2: 角色表结构确认（JOIN 字段名以实际库为准，执行前核对）
-- DESCRIBE dss_workspace_role;
-- DESCRIBE dss_workspace_user_role;

-- ============================================================================
-- 段一 Q-1: 待删清单查询（E1 只读，需求方提供 8 条明细后逐条核对）
-- 每个三元组执行一次；核对字段: username / workspace_id / role_id / 角色名 / 授权时间
-- ============================================================================

SELECT ur.username, ur.workspace_id, ur.role_id, ur.create_time AS granted_time
  FROM dss_workspace_user_role ur
 WHERE ur.username = ?   /* 用户名1 */
   AND ur.workspace_id = ?   /* workspace_id 1 */
   AND ur.role_id = ?;   /* role_id 1 */

-- …… 8 条明细逐条执行同构查询 ……

-- 汇总版（一屏核对全部 8 条；三元组列表按明细填入）
SELECT ur.username, ur.workspace_id, ur.role_id, ur.create_time AS granted_time
  FROM dss_workspace_user_role ur
 WHERE (ur.username, ur.workspace_id, ur.role_id) IN (
       (/* u1 */ NULL, /* w1 */ NULL, /* r1 */ NULL),
       (/* u2 */ NULL, /* w2 */ NULL, /* r2 */ NULL),
       (/* u3 */ NULL, /* w3 */ NULL, /* r3 */ NULL),
       (/* u4 */ NULL, /* w4 */ NULL, /* r4 */ NULL),
       (/* u5 */ NULL, /* w5 */ NULL, /* r5 */ NULL),
       (/* u6 */ NULL, /* w6 */ NULL, /* r6 */ NULL),
       (/* u7 */ NULL, /* w7 */ NULL, /* r7 */ NULL),
       (/* u8 */ NULL, /* w8 */ NULL, /* r8 */ NULL)
 );
-- 预期: 恰好返回 8 行；多于 8 行 → 与 UM 明细不符，停止执行并反馈需求方

-- ============================================================================
-- 段二 B-0: 删除前备份（回滚依据，必须执行）
-- ============================================================================
CREATE TABLE IF NOT EXISTS dss_workspace_user_role_bak_20261113 AS
SELECT ur.*
  FROM dss_workspace_user_role ur
 WHERE (ur.username, ur.workspace_id, ur.role_id) IN (
       (/* u1 */ NULL, /* w1 */ NULL, /* r1 */ NULL),
       (/* u2 */ NULL, /* w2 */ NULL, /* r2 */ NULL),
       (/* u3 */ NULL, /* w3 */ NULL, /* r3 */ NULL),
       (/* u4 */ NULL, /* w4 */ NULL, /* r4 */ NULL),
       (/* u5 */ NULL, /* w5 */ NULL, /* r5 */ NULL),
       (/* u6 */ NULL, /* w6 */ NULL, /* r6 */ NULL),
       (/* u7 */ NULL, /* w7 */ NULL, /* r7 */ NULL),
       (/* u8 */ NULL, /* w8 */ NULL, /* r8 */ NULL)
 );
-- 预期: 备份表 8 行；SELECT COUNT(*) FROM dss_workspace_user_role_bak_20261113; 核对

-- ============================================================================
-- 段二 T-1: 事务化删除（E2，三元组精确匹配，逐条执行并核对 affected rows = 1）
-- ============================================================================
START TRANSACTION;

DELETE FROM dss_workspace_user_role
 WHERE username = ? /* u1 */ AND workspace_id = ? /* w1 */ AND role_id = ? /* r1 */;
-- 预期 affected rows = 1；=0 记录不存在，<>1 范围异常 → ROLLBACK 排查

DELETE FROM dss_workspace_user_role
 WHERE username = ? /* u2 */ AND workspace_id = ? /* w2 */ AND role_id = ? /* r2 */;

DELETE FROM dss_workspace_user_role
 WHERE username = ? /* u3 */ AND workspace_id = ? /* w3 */ AND role_id = ? /* r3 */;

DELETE FROM dss_workspace_user_role
 WHERE username = ? /* u4 */ AND workspace_id = ? /* w4 */ AND role_id = ? /* r4 */;

DELETE FROM dss_workspace_user_role
 WHERE username = ? /* u5 */ AND workspace_id = ? /* w5 */ AND role_id = ? /* r5 */;

DELETE FROM dss_workspace_user_role
 WHERE username = ? /* u6 */ AND workspace_id = ? /* w6 */ AND role_id = ? /* r6 */;

DELETE FROM dss_workspace_user_role
 WHERE username = ? /* u7 */ AND workspace_id = ? /* w7 */ AND role_id = ? /* r7 */;

DELETE FROM dss_workspace_user_role
 WHERE username = ? /* u8 */ AND workspace_id = ? /* w8 */ AND role_id = ? /* r8 */;

-- ============================================================================
-- 段二 T-2: 事务内核验（预期 residual = 0）
-- ============================================================================
SELECT COUNT(*) AS residual
  FROM dss_workspace_user_role
 WHERE (username, workspace_id, role_id) IN (
       (/* u1 */ NULL, /* w1 */ NULL, /* r1 */ NULL),
       (/* u2 */ NULL, /* w2 */ NULL, /* r2 */ NULL),
       (/* u3 */ NULL, /* w3 */ NULL, /* r3 */ NULL),
       (/* u4 */ NULL, /* w4 */ NULL, /* r4 */ NULL),
       (/* u5 */ NULL, /* w5 */ NULL, /* r5 */ NULL),
       (/* u6 */ NULL, /* w6 */ NULL, /* r6 */ NULL),
       (/* u7 */ NULL, /* w7 */ NULL, /* r7 */ NULL),
       (/* u8 */ NULL, /* w8 */ NULL, /* r8 */ NULL)
 );
-- residual = 0 → 执行 COMMIT
-- residual <> 0 → 执行 ROLLBACK，保留现场，通知需求方

-- COMMIT;
-- ROLLBACK;

-- ============================================================================
-- 段三 V-1: 删后核验——该批用户剩余授权明细（人工比对: 仅剩合法授权）
-- ============================================================================
SELECT ur.username, ur.workspace_id, ur.role_id, ur.create_time
  FROM dss_workspace_user_role ur
 WHERE ur.username IN (
       /* u1 */ NULL, /* u2 */ NULL, /* u3 */ NULL, /* u4 */ NULL,
       /* u5 */ NULL, /* u6 */ NULL, /* u7 */ NULL, /* u8 */ NULL
 )
 ORDER BY ur.username, ur.workspace_id, ur.role_id;

-- ============================================================================
-- 段三 V-2: 该批用户剩余授权数（对比删除前；若某用户剩余 0 条，
--           该用户将失去全部工作空间角色——需需求方确认是否预期内）
-- ============================================================================
SELECT username, COUNT(*) AS remaining_roles
  FROM dss_workspace_user_role
 WHERE username IN (
       /* u1 */ NULL, /* u2 */ NULL, /* u3 */ NULL, /* u4 */ NULL,
       /* u5 */ NULL, /* u6 */ NULL, /* u7 */ NULL, /* u8 */ NULL
 )
 GROUP BY username
 ORDER BY username;

-- ============================================================================
-- 段三 V-3: 角色本体未受影响核验（需求约束: 不删角色）
--           执行前后该计数必须一致
-- ============================================================================
SELECT COUNT(*) AS role_total FROM dss_workspace_role;

-- ============================================================================
-- 附: 回滚还原（仅在确认误删时使用，DBA 执行）
-- ============================================================================
-- INSERT INTO dss_workspace_user_role (username, workspace_id, role_id, create_time /* 以实际字段为准 */)
-- SELECT username, workspace_id, role_id, create_time
--   FROM dss_workspace_user_role_bak_20261113
--  WHERE (username, workspace_id, role_id) IN ( /* 误删的三元组 */ );
-- 还原后核对: SELECT COUNT(*) FROM dss_workspace_user_role_bak_20261113 对比已还原行数

-- ============================================================================
-- 附: 备份表清理（ITSM 12570114 关单验收后 30 天执行）
-- ============================================================================
-- DROP TABLE IF EXISTS dss_workspace_user_role_bak_20261113;

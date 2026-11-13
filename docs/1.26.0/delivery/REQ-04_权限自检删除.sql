-- REQ-04 权限自检整改（ITSM 12570114）：只删除指定用户的角色关联，保留角色和用户账号。
-- 先执行查询，确认 8 条记录及工作空间、角色名称与需求截图一致，再执行删除。
SELECT ur.username, ur.workspace_id, w.name AS workspace_name,
       ur.role_id, r.front_name AS role_name
  FROM dss_workspace_user_role ur
  LEFT JOIN dss_workspace w ON w.id = ur.workspace_id
  LEFT JOIN dss_workspace_role r ON r.id = ur.role_id
 WHERE (ur.username = 'connorliuyude' AND ur.workspace_id = 233 AND ur.role_id IN (2, 3, 4, 5))
    OR (ur.username = 'kobecheng' AND ur.workspace_id = 1 AND ur.role_id IN (2, 3, 4))
    OR (ur.username = 'kobecheng' AND ur.workspace_id = 224 AND ur.role_id = 4)
 ORDER BY ur.username, ur.workspace_id, ur.role_id
 LIMIT 10;

-- connorliuyude：233(WBRM_WORKSPACE)，删除运维、开发、分析、运营角色关联。
DELETE FROM dss_workspace_user_role
 WHERE username = 'connorliuyude' AND workspace_id = 233 AND role_id = 2
 LIMIT 1;

DELETE FROM dss_workspace_user_role
 WHERE username = 'connorliuyude' AND workspace_id = 233 AND role_id = 3
 LIMIT 1;

DELETE FROM dss_workspace_user_role
 WHERE username = 'connorliuyude' AND workspace_id = 233 AND role_id = 4
 LIMIT 1;

DELETE FROM dss_workspace_user_role
 WHERE username = 'connorliuyude' AND workspace_id = 233 AND role_id = 5
 LIMIT 1;

-- kobecheng：1(bdapWorkspace_0X)，删除运维、开发、分析角色关联。
DELETE FROM dss_workspace_user_role
 WHERE username = 'kobecheng' AND workspace_id = 1 AND role_id = 2
 LIMIT 1;

DELETE FROM dss_workspace_user_role
 WHERE username = 'kobecheng' AND workspace_id = 1 AND role_id = 3
 LIMIT 1;

DELETE FROM dss_workspace_user_role
 WHERE username = 'kobecheng' AND workspace_id = 1 AND role_id = 4
 LIMIT 1;

-- kobecheng：224(bdapWorkspace)，删除分析角色关联。
DELETE FROM dss_workspace_user_role
 WHERE username = 'kobecheng' AND workspace_id = 224 AND role_id = 4
 LIMIT 1;

-- 删除后核对：应返回 0 行。
SELECT username, workspace_id, role_id
  FROM dss_workspace_user_role
 WHERE (username = 'connorliuyude' AND workspace_id = 233 AND role_id IN (2, 3, 4, 5))
    OR (username = 'kobecheng' AND workspace_id = 1 AND role_id IN (2, 3, 4))
    OR (username = 'kobecheng' AND workspace_id = 224 AND role_id = 4)
 LIMIT 10;

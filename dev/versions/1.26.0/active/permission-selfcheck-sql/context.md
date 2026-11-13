---
contextSchemaVersion: "1.0"
contextRevision: 1
checkpointId: ""
checkpointState: "clean"
currentStage: "0"
lastCompletedStage: ""
stageHistory: []
skipDecisions: {}
rollbackHistory: []
taskName: "permission-selfcheck-sql"
requirementType: "OPTIMIZE"
executionMode: "快速模式"
interactionConfig: ""
loopDecisionMode: "ask"
inputSource: "version_mode"
reqIndex: "02"
reqPrefix: "REQ-04"
versionName: "1.26.0"
versionContextPath: "dev/versions/1.26.0/version-context.md"
dpmsStoryId: 559046
testSetId: "327880"
regressionTestSetId: "327881"
releasePlanId: "181933"
productId: "100199"
productName: BDP_DSS
subsystemId: "5425"
subsystemName: DSS-IDE
subsystemVersionId: "1.26.0"
branchName: "dev-1.26.0"
pipelineId: null
pipelineVersion: "DSS-IDE_1.26.0"
reqSubsystemId: "5425"
repositoryId: null
functionAttributes: ["后端"]
frontendType: ""
selectedDevAgents: []
knowledgeBaseStatus: "skipped"
moduleIndexPath: ""
stageOutputs: {}
artifactIndex: {}
apiDocumentStatus: {}
legacyImpactList: []
legacyImpactStatus: ""
legacyImpactRequirementSha256: ""
legacyImpactVerification: {}
legacyImpactNaConfirmed: {}
documentArchive: {}
stage3DesignReclosure: {}
staleArtifacts: []
openRisks: []
decisionLog: []
createdAt: "2026-11-13T10:00:00+08:00"
updatedAt: "2026-11-13T10:00:00+08:00"
---

# 任务上下文

## 📊 阶段执行记录（需求/设计相位）

| 阶段 | 名称 | 状态 | 产出物 | 完成时间 | 备注 |
|------|------|------|--------|----------|------|

## 📋 任务基本信息

**任务名称**: permission-selfcheck-sql
**需求类型**: OPTIMIZE（权限运维优化，SQL 脚本交付，无业务代码改动）
**执行模式**: 快速模式
**竞品分析摘要**: （用户跳过竞品分析）
**价值收益摘要**: （用户跳过价值收益评估）

## 📝 需求描述

**DPMS Story**: [559046](http://dpms.weoa.com/#/product/100199/story/detail/559046)（【低风险】【系统优化】【DSS】【权限管理】【后端需求】个人权限自检删除用户权限，status=3，priority=1，releasePlanId=181933）

**需求背景**（story 原文）：根据 UM 的全行权限检视结果，5425|意书 有 8 条用户角色需删除，请在本系统内完成用户删除。ITSM 单号：12570114。

**澄清结论**（2026-11-13 用户确认，三轮澄清收敛）：
1. **交付形态 = SQL 脚本**：查询待删清单 + 事务化 DELETE + 删后核验，人工在库上执行；不删角色本身，仅删角色下的用户授权；
2. **不做 ITSM 自动化集成**（曾考虑"审批后自动调 DSS 接口"，因现有接口已接入 ITSM/HPMS 鉴权码、集成复杂度高，用户决策改 SQL 脚本交付，自动化另立需求）；
3. **现有能力已核实**：DSS 已有 `DSSWorkspaceUserRestful`（/clearUser、/revokeUserRole，HPMS_USER_TOKEN 鉴权）与 `DSSWorkspaceUserService.revokeUserRoles/clearUserByUserName`（dss_workspace_user_role 表），本次 SQL 与该表操作语义一致。

**8 条待删明细**：DPMS 需求描述为图片（下载 403 需登录），明细清单执行前由用户从 UM 检视结果/ITSM 单 12570114 提供或在 DSS 库按 UM 口径自检定位。

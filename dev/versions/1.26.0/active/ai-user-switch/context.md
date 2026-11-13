---
contextSchemaVersion: '1.0'
contextRevision: 13
checkpointId: "1.26.0:REQ-01:5:2026-11-13T16:40:00+08:00"
checkpointState: clean
currentStage: '6'
lastCompletedStage: '5'
stageHistory:
- '0'
- '1'
- '1.1'
- '1.2'
- '1.6'
- 2
- 2.1
- 3
- 4
- 5
skipDecisions:
  '1.1': executed
  '1.2': skipped
rollbackHistory: []
taskName: ai-user-switch
requirementType: ENHANCE
executionMode: 快速模式
interactionConfig: ''
loopDecisionMode: ask
inputSource: version_mode
reqIndex: '01'
reqPrefix: REQ-01
versionName: 1.26.0
versionContextPath: dev/versions/1.26.0/version-context.md
requirementDocument: docs/1.26.0/requirements/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_需求.md
designDocument: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
designDocuments:
- docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
dpmsStoryId: 560976
testSetId: '327880'
regressionTestSetId: '327881'
releasePlanId: '181933'
productId: '100199'
productName: BDP_DSS
subsystemId: '5425'
subsystemName: DSS-IDE
subsystemVersionId: 1.26.0
branchName: dev-1.26.0
pipelineId: null
pipelineVersion: DSS-IDE_1.26.0
reqSubsystemId: '5425'
repositoryId: null
functionAttributes:
- 后端
frontendType: ''
selectedDevAgents: []
knowledgeBaseStatus: skipped
moduleIndexPath: ''
stageOutputs:
  '0':
    status: completed
    summary: 需求澄清完成（主会话内联执行，req-clarification-orchestrator 子代理因模型白名单403降级）。澄清结论：①aiSwitch=true
      时免授权校验范围=当前用户+_ai 与当前代理用户+_ai（用户修正口径，非任意_ai后缀）；②governanceStationAdmin 调用失败/超时/字段缺失回退原
      isExists 授权校验；③aiSwitch 查询由 DSS 后端在 setProxyUser 链路内执行，前端不改动；④aiSwitch 契约=Linkis
      release_v2.1.7 REQ-01（governanceStationAdmin 追加 data.aiSwitch 原生布尔，linkis.ai.user.enable
      默认 false 热加载，URL 在 linkis-mg 白名单免登录，实现在 incubator-linkis release_v2.1.7 commit
      81f5fd117）；⑤hadoop 用户不允许切换 _ai 用户；⑥功能属性=后端。已核实现状代码：/dss/scriptis/proxy/setProxyUser
      → DssProxyUserController.setProxyUserCookie，校验点=(DS_PROXY_SELF_ENABLE&&本人)||isExists(userName,proxyUserName,workspace)，成功路径=ProxyUserSSOUtils
      写 cookie。
    artifacts: []
    decisions:
    - aiSwitch免校验范围=当前用户+_ai与当前代理用户+_ai（用户口径修正，非任意_ai后缀）
    - governanceStationAdmin调用失败/超时/字段缺失时回退原isExists授权校验
    - aiSwitch查询由DSS后端setProxyUser链路执行，前端不改动
    - aiSwitch契约=Linkis release_v2.1.7 REQ-01：linkis.ai.user.enable默认false热加载，data.aiSwitch为JSON原生布尔，URL在linkis-mg网关白名单免登录
    - hadoop用户不允许切换_ai用户
    - 功能属性=后端
    risks: []
    contextUpdates:
      functionAttributes: 后端
    completedAt: '2026-11-12T11:15:00+08:00'
  '1':
    status: completed
    summary: 需求文档 v1.0 生成（主会话内联执行 req-enhance-feature-analyzer，子代理403降级）：12章模板全量填写，含
      2026-11-12 需求方澄清结论5项（豁免范围=当前用户+_ai与当前代理用户+_ai、降级回退原校验、DSS后端调用、aiSwitch契约=Linkis
      release_v2.1.7 REQ-01、hadoop硬约束）、三段式验收标准9条（AC1.1-AC3.3）、基线对拍要求、范围外清单（禁止任意_ai豁免/前端改动/授权管理流程）。
    artifacts:
    - type: requirement_document
      path: docs/1.26.0/requirements/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_需求.md
    decisions: []
    risks: []
    contextUpdates: {}
    completedAt: '2026-11-12T11:30:00+08:00'
artifactIndex:
  requirement_document:
  - docs/1.26.0/requirements/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_需求.md
apiDocumentStatus: {}
legacyImpactList: []
legacyImpactStatus: ''
legacyImpactRequirementSha256: ''
legacyImpactVerification: {}
legacyImpactNaConfirmed: {}
documentArchive: {}
stage3DesignReclosure: {}
diagramSyncChecks:
  requirement:
    documentPath: docs/1.26.0/requirements/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_需求.md
    documentSha256: 1e4711596c051139bced56d8b40980b9554517b3f7f75f0d11e9b94e4a8ab4db
    status: passed
    checkedAt: '2026-11-12T14:05:00+08:00'
  design:
    documentPath: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
    documentSha256: 446fdcdc1535
    status: passed
    checkedAt: '2026-11-12T16:40:00+08:00'
apiDocumentStatus:
  status: not_applicable
  markdownPaths: []
  decidedAt: '2026-11-12T16:40:00+08:00'
  sourceStage: 2.1.finalization
  degradations: []
injection_risk:
  level: N/A
  reason: 非Claude插件/Agent/Skill/Command需求（后端Java鉴权链路增强），提示词注入威胁不适用
  decidedAt: '2026-11-12T14:05:00+08:00'
componentDependencyArtifacts:
- dev/versions/1.26.0/active/ai-user-switch/service-dependency-analysis.md
- dev/versions/1.26.0/active/ai-user-switch/service-dependency-graph.mermaid
- dev/versions/1.26.0/active/ai-user-switch/http-calls.json
deploymentStatus: confirmed
staleArtifacts: []
openRisks: []
decisionLog:
- aiSwitch免校验范围=当前用户+_ai与当前代理用户+_ai（用户口径修正）
- governanceStationAdmin降级=回退原isExists授权校验
- aiSwitch查询链路=DSS后端setProxyUser内执行，前端不改动
- aiSwitch契约=Linkis release_v2.1.7 REQ-01（data.aiSwitch原生布尔，默认false，mg白名单免登录）
- hadoop用户不允许切换_ai用户
- 功能属性=后端
createdAt: '2026-11-11T09:40:00+08:00'
updatedAt: "2026-11-13T16:40:00+08:00"
stabilityRisks:
- risk_type: 依赖
  affected_scope: dss-framework-proxy-user-service setProxyUser 链路 → linkis-mg 网关
    → linkis-jobhistory governanceStationAdmin
  evidence: 需求事实：setProxyUser 处理链路内 DSS 后端新增对 Linkis governanceStationAdmin 的同步 HTTP
    只读调用，解析 data.aiSwitch 作为豁免判定唯一输入。影响依据：结构化知识确认 DSS 系统对 Linkis 系统存在出向依赖关系；本次为该依赖上新增的运行时调用点。风险判断：governanceStationAdmin
    不可达、超时、返回缺 aiSwitch 字段（目标部署 Linkis 版本低于该字段落地版本）时，豁免能力整体失效，开关开启环境下 _ai 用户切换退回被拒状态；既有切换行为不受影响（全部失败情形降级为不豁免）。缺失信息：各环境部署的
    Linkis 版本是否含 aiSwitch 字段未逐环境核实；调用基址与超时上限未在需求阶段闭合。处理建议：设计阶段闭合调用基址配置项与超时上限，上线前逐环境核对部署版本含该字段，降级日志留痕含完整
    URL/异常/耗时便于归因。
  id: R-001
  risk_level: 高
  status: 已覆盖
  source_stage: '1.1'
  test_case_requirements: 压测<br>可用性测试<br>依赖不可用降级验证：依赖系统：Linkis governanceStationAdmin；不可用方式：连接拒绝/超时/非0
    status/JSON解析失败/字段缺失或类型非法；降级策略：一律视为 aiSwitch=false 走原校验，warn 留痕（URL/异常/耗时）；业务影响：豁免不可用但既有切换行为与现网一致<br>依赖超时验证：依赖系统：Linkis
    governanceStationAdmin；超时阈值：建议 ≤3s（设计阶段定）；超时后的业务行为：视为 aiSwitch=false，回退原 isExists
    授权校验<br>依赖重试验证
- risk_type: 模块链路
  affected_scope: setProxyUser 代理授权校验链路（isExists 豁免判定）
  evidence: 需求事实：开关开启且目标属于豁免集合时跳过代理授权校验，豁免命中时响应由拒绝错误变为切换成功。影响依据：setProxyUser 是代理身份切换的唯一入口，豁免判定直接改变授权边界；结构化知识确认
    DSS 系统存在下游依赖方。风险判断：若实现把豁免范围放大为任意 _ai 后缀目标（他人派生用户）、或 hadoop 两拒特判遗漏（请求者为 hadoop
    或目标派生自 hadoop 的场景绕过），将造成越权切换；当前需求已以特判次序、精确全等匹配与边界验收用例收窄。缺失信息：当前代理用户的 cookie 读取口径需设计阶段闭合，口径不一致可能引入第二类派生源。处理建议：设计阶段明确当前代理用户读取口径并在代码评审中逐条核对豁免集合判定与特判次序；他人派生目标与
    hadoop 派生目标拒绝场景纳入必测。
  id: R-002
  risk_level: 高
  status: 已覆盖
  source_stage: '1.1'
  test_case_requirements: 下游联动验证<br>跨系统调用异常验证：调用方：切换请求用户；被调用方：DSS setProxyUser 授权校验链路；异常类型：越权目标（他人派生
    _ai / hadoop 派生）；预期业务响应：不豁免，走原 isExists 校验并拒绝<br>级联失败恢复验证
- risk_type: 模块链路
  affected_scope: dss-framework-release-server-webank ProxyUserController（继承方路由）
  evidence: 需求事实：release-server 的 ProxyUserController 继承 DssProxyUserController，基类增强逻辑被两个继承控制器同时继承，继承方无代码改动即行为变化。影响依据：基类为唯一代码改动点，继承方路由（release
    服务）随基类自动获得豁免行为；测试关注点已将 release-server 继承路由列入回归范围。风险判断：release 路由的实际调用方未逐一核实，若发布场景存在对原校验行为的隐性依赖，行为变化可能未被现有回归覆盖。缺失信息：release-server
    路由下 setProxyUser 的实际调用方与调用频率未核实。处理建议：回归范围显式纳入 release-server 继承路由，联调阶段确认 release
    路由下代理切换行为。
  id: R-003
  risk_level: 高
  status: 已覆盖
  source_stage: '1.1'
  test_case_requirements: 下游联动验证<br>跨系统调用异常验证<br>级联失败恢复验证
- risk_type: 参数配置
  affected_scope: DSS 侧新增 governanceStationAdmin 调用基址/超时配置项（设计阶段闭合）
  evidence: 需求事实：调用基址（网关地址/直连地址）与配置项来源由设计文档明确，超时上限建议三秒以内设计阶段定。影响依据：基址与超时为豁免链路的唯一外部参数，配置错误（基址错误、超时未设置）直接导致豁免能力静默失效或切换请求阻塞。风险判断：配置项名称、原值、生效范围、生效方式均未在需求阶段闭合，属需求完整性缺口型风险而非确定性缺陷；需求已明确调用必须设置超时、禁止无限等待。缺失信息：配置项名称与生效范围未定；linkis-mg
    网关地址在 DSS 侧的既有配置来源未核实。处理建议：设计文档闭合配置项定义（名称/默认值/生效范围/读取方式），上线前逐环境核对基址配置并纳入降级验证。
  id: R-004
  risk_level: 高
  status: 已覆盖
  source_stage: '1.1'
  test_case_requirements: 压测<br>可用性测试<br>配置变更生效验证：配置项：governanceStationAdmin 调用基址/超时（名称设计定）；变更前值：无（新增）；变更后值：设计阶段闭合；生效范围：scriptis-server
    全部实例；生效方式：设计阶段定<br>配置回滚验证<br>配置边界值验证：配置项：超时上限；合法边界：建议 ≤3s；非法边界：0 或未设置；预期保护行为：禁止无限等待，超时降级为
    aiSwitch=false
- risk_type: 性能容量
  affected_scope: setProxyUser 请求延迟（新增 governanceStationAdmin 同步调用叠加）
  evidence: 需求事实：非本人切换请求在原校验路径前新增一次同步 HTTP 调用，性能目标为较原路径增加不超过一秒（governanceStationAdmin
    超时上限以内）。影响依据：setProxyUser 属用户交互路径（前端切换代理用户操作），延迟直接叠加到用户操作响应；governanceStationAdmin
    为页面初始化高频接口。风险判断：正常路径（网关内网调用）延迟增量毫秒级可控；降级路径最坏情况切换请求阻塞至超时上限，对用户体验有实质影响；需求未提供压测或容量论证。缺失信息：governanceStationAdmin
    现网延迟基线与切换请求量未提供。处理建议：设计阶段将超时上限收敛（内网调用建议亚秒级），测试阶段以降级路径实测耗时作为容量证据，延迟增量目标纳入测试计划。
  id: R-005
  risk_level: 高
  status: 已覆盖
  source_stage: '1.1'
  test_case_requirements: 压测：并发量：未提供；数据量：未提供；持续时间：未提供；吞吐量目标：未提供；延迟目标：较原路径增加 ≤ 1s（需求未量化基线）；资源阈值：未提供<br>可用性测试<br>容量评估验证
stabilityRiskAssessmentTrace:
  R-001:
    risk_id: R-001
    rules_version: '1.1'
    catalog_version: '2.0'
    scoring_provisional: true
    final_level: 高
    deterministic_level: 高
    llm_proposed_level: 高
    base_level: 高
    minimum_level: 高
    score: 150
    score_level: 高
    score_breakdown:
      asset_criticality: 40
      module_criticality: 30
      change_type: 25
      affected_component_count: 0
      concerns: 40
      missing_safeguards: 15
    knowledge_status: partial
    resolved_asset_ids:
    - system:dss
    resolved_module_ids: []
    asset_criticality: unknown
    module_criticality: unknown
    matched_rule_ids:
    - KNOWLEDGE-UNKNOWN-001
    - SAFEGUARD-MISSING-001
    downgrade_rule_id: null
    missing_knowledge:
    - asset_catalog_entry
    - asset_catalog_entry:system:linkis
    - module_catalog_entry
    - module_catalog_entry:dss_framework_proxy_user_service
    - asset_criticality
    unknown_input_values: []
    degraded: true
    blocking: false
  R-002:
    risk_id: R-002
    rules_version: '1.1'
    catalog_version: '2.0'
    scoring_provisional: true
    final_level: 高
    deterministic_level: 高
    llm_proposed_level: 高
    base_level: 高
    minimum_level: 高
    score: 135
    score_level: 高
    score_breakdown:
      asset_criticality: 40
      module_criticality: 30
      change_type: 25
      affected_component_count: 0
      concerns: 40
      missing_safeguards: 0
    knowledge_status: partial
    resolved_asset_ids:
    - system:dss
    resolved_module_ids: []
    asset_criticality: unknown
    module_criticality: unknown
    matched_rule_ids:
    - KNOWLEDGE-UNKNOWN-001
    - CRITICAL-CONCERN-001
    downgrade_rule_id: null
    missing_knowledge:
    - module_catalog_entry
    - module_catalog_entry:dss_framework_proxy_user_service
    - asset_criticality
    unknown_input_values: []
    degraded: true
    blocking: false
  R-003:
    risk_id: R-003
    rules_version: '1.1'
    catalog_version: '2.0'
    scoring_provisional: true
    final_level: 高
    deterministic_level: 高
    llm_proposed_level: 高
    base_level: 高
    minimum_level: 高
    score: 115
    score_level: 高
    score_breakdown:
      asset_criticality: 40
      module_criticality: 30
      change_type: 25
      affected_component_count: 0
      concerns: 20
      missing_safeguards: 0
    knowledge_status: partial
    resolved_asset_ids:
    - system:dss
    resolved_module_ids: []
    asset_criticality: unknown
    module_criticality: unknown
    matched_rule_ids:
    - KNOWLEDGE-UNKNOWN-001
    downgrade_rule_id: null
    missing_knowledge:
    - module_catalog_entry
    - module_catalog_entry:dss_framework_release_server_webank
    - asset_criticality
    unknown_input_values: []
    degraded: true
    blocking: false
  R-004:
    risk_id: R-004
    rules_version: '1.1'
    catalog_version: '2.0'
    scoring_provisional: true
    final_level: 高
    deterministic_level: 高
    llm_proposed_level: 高
    base_level: 高
    minimum_level: 高
    score: 145
    score_level: 高
    score_breakdown:
      asset_criticality: 40
      module_criticality: 30
      change_type: 25
      affected_component_count: 0
      concerns: 20
      missing_safeguards: 30
    knowledge_status: partial
    resolved_asset_ids:
    - system:dss
    resolved_module_ids: []
    asset_criticality: unknown
    module_criticality: unknown
    matched_rule_ids:
    - KNOWLEDGE-UNKNOWN-001
    - SAFEGUARD-MISSING-001
    downgrade_rule_id: null
    missing_knowledge:
    - asset_catalog_entry
    - asset_catalog_entry:system:linkis
    - module_catalog_entry
    - module_catalog_entry:dss_framework_proxy_user_service
    - asset_criticality
    unknown_input_values: []
    degraded: true
    blocking: false
  R-005:
    risk_id: R-005
    rules_version: '1.1'
    catalog_version: '2.0'
    scoring_provisional: true
    final_level: 高
    deterministic_level: 高
    llm_proposed_level: 高
    base_level: 高
    minimum_level: 高
    score: 160
    score_level: 高
    score_breakdown:
      asset_criticality: 40
      module_criticality: 30
      change_type: 30
      affected_component_count: 0
      concerns: 45
      missing_safeguards: 15
    knowledge_status: partial
    resolved_asset_ids:
    - system:dss
    resolved_module_ids: []
    asset_criticality: unknown
    module_criticality: unknown
    matched_rule_ids:
    - KNOWLEDGE-UNKNOWN-001
    - SAFEGUARD-MISSING-001
    downgrade_rule_id: null
    missing_knowledge:
    - asset_catalog_entry
    - asset_catalog_entry:system:linkis
    - module_catalog_entry
    - module_catalog_entry:dss_framework_proxy_user_service
    - asset_criticality
    unknown_input_values: []
    degraded: true
    blocking: false
stabilityRiskSourceEvidence:
- evidence_id: catalog:enhancement:E1
  kind: requirement_item
  value: aiSwitch 查询
  state: verified
  source_refs:
  - §4.2
  source_excerpt: '#### E1: aiSwitch 查询 `P0` `已确认`'
  resolver_id: requirement_document_literal_resolver
- evidence_id: catalog:enhancement:E2
  kind: requirement_item
  value: _ai 目标豁免授权校验
  state: verified
  source_refs:
  - §4.2
  source_excerpt: '#### E2: _ai 目标豁免授权校验 `P0` `已确认`'
  resolver_id: requirement_document_literal_resolver
- evidence_id: catalog:enhancement:E3
  kind: requirement_item
  value: 降级回退
  state: verified
  source_refs:
  - §4.2
  source_excerpt: '#### E3: 降级回退 `P0` `已确认`'
  resolver_id: requirement_document_literal_resolver
- evidence_id: detector:REQ-COMPLETENESS-001:PARAM-SCOPE-MISSING
  kind: risk_candidate
  value: 调用基址（网关地址/直连地址）与配置项来源由设计文档明确
  state: verified
  source_refs:
  - §4.2
  source_excerpt: 调用基址（网关地址/直连地址）与配置项来源由设计文档明确
  resolver_id: requirement_document_literal_resolver
- evidence_id: conclusion:R-001:change_fact
  kind: risk_business_fact
  value: setProxyUser 处理链路内，DSS 后端向 Linkis 查询集群级 `_ai` 能力开关，作为豁免判定的唯一输入
  state: verified
  source_refs:
  - §4.2
  source_excerpt: setProxyUser 处理链路内，DSS 后端向 Linkis 查询集群级 `_ai` 能力开关，作为豁免判定的唯一输入。查询失败不影响现有行为（降级为不豁免）。
  resolver_id: requirement_document_literal_resolver
- evidence_id: conclusion:R-001:business_impact
  kind: risk_business_fact
  value: 查询失败不影响现有行为（降级为不豁免）
  state: verified
  source_refs:
  - §4.2
  source_excerpt: setProxyUser 处理链路内，DSS 后端向 Linkis 查询集群级 `_ai` 能力开关，作为豁免判定的唯一输入。查询失败不影响现有行为（降级为不豁免）。
  resolver_id: requirement_document_literal_resolver
- evidence_id: conclusion:R-002:change_fact
  kind: risk_business_fact
  value: 目标代理用户为「当前登录用户+_ai」或「当前代理用户+_ai」的切换请求跳过 isExists 授权校验
  state: verified
  source_refs:
  - §4.2
  source_excerpt: aiSwitch=true 时，目标代理用户为「当前登录用户+_ai」或「当前代理用户+_ai」的切换请求跳过 isExists
    授权校验，直接走清旧 cookie + 写代理 cookie 成功路径。
  resolver_id: requirement_document_literal_resolver
- evidence_id: conclusion:R-002:business_impact
  kind: risk_business_fact
  value: 直接走清旧 cookie + 写代理 cookie 成功路径
  state: verified
  source_refs:
  - §4.2
  source_excerpt: aiSwitch=true 时，目标代理用户为「当前登录用户+_ai」或「当前代理用户+_ai」的切换请求跳过 isExists
    授权校验，直接走清旧 cookie + 写代理 cookie 成功路径。
  resolver_id: requirement_document_literal_resolver
- evidence_id: conclusion:R-003:change_fact
  kind: risk_business_fact
  value: 继承 DssProxyUserController，自动获得豁免行为
  state: verified
  source_refs:
  - §八、关联影响分析 【参考】
  source_excerpt: dss-framework-release-server-webank ProxyUserController | 自动继承 |
    继承 DssProxyUserController，自动获得豁免行为 | 回归验证 release 路由行为
  resolver_id: requirement_document_literal_resolver
- evidence_id: conclusion:R-003:business_impact
  kind: risk_business_fact
  value: 回归验证 release 路由行为
  state: verified
  source_refs:
  - §八、关联影响分析 【参考】
  source_excerpt: dss-framework-release-server-webank ProxyUserController | 自动继承 |
    继承 DssProxyUserController，自动获得豁免行为 | 回归验证 release 路由行为
  resolver_id: requirement_document_literal_resolver
- evidence_id: conclusion:R-004:change_fact
  kind: risk_business_fact
  value: governanceStationAdmin 调用须设置超时
  state: verified
  source_refs:
  - §4.2
  source_excerpt: governanceStationAdmin 调用须设置超时，禁止无限等待阻塞切换请求
  resolver_id: requirement_document_literal_resolver
- evidence_id: conclusion:R-004:business_impact
  kind: risk_business_fact
  value: 禁止无限等待阻塞切换请求
  state: verified
  source_refs:
  - §4.2
  source_excerpt: governanceStationAdmin 调用须设置超时，禁止无限等待阻塞切换请求
  resolver_id: requirement_document_literal_resolver
- evidence_id: conclusion:R-005:change_fact
  kind: risk_business_fact
  value: setProxyUser 增强后响应时间（aiSwitch 查询叠加）
  state: verified
  source_refs:
  - §六、非功能需求 【重要】
  source_excerpt: 性能 | setProxyUser 增强后响应时间（aiSwitch 查询叠加） | 较原路径增加 ≤ 1s（governanceStationAdmin
    超时上限以内）
  resolver_id: requirement_document_literal_resolver
- evidence_id: conclusion:R-005:business_impact
  kind: risk_business_fact
  value: 较原路径增加 ≤ 1s（governanceStationAdmin 超时上限以内）
  state: verified
  source_refs:
  - §六、非功能需求 【重要】
  source_excerpt: 性能 | setProxyUser 增强后响应时间（aiSwitch 查询叠加） | 较原路径增加 ≤ 1s（governanceStationAdmin
    超时上限以内）
  resolver_id: requirement_document_literal_resolver
stabilityRiskRequirementMappings:
- risk_id: R-001
  requirement_items:
  - item_id: E1
    item_type: enhancement
    title: aiSwitch 查询
    source_ref: §4.2
  - item_id: E3
    item_type: enhancement
    title: 降级回退
    source_ref: §4.2
- risk_id: R-002
  requirement_items:
  - item_id: E2
    item_type: enhancement
    title: _ai 目标豁免授权校验
    source_ref: §4.2
- risk_id: R-003
  requirement_items:
  - item_id: E2
    item_type: enhancement
    title: _ai 目标豁免授权校验
    source_ref: §4.2
- risk_id: R-004
  requirement_items:
  - item_id: E1
    item_type: enhancement
    title: aiSwitch 查询
    source_ref: §4.2
- risk_id: R-005
  requirement_items:
  - item_id: E1
    item_type: enhancement
    title: aiSwitch 查询
    source_ref: §4.2
stabilityRiskBusinessConclusions:
- risk_id: R-001
  change_fact: setProxyUser 处理链路内，DSS 后端向 Linkis 查询集群级 `_ai` 能力开关，作为豁免判定的唯一输入
  business_impact: 查询失败不影响现有行为（降级为不豁免）
  conclusion: setProxyUser 处理链路内，DSS 后端向 Linkis 查询集群级 `_ai` 能力开关，作为豁免判定的唯一输入可能导致查询失败不影响现有行为（降级为不豁免）
- risk_id: R-002
  change_fact: 目标代理用户为「当前登录用户+_ai」或「当前代理用户+_ai」的切换请求跳过 isExists 授权校验
  business_impact: 直接走清旧 cookie + 写代理 cookie 成功路径
  conclusion: 目标代理用户为「当前登录用户+_ai」或「当前代理用户+_ai」的切换请求跳过 isExists 授权校验可能导致直接走清旧 cookie
    + 写代理 cookie 成功路径
- risk_id: R-003
  change_fact: 继承 DssProxyUserController，自动获得豁免行为
  business_impact: 回归验证 release 路由行为
  conclusion: 继承 DssProxyUserController，自动获得豁免行为可能导致回归验证 release 路由行为
- risk_id: R-004
  change_fact: governanceStationAdmin 调用须设置超时
  business_impact: 禁止无限等待阻塞切换请求
  conclusion: governanceStationAdmin 调用须设置超时可能导致禁止无限等待阻塞切换请求
- risk_id: R-005
  change_fact: setProxyUser 增强后响应时间（aiSwitch 查询叠加）
  business_impact: 较原路径增加 ≤ 1s（governanceStationAdmin 超时上限以内）
  conclusion: setProxyUser 增强后响应时间（aiSwitch 查询叠加）可能导致较原路径增加 ≤ 1s（governanceStationAdmin
    超时上限以内）
stabilityRiskTestRequirements:
- requirement_id: RTR-001-PRESSURE
  risk_id: R-001
  policy_rule_ids:
  - RTP-HIGH-BASE-ENGINE-PRESSURE-001
  definition_id: RTEST-PRESSURE-001
  policy_version: '1.0'
  test_type: pressure
  title: 压测
  business_context: null
  display_text: 压测
  verification_points:
  - 明确压测负载、数据量和持续时间
  - 验证吞吐量、延迟和资源水位达到目标
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  - test_report_available
  required: true
  applicability_basis:
  - risk.risk_level="高"
  - asset.categories=["数据分析工场", "计算中间件"]
- requirement_id: RTR-001-AVAILABILITY
  risk_id: R-001
  policy_rule_ids:
  - RTP-HIGH-BASE-ENGINE-AVAILABILITY-001
  definition_id: RTEST-AVAILABILITY-001
  policy_version: '1.0'
  test_type: availability
  title: 可用性测试
  business_context: null
  display_text: 可用性测试
  verification_points:
  - 覆盖单节点、单实例或关键依赖故障
  - 验证切换、降级和恢复期间的可用性目标
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  - test_report_available
  required: true
  applicability_basis:
  - risk.risk_level="高"
  - asset.categories=["数据分析工场", "计算中间件"]
- requirement_id: RTR-001-DEPENDENCY-DEGRADATION
  risk_id: R-001
  policy_rule_ids:
  - RTP-DEPENDENCY-001
  definition_id: RTEST-DEPENDENCY-DEGRADATION-001
  policy_version: '1.0'
  test_type: dependency_degradation
  title: 依赖不可用降级验证
  business_context: 依赖系统：Linkis governanceStationAdmin；不可用方式：连接拒绝/超时/非0 status/JSON解析失败/字段缺失或类型非法；降级策略：一律视为
    aiSwitch=false 走原校验，warn 留痕（URL/异常/耗时）；业务影响：豁免不可用但既有切换行为与现网一致
  display_text: 依赖不可用降级验证：依赖系统：Linkis governanceStationAdmin；不可用方式：连接拒绝/超时/非0 status/JSON解析失败/字段缺失或类型非法；降级策略：一律视为
    aiSwitch=false 走原校验，warn 留痕（URL/异常/耗时）；业务影响：豁免不可用但既有切换行为与现网一致
  verification_points:
  - 模拟依赖不可用或异常返回
  - 验证降级策略、业务提示和状态收敛
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  required: true
  applicability_basis:
  - risk.risk_type="依赖"
- requirement_id: RTR-001-DEPENDENCY-TIMEOUT
  risk_id: R-001
  policy_rule_ids:
  - RTP-DEPENDENCY-001
  definition_id: RTEST-DEPENDENCY-TIMEOUT-001
  policy_version: '1.0'
  test_type: dependency_timeout
  title: 依赖超时验证
  business_context: 依赖系统：Linkis governanceStationAdmin；超时阈值：建议 ≤3s（设计阶段定）；超时后的业务行为：视为
    aiSwitch=false，回退原 isExists 授权校验
  display_text: 依赖超时验证：依赖系统：Linkis governanceStationAdmin；超时阈值：建议 ≤3s（设计阶段定）；超时后的业务行为：视为
    aiSwitch=false，回退原 isExists 授权校验
  verification_points:
  - 覆盖连接、请求或处理超时
  - 验证超时后的资源释放和业务响应
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  required: true
  applicability_basis:
  - risk.risk_type="依赖"
- requirement_id: RTR-001-DEPENDENCY-RETRY
  risk_id: R-001
  policy_rule_ids:
  - RTP-DEPENDENCY-001
  definition_id: RTEST-DEPENDENCY-RETRY-001
  policy_version: '1.0'
  test_type: dependency_retry
  title: 依赖重试验证
  business_context: null
  display_text: 依赖重试验证
  verification_points:
  - 验证重试次数、间隔和终止条件
  - 验证重试过程满足幂等要求且不放大故障
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  required: true
  applicability_basis:
  - risk.risk_type="依赖"
- requirement_id: RTR-002-DOWNSTREAM-LINKAGE
  risk_id: R-002
  policy_rule_ids:
  - RTP-MODULE-LINK-001
  definition_id: RTEST-DOWNSTREAM-LINKAGE-001
  policy_version: '1.0'
  test_type: downstream_linkage
  title: 下游联动验证
  business_context: null
  display_text: 下游联动验证
  verification_points:
  - 覆盖已确认的下游调用链
  - 验证上下游输入输出和状态传递
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  required: true
  applicability_basis:
  - risk.risk_type="模块链路"
- requirement_id: RTR-002-CROSS-SYSTEM-FAILURE
  risk_id: R-002
  policy_rule_ids:
  - RTP-MODULE-LINK-001
  definition_id: RTEST-CROSS-SYSTEM-FAILURE-001
  policy_version: '1.0'
  test_type: cross_system_failure
  title: 跨系统调用异常验证
  business_context: 调用方：切换请求用户；被调用方：DSS setProxyUser 授权校验链路；异常类型：越权目标（他人派生 _ai / hadoop
    派生）；预期业务响应：不豁免，走原 isExists 校验并拒绝
  display_text: 跨系统调用异常验证：调用方：切换请求用户；被调用方：DSS setProxyUser 授权校验链路；异常类型：越权目标（他人派生 _ai
    / hadoop 派生）；预期业务响应：不豁免，走原 isExists 校验并拒绝
  verification_points:
  - 覆盖跨系统调用失败或异常返回
  - 验证调用方错误处理和业务状态
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  required: true
  applicability_basis:
  - risk.risk_type="模块链路"
- requirement_id: RTR-002-CASCADE-RECOVERY
  risk_id: R-002
  policy_rule_ids:
  - RTP-MODULE-LINK-001
  definition_id: RTEST-CASCADE-RECOVERY-001
  policy_version: '1.0'
  test_type: cascade_recovery
  title: 级联失败恢复验证
  business_context: null
  display_text: 级联失败恢复验证
  verification_points:
  - 覆盖局部故障向下游传播的场景
  - 验证隔离、恢复和业务状态收敛
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  required: true
  applicability_basis:
  - risk.risk_type="模块链路"
- requirement_id: RTR-003-DOWNSTREAM-LINKAGE
  risk_id: R-003
  policy_rule_ids:
  - RTP-MODULE-LINK-001
  definition_id: RTEST-DOWNSTREAM-LINKAGE-001
  policy_version: '1.0'
  test_type: downstream_linkage
  title: 下游联动验证
  business_context: null
  display_text: 下游联动验证
  verification_points:
  - 覆盖已确认的下游调用链
  - 验证上下游输入输出和状态传递
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  required: true
  applicability_basis:
  - risk.risk_type="模块链路"
- requirement_id: RTR-003-CROSS-SYSTEM-FAILURE
  risk_id: R-003
  policy_rule_ids:
  - RTP-MODULE-LINK-001
  definition_id: RTEST-CROSS-SYSTEM-FAILURE-001
  policy_version: '1.0'
  test_type: cross_system_failure
  title: 跨系统调用异常验证
  business_context: null
  display_text: 跨系统调用异常验证
  verification_points:
  - 覆盖跨系统调用失败或异常返回
  - 验证调用方错误处理和业务状态
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  required: true
  applicability_basis:
  - risk.risk_type="模块链路"
- requirement_id: RTR-003-CASCADE-RECOVERY
  risk_id: R-003
  policy_rule_ids:
  - RTP-MODULE-LINK-001
  definition_id: RTEST-CASCADE-RECOVERY-001
  policy_version: '1.0'
  test_type: cascade_recovery
  title: 级联失败恢复验证
  business_context: null
  display_text: 级联失败恢复验证
  verification_points:
  - 覆盖局部故障向下游传播的场景
  - 验证隔离、恢复和业务状态收敛
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  required: true
  applicability_basis:
  - risk.risk_type="模块链路"
- requirement_id: RTR-004-PRESSURE
  risk_id: R-004
  policy_rule_ids:
  - RTP-HIGH-BASE-ENGINE-PRESSURE-001
  definition_id: RTEST-PRESSURE-001
  policy_version: '1.0'
  test_type: pressure
  title: 压测
  business_context: null
  display_text: 压测
  verification_points:
  - 明确压测负载、数据量和持续时间
  - 验证吞吐量、延迟和资源水位达到目标
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  - test_report_available
  required: true
  applicability_basis:
  - risk.risk_level="高"
  - asset.categories=["数据分析工场", "计算中间件"]
- requirement_id: RTR-004-AVAILABILITY
  risk_id: R-004
  policy_rule_ids:
  - RTP-HIGH-BASE-ENGINE-AVAILABILITY-001
  definition_id: RTEST-AVAILABILITY-001
  policy_version: '1.0'
  test_type: availability
  title: 可用性测试
  business_context: null
  display_text: 可用性测试
  verification_points:
  - 覆盖单节点、单实例或关键依赖故障
  - 验证切换、降级和恢复期间的可用性目标
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  - test_report_available
  required: true
  applicability_basis:
  - risk.risk_level="高"
  - asset.categories=["数据分析工场", "计算中间件"]
- requirement_id: RTR-004-CONFIG-EFFECT
  risk_id: R-004
  policy_rule_ids:
  - RTP-CONFIGURATION-001
  definition_id: RTEST-CONFIG-EFFECT-001
  policy_version: '1.0'
  test_type: configuration_effect
  title: 配置变更生效验证
  business_context: 配置项：governanceStationAdmin 调用基址/超时（名称设计定）；变更前值：无（新增）；变更后值：设计阶段闭合；生效范围：scriptis-server
    全部实例；生效方式：设计阶段定
  display_text: 配置变更生效验证：配置项：governanceStationAdmin 调用基址/超时（名称设计定）；变更前值：无（新增）；变更后值：设计阶段闭合；生效范围：scriptis-server
    全部实例；生效方式：设计阶段定
  verification_points:
  - 验证目标配置值在声明范围内生效
  - 验证生效后的业务行为符合预期
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  required: true
  applicability_basis:
  - risk.risk_type="参数配置"
- requirement_id: RTR-004-CONFIG-ROLLBACK
  risk_id: R-004
  policy_rule_ids:
  - RTP-CONFIGURATION-001
  definition_id: RTEST-CONFIG-ROLLBACK-001
  policy_version: '1.0'
  test_type: configuration_rollback
  title: 配置回滚验证
  business_context: null
  display_text: 配置回滚验证
  verification_points:
  - 验证配置可恢复至变更前状态
  - 验证回滚后的业务行为和数据状态
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  required: true
  applicability_basis:
  - risk.risk_type="参数配置"
- requirement_id: RTR-004-CONFIG-BOUNDARY
  risk_id: R-004
  policy_rule_ids:
  - RTP-CONFIGURATION-001
  definition_id: RTEST-CONFIG-BOUNDARY-001
  policy_version: '1.0'
  test_type: configuration_boundary
  title: 配置边界值验证
  business_context: 配置项：超时上限；合法边界：建议 ≤3s；非法边界：0 或未设置；预期保护行为：禁止无限等待，超时降级为 aiSwitch=false
  display_text: 配置边界值验证：配置项：超时上限；合法边界：建议 ≤3s；非法边界：0 或未设置；预期保护行为：禁止无限等待，超时降级为 aiSwitch=false
  verification_points:
  - 覆盖最小值、最大值和越界值
  - 验证非法配置的拒绝或保护行为
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  required: true
  applicability_basis:
  - risk.risk_type="参数配置"
- requirement_id: RTR-005-PRESSURE
  risk_id: R-005
  policy_rule_ids:
  - RTP-HIGH-BASE-ENGINE-PRESSURE-001
  - RTP-PERFORMANCE-CAPACITY-001
  definition_id: RTEST-PRESSURE-001
  policy_version: '1.0'
  test_type: pressure
  title: 压测
  business_context: 并发量：未提供；数据量：未提供；持续时间：未提供；吞吐量目标：未提供；延迟目标：较原路径增加 ≤ 1s（需求未量化基线）；资源阈值：未提供
  display_text: 压测：并发量：未提供；数据量：未提供；持续时间：未提供；吞吐量目标：未提供；延迟目标：较原路径增加 ≤ 1s（需求未量化基线）；资源阈值：未提供
  verification_points:
  - 明确压测负载、数据量和持续时间
  - 验证吞吐量、延迟和资源水位达到目标
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  - test_report_available
  required: true
  applicability_basis:
  - risk.risk_level="高"
  - asset.categories=["数据分析工场", "计算中间件"]
  - risk.risk_type="性能容量"
- requirement_id: RTR-005-AVAILABILITY
  risk_id: R-005
  policy_rule_ids:
  - RTP-HIGH-BASE-ENGINE-AVAILABILITY-001
  definition_id: RTEST-AVAILABILITY-001
  policy_version: '1.0'
  test_type: availability
  title: 可用性测试
  business_context: null
  display_text: 可用性测试
  verification_points:
  - 覆盖单节点、单实例或关键依赖故障
  - 验证切换、降级和恢复期间的可用性目标
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  - test_report_available
  required: true
  applicability_basis:
  - risk.risk_level="高"
  - asset.categories=["数据分析工场", "计算中间件"]
- requirement_id: RTR-005-CAPACITY
  risk_id: R-005
  policy_rule_ids:
  - RTP-PERFORMANCE-CAPACITY-001
  definition_id: RTEST-CAPACITY-001
  policy_version: '1.0'
  test_type: capacity
  title: 容量评估验证
  business_context: null
  display_text: 容量评估验证
  verification_points:
  - 验证目标数据量或请求量下的容量余量
  - 明确容量上限、扩容阈值和风险点
  evidence_requirements:
  - test_case_generated
  - review_passed
  - execution_passed
  - test_report_available
  required: true
  applicability_basis:
  - risk.risk_type="性能容量"
stabilityDesignObligations:
- obligation_id: R-001-handling_strategy
  risk_id: R-001
  concern_type: handling_strategy
  concern: 处理方案
  required: true
  fact_fields:
  - 处理对象
  - 实施动作
  - 预期结果
  target_section_roles:
  - architecture
  - configuration
  - data
- obligation_id: R-001-exception_path
  risk_id: R-001
  concern_type: exception_path
  concern: 异常路径
  required: true
  fact_fields:
  - 触发条件
  - 处理动作
  - 业务结果
  target_section_roles:
  - exception
  - degradation
  - architecture
- obligation_id: R-001-recovery_strategy
  risk_id: R-001
  concern_type: recovery_strategy
  concern: 恢复方式
  required: true
  fact_fields:
  - 恢复触发
  - 恢复动作
  - 成功标准
  target_section_roles:
  - recovery
  - rollback
  - exception
- obligation_id: R-001-compatibility
  risk_id: R-001
  concern_type: compatibility
  concern: 兼容性
  required: true
  fact_fields:
  - 兼容对象
  - 兼容策略
  target_section_roles:
  - compatibility
  - architecture
- obligation_id: R-001-degradation
  risk_id: R-001
  concern_type: degradation
  concern: 降级与熔断
  required: true
  fact_fields:
  - 降级触发
  - 降级动作
  target_section_roles:
  - degradation
  - exception
  - recovery
- obligation_id: R-001-observability
  risk_id: R-001
  concern_type: observability
  concern: 监控与告警
  required: true
  fact_fields:
  - 监控指标
  - 告警条件
  target_section_roles:
  - monitoring
  - exception
- obligation_id: R-002-handling_strategy
  risk_id: R-002
  concern_type: handling_strategy
  concern: 处理方案
  required: true
  fact_fields:
  - 处理对象
  - 实施动作
  - 预期结果
  target_section_roles:
  - architecture
  - configuration
  - data
- obligation_id: R-002-exception_path
  risk_id: R-002
  concern_type: exception_path
  concern: 异常路径
  required: true
  fact_fields:
  - 触发条件
  - 处理动作
  - 业务结果
  target_section_roles:
  - exception
  - degradation
  - architecture
- obligation_id: R-002-recovery_strategy
  risk_id: R-002
  concern_type: recovery_strategy
  concern: 恢复方式
  required: true
  fact_fields:
  - 恢复触发
  - 恢复动作
  - 成功标准
  target_section_roles:
  - recovery
  - rollback
  - exception
- obligation_id: R-002-compatibility
  risk_id: R-002
  concern_type: compatibility
  concern: 兼容性
  required: true
  fact_fields:
  - 兼容对象
  - 兼容策略
  target_section_roles:
  - compatibility
  - architecture
- obligation_id: R-002-degradation
  risk_id: R-002
  concern_type: degradation
  concern: 降级与熔断
  required: true
  fact_fields:
  - 降级触发
  - 降级动作
  target_section_roles:
  - degradation
  - exception
  - recovery
- obligation_id: R-002-observability
  risk_id: R-002
  concern_type: observability
  concern: 监控与告警
  required: true
  fact_fields:
  - 监控指标
  - 告警条件
  target_section_roles:
  - monitoring
  - exception
- obligation_id: R-003-handling_strategy
  risk_id: R-003
  concern_type: handling_strategy
  concern: 处理方案
  required: true
  fact_fields:
  - 处理对象
  - 实施动作
  - 预期结果
  target_section_roles:
  - architecture
  - configuration
  - data
- obligation_id: R-003-exception_path
  risk_id: R-003
  concern_type: exception_path
  concern: 异常路径
  required: true
  fact_fields:
  - 触发条件
  - 处理动作
  - 业务结果
  target_section_roles:
  - exception
  - degradation
  - architecture
- obligation_id: R-003-recovery_strategy
  risk_id: R-003
  concern_type: recovery_strategy
  concern: 恢复方式
  required: true
  fact_fields:
  - 恢复触发
  - 恢复动作
  - 成功标准
  target_section_roles:
  - recovery
  - rollback
  - exception
- obligation_id: R-003-compatibility
  risk_id: R-003
  concern_type: compatibility
  concern: 兼容性
  required: true
  fact_fields:
  - 兼容对象
  - 兼容策略
  target_section_roles:
  - compatibility
  - architecture
- obligation_id: R-003-degradation
  risk_id: R-003
  concern_type: degradation
  concern: 降级与熔断
  required: true
  fact_fields:
  - 降级触发
  - 降级动作
  target_section_roles:
  - degradation
  - exception
  - recovery
- obligation_id: R-003-observability
  risk_id: R-003
  concern_type: observability
  concern: 监控与告警
  required: true
  fact_fields:
  - 监控指标
  - 告警条件
  target_section_roles:
  - monitoring
  - exception
- obligation_id: R-004-handling_strategy
  risk_id: R-004
  concern_type: handling_strategy
  concern: 处理方案
  required: true
  fact_fields:
  - 处理对象
  - 实施动作
  - 预期结果
  target_section_roles:
  - architecture
  - configuration
  - data
- obligation_id: R-004-exception_path
  risk_id: R-004
  concern_type: exception_path
  concern: 异常路径
  required: true
  fact_fields:
  - 触发条件
  - 处理动作
  - 业务结果
  target_section_roles:
  - exception
  - degradation
  - architecture
- obligation_id: R-004-recovery_strategy
  risk_id: R-004
  concern_type: recovery_strategy
  concern: 恢复方式
  required: true
  fact_fields:
  - 恢复触发
  - 恢复动作
  - 成功标准
  target_section_roles:
  - recovery
  - rollback
  - exception
- obligation_id: R-004-rollback
  risk_id: R-004
  concern_type: rollback
  concern: 回滚与止损
  required: true
  fact_fields:
  - 回滚触发
  - 回滚动作
  - 验证标准
  target_section_roles:
  - rollback
  - recovery
  - configuration
- obligation_id: R-004-observability
  risk_id: R-004
  concern_type: observability
  concern: 监控与告警
  required: true
  fact_fields:
  - 监控指标
  - 告警条件
  target_section_roles:
  - monitoring
  - exception
- obligation_id: R-005-handling_strategy
  risk_id: R-005
  concern_type: handling_strategy
  concern: 处理方案
  required: true
  fact_fields:
  - 处理对象
  - 实施动作
  - 预期结果
  target_section_roles:
  - architecture
  - configuration
  - data
- obligation_id: R-005-exception_path
  risk_id: R-005
  concern_type: exception_path
  concern: 异常路径
  required: true
  fact_fields:
  - 触发条件
  - 处理动作
  - 业务结果
  target_section_roles:
  - exception
  - degradation
  - architecture
- obligation_id: R-005-recovery_strategy
  risk_id: R-005
  concern_type: recovery_strategy
  concern: 恢复方式
  required: true
  fact_fields:
  - 恢复触发
  - 恢复动作
  - 成功标准
  target_section_roles:
  - recovery
  - rollback
  - exception
- obligation_id: R-005-capacity
  risk_id: R-005
  concern_type: capacity
  concern: 容量与性能
  required: true
  fact_fields:
  - 目标负载
  - 容量阈值
  target_section_roles:
  - performance
  - monitoring
- obligation_id: R-005-observability
  risk_id: R-005
  concern_type: observability
  concern: 监控与告警
  required: true
  fact_fields:
  - 监控指标
  - 告警条件
  target_section_roles:
  - monitoring
  - exception
stabilityDesignConcerns:
- concern_id: R-001-handling_strategy
  risk_id: R-001
  concern_type: handling_strategy
  concern: 处理方案
  required: true
  design_response: '- **处理对象**：setProxyUser 非本人切换链路对 Linkis governanceStationAdmin
    的新增同步只读调用（HC-001，详见 http-calls.json）。

    - **实施动作**：在 `DssProxyUserController.setProxyUserCookie` 的授权校验前，新增独立私有方法 `queryAiSwitch()`——复用既有先例三要素：①URL
    复用 `LinkisConnConf.LINKIS_URL + "/api/rest_j/v1/jobhistory/governanceStationAdmin"`（与
    `ResourceManageClient.CONF_GOVERNANCE_ADMIN_PATH` 同源，无新配置项）；②HTTP 客户端复用 `HttpClientUtil.invokeGet(path,
    headers, null, "utf-8")`（dss-commons/dss-common-server-webank，RequestConfig 自带
    connectTimeout/socketTimeout/connectionRequestTimeout 统一超时）；③认证复用 Token header（`LINKIS_RESOURCE_ADMIN_TOKEN_KEY/VALUE`
    + user key，与 HttpResourceManageClient.java:276-278 同款——rest_j 路径需认证，非 mg 白名单免登录路径）。

    - **预期结果**：aiSwitch=true 时豁免链路可用；任何失败情形（不可达/超时/非200/解析失败/字段缺失）返回 false 走原校验，不抛异常、不阻断切换。


    **不直接复用 `HttpResourceManageClient.governanceStationAdmin()` 方法的理由**：该方法非 200 时抛
    `DSSRuntimeException`（HttpResourceManageClient.java:280-283），与豁免链路"降级不抛"语义冲突；本设计在其同源范式上实现独立的降级语义封装。'
  design_facts:
    处理对象: setProxyUser 非本人切换链路对 Linkis governanceStationAdmin 的新增同步只读调用（HC-001，详见
      http-calls.json）。
    实施动作: 在 `DssProxyUserController.setProxyUserCookie` 的授权校验前，新增独立私有方法 `queryAiSwitch()`——复用既有先例三要素：①URL
      复用 `LinkisConnConf.LINKIS_URL + "/api/rest_j/v1/jobhistory/governanceStationAdmin"`（
    预期结果: aiSwitch=true 时豁免链路可用；任何失败情形（不可达/超时/非200/解析失败/字段缺失）返回 false 走原校验，不抛异常、不阻断切换。
  design_reference: §1.1.2
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-001-exception_path
  risk_id: R-001
  concern_type: exception_path
  concern: 异常路径
  required: true
  design_response: '- **触发条件**：governanceStationAdmin 连接拒绝、读/连超时、HTTP 非 200、JSON 解析失败、`data.aiSwitch`
    缺失或非布尔。

    - **处理动作**：catch 全部异常类型，`LOGGER.warn` 留痕（完整请求 URL、异常类型/根因、耗时毫秒），返回 `false`。

    - **业务结果**：本次请求按 aiSwitch=false 处理，走原 isExists 校验；setProxyUser 响应与现网基线一致（AC3.1/AC1.3）。'
  design_facts:
    触发条件: governanceStationAdmin 连接拒绝、读/连超时、HTTP 非 200、JSON 解析失败、`data.aiSwitch` 缺失或非布尔。
    处理动作: catch 全部异常类型，`LOGGER.warn` 留痕（完整请求 URL、异常类型/根因、耗时毫秒），返回 `false`。
    业务结果: 本次请求按 aiSwitch=false 处理，走原 isExists 校验；setProxyUser 响应与现网基线一致（AC3.1/AC1.3）。
  design_reference: §1.2.2
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-001-recovery_strategy
  risk_id: R-001
  concern_type: recovery_strategy
  concern: 恢复方式
  required: true
  design_response: 'R-001 + 恢复方式（recovery/rollback/exception 承接）


    - **恢复触发**：Linkis 恢复可达/部署版本补齐 aiSwitch 字段。

    - **恢复动作**：无需任何操作（每次请求实时查询，下一请求自动恢复豁免能力——无缓存无状态）。

    - **成功标准**：aiSwitch=true 请求实测豁免成功（AC2.1）。'
  design_facts:
    恢复触发: Linkis 恢复可达/部署版本补齐 aiSwitch 字段。
    恢复动作: 无需任何操作（每次请求实时查询，下一请求自动恢复豁免能力——无缓存无状态）。
    成功标准: aiSwitch=true 请求实测豁免成功（AC2.1）。
  design_reference: §2.8
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-001-compatibility
  risk_id: R-001
  concern_type: compatibility
  concern: 兼容性
  required: true
  design_response: '### R-001 + 兼容（compatibility/architecture 承接）


    - **兼容对象**：Linkis governanceStationAdmin 响应体（仅追加 aiSwitch，既有 5 字段不变）。

    - **兼容策略**：Gson 反序列化对未知字段容忍、缺失字段返回 null；DSS 侧不依赖版本号探测，按"字段缺失=未开启"统一降级——新旧 Linkis
    版本共存时行为确定性一致。


    ### R-002 + 兼容（compatibility/architecture 承接）


    - **兼容对象**：既有授权体系（dss_proxy_user 表、isExists 校验、DS_PROXY_SELF_ENABLE 本人分支）。

    - **兼容策略**：豁免仅是校验链路上的前置旁路，未命中旁路时原链路逐字节执行；授权管理流程（add/revoke/ITSM 导入）零触碰。


    ### R-003 + 兼容（compatibility/architecture 承接）


    - **兼容对象**：release-server 继承路由及其调用方。

    - **兼容策略**：继承方零代码改动；豁免行为对 release 路由调用方透明（多了一条免授权切换的通路，原授权通路保持）；回归显式覆盖 release
    路由三种请求（本人/授权切换/无授权拒绝）。'
  design_facts:
    兼容对象: Linkis governanceStationAdmin 响应体（仅追加 aiSwitch，既有 5 字段不变）。
    兼容策略: Gson 反序列化对未知字段容忍、缺失字段返回 null；DSS 侧不依赖版本号探测，按"字段缺失=未开启"统一降级——新旧 Linkis 版本共存时行为确定性一致。
  design_reference: §2.5
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-001-degradation
  risk_id: R-001
  concern_type: degradation
  concern: 降级与熔断
  required: true
  design_response: '### R-001 + 降级（degradation/exception/recovery 承接）


    - **降级触发**：七情形（连接拒绝/超时/非200/解析失败/字段缺失或非法/Token 失效/网络不通）。

    - **降级动作**：返回 aiSwitch=false → 本次请求走原 isExists 校验；warn 留痕（URL/异常/耗时）。


    ### R-002 + 降级（degradation 承接）


    - **降级触发**：aiSwitch=false（配置关闭或部署版本低于 release_v2.1.7）。

    - **降级动作**：豁免链路整体旁路，全部请求走原校验（与升级前行为一致）。


    ### R-003 + 降级（degradation 承接）


    - **降级触发**：release 路由下 Linkis 不可用（同 R-001 七情形）。

    - **降级动作**：与 scriptis 路由相同降级（继承同一方法，无差异化分支）。


    ### R-004 + 降级（degradation 承接）


    - **降级触发**：配置项缺失/值失效/网络不通（部署缺陷面）。

    - **降级动作**：同 R-001 catch-all 降级；上线核对清单（Token 值有效性 + 连通性）作为部署检查项而非启动硬校验。'
  design_facts:
    降级触发: 七情形（连接拒绝/超时/非200/解析失败/字段缺失或非法/Token 失效/网络不通）。
    降级动作: 返回 aiSwitch=false → 本次请求走原 isExists 校验；warn 留痕（URL/异常/耗时）。
  design_reference: §2.7
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-001-observability
  risk_id: R-001
  concern_type: observability
  concern: 监控与告警
  required: true
  design_response: "### R-001~R-005 + 监控（monitoring/exception 承接，5 条 observability\
    \ 义务合并承接）\n\n- **监控指标**（复用现有 LOGGER 体系，不引入新监控组件）：\n  1. `queryAiSwitch` 降级 warn\
    \ 日志速率（关键字：URL + 异常类型）——R-001/R-004 健康度；\n  2. 豁免命中 info 日志速率（bypass 关键字）——R-002\
    \ 豁免使用量；\n  3. queryAiSwitch 耗时（warn 留痕内含 elapsed）——R-005 延迟观测；\n  4. 原有 \"have\
    \ no permission\" 日志速率对比——R-002/R-003 拒绝率变化（灰度期应见 _ai 目标拒绝率下降）；\n  5. Token 认证失败类\
    \ warn（401/403 关键字）速率——R-004 配置健康度；\n  6. release 路由 setProxyUser 拒绝率与豁免命中率——R-003\
    \ 继承路由行为一致性观测（与 scriptis 路由同口径对比）。\n- **告警条件**（接现有日志告警平台关键字规则）：\n  1. 降级 warn\
    \ 持续出现（>5 分钟连续）→ Linkis 依赖异常（R-001/R-004）；\n  2. 豁免命中日志中出现非预期目标模式 → 越权嫌疑（R-002，人工核查）；\n\
    \  3. elapsed 接近超时上限频次上升 → R-005 延迟风险；\n  4. release 路由拒绝率异常上升 → 路由行为差异人工核查（R-003）；\n\
    \  5. 认证失败 warn 连续出现 → Token 配置失效，触发部署核对清单（R-004）。\n- **留痕格式**：沿用现有 \"user {}\
    \ try to add user proxy cookie, params:{}.\" 格式族，新增豁免结果字段（R2.3 审计要求）。"
  design_facts:
    监控指标: '`queryAiSwitch` 降级 warn 日志速率（关键字：URL + 异常类型）——R-001/R-004 健康度'
    告警条件: 降级 warn 持续出现（>5 分钟连续）→ Linkis 依赖异常（R-001/R-004）
  design_reference: §2.9
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-002-handling_strategy
  risk_id: R-002
  concern_type: handling_strategy
  concern: 处理方案
  required: true
  design_response: '- **处理对象**：setProxyUser 授权边界的条件性放宽（isExists 豁免判定）。

    - **实施动作**：豁免目标集合按精确全等构造：`{userName + "_ai"}` ∪（当前代理用户非空时 `{当前代理用户 + "_ai"}`）；判定次序：①hadoop
    两拒特判（最先）→ ②queryAiSwitch() → ③目标集合全等判定 → ④命中跳过 isExists 直接走清旧 cookie + 写代理 cookie
    成功路径。后缀常量 `_ai` 以代码常量定义（与 Linkis `linkis.ai.user.suffix` 默认值对齐，本版本不在 DSS 侧做可配置化，避免新增配置面；若后续
    Linkis 侧改后缀，DSS 随版本同步）。

    - **预期结果**：豁免仅命中两类本人派生目标；他人派生 `_ai`（otheruser_ai）、任意非 `_ai` 目标全部走原 isExists 校验；本人切换分支（DS_PROXY_SELF_ENABLE）与参数校验零触碰。'
  design_facts:
    处理对象: setProxyUser 授权边界的条件性放宽（isExists 豁免判定）。
    实施动作: 豁免目标集合按精确全等构造：`{userName + "_ai"}` ∪（当前代理用户非空时 `{当前代理用户 + "_ai"}`）；判定次序：①hadoop
      两拒特判（最先）→ ②queryAiSwitch() → ③目标集合全等判定 → ④命中跳过 isExists 直接走清旧 cookie + 写代理 cookie
      成功路径。后缀常量 `_ai` 以代
    预期结果: 豁免仅命中两类本人派生目标；他人派生 `_ai`（otheruser_ai）、任意非 `_ai` 目标全部走原 isExists 校验；本人切换分支（DS_PROXY_SELF_ENABLE）与参数校验零触碰。
  design_reference: §1.1.3
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-002-exception_path
  risk_id: R-002
  concern_type: exception_path
  concern: 异常路径
  required: true
  design_response: '- **触发条件**：aiSwitch=true 但目标为越权目标（他人派生 `_ai`、任意非本人派生后缀用户）。

    - **处理动作**：目标集合全等判定不命中 → 走原 isExists 校验；拒绝时沿用现网错误文案 `"user X have no permission
    to proxy to user Y"`。

    - **业务结果**：越权目标被原授权体系拦截；豁免判定无越权放大。'
  design_facts:
    触发条件: aiSwitch=true 但目标为越权目标（他人派生 `_ai`、任意非本人派生后缀用户）。
    处理动作: 目标集合全等判定不命中 → 走原 isExists 校验；拒绝时沿用现网错误文案 `"user X have no permission to
      proxy to user Y"`。
    业务结果: 越权目标被原授权体系拦截；豁免判定无越权放大。
  design_reference: §1.2.3
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-002-recovery_strategy
  risk_id: R-002
  concern_type: recovery_strategy
  concern: 恢复方式
  required: true
  design_response: 'R-002 + 恢复方式（recovery 承接）


    - **恢复触发**：豁免误判（误放大/误拒绝）在测试或灰度中发现。

    - **恢复动作**：代码修复走 git revert/fix 流程；灰度期以 `linkis.ai.user.enable=false` 立即收回豁免入口（Linkis
    侧热加载开关，秒级生效）。

    - **成功标准**：开关关闭后全部请求走原校验（AC3.2）。'
  design_facts:
    恢复触发: 豁免误判（误放大/误拒绝）在测试或灰度中发现。
    恢复动作: 代码修复走 git revert/fix 流程；灰度期以 `linkis.ai.user.enable=false` 立即收回豁免入口（Linkis
      侧热加载开关，秒级生效）。
    成功标准: 开关关闭后全部请求走原校验（AC3.2）。
  design_reference: §2.8
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-002-compatibility
  risk_id: R-002
  concern_type: compatibility
  concern: 兼容性
  required: true
  design_response: '### R-001 + 兼容（compatibility/architecture 承接）


    - **兼容对象**：Linkis governanceStationAdmin 响应体（仅追加 aiSwitch，既有 5 字段不变）。

    - **兼容策略**：Gson 反序列化对未知字段容忍、缺失字段返回 null；DSS 侧不依赖版本号探测，按"字段缺失=未开启"统一降级——新旧 Linkis
    版本共存时行为确定性一致。


    ### R-002 + 兼容（compatibility/architecture 承接）


    - **兼容对象**：既有授权体系（dss_proxy_user 表、isExists 校验、DS_PROXY_SELF_ENABLE 本人分支）。

    - **兼容策略**：豁免仅是校验链路上的前置旁路，未命中旁路时原链路逐字节执行；授权管理流程（add/revoke/ITSM 导入）零触碰。


    ### R-003 + 兼容（compatibility/architecture 承接）


    - **兼容对象**：release-server 继承路由及其调用方。

    - **兼容策略**：继承方零代码改动；豁免行为对 release 路由调用方透明（多了一条免授权切换的通路，原授权通路保持）；回归显式覆盖 release
    路由三种请求（本人/授权切换/无授权拒绝）。'
  design_facts:
    兼容对象: Linkis governanceStationAdmin 响应体（仅追加 aiSwitch，既有 5 字段不变）。
    兼容策略: Gson 反序列化对未知字段容忍、缺失字段返回 null；DSS 侧不依赖版本号探测，按"字段缺失=未开启"统一降级——新旧 Linkis 版本共存时行为确定性一致。
  design_reference: §2.5
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-002-degradation
  risk_id: R-002
  concern_type: degradation
  concern: 降级与熔断
  required: true
  design_response: '### R-001 + 降级（degradation/exception/recovery 承接）


    - **降级触发**：七情形（连接拒绝/超时/非200/解析失败/字段缺失或非法/Token 失效/网络不通）。

    - **降级动作**：返回 aiSwitch=false → 本次请求走原 isExists 校验；warn 留痕（URL/异常/耗时）。


    ### R-002 + 降级（degradation 承接）


    - **降级触发**：aiSwitch=false（配置关闭或部署版本低于 release_v2.1.7）。

    - **降级动作**：豁免链路整体旁路，全部请求走原校验（与升级前行为一致）。


    ### R-003 + 降级（degradation 承接）


    - **降级触发**：release 路由下 Linkis 不可用（同 R-001 七情形）。

    - **降级动作**：与 scriptis 路由相同降级（继承同一方法，无差异化分支）。


    ### R-004 + 降级（degradation 承接）


    - **降级触发**：配置项缺失/值失效/网络不通（部署缺陷面）。

    - **降级动作**：同 R-001 catch-all 降级；上线核对清单（Token 值有效性 + 连通性）作为部署检查项而非启动硬校验。'
  design_facts:
    降级触发: 七情形（连接拒绝/超时/非200/解析失败/字段缺失或非法/Token 失效/网络不通）。
    降级动作: 返回 aiSwitch=false → 本次请求走原 isExists 校验；warn 留痕（URL/异常/耗时）。
  design_reference: §2.7
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-002-observability
  risk_id: R-002
  concern_type: observability
  concern: 监控与告警
  required: true
  design_response: "### R-001~R-005 + 监控（monitoring/exception 承接，5 条 observability\
    \ 义务合并承接）\n\n- **监控指标**（复用现有 LOGGER 体系，不引入新监控组件）：\n  1. `queryAiSwitch` 降级 warn\
    \ 日志速率（关键字：URL + 异常类型）——R-001/R-004 健康度；\n  2. 豁免命中 info 日志速率（bypass 关键字）——R-002\
    \ 豁免使用量；\n  3. queryAiSwitch 耗时（warn 留痕内含 elapsed）——R-005 延迟观测；\n  4. 原有 \"have\
    \ no permission\" 日志速率对比——R-002/R-003 拒绝率变化（灰度期应见 _ai 目标拒绝率下降）；\n  5. Token 认证失败类\
    \ warn（401/403 关键字）速率——R-004 配置健康度；\n  6. release 路由 setProxyUser 拒绝率与豁免命中率——R-003\
    \ 继承路由行为一致性观测（与 scriptis 路由同口径对比）。\n- **告警条件**（接现有日志告警平台关键字规则）：\n  1. 降级 warn\
    \ 持续出现（>5 分钟连续）→ Linkis 依赖异常（R-001/R-004）；\n  2. 豁免命中日志中出现非预期目标模式 → 越权嫌疑（R-002，人工核查）；\n\
    \  3. elapsed 接近超时上限频次上升 → R-005 延迟风险；\n  4. release 路由拒绝率异常上升 → 路由行为差异人工核查（R-003）；\n\
    \  5. 认证失败 warn 连续出现 → Token 配置失效，触发部署核对清单（R-004）。\n- **留痕格式**：沿用现有 \"user {}\
    \ try to add user proxy cookie, params:{}.\" 格式族，新增豁免结果字段（R2.3 审计要求）。"
  design_facts:
    监控指标: 豁免命中 info 日志速率（bypass 关键字）——R-002 豁免使用量
    告警条件: 豁免命中日志中出现非预期目标模式 → 越权嫌疑（R-002，人工核查）
  design_reference: §2.9
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-003-handling_strategy
  risk_id: R-003
  concern_type: handling_strategy
  concern: 处理方案
  required: true
  design_response: '- **处理对象**：基类增强逻辑被 ScriptisProxyUserController（/dss/scriptis/proxy）与
    dss-framework-release-server-webank ProxyUserController 两个继承控制器同时继承。

    - **实施动作**：不修改任何继承方代码；豁免行为经继承自动生效；回归范围显式纳入 release-server 继承路由（§3.4 测试设计）。

    - **预期结果**：两个继承路由行为一致且可预期，无代码改动即无新增缺陷面。'
  design_facts:
    处理对象: 基类增强逻辑被 ScriptisProxyUserController（/dss/scriptis/proxy）与 dss-framework-release-server-webank
      ProxyUserController 两个继承控制器同时继承。
    实施动作: 不修改任何继承方代码；豁免行为经继承自动生效；回归范围显式纳入 release-server 继承路由（§3.4 测试设计）。
    预期结果: 两个继承路由行为一致且可预期，无代码改动即无新增缺陷面。
  design_reference: §1.1.4
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-003-exception_path
  risk_id: R-003
  concern_type: exception_path
  concern: 异常路径
  required: true
  design_response: '- **触发条件**：release-server 继承路由收到 setProxyUser 请求（发布场景调用方）。

    - **处理动作**：与 scriptis 路由完全相同的豁免判定链（继承生效，无特判差异）。

    - **业务结果**：release 路由行为与 scriptis 一致；调用方未配置授权时按原语义拒绝（不豁免），配置了授权或命中豁免集合时正常切换。'
  design_facts:
    触发条件: release-server 继承路由收到 setProxyUser 请求（发布场景调用方）。
    处理动作: 与 scriptis 路由完全相同的豁免判定链（继承生效，无特判差异）。
    业务结果: release 路由行为与 scriptis 一致；调用方未配置授权时按原语义拒绝（不豁免），配置了授权或命中豁免集合时正常切换。
  design_reference: §1.2.4
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-003-recovery_strategy
  risk_id: R-003
  concern_type: recovery_strategy
  concern: 恢复方式
  required: true
  design_response: 'R-003 + 恢复方式（recovery 承接）


    - **恢复触发**：release 路由豁免行为引发业务异常。

    - **恢复动作**：同 R-002——Linkis 侧开关关闭即全路由（scriptis+release）收回豁免；无需按路由分别处置。

    - **成功标准**：关闭后 release 路由行为回基线。'
  design_facts:
    恢复触发: release 路由豁免行为引发业务异常。
    恢复动作: 同 R-002——Linkis 侧开关关闭即全路由（scriptis+release）收回豁免；无需按路由分别处置。
    成功标准: 关闭后 release 路由行为回基线。
  design_reference: §2.8
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-003-compatibility
  risk_id: R-003
  concern_type: compatibility
  concern: 兼容性
  required: true
  design_response: '### R-001 + 兼容（compatibility/architecture 承接）


    - **兼容对象**：Linkis governanceStationAdmin 响应体（仅追加 aiSwitch，既有 5 字段不变）。

    - **兼容策略**：Gson 反序列化对未知字段容忍、缺失字段返回 null；DSS 侧不依赖版本号探测，按"字段缺失=未开启"统一降级——新旧 Linkis
    版本共存时行为确定性一致。


    ### R-002 + 兼容（compatibility/architecture 承接）


    - **兼容对象**：既有授权体系（dss_proxy_user 表、isExists 校验、DS_PROXY_SELF_ENABLE 本人分支）。

    - **兼容策略**：豁免仅是校验链路上的前置旁路，未命中旁路时原链路逐字节执行；授权管理流程（add/revoke/ITSM 导入）零触碰。


    ### R-003 + 兼容（compatibility/architecture 承接）


    - **兼容对象**：release-server 继承路由及其调用方。

    - **兼容策略**：继承方零代码改动；豁免行为对 release 路由调用方透明（多了一条免授权切换的通路，原授权通路保持）；回归显式覆盖 release
    路由三种请求（本人/授权切换/无授权拒绝）。'
  design_facts:
    兼容对象: Linkis governanceStationAdmin 响应体（仅追加 aiSwitch，既有 5 字段不变）。
    兼容策略: Gson 反序列化对未知字段容忍、缺失字段返回 null；DSS 侧不依赖版本号探测，按"字段缺失=未开启"统一降级——新旧 Linkis 版本共存时行为确定性一致。
  design_reference: §2.5
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-003-degradation
  risk_id: R-003
  concern_type: degradation
  concern: 降级与熔断
  required: true
  design_response: '### R-001 + 降级（degradation/exception/recovery 承接）


    - **降级触发**：七情形（连接拒绝/超时/非200/解析失败/字段缺失或非法/Token 失效/网络不通）。

    - **降级动作**：返回 aiSwitch=false → 本次请求走原 isExists 校验；warn 留痕（URL/异常/耗时）。


    ### R-002 + 降级（degradation 承接）


    - **降级触发**：aiSwitch=false（配置关闭或部署版本低于 release_v2.1.7）。

    - **降级动作**：豁免链路整体旁路，全部请求走原校验（与升级前行为一致）。


    ### R-003 + 降级（degradation 承接）


    - **降级触发**：release 路由下 Linkis 不可用（同 R-001 七情形）。

    - **降级动作**：与 scriptis 路由相同降级（继承同一方法，无差异化分支）。


    ### R-004 + 降级（degradation 承接）


    - **降级触发**：配置项缺失/值失效/网络不通（部署缺陷面）。

    - **降级动作**：同 R-001 catch-all 降级；上线核对清单（Token 值有效性 + 连通性）作为部署检查项而非启动硬校验。'
  design_facts:
    降级触发: 七情形（连接拒绝/超时/非200/解析失败/字段缺失或非法/Token 失效/网络不通）。
    降级动作: 返回 aiSwitch=false → 本次请求走原 isExists 校验；warn 留痕（URL/异常/耗时）。
  design_reference: §2.7
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-003-observability
  risk_id: R-003
  concern_type: observability
  concern: 监控与告警
  required: true
  design_response: "### R-001~R-005 + 监控（monitoring/exception 承接，5 条 observability\
    \ 义务合并承接）\n\n- **监控指标**（复用现有 LOGGER 体系，不引入新监控组件）：\n  1. `queryAiSwitch` 降级 warn\
    \ 日志速率（关键字：URL + 异常类型）——R-001/R-004 健康度；\n  2. 豁免命中 info 日志速率（bypass 关键字）——R-002\
    \ 豁免使用量；\n  3. queryAiSwitch 耗时（warn 留痕内含 elapsed）——R-005 延迟观测；\n  4. 原有 \"have\
    \ no permission\" 日志速率对比——R-002/R-003 拒绝率变化（灰度期应见 _ai 目标拒绝率下降）；\n  5. Token 认证失败类\
    \ warn（401/403 关键字）速率——R-004 配置健康度；\n  6. release 路由 setProxyUser 拒绝率与豁免命中率——R-003\
    \ 继承路由行为一致性观测（与 scriptis 路由同口径对比）。\n- **告警条件**（接现有日志告警平台关键字规则）：\n  1. 降级 warn\
    \ 持续出现（>5 分钟连续）→ Linkis 依赖异常（R-001/R-004）；\n  2. 豁免命中日志中出现非预期目标模式 → 越权嫌疑（R-002，人工核查）；\n\
    \  3. elapsed 接近超时上限频次上升 → R-005 延迟风险；\n  4. release 路由拒绝率异常上升 → 路由行为差异人工核查（R-003）；\n\
    \  5. 认证失败 warn 连续出现 → Token 配置失效，触发部署核对清单（R-004）。\n- **留痕格式**：沿用现有 \"user {}\
    \ try to add user proxy cookie, params:{}.\" 格式族，新增豁免结果字段（R2.3 审计要求）。"
  design_facts:
    监控指标: release 路由 setProxyUser 拒绝率与豁免命中率——R-003 继承路由行为一致性观测（与 scriptis 路由同口径对比）
    告警条件: release 路由拒绝率异常上升 → 路由行为差异人工核查（R-003）
  design_reference: §2.9
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-004-handling_strategy
  risk_id: R-004
  concern_type: handling_strategy
  concern: 处理方案
  required: true
  design_response: '- **处理对象**：governanceStationAdmin 调用的基址/超时/认证配置正确性。

    - **实施动作**：零新增配置项——基址复用 `LinkisConnConf.LINKIS_URL`（现网已配置且被 checkSuperAdmin 等既有调用使用），超时复用
    HttpClientUtil 统一 connectTimeout，认证复用 `LINKIS_RESOURCE_ADMIN_TOKEN_*` 既有配置；上线核对项仅两项：各环境
    `LINKIS_RESOURCE_ADMIN_TOKEN_*` 值有效性（运维核对）、scriptis-server→linkis-mg 网络连通性（联调验证）。

    - **预期结果**：不出现"新配置漏配致豁免静默失效"的部署缺陷面；配置与既有生产调用完全同源。'
  design_facts:
    处理对象: governanceStationAdmin 调用的基址/超时/认证配置正确性。
    实施动作: 零新增配置项——基址复用 `LinkisConnConf.LINKIS_URL`（现网已配置且被 checkSuperAdmin 等既有调用使用），超时复用
      HttpClientUtil 统一 connectTimeout，认证复用 `LINKIS_RESOURCE_ADMIN_TOKEN_*` 既有配置；上线核对项仅两项：各环境
      `LINKIS_RESOU
    预期结果: 不出现"新配置漏配致豁免静默失效"的部署缺陷面；配置与既有生产调用完全同源。
  design_reference: §1.1.5
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-004-exception_path
  risk_id: R-004
  concern_type: exception_path
  concern: 异常路径
  required: true
  design_response: '- **触发条件**：`LINKIS_RESOURCE_ADMIN_TOKEN_*` 配置缺失/值失效、scriptis-server
    与 linkis-mg 网络不通。

    - **处理动作**：同 R-001 降级路径（catch → warn 留痕 → 返回 false）；启动期不做连通性硬校验（避免阻断服务启动）。

    - **业务结果**：豁免能力静默降级为不可用，既有切换行为不受任何影响。'
  design_facts:
    触发条件: '`LINKIS_RESOURCE_ADMIN_TOKEN_*` 配置缺失/值失效、scriptis-server 与 linkis-mg 网络不通。'
    处理动作: 同 R-001 降级路径（catch → warn 留痕 → 返回 false）；启动期不做连通性硬校验（避免阻断服务启动）。
    业务结果: 豁免能力静默降级为不可用，既有切换行为不受任何影响。
  design_reference: §1.2.5
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-004-recovery_strategy
  risk_id: R-004
  concern_type: recovery_strategy
  concern: 恢复方式
  required: true
  design_response: 'R-004 + 恢复方式（recovery/rollback/configuration 承接）


    - **恢复触发**：配置错误（Token 失效/基址不可达）且需恢复豁免能力。

    - **恢复动作**：修复 `LINKIS_RESOURCE_ADMIN_TOKEN_*` 配置值或网络策略；无代码回滚需求（配置与既有生产调用同源，出错概率收敛于环境问题）。

    - **成功标准**：queryAiSwitch 实测返回 true（warn 消失）。'
  design_facts:
    恢复触发: 配置错误（Token 失效/基址不可达）且需恢复豁免能力。
    恢复动作: 修复 `LINKIS_RESOURCE_ADMIN_TOKEN_*` 配置值或网络策略；无代码回滚需求（配置与既有生产调用同源，出错概率收敛于环境问题）。
    成功标准: queryAiSwitch 实测返回 true（warn 消失）。
  design_reference: §2.8
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-004-rollback
  risk_id: R-004
  concern_type: rollback
  concern: 回滚与止损
  required: true
  design_response: 'R-004 + 回滚（rollback 承接，含 R-004-rollback 义务）


    - **回滚触发**：DSS 侧代码缺陷（豁免逻辑错误）需版本级回退。

    - **回滚动作**：git revert 单提交（唯一模块 dss-framework-proxy-user-service + Response DTO
    追加），重新构建发布；无配置/数据回滚（§10.2 需求文档）。

    - **验证标准**：回滚后 setProxyUser 全量行为与升级前基线逐字段一致（AC3.3）。'
  design_facts:
    回滚触发: DSS 侧代码缺陷（豁免逻辑错误）需版本级回退。
    回滚动作: git revert 单提交（唯一模块 dss-framework-proxy-user-service + Response DTO 追加），重新构建发布；无配置/数据回滚（§10.2
      需求文档）。
    验证标准: 回滚后 setProxyUser 全量行为与升级前基线逐字段一致（AC3.3）。
  design_reference: §2.8
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-004-observability
  risk_id: R-004
  concern_type: observability
  concern: 监控与告警
  required: true
  design_response: "### R-001~R-005 + 监控（monitoring/exception 承接，5 条 observability\
    \ 义务合并承接）\n\n- **监控指标**（复用现有 LOGGER 体系，不引入新监控组件）：\n  1. `queryAiSwitch` 降级 warn\
    \ 日志速率（关键字：URL + 异常类型）——R-001/R-004 健康度；\n  2. 豁免命中 info 日志速率（bypass 关键字）——R-002\
    \ 豁免使用量；\n  3. queryAiSwitch 耗时（warn 留痕内含 elapsed）——R-005 延迟观测；\n  4. 原有 \"have\
    \ no permission\" 日志速率对比——R-002/R-003 拒绝率变化（灰度期应见 _ai 目标拒绝率下降）；\n  5. Token 认证失败类\
    \ warn（401/403 关键字）速率——R-004 配置健康度；\n  6. release 路由 setProxyUser 拒绝率与豁免命中率——R-003\
    \ 继承路由行为一致性观测（与 scriptis 路由同口径对比）。\n- **告警条件**（接现有日志告警平台关键字规则）：\n  1. 降级 warn\
    \ 持续出现（>5 分钟连续）→ Linkis 依赖异常（R-001/R-004）；\n  2. 豁免命中日志中出现非预期目标模式 → 越权嫌疑（R-002，人工核查）；\n\
    \  3. elapsed 接近超时上限频次上升 → R-005 延迟风险；\n  4. release 路由拒绝率异常上升 → 路由行为差异人工核查（R-003）；\n\
    \  5. 认证失败 warn 连续出现 → Token 配置失效，触发部署核对清单（R-004）。\n- **留痕格式**：沿用现有 \"user {}\
    \ try to add user proxy cookie, params:{}.\" 格式族，新增豁免结果字段（R2.3 审计要求）。"
  design_facts:
    监控指标: Token 认证失败类 warn（401/403 关键字）速率——R-004 配置健康度
    告警条件: 认证失败 warn 连续出现 → Token 配置失效，触发部署核对清单（R-004）
  design_reference: §2.9
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-005-handling_strategy
  risk_id: R-005
  concern_type: handling_strategy
  concern: 处理方案
  required: true
  design_response: '- **处理对象**：setProxyUser 用户交互路径新增同步 HTTP 调用的延迟叠加。

    - **实施动作**：调用仅在"非本人切换且非 hadoop 两拒"时触发（本人切换与 hadoop 路径零开销）；正常路径为内网/网关毫秒级调用；超时上限由
    HttpClientUtil 统一超时兜底（设计确认现值，若现值 >3s 则为该方法单独传入更短超时参数——HttpClientUtil.invokeGet
    的超时来自其内部统一值，本设计不改公共工具类，延迟预算验证交测试阶段实测）。

    - **预期结果**：正常路径延迟增量毫秒级；最坏情况（超时）阻塞上限 = HttpClientUtil 统一超时值，且降级后本次请求走原校验。'
  design_facts:
    处理对象: setProxyUser 用户交互路径新增同步 HTTP 调用的延迟叠加。
    实施动作: 调用仅在"非本人切换且非 hadoop 两拒"时触发（本人切换与 hadoop 路径零开销）；正常路径为内网/网关毫秒级调用；超时上限由 HttpClientUtil
      统一超时兜底（设计确认现值，若现值 >3s 则为该方法单独传入更短超时参数——HttpClientUtil.invokeGet 的超时来自其内部统一值，本设计不改公共工具类，延迟预算验证交测试
    预期结果: 正常路径延迟增量毫秒级；最坏情况（超时）阻塞上限 = HttpClientUtil 统一超时值，且降级后本次请求走原校验。
  design_reference: §1.1.6
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-005-exception_path
  risk_id: R-005
  concern_type: exception_path
  concern: 异常路径
  required: true
  design_response: '- **触发条件**：governanceStationAdmin 响应时间超过 HttpClientUtil 统一超时。

    - **处理动作**：超时异常进入 R-001 同一 catch 降级路径。

    - **业务结果**：单次请求最坏阻塞 = 统一超时上限；不重试（避免延迟叠加，单次调用语义与开关热加载对齐）。'
  design_facts:
    触发条件: governanceStationAdmin 响应时间超过 HttpClientUtil 统一超时。
    处理动作: 超时异常进入 R-001 同一 catch 降级路径。
    业务结果: 单次请求最坏阻塞 = 统一超时上限；不重试（避免延迟叠加，单次调用语义与开关热加载对齐）。
  design_reference: §1.2.6
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-005-recovery_strategy
  risk_id: R-005
  concern_type: recovery_strategy
  concern: 恢复方式
  required: true
  design_response: 'R-005 + 恢复方式（recovery 承接）


    - **恢复触发**：延迟超标告警（见 §2.9）。

    - **恢复动作**：先关 Linkis 开关收回豁免（消除延迟源），再定位 Linkis 侧性能；DSS 侧无需回滚。

    - **成功标准**：关闭后 setProxyUser 延迟回基线。'
  design_facts:
    恢复触发: 延迟超标告警（见 §2.9）。
    恢复动作: 先关 Linkis 开关收回豁免（消除延迟源），再定位 Linkis 侧性能；DSS 侧无需回滚。
    成功标准: 关闭后 setProxyUser 延迟回基线。
  design_reference: §2.8
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-005-capacity
  risk_id: R-005
  concern_type: capacity
  concern: 容量与性能
  required: true
  design_response: '- **目标负载**：setProxyUser 现网切换请求 QPS 量级（用户交互触发，低频）；governanceStationAdmin
    既有调用方（页面初始化高频 + checkSuperAdmin）不受影响——本调用仅在切换时触发，频次远低于既有调用。

    - **容量阈值**：延迟增量 ≤1s（需求 §六）；超时上限 = HttpClientUtil 统一超时（设计确认现值）；单次调用不重试。

    - **容量证据计划**：测试阶段以降级路径实测耗时（AC3.1 场景）+ 正常路径 TP99 对比作为容量证据（呼应触点① RTEST-PRESSURE-001
    缺口）。'
  design_facts:
    目标负载: setProxyUser 现网切换请求 QPS 量级（用户交互触发，低频）；governanceStationAdmin 既有调用方（页面初始化高频
      + checkSuperAdmin）不受影响——本调用仅在切换时触发，频次远低于既有调用。
    容量阈值: 延迟增量 ≤1s（需求 §六）；超时上限 = HttpClientUtil 统一超时（设计确认现值）；单次调用不重试。
  design_reference: §2.4
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
- concern_id: R-005-observability
  risk_id: R-005
  concern_type: observability
  concern: 监控与告警
  required: true
  design_response: "### R-001~R-005 + 监控（monitoring/exception 承接，5 条 observability\
    \ 义务合并承接）\n\n- **监控指标**（复用现有 LOGGER 体系，不引入新监控组件）：\n  1. `queryAiSwitch` 降级 warn\
    \ 日志速率（关键字：URL + 异常类型）——R-001/R-004 健康度；\n  2. 豁免命中 info 日志速率（bypass 关键字）——R-002\
    \ 豁免使用量；\n  3. queryAiSwitch 耗时（warn 留痕内含 elapsed）——R-005 延迟观测；\n  4. 原有 \"have\
    \ no permission\" 日志速率对比——R-002/R-003 拒绝率变化（灰度期应见 _ai 目标拒绝率下降）；\n  5. Token 认证失败类\
    \ warn（401/403 关键字）速率——R-004 配置健康度；\n  6. release 路由 setProxyUser 拒绝率与豁免命中率——R-003\
    \ 继承路由行为一致性观测（与 scriptis 路由同口径对比）。\n- **告警条件**（接现有日志告警平台关键字规则）：\n  1. 降级 warn\
    \ 持续出现（>5 分钟连续）→ Linkis 依赖异常（R-001/R-004）；\n  2. 豁免命中日志中出现非预期目标模式 → 越权嫌疑（R-002，人工核查）；\n\
    \  3. elapsed 接近超时上限频次上升 → R-005 延迟风险；\n  4. release 路由拒绝率异常上升 → 路由行为差异人工核查（R-003）；\n\
    \  5. 认证失败 warn 连续出现 → Token 配置失效，触发部署核对清单（R-004）。\n- **留痕格式**：沿用现有 \"user {}\
    \ try to add user proxy cookie, params:{}.\" 格式族，新增豁免结果字段（R2.3 审计要求）。"
  design_facts:
    监控指标: queryAiSwitch 耗时（warn 留痕内含 elapsed）——R-005 延迟观测
    告警条件: elapsed 接近超时上限频次上升 → R-005 延迟风险
  design_reference: §2.9
  response_origin: existing_design
  evidence_document: docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md
  assessment_basis: 最终设计存在可定位且最小业务事实完整的具体响应
  status: 已覆盖
  status_source: provider
stabilityDesignQualityAssessment:
  scope: 运营稳定性
  evaluation_status: 已完成
  verdict: 符合标准
  risk_total: 5
  risk_covered: 5
  risk_coverage_rate: 100.0
  concern_total: 28
  concern_covered: 28
  concern_coverage_rate: 100.0
  unresolved_high_risk_count: 0
  basis: 本设计已完整承接已识别的运营稳定性风险。
---

# 任务上下文

## 📊 阶段执行记录（需求/设计相位）

> 仅记录需求相位（Stage 0/1/1.1）与设计相位（Stage 2/2.1）的终态（成功/跳过/失败），其余阶段不记。
> 每 stage 至多一行；写入前若该 stage 已有行则先替换旧行（兼容"失败后重试成功"）。
> 写入规则与点位见 `stage-hooks/engine.md` 步骤10.5（成功/跳过）与强制恢复流程步骤2.5（失败）。

| 阶段 | 名称 | 状态 | 产出物 | 完成时间 | 备注 |
|------|------|------|--------|----------|------|
| 0 | 需求澄清 | 成功 | - | 2026-11-12T11:15:00+08:00 | 主会话内联执行（子代理403降级）；澄清结论6项已入 decisionLog/stageOutputs |
| 1 | 需求分析 | 成功 | docs/1.26.0/requirements/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_需求.md | 2026-11-12T11:30:00+08:00 | 主会话内联执行（子代理403降级）；12章模板全量填写，AC 9条 |
| 1.2 | 需求知识同步 | 跳过 | - | 2026-11-12T14:20:00+08:00 | P7模块索引未构建（用户跳过），无同步目标 |
| 1.1 | 需求检视 | 成功 | - | 2026-11-12T14:05:00+08:00 | 已应用3项优化（OPT-001~003），需求文档v1.1；触点①完成：5条风险（R-001~R-005全高）+6项稳定性产物+需求风险摘要章节；DPMS同步本会话无MCP已降级跳过；组织基线补登system:dss is_base_engine=false（用户确认） |
| 1.6 | 组件依赖分析 | 成功 | service-dependency-analysis.md / http-calls.json / service-dependency-graph.mermaid | 2026-11-12T14:40:00+08:00 | 主会话内联执行；发现既有调用先例（HttpResourceManageClient），闭合OPT-001调用基址口径=复用CONF_GOVERNANCE_ADMIN_PATH无新配置项 |

## 📋 任务基本信息

**任务名称**: ai-user-switch
**需求类型**: ENHANCE（置信度 0.95，用户已确认；优先级 P2）
**执行模式**: 快速模式
**竞品分析摘要**: 业界先例：数据平台 AI 代理普遍采用代理用户/impersonation 模式（Trino user impersonation、Superset proxy user、Metabase impersonation、Databricks service principal run-as、CNCF agentic identity propagation）。安全警示（OWASP GenAI LLM Top 10、CWE-284 confused deputy）：跳过终端用户授权校验的代理切换存在身份遮蔽与越权风险。业界共识设计义务：①旁路范围收窄（目标用户须由请求者本人派生，如 _ai 后缀）②平台级开关门控（aiSwitch）③切换成功/失败均落审计日志。检索质量：中英文双检索命中一手安全标准与厂商文档，结论可信。
**价值收益摘要**: （用户跳过价值收益评估）

## 📋 项目上下文确认

**项目上下文状态**: 已确认（P4 用户确认正确）
**生成时间戳**: 2026-11-11T09:30:00+08:00（P1 内联生成；project-context-analyzer 子代理因模型白名单 403 降级为主会话内联分析）

## 📚 知识库状态

**知识库状态**: 待P7决策
**模块索引**: 未构建

## 📝 需求描述

当前 DSS 的 `/dss/scriptis/proxy/setProxyUser` 接口（`dss-framework/dss-framework-proxy-user-service` 的 `DssProxyUserController.setProxyUserCookie`，经 `dss-apps/dss-scriptis-server` 的 `ScriptisProxyUserController`（`@RequestMapping("/dss/scriptis/proxy")`）暴露）在切换代理用户前强制执行代理授权校验 `dssProxyUserService.isExists(userName, proxyUserName, workspace)`，未授权用户返回 "have no permission to proxy" 无法切换。

本需求：切换前先查询 Linkis 的 `/api/jobhistory/governanceStationAdmin` 接口，若返回 `data.aiSwitch == true`，则跳过代理授权校验（isExists），直接切换到 `_ai` 用户（如 alexyang → alexyang_ai），写入 proxy cookie 完成切换。

参考代码库：`C:\GitProject\bdp\` 下所有项目；Linkis 参考分支 release_v2.1.7（本地实际远端分支为 `origin/dev-2.1.7`，无 release_v2.1.7 分支名，需结合实际代码确认）。已核实事实：governanceStationAdmin 接口实现不在本机任何仓库（全机检索仅 `wedatasphere-linkis/wds-linkis-dist/package/conf*/linkis-mg-white-url.properties` 白名单配置出现该 URL；`aiSwitch` 字段源码零命中），属 Linkis 侧部署契约/并行依赖。

**待澄清问题**（Stage 0 需求澄清输入）：
1. 跳过校验的目标范围：仅限请求者本人派生的 `_ai` 后缀目标用户，还是所有代理用户？（竞品分析强烈建议仅本人派生目标）
2. 调用方式与降级：DSS 后端如何调用 governanceStationAdmin（HTTP 直调 Linkis？走网关？）；接口不可达/超时/字段缺失时回退原校验还是直接报错？
3. 开关优先级：`ProxyUserConfiguration.isProxyUserEnable()`、`DS_PROXY_SELF_ENABLE` 与 aiSwitch 的组合关系；aiSwitch=true 时 `_ai` 用户无授权记录（isExists=false）是否也放行？
4. governanceStationAdmin 的 aiSwitch 字段由谁提供：Linkis 侧已部署（版本口径）还是本版本 Linkis 侧并行新增（DSS 只按契约消费）？

**澄清结论（2026-11-12，需求方确认）**：

1. **免校验范围**：仅允许 **当前用户+_ai** 与 **当前代理用户+_ai**（修正：非任意 _ai 后缀目标）。
2. **降级行为**：governanceStationAdmin 调用失败/超时/字段缺失时**回退原 isExists 授权校验**，接口异常不影响现有行为。
3. **调用链路**：aiSwitch 查询放在 **DSS 后端**（setProxyUser 处理链路内），前端不改动。
4. **aiSwitch 口径**：Linkis 侧已实现——release_v2.1.7 分支 REQ-01（incubator-linkis commit 81f5fd117）：governanceStationAdmin 在 data 中追加 aiSwitch（JSON 原生布尔），取自 linkis.ai.user.enable（默认 false，热加载）；URL 在 linkis-mg 网关白名单内（免登录）；既有字段 admin/historyAdmin/deptAdmin/canResultSet/errorMsgTip 不变。
5. **硬约束**：**hadoop 用户不允许切换 _ai 用户**。

## 🔗 组件依赖分析摘要（Stage 1.6，2026-11-12T14:40:00+08:00）

组件依赖分析完成（主会话内联执行 component-dependency-analyzer-java 契约）：DSS 已存在 governanceStationAdmin 生产调用先例（HttpResourceManageClient.checkSuperAdmin，ResourceManageClient.java:36 CONF_GOVERNANCE_ADMIN_PATH=LinkisConnConf.LINKIS_URL+/api/rest_j/v1/jobhistory/governanceStationAdmin），本需求不引入新调用范式——复用 URL/HttpClientUtil（自带统一超时）/Token 认证，aiSwitch 仅需在 GovernanceStationAdminResponse.Data 追加 Boolean 字段（Gson 缺失字段自然 null→降级零成本）。关键设计输入：先例非200抛DSSRuntimeException，豁免链路须降级不抛需独立私有方法；Token 用 LINKIS_RESOURCE_ADMIN_TOKEN_* 配置（rest_j 路径需认证，非白名单免登录）。产物：service-dependency-analysis.md / service-dependency-graph.mermaid / http-calls.json（2条：HC-001新增+HC-002既有）/ rpc,mq,sdk空清单。

**设计输入五项**：①复用 CONF_GOVERNANCE_ADMIN_PATH（无新配置项）；②HttpClientUtil 自带统一超时（设计确认现值 vs ≤1s 增量目标）；③Token 复用 LINKIS_RESOURCE_ADMIN_TOKEN_*；④独立私有方法（先例抛异常语义与降级需求冲突）；⑤scriptis-server→linkis-mg 连通性纳入联调。

stageOutputs 的 '2' 条目（写入于 2026-11-12T15:30:00+08:00）：
  status: completed
  summary: 设计文档 v1.0 生成（主会话内联执行 des-enhance-feature，子代理403降级）：复用既有 governanceStationAdmin 调用先例（CONF_GOVERNANCE_ADMIN_PATH+HttpClientUtil+Token），queryAiSwitch 独立私有方法降级不抛，aiSwitch 目标集合精确全等+hadoop两拒最先，零新配置项；触点②两段完成（prepare_obligations 28条义务 + summarize_response 28条响应）；设计质量评估=待核验（2.1 verify_final_design 最终核验）。
  artifacts: [design_document=docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md]
  decisions: [复用既有调用先例零新配置, 独立私有方法因降级语义, Response DTO 追加 Boolean aiSwitch 向后兼容, hadoop 两拒判定次序最先]
| 2 | 设计方案 | 成功 | docs/1.26.0/design/REQ-01_新增AI用户切换（aiSwitch开启时免代理授权校验，直接切换至_ai用户）_设计.md | 2026-11-12T15:30:00+08:00 | 主会话内联执行；28条设计义务全承接（文末导航）；触点②两段完成 |
| 2.1 | 设计检视 | 成功 | - | 2026-11-12T16:40:00+08:00 | 六维度检视应用OPT-D1/D2（设计v1.1）；触点②verify_final_design最终核验verdict=符合标准（5/5风险已覆盖，28/28关注点已覆盖）；修复§2.8-R-004恢复方式字段标签错误、§2.9补R-003/R-004监控条目；Stage2-Hook：图表同步passed（mermaid 1块）、接口文档not_applicable、DPMS同步无MCP降级跳过 |

## 💻 Stage 3 代码开发记录（2026-11-12T22:40:00+08:00）

**changedFiles**：
- dss-framework/dss-framework-proxy-user-service/src/main/java/com/webank/wedatasphere/dss/framework/proxy/dto/AiSwitchResponse.java
- dss-framework/dss-framework-proxy-user-service/src/main/java/com/webank/wedatasphere/dss/framework/proxy/conf/ProxyUserConfiguration.java
- dss-framework/dss-framework-proxy-user-service/src/main/java/com/webank/wedatasphere/dss/framework/proxy/restful/DssProxyUserController.java
- dss-framework/dss-framework-proxy-user-service/pom.xml

**codeChangeSummary**：按设计 v1.1 实施 AI 用户切换豁免链。①AiSwitchResponse 轻量 DTO（gson @SerializedName aiSwitch，缺失字段 null→降级）；②ProxyUserConfiguration 同 key 自声明 LINKIS_GATEWAY_URL/ADMIN_TOKEN_*/超时(3000ms)/_ai后缀/hadoop常量（零新增配置项，key 与 LinkisConnConf 同源）；③DssProxyUserController 追加 isAiSwitchBypass/stripAiSuffix/queryAiSwitch 三私有方法：豁免判定以布尔等价重排置于 isExists 之后（仅无授权记录请求触发外部调用，本人分支与已授权用户零开销），hadoop 两拒最先（equalsIgnoreCase 请求者与派生源双侧），目标精确全等 user+_ai，queryAiSwitch 用 JDK HttpURLConnection（pom 不依赖 dss-common-server-webank，按设计 §2.6 依赖方向原则，URL/Token/超时全复用配置），七情形降级 warn 留痕（URL/异常/耗时）；④pom 显式加 gson（父 dependencyManagement 管版本）。

**编译验证**：mvn compile -pl dss-framework/dss-framework-proxy-user-service -am 通过（修复本地 m2 仓库 linkis-pes-client/pes-common 截断 jar 后重下成功）；产物 AiSwitchResponse.class + DssProxyUserController.class 确认生成。

**设计符合性说明**：设计 §1.1.1 写明"复用 HttpClientUtil"，实施时发现 proxy-user-service 的 pom 不依赖 dss-common-server-webank（HttpClientUtil 所在模块），按设计 §2.6 依赖方向原则（不为读一个布尔引入跨模块编译依赖）以 JDK HttpURLConnection 等价实现，URL/Token/超时语义与设计一致——已在本记录留痕，设计-代码偏差仅此一处且向依赖最小化方向收敛。
| 3 | 代码开发 | 成功 | 4文件（见 changedFiles） | 2026-11-12T22:40:00+08:00 | 主会话内联开发；编译通过；HttpClientUtil→HttpURLConnection 偏差留痕（依赖最小化） |

## 🚀 Stage 4 Git 提交记录（2026-11-12T22:52:00+08:00）

- **commitId**: 982458ee4d2751dffb72b08f810d3bc19b516b9c（本地 dev-1.26.0）
- **变更范围**: 精确 add 4 文件（DTO/Configuration/Controller/pom），216 insertions / 1 deletion
- **push 结果**: 未推送——内网机器无法直连 github，dev-1.26.0 为行外 github 同步分支，按用户指示走"本地 commit → patch/bundle 导出 → 人工搬运行外 github → PR 合入 → 既有机制同步回行内"路径
- **搬运介质**: dev/versions/1.26.0/active/ai-user-switch/sync-patches/
  - 0001-*.patch（format-patch 单提交补丁，行外 `git am` 应用）
  - dev-1.26.0-REQ01.bundle（git bundle 完整保真，行外 `git bundle verify` 后 fetch 应用；基于 e9372468d）
- **push 待办**: 行外 PR 合入且行内同步回 dev-1.26.0 后，回填 pushResult 并作废 push 待补录（此前 biz API :50009 不可用无待补录项，本条为流程内待办）
- **deploymentTriggerSummary**: 行内 push 由同步机制完成后自然触发（wds.dss 流水线），本地无 CI/CD 直推动作
| 5 | 部署确认 | 成功 | - | 2026-11-13T16:40:00+08:00 | PACE流水线1039300005095610执行成功（trigger 1039600010534955，16:17-16:33）：组1编译dev-1.26.0 build#367✅、组2编译Release1.26.0 build#342✅、组3部署DSS-GZ-BDAP-DEV(10.107.97.166)✅；deploymentStatus=confirmed |
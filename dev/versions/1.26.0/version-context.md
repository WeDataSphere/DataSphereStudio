---
contextSchemaVersion: '1.0'
contextRevision: 12
versionName: 1.26.0
versionStatus: in_progress
productId: '100199'
productName: null
subsystemId: '5425'
subsystemName: DSS-IDE
subsystemVersionId: 1.26.0
testSetId: null
regressionTestSetId: null
isNeedGrayRelease: 1
grayReleaseDate: '2026-11-26'
branchName: dev-1.26.0
pipelineId: null
pipelineVersion: DSS-IDE_1.26.0
requirementContextIndex:
  REQ-04:
    taskName: "permission-selfcheck-sql"
    contextPath: "dev/versions/1.26.0/active/permission-selfcheck-sql/context.md"
    currentStage: "6"
    lastCompletedStage: "5.5"
    contextRevision: 2
    status: "development"
    segmentProgress:
      P1: {status: "running"}
      S1: {status: "pending"}
      P2: {status: "pending"}
      S2: {status: "pending"}
      D: {status: "pending"}
      V: {status: "pending"}
  REQ-01:
    taskName: ai-user-switch
    contextPath: dev/versions/1.26.0/active/ai-user-switch/context.md
    currentStage: '1.1'
    lastCompletedStage: '1'
    contextRevision: 11
    status: running
    checkpointId: 1.26.0:REQ-01:1:2026-11-12T11:30:00+08:00
    segmentProgress:
      P1:
        status: running
      S1:
        status: pending
      P2:
        status: pending
      S2:
        status: pending
      D:
        status: pending
      V:
        status: pending
openRisks: []
stage10Result: {}
updatedAt: "2026-11-12T23:00:00+08:00"
stabilityRisksIndex:
  REQ-01:
  - risk_type: 依赖
    affected_scope: dss-framework-proxy-user-service setProxyUser 链路 → linkis-mg 网关
      → linkis-jobhistory governanceStationAdmin
    evidence: 需求事实：setProxyUser 处理链路内 DSS 后端新增对 Linkis governanceStationAdmin 的同步
      HTTP 只读调用，解析 data.aiSwitch 作为豁免判定唯一输入。影响依据：结构化知识确认 DSS 系统对 Linkis 系统存在出向依赖关系；本次为该依赖上新增的运行时调用点。风险判断：governanceStationAdmin
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
      或目标派生自 hadoop 的场景绕过），将造成越权切换；当前需求已以特判次序、精确全等匹配与边界验收用例收窄。缺失信息：当前代理用户的 cookie
      读取口径需设计阶段闭合，口径不一致可能引入第二类派生源。处理建议：设计阶段明确当前代理用户读取口径并在代码评审中逐条核对豁免集合判定与特判次序；他人派生目标与
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
---

# 版本上下文

本文件由 dev-flow version 主会话维护。禁止 Agent/Skill 直接修改控制字段。

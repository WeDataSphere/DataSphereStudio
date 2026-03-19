# language: zh-CN
功能: 工作流Spark版本白名单与发布信息查询
  作为 DSS系统管理员或开发者
  我想要 恢复工作流节点Spark版本的白名单控制机制，并提供发布信息查询和白名单管理接口
  以便 精细化控制Spark版本配置权限，支持Spark3升级项目管控

  背景:
    假定 DSS工作流服务已启动
    而且 数据库连接正常
    而且 白名单表 dss_project_orchestrator_white 存在

  @smoke @critical @E1
  场景: 白名单项目查询节点类型列表返回完整配置
    给定 项目ID为 "1001"
    而且 编排ID为 "2001"
    而且 项目 "1001" 和编排 "2001" 在白名单中
    当 我请求节点类型列表接口 "GET /dss/workflow/listNodeType"
    那么 响应状态码应为 200
    而且 响应中 isWhite 字段值为 true
    而且 节点配置列表中包含 sparkVersion 参数

  @smoke @critical @E1
  场景: 非白名单项目查询节点类型列表过滤sparkVersion
    给定 项目ID为 "1002"
    而且 编排ID为 "2002"
    而且 项目 "1002" 和编排 "2002" 不在白名单中
    当 我请求节点类型列表接口 "GET /dss/workflow/listNodeType"
    那么 响应状态码应为 200
    而且 响应中 isWhite 字段值为 false
    而且 节点配置列表中不包含 sparkVersion 参数

  @E1 @boundary
  场景: 生产环境自动视为白名单项目
    给定 项目ID为 "1003"
    而且 编排ID为 "2003"
    而且 项目 "1003" 和编排 "2003" 不在白名单中
    而且 环境标签为 "prod"
    当 我请求节点类型列表接口 "GET /dss/workflow/listNodeType"
    那么 响应状态码应为 200
    而且 响应中 isWhite 字段值为 true
    而且 节点配置列表中包含 sparkVersion 参数

  @E1 @negative
  场景: projectId为null时按非白名单处理
    给定 项目ID为 null
    而且 编排ID为 "2004"
    当 我请求节点类型列表接口 "GET /dss/workflow/listNodeType"
    那么 响应状态码应为 200
    而且 响应中 isWhite 字段值为 false
    而且 节点配置列表中不包含 sparkVersion 参数

  @E1 @negative
  场景: orchestratorId为null时按非白名单处理
    给定 项目ID为 "1004"
    而且 编排ID为 null
    当 我请求节点类型列表接口 "GET /dss/workflow/listNodeType"
    那么 响应状态码应为 200
    而且 响应中 isWhite 字段值为 false
    而且 节点配置列表中不包含 sparkVersion 参数

  @E1 @POST
  场景: POST请求时白名单项目sparkVersion默认值为3
    给定 项目ID为 "1001"
    而且 编排ID为 "2001"
    而且 项目 "1001" 和编排 "2001" 在白名单中
    而且 请求方法为 POST
    当 我请求节点类型列表接口 "POST /dss/workflow/listNodeType"
    那么 响应状态码应为 200
    而且 响应中 isWhite 字段值为 true
    而且 节点配置中 sparkVersion 默认值为 "3"

  @smoke @critical @E2
  场景: AISQL节点不显示sparkVersion配置
    给定 项目ID为 "1001"
    而且 编排ID为 "2001"
    而且 项目 "1001" 和编排 "2001" 在白名单中
    而且 节点类型为 "linkis.ai.sql"
    当 我请求节点类型列表接口 "GET /dss/workflow/listNodeType"
    那么 响应状态码应为 200
    而且 AISQL节点配置中不包含 sparkVersion 参数

  @E2 @boundary
  场景: AISQL节点在非白名单项目中也不显示sparkVersion
    给定 项目ID为 "1002"
    而且 编排ID为 "2002"
    而且 项目 "1002" 和编排 "2002" 不在白名单中
    而且 节点类型为 "linkis.ai.sql"
    当 我请求节点类型列表接口 "GET /dss/workflow/listNodeType"
    那么 响应状态码应为 200
    而且 响应中 isWhite 字段值为 false
    而且 AISQL节点配置中不包含 sparkVersion 参数

  @E2 @validation
  场景: 非AISQL节点在白名单项目中正常显示sparkVersion
    给定 项目ID为 "1001"
    而且 编排ID为 "2001"
    而且 项目 "1001" 和编排 "2001" 在白名单中
    而且 节点类型为 "linkis.spark.sql"
    当 我请求节点类型列表接口 "GET /dss/workflow/listNodeType"
    那么 响应状态码应为 200
    而且 响应中 isWhite 字段值为 true
    而且 SparkSQL节点配置中包含 sparkVersion 参数

  @E2 @validation
  场景: 非AISQL节点在非白名单项目中不显示sparkVersion
    给定 项目ID为 "1002"
    而且 编排ID为 "2002"
    而且 项目 "1002" 和编排 "2002" 不在白名单中
    而且 节点类型为 "linkis.spark.sql"
    当 我请求节点类型列表接口 "GET /dss/workflow/listNodeType"
    那么 响应状态码应为 200
    而且 响应中 isWhite 字段值为 false
    而且 SparkSQL节点配置中不包含 sparkVersion 参数

  @smoke @E3
  场景: 发布信息查询接口正常查询
    给定 项目名称为 "test-project"
    而且 编排名称列表为 "[workflow1, workflow2]"
    而且 工作流 "workflow1" 存在发布成功记录
    而且 工作流 "workflow2" 存在发布成功记录
    当 我请求发布信息查询接口 "POST /dss/framework/orchestrator/getReleaseInfo"
    那么 响应状态码应为 200
    而且 响应中包含 orchestratorId 字段
    而且 响应中包含 orchestratorName 字段
    而且 响应中包含 status 字段
    而且 响应中包含 releaseUser 字段
    而且 响应中包含 releaseTime 字段
    而且 响应中包含 projectId 字段
    而且 响应中包含 projectName 字段

  @E3 @validation
  场景: 发布信息查询只返回最新发布成功记录
    给定 项目名称为 "test-project"
    而且 编排名称列表为 "[workflow1]"
    而且 工作流 "workflow1" 存在多条发布记录
    而且 最新发布成功记录的更新时间为 "2026-03-17 10:00:00"
    当 我请求发布信息查询接口 "POST /dss/framework/orchestrator/getReleaseInfo"
    那么 响应状态码应为 200
    而且 返回的发布信息只有一条记录
    而且 返回记录的 status 为 "Success"
    而且 返回记录为最新的发布成功记录

  @E3 @negative
  场景: 发布信息查询projectName为空返回参数错误
    给定 项目名称为 ""
    而且 编排名称列表为 "[workflow1]"
    当 我请求发布信息查询接口 "POST /dss/framework/orchestrator/getReleaseInfo"
    那么 响应状态码应为 400
    而且 响应错误码为 60015
    而且 响应消息包含 "参数错误"

  @E3 @negative
  场景: 发布信息查询orchestratorNames为空列表返回参数错误
    给定 项目名称为 "test-project"
    而且 编排名称列表为 "[]"
    当 我请求发布信息查询接口 "POST /dss/framework/orchestrator/getReleaseInfo"
    那么 响应状态码应为 400
    而且 响应错误码为 60015
    而且 响应消息包含 "参数错误"

  @E3 @boundary
  场景: 发布信息查询未发布的工作流返回空列表
    给定 项目名称为 "test-project"
    而且 编排名称列表为 "[workflow-new]"
    而且 工作流 "workflow-new" 不存在发布记录
    当 我请求发布信息查询接口 "POST /dss/framework/orchestrator/getReleaseInfo"
    那么 响应状态码应为 200
    而且 响应中发布信息列表为空

  @E3 @boundary
  场景: 发布信息查询批量查询不超过100条
    给定 项目名称为 "test-project"
    而且 编排名称列表包含 100 个编排名称
    当 我请求发布信息查询接口 "POST /dss/framework/orchestrator/getReleaseInfo"
    那么 响应状态码应为 200
    而且 响应时间不超过 1 秒

  @E3 @performance
  场景: 发布信息查询响应时间满足性能要求
    给定 项目名称为 "test-project"
    而且 编排名称列表包含 100 个编排名称
    而且 每个编排都有发布成功记录
    当 我请求发布信息查询接口 "POST /dss/framework/orchestrator/getReleaseInfo"
    那么 响应状态码应为 200
    而且 响应时间不超过 1000 毫秒

  @smoke @E4 @ITSM
  场景: ITSM接口添加白名单成功
    给定 ITSM接口签名验证通过
    而且 项目名称为 "test-project"
    而且 项目 "test-project" 存在
    而且 编排名称为 "workflow1"
    而且 工作流 "workflow1" 存在
    而且 创建用户为 "admin"
    而且 创建原因为 "Spark3升级测试"
    当 我请求ITSM白名单接口 "POST /dss/framework/orchestrator/addOrchestratorWhite"
    那么 响应状态码应为 200
    而且 响应 retCode 为 0
    而且 数据库中存在白名单记录
    而且 白名单记录的 type 字段为 "schedulis"
    而且 白名单记录的 reason 字段为 "Spark3升级测试"

  @E4 @ITSM @boundary
  场景: ITSM接口添加项目级白名单
    给定 ITSM接口签名验证通过
    而且 项目名称为 "test-project"
    而且 项目 "test-project" 存在
    而且 编排名称为 "*" 或为空
    而且 创建用户为 "admin"
    当 我请求ITSM白名单接口 "POST /dss/framework/orchestrator/addOrchestratorWhite"
    那么 响应状态码应为 200
    而且 响应 retCode 为 0
    而且 数据库中存在白名单记录
    而且 白名单记录的 orchestrator_id 为 0

  @E4 @ITSM @negative
  场景: ITSM接口签名验证失败返回403
    给定 ITSM接口签名验证失败
    而且 项目名称为 "test-project"
    当 我请求ITSM白名单接口 "POST /dss/framework/orchestrator/addOrchestratorWhite"
    那么 响应状态码应为 403
    而且 响应消息包含 "鉴权失败"

  @E4 @ITSM @negative
  场景: ITSM接口项目不存在返回错误
    给定 ITSM接口签名验证通过
    而且 项目名称为 "non-existent-project"
    而且 项目 "non-existent-project" 不存在
    当 我请求ITSM白名单接口 "POST /dss/framework/orchestrator/addOrchestratorWhite"
    那么 响应状态码应为 200
    而且 响应 retCode 非 0
    而且 响应消息包含 "项目不存在"

  @E4 @ITSM @negative
  场景: ITSM接口工作流不存在返回错误
    给定 ITSM接口签名验证通过
    而且 项目名称为 "test-project"
    而且 项目 "test-project" 存在
    而且 编排名称为 "non-existent-workflow"
    而且 工作流 "non-existent-workflow" 不存在
    当 我请求ITSM白名单接口 "POST /dss/framework/orchestrator/addOrchestratorWhite"
    那么 响应状态码应为 200
    而且 响应 retCode 非 0
    而且 响应消息包含 "工作流不存在"

  @smoke @E4 @Simple
  场景: 普通接口添加白名单成功
    给定 用户已登录，用户名为 "developer"
    而且 项目名称为 "test-project"
    而且 项目 "test-project" 存在
    而且 编排名称为 "workflow1"
    而且 工作流 "workflow1" 存在
    而且 添加原因为 "业务需求"
    当 我请求普通白名单接口 "POST /dss/framework/orchestrator/addOrchestratorWhiteSimple"
    那么 响应状态码应为 200
    而且 响应消息为成功
    而且 数据库中存在白名单记录
    而且 白名单记录的 create_user 为 "developer"
    而且 白名单记录的 type 字段为 "schedulis"
    而且 白名单记录的 reason 字段为 "业务需求"

  @E4 @Simple @boundary
  场景: 普通接口添加项目级白名单
    给定 用户已登录，用户名为 "developer"
    而且 项目名称为 "test-project"
    而且 项目 "test-project" 存在
    而且 编排名称为空
    当 我请求普通白名单接口 "POST /dss/framework/orchestrator/addOrchestratorWhiteSimple"
    那么 响应状态码应为 200
    而且 响应消息为成功
    而且 数据库中存在白名单记录
    而且 白名单记录的 orchestrator_id 为 0

  @E4 @Simple @negative
  场景: 普通接口projectName为空返回错误
    给定 用户已登录，用户名为 "developer"
    而且 项目名称为 ""
    当 我请求普通白名单接口 "POST /dss/framework/orchestrator/addOrchestratorWhiteSimple"
    那么 响应状态码应为 400
    而且 响应错误码为 60015

  @E4 @Simple @negative
  场景: 普通接口项目不存在返回错误
    给定 用户已登录，用户名为 "developer"
    而且 项目名称为 "non-existent-project"
    而且 项目 "non-existent-project" 不存在
    当 我请求普通白名单接口 "POST /dss/framework/orchestrator/addOrchestratorWhiteSimple"
    那么 响应状态码应为 200
    而且 响应消息包含 "项目不存在"

  @E4 @idempotent
  场景: 重复添加白名单不报错并更新记录
    给定 ITSM接口签名验证通过
    而且 项目名称为 "test-project"
    而且 项目 "test-project" 存在
    而且 编排名称为 "workflow1"
    而且 工作流 "workflow1" 存在
    而且 白名单记录已存在
    而且 更新的原因为 "更新原因"
    当 我请求ITSM白名单接口 "POST /dss/framework/orchestrator/addOrchestratorWhite"
    那么 响应状态码应为 200
    而且 响应 retCode 为 0
    而且 数据库中白名单记录被更新
    而且 白名单记录的 reason 字段为 "更新原因"

  @E1 @regression
  场景: 批量编辑非白名单工作流禁止修改sparkVersion
    给定 项目ID为 "1002"
    而且 编排ID为 "2002"
    而且 项目 "1002" 和编排 "2002" 不在白名单中
    而且 批量编辑节点参数
    而且 节点参数包含 sparkVersion
    当 执行批量编辑操作
    那么 sparkVersion 参数被过滤
    而且 其他参数正常更新

  @E1 @regression
  场景: 批量编辑白名单工作流允许修改sparkVersion
    给定 项目ID为 "1001"
    而且 编排ID为 "2001"
    而且 项目 "1001" 和编排 "2001" 在白名单中
    而且 批量编辑节点参数
    而且 节点参数包含 sparkVersion 值为 "3"
    当 执行批量编辑操作
    那么 sparkVersion 参数被更新为 "3"
    而且 其他参数正常更新

  @E1 @exception
  场景: 白名单查询失败时按非白名单处理
    给定 项目ID为 "1005"
    而且 编排ID为 "2005"
    而且 白名单查询服务异常
    当 我请求节点类型列表接口 "GET /dss/workflow/listNodeType"
    那么 响应状态码应为 200
    而且 响应中 isWhite 字段值为 false
    而且 节点配置列表中不包含 sparkVersion 参数
    而且 错误日志被记录

  @integration @E1_E2
  场景: 白名单项目中AISQL与其他节点混合处理
    给定 项目ID为 "1001"
    而且 编排ID为 "2001"
    而且 项目 "1001" 和编排 "2001" 在白名单中
    而且 工作流包含多个节点类型
    当 我请求节点类型列表接口 "GET /dss/workflow/listNodeType"
    那么 响应状态码应为 200
    而且 响应中 isWhite 字段值为 true
    而且 AISQL节点配置中不包含 sparkVersion 参数
    而且 SparkSQL节点配置中包含 sparkVersion 参数
    而且 Python节点配置中包含 sparkVersion 参数

  @integration @E1_E2
  场景: 非白名单项目中AISQL与其他节点混合处理
    给定 项目ID为 "1002"
    而且 编排ID为 "2002"
    而且 项目 "1002" 和编排 "2002" 不在白名单中
    而且 工作流包含多个节点类型
    当 我请求节点类型列表接口 "GET /dss/workflow/listNodeType"
    那么 响应状态码应为 200
    而且 响应中 isWhite 字段值为 false
    而且 AISQL节点配置中不包含 sparkVersion 参数
    而且 SparkSQL节点配置中不包含 sparkVersion 参数
    而且 Python节点配置中不包含 sparkVersion 参数
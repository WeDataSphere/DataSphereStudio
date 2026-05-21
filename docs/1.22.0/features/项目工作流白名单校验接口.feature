# language: zh-CN
功能: 项目工作流白名单校验接口

  背景:
    Given DSS系统服务正常运行
    And 白名单表 dss_project_orchestrator_white 已存在

  @checkIsWhite @P0
  场景: 校验项目级白名单-项目在白名单中
    Given 白名单表中存在项目级白名单记录
    And project_id 为已添加白名单的项目ID
    When 调用 /dss/framework/project/checkIsWhite 接口，参数 projectId=1, orchestratorId=0
    Then 返回状态码 200
    And 返回 isWhite: true

  @checkIsWhite @P0
  场景: 校验项目级白名单-项目不在白名单中
    Given 白名单表中不存在该项目的白名单记录
    And project_id 为未添加白名单的项目ID
    When 调用 /dss/framework/project/checkIsWhite 接口，参数 projectId=999, orchestratorId=0
    Then 返回状态码 200
    And 返回 isWhite: false

  @checkIsWhite @P0
  场景: 校验工作流级白名单-工作流在白名单中
    Given 白名单表中存在工作流级白名单记录
    And project_id 和 orchestrator_id 对应的白名单记录存在
    When 调用 /dss/framework/project/checkIsWhite 接口，参数 projectId=1, orchestratorId=100
    Then 返回状态码 200
    And 返回 isWhite: true

  @checkIsWhite @P0
  场景: 项目级白名单覆盖所有工作流
    Given 白名单表中存在项目级白名单记录（orchestrator_id=0）
    And 该项目下任意工作流均可通过白名单校验
    When 调用 /dss/framework/project/checkIsWhite 接口，参数 projectId=1, orchestratorId=任意工作流ID
    Then 返回状态码 200
    And 返回 isWhite: true

  @checkIsWhite @P1
  场景: 校验工作流级白名单-工作流不在白名单中
    Given 白名单表中不存在该工作流的白名单记录
    And 项目也没有项目级白名单记录
    When 调用 /dss/framework/project/checkIsWhite 接口，参数 projectId=1, orchestratorId=200
    Then 返回状态码 200
    And 返回 isWhite: false

  @checkIsWhite @P1
  场景: projectId为空
    Given 用户调用接口但未提供projectId参数
    When 调用 /dss/framework/project/checkIsWhite 接口，参数 projectId=null
    Then 返回状态码 200
    And 返回 isWhite: false

  @checkIsWhite @P1
  场景: orchestratorId为空默认校验项目级白名单
    Given 用户调用接口只提供projectId
    When 调用 /dss/framework/project/checkIsWhite 接口，参数 projectId=1, orchestratorId不传
    Then 返回状态码 200
    And 按 orchestratorId=0 处理
    And 返回项目级白名单校验结果

  @checkIsWhite @P1
  场景: 未登录用户调用接口
    Given 用户未登录DSS系统
    When 调用 /dss/framework/project/checkIsWhite 接口
    Then 请求被SSO拦截
    And 返回认证失败

  @前端集成 @P0
  场景: 项目列表页面-白名单项目显示复制按钮
    Given 用户已登录DSS系统
    And 用户进入项目列表页面（#/workspaceHome）
    And 项目A在白名单中
    And 项目B不在白名单中
    When 页面加载时调用白名单校验接口
    Then 项目A的"复制项目"按钮可见
    And 项目B的"复制项目"按钮隐藏

  @前端集成 @P0
  场景: 工作流页面-白名单工作流显示复制按钮
    Given 用户已登录DSS系统
    And 用户进入工作流页面（#/workflow）
    And 工作流A在白名单中
    And 工作流B不在白名单中
    When 页面加载时调用白名单校验接口
    Then 工作流A的"复制工作流"按钮可见
    And 工作流B的"复制工作流"按钮隐藏

  @前端集成 @P1
  场景: 点击白名单项目的复制按钮成功弹窗
    Given 项目在白名单中
    And "复制项目"按钮可见
    When 用户点击"复制项目"按钮
    Then 弹出项目复制对话框
    And 用户可正常执行项目复制操作

  @前端集成 @P1
  场景: 项目复制后新项目白名单继承
    Given 源项目在白名单中
    And 用户成功复制项目
    Then 复制后的新项目自动添加到白名单中
    And 新项目的"复制项目"按钮可见

  @回归 @P0
  场景: 白名单校验不影响现有接口
    Given 白名单校验接口已上线
    When 调用 /dss/workflow/listNodeType 接口
    And 调用 /dss/framework/project/copyProject 接口
    And 调用 /dss/framework/orchestrator/copyOrchestrator 接口
    Then 所有接口功能与上线前一致
    And isWhite字段返回值正确
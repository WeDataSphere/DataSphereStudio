# language: zh-CN
功能: ITSM接口字段适配

  背景:
    Given DSS系统服务正常运行
    And ITSM鉴权配置正确

  @updateWorkspace @P0
  场景: updateWorkspace接口-正常请求无未知字段
    Given ITSM发送标准格式请求
    And 请求不包含未知字段
    When 调用 /dss/framework/workspace/updateWorkspace 接口
    Then 返回状态码 200
    And 返回 success: true
    And 工作空间创建成功

  @updateWorkspace @P0
  场景: updateWorkspace接口-包含未知字段静默忽略
    Given ITSM发送包含新字段的请求
    And 请求包含未知字段 "newField1" 和 "newField2"
    When 调用 /dss/framework/workspace/updateWorkspace 接口
    Then 返回状态码 200
    And 返回 success: true
    And 工作空间创建成功
    And 未知字段被静默忽略不影响业务

  @updateWorkspace @P1
  场景: updateWorkspace接口-数据为空
    Given ITSM发送请求
    And dataList为空
    When 调用 /dss/framework/workspace/updateWorkspace 接口
    Then 返回状态码 200
    And 返回错误信息 "data is empty"

  @updateWorkspace @P1
  场景: updateWorkspace接口-鉴权失败
    Given ITSM发送请求
    And 请求头包含错误的timestamp和sign
    When 调用 /dss/framework/workspace/updateWorkspace 接口
    Then 返回状态码 403
    And 返回错误信息 "Authentication failed."

  @addOrchestratorWhite @P0
  场景: addOrchestratorWhite接口-正常请求无未知字段
    Given ITSM发送标准格式请求
    And 项目和工作流已存在
    And 请求不包含未知字段
    When 调用 /dss/framework/orchestrator/addOrchestratorWhite 接口
    Then 返回状态码 200
    And 返回 success: true
    And 白名单添加成功

  @addOrchestratorWhite @P0
  场景: addOrchestratorWhite接口-包含未知字段静默忽略
    Given ITSM发送包含新字段的请求
    And 请求包含未知字段 "newField" 和 "extraData"
    And 项目和工作流已存在
    When 调用 /dss/framework/orchestrator/addOrchestratorWhite 接口
    Then 返回状态码 200
    And 返回 success: true
    And 白名单添加成功
    And 未知字段被静默忽略不影响业务

  @addOrchestratorWhite @P1
  场景: addOrchestratorWhite接口-项目不存在
    Given ITSM发送请求
    And 项目名称不存在
    And 请求包含未知字段
    When 调用 /dss/framework/orchestrator/addOrchestratorWhite 接口
    Then 返回状态码 200
    And 返回错误信息

  @addUserProxy @P0
  场景: addUserProxy接口-正常请求无未知字段
    Given ITSM发送标准格式请求
    And 请求不包含未知字段
    When 调用 /dss/scriptis/proxy/addUserProxy 接口
    Then 返回状态码 200
    And 返回 success: true
    And 代理用户添加成功

  @addUserProxy @P0
  场景: addUserProxy接口-包含未知字段静默忽略
    Given ITSM发送包含新字段的请求
    And 请求包含未知字段 "newField1"、"newField2" 和 "extraInfo"
    When 调用 /dss/scriptis/proxy/addUserProxy 接口
    Then 返回状态码 200
    And 返回 success: true
    And 代理用户添加成功
    And 未知字段被静默忽略不影响业务

  @addUserProxy @P1
  场景: addUserProxy接口-数据为空
    Given ITSM发送请求
    And dataList为空
    And 请求包含未知字段
    When 调用 /dss/scriptis/proxy/addUserProxy 接口
    Then 返回状态码 200
    And 返回错误信息 "data is empty"

  @addUserProxy @P1
  场景: addUserProxy接口-鉴权失败
    Given ITSM发送请求
    And 请求头包含错误的timestamp和sign
    When 调用 /dss/scriptis/proxy/addUserProxy 接口
    Then 返回状态码 403
    And 返回错误信息 "Authentication failed."

  @addUserProxy @P1
  场景: addUserProxy接口-多个未知字段
    Given ITSM发送请求
    And 请求包含5个未知字段 field1、field2、field3、field4、field5
    And field4为嵌套对象
    And field5为数组
    When 调用 /dss/scriptis/proxy/addUserProxy 接口
    Then 返回状态码 200
    And 返回 success: true
    And 代理用户添加成功
    And 所有未知字段被静默忽略

  @BackwardCompatibility @P0
  场景: 向后兼容性-旧版请求格式
    Given ITSM使用升级前的标准请求格式
    When 调用 updateWorkspace 接口
    And 调用 addOrchestratorWhite 接口
    And 调用 addUserProxy 接口
    Then 所有接口返回正常
    And 功能与升级前一致

  @BackwardCompatibility @P1
  场景: 向后兼容性-新旧请求混合场景
    Given ITSM交替发送旧版请求和新版请求
    And 新版请求包含未知字段
    When 调用各接口
    Then 所有请求正常处理
    And 功能表现一致

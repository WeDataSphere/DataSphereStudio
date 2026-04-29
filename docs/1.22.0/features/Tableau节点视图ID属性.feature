# language: zh-CN
功能: Tableau相关节点ID属性管理

  背景:
   Given 用户已登录DSS系统
    And 用户已进入"数据可视化"管理页面
    And 用户的项目权限为编辑权限

  @ViewId @P0
  场景: 查看tableau节点的视图ID
    Given 系统中存在tableau类型节点，已绑定视图ID "view-12345"
    And 系统中存在tableauDataRefre类型节点，已绑定数据源ID "datasource-67890"
    When 用户查询数据可视化节点列表
    Then tableau节点的"绑定Tableau视图ID或数据源ID"列显示 "view-12345"
    And tableauDataRefre节点的"绑定Tableau视图ID或数据源ID"列显示 "datasource-67890"
    And ID显示为蓝色可点击链接
    And 点击ID链接能跳转到Tableau服务

  @ViewId @P0
  场景: 查看未绑定ID的tableau相关节点
    Given 系统中存在tableau类型节点，未绑定视图ID
    And 系统中存在tableauDataRefre类型节点，未绑定数据源ID
    When 用户查询数据可视化节点列表
    Then tableau节点的"绑定Tableau视图ID或数据源ID"列显示 "--"
    And tableauDataRefre节点的"绑定Tableau视图ID或数据源ID"列显示 "--"

  @EditViewId @P0
  场景: 编辑tableau节点的视图ID-成功
    Given 系统中存在tableau类型节点，当前视图ID为 "view-old-001"
    When 用户点击该节点的"编辑"按钮
    And 编辑弹窗中视图ID输入框显示当前值 "view-old-001"
    And 用户将视图ID修改为 "view-new-002"
    And 用户点击"确认"按钮
    Then 系统显示"编辑成功"提示信息
    And 编辑弹窗自动关闭
    And 节点列表自动刷新
    And 该节点的视图ID显示为 "view-new-002"

  @EditDatasourceId @P0
  场景: 编辑tableauDataRefre节点的数据源ID-成功
    Given 系统中存在tableauDataRefre类型节点，当前数据源ID为 "datasource-old-001"
    When 用户点击该节点的"编辑"按钮
    And 编辑弹窗中数据源ID输入框显示当前值 "datasource-old-001"
    And 用户将数据源ID修改为 "datasource-new-002"
    And 用户点击"确认"按钮
    Then 系统显示"编辑成功"提示信息
    And 编辑弹窗自动关闭
    And 节点列表自动刷新
    And 该节点的数据源ID显示为 "datasource-new-002"

  @EditViewId @P0
  场景: 编辑tableau节点的视图ID-取消
    Given 系统中存在tableau类型节点，当前视图ID为 "view-001"
    When 用户点击该节点的"编辑"按钮
    And 用户将视图ID修改为 "view-modified"
    And 用户点击"取消"按钮
    Then 编辑弹窗关闭
    And 节点列表中的视图ID仍为 "view-001"

  @EditDatasourceId @P0
  场景: 编辑tableauDataRefre节点的数据源ID-取消
    Given 系统中存在tableauDataRefre类型节点，当前数据源ID为 "datasource-001"
    When 用户点击该节点的"编辑"按钮
    And 用户将数据源ID修改为 "datasource-modified"
    And 用户点击"取消"按钮
    Then 编辑弹窗关闭
    And 节点列表中的数据源ID仍为 "datasource-001"

  @EditViewId @P0
  场景: 编辑tableau节点视图ID-必填校验
    Given 系统中存在tableau类型节点，当前视图ID为 "view-001"
    When 用户点击该节点的"编辑"按钮
    And 用户清空视图ID输入框
    And 用户点击"确认"按钮
    Then 系统显示"该字段为必填项"错误提示
    And 视图ID输入框显示错误状态
    And 编辑弹窗保持打开状态

  @EditDatasourceId @P0
  场景: 编辑tableauDataRefre节点数据源ID-必填校验
    Given 系统中存在tableauDataRefre类型节点，当前数据源ID为 "datasource-001"
    When 用户点击该节点的"编辑"按钮
    And 用户清空数据源ID输入框
    And 用户点击"确认"按钮
    Then 系统显示"该字段为必填项"错误提示
    And 数据源ID输入框显示错误状态
    And 编辑弹窗保持打开状态

  @Permission @P0
  场景: 无编辑权限用户无法编辑
    Given 用户的项目权限为只读权限
    When 用户查看数据可视化节点列表
    Then 节点的"编辑"按钮显示为禁用状态
    And 用户无法点击编辑按钮

  @Execution @P0
  场景: 执行tableau节点使用更新后的视图ID
    Given tableau节点的视图ID已被修改为 "view-updated-004"
    And 该节点属于工作流 "wf-test-001"
    When 用户执行该工作流
    And 在工作流执行详情中点击该tableau节点跳转Tableau
    Then 跳转的Tableau URL中包含 "viewId=view-updated-004"
    And Tableau服务显示视图ID为 "view-updated-004" 的视图内容

  @Execution @P0
  场景: 执行tableauDataRefre节点使用更新后的数据源ID
    Given tableauDataRefre节点的数据源ID已被修改为 "datasource-updated-004"
    When 用户执行该工作流
    And 验证数据源刷新操作
    Then 数据源刷新使用数据源ID为 "datasource-updated-004"

  @Performance @P1
  场景: 节点列表查询性能
    Given 数据库中存在100个数据可视化节点
    When 用户查询数据可视化节点列表
    Then 查询响应时间小于3秒
    And 节点列表正确显示所有节点信息

  @UX @P1
  场景: 长ID显示省略
    Given tableau节点的视图ID为 "view-very-long-id-that-exceeds-column-width-for-ellipsis-display-purposes"
    And tableauDataRefre节点的数据源ID为 "datasource-very-long-id-for-ellipsis"
    When 用户查看数据可视化节点列表
    Then ID列显示省略号截断效果
    And 鼠标悬停在该单元格时显示完整ID

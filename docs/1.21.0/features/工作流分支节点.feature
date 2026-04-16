# language: zh-CN
功能: 工作流分支节点（严格 DSS FlowExecution 语义）
  为了在 DSS 工作流中支持运行时条件分支
  作为 工作流开发人员
  我希望通过分支节点属性 branch.rules 控制下游节点执行，并且语义与 DSS FlowExecution 一致

  背景:
    假如 工作流支持节点类型 workflow.branch
    并且 分支规则配置在节点属性 branch.rules 中
    并且 branch.rules 采用 condition.N / on.success.N / on.failure.N 编号格式
    并且 分支节点发布到调度系统后为 decision 节点

  场景: 分支节点规则可配置并保存
    假如 用户在画布中添加分支节点
    当 用户在 branch.rules 中填写
      """
      condition.1=amount > 100
      on.success.1=节点A
      on.failure.1=节点B
      condition.2=amount > 10
      on.success.2=节点C
      on.failure.2=
      """
    那么 系统保存分支节点属性成功
    并且 分支规则文本保存在节点配置中

  场景: 发布后分支节点映射为 decision 属性
    假如 分支节点 branch.rules 为
      """
      condition.1=b==200
      on.success.1=sql_1345
      on.failure.1=sql_9000
      condition.2=b<50
      on.success.2=sql_2001
      on.failure.2=
      """
    当 工作流发布到调度系统
    那么 分支节点 .job 中应包含 type=decision
    并且 包含 condition.1=b==200
    并且 包含 on.success.1=sql_1345
    并且 包含 on.failure.1=sql_9000
    并且 包含 condition.2=b<50
    并且 包含 on.success.2=sql_2001
    并且 包含 on.failure.2=

  场景: 分支节点按顺序命中条件成立的 success 目标
    假如 分支节点 branch.rules 为
      """
      condition.1=amount > 100
      on.success.1=节点A
      on.failure.1=节点B
      condition.2=amount > 10
      on.success.2=节点C
      on.failure.2=节点D
      """
    并且 运行时变量 amount = 120
    当 工作流执行到分支节点
    那么 系统应选择下游节点 节点A
    并且 不再继续匹配后续规则

  场景: 条件不成立时走 on.failure 目标
    假如 分支节点 branch.rules 为
      """
      condition.1=amount > 100
      on.success.1=节点A
      on.failure.1=节点B
      """
    并且 运行时变量 amount = 50
    当 工作流执行到分支节点
    那么 系统应选择下游节点 节点B

  场景: 当前规则目标为空时继续判断下一条
    假如 分支节点 branch.rules 为
      """
      condition.1=amount > 100
      on.success.1=
      on.failure.1=
      condition.2=amount > 10
      on.success.2=节点B
      on.failure.2=节点C
      """
    并且 运行时变量 amount = 20
    当 工作流执行到分支节点
    那么 系统应继续评估第2条规则
    并且 选择下游节点 节点B

  场景: 无有效下游时失败
    假如 分支节点 branch.rules 为
      """
      condition.1=amount > 100
      on.success.1=
      on.failure.1=
      """
    并且 运行时变量 amount = 50
    当 工作流执行到分支节点
    那么 分支节点执行失败
    并且 错误信息包含 "No branch rule matched"

  场景: 分支节点可读取上游 SQL 输出变量
    假如 上游 SQL 节点输出 amount = 1200
    并且 分支节点 branch.rules 为
      """
      condition.1=amount > 1000
      on.success.1=大额处理
      on.failure.1=普通处理
      """
    当 工作流执行到分支节点
    那么 分支节点能够读取 amount = 1200
    并且 选择下游节点 大额处理

  场景: 分支节点可读取上游映射后的变量
    假如 上游节点输出 total_amount = 1200
    并且 上游节点配置 branch.output.mapping 为 "amount=total_amount"
    并且 分支节点 branch.rules 为
      """
      condition.1=amount > 1000
      on.success.1=大额处理
      on.failure.1=普通处理
      """
    当 工作流执行到分支节点
    那么 分支节点能够读取变量 amount = 1200
    并且 选择下游节点 大额处理

  场景: 未命中兄弟下游节点被跳过
    假如 分支节点有两个直接下游 节点A 和 节点B
    并且 分支节点 branch.rules 为
      """
      condition.1=amount > 100
      on.success.1=节点A
      on.failure.1=节点B
      """
    并且 运行时变量 amount = 150
    当 工作流执行到分支节点
    那么 节点A 状态应为 scheduled
    并且 节点B 状态应为 skipped

  场景: 混合上游场景按 DSS 语义优先跳过（编号10）
    假如 节点X有两个上游 普通节点A 和 分支节点B
    并且 普通节点A执行成功
    并且 分支节点B未命中节点X
    当 调度系统判定节点X是否可执行
    那么 节点X状态应为 skipped
    并且 不能因为普通节点A成功而执行节点X

  场景: 保存时校验规则目标节点名
    假如 分支节点直接下游只有 节点A 和 节点B
    当 用户在 branch.rules 中填写
      """
      condition.1=amount > 100
      on.success.1=节点X
      on.failure.1=节点B
      """
    那么 系统保存失败
    并且 错误信息提示规则引用了非直接下游节点

  场景: 默认关键字不再支持
    当 用户在 branch.rules 中填写
      """
      condition.1=default
      on.success.1=节点A
      on.failure.1=节点B
      """
    那么 系统应将该规则视为无效规则

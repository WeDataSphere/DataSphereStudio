# language: zh-CN
功能: 工作流分支节点
  为了在 DSS 工作流中支持运行时条件分支
  作为工作流开发人员
  我希望通过分支节点属性 branch.rules 控制唯一一个下游节点执行

  背景:
    假如 工作流支持节点类型 workflow.branch
    并且 分支规则配置在节点属性 branch.rules 中
    并且 分支节点至少连接两个直接下游节点

  场景: 用户通过节点属性配置分支规则
    假如 用户已经在画布中添加分支节点
    当 用户在 branch.rules 中填写
      """
      amount>100=节点A
      default=节点B
      """
    那么 系统保存分支节点属性成功
    并且 分支规则文本保存在节点配置中

  场景: 分支节点按顺序命中第一条规则
    假如 工作流包含分支节点
    并且 分支节点 branch.rules 为
      """
      amount>100=节点A
      amount>10=节点B
      default=节点C
      """
    并且 运行时变量 amount = 120
    当 工作流执行到分支节点
    那么 系统应选择下游节点 节点A
    并且 不再继续匹配后续普通规则

  场景: 所有普通规则不命中时执行默认规则
    假如 分支节点 branch.rules 为
      """
      amount>100=节点A
      amount<10=节点B
      default=节点C
      """
    并且 运行时变量 amount = 50
    当 工作流执行到分支节点
    那么 系统应选择下游节点 节点C

  场景: 无命中且无默认规则时报错
    假如 分支节点 branch.rules 为
      """
      amount>100=节点A
      amount<10=节点B
      """
    并且 运行时变量 amount = 50
    当 工作流执行到分支节点
    那么 分支节点执行失败
    并且 错误信息包含 "No branch rule matched"

  场景: 分支节点读取上游 SQL 输出变量
    假如 上游 SQL 节点输出列 amount = 1200
    并且 分支节点 branch.rules 为
      """
      amount>1000=大额处理
      default=普通处理
      """
    当 工作流执行到分支节点
    那么 分支节点能够读取 amount = 1200
    并且 选择下游节点 大额处理

  场景: 分支节点读取上游映射后的变量
    假如 上游节点输出列 total_amount = 1200
    并且 上游节点配置 branch.output.mapping 为 "amount=total_amount"
    并且 分支节点 branch.rules 为
      """
      amount>1000=大额处理
      default=普通处理
      """
    当 工作流执行到分支节点
    那么 分支节点能够读取变量 amount = 1200
    并且 选择下游节点 大额处理

  场景: 未命中的兄弟下游节点被跳过
    假如 分支节点有两个直接下游 节点A 和 节点B
    并且 分支节点 branch.rules 为
      """
      amount>100=节点A
      default=节点B
      """
    并且 运行时变量 amount = 150
    当 工作流执行到分支节点
    那么 节点A 状态应变为 scheduled
    并且 节点B 状态应变为 skipped

  场景: 保存时校验规则中的目标节点名
    假如 分支节点直接下游只有 节点A 和 节点B
    当 用户在 branch.rules 中填写
      """
      amount>100=节点X
      default=节点B
      """
    那么 系统保存失败
    并且 错误信息提示规则引用了非直接下游节点
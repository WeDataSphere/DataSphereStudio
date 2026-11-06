# 语言: zh-CN
# Feature: 信号节点状态查询超时永久Running修复
# 需求编号: REQ-DSS-1.26.0-FIX-001
# 关联需求文档: docs/1.26.0/requirements/信号节点状态查询超时永久Running修复_需求.md

Feature: 信号节点状态查询超时永久Running修复
  当 Linkis entrance 机器负载高或发生 Full GC 时，DSS flow-execution-server 的节点状态查询 HTTP 请求超时，
  DefaultNodeRunner.isLinkisJobCompleted 的 Throwable 异常分支返回 true 但不翻转节点状态，
  NodeExecutionStatusPoller 据此停止轮询，导致节点永久 Running、工作流永久挂起。
  本 Feature 验证修复后：短暂抖动可自愈、长期不可用可收敛告警、未知状态不中断轮询、正常路径行为不变。

  Background:
    Given DSS flow-execution-server 服务已启动
    And Linkis entrance 服务已启动
    And 工作流A包含 EventSender 信号发送节点
    And 工作流B包含 EventReceiver 信号接收节点且依赖工作流A的信号
    And EventReceiver 节点的 msg.after.send 参数为 false
    And 状态获取失败阈值 wds.dss.flow.node.state.fetch.fail.timeout 已配置为 1800000 毫秒（默认30分钟）

  Rule: Bug复现 - 记录修复前的错误行为

    @bug @reproduction @skip
    Scenario: 复现Bug - entrance状态查询超时后节点永久Running
      Given 工作流A的 EventSender 节点已执行成功
      And 信号已写入 event_queue 表
      And 工作流B的 EventReceiver 节点已提交且状态为 Running
      When entrance 机器负载升高发生 Full GC
      And DSS 调用 entrance 状态查询接口抛出 SocketTimeoutException（readTimeout 默认300秒）
      Then isLinkisJobCompleted 走 case t:Throwable 分支返回 true
      And 节点状态未被 transitionState 翻转仍为 Running
      And NodeExecutionStatusPoller 将 runner 移出 nodeRunnerQueue 不再轮询
      And 日志输出 "status is completed" 但节点实际未完成
      And EventReceiver 节点永久停留 Running
      And 工作流B永久挂起

    @bug @reproduction @skip
    Scenario Outline: 复现Bug - 任意非ErrorException异常均触发永久Running
      Given 工作流B的 EventReceiver 节点已提交且状态为 Running
      When 状态查询过程抛出 <异常类型>
      Then isLinkisJobCompleted 走 case t:Throwable 分支返回 true
      And 节点状态停留 Running 且不再被轮询
      And 工作流B永久挂起

      Examples:
        | 异常类型                    |
        | SocketTimeoutException      |
        | HttpClientException         |
        | ConnectException（连接reset） |
        | NoSuchElementException（未知状态名） |

  Rule: Bug修复 - 短暂抖动自愈（F-P0-01/F-P0-04，AC-01）

    @bugfix @critical @smoke
    Scenario: 修复后 - entrance短暂Full GC恢复后节点继续轮询并正常翻转
      Given 工作流A的 EventSender 节点已执行成功且信号已写入 event_queue 表
      And 工作流B的 EventReceiver 节点已提交且状态为 Running
      When entrance 发生 Full GC 持续 5 分钟（小于失败阈值30分钟）
      Then 状态查询异常期间 isLinkisJobCompleted 返回 false
      And 节点仍保留在 nodeRunnerQueue 中被继续轮询
      And 日志周期输出连续失败 warn 日志（含节点名、失败时长、阈值剩余时间）
      When entrance 恢复正常
      Then EventReceiver 节点在下一个轮询周期内查询到 entrance 侧终态 Success
      And 节点状态翻转为 Succeed
      And 工作流B的下游节点正常调度执行
      And 不出现"返回true但节点状态为Running"的组合

    @bugfix
    Scenario: 修复后 - 连续失败期间失败计时正确累积与重置
      Given 工作流B的 EventReceiver 节点状态为 Running 且轮询正常
      When entrance 状态查询连续失败 10 分钟
      Then 日志输出的连续失败时长约为 10 分钟且小于阈值
      When entrance 恢复且下一次状态查询成功
      Then 连续失败计时被重置
      And 后续再次发生单次查询失败时失败时长从零重新累积

  Rule: Bug修复 - 长期不可用收敛告警（F-P0-02，AC-02）

    @bugfix @critical
    Scenario: 修复后 - entrance长期不可用超过阈值时节点翻Failed并告警
      Given 工作流B的 EventReceiver 节点已提交且状态为 Running
      And 失败阈值配置为加速验证用的较小值（如300000毫秒）
      When entrance 状态查询连续失败时长达到阈值
      Then 系统对节点对应的 Linkis 任务执行 kill
      And 节点状态翻转为 Failed（终态）
      And error 级告警日志输出（含节点名、flowExecId、失败时长、最后异常摘要）
      And runner 被移出 nodeRunnerQueue 且不再被重复处理
      And 工作流B正常结束为 Failed 状态且可通过重跑恢复
      And 工作流B不再永久挂起

  Rule: Bug修复 - 未知状态名兜底（F-P0-03，AC-03）

    @bugfix
    Scenario: 修复后 - entrance返回未知状态串时映射为Running继续轮询
      Given 工作流B的 EventReceiver 节点状态为 Running
      When entrance 状态查询返回 DSS NodeExecutionState 枚举中不存在的状态串 "SomeNewLinkisState"
      Then 未知状态被映射为 Running（非终态）
      And 节点不因 NoSuchElementException 中断轮询
      And warn 日志输出原始状态串 "SomeNewLinkisState"
      When entrance 恢复返回正常状态串
      Then 节点按真实终态正常翻转

  Rule: 回归验证 - 正常路径行为不变（AC-04/AC-05/AC-06）

    @regression @critical
    Scenario: 回归 - entrance健康时普通节点状态翻转正常
      Given entrance 服务健康
      And 工作流包含 Scriptis 脚本节点
      When 工作流正常执行
      Then 脚本节点按真实执行结果翻转为 Succeed
      And 节点翻转路径行为与修复前完全一致

    @regression @critical
    Scenario: 回归 - 节点执行失败与人工取消场景正常
      Given entrance 服务健康
      When 工作流中脚本节点执行抛错
      Then 该节点翻转为 Failed 且工作流正常结束
      When 用户对运行中的工作流执行取消操作
      Then 节点翻转为 Cancelled 且 runner 移出轮询队列

    @regression
    Scenario: 回归 - EmptyJob节点与EventSender节点不受影响
      Given 工作流包含 EmptyJob 空节点和 EventSender 信号发送节点
      When 工作流正常执行
      Then EmptyJob 节点提交时直接翻转为 Succeed
      And EventSender 发送成功后正常翻转为 Succeed
      And 两类节点均不进入异常分支

    @regression
    Scenario: 回归 - 不配置任何新增配置项时服务行为兼容
      Given 使用修复前相同的部署配置启动服务（未配置任何新增配置项）
      When 执行含信号节点的工作流全流程
      Then 新配置项按默认值生效且服务不报错
      And wds.linkis.flow.connection.timeout 语义与修复前一致
      And 全流程行为正常

  Rule: 应急方案验证 - kill重跑数据语义（AC-07）

    @regression @sop
    Scenario: 应急 - msg.after.send为false时kill重跑后旧信号可被消费
      Given 存在一个因本Bug已卡死 Running 的 EventReceiver 节点
      And 该节点的 msg.after.send 参数为 false
      When 运维对该卡死节点执行 kill
      And 重新运行工作流B
      Then 新实例按 event_status 表游标继续消费
      And 旧信号可被接收
      And EventReceiver 节点翻转为 Success

    @regression @sop
    Scenario: 应急 - msg.after.send为true时kill重跑后旧信号不被消费需重发
      Given 存在一个因本Bug已卡死 Running 的 EventReceiver 节点
      And 该节点的 msg.after.send 参数为 true
      When 运维对该卡死节点执行 kill
      And 重新运行工作流B
      Then 查询窗口起点为新实例的进程启动时间
      And 旧信号落在窗口外不被消费
      And EventReceiver 节点持续 Running 等待新信号
      When 业务方人工重发信号
      Then EventReceiver 节点翻转为 Success

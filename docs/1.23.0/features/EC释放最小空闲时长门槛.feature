# language: zh-CN
功能: EC 释放规则新增"最小空闲时长"门槛
  作为工作空间管理员
  我希望在 EC 释放规则里配置"最小空闲时长"
  以便只回收空闲足够久的引擎，保护高频复用引擎不被误杀

  背景:
    假如 用户已登录DSS系统且为工作空间管理员
    而且 用户已进入"工作空间管理 → EC 引擎释放规则"页面

  # ============ 前端：规则配置 ============

  场景: 新增规则时门槛字段默认为 0(不限)
    当 用户点击"新增释放规则"打开编辑弹窗
    那么 "最小空闲时长(分钟)"输入框默认显示 0
    而且 输入框最小值为 0，无法输入负数

    注意事项:
    - InputNumber 绑定 formData.minIdleMinutes，data() 默认 0
    - :min="0" 钳制下限

  场景: 保存规则时门槛值正确入库
    当 用户在"最小空闲时长(分钟)"输入 8
    而且 用户填写其他必填项并点击"确定"
    那么 规则保存成功
    而且 数据库 dss_ec_release_strategy.min_idle_minutes 该行为 8

    执行步骤:
    1. 新增/编辑弹窗 InputNumber 填 8
    2. 点击确定，saveRule 携带 minIdleMinutes 调用 saveEcReleaseStrategy
    3. 查库校验列值

  场景: 规则列表卡片正确展示门槛
    假如 存在规则 A(min_idle_minutes=0) 与规则 B(min_idle_minutes=8)
    那么 规则 A 卡片显示"最小空闲时长(分钟)：不限"
    而且 规则 B 卡片显示"最小空闲时长(分钟)：8分钟"

  场景: 编辑已有规则时回填门槛值
    假如 存在规则 B(min_idle_minutes=8)
    当 用户点击规则 B 的"编辑"
    那么 "最小空闲时长(分钟)"输入框显示 8

  # ============ 后端：候选门槛过滤 ============

  场景: 门槛为 0(不限)时候选集合与改动前一致
    假如 规则 minIdleMinutes=0 且触发条件成立
    而且 队列空闲引擎集合为 S(含各种空闲时长)
    当 定时任务执行候选筛选
    那么 候选集合等于 S
    而且 未剔除任何引擎

    验证步骤:
    1. minIdleMs=0 时 .filter 恒真
    2. 候选数 == fetchECInstance(经队列名过滤)的结果数

  场景: 门槛为 5 分钟时空闲不足的引擎被剔除
    假如 规则 minIdleMinutes=5 且触发条件成立
    而且存在引擎 E1(空闲4分钟)、E2(空闲6分钟)，均 Unlock 且属该队列
    当 定时任务执行候选筛选
    那么 E1 不在候选集合
    而且 E2 在候选集合

    验证步骤:
    1. minIdleMs = 5 * 60_000 = 300000
    2. E1 unlockDuration=240000 < 300000 → 滤掉
    3. E2 unlockDuration=360000 >= 300000 → 保留

  场景: 短空闲引擎不进入 kill 优先队列
    假如 规则 minIdleMinutes=5 且触发条件成立
    而且候选含 E1(空闲1分钟)、E2(空闲6分钟)
    当 进入 while kill 循环
    那么 仅 E2 进入 UserFairECKillingPriorityQueue
    而且 E1 未被 poll、未加入 kill 请求列表

  场景: 触发条件不满足时门槛无副作用
    假如 规则 minIdleMinutes=5
    而且队列用量未达触发高水位
    当 定时任务执行
    那么 不进入候选筛选分支
    而且 无引擎被 kill

  场景: 未知空闲时长的引擎被保守剔除
    假如 规则 minIdleMinutes=5
    而且引擎 E 的 lastUnlockTimestamp 为空
    当 定时任务执行候选筛选
    那么 E 的 unlockDuration 被置 0
    而且 E 被门槛滤掉(0 < 300000)

  # ============ 数据：默认值固化 ============

  场景: 存量规则升级后门槛默认为 0
    当 执行 dss_1.23.0_update.sql 的 ALTER TABLE
    那么 min_idle_minutes 列为 NOT NULL DEFAULT 0
    而且所有存量行值为 0

  场景: 后端字段为基本类型 int 默认 0
    当 new ECReleaseStrategy() 与 new ECReleaseStrategyDO()
    那么 getMinIdleMinutes() 返回 0
    而且不可能为 null(基本类型)

  # ============ 范围边界 ============

  场景: 门槛不改变 kill 优先队列的排序逻辑
    假如 规则 minIdleMinutes=3
    那么进入候选池的引擎仍按 UserFairECKillingPriorityQueue 三阶段排序
    而且内部 LONG_TIME_THRESHOLD(5min) 分组逻辑不变

  场景: 门槛不修复 fetch→kill 竞态(已知范围外)
    假如 规则 minIdleMinutes=5
    而且某引擎 fetch 时 Unlock，门槛放行
    当 该引擎在 kill 间隙被新任务锁走但仍被 kill
    那么 该任务失败(本任务不修此竞态，见设计文档 §6)

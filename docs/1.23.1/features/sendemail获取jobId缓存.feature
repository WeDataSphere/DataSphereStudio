# language: zh-CN
功能: sendemail 节点执行内缓存 CS 获取的 jobIds
  作为 DSS 工作流开发者
  我希望 sendemail 节点在同一次执行内重复获取上游节点 jobIds 时命中执行内缓存
  以便减少 CS 重复查询、加快节点执行且不改变任何对外行为

  背景:
    假如 用户已登录 DSS 系统并已创建包含上游节点与 sendemail 节点的工作流
    而且 DSS 服务器与 CS（ContextService）服务网络连通
    而且 sendemail 节点内容来源为上游节点（category=node）

  # ============================================================
  # 场景1: 缓存命中（核心收益）
  # ============================================================

  场景: 同一节点执行内多次获取 jobIds 时仅首次查询 CS
    假如 sendemail 节点关联 2 个以上上游节点
    而且 节点执行过程中邮件内容生成触发了多次 EmailCSHelper.getJobIds 调用
    当 执行 sendemail 节点
    那么 首次调用从 CS 加载 jobIds，日志输出 From cs to getJob ids
    而且 后续调用命中执行内缓存，日志输出 From sendemail execution cache to get Job IDs
    而且 整个执行过程中 From cs to getJob ids 日志仅出现一次
    而且 sendemail 节点状态为成功

  场景: 缓存返回的 jobIds 内容与 CS 加载结果一致
    假如 sendemail 节点关联多个上游节点
    当 执行 sendemail 节点
    那么 命中缓存返回的 jobIds 与首次加载结果在元素值与顺序上完全一致
    而且 邮件内容（图片/HTML 附件）与升级前版本一致

  场景: 缓存返回的数组为副本，调用方修改不影响缓存
    假如 sendemail 节点执行过程中已命中 jobIds 缓存
    当 调用方对返回的数组进行排序或增删元素
    那么 同一执行内后续获取 jobIds 仍返回原始顺序与原始长度

  # ============================================================
  # 场景2: 首次加载（原逻辑保持）
  # ============================================================

  场景: 执行内首次获取 jobIds 走 CS 原有加载链
    假如 本次节点执行尚未加载过 jobIds
    当 执行 sendemail 节点触发首次 EmailCSHelper.getJobIds
    那么 从 runtimeMap 的 content 解析上游节点 ID 列表
    而且 每个节点依次执行 getNodeNameByNodeID 与 getLinkisJobData 查询
    而且 加载成功后 jobIds 以副本形式写入 runtimeMap 缓存
    而且 日志输出 Job IDs is ...

  场景: content 为空时仍抛 80003 异常且不写缓存
    假如 sendemail 节点的 runtimeMap 中 content 为空列表
    当 执行 sendemail 节点
    那么 抛出 EmailSendFailedException(80003, empty result set is not allowed)
    而且 runtimeMap 中未写入 __dss_sendemail_job_ids_cache__ 缓存
    而且 sendemail 节点状态为失败

  # ============================================================
  # 场景3: 执行结束清理（生命周期）
  # ============================================================

  场景: 节点执行成功后清理 jobIds 缓存
    假如 sendemail 节点执行过程中已写入 jobIds 缓存
    而且 邮件发送成功
    当 节点执行正常返回
    那么 runtimeMap 中的 __dss_sendemail_job_ids_cache__ 被移除
    而且 下一次节点执行重新从 CS 加载 jobIds

  场景: 节点执行失败后同样清理 jobIds 缓存
    假如 sendemail 节点执行过程中已写入 jobIds 缓存
    而且 邮件发送或飞书外发失败导致节点返回错误响应
    当 节点执行以失败结束
    那么 runtimeMap 中的 __dss_sendemail_job_ids_cache__ 仍被移除（tryFinally 无条件清理）

  场景: 执行体抛出异常时缓存也被清理
    假如 sendemail 节点执行过程中已写入 jobIds 缓存
    而且 执行体抛出未捕获异常
    当 异常向外传播
    那么 清理动作在异常传播前执行，缓存被移除

  # ============================================================
  # 场景4: 并发与边界
  # ============================================================

  场景: 并发首次获取 jobIds 时仅触发一次 CS 加载
    假如 同一节点执行内多个线程同时首次调用 EmailCSHelper.getJobIds
    当 两个线程竞争加载
    那么 仅一个线程执行 CS 加载链，另一线程等锁后命中缓存
    而且 两个线程拿到的 jobIds 内容一致

  场景: 清理方法对空上下文不抛异常
    假如 ExecutionRequestRefContext 或其 runtimeMap 为 null
    当 调用 EmailCSHelper.clearJobIdsCache
    那么 静默返回，不抛出任何异常

  # ============================================================
  # 场景5: 兼容性验证
  # ============================================================

  场景: 现有 sendemail 工作流行为与升级前完全一致
    假如 DSS 升级到包含 jobIds 缓存的版本
    而且 存量工作流的 sendemail 节点参数与配置未做任何修改
    当 执行存量工作流的 sendemail 节点
    那么 邮件收件人、主题、附件内容与升级前完全一致
    而且 无任何新增配置项要求
    而且 sendemail 节点状态与升级前一致

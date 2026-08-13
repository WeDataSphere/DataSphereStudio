# language: zh-CN
功能: sendemail 节点调用 DataGo 数据外发图片消息
  作为 DSS 工作流开发者
  我希望在 sendemail 节点中选择发送飞书后，通过 DataGo 数据外发通道投递飞书图片消息
  以便工作流执行结果经敏感检测后安全地通知到指定飞书用户

  背景:
    假如 用户已登录 DSS 系统
    而且 DSS 服务器已配置 DataGo 外发参数（api.base.url / token）
    而且 DSS 服务器与 DataGo outbound 接口网络连通

    # 配置前提（appconn.properties）
    # | 配置项 | 值 | 说明 |
    # | wds.dss.appconn.datago.outbound.api.base.url | http://DATAGO_HOST:3003 | DataGo 基础地址 |
    # | wds.dss.appconn.datago.outbound.token | 有效固定Token | DSS 服务间 Bearer Token |

  # ============================================================
  # 场景1: sendFeishu 开关控制
  # ============================================================

  场景: 节点选择发送飞书且配置完整时，sendemail 节点同时发送邮件并发起 DataGo 外发
    假如 sendemail 节点参数 sendFeishu=true
    而且 DataGo 外发 api.base.url 和 token 配置正确
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    而且 sendemail 节点包含邮件主题和 PNG 图片附件
    当 执行 sendemail 节点
    那么 邮件发送成功
    而且 调用 DataGo ① 受理接口 POST /api/outbound/send（source=dss,type=image）
    而且 调用 DataGo ② 轮询接口 POST /api/outbound/task 直到终态
    而且 DataGo 任务状态为 exported
    而且 飞书接收者收到文本消息和图片消息
    而且 sendemail 节点状态为成功

  场景: 节点未选择发送飞书时，sendemail 节点仅发送邮件
    假如 sendemail 节点参数 sendFeishu=false
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    而且 sendemail 节点包含邮件主题和附件
    当 执行 sendemail 节点
    那么 邮件发送成功
    而且 不调用任何 DataGo 外发接口
    而且 sendemail 节点状态为成功

  # ============================================================
  # 场景2: feishuTo 参数控制
  # ============================================================

  场景: feishuTo 为空时，仅发送邮件不发起 DataGo 外发
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点参数 feishuTo 为空或未配置
    而且 sendemail 节点包含邮件主题和附件
    当 执行 sendemail 节点
    那么 邮件发送成功
    而且 不调用任何 DataGo 外发接口
    而且 sendemail 节点状态为成功

  场景: feishuTo 为纯空格时，仅发送邮件不发起 DataGo 外发
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点参数 feishuTo 为"   "（仅空格）
    当 执行 sendemail 节点
    那么 邮件发送成功
    而且 不调用任何 DataGo 外发接口
    而且 sendemail 节点状态为成功

  场景: feishuTo 配置多个接收人时，所有接收人均作为 recipients 提交
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点参数 feishuTo 为"zhangsan;lisi;wangwu"
    而且 sendemail 节点包含邮件主题和 1 个 PNG 附件
    当 执行 sendemail 节点
    那么 邮件发送成功
    而且 DataGo ① 受理请求 recipients 为 ["zhangsan","lisi","wangwu"]
    而且 DataGo 任务 exported 后三个接收人均收到飞书图片消息
    而且 sendemail 节点状态为成功

  场景: feishuTo 含前后空格时，空格被自动去除
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点参数 feishuTo 为"  zhangsan  ;  lisi  "
    当 执行 sendemail 节点
    那么 DataGo ① 受理请求 recipients 为 ["zhangsan","lisi"]
    而且 不向含空格的工号提交外发

  # ============================================================
  # 场景3: 文本消息内容（text 来源）
  # ============================================================

  场景: 飞书外发以邮件主题作为 text 提交
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点邮件主题为"2026年7月审计报表"
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    当 执行 sendemail 节点
    那么 DataGo ① 受理请求 text 为"2026年7月审计报表"

  场景: 邮件主题为 null 时，飞书外发使用默认 text
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点邮件主题为 null
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    当 执行 sendemail 节点
    那么 DataGo ① 受理请求 text 为"DSS Email Notification"

  场景: 邮件主题为纯空格时，飞书外发使用默认 text
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点邮件主题为"   "
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    当 执行 sendemail 节点
    那么 DataGo ① 受理请求 text 为"DSS Email Notification"

  场景: 邮件主题含特殊字符时，text 正确转义
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点邮件主题包含双引号和换行符
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    当 执行 sendemail 节点
    那么 recipients JSON 中双引号被转义为 \"
    而且 recipients JSON 格式合法

  # ============================================================
  # 场景4: 附件格式支持（仅图片提交为 images）
  # ============================================================

  场景: 飞书外发提交 PNG 图片附件
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    而且 sendemail 节点包含 PNG 图片附件
    当 执行 sendemail 节点
    那么 PNG 图片作为 images 字段提交到 DataGo ① 受理接口
    而且 DataGo 检测通过后飞书接收者收到真图片消息

  场景: 飞书外发不提交 CSV 附件
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    而且 sendemail 节点包含 CSV 附件
    当 执行 sendemail 节点
    那么 CSV 附件不作为 images 提交
    而且 DataGo ① 受理请求 images 为空

  场景: 飞书外发不提交 Excel/PDF/Markdown 附件
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    而且 sendemail 节点包含 Excel、PDF、Markdown 附件
    当 执行 sendemail 节点
    那么 这些非图片附件均不作为 images 提交
    而且 DataGo ① 受理请求 images 为空

  场景: sendemail 节点无附件时，DataGo 仅受理文本
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    而且 sendemail 节点无附件（仅邮件正文）
    当 执行 sendemail 节点
    那么 DataGo ① 受理请求 images 为空
    而且 DataGo 任务 exported 后飞书接收者仅收到文本消息

  场景: sendemail 节点含多个附件时，仅图片附件提交为 images
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    而且 sendemail 节点包含 3 个附件（CSV、Excel、PNG）
    当 执行 sendemail 节点
    那么 仅 PNG 图片附件作为 images 提交
    而且 DataGo ① 受理请求 imageCount 为 1

  # ============================================================
  # 场景5: 配置校验
  # ============================================================

  场景: 节点选择发送飞书但 token 为空时，sendemail 节点执行失败
    假如 sendemail 节点参数 sendFeishu=true
    而且 wds.dss.appconn.datago.outbound.token 为空
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    当 执行 sendemail 节点
    那么 抛出 IllegalArgumentException
    而且 异常消息包含"outbound token is not configured"
    而且 sendemail 节点状态为失败

  场景: 节点选择发送飞书但 api.base.url 为空时，sendemail 节点执行失败
    假如 sendemail 节点参数 sendFeishu=true
    而且 wds.dss.appconn.datago.outbound.api.base.url 为空
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    当 执行 sendemail 节点
    那么 抛出 IllegalArgumentException
    而且 异常消息包含"api.base.url is not configured"
    而且 sendemail 节点状态为失败

  场景: 轮询参数配置为非正数时校验失败
    假如 sendemail 节点参数 sendFeishu=true
    而且 wds.dss.appconn.datago.outbound.poll.interval 为 0
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    当 执行 sendemail 节点
    那么 抛出 IllegalArgumentException
    而且 异常消息包含"poll.interval must be positive"

  # ============================================================
  # 场景6: 受理与轮询
  # ============================================================

  场景: ① 受理成功返回 taskId 后进入轮询
    假如 sendemail 节点参数 sendFeishu=true
    而且 DataGo ① 受理接口返回 success=true 且 taskId=2048
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    当 执行 sendemail 节点
    那么 调用 ② 轮询接口携带 taskId=2048 和 source=dss
    而且 请求头携带 Authorization: Bearer <token>

  场景: ① 受理 502/504 时按配置退避重试
    假如 sendemail 节点参数 sendFeishu=true
    而且 DataGo ① 受理接口前两次返回 502
    而且 第三次返回 success=true 且 taskId=2048
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    当 执行 sendemail 节点
    那么 受理重试 2 次后成功
    而且 继续轮询直到终态

  场景: ① 受理 502/504 重试次数耗尽时节点失败
    假如 sendemail 节点参数 sendFeishu=true
    而且 DataGo ① 受理接口连续返回 502 超过 retry.max 次
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    当 执行 sendemail 节点
    那么 抛出 EmailSendFailedException(81002)
    而且 sendemail 节点状态为失败

  场景: ② 轮询至 exported 时外发成功
    假如 sendemail 节点参数 sendFeishu=true
    而且 DataGo ② 轮询接口返回 status=exported
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    当 执行 sendemail 节点
    那么 sendemail 节点状态为成功
    而且 飞书接收者收到图片消息

  场景: ② 轮询命中敏感（detected_fail）时节点失败且不重发
    假如 sendemail 节点参数 sendFeishu=true
    而且 DataGo ② 轮询接口返回 status=detected_fail
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    当 执行 sendemail 节点
    那么 抛出 EmailSendFailedException(81004)
    而且 sendemail 节点状态为失败
    而且 DataGo 已主动通知接收人，sendemail 不重复发送

  场景: ② 轮询异常（detect_error）时节点失败
    假如 sendemail 节点参数 sendFeishu=true
    而且 DataGo ② 轮询接口返回 status=detect_error
    当 执行 sendemail 节点
    那么 抛出 EmailSendFailedException(81004)
    而且 sendemail 节点状态为失败

  场景: ② 轮询投递失败（export_failed）时节点失败
    假如 sendemail 节点参数 sendFeishu=true
    而且 DataGo ② 轮询接口返回 status=export_failed
    当 执行 sendemail 节点
    那么 抛出 EmailSendFailedException(81004)
    而且 sendemail 节点状态为失败

  场景: ② 轮询长时间非终态时超时失败
    假如 sendemail 节点参数 sendFeishu=true
    而且 wds.dss.appconn.datago.outbound.max.wait=120
    而且 DataGo ② 轮询接口持续返回 status=pending 超过 120 秒
    当 执行 sendemail 节点
    那么 抛出 EmailSendFailedException(81005)
    而且 异常消息包含"polling timed out"

  场景: ② 轮询 502/504 时按配置退避重试
    假如 sendemail 节点参数 sendFeishu=true
    而且 DataGo ② 轮询接口首次返回 504
    而且 第二次返回 status=exported
    当 执行 sendemail 节点
    那么 轮询重试 1 次后成功
    而且 sendemail 节点状态为成功

  # ============================================================
  # 场景7: 图片附件准备
  # ============================================================

  场景: 图片附件有 File 引用时直接使用 File 提交
    假如 sendemail 节点参数 sendFeishu=true
    而且 附件的 getFile() 返回存在的 File 对象
    当 执行 sendemail 节点
    那么 直接使用 File 对象作为 images 提交
    而且 不创建临时文件

  场景: 图片附件无 File 引用时使用 Base64 解码临时文件提交
    假如 sendemail 节点参数 sendFeishu=true
    而且 附件的 getFile() 返回 null
    而且 附件的 getBase64Str() 返回合法的 Base64 编码
    当 执行 sendemail 节点
    那么 将 Base64 解码后写入临时文件并作为 images 提交

  场景: 图片附件超过 10MB 时外发失败
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点包含大于 10MB 的 PNG 图片附件
    当 执行 sendemail 节点
    那么 抛出 EmailSendFailedException(81006)
    而且 异常消息包含"exceeds"
    而且 sendemail 节点状态为失败

  场景: 图片附件 Base64 非法时外发失败
    假如 sendemail 节点参数 sendFeishu=true
    而且 附件的 getFile() 返回 null
    而且 附件的 getBase64Str() 返回非法 Base64 字符串
    当 执行 sendemail 节点
    那么 抛出 EmailSendFailedException(81006)
    而且 异常消息包含"decode base64"

  # ============================================================
  # 场景8: 异常处理
  # ============================================================

  场景: 邮件发送失败时不执行 DataGo 外发
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    而且 邮件发送因 SMTP 异常而失败
    当 执行 sendemail 节点
    那么 不调用任何 DataGo 外发接口
    而且 sendemail 节点状态为失败
    而且 错误消息包含"发送邮件失败"

  场景: DataGo 外发失败时节点标记为失败
    假如 sendemail 节点参数 sendFeishu=true
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    而且 DataGo ① 受理接口返回业务失败
    当 执行 sendemail 节点
    那么 邮件发送成功
    而且 抛出 EmailSendFailedException(81002)
    而且 sendemail 节点状态为失败
    而且 错误消息包含"飞书发送失败"

  # ============================================================
  # 场景9: 兼容性验证
  # ============================================================

  场景: 未升级配置的现有 sendemail 节点不受影响
    假如 DSS 未配置任何 DataGo 外发相关配置项（使用默认值）
    而且 sendemail 节点未配置 feishuTo 参数
    而且 sendemail 节点包含邮件主题和附件
    当 执行 sendemail 节点
    那么 邮件发送成功
    而且 不执行任何 DataGo 外发相关逻辑
    而且 sendemail 节点行为与升级前完全一致

  场景: 配置自定义 DataGo 基础地址时请求发送到自定义地址
    假如 sendemail 节点参数 sendFeishu=true
    而且 wds.dss.appconn.datago.outbound.api.base.url=http://custom-datago.example.com:3003
    而且 sendemail 节点参数 feishuTo 配置了有效的飞书接收人工号
    当 执行 sendemail 节点
    那么 DataGo 外发请求发送到自定义地址

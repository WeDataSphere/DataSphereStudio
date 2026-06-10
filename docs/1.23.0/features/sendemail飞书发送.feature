# language: zh-CN
功能: sendemail节点飞书发送功能
  作为DSS工作流开发者
  我希望在sendemail节点中配置飞书发送
  以便工作流执行结果能同时通过飞书通知到指定用户

  背景:
    假如 用户已登录DSS系统
    而且 DSS服务器已配置飞书应用参数（appId/appToken）
    而且 DSS服务器与飞书API网络连通

    # 配置前提
    # | 配置项 | 值 | 说明 |
    # |-------|-----|------|
    # | wds.dss.appconn.feishu.app.id | 有效AppID | 飞书应用App ID |
    # | wds.dss.appconn.feishu.app.token | 有效appToken | 飞书应用App Token |

  # ============================================================
  # 场景1: 节点sendFeishu选项控制
  # ============================================================

  场景: 节点选择发送飞书且配置完整时，sendemail节点同时发送邮件和飞书消息
    假如 sendemail节点参数sendFeishu=true
    而且 飞书应用appId和appToken配置正确
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    而且 sendemail节点包含邮件主题和CSV附件
    当 执行sendemail节点
    那么 邮件发送成功
    而且 飞书接收者收到文本消息"[DSS邮件通知] {邮件主题}"
    而且 飞书接收者收到CSV飞书消息
    而且 sendemail节点状态为成功

  场景: 节点未选择发送飞书时，sendemail节点仅发送邮件
    假如 sendemail节点参数sendFeishu=false
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    而且 sendemail节点包含邮件主题和附件
    当 执行sendemail节点
    那么 邮件发送成功
    而且 不调用任何飞书API
    而且 sendemail节点状态为成功

  # ============================================================
  # 场景2: feishuTo参数控制
  # ============================================================

  场景: feishuTo为空时，仅发送邮件不发送飞书消息
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo为空或未配置
    而且 sendemail节点包含邮件主题和附件
    当 执行sendemail节点
    那么 邮件发送成功
    而且 不执行飞书发送
    而且 sendemail节点状态为成功

  场景: feishuTo为纯空格时，仅发送邮件不发送飞书消息
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo为"   "（仅空格）
    当 执行sendemail节点
    那么 邮件发送成功
    而且 不执行飞书发送
    而且 sendemail节点状态为成功

  场景: feishuTo配置多个接收者时，所有接收者均收到飞书消息
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo为"zhangsan;lisi;wangwu"
    而且 sendemail节点包含邮件主题和1个CSV附件
    当 执行sendemail节点
    那么 邮件发送成功
    而且 zhangsan收到文本消息和飞书消息
    而且 lisi收到文本消息和飞书消息
    而且 wangwu收到文本消息和飞书消息
    而且 sendemail节点状态为成功

  场景: feishuTo含前后空格时，空格被自动去除
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo为"  zhangsan  ;  lisi  "
    当 执行sendemail节点
    那么 zhangsan和lisi均收到飞书消息
    而且 不向含空格的ID发送消息

  # ============================================================
  # 场景3: 飞书消息内容
  # ============================================================

  场景: 飞书发送邮件主题作为文本消息
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点邮件主题为"2024年Q4销售报表"
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    当 执行sendemail节点
    那么 飞书接收者收到文本消息"[DSS邮件通知] 2024年Q4销售报表"

  场景: 邮件主题为null时，飞书使用默认主题发送文本消息
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点邮件主题为null
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    当 执行sendemail节点
    那么 飞书接收者收到文本消息"[DSS邮件通知] DSS Email Notification"

  场景: 邮件主题含特殊字符时，飞书文本消息正确转义
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点邮件主题包含双引号和换行符
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    当 执行sendemail节点
    那么 飞书文本消息中双引号被转义为\"
    而且 飞书文本消息中换行符被转义为\n
    而且 飞书消息JSON格式合法

  # ============================================================
  # 场景4: 附件格式支持
  # ============================================================

  场景: 飞书发送CSV格式附件
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    而且 sendemail节点包含CSV附件
    当 执行sendemail节点
    那么 CSV附件不上传到飞书
    而且 飞书接收者收到CSV飞书消息

  场景: 飞书发送Excel格式附件
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    而且 sendemail节点包含Excel附件
    当 执行sendemail节点
    那么 Excel附件不上传到飞书
    而且 飞书接收者收到Excel飞书消息

  场景: 飞书发送PNG图片附件
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    而且 sendemail节点包含PNG图片附件
    当 执行sendemail节点
    那么 PNG图片成功上传到飞书并返回key
    而且 飞书接收者收到携带图片key的飞书消息

  场景: 飞书发送PDF格式附件
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    而且 sendemail节点包含PDF附件
    当 执行sendemail节点
    那么 PDF附件不上传到飞书
    而且 飞书接收者收到PDF飞书消息

  场景: 飞书发送Markdown格式附件
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    而且 sendemail节点包含Markdown附件
    当 执行sendemail节点
    那么 Markdown附件不上传到飞书
    而且 飞书接收者收到Markdown飞书消息

  场景: sendemail节点无附件时，飞书仅发送文本主题消息
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    而且 sendemail节点无附件（仅邮件正文）
    当 执行sendemail节点
    那么 飞书接收者仅收到文本消息
    而且 不发送任何飞书消息

  场景: sendemail节点含多个附件时，仅图片附件上传并通过一次飞书模板消息发送
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    而且 sendemail节点包含3个附件（CSV、Excel、PNG）
    当 执行sendemail节点
    那么 仅PNG图片附件上传到飞书并得到图片key
    而且 飞书接收者收到1条携带邮件主题和图片key的模板消息

  # ============================================================
  # 场景5: 配置校验
  # ============================================================

  场景: 节点选择发送飞书但appId为空时，sendemail节点执行失败
    假如 sendemail节点参数sendFeishu=true
    而且 wds.dss.appconn.feishu.app.id为空
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    当 执行sendemail节点
    那么 抛出IllegalArgumentException
    而且 异常消息包含"app.id is not configured"
    而且 sendemail节点状态为失败

  场景: 节点选择发送飞书但appToken为空时，sendemail节点执行失败
    假如 sendemail节点参数sendFeishu=true
    而且 wds.dss.appconn.feishu.app.token为空
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    当 执行sendemail节点
    那么 抛出IllegalArgumentException
    而且 异常消息包含"app.token is not configured"
    而且 sendemail节点状态为失败

  # ============================================================
  # 场景6: 异常处理
  # ============================================================

  场景: 邮件发送失败时，不执行飞书发送
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    而且 邮件发送因SMTP异常而失败
    当 执行sendemail节点
    那么 不执行飞书发送
    而且 sendemail节点状态为失败
    而且 错误消息包含"发送邮件失败"

  场景: 飞书Token获取失败时，sendemail节点执行失败
    假如 sendemail节点参数sendFeishu=true
    而且 飞书appId或appToken无效
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    当 执行sendemail节点
    那么 邮件发送成功
    而且 飞书Token获取失败
    而且 抛出EmailSendFailedException(80002)
    而且 sendemail节点状态为失败
    而且 错误消息包含"飞书发送失败"

  场景: 飞书文件上传失败时，sendemail节点执行失败
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    而且 飞书文件上传API返回错误
    当 执行sendemail节点
    那么 邮件发送成功
    而且 飞书主题文本消息发送成功
    而且 飞书附件上传失败
    而且 抛出EmailSendFailedException(80007)
    而且 sendemail节点状态为失败

  场景: 向无效飞书接收人英文名发送飞书消息时，sendemail节点执行失败
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo包含无效的飞书接收人英文名
    当 执行sendemail节点
    那么 邮件发送成功
    而且 飞书API返回接收者无效错误
    而且 抛出EmailSendFailedException(80004)
    而且 sendemail节点状态为失败

  # ============================================================
  # 场景7: 飞书接口签名认证
  # ============================================================

  场景: 飞书发送请求携带FS签名认证头
    假如 sendemail节点参数sendFeishu=true
    而且 飞书应用配置正确
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    当 执行sendemail节点
    那么 请求飞书接口时携带FS-AppId、FS-Nonce、FS-Timestamp、FS-Signature、FS-Source
    而且 FS-Signature按sha256(sha256(AppId + Nonce + Timestamp) + appToken)生成
    而且 飞书消息发送成功

  场景: 每次飞书请求生成新的Nonce和Timestamp
    假如 sendemail节点参数sendFeishu=true
    而且 飞书应用配置正确
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    当 执行sendemail节点
    那么 每次上传文件和发送消息请求均重新生成FS-Nonce和FS-Timestamp
    而且 每次请求均重新生成FS-Signature

  场景: appToken错误导致飞书认证失败
    假如 sendemail节点参数sendFeishu=true
    而且 飞书appToken无效
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    当 执行sendemail节点
    那么 邮件发送成功
    而且 飞书接口返回认证失败
    而且 sendemail节点状态为失败

  # ============================================================
  # 场景8: 附件上传模式
  # ============================================================

  场景: 附件有File引用时，直接使用File上传到飞书
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    而且 附件的getFile()返回存在的File对象
    当 执行sendemail节点
    那么 直接使用File对象上传到飞书
    而且 不创建临时文件

  场景: 附件无File引用时，使用Base64解码临时文件上传
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    而且 附件的getFile()返回null
    而且 附件的getBase64Str()返回合法的Base64编码
    当 执行sendemail节点
    那么 将Base64解码后写入临时文件
    而且 使用临时文件上传到飞书
    而且 上传完成后临时文件被删除

  场景: 附件Base64非法时，上传失败并抛出异常
    假如 sendemail节点参数sendFeishu=true
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    而且 附件的getFile()返回null
    而且 附件的getBase64Str()返回非法Base64字符串
    当 执行sendemail节点
    那么 Base64解码失败
    而且 临时文件在finally块中被清理
    而且 抛出异常

  # ============================================================
  # 场景9: 自定义飞书API地址
  # ============================================================

  场景: 配置自定义飞书API地址时，请求发送到自定义地址
    假如 sendemail节点参数sendFeishu=true
    而且 wds.dss.appconn.feishu.api.base.url=https://custom-proxy.example.com/open-apis
    而且 sendemail节点参数feishuTo配置了有效的飞书接收人英文名
    当 执行sendemail节点
    那么 飞书API请求发送到自定义地址
    而且 飞书消息发送成功

  # ============================================================
  # 场景10: 兼容性验证
  # ============================================================

  场景: 未升级配置的现有sendemail节点不受影响
    假如 DSS未配置任何飞书相关配置项（使用默认值）
    而且 sendemail节点未配置feishuTo参数
    而且 sendemail节点包含邮件主题和附件
    当 执行sendemail节点
    那么 邮件发送成功
    而且 不执行任何飞书相关逻辑
    而且 sendemail节点行为与升级前完全一致


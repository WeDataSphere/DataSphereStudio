# LLM AppConn 使用文档

## 概述

LLM AppConn是DSS工作流中的AI节点，提供大模型调用能力。支持OpenAI兼容的API、MCP工具配置、以及自动读取上游节点数据作为prompt输入。

## 功能特性

- ✅ **OpenAI兼容API**: 遵循OpenAI API标准，支持各种兼容的LLM服务
- ✅ **API Key认证**: 通过HTTP Header传递API Key进行安全认证
- ✅ **系统提示词配置**: 可自定义AI助手的角色和行为规范
- ✅ **上游数据自动读取**: 自动获取上游所有节点的结果集作为上下文
- ✅ **MCP工具支持**: 通过JSON配置支持Model Context Protocol工具
- ✅ **结果集保存**: 将LLM响应保存到Context Service供下游节点使用
- ✅ **重试机制**: 内置API调用失败自动重试，提升稳定性
- ✅ **Token统计**: 实时统计prompt和completion的token使用量

## 部署步骤

### 1. 编译打包

在项目根目录执行：

```bash
cd dss-appconn/appconns/dss-llm-appconn
mvn clean package
```

打包后的文件位于：`target/out/llm/`

### 2. 部署到DSS

将打包后的目录复制到DSS安装目录：

```bash
cp -r target/out/llm $DSS_HOME/dss-appconns/
```

### 3. 数据库初始化

执行init.sql脚本：

```bash
mysql -u<username> -p<password> <database> < src/main/resources/init.sql
```

**重要**: 执行后需要更新`dss_appconn_instance`表中的配置：

```sql
UPDATE dss_appconn_instance
SET enhance_json = '{
  "llm.api.url": "https://api.openai.com/v1/chat/completions",
  "llm.api.key": "your-actual-api-key-here",
  "llm.max.retries": "3",
  "llm.retry.delay.ms": "1000"
}'
WHERE appconn_id = (SELECT id FROM dss_appconn WHERE appconn_name = 'llm');
```

### 4. 重启DSS服务

```bash
cd $DSS_HOME
sh bin/stop-all.sh
sh bin/start-all.sh
```

## 使用示例

### 示例1：数据分析助手

**场景**: 分析SQL查询结果并生成报告

1. **上游节点**: SQL查询节点，执行 `SELECT * FROM sales_data LIMIT 100`
2. **LLM节点配置**:
   - 系统提示词: `你是一个专业的数据分析师，擅长从数据中发现洞察`
   - 用户提示词: `请分析以下销售数据，给出关键发现和建议`
   - 模型: `gpt-4`
   - 温度: `0.3`（更确定的输出）
3. **下游节点**: 邮件发送节点，将分析报告发送给团队

### 示例2：数据清洗建议

**场景**: 对数据质量问题提供清洗建议

1. **上游节点**: 数据检查节点，输出数据质量报告
2. **LLM节点配置**:
   - 系统提示词: `你是数据治理专家，提供数据清洗和质量改进方案`
   - 用户提示词: `根据数据质量报告，提供详细的清洗方案`
   - 模型: `gpt-3.5-turbo`
   - MCP工具配置: (示例见下文)
3. **下游节点**: 脚本节点，执行清洗建议

### 示例3：多语言翻译

**场景**: 将数据表中的文本翻译成多种语言

1. **上游节点**: SQL查询，获取待翻译文本
2. **LLM节点配置**:
   - 系统提示词: `你是专业翻译，提供准确的多语言翻译`
   - 用户提示词: `将以下文本翻译成英语、日语和韩语`
   - 模型: `gpt-3.5-turbo`
   - 温度: `0.2`
3. **下游节点**: 将翻译结果写入数据库

## MCP工具配置示例

MCP工具配置采用JSON格式，遵循OpenAI Function Calling规范：

```json
[
  {
    "type": "function",
    "function": {
      "name": "execute_sql",
      "description": "执行SQL查询并返回结果",
      "parameters": {
        "type": "object",
        "properties": {
          "sql": {
            "type": "string",
            "description": "要执行的SQL查询语句"
          },
          "database": {
            "type": "string",
            "description": "目标数据库名称"
          }
        },
        "required": ["sql"]
      }
    }
  },
  {
    "type": "function",
    "function": {
      "name": "send_notification",
      "description": "发送通知消息",
      "parameters": {
        "type": "object",
        "properties": {
          "message": {
            "type": "string",
            "description": "通知内容"
          },
          "recipient": {
            "type": "string",
            "description": "接收者邮箱或用户名"
          }
        },
        "required": ["message", "recipient"]
      }
    }
  }
]
```

## 参数配置说明

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| llm.system.prompt | TextArea | 否 | "你是一个专业的数据分析助手" | 系统提示词，定义AI角色 |
| llm.user.prompt | TextArea | 是 | - | 用户提示词，描述任务需求 |
| llm.model | Select | 是 | gpt-3.5-turbo | LLM模型名称 |
| llm.temperature | Input | 否 | 0.7 | 温度参数（0-2），控制随机性 |
| llm.max.tokens | Input | 否 | 2000 | 最大生成Token数 |
| llm.mcp.tools | TextArea | 否 | - | MCP工具配置（JSON格式） |
| llm.save.key | Input | 否 | llm.response | Context Service保存键名 |

## 下游节点引用LLM结果

在下游节点中，可以通过`${llm.response}`（或自定义的save key）引用LLM的响应结果：

**示例**: 在邮件发送节点中

```
邮件正文：${llm.response}
```

**示例**: 在SQL节点中

```sql
INSERT INTO ai_analysis (content, created_at)
VALUES ('${llm.response}', NOW());
```

## 故障排查

### 问题1: API调用失败

**症状**: 节点执行失败，日志显示"LLM API调用失败"

**解决方案**:
1. 检查API URL是否正确
2. 验证API Key是否有效
3. 检查网络连接
4. 查看LLM服务是否可用

### 问题2: Token超限

**症状**: 执行失败，提示"超出最大Token限制"

**解决方案**:
1. 减少上游数据量
2. 增加`llm.max.tokens`参数
3. 优化prompt内容，减少不必要的描述

### 问题3: 结果为空

**症状**: 节点执行成功但下游无法获取结果

**解决方案**:
1. 检查`llm.save.key`配置是否正确
2. 确认下游节点引用的变量名与save key一致
3. 查看Context Service是否正常工作

### 问题4: 响应质量差

**症状**: LLM返回的内容不符合预期

**解决方案**:
1. 优化系统提示词，更明确地定义AI角色
2. 改进用户提示词，提供更详细的要求
3. 调整temperature参数：
   - 需要更确定的输出，降低temperature（0.1-0.3）
   - 需要更有创意的输出，提高temperature（0.7-1.0）
4. 尝试使用更强大的模型（如从gpt-3.5升级到gpt-4）

## 性能优化建议

1. **合理控制上游数据量**: 上游节点的数据会全部传递给LLM，建议限制在10000字符以内
2. **使用流式输出**: 对于长文本生成，可以考虑启用流式输出（需修改代码支持）
3. **缓存策略**: 对于重复的prompt，可以在上层实现缓存机制
4. **并发控制**: 避免同时提交大量LLM节点，建议使用工作流编排控制并发度
5. **模型选择**: 根据任务复杂度选择合适的模型：
   - 简单任务: gpt-3.5-turbo（快速、低成本）
   - 复杂任务: gpt-4（高质量、高成本）

## 最佳实践

### 1. Prompt设计原则

- **明确角色**: 在系统提示词中清晰定义AI的角色和专业领域
- **具体任务**: 用户提示词应详细描述期望的输出格式和内容
- **提供示例**: 在prompt中包含期望输出的示例（Few-shot Learning）
- **结构化输出**: 要求LLM以JSON、Markdown表格等结构化格式输出

### 2. 工作流设计

```
数据采集节点 → 数据清洗节点 → LLM分析节点 → 结果存储节点 → 通知节点
                                    ↓
                               可视化节点
```

### 3. 错误处理

- 在下游节点添加条件判断，检查LLM结果是否为空
- 使用DSS的异常处理机制捕获LLM节点失败
- 设置合理的重试次数和延迟

## 开发文档

### 扩展开发

如需扩展LLMAppConn功能，可以修改以下关键类：

- `LLMClient.java`: 修改API调用逻辑
- `LLMRefExecutionOperation.scala`: 修改执行流程
- `LLMUtils.java`: 添加辅助功能

### 添加新的LLM服务支持

1. 修改`LLMClient.java`中的API调用逻辑
2. 根据新服务的API规范调整请求和响应格式
3. 更新配置项（如认证方式、请求头等）

## 许可证

本项目基于 Apache License 2.0 开源协议。

## 联系方式

如有问题或建议，请提交Issue到项目仓库。

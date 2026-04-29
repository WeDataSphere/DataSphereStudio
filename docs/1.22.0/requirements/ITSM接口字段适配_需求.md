# ITSM接口字段适配_需求

## 一、功能概述

### 1.1 需求类型
新增功能（NEW）

### 1.2 需求描述
ITSM系统版本升级后，请求报文中新增了若干字段，DSS系统需要适配这些新增字段，确保：
- 接口能够正常接收并解析包含新字段的请求报文
- 对于未知字段采用静默忽略策略，不抛出异常
- 保持向后兼容性，不影响现有功能正常运行

### 1.3 基础模块
- **模块名称**：ITSM集成接口
- **涉及模块**：
  - `dss-framework-workspace-server`：工作空间管理（ItsmRequest、DSSWorkspaceRestful）
  - `dss-scriptis-server-webank`：Scriptis服务（ItsmRequest、ScriptisRestful）
  - `dss-framework-orchestrator-server`：编排管理（DSSFrameworkOrchestratorRestful）

### 1.4 当前痛点
ITSM系统版本升级后，请求报文结构发生变化，新增了若干字段。当前DSS系统的`ItsmRequest`类使用固定字段定义，无法接收新字段，可能导致：
- 反序列化时抛出异常，接口调用失败
- 业务流程中断

## 二、现有功能描述

### 2.1 ItsmRequest类现状
系统存在两处`ItsmRequest`定义：

#### 2.1.1 dss-framework-workspace-server模块
**位置**：`dss-framework/dss-framework-workspace-server/src/main/java/com/webank/wedatasphere/dss/framework/workspace/bean/itsm/ItsmRequest.java`

**现有字段**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| createDate | String | 创建日期 |
| createUser | String | 创建用户 |
| data | String | 数据内容（JSON字符串） |
| externalId | String | 外部ID |
| flowId | String | 流程ID |
| operateUser | String | 操作用户 |
| requestTitle | String | 请求标题 |
| style | String | 样式 |
| taskId | String | 任务ID |
| formId | String | 表单ID |

**使用接口**：
- `DSSWorkspaceRestful.updateWorkspace()`：工作空间新建/修改接口
- `DSSFrameworkOrchestratorRestful.addProjectAndOrchestratorWhite()`：添加工作流白名单接口

#### 2.1.2 dss-scriptis-server-webank模块
**位置**：`dss-apps/dss-scriptis-server-webank/src/main/java/com/webank/wedatasphere/dss/scriptis/bean/ItsmRequest.java`

**现有字段**：
| 字段名 | 类型 | 说明 |
|-------|------|------|
| createDate | String | 创建日期 |
| createUser | String | 创建用户 |
| data | String | 数据内容（JSON字符串） |
| externalId | String | 外部ID |
| flowId | String | 流程ID |
| operateUser | String | 操作用户 |
| requestTitle | String | 请求标题 |
| style | String | 样式 |
| taskId | String | 任务ID |
| expireTime | String | 过期时间（该模块特有） |

**使用接口**：
- `ScriptisRestful.addUserProxy()`：添加代理用户接口

### 2.2 现有反序列化方式
Spring MVC 使用 Jackson 进行 JSON 反序列化，默认情况下遇到未知字段会抛出 `UnrecognizedPropertyException` 异常。

### 2.3 问题分析
- Jackson 默认不允许未知字段，会导致反序列化失败
- 接口调用会因为未知字段而报错

## 三、详细功能描述

### 3.1 功能范围

#### 3.1.1 核心功能
| 序号 | 功能名称 | 功能描述 | 涉及模块 |
|-----|---------|---------|---------|
| 1 | 未知字段忽略 | 对于ITSM请求中的未知字段，静默忽略，不抛出异常 | 所有使用ItsmRequest的模块 |
| 2 | 向后兼容 | 确保现有接口功能不受影响，保持向后兼容 | 所有使用ItsmRequest的模块 |

### 3.2 功能详情

#### 3.2.1 未知字段处理策略
**触发条件**：当ITSM请求JSON中包含`ItsmRequest`类未定义的字段时

**处理方式**：
1. **静默忽略**：反序列化正常进行，不因未知字段而失败
2. **不记录日志**：减少日志输出，保持简洁

**实现方式**：在 `ItsmRequest` 类上添加 Jackson 注解 `@JsonIgnoreProperties(ignoreUnknown = true)`

#### 3.2.2 接口改造影响范围

| 接口 | 路径 | 改造内容 |
|-----|------|---------|
| updateWorkspace | /dss/framework/workspace/updateWorkspace | 使用改造后的ItsmRequest |
| addProjectAndOrchestratorWhite | /dss/framework/orchestrator/addOrchestratorWhite | 使用改造后的ItsmRequest |
| addUserProxy | /dss/scriptis/proxy/addUserProxy | 使用改造后的ItsmRequest |

### 3.3 权限控制
- 权限验证逻辑保持不变
- ITSM请求鉴权（timestamp + sign验证）保持不变
- 新增字段不影响现有权限判断逻辑

## 四、验收标准

### 4.1 未知字段处理验收

#### 4.1.1 输入验证
- ITSM请求包含`ItsmRequest`未定义的新字段
- ITSM请求不包含新字段（原有格式）

#### 4.1.2 处理验证
- 包含新字段的请求能够正常反序列化成功
- 不抛出任何异常
- 业务逻辑正常执行

#### 4.1.3 输出验证
- 接口返回结果与未添加新字段时一致

### 4.2 向后兼容验收

#### 4.2.1 输入验证
- 使用旧版ITSM请求格式（无新字段）调用接口

#### 4.2.2 处理验证
- 接口正常处理请求

#### 4.2.3 输出验证
- 接口返回结果与改造前完全一致

### 4.3 功能回归验收

#### 4.3.1 工作空间管理接口
| 测试场景 | 输入 | 预期结果 |
|---------|------|---------|
| 新建工作空间 | 包含新字段的ITSM请求 | 正常创建 |
| 修改工作空间 | 包含新字段的ITSM请求 | 正常修改 |
| 数据为空 | dataList为空 | 返回"data is empty"错误 |

#### 4.3.2 工作流白名单接口
| 测试场景 | 输入 | 预期结果 |
|---------|------|---------|
| 添加白名单 | 包含新字段的ITSM请求 | 正常添加 |
| 项目不存在 | 不存在的项目名 | 返回错误信息 |
| 工作流不存在 | 不存在的工作流名 | 返回错误信息 |

#### 4.3.3 代理用户接口
| 测试场景 | 输入 | 预期结果 |
|---------|------|---------|
| 添加代理用户 | 包含新字段的ITSM请求 | 正常添加 |
| 鉴权失败 | 错误的timestamp/sign | 返回403错误 |

## 五、非功能需求

### 5.1 性能要求
- 反序列化性能无影响
- 接口响应时间无变化

### 5.2 兼容性要求
- 向后兼容：支持旧版ITSM请求格式
- 向前兼容：支持未来新增字段
- JDK版本兼容：JDK 8+

### 5.3 安全要求
- 保持现有ITSM鉴权机制
- 未知字段不做业务处理

## 六、数据模型变更

### 6.1 ItsmRequest类变更

#### 变更内容
```java
// 添加导入
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// 添加注解
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItsmRequest {
    // 现有字段保持不变...
}
```

### 6.2 无接口变更
- 现有接口签名保持不变
- 现有接口参数保持不变
- 现有接口返回值保持不变

### 6.3 无数据库变更
- 本需求不涉及数据库表结构变更

## 七、影响分析

### 7.1 代码影响范围
| 模块 | 文件 | 改造类型 |
|-----|------|---------|
| dss-framework-workspace-server | ItsmRequest.java | 添加注解 |
| dss-scriptis-server-webank | ItsmRequest.java | 添加注解 |

### 7.2 测试影响
- 需要回归测试所有ITSM相关接口
- 需要验证向后兼容性

### 7.3 部署影响
- 无需数据库变更
- 无需配置变更
- 需要重启服务

## 八、附录

### 8.1 参考资料
- Jackson注解文档：https://fasterxml.github.io/jackson-annotations/javadoc/2.9/com/fasterxml/jackson/annotation/JsonIgnoreProperties.html

### 8.2 相关接口
| 接口名称 | 接口路径 | 说明 |
|---------|---------|------|
| 更新工作空间 | /dss/framework/workspace/updateWorkspace | POST，ITSM调用 |
| 添加工作流白名单 | /dss/framework/orchestrator/addOrchestratorWhite | POST，ITSM调用 |
| 添加代理用户 | /dss/scriptis/proxy/addUserProxy | POST，ITSM调用 |

### 8.3 预估工时
| 任务 | 工时（人天） |
|-----|------------|
| ItsmRequest类添加注解 | 0.1 |
| 回归测试 | 0.5 |
| 文档更新 | 0.2 |
| **合计** | **0.8** |

---

**文档版本**：v2.0
**创建日期**：2026-04-27
**更新日期**：2026-04-27
**需求负责人**：待定
**优先级**：P1（当前迭代完成）

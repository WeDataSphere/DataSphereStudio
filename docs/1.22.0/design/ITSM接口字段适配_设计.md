# ITSM接口字段适配_设计

## 一、设计概述

### 1.1 设计目标
适配ITSM系统版本升级后的请求报文，确保：
- 接口能够正常接收并解析包含新字段的请求报文
- 对于未知字段采用静默忽略策略，不抛出异常
- 保持向后兼容性，不影响现有功能正常运行

### 1.2 设计范围
- **dss-framework-workspace-server**：ItsmRequest类添加Jackson注解
- **dss-scriptis-server-webank**：ItsmRequest类添加Jackson注解

### 1.3 影响接口
| 接口 | 路径 | 说明 |
|-----|------|------|
| updateWorkspace | /dss/framework/workspace/updateWorkspace | 工作空间新建/修改 |
| addProjectAndOrchestratorWhite | /dss/framework/orchestrator/addOrchestratorWhite | 添加工作流白名单 |
| addUserProxy | /dss/scriptis/proxy/addUserProxy | 添加代理用户 |

## 二、现状分析

### 2.1 ItsmRequest类现状

系统中存在两处`ItsmRequest`定义：

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

### 2.2 反序列化方式
Spring MVC 使用 Jackson 进行 JSON 反序列化，默认情况下遇到未知字段会抛出 `UnrecognizedPropertyException` 异常。

### 2.3 问题分析
- Jackson 默认不允许未知字段，会导致反序列化失败
- 接口调用会因为未知字段而报错

## 三、详细设计

### 3.1 设计方案

采用 **Jackson 注解方案**，在 `ItsmRequest` 类上添加 `@JsonIgnoreProperties(ignoreUnknown = true)` 注解。

#### 3.1.1 方案优势
- 实现简单，改动最小
- 无需新增文件
- 无需修改其他代码
- Spring 原生支持，无需额外配置

### 3.2 ItsmRequest类改造

#### 3.2.1 dss-framework-workspace-server模块

**文件**：`dss-framework/dss-framework-workspace-server/src/main/java/com/webank/wedatasphere/dss/framework/workspace/bean/itsm/ItsmRequest.java`

**修改内容**：
```java
package com.webank.wedatasphere.dss.framework.workspace.bean.itsm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ItsmRequest {
    // ... 现有字段保持不变 ...
}
```

#### 3.2.2 dss-scriptis-server-webank模块

**文件**：`dss-apps/dss-scriptis-server-webank/src/main/java/com/webank/wedatasphere/dss/scriptis/bean/ItsmRequest.java`

**修改内容**：
```java
package com.webank.wedatasphere.dss.scriptis.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** itsm请求我们的入参
 * Author: xlinliu
 * Date: 2024/5/22
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItsmRequest {
    // ... 现有字段保持不变 ...
}
```

### 3.3 注解说明

`@JsonIgnoreProperties(ignoreUnknown = true)` 注解作用：
- 告诉 Jackson 在反序列化时忽略 JSON 中存在但 Java 类中不存在的属性
- 不会抛出 `UnrecognizedPropertyException` 异常
- 静默忽略未知字段，不影响业务逻辑

## 四、数据模型变更

### 4.1 ItsmRequest类变更

#### 变更前
```java
public class ItsmRequest {
    private String createDate;
    private String createUser;
    // ... 其他字段 ...
}
```

#### 变更后
```java
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ItsmRequest {
    private String createDate;
    private String createUser;
    // ... 其他字段 ...
}
```

### 4.2 无数据库变更
- 本需求不涉及数据库表结构变更
- 无需执行DDL脚本

## 五、测试设计

### 5.1 单元测试

#### 5.1.1 测试用例1：正常请求（无未知字段）
```java
@Test
public void testDeserializeWithKnownFields() throws Exception {
    String json = "{\"createUser\":\"admin\",\"data\":\"{\\\"dataList\\\":[]}\",\"externalId\":\"123\"}";
    ObjectMapper mapper = new ObjectMapper();
    ItsmRequest request = mapper.readValue(json, ItsmRequest.class);

    assertEquals("admin", request.getCreateUser());
    assertEquals("123", request.getExternalId());
}
```

#### 5.1.2 测试用例2：包含未知字段
```java
@Test
public void testDeserializeWithUnknownFields() throws Exception {
    String json = "{\"createUser\":\"admin\",\"newField\":\"newValue\",\"externalId\":\"123\"}";
    ObjectMapper mapper = new ObjectMapper();
    ItsmRequest request = mapper.readValue(json, ItsmRequest.class);

    // 正常解析，不抛出异常
    assertEquals("admin", request.getCreateUser());
    assertEquals("123", request.getExternalId());
}
```

#### 5.1.3 测试用例3：向后兼容性
```java
@Test
public void testBackwardCompatibility() throws Exception {
    // 旧版请求格式
    String oldJson = "{\"createUser\":\"admin\",\"data\":\"{\\\"dataList\\\":[]}\"}";
    ObjectMapper mapper = new ObjectMapper();
    ItsmRequest request = mapper.readValue(oldJson, ItsmRequest.class);

    // 正常解析，无异常
    assertNotNull(request);
    assertEquals("admin", request.getCreateUser());
}
```

### 5.2 集成测试

| 测试场景 | 输入 | 预期结果 |
|---------|------|---------|
| 工作空间更新-含新字段 | ITSM请求含新字段 | 正常处理，无异常 |
| 工作空间更新-无新字段 | ITSM请求标准格式 | 正常处理 |
| 白名单添加-含新字段 | ITSM请求含新字段 | 正常处理，无异常 |
| 代理用户添加-含新字段 | ITSM请求含新字段 | 正常处理，无异常 |

## 六、部署方案

### 6.1 部署步骤
1. 编译打包：`mvn clean package -DskipTests`
2. 停止服务：`sh $DSS_HOME/sbin/dss-stop-all.sh`
3. 替换jar包
4. 启动服务：`sh $DSS_HOME/sbin/dss-start-all.sh`

### 6.2 回滚方案
- 保留原jar包备份
- 如有问题，回滚至原版本即可

### 6.3 配置变更
无需配置变更

## 七、风险评估

### 7.1 风险项
| 风险项 | 影响 | 概率 | 应对措施 |
|-------|------|------|---------|
| Jackson注解不生效 | 高 | 低 | Spring默认使用Jackson，注解生效有保障 |

### 7.2 兼容性
- **向后兼容**：完全兼容旧版ITSM请求格式
- **向前兼容**：支持未来新增字段

## 八、附录

### 8.1 相关文件清单
| 模块 | 文件 | 变更类型 |
|-----|------|---------|
| dss-framework-workspace-server | ItsmRequest.java | 添加注解 |
| dss-scriptis-server-webank | ItsmRequest.java | 添加注解 |

### 8.2 预估工时
| 任务 | 工时（人天） |
|-----|------------|
| ItsmRequest类添加注解 | 0.1 |
| 单元测试 | 0.2 |
| 集成测试 | 0.3 |
| 文档更新 | 0.2 |
| **合计** | **0.8** |

---

**文档版本**：v2.0
**创建日期**：2026-04-27
**更新日期**：2026-04-27
**设计负责人**：待定
**需求关联**：[ITSM接口字段适配_需求](../requirements/ITSM接口字段适配_需求.md)

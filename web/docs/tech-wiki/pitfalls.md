---
title: 踩坑与排查指南
type: pitfall
created: 2026-06-09
tags: [踩坑, 排查, 问题定位]
---

# 踩坑与排查指南

## 常见问题

### 1. npm install 失败

**触发条件**：执行 `npm install` 时报错

**现象**：
- 依赖下载超时
- node-gyp 编译失败
- 权限错误

**根因**：
- 网络问题
- Node 版本不兼容
- 缓存损坏

**排查步骤**：
1. 检查 Node 版本（推荐 14.x-16.x）
2. 清除 npm 缓存：`npm cache clean --force`
3. 删除 node_modules 和 package-lock.json
4. 使用国内镜像：`npm config set registry https://registry.npmmirror.com`

**修复方案**：
```bash
# 清理并重新安装
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

---

### 2. 子包依赖未安装

**触发条件**：Monorepo 子包代码报错找不到模块

**现象**：
```
Module not found: Can't resolve '@dataspherestudio/shared'
```

**根因**：npm workspaces 未正确链接子包

**排查步骤**：
1. 检查根目录 package.json 的 workspaces 配置
2. 检查 node_modules/@dataspherestudio 目录是否存在软链接

**修复方案**：
```bash
# 重新安装
npm install

# 或手动链接
cd packages/shared && npm link
cd packages/dss && npm link @dataspherestudio/shared
```

---

### 3. 热更新不生效

**触发条件**：修改代码后浏览器不刷新

**现象**：
- 代码修改后页面不更新
- 控制台报 WebSocket 错误

**根因**：
- 开发服务器端口被占用
- 浏览器缓存

**排查步骤**：
1. 检查控制台是否有 WebSocket 连接错误
2. 检查是否有多个开发服务器运行

**修复方案**：
```bash
# 杀掉占用端口的进程
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Mac/Linux
lsof -i :8080
kill -9 <PID>

# 清除浏览器缓存
# Chrome: Ctrl+Shift+Delete
```

---

### 4. ESLint 报错阻止提交

**触发条件**：git commit 时 ESLint 检查失败

**现象**：
```
ERROR: Expected indentation of 2 spaces but found 4
```

**根因**：代码风格不符合 ESLint 规则

**修复方案**：
```bash
# 自动修复
npm run fix

# 或手动修复后重新提交
git add .
git commit -m "message"
```

---

### 5. 路由跳转 404

**触发条件**：访问路由页面显示 404

**现象**：
- 页面显示 404
- 控制台无报错

**根因**：
- 路由配置错误
- 权限不足

**排查步骤**：
1. 检查 `router.js` 是否配置了该路由
2. 检查路由 `meta.publicPage` 或 `meta.admin` 配置
3. 检查 storage 中的用户信息

**修复方案**：
```javascript
// 确保路由配置正确
{
  path: '/workflow',
  name: 'Workflow',
  meta: {
    title: 'My Workflow',
    publicPage: true  // 公开页面
  },
  component: () => import('./view/workflow/index.vue')
}
```

---

### 6. API 请求 401 未授权

**触发条件**：API 请求返回 401

**现象**：
```
Request failed with status code 401
```

**根因**：
- Token 过期
- 未登录

**排查步骤**：
1. 检查 storage 中的 token
2. 检查 cookie 是否有效

**修复方案**：
```javascript
// api.js 已处理 401，会自动跳转登录
// 如需手动处理：
storage.remove('token', 'cookie')
window.location.href = '/#/login'
```

---

### 7. WebSocket 连接失败

**触发条件**：实时任务状态不更新

**现象**：
- 日志不刷新
- 任务状态不更新

**根因**：
- WebSocket 服务未启动
- 网络问题

**排查步骤**：
1. 检查控制台 WebSocket 错误
2. 检查 `process.env.VUE_APP_MN_CONFIG_SOCKET` 配置

**修复方案**：
```javascript
// WebSocket 会自动降级为 HTTP
// 检查降级逻辑是否正常
socket.on('inconnect', () => {
  console.log('WebSocket 降级为 HTTP')
})
```

---

### 8. 构建产物体积过大

**触发条件**：`npm run build` 产物超过预期

**现象**：
- 构建时间过长
- 产物体积过大（>5MB）

**根因**：
- 未使用动态导入
- 依赖未拆分

**修复方案**：
```javascript
// 使用动态导入
component: () => import('./view/workflow/index.vue')

// 使用按需打包
npm run build --module=scriptis --micro_module=scriptis
```

---

### 9. Monaco Editor 加载失败

**触发条件**：代码编辑器不显示

**现象**：
- 编辑器区域空白
- 控制台报 Worker 错误

**根因**：
- Monaco Worker 配置错误
- Webpack 插件未正确配置

**修复方案**：
```javascript
// 检查 vue.config.js
const MonacoWebpackPlugin = require('monaco-editor-webpack-plugin')

module.exports = {
  configureWebpack: {
    plugins: [
      new MonacoWebpackPlugin({
        languages: ['javascript', 'typescript', 'sql']
      })
    ]
  }
}
```

---

### 10. 国际化文本不显示

**触发条件**：页面显示 key 而非翻译文本

**现象**：
```
message.workflow.title
```

**根因**：
- i18n 资源未加载
- key 路径错误

**排查步骤**：
1. 检查 `shared/common/i18n/zh.json` 是否有该 key
2. 检查 `$t()` 中的 key 路径

**修复方案**：
```javascript
// 确保资源文件中有对应 key
// shared/common/i18n/zh.json
{
  "message": {
    "workflow": {
      "title": "工作流标题"
    }
  }
}

// 使用正确的 key
this.$t('message.workflow.title')
```

---

## 性能问题

### 大数据量渲染卡顿

**现象**：表格渲染 1000+ 行数据时卡顿

**修复方案**：
```vue
<!-- 使用虚拟列表 -->
<virtual-list :size="40" :remain="20">
  <div v-for="item in largeList" :key="item.id">
    {{ item.name }}
  </div>
</virtual-list>
```

### 内存泄漏

**现象**：页面长时间运行后变慢

**修复方案**：
```javascript
// 组件销毁时清理事件监听
beforeDestroy() {
  eventbus.off('event:name', this.handler)
  clearTimeout(this.timer)
}
```

---

## 环境问题

### Node 版本不兼容

**现象**：`npm install` 报 node-gyp 错误

**修复方案**：
```bash
# 切换到兼容版本
nvm use 14

# 或使用 nvm-windows
nvm install 14
nvm use 14
```

### 端口被占用

**现象**：`npm run serve` 报端口占用

**修复方案**：
```bash
# 使用其他端口
npm run serve -- --port 8081
```

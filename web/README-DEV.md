### 1.1.0版本说明

- 前端工程管理方面达到目录结构清晰，方便维护及迭代开发
- 可选模块构建，产出不同用户的应用
- 插件开发支持

### 项目结构

```
├─dist              # 构建后静态资源
├─docs              # 文档
├─node_modules
└─packages          # 各应用模块
    ├─apiServices   # 数据服务
    ├─dataGovernance    # 数据治理（开源提供的行内没有使用）
    ├─dataService       # 数据服务（开源提供的行内没有使用）
    ├─dolphinScheduler  # dolphinScheduler（开源提供的行内没有使用）
    ├─dss            # DSS主应用  
    ├─editor         # 编辑器
    ├─editorLsp      # 编辑器LSP
    ├─exts           # 扩展模块
    ├─scheduleCenter # 生产中心
    ├─scriptis       # 脚本编辑，脚本管理，数据库表，函数管理，脚本执行
    ├─shared         # 共享模块 公共组件方法
    ├─workflows      # 工作流编辑/管理/执行/发布
    └─workspace      # 工作空间管理

```

### 建议/约束

新增功能模块先确定涉及应用，按照上面目录结构维护代码同时建议遵守以下约束：

- 子应用可以配置自己的layout需要在应用router模块导出配置subAppRoutes
- 子应用支持使用自己的header，需要在config.json里配置模块路径
- 各应用需要使用iView作为UI库，并提供路由，国际化等配置写入config.json
- 可复用组件，资源需要合理放置，packages/shared 共享组件方法，修改需要注意影响
- 各应用路由应以应用名做统一前缀
- 各应用之间需要事件通信，应当在config.json 里声明对应module文件路径
- 新增功能模块需要按照现有目录约束建立文件，已有功能修改应在有限模块内进行，控制影响范围
- 全局共用组件、公共基础样式、工具方法修改需评估后才能修改，并且重点review
- 插件扩展开发，扩展点增加，修改需要讨论，注意兼容

### 如何新增一个子应用

1. config.json 新增应用配置
2. packages 下新建应用目录进行应用开发


### 前端开发、构建打包
packages目录下packages/apiServices、packages/scheduleCenter、packages/exts/webank为行内部分，其它为开源部分。
- 开发时通过npm run sync拉取指定开源部分代码（注意修改分支），禁止在行内部分修改开源部分代码。
- 注意网络环境，内网使用不了npm，外网也没法使用wnpm,，即行版本在内网下进行构建，开源版本需要在外网环境下

```
# 安装依赖
npm install

npm run serve
# 运行部分模块子应用，支持通过module配置, 子应用可以有独立的顶层路由配置layout，header，footer，可配置micro_module参数使用子应用的layout。如科管版本：
npm run serve --module=scriptis --micro_module=scriptis
# 打包DSS应用
npm run build
# 打包子应用，支持通过module组合
npm run build --module=scriptis
npm run build --module=apiServices,workspace --micro_module=apiServices
npm run build --module=scheduleCenter,workflows,workspace,scriptis --micro_module=scheduleCenter
npm run serve --module=scheduleCenter,workflows,workspace,scriptis --micro_module=scheduleCenter
```

### 其它

DSS已完成开源和行内特性拆分，后续开发开源为基础，行内特有功能通过插件形式开发，新增或修改需要明确改动部分是开源还是行内。
开源Github上的项目可自动同步至行内Gitlab仓库(git@git.weoa.com:pes/dataspherestudio-wedatasphere-sync.git)，本仓库除扩展目录外，其它packages目录来自同步的代码，通过sync实现，请查看package.json

https://docs.qq.com/sheet/DT1Jad2hUbFRwSVVX?tab=BB08J2&version=4.0.0.6007&platform=win
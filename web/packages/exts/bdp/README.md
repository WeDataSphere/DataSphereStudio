## 说明

此扩展包括以下功能，BDAP版本
- 生产中心项目操作历史、管理
- 运维用户设置代理用户

BDAP版本打包时config.json 需配置上对应扩展
```json

"dss-plugin-bdp": {
    "module": "exts/bdp/index.js",
    "i18n": {
      "en": "exts/bdp/i18n/en.json",
      "zh-CN": "exts/bdp/i18n/zh.json"
    },
    "options": null
  }
```
export default {
  apps: {
    workspace: {
      routes: 'workspace/router',
      module: 'workspace/module',
      i18n: {
        en: 'workspace/i18n/en.js',
        'zh-CN': 'workspace/i18n/zh.js',
      },
    },
    appconn: {
      routes: 'appconn/router',
      module: 'appconn/module',
    },
    accounts: {
      routes: 'accounts/router',
      module: 'accounts/module',
      i18n: {
        en: 'accounts/i18n/en.js',
        'zh-CN': 'accounts/i18n/zh.js',
      },
    },
  },
  exts: {},
  conf: {
    app_name: 'DataSphere Studio',
    // app_logo: 'src/assets/images/dssLogo.png',
    user_guide: '',
    // hide_view_tb_detail: true,
    // hide_view_db_detail: true,
    watermark: {
      show: false,
      template: '${username} ${time}',
      timeupdate: 60000,
    },
    // lsp_service: {
    //   sql: "${protocol}//${host}/server",
    //   py: "${protocol}//${host}/python",
    // },
  },
  version: '1.1.12',
};

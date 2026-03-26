import wa from '@webank/wa-sdk';

const config =  {
  uat: {
    waAppId: 'bdp_ide',
    subAppId: 'bdp_ide_uat',
    env: 'test',
  },
  sit: {
    waAppId: 'bdp_ide',
    subAppId: 'bdp_ide_sit',
    env: 'test',
  },
  release: {
    waAppId: 'bdp_ide',
    subAppId: 'bdp_ide',
    env: 'adm',
  },
};

/**
 *
 * @export
 */
export default function () {
  if (process.env.NODE_ENV == 'development') return
  const env = process.env.VUE_APP_ENV; // uat sit release
  const analysis = config[env];
  if (!analysis) return
  window.$Wa = wa;
  wa.init(analysis);
  window.addEventListener('load', function (event) {
    if (window.performance && window.performance.navigation.type === window.performance.navigation.TYPE_RELOAD) {
      // 页面被刷新
    } else {
      if (window.username) {
        window.$Wa.track('user_login', {
          userName: window.username,
          timestamp: Date.now()
        })
      }
    }
  });
  window.addEventListener('beforeunload', function (event) {
    if (window.performance && window.performance.navigation.type === window.performance.navigation.TYPE_RELOAD) {
      // 页面被刷新
    } else {
      // 页面正在关闭
      if (window.username) {
        window.$Wa.track('user_logout', {
          userName: window.username,
          timestamp: Date.now()
        })
      }
    }
  });
}
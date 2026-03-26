import { resolve } from 'path';
import vue from '@vitejs/plugin-vue';
import vueJsx from '@vitejs/plugin-vue-jsx';
import { ConfigEnv, defineConfig, loadEnv, UserConfig } from 'vite';
import host from './vite-plugin/vite-host';
import eslintPlugin from 'vite-plugin-eslint';
import virtualModule from './vite-plugin/vite-dynamic-module';
import monacoEditorPlugin from 'vite-plugin-monaco-editor';

// https://vitejs.dev/config/
export default defineConfig(({ command, mode }: ConfigEnv): UserConfig => {
  // 根据当前工作目录中的 `mode` 加载 .env 文件
  // 设置第三个参数为 '' 来加载所有环境变量，而不管是否有 `VITE_` 前缀。
  const env = loadEnv(mode, process.cwd(), '');
  const monacoEditorPluginInstance = (
    monacoEditorPlugin as Record<string, any>
  ).default({
    languageWorkers: ['editorWorkerService', 'typescript', 'json', 'html'],
  });
  console.log(env, command, mode);
  return {
    define: {},
    plugins: [
      host(),
      vue(),
      vueJsx(),
      virtualModule(),
      eslintPlugin(),
      monacoEditorPluginInstance,
    ],
    resolve: {
      alias: {
        '@': resolve(__dirname, './'),
        '@dataspherestudio': resolve(__dirname, './packages'),
      },
    },
    css: {
      preprocessorOptions: {
        less: {
          javascriptEnabled: true,
        },
      },
    },
    server: {
      https: false,
      proxy: {
        '/api': {
          // target: 'http://10.107.97.166:8088',
          // target: 'http://10.107.116.246:8088',
          // target: 'http://sit.dss.bdap.weoa.com',
          target: 'http://uat.dss.bdap.weoa.com/',
          secure: false,
          changeOrigin: true,
          followRedirects: true,
          headers: {
            // cookie: '',
            // cookie:
            // 'linkis_user_session_ticket_id_v1=FmzyJmP2DGUzEo5Hy/D0OqyinoWZ4+L52vOV55MqIl4=; workspaceId=104; workspaceName=bdapWorkspace_move',
            // TokenCode: 'admin-kmsnd',
            // TokenUser: 'stacyyan',
            // TokenAlive: 'true',
          },
        },
      },
    },
    base: './',
    build: { target: 'chrome66' },
  };
});

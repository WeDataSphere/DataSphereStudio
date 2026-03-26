const routes = [
  {
    path: '/appconn',
    name: 'appconn',
    meta: {
      title: 'AppConn',
      publicPage: true,
    },
    component: () => import('./view/layout.vue'),
    redirect: '/appconn/manage',
    children: [
      {
        path: 'manage',
        name: 'manage',
        meta: {
          title: 'AppConn管理台',
          publicPage: true,
        },
        component: () => import('./module/manage/index.vue'),
      },
      {
        path: 'instance',
        name: 'instance',
        meta: {
          title: '实例管理',
          publicPage: true,
        },
        component: () => import('./module/instance/index.jsx'),
      },
      {
        path: 'queryNode',
        name: 'queryNode',
        meta: {
          title: '节点管理',
          publicPage: true,
        },
        component: () => import('./module/queryNode/index.jsx'),
      },
      {
        path: 'addNode',
        name: '新增节点',
        meta: {
          title: '新增节点',
          publicPage: true,
        },
        component: () => import('./module/addNode/index.vue'),
      },
      {
        path: 'editNode',
        name: '编辑节点',
        meta: {
          title: '编辑节点',
          publicPage: true,
        },
        component: () => import('./module/editNode/index.vue'),
      },
    ],
  },
];

export default routes;

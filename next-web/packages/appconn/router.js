const routes = [
    {
      path: '/appconn',
      name: 'workspace',
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
            title: 'AppConn Manage',
            publicPage: true,
          },
          component: () => import('./module/manage/index.vue'),
        }
      ],
    },
  ];
  
  export default routes;
  
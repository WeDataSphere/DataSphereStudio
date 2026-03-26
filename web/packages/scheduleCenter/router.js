// export const subAppRoutes = {
//   path: '',
//   name: 'layout',
//   component: () => import('./view/layout.vue'),
//   redirect: '/workspaceHome',
//   meta: {
//     publicPage: true, // 权限公开
//   },
//   children: []
// }

const routes = [
  {
    path: '/scheduleCenter',
    name: 'ScheduleCenter',
    meta: {
      publicPage: true
    },
    component: () =>
      import('./view/scheduleCenter/index.vue'),
  },
  {
    path: '/scheduleCenterDetail',
    name: 'ScheduleCenterDetail',
    meta: {
      publicPage: true
    },
    component: () =>
      import('./view/scheduleCenterDetail/index.vue'),
  }
]

export default routes;

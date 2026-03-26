import { getUrlParams } from '@fesjs/traction-widget';

const getRedirectPath = () => {
  const { workspaceId, timestamp = '' } = getUrlParams() || {};
  const { path, key = '' } = JSON.parse(
    sessionStorage.getItem('meta_workflow_current_path') || '{}'
  );
  if (top !== self && timestamp === key && path) {
    return `${path}?workspaceId=${workspaceId}`;
  }
  return `/accounts/project?workspaceId=${workspaceId}`;
};
const routes = [
  {
    path: '/accounts',
    name: 'accounts',
    meta: {
      title: 'Accounts',
      publicPage: true,
    },
    component: () => import('./view/layout.vue'),
    redirect: getRedirectPath(),
    children: [
      {
        path: 'workflow',
        name: 'workflowAccount',
        meta: {
          title: 'workflowAccount',
          publicPage: true,
        },
        component: () => import('./module/workflow/index.vue'),
      },
      {
        path: 'project',
        name: 'project',
        meta: {
          title: 'project',
          publicPage: true,
        },
        component: () => import('./module/project/index.vue'),
      },
      {
        path: 'dataVisualization',
        name: 'dataVisualization',
        meta: {
          title: 'dataVisualization',
          publicPage: true,
        },
        component: () => import('./module/dataVisualization/index.vue'),
      },
      {
        path: 'dataDev',
        name: 'dataDev',
        meta: {
          title: 'dataDev',
          publicPage: true,
        },
        component: () => import('./module/dataDev/index.vue'),
      },
      {
        path: 'signal',
        name: 'signal',
        meta: {
          title: 'signal',
          publicPage: true,
        },
        component: () => import('./module/signal/layout.vue'),
        redirect: '/accounts/signal/datachecker',
        children: [
          {
            path: ':type',
            name: 'signalNode',
            meta: {
              title: 'signalNode',
              publicPage: true,
            },
            component: () => import('./module/signal/index.vue'),
          },
        ],
      },
    ],
  },
];

export default routes;

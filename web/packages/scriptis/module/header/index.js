export default {
  name: 'scriptisHeader',
  component: () => import('./index.vue'),
  dispatchs: ['Workbench:switchAwayFromAiTab'],
};
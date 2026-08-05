// Create a disposable temp orchestrator for the DAG validation tests; save its flowId.
const { api } = require('./http');
const fs = require('fs');
const path = require('path');

const TS = Date.now();
const NAME = 'dag_api_test_' + TS;

(async () => {
  console.log('=== createOrchestrator name=' + NAME + ' (proj=dss_test_2021223, ws=bdapWorkspace/224) ===');
  const body = {
    workspaceId: 224,
    projectId: 1237,
    projectName: 'dss_test_2021223',
    workspaceName: 'bdapWorkspace',
    orchestratorName: NAME,
    orchestratorWays: ['01'],
    orchestratorMode: 'pom_work_flow',
    orchestratorLevel: '重要级别-日常',
    dssLabels: ['dev'],
    uses: 'DAG校验API测试临时工作流',
    description: 'DAG校验API测试临时工作流，测完即删',
    isDefaultReference: '0',
    labels: { route: 'dev' }
  };
  const c = await api('POST', '/dss/framework/orchestrator/createOrchestrator', { body });
  console.log('HTTP', c.status);
  console.log('BODY:', c.text.slice(0, 600));
  let orchestratorId = c.json && c.json.data && c.json.data.orchestratorId;
  if (!orchestratorId) { console.log('FAILED to create orchestrator'); return; }
  console.log('orchestratorId:', orchestratorId);

  // get flowId (appId) via versions
  const v = await api('POST', '/dss/framework/orchestrator/getVersionByOrchestratorId', { body: { orchestratorId } });
  const list = v.json && v.json.data && v.json.data.list;
  let flowId = null;
  if (Array.isArray(list) && list[0]) flowId = list[0].appId;
  console.log('versions list:', JSON.stringify(list && list[0]));
  console.log('flowId(appId):', flowId);
  if (!flowId) { console.log('FAILED to resolve flowId'); return; }

  // read the freshly created flow json (template)
  const g = await api('GET', '/dss/workflow/get', { query: { flowId, isNotHaveLock: true } });
  let template = null, projectName = 'dss_test_2021223', workspaceName = 'bdapWorkspace';
  if (g.json && g.json.data && g.json.data.flow) {
    const flow = g.json.data.flow;
    template = flow.flowJson;
    projectName = flow.projectName || projectName;
    workspaceName = flow.workspaceName || workspaceName;
    console.log('created flow: id=' + flow.id, 'name=' + flow.name, 'rootFlow=' + flow.rootFlow,
      'nodes=' + (template ? JSON.parse(template).nodes.length : '?'));
    fs.writeFileSync(path.join(__dirname, 'temp_flow_template.json'), template || '{}');
  } else {
    console.log('getFlow RAW:', g.text.slice(0, 400));
  }

  fs.writeFileSync(path.join(__dirname, 'temp_context.json'), JSON.stringify({
    orchestratorId, flowId, name: NAME, projectName, workspaceName, createdAt: TS, template: !!template
  }, null, 2));
  console.log('wrote temp_context.json');
})();

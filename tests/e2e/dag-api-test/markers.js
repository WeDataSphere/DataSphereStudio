// Deployment success markers (gate). Run before full suite.
// Marker1: saveFlow with forceSave:false must NOT be bare-Spring-400 (controller deployed).
// Marker2: cycle jsonFlow -> data.status=VALIDATION_FAILED + ruleId CYCLE.
const { api } = require('./http');
const fs = require('fs');
const path = require('path');

const FLOW_ID = Number(JSON.parse(fs.readFileSync(path.join(__dirname, 'temp_context.json'), 'utf8')).flowId);
const PROJECT_NAME = 'dss_test_2021223';
const WORKSPACE_NAME = 'bdapWorkspace';
const LABELS = { route: 'dev' };

const baseTpl = JSON.parse(fs.readFileSync(path.join(__dirname, 'temp_flow_template.json'), 'utf8'));
function flowWith(nodes, edges) {
  return JSON.stringify(Object.assign({}, baseTpl, { nodes, edges }));
}
function node(key, title) {
  return {
    id: key, key, title, jobType: 'spark.sql',
    desc: '', layout: { x: 100, y: 100, width: 150, height: 40 },
    params: { variable: {}, configuration: { runtime: {}, startup: {}, special: {} } },
    resources: [], dependencys: [], jobContent: {}
  };
}
function edge(s, t) {
  return { source: s, target: t, linkType: 'straight', sourceLocation: 'bottom', targetLocation: 'top' };
}

async function saveFlow(json, forceSave, label) {
  const body = { id: FLOW_ID, json, workspaceName: WORKSPACE_NAME, projectName: PROJECT_NAME, labels: LABELS, forceSave };
  const r = await api('POST', '/dss/workflow/saveFlow', { body });
  const status = r.json && r.json.data && r.json.data.status;
  const issues = (r.json && r.json.data && r.json.data.validationIssues) || [];
  const ruleIds = issues.map(i => i.ruleId + (i.subType ? '/' + i.subType : '') + '(' + i.level + ')');
  console.log(`\n[${label}] forceSave=${forceSave} HTTP=${r.status} status_code=${r.json && r.json.status} message="${r.json && r.json.message}"`);
  console.log(`  data.status=${status || '(none)'}  flowVersion=${r.json && r.json.data && r.json.data.flowVersion}`);
  console.log(`  ruleIds=${JSON.stringify(ruleIds)}`);
  issues.forEach(i => console.log('    - ' + i.ruleId + '/' + (i.subType || '') + ' [' + i.level + '] ' + i.message));
  return { httpStatus: r.status, bizStatus: r.json && r.json.status, dataStatus: status, ruleIds, flowVersion: r.json && r.json.data && r.json.data.flowVersion, message: r.json && r.json.message, raw: r.text };
}

(async () => {
  console.log('flowId =', FLOW_ID);

  // ---- Marker 1: body with forceSave, valid simple flow (A->B), forceSave:false ----
  const validFlow = flowWith([node('A', 'n_a'), node('B', 'n_b')], [edge('A', 'B')]);
  const m1 = await saveFlow(validFlow, false, 'MARKER1 forceSave field accepted?');
  // Bare Spring 400 = Spring framework JSON parse error (FAIL_ON_UNKNOWN_PROPERTIES).
  // Detect: HTTP 400 AND body looks like {"timestamp":...,"status":400,"error":"Bad Request"} OR no dss method field.
  const isBareSpring400 = (m1.httpStatus === 400 && !(m1.bizStatus === 0 || m1.bizStatus === 1) && (!m1.message || m1.message.toLowerCase().includes('json') || (m1.raw.includes('"error":"Bad Request"'))));
  const marker1Pass = !isBareSpring400 && m1.httpStatus !== 400;
  console.log(`\n>>> MARKER1 (forceSave not bare-400): ${marker1Pass ? 'PASS' : 'FAIL'} (httpStatus=${m1.httpStatus} bareSpring400=${isBareSpring400})`);

  if (!marker1Pass) {
    console.log('\n### MARKER1 FAILED -> deployment NOT effective. STOP. ###');
    console.log('RAW:', m1.raw.slice(0, 500));
    fs.writeFileSync(path.join(__dirname, 'markers_result.json'), JSON.stringify({ marker1Pass, marker2Pass: null, m1 }, null, 2));
    return;
  }

  // ---- Marker 2: cycle A->B->C->A ----
  const cycle = flowWith([node('A', 'n_a'), node('B', 'n_b'), node('C', 'n_c')], [edge('A', 'B'), edge('B', 'C'), edge('C', 'A')]);
  const m2 = await saveFlow(cycle, false, 'MARKER2 cycle -> VALIDATION_FAILED+CYCLE?');
  const marker2Pass = (m2.dataStatus === 'VALIDATION_FAILED') && m2.ruleIds.some(x => x.startsWith('CYCLE'));
  console.log(`\n>>> MARKER2 (cycle -> VALIDATION_FAILED+CYCLE): ${marker2Pass ? 'PASS' : 'FAIL'}`);

  fs.writeFileSync(path.join(__dirname, 'markers_result.json'), JSON.stringify({ marker1Pass, marker2Pass, m1, m2 }, null, 2));
  console.log('\n=== GATE:', (marker1Pass && marker2Pass) ? 'BOTH PASS -> run full suite' : 'NOT MET -> stop', '===');
})();

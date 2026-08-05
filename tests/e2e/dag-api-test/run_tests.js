// DAG structure validation API tests (stage 7). Target = disposable temp flow (temp_context.json).
//
// v2.3 scope (this run):
//   - 检查项④「开始结束结构」(孤岛/多起点/断链 warn) 已移除 -> 校验收敛为 3 项 error。
//   - Case1 CYCLE / Case2 EDGE_REF / Case3 DUPLICATE_NAME (error, must reject)
//   - Case3 额外验 v2.3 修复点: issue.nodeId == 节点身份(key/id), 不再是 name 字符串
//   - Case4 (v2.3 核心修复): 孤岛节点 -> 应正常保存成功(非 PENDING_CONFIRM / 非 VALIDATION_FAILED)
//   - Case5: body 带 forceSave 字段 -> 后端兼容, 不应 400
//   - 后端不再返回 PENDING_CONFIRM；前端不再发 forceSave、不再处理 PENDING_CONFIRM。
const { api } = require('./http');
const fs = require('fs');
const path = require('path');

const FLOW_ID = Number(JSON.parse(fs.readFileSync(path.join(__dirname, 'temp_context.json'), 'utf8')).flowId);
const PROJECT_NAME = 'dss_test_2021223';
const WORKSPACE_NAME = 'bdapWorkspace';
const LABELS = { route: 'dev' };

// Base jsonFlow keys from the temp flow's own template (preserves contextID for a valid save).
const baseTpl = JSON.parse(fs.readFileSync(path.join(__dirname, 'temp_flow_template.json'), 'utf8'));
function flowWith(nodes, edges) {
  return JSON.stringify(Object.assign({}, baseTpl, { nodes, edges }));
}

// Minimal valid node. identity = key (priority); name = title.
function node(key, title, jobType) {
  return {
    id: key, key, title, jobType: jobType || 'spark.sql',
    desc: '', layout: { x: 100, y: 100, width: 150, height: 40 },
    params: { variable: {}, configuration: { runtime: {}, startup: {}, special: {} } },
    resources: [], dependencys: [], jobContent: {}
  };
}
function edge(s, t) {
  return { source: s, target: t, linkType: 'straight', sourceLocation: 'bottom', targetLocation: 'top' };
}

const results = [];

async function saveFlow(json, forceSave, label) {
  const body = {
    id: FLOW_ID, json, workspaceName: WORKSPACE_NAME,
    projectName: PROJECT_NAME, labels: LABELS, forceSave
  };
  const r = await api('POST', '/dss/workflow/saveFlow', { body });
  const status = r.json && r.json.data && r.json.data.status;
  const issues = (r.json && r.json.data && r.json.data.validationIssues) || [];
  const ruleIds = issues.map(i => i.ruleId + (i.subType ? '/' + i.subType : '') + '(' + i.level + ')');
  console.log(`\n[${label}] forceSave=${forceSave} HTTP=${r.status} message="${r.json && r.json.message}"`);
  console.log(`  data.status=${status || '(none)'}  flowVersion=${r.json && r.json.data && r.json.data.flowVersion}`);
  console.log(`  validationIssues ruleIds=${JSON.stringify(ruleIds)}`);
  issues.forEach(i => console.log('    - ' + i.ruleId + '/' + (i.subType||'') + ' [' + i.level + '] nodeId=' + (i.nodeId||'') + ' :: ' + i.message));
  return { status: r.status, httpStatus: r.status, dataStatus: status, ruleIds, issues,
    flowVersion: r.json && r.json.data && r.json.data.flowVersion,
    message: r.json && r.json.message, raw: r.text };
}

function check(label, cond, detail) {
  console.log(`  => ${cond ? 'PASS' : 'FAIL'}: ${label}${cond ? '' : ' :: ' + detail}`);
  return cond;
}

(async () => {
  // ---------- Case 1: cycle A->B->C->A ----------
  const c1 = flowWith(
    [node('A', 'n_a'), node('B', 'n_b'), node('C', 'n_c')],
    [edge('A', 'B'), edge('B', 'C'), edge('C', 'A')]
  );
  const r1 = await saveFlow(c1, false, 'Case1 CYCLE');
  const p1 = check('status==VALIDATION_FAILED', r1.dataStatus === 'VALIDATION_FAILED', 'got ' + r1.dataStatus)
    && check('issues contain CYCLE', r1.ruleIds.some(x => x.startsWith('CYCLE')), JSON.stringify(r1.ruleIds));
  results.push({ case: 'Case1 CYCLE', pass: p1, ...r1 });

  // ---------- Case 2: dangling edge A->X ----------
  const c2 = flowWith([node('A', 'n_a')], [edge('A', 'X')]);
  const r2 = await saveFlow(c2, false, 'Case2 EDGE_REF');
  const p2 = check('status==VALIDATION_FAILED', r2.dataStatus === 'VALIDATION_FAILED', 'got ' + r2.dataStatus)
    && check('issues contain EDGE_REF', r2.ruleIds.some(x => x.startsWith('EDGE_REF')), JSON.stringify(r2.ruleIds));
  results.push({ case: 'Case2 EDGE_REF', pass: p2, ...r2 });

  // ---------- Case 3: duplicate name (title 同) + v2.3 nodeId 修复点 ----------
  const dupTitle = 'same_name';
  const c3 = flowWith(
    [node('A', dupTitle), node('B', dupTitle)],
    [edge('A', 'B')]
  );
  const r3 = await saveFlow(c3, false, 'Case3 DUPLICATE_NAME');
  // 提取 DUPLICATE_NAME issue 的 nodeId
  const dupIssues = r3.issues.filter(i => ('' + i.ruleId).startsWith('DUPLICATE_NAME'));
  const dupNodeIds = dupIssues.map(i => i.nodeId);
  const nodeIdIsIdentity = dupNodeIds.length > 0 && dupNodeIds.every(nid => nid !== dupTitle && (nid === 'A' || nid === 'B'));
  const p3 = check('status==VALIDATION_FAILED', r3.dataStatus === 'VALIDATION_FAILED', 'got ' + r3.dataStatus)
    && check('issues contain DUPLICATE_NAME', r3.ruleIds.some(x => x.startsWith('DUPLICATE_NAME')), JSON.stringify(r3.ruleIds))
    && check('nodeId 是节点身份(key/id), 不是 name 字符串', nodeIdIsIdentity, 'dupNodeIds=' + JSON.stringify(dupNodeIds));
  results.push({ case: 'Case3 DUPLICATE_NAME (nodeId=identity)', pass: p3, dupNodeIds, ...r3 });

  // ---------- Case 4 (v2.3 核心修复): 孤岛节点(无入边无出边) -> 应正常保存成功 ----------
  const c4 = flowWith([node('ISO', 'isolated_node')], []);
  const r4 = await saveFlow(c4, false, 'Case4 ISOLATED node (v2.3: ④ removed -> save OK)');
  const notPending = r4.dataStatus !== 'PENDING_CONFIRM';
  const notFailed = r4.dataStatus !== 'VALIDATION_FAILED';
  const saved = !!r4.flowVersion || /OK|0/i.test(r4.dataStatus || '');
  const p4 = check('NOT PENDING_CONFIRM (④ removed)', notPending, 'got ' + r4.dataStatus)
    && check('NOT VALIDATION_FAILED', notFailed, 'got ' + r4.dataStatus)
    && check('save succeeded (flowVersion/OK)', saved, 'status=' + r4.dataStatus + ' msg=' + r4.message);
  results.push({ case: 'Case4 IsolatedNode save OK (v2.3 fix)', pass: p4, ...r4 });

  // ---------- Case 5: forceSave 字段兼容(后端保留) -> 不应 400 ----------
  const r5 = await saveFlow(c4, true, 'Case5 forceSave=true (field compat, expect no 400)');
  const p5 = check('HTTP != 400 (forceSave field accepted)', r5.status !== 400, 'HTTP=' + r5.status)
    && check('not a server error (2xx)', r5.status >= 200 && r5.status < 300, 'HTTP=' + r5.status);
  results.push({ case: 'Case5 forceSave field compat', pass: p5, ...r5 });

  // ---------- summary ----------
  console.log('\n========== SUMMARY ==========');
  let allPass = true;
  results.forEach(r => { console.log((r.pass ? 'PASS ' : 'FAIL ') + r.case); if (!r.pass) allPass = false; });
  console.log('OVERALL: ' + (allPass ? 'ALL PASS' : 'SOME FAIL'));
  fs.writeFileSync(path.join(__dirname, 'results.json'), JSON.stringify(results, null, 2));
})();

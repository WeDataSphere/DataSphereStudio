// Cleanup: delete the temp orchestrator created for testing (reads temp_context.json).
const { api } = require('./http');
const fs = require('fs');
const path = require('path');

(async () => {
  let ctx;
  try { ctx = JSON.parse(fs.readFileSync(path.join(__dirname, 'temp_context.json'), 'utf8')); }
  catch (e) { console.log('no temp_context.json, nothing to clean'); return; }
  const oid = ctx.orchestratorId;
  const fid = ctx.flowId;
  console.log('=== deleteOrchestrator id=' + oid + ' projectId=1237 workspaceId=224 (flowId=' + fid + ') ===');
  const r = await api('POST', '/dss/framework/orchestrator/deleteOrchestrator', {
    body: { id: oid, projectId: 1237, workspaceId: 224 }
  });
  console.log('HTTP', r.status, 'BODY:', r.text.slice(0, 400));

  // verify it's gone
  const m = await api('POST', '/dss/framework/orchestrator/getVersionByOrchestratorId', { body: { orchestratorId: oid } });
  console.log('post-delete version check HTTP', m.status, '->', m.text.slice(0, 200));
})();

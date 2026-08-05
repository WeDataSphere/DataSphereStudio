// SAFE isolated-node test: detect backend validator presence + capture, GUARANTEED cleanup.
// Strategy: add orphan, autoSave; if backend 1.24.0 -> PENDING_CONFIRM (not persisted); if 1.22.0 -> persisted.
// Always: remove orphan locally + force a clean autoSave of the valid original DAG to purge any orphan server-side.
const out = { test: 'isolated-node-safe', steps: [] };
const cy = window.__dss && window.__dss.cy;
const mod = window.__dss && window.__dss.module;
if (!cy || !mod) return { error: 'no cy/mod' };

// snapshot ORIGINAL valid state (deep) for guaranteed restore
const origNodes = JSON.parse(JSON.stringify((mod.json && mod.json.nodes) || []));
const origEdges = JSON.parse(JSON.stringify((mod.json && mod.json.edges) || []));
out.origNodeCount = origNodes.length;
out.origEdgeCount = origEdges.length;

// close any modals + clear highlights
(function closeAll() {
  document.querySelectorAll('.ivu-modal-wrap').forEach((w) => {
    if (w.style.display !== 'none') { const x = w.querySelector('.ivu-modal-close'); if (x) x.click(); }
  });
  document.querySelectorAll('.ivu-modal-confirm .ivu-btn:not(.ivu-btn-primary)').forEach((b) => b.click());
})();
await new Promise((r) => setTimeout(r, 700));
cy.$('.dag-error, .dag-warn').removeClass('dag-error dag-warn');

// add orphan modeled on node[0]
const exemplar = origNodes[0];
const orphanId = 'orphan_safe_' + Date.now();
const orphan = {
  key: orphanId, id: orphanId, title: 'orphan_safe_test',
  type: exemplar.type || 'sparksql', jobType: exemplar.jobType || exemplar.type || 'sparksql',
  params: {}, resources: [],
};
mod.json.nodes.push(orphan);
out.steps.push('added orphan ' + orphanId);
try { cy.add({ group: 'nodes', data: { id: orphanId, key: orphanId, title: 'orphan_safe_test', jobType: orphan.type } }); } catch (e) { out.cyAddErr = e.message; }

// call autoSave
let ret;
try { ret = await mod.autoSave('UI_TEST_ISOLATED_SAFE', false); } catch (e) { out.saveThrew = e.message; }
out.autoSaveReturn = ret;
await new Promise((r) => setTimeout(r, 3000));

// capture: any warn/error dialog + highlights
const body = (document.body.innerText||'').replace(/\s+/g,' ');
out.keywordHits = {};
['孤岛','孤立','断链','结构','校验','警告','确认','ISOLATED','warn'].forEach((k) => {
  const i = body.indexOf(k);
  if (i >= 0) out.keywordHits[k] = body.slice(Math.max(0,i-20), i+50);
});
out.highlights = {
  dagError: cy.$('.dag-error').length,
  dagWarn: cy.$('.dag-warn').length,
};
out.modRepeatTitles = mod.repeatTitles;
out.modRepetitionShow = mod.repetitionNameShow;

// ===== GUARANTEED CLEANUP (regardless of backend version) =====
// 1. dismiss any confirm dialog WITHOUT confirming (never forceSave the orphan)
(function dismissConfirms() {
  document.querySelectorAll('.ivu-modal-confirm .ivu-btn:not(.ivu-btn-primary)').forEach((b) => b.click());
  document.querySelectorAll('.ivu-modal-wrap').forEach((w) => {
    if (w.style.display !== 'none') { const x = w.querySelector('.ivu-modal-close'); if (x) x.click(); }
  });
})();
await new Promise((r) => setTimeout(r, 600));

// 2. restore original nodes/edges exactly
mod.json.nodes = JSON.parse(JSON.stringify(origNodes));
mod.json.edges = JSON.parse(JSON.stringify(origEdges));
// remove orphan from cy if still present
try { const o = cy.getElementById(orphanId); if (o && o.length) o.remove(); } catch (_) {}
cy.$('.dag-error, .dag-warn').removeClass('dag-error dag-warn');
out.steps.push('restored original nodes/edges locally');

// 3. force a CLEAN autoSave to purge any orphan the backend may have persisted (1.22.0 case)
let cleanRet, cleanErr;
try { cleanRet = await mod.autoSave('UI_TEST_CLEANUP', false); } catch (e) { cleanErr = e.message; }
out.cleanSaveReturn = cleanRet;
out.cleanSaveErr = cleanErr;
out.finalNodeCount = (mod.json.nodes || []).length;
out.steps.push('clean save done');
return out;

// TEST: duplicate node name (DUPLICATE_NAME, ERROR, VALIDATION_FAILED).
// Pre: window.__dss.{cy,module} set (run probe-cy.js first); inside a workflow editor in cyeditor mode.
// Strategy: mutate mod.json.nodes[].title (the authoritative field serialized by autoSave) +
// mirror to cy for visual consistency; then call mod.autoSave('UI_TEST', false) = manual save.
const out = { test: 'duplicate-name', steps: [] };
const cy = window.__dss && window.__dss.cy;
const mod = window.__dss && window.__dss.module;
if (!cy || !mod) { out.error = 'window.__dss.cy/module not set — run probe-cy.js first'; return out; }
const nodes = (mod.json && mod.json.nodes) || [];
if (nodes.length < 2) { out.error = 'need >=2 nodes; have ' + nodes.length; return out; }

const n1 = nodes[0];
const n2 = nodes[1];
const origTitle1 = n1.title;
const origTitle2 = n2.title;
out.original = { id1: n1.key || n1.id, title1: origTitle1, id2: n2.key || n2.id, title2: origTitle2 };

// Validate regex autoSave enforces: ^[a-zA-Z][a-zA-Z0-9_]*$
const dupName = 'DUP_TEST_' + Date.now();
out.dupName = dupName;
n1.title = dupName;
n2.title = dupName;
out.steps.push('mutated mod.json.nodes[0,1].title => ' + dupName);
// mirror to cy if ids map
try {
  const c1 = cy.getElementById(n1.key || n1.id);
  const c2 = cy.getElementById(n2.key || n2.id);
  if (c1 && c1.length) c1.data('title', dupName);
  if (c2 && c2.length) c2.data('title', dupName);
  out.steps.push('mirrored titles to cy');
} catch (e) { out.cyMirrorError = e.message; }

// reset highlights + close modals
cy.$('.dag-error, .dag-warn').removeClass('dag-error dag-warn');
// reset the validation dialog state on the module
try { mod.validationDialogVisible = false; } catch (_) {}

let saveThrew = null;
try {
  // autoSave(comment, f) with f=false => manual save path => shows dialog on validation error
  await mod.autoSave('UI_TEST_DUP', false);
  out.steps.push('called mod.autoSave()');
} catch (e) { saveThrew = e.message; }
out.saveThrew = saveThrew;

// allow network + Vue render
await new Promise((r) => setTimeout(r, 4000));

out.modal = (function () {
  const vdm = document.querySelector('.validation-result-modal');
  if (vdm) {
    const wrap = vdm.closest('.ivu-modal-wrap');
    if (wrap && wrap.style.display !== 'none') return { type: 'ValidationResultDialog', level: 'error', text: (vdm.innerText || '').slice(0, 800) };
  }
  const cm = document.querySelector('.ivu-modal-confirm');
  if (cm) {
    const wrap = cm.closest('.ivu-modal-wrap');
    if (wrap && wrap.style.display !== 'none') return { type: 'ModalConfirm', text: (cm.innerText || '').slice(0, 800) };
  }
  const vis = Array.from(document.querySelectorAll('.ivu-modal-wrap')).filter((w) => w.style.display !== 'none');
  if (vis.length) return { type: 'otherModal', text: (vis[0].innerText || '').slice(0, 500) };
  return { type: 'none' };
})();

out.highlights = {
  dagError: cy.$('.dag-error').length,
  dagWarn: cy.$('.dag-warn').length,
  errorNodeIds: cy.$('.dag-error').nodes().map((n) => n.data('id')),
};
out.errorNodesInfo = cy.$('.dag-error').nodes().map((n) => ({ id: n.data('id'), title: n.data('title') }));
out.notices = Array.from(document.querySelectorAll('.ivu-message-notice-content, .ivu-notice-content')).map((e) => (e.innerText || '').slice(0, 200)).filter(Boolean);

// RESTORE (error save blocked by backend; revert model + cy)
n1.title = origTitle1;
n2.title = origTitle2;
try {
  const c1 = cy.getElementById(n1.key || n1.id);
  const c2 = cy.getElementById(n2.key || n2.id);
  if (c1 && c1.length) c1.data('title', origTitle1);
  if (c2 && c2.length) c2.data('title', origTitle2);
} catch (_) {}
cy.$('.dag-error, .dag-warn').removeClass('dag-error dag-warn');
try { mod.validationDialogVisible = false; } catch (_) {}
out.restored = { title1: n1.title, title2: n2.title };
return out;

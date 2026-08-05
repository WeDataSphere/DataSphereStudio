// Probe: find module.vue process component, ensure cyeditor mode, expose cy instance.
// Exposes window.__dss = { module, cy } for subsequent test scripts.
const out = { steps: [] };
function findModuleVue() {
  // The process-module root
  const el = document.querySelector('.process-module');
  if (el && el.__vue__) return el.__vue__;
  // fallback: search any element with __vue__ whose $options.name is 'process' / 'Process'
  const all = document.querySelectorAll('[class*=process]');
  for (const e of all) {
    let v = e.__vue__;
    while (v) {
      if (v.$options && /process/i.test(v.$options.name || '') && v.$refs && v.$refs.process) return v;
      v = v.$parent;
    }
  }
  return null;
}
const mod = findModuleVue();
if (!mod) {
  out.error = 'module.vue (process component) not found — are we inside a workflow editor? url=' + location.href;
  // expose diagnostic
  out.processModuleEl = !!document.querySelector('.process-module');
  out.hasNodeName = document.querySelectorAll('.node-name').length;
  return out;
}
window.__dss = window.__dss || {};
window.__dss.module = mod;
out.steps.push('found module.vue: name=' + (mod.$options.name || '?'));

// viewMode
out.viewMode = mod.viewMode;
if (mod.viewMode !== 'cyeditor') {
  out.steps.push('viewMode=' + mod.viewMode + ', attempting changeViewMode("cyeditor")');
  try {
    if (typeof mod.changeViewMode === 'function') {
      mod.changeViewMode('cyeditor');
      // wait a tick is not possible in sync eval; just report
      out.requestedSwitch = true;
    }
  } catch (e) {
    out.switchError = e.message;
  }
}

// Access cy via $refs.process.instance.cy
const proc = mod.$refs && mod.$refs.process;
if (!proc) {
  out.error = 'module.$refs.process is null (editor component not rendered yet)';
  return out;
}
out.procName = proc.$options && proc.$options.name;
let cy = null;
try {
  cy = proc.instance && proc.instance.cy;
} catch (e) {
  out.cyAccessError = e.message;
}
// fallback: window.cy
if (!cy && window.cy) { cy = window.cy; out.usedWindowCy = true; }
if (!cy) {
  out.error = 'cy instance not accessible via $refs.process.instance.cy or window.cy';
  out.procKeys = Object.keys(proc.$data || {}).slice(0, 30);
  out.procInstanceKeys = proc.instance ? Object.keys(proc.instance) : 'no instance';
  return out;
}
window.__dss.cy = cy;
out.cyFound = true;
// Node/edge summary
const nodes = cy.nodes();
const edges = cy.edges();
out.nodeCount = nodes.length;
out.edgeCount = edges.length;
out.sampleNodes = nodes.slice(0, 6).map((n) => {
  const d = n.data();
  return { id: d.id, title: d.title, name: d.name, key: d.key, jobType: d.jobType };
});
out.sampleEdges = edges.slice(0, 4).map((e) => {
  const d = e.data();
  return { id: d.id, source: d.source, target: d.target };
});
return out;

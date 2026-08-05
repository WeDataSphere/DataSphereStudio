// Final residue cleanup: ensure no leftover modals/highlights/repeat state.
const out = {};
const cy = window.__dss && window.__dss.cy;
const mod = window.__dss && window.__dss.module;
// clear highlights
if (cy) { try { cy.$('.dag-error, .dag-warn').removeClass('dag-error dag-warn'); } catch(_){} }
// reset repeat state
if (mod) { try { mod.repetitionNameShow = false; mod.repeatTitles = []; } catch(_){} }
// dismiss any stray confirm dialogs (cancel, never OK)
document.querySelectorAll('.ivu-modal-confirm .ivu-btn:not(.ivu-btn-primary)').forEach((b) => b.click());
out.localNodeCount = mod && mod.json && mod.json.nodes ? mod.json.nodes.length : 'n/a';
out.localTitles = mod && mod.json && mod.json.nodes ? mod.json.nodes.map((n) => n.title) : [];
out.residueHighlights = cy ? cy.$('.dag-error, .dag-warn').length : 'no cy';
out.residueRepeat = mod ? mod.repetitionNameShow : 'no mod';
return out;

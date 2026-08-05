// Emit test bodies to files for curl debugging.
const fs = require('fs');
const path = require('path');
const baseTpl = JSON.parse(fs.readFileSync(path.join(__dirname, 'temp_flow_template.json'), 'utf8'));
function flowWith(nodes, edges) { return JSON.stringify(Object.assign({}, baseTpl, { nodes, edges })); }
function node(key, title) { return { id: key, key, title, jobType: 'spark.sql', layout:{x:100,y:100,width:150,height:40}, params:{variable:{},configuration:{runtime:{},startup:{},special:{}}}, resources:[], dependencys:[], jobContent:{} }; }
function edge(s,t){ return {source:s,target:t,linkType:'straight',sourceLocation:'bottom',targetLocation:'top'}; }

// Case 1 cycle
const c1 = flowWith([node('A','n_a'),node('B','n_b'),node('C','n_c')],[edge('A','B'),edge('B','C'),edge('C','A')]);
const body1 = { id: 3709, json: c1, workspaceName:'bdapWorkspace', projectName:'dss_test_2021223', labels:{route:'dev'}, forceSave:false };
fs.writeFileSync(path.join(__dirname,'body_case1.json'), JSON.stringify(body1));
console.log('case1 body length:', JSON.stringify(body1).length);

// Also a simple 3-node cycle jsonFlow WITHOUT base template extras (minimal)
const minCycle = JSON.stringify({ nodes:[node('A','n_a'),node('B','n_b'),node('C','n_c')], edges:[edge('A','B'),edge('B','C'),edge('C','A')] });
const body1m = { id: 3709, json: minCycle, labels:{route:'dev'}, forceSave:false };
fs.writeFileSync(path.join(__dirname,'body_case1_min.json'), JSON.stringify(body1m));
console.log('case1-min body length:', JSON.stringify(body1m).length);
console.log('minCycle json string:', minCycle);

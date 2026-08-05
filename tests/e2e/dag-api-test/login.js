// Login to DSS and persist a fresh cookie header for API tests.
// Usage: node login.js <username> <password>
// Writes combined Cookie header to tests/e2e/.auth/cookie_header.txt (default path http.js reads).
const fs = require('fs');
const path = require('path');

const BASE = 'http://10.107.97.166:8088/api/rest_j/v1';
const user = process.argv[2];
const pass = process.argv[3];
if (!user || !pass) { console.error('Usage: node login.js <username> <password>  (凭证请从 .claude/config/deployment-config.json 读取，勿硬编码)'); process.exit(1); }
const OUT = path.join(__dirname, '..', '.auth', 'cookie_header.txt');

(async () => {
  console.log('=== login', user, '->', BASE + '/user/login');
  const res = await fetch(BASE + '/user/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userName: user, password: pass })
  });
  const text = await res.text();
  console.log('HTTP', res.status);
  // Collect set-cookie
  const cookies = res.headers.getSetCookie ? res.headers.getSetCookie() : [res.headers.get('set-cookie')].filter(Boolean);
  console.log('set-cookie count:', cookies.length);
  const pairs = cookies.map(c => c.split(';')[0]).filter(Boolean);
  console.log('cookie pairs:', pairs.join('; ').slice(0, 120), '...');
  let json = null;
  try { json = JSON.parse(text); } catch (_) {}
  console.log('message:', json && json.message, 'method:', json && json.data && json.data.method);
  if (!pairs.length) { console.log('FAILED no cookie'); console.log('BODY:', text.slice(0, 300)); process.exit(1); }
  fs.writeFileSync(OUT, pairs.join('; '));
  console.log('wrote', OUT);
})();

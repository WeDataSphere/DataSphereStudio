// CDP helper for DSS workflow DAG validation UI tests.
// Usage:
//   node tests/cdp-helper.js eval <jsFile>      // eval JS file in page, print JSON result
//   node tests/cdp-helper.js screenshot <outPng> // take screenshot
//   node tests/cdp-helper.js info                // dump current url + basic DOM info
const fs = require('fs');
const { chromium } = require('playwright');

const CDP_URL = 'http://localhost:9222';
const URL_SUBSTR = '10.107.97.166';

function result(data) {
  console.log(JSON.stringify(data, null, 2));
}

(async () => {
  const cmd = process.argv[2];
  const browser = await chromium.connectOverCDP(CDP_URL);
  const ctx = browser.contexts()[0];
  const pages = ctx.pages();
  let page = pages.find((p) => p.url().includes(URL_SUBSTR));
  if (!page) {
    page = pages[0];
  }
  if (!page) {
    console.error('No page found. Pages:', pages.map((p) => p.url()));
    process.exit(2);
  }

  try {
    if (cmd === 'eval') {
      const jsFile = process.argv[3];
      const code = fs.readFileSync(jsFile, 'utf8');
      // wrap so the file's last expression (or return value) is captured
      const wrapped = `(async () => { ${code} })()`;
      const data = await page.evaluate(wrapped);
      result(data);
    } else if (cmd === 'screenshot') {
      const out = process.argv[3];
      await page.screenshot({ path: out, fullPage: false });
      result({ screenshot: out });
    } else if (cmd === 'goto') {
      const url = process.argv[3];
      await page.goto(url, { waitUntil: 'networkidle' });
      result({ url: page.url(), title: await page.title() });
    } else if (cmd === 'login') {
      const user = process.argv[3];
      const pass = process.argv[4];
      // ensure on login page
      if (!page.url().includes('/#/login')) {
        await page.goto('http://10.107.97.166:8088/#/login', { waitUntil: 'networkidle' });
      }
      const loginRes = await page.evaluate(async (u, p) => {
        const base = '/api/rest_j/v1';
        let password = p;
        let params = { userName: u, password: p };
        try {
          const pk = await (await fetch(base + '/user/publicKey', { method: 'GET', credentials: 'include' })).json();
          if (pk && pk.enableLoginEncrypt && pk.publicKey) {
            const key = `-----BEGIN PUBLIC KEY-----${pk.publicKey}-----END PUBLIC KEY-----`;
            const enc = new JSEncrypt();
            enc.setPublicKey(key);
            password = enc.encrypt(p);
            params = { userName: u, password };
          }
        } catch (_) { /* fall back to plain */ }
        const r = await fetch(base + '/user/login', {
          method: 'POST',
          credentials: 'include',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(params),
        });
        const body = await r.text();
        return { status: r.status, encrypted: password !== p, bodyHead: body.slice(0, 400) };
      }, user, pass);
      result(loginRes);
    } else if (cmd === 'cookies') {
      const cookies = await ctx.cookies();
      const filtered = cookies.filter((c) => c.domain.includes('10.107.97.166') || c.domain.includes('localhost'));
      result(filtered.map((c) => ({ name: c.name, value: (c.value||'').slice(0, 20) + '...', domain: c.domain, expires: c.expires })));
    } else if (cmd === 'info') {
      const data = await page.evaluate(() => ({
        url: location.href,
        title: document.title,
        hasProcessModule: !!document.querySelector('.process-module'),
        hasCyeditor: !!document.querySelector('.cy-editor-container'),
        modals: Array.from(document.querySelectorAll('.ivu-modal')).map((m) => ({
          visible: m.style.display !== 'none',
          title: (m.querySelector('.ivu-modal-header') || {}).innerText || '',
        })),
        noticeBars: document.querySelectorAll('[class*=notice],[class*=Notice],.top-login-notice-bar').length,
      }));
      result(data);
    } else {
      console.error('Unknown command:', cmd);
      process.exit(2);
    }
  } catch (e) {
    console.error('EVAL_ERROR:', e.message);
    process.exit(1);
  } finally {
    // do NOT close the browser (it's the user's session); only disconnect
    await browser.close();
  }
})().catch((e) => {
  console.error('FATAL:', e.message);
  process.exit(1);
});

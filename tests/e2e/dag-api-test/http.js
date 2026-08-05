// Shared HTTP helper for DAG structure validation API tests.
// Reads cookie header from tests/e2e/.auth/cookie_header.txt
const fs = require('fs');
const path = require('path');

const BASE = 'http://10.107.97.166:8088/api/rest_j/v1';
const DEFAULT_COOKIE_PATH = path.join(__dirname, '..', '.auth', 'cookie_header.txt');
const COOKIE_PATH = process.env.DSS_COOKIE_FILE
  ? (path.isAbsolute(process.env.DSS_COOKIE_FILE) ? process.env.DSS_COOKIE_FILE : path.join(__dirname, process.env.DSS_COOKIE_FILE))
  : DEFAULT_COOKIE_PATH;

function cookieHeader() {
  return fs.readFileSync(COOKIE_PATH, 'utf8').trim();
}

async function api(method, urlPath, { body, query } = {}) {
  let url = BASE + urlPath;
  if (query) {
    const qs = new URLSearchParams();
    for (const [k, v] of Object.entries(query)) {
      if (v !== undefined && v !== null) qs.set(k, String(v));
    }
    url += (url.includes('?') ? '&' : '?') + qs.toString();
  }
  const headers = { Cookie: cookieHeader() };
  const init = { method, headers };
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
    init.body = typeof body === 'string' ? body : JSON.stringify(body);
  }
  const res = await fetch(url, init);
  const text = await res.text();
  let json = null;
  try { json = JSON.parse(text); } catch (_) { /* keep text */ }
  return { status: res.status, text, json };
}

module.exports = { BASE, cookieHeader, api };

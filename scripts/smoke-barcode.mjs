import assert from "node:assert/strict";

const base = process.env.API_BASE_URL ?? "http://localhost:8081";
const down = process.argv.includes("--down");
for (const [code, expected] of down ? [["3017624010701", 503]] : [
  ["3017624010701", 200], ["96385074", 404], ["012345678905", 503], ["bad", 422],
]) {
  const start = Date.now();
  const response = await fetch(`${base}/api/v1/codigos-barras/${code}`, {
    signal: AbortSignal.timeout(6000),
  });
  const body = await response.json();
  if (down) {
    assert.ok([503, 504].includes(response.status), JSON.stringify(body));
  } else {
    assert.equal(response.status, expected, JSON.stringify(body));
  }
  assert.ok(Date.now() - start < 6000, "lookup did not respect bounded wait");
  if (expected === 200) {
    assert.equal(body.codigo, code);
    assert.equal(body.nome, "Produto de teste");
    assert.equal(body.fonte, "open-food-facts");
  } else {
    assert.match(response.headers.get("content-type"), /application\/problem\+json/);
    assert.equal(body.status, response.status);
  }
}
const health = await fetch(`${base}/q/health/ready`, { signal: AbortSignal.timeout(5000) });
assert.equal(health.status, 200, "Go availability must not control API readiness");
console.log(`Barcode REST smoke passed (${down ? "Go stopped" : "controlled HTTP provider"}).`);

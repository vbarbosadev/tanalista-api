import assert from 'node:assert/strict';
import { randomUUID } from 'node:crypto';
import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { dirname } from 'node:path';
import { setTimeout as delay } from 'node:timers/promises';

// Node 22+, sem dependências. Cria e remove somente os recursos desta execução.
const base = (process.env.API_URL ?? 'http://localhost:8081').replace(/\/$/, '');
const stateFile = process.env.SMOKE_STATE_FILE ?? 'target/smoke-sprint1-state.json';
let checks = 0;

async function request(path, status = 200, { method = 'GET', body, raw, headers = {} } = {}) {
  const response = await fetch(base + path, {
    method,
    headers: { 'Content-Type': 'application/json', ...headers },
    body: raw ?? (body === undefined ? undefined : JSON.stringify(body)),
    signal: AbortSignal.timeout(15_000),
  });
  const text = await response.text();
  assert.equal(response.status, status, `${method} ${path}: ${text}`);
  checks++;
  let data;
  if (status === 204) assert.equal(text, '');
  else if (response.headers.get('content-type')?.includes('json')) data = JSON.parse(text);
  if (status >= 400) {
    assert.match(response.headers.get('content-type') ?? '', /^application\/problem\+json/);
    assert.equal(data.status, status);
    for (const key of ['type', 'title', 'detail', 'instance']) assert.equal(typeof data[key], 'string');
    assert.equal(data.instance, new URL(base + path).pathname);
    assert.doesNotMatch(data.detail, /SQL|Exception|password|stacktrace/i);
  }
  return { data, response, text };
}

async function ready() {
  for (let attempt = 0; attempt < 60; attempt++) {
    try {
      const r = await fetch(base + '/hello', { signal: AbortSignal.timeout(2_000) });
      if (r.status === 200) return;
    } catch { /* A API pode estar iniciando as migrações. */ }
    await delay(1_000);
  }
  throw new Error('API não ficou pronta em 60 tentativas.');
}

await ready();
const root = '/api/v1/listas';
const mode = process.argv[2];
if (mode === '--disabled') {
  await request(root, 503);
  await request(root, 503, { method: 'POST', body: { nome: 'Não deve ser criado' } });
} else if (mode === '--seed') {
  const nome = `persistencia-${randomUUID()}`;
  const { data: lista } = await request(root, 201, { method: 'POST', body: { nome } });
  const { data: item } = await request(`${root}/${lista.id}/itens`, 201, {
    method: 'POST', body: { nome: 'Item persistente', quantidade: 2, precoEstimado: 3.45 },
  });
  await mkdir(dirname(stateFile), { recursive: true });
  await writeFile(stateFile, JSON.stringify({ lista, item }));
} else if (mode === '--verify' || mode === '--hidden') {
  const { lista, item } = JSON.parse(await readFile(stateFile, 'utf8'));
  const listPath = `${root}/${lista.id}`;
  const itemPath = `${listPath}/itens/${item.id}`;
  if (mode === '--hidden') {
    await request(listPath, 404);
    await request(itemPath, 404);
    await request(`${listPath}/itens`, 404);
    await request(listPath, 404, { method: 'PATCH', body: { nome: 'Outro dono' } });
    await request(listPath, 404, { method: 'DELETE' });
    const { data: pagina } = await request(`${root}?nome=${lista.nome}`);
    assert.equal(pagina.total, 0);
  } else {
    const { data: l } = await request(listPath);
    const { data: i } = await request(itemPath);
    assert.equal(l.nome, lista.nome);
    assert.equal(i.quantidade, 2);
    assert.equal(i.precoEstimado, 3.45);
    await request(listPath, 204, { method: 'DELETE' });
  }
} else {
  const ids = new Set();
  const nome = `smoke-${randomUUID()}`;
  async function createList(name) {
    const result = await request(root, 201, { method: 'POST', body: { nome: name } });
    ids.add(result.data.id);
    assert.match(result.data.id, /^[0-9a-f-]{36}$/);
    assert.equal(new URL(result.response.headers.get('location')).pathname, `${root}/${result.data.id}`);
    return result.data;
  }
  try {
    const a = await createList(nome);
    const b = await createList(`${nome}-segunda`);
    const path = `${root}/${a.id}`;
    const items = `${path}/itens`;
    const { data: list } = await request(path);
    assert.equal(list.nome, nome);
    assert.equal(list.quantidadeComprada, undefined);
    await request(root, 409, { method: 'POST', body: { nome: `  ${nome.toUpperCase()}  ` } });
    const { data: first } = await request(`${root}?nome=${nome}&page=0&size=1`);
    const { data: second } = await request(`${root}?nome=${nome}&page=1&size=1`);
    assert.equal(first.total, 2);
    assert.equal(first.items.length, 1);
    assert.equal(first.page, 0);
    assert.equal(first.size, 1);
    assert.notEqual(first.items[0].id, second.items[0].id);
    assert.deepEqual((await request(`${root}?nome=${nome}&page=2&size=1`)).data.items, []);
    const { data: updated } = await request(path, 200, { method: 'PATCH', body: { nome: `${nome}-renomeada` } });
    assert.equal(updated.nome, `${nome}-renomeada`);
    await request(path, 409, { method: 'PATCH', body: { nome: b.nome } });

    const { data: item, response } = await request(items, 201, {
      method: 'POST', body: { nome: 'Café com leite', quantidade: 2, unidade: 'UNIDADE', categoria: 'ALIMENTOS', precoEstimado: 6.5 },
    });
    assert.equal(new URL(response.headers.get('location')).pathname, `${items}/${item.id}`);
    assert.equal((await request(`${items}/${item.id}`)).data.listaId, a.id);
    await request(items, 409, { method: 'POST', body: { nome: '  CAFE   COM LEITE ' } });
    const { data: small } = await request(items, 201, { method: 'POST', body: { nome: 'Leite 200g', categoria: 'ALIMENTOS' } });
    await request(items, 201, { method: 'POST', body: { nome: 'Leite 400g', categoria: 'ALIMENTOS' } });
    const { data: page } = await request(`${items}?nome=leite&categoria=ALIMENTOS&size=1`);
    assert.equal(page.total, 3);
    assert.equal(page.items.length, 1);
    assert.equal((await request(`${items}?nome=leite&categoria=OUTRA`)).data.total, 0);
    assert.equal((await request(`${items}?nome=%25`)).data.total, 0);
    assert.equal((await request(`${items}?page=1&size=1`)).data.items.length, 1);
    assert.deepEqual((await request(`${items}?page=10&size=1`)).data.items, []);
    const { data: changed } = await request(`${items}/${item.id}`, 200, { method: 'PATCH', body: { quantidade: null, nome: 'CAFÉ COM LEITE' } });
    assert.equal(changed.quantidade, null);
    assert.equal(changed.precoEstimado, 6.5);
    assert.equal(changed.categoria, 'ALIMENTOS');
    await request(`${items}/${item.id}`, 409, { method: 'PATCH', body: { nome: small.nome } });
    assert.equal((await request(`${items}/${item.id}`)).data.nome, 'CAFÉ COM LEITE');
    assert.ok(Date.parse((await request(path)).data.atualizadaEm) >= Date.parse(updated.atualizadaEm));

    const cross = `${root}/${b.id}/itens/${item.id}`;
    await request(cross, 404);
    await request(cross, 404, { method: 'PATCH', body: { nome: 'Outro' } });
    await request(cross, 404, { method: 'DELETE' });
    for (const body of [{}, { nome: '' }, { nome: ' '.repeat(3) }, { nome: 'x'.repeat(121) }, { nome: null }]) {
      await request(root, 422, { method: 'POST', body });
    }
    for (const body of [null, { nome: 42 }, { nome: 'x', desconhecido: true }, []]) {
      await request(root, body === null ? 422 : 400, { method: 'POST', body });
    }
    await request(root, 400, { method: 'POST', raw: '{' });
    await request(root, 400, { method: 'POST', raw: '{"nome":"x"} {}' });
    for (const body of [{ quantidade: 0 }, { quantidade: -1 }, { quantidade: 1.2345 },
      { precoEstimado: -1 }, { precoEstimado: 1.234 }, { quantidade: 1000000000 },
      { unidade: '' }, { categoria: 'x'.repeat(61) }]) {
      await request(items, 422, { method: 'POST', body: { nome: 'Inválido', ...body } });
    }
    await request(items, 400, { method: 'POST', body: { nome: 'Tipo errado', quantidade: '2' } });
    for (const body of [{}, { nome: null }, { quantidade: 0 }]) {
      await request(`${items}/${item.id}`, 422, { method: 'PATCH', body });
    }
    await request(`${items}/${item.id}`, 400, { method: 'PATCH', body: { extra: 1 } });
    for (const query of ['size=0', 'size=101', 'page=-1', 'page=2147483647&size=100']) {
      await request(`${root}?${query}`, 422);
      await request(`${items}?${query}`, 422);
    }
    await request(`${root}?page=abc`, 400);
    await request(`${root}?page=`, 400);
    await request(`${root}?size=`, 400);
    await request(`${root}?size=1&size=2`, 400);
    await request(`${root}?filtroDesconhecido=x`, 400);
    await request(`${items}?nome=${'x'.repeat(121)}`, 422);
    await request(`${items}?categoria=`, 422);
    await request(`${root}/invalido`, 400);
    await request(`${items}/invalido`, 400);
    const missing = `${root}/${randomUUID()}`;
    await request(missing, 404);
    await request(missing, 404, { method: 'PATCH', body: { nome: 'Ausente' } });
    await request(missing, 404, { method: 'DELETE' });
    await request(`${missing}/itens`, 404);
    await request(`${missing}/itens`, 404, { method: 'POST', body: { nome: 'Ausente' } });
    await request(`${items}/${randomUUID()}`, 404);
    await request(path, 405, { method: 'PUT', body: { nome: 'Método não contratado' } });
    await request(root, 415, { method: 'POST', raw: 'texto', headers: { 'Content-Type': 'text/plain' } });
    await request(root, 406, { headers: { Accept: 'text/plain' } });

    const concurrent = await Promise.all([0, 1].map(async () => {
      const r = await fetch(base + items, { method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ nome: 'Concorrente' }), signal: AbortSignal.timeout(15_000) });
      const data = await r.json();
      assert.ok([201, 409].includes(r.status), JSON.stringify(data));
      if (r.status === 409) assert.match(r.headers.get('content-type'), /application\/problem\+json/);
      return r.status;
    }));
    assert.deepEqual(concurrent.sort(), [201, 409]);
    checks += 2;
    await request(`${items}/${item.id}`, 204, { method: 'DELETE' });
    await request(`${items}/${item.id}`, 404);
    await request(`${items}/${item.id}`, 404, { method: 'DELETE' });
    await request(path, 204, { method: 'DELETE' });
    ids.delete(a.id);
    await request(path, 404);
    await request(items, 404);

    const { data: api } = await request('/q/openapi?format=json');
    assert.deepEqual(Object.keys(api.paths).sort(), [root, `${root}/{listaId}`, `${root}/{listaId}/itens`, `${root}/{listaId}/itens/{itemId}`].sort());
    const resolve = schema => schema?.$ref ? api.components.schemas[schema.$ref.split('/').at(-1)] : schema;
    let operations = 0;
    for (const [p, methods] of Object.entries(api.paths)) {
      for (const [method, op] of Object.entries(methods)) {
        operations++;
        for (const code of ['400', '404', '409', '422', '500', '503']) {
          assert.ok(op.responses[code].content['application/problem+json']);
        }
        for (const param of op.parameters ?? []) {
          if (param.in === 'path') assert.equal(resolve(param.schema).format, 'uuid');
        }
        if (method === 'post') assert.ok(op.responses['201'].headers.Location);
        if (method === 'delete') assert.ok(op.responses['204']);
        if (method === 'get' && !p.endsWith('Id}')) {
          const schema = resolve(op.responses['200'].content['application/json'].schema);
          assert.ok(schema.properties.items);
        }
      }
    }
    assert.equal(operations, 10);
    assert.match((await request('/q/swagger-ui/')).text, /swagger-ui/i);
  } finally {
    for (const id of ids) {
      // Apenas listas criadas por esta execução; nunca limpar tabelas ou listas alheias.
      const response = await fetch(`${base}${root}/${id}`, { method: 'DELETE', signal: AbortSignal.timeout(15_000) });
      assert.ok([204, 404].includes(response.status), `Falha ao limpar lista de smoke ${id}`);
    }
  }
}
console.log(`Smoke Sprint 1 ${mode ?? 'CRUD'}: ${checks} requisições verificadas.`);

// Verificação documental; não substitui testes da aplicação nem validação OpenAPI completa.
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const { createRequire } = require('node:module');
const feature = path.resolve(__dirname, '..');
const repo = path.resolve(feature, '../..');
const frontendRequire = createRequire(path.join(repo, 'alexandria-frontend/package.json'));
const yaml = frontendRequire('js-yaml');
const Ajv = frontendRequire('ajv');
const read = name => fs.readFileSync(path.join(feature, name), 'utf8');
const spec = read('spec.md');
const taskText = read('tasks.md');
const requirements = [...spec.matchAll(/^- \*\*((?:FR|SC)-\d{3})\*\*:/gm)].map(m => m[1]);
assert.equal(requirements.length, 26, 'Inventário de requisitos mudou; revisar a cobertura.');
assert.equal(new Set(requirements).size, requirements.length);
assert(!/\[NEEDS CLARIFICATION\]/.test(spec), 'Esclarecimento pendente.');

const tasks = new Map();
let phase;
for (const line of taskText.split(/\r?\n/)) {
  const heading = line.match(/^## Fase (\d+)/);
  if (heading) phase = Number(heading[1]);
  const match = line.match(/^- \[([ xX])\] (T\d{3}) (.+)/);
  if (!match) continue;
  assert(!tasks.has(match[2]), `Tarefa duplicada: ${match[2]}`);
  tasks.set(match[2], { text: match[3], checked: match[1] !== ' ', phase,
    story: match[3].match(/\[US(\d)\]/)?.[1] });
}
assert.equal(tasks.size, 55);
for (let i = 1; i <= tasks.size; i++) assert(tasks.has(`T${String(i).padStart(3, '0')}`));
function taskIds(text) {
  const result = new Set();
  for (const m of text.matchAll(/T(\d{3})(?:\s*(?:a|–|-)\s*T?(\d{3}))?/g)) {
    for (let i = +m[1]; i <= +(m[2] || m[1]); i++) {
      const id = `T${String(i).padStart(3, '0')}`;
      assert(tasks.has(id), `Referência a tarefa ausente: ${id}`);
      result.add(id);
    }
  }
  return result;
}
const coverage = new Map();
for (const row of taskText.split('## Rastreabilidade de requisitos')[1].split(/\r?\n/)) {
  const ids = taskIds(row);
  for (const req of row.match(/(?:FR|SC)-\d{3}/g) || []) {
    assert(ids.size > 0, `Sem tarefa na linha de ${req}`);
    coverage.set(req, [...ids]);
  }
}
for (const req of requirements) assert(coverage.has(req), `Sem cobertura: ${req}`);
const dependencies = new Map();
for (const [id, task] of tasks) {
  const dependencyText = task.text.match(/\(depende[^)]*\)/i)?.[0] || '';
  const ids = taskIds(dependencyText);
  const dependencyPhase = dependencyText.match(/Fase (\d+)/i)?.[1];
  const dependencyStory = dependencyText.match(/\bUS(\d)\b/)?.[1];
  for (const [otherId, other] of tasks) {
    if ((dependencyPhase && other.phase === +dependencyPhase)
        || (dependencyStory && other.story === dependencyStory)) ids.add(otherId);
  }
  dependencies.set(id, ids);
}
const completed = new Set();
function visit(id, active = new Set()) {
  assert(!active.has(id), `Ciclo de dependências em ${id}`);
  if (completed.has(id)) return;
  for (const dependency of dependencies.get(id)) visit(dependency, new Set([...active, id]));
  completed.add(id);
}
for (const id of tasks.keys()) visit(id);

let links = 0;
function checkLinks(directory) {
  for (const entry of fs.readdirSync(directory, { withFileTypes: true })) {
    const filename = path.join(directory, entry.name);
    if (entry.isDirectory()) checkLinks(filename);
    else if (entry.name.endsWith('.md')) {
      for (const match of fs.readFileSync(filename, 'utf8').matchAll(/\[[^\]]*\]\(([^)]+)\)/g)) {
        const target = match[1].split('#')[0];
        if (!target || /^[a-z]+:\/\//i.test(target) || target.startsWith('/')) continue;
        assert(fs.existsSync(path.resolve(directory, target)), `Link local ausente: ${filename}: ${target}`);
        links++;
      }
    }
  }
}
checkLinks(feature);

const api = yaml.load(read('contracts/recommendations.openapi.yaml'));
assert.equal(api.openapi, '3.0.3');
const references = new Set();
function dereference(value, stack = []) {
  if (Array.isArray(value)) return value.map(v => dereference(v, stack));
  if (!value || typeof value !== 'object') return value;
  if (value.$ref) {
    const ref = value.$ref;
    assert(ref.startsWith('#/'), `Referência externa não suportada: ${ref}`);
    assert(!stack.includes(ref), `Ciclo de schemas: ${[...stack, ref].join(' -> ')}`);
    const resolved = ref.slice(2).split('/').reduce((node, key) =>
      node?.[key.replace(/~1/g, '/').replace(/~0/g, '~')], api);
    assert(resolved !== undefined, `Referência não resolvida: ${ref}`);
    references.add(ref);
    return dereference(resolved, [...stack, ref]);
  }
  const result = Object.fromEntries(Object.entries(value).map(([key, child]) => [key, dereference(child, stack)]));
  if (result.additionalProperties === false) {
    for (const key of result.required || []) assert(key in (result.properties || {}), `required não declarado: ${key}`);
  }
  return result;
}
dereference(api);
function jsonSchema(value) {
  if (Array.isArray(value)) return value.map(jsonSchema);
  if (!value || typeof value !== 'object') return value;
  const converted = Object.fromEntries(Object.entries(value).map(([key, child]) => [key, jsonSchema(child)]));
  if (converted.nullable && converted.type) converted.type = [converted.type, 'null'];
  delete converted.nullable;
  return converted;
}
const ajv = new Ajv({ allErrors: true });
ajv.addFormat('int64', { type: 'number', validate: Number.isInteger });
const validators = Object.fromEntries(Object.entries(api.components.schemas)
  .map(([name, schema]) => [name, ajv.compile(jsonSchema(dereference(schema)))]));
let accepted = 0;
let rejected = 0;
function sample(name, value, expected = true) {
  const valid = validators[name](value);
  assert.equal(valid, expected, `${name}: ${JSON.stringify(validators[name].errors)}`);
  expected ? accepted++ : rejected++;
}
const selection = { id: 1, referenceDate: '2026-10-30', generatedAt: '2026-10-30T03:00:00Z', algorithmVersion: 'content-v1', selectedCount: 1 };
const item = { position: 1, book: { id: 2, title: 'Livro', authors: [{ id: 1, name: 'Autora' }], coverUrl: null }, reason: { type: 'SUBJECT', matchedValue: 'ficção', text: 'Assunto presente na biblioteca na data da seleção.' } };
const ready = { state: 'READY', selection, items: [item], emptyReason: null, updatePending: false, retryAfterSeconds: null, generationStatus: 'READY', message: null };
sample('RecommendationResponse', ready);
sample('RecommendationResponse', { ...ready, state: 'EMPTY', items: [], selection: null, emptyReason: 'NO_PROFILE', generationStatus: 'NO_PROFILE' });
sample('RecommendationResponse', { ...ready, state: 'EMPTY', selection: { ...selection, selectedCount: 0 }, items: [], emptyReason: 'NO_MATCHES' });
sample('RecommendationResponse', { ...ready, state: 'EMPTY', items: [], emptyReason: 'ALL_ADDED_OR_UNAVAILABLE' });
sample('RecommendationResponse', { ...ready, state: 'STALE', updatePending: true, generationStatus: 'FAILED' });
sample('RecommendationResponse', { ...ready, state: 'STALE', items: [], updatePending: true, generationStatus: 'PENDING', retryAfterSeconds: 3 });
sample('RecommendationResponse', { ...ready, state: 'GENERATING', selection: null, items: [], updatePending: true, generationStatus: 'BUILDING', retryAfterSeconds: 3 });
const unavailable = { message: 'Temporariamente indisponível', status: 503, timestamp: '2026-10-30T12:00:00', generationStatus: 'FAILED', retryAfterSeconds: 60 };
sample('RecommendationUnavailableResponse', unavailable);
sample('RecommendationUnavailableResponse', { ...unavailable, retryAfterSeconds: null });
sample('RecommendationUnavailableResponse', { ...unavailable, generationStatus: 'IDLE', retryAfterSeconds: 3 });
for (const state of ['ACCEPTED', 'ALREADY_RUNNING', 'READY', 'NO_PROFILE']) sample('GenerationResponse', { state, referenceDate: selection.referenceDate });
for (const key of Object.keys(ready)) {
  const value = { ...ready }; delete value[key]; sample('RecommendationResponse', value, false);
}
for (const key of Object.keys(unavailable)) {
  const value = { ...unavailable }; delete value[key]; sample('RecommendationUnavailableResponse', value, false);
}
sample('RecommendationResponse', { ...ready, generationStatus: 'UNKNOWN' }, false);
sample('RecommendationResponse', { ...ready, unexpected: true }, false);
sample('RecommendationResponse', { ...ready, items: Array(31).fill(item) }, false);
sample('RecommendationUnavailableResponse', { ...unavailable, generationStatus: 'BUILDING' }, false);
sample('RecommendationUnavailableResponse', { ...unavailable, retryAfterSeconds: 0 }, false);
for (const operation of Object.values(api.paths).flatMap(item => Object.values(item))) {
  for (const [status, response] of Object.entries(operation.responses)) {
    if (status !== '401') assert.deepEqual(response.headers?.['Cache-Control']?.schema?.enum,
      ['no-store'], `no-store ausente em ${status}`);
    for (const media of Object.values(response.content || {})) {
      for (const example of Object.values(media.examples || {})) {
        assert(ajv.validate(jsonSchema(dereference(media.schema)), example.value));
      }
    }
  }
}
console.log(JSON.stringify({ requirements: requirements.length, covered: coverage.size,
  tasks: tasks.size, checkedTasks: [...tasks.values()].filter(t => t.checked).length,
  dependencyCycles: 0, localLinks: links, resolvedReferences: references.size,
  compiledSchemas: Object.keys(validators).length, acceptedSamples: accepted, rejectedSamples: rejected }, null, 2));
console.log('PASS documental. Não valida combinações semânticas de estados, runtime, concorrência ou OpenAPI completo.');

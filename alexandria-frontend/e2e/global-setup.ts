import { spawn, type ChildProcess } from 'node:child_process';
import { closeSync, openSync } from 'node:fs';
import { mkdtemp, readFile, unlink } from 'node:fs/promises';
import { randomUUID } from 'node:crypto';
import os from 'node:os';
import path from 'node:path';
import { writeControlCommand } from './fixtures/control-file';

const repo = path.resolve(__dirname, '../..');
const backend = path.join(repo, 'alexandria-backend');
const wait = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

function finished(child: ChildProcess, description: string, timeout: number): Promise<void> {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => { child.kill(); reject(new Error(`${description} excedeu o prazo; consultar backend.log do fixture.`)); }, timeout);
    child.once('error', error => { clearTimeout(timer); reject(error); });
    child.once('exit', code => {
      clearTimeout(timer);
      if (code === 0) resolve();
      else reject(new Error(`${description} falhou (exit ${code}); consultar backend.log do fixture.`));
    });
  });
}

export default async function globalSetup() {
  if (process.env.E2E_FIXTURE_DIR) return;
  const directory = await mkdtemp(path.join(os.tmpdir(), 'alexandria-recommendations-e2e-'));
  const classpathFile = path.join(directory, 'classpath.txt');
  const logPath = path.join(directory, 'backend.log');
  const descriptor = openSync(logPath, 'a');
  const options = { cwd: backend, windowsHide: true, stdio: ['ignore', descriptor, descriptor] as ['ignore', number, number] };
  let server: ChildProcess | undefined;
  try {
    const wrapper = path.join(backend, process.platform === 'win32' ? 'mvnw.cmd' : 'mvnw');
    let maven: ChildProcess;
    if (process.platform === 'win32') {
      // Paths come only from this checkout and mkdtemp. Reject shell metacharacter
      // expansion before composing the fixed .cmd invocation.
      if (/["%!\r\n]/.test(wrapper + classpathFile)) throw new Error('Caminho incompatível com a chamada segura do Maven wrapper.');
      const command = `""${wrapper}" -DskipTests test-compile dependency:build-classpath "-Dmdep.outputFile=${classpathFile}""`;
      maven = spawn(process.env.ComSpec || 'cmd.exe', ['/d', '/s', '/c', command], { ...options, windowsVerbatimArguments: true });
    } else {
      maven = spawn(wrapper, ['-DskipTests', 'test-compile', 'dependency:build-classpath', `-Dmdep.outputFile=${classpathFile}`], options);
    }
    await finished(maven, 'Compilação do backend E2E', 180_000);
    const dependencies = (await readFile(classpathFile, 'utf8')).trim();
    const classpath = [path.join(backend, 'target/test-classes'), path.join(backend, 'target/classes'), dependencies].join(path.delimiter);
    server = spawn('java', [`-Dapi.version=${process.env.E2E_DOCKER_API_VERSION || '1.44'}`, '-cp', classpath,
      'com.pucsp.alexandria.support.RecommendationE2EServer', directory, process.env.E2E_BACKEND_PORT || '18080'], options);
    let launchError: Error | undefined;
    server.once('error', error => { launchError = error; });
    const deadline = Date.now() + 180_000;
    let ready = false;
    while (Date.now() < deadline) {
      if (launchError) throw launchError;
      if (server.exitCode !== null) throw new Error(`Backend E2E encerrou no startup; consultar ${logPath}`);
      try { JSON.parse(await readFile(path.join(directory, 'ready.json'), 'utf8')); ready = true; break; } catch { /* Startup is still pending. */ }
      await wait(250);
    }
    if (!ready) throw new Error(`Backend E2E não ficou pronto; consultar ${logPath}`);
    process.env.E2E_FIXTURE_DIR = directory;
  } catch (error) {
    server?.kill();
    throw error;
  } finally {
    closeSync(descriptor);
  }
  return async () => {
    if (!server || server.exitCode !== null) return;
    try {
      await writeControlCommand(directory, { id: randomUUID(), action: 'stop' });
      const deadline = Date.now() + 20_000;
      while (server.exitCode === null && server.signalCode === null && Date.now() < deadline) await wait(100);
    } finally {
      if (server.exitCode === null && server.signalCode === null) server.kill();
      // Retain only diagnostics, removing test JWTs and control files individually.
      await Promise.allSettled(['ready.json', 'request.json', 'response.json'].map(name => unlink(path.join(directory, name))));
    }
  };
}

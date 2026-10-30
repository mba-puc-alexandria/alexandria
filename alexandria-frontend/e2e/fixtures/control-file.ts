import { rename, writeFile } from 'node:fs/promises';
import { randomUUID } from 'node:crypto';
import { setTimeout as delay } from 'node:timers/promises';
import path from 'node:path';

export async function writeControlCommand(directory: string, command: { id: string; action: string; [key: string]: unknown }): Promise<void> {
  const temporary = path.join(directory, `request-${randomUUID()}.tmp`);
  await writeFile(temporary, JSON.stringify(command), 'utf8');
  const deadline = Date.now() + 2_000;
  while (true) {
    try {
      await rename(temporary, path.join(directory, 'request.json'));
      return;
    } catch (error) {
      // Windows can briefly deny replacing the file while Java reads it.
      // Retrying rename preserves the atomic handoff; never delete the target.
      const code = (error as NodeJS.ErrnoException).code;
      if (!['EPERM', 'EBUSY', 'EACCES'].includes(code || '') || Date.now() >= deadline) throw error;
      await delay(25);
    }
  }
}

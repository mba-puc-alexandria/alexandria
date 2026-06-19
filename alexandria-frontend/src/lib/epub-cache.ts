export interface CacheEntry {
  data: ArrayBuffer;
  lastAccessed: number;
  size: number;
}

const LRU_MAX_ENTRIES = 30;
const LRU_MAX_SIZE_BYTES = 250 * 1024 * 1024;

const MEMORY_CACHE = new Map<string, CacheEntry>();

const DB_NAME = "alexandria-epub-cache";
const STORE_NAME = "epubs";
const DB_VERSION = 2;

function openDB(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);

    request.onupgradeneeded = () => {
      const db = request.result;
      if (!db) return;

      if (!db.objectStoreNames.contains(STORE_NAME)) {
        db.createObjectStore(STORE_NAME);
      }
    };

    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

function idbGet<T>(store: IDBObjectStore, key: IDBValidKey): Promise<T | undefined> {
  return new Promise((resolve, reject) => {
    const req = store.get(key);
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

function idbWait(tx: IDBTransaction): Promise<void> {
  return new Promise((resolve, reject) => {
    tx.oncomplete = () => resolve();
    tx.onerror = () => reject(tx.error);
  });
}

export async function markBookAsAccessed(bookId: string): Promise<void> {
  try {
    const memEntry = MEMORY_CACHE.get(bookId);
    if (memEntry) {
      memEntry.lastAccessed = Date.now();
    }

    const db = await openDB();
    const tx = db.transaction(STORE_NAME, "readwrite");
    const store = tx.objectStore(STORE_NAME);

    const entry = await idbGet<CacheEntry>(store, bookId);
    if (entry) {
      entry.lastAccessed = Date.now();
      store.put(entry);
    }

    await idbWait(tx);
    db.close();
  } catch {
    // Fire-and-forget: errors não afetam a UI
  }
}

export async function getCachedEpub(bookId: string): Promise<ArrayBuffer | null> {
  const memEntry = MEMORY_CACHE.get(bookId);
  if (memEntry) {
    memEntry.lastAccessed = Date.now();
    return memEntry.data.slice(0);
  }

  try {
    const db = await openDB();
    const tx = db.transaction(STORE_NAME, "readonly");
    const store = tx.objectStore(STORE_NAME);

    const entry = await idbGet<CacheEntry>(store, bookId);
    db.close();

    if (entry) {
      entry.lastAccessed = Date.now();
      MEMORY_CACHE.set(bookId, entry);
      markBookAsAccessed(bookId);
      return entry.data.slice(0);
    }
  } catch {
    // Fallback para download se IndexedDB falhar
  }

  return null;
}

export async function saveEpubToCache(bookId: string, data: ArrayBuffer): Promise<void> {
  const entry: CacheEntry = {
    data: data.slice(0),
    lastAccessed: Date.now(),
    size: data.byteLength,
  };

  MEMORY_CACHE.set(bookId, entry);

  try {
    const db = await openDB();
    const tx = db.transaction(STORE_NAME, "readwrite");
    const store = tx.objectStore(STORE_NAME);

    store.put(entry, bookId);
    await idbWait(tx);
    db.close();
  } catch {
    return;
  }

  evictIfNeeded();
}

async function evictIfNeeded(): Promise<void> {
  try {
    const db = await openDB();

    const tx = db.transaction(STORE_NAME, "readonly");
    const store = tx.objectStore(STORE_NAME);
    const entries: Array<{ key: string; entry: CacheEntry }> = [];

    await new Promise<void>((resolve, reject) => {
      const cursor = store.openCursor();
      cursor.onsuccess = () => {
        const result = cursor.result;
        if (result) {
          entries.push({ key: result.key as string, entry: result.value });
          result.continue();
        } else {
          resolve();
        }
      };
      cursor.onerror = () => reject(cursor.error);
    });

    tx.oncomplete = () => db.close();

    const totalSize = entries.reduce((s, e) => s + e.entry.size, 0);
    if (entries.length <= LRU_MAX_ENTRIES && totalSize <= LRU_MAX_SIZE_BYTES) {
      return;
    }

    entries.sort((a, b) => a.entry.lastAccessed - b.entry.lastAccessed);

    const removeCount = Math.ceil(entries.length * 0.3);
    const toRemove = entries.slice(0, removeCount);

    const db2 = await openDB();
    const tx2 = db2.transaction(STORE_NAME, "readwrite");
    const store2 = tx2.objectStore(STORE_NAME);

    for (const { key } of toRemove) {
      store2.delete(key);
      MEMORY_CACHE.delete(key);
    }

    await idbWait(tx2);
    db2.close();
  } catch {
    // Evicção silenciosa
  }
}

export async function removeEpubFromCache(bookId: string): Promise<void> {
  MEMORY_CACHE.delete(bookId);

  try {
    const db = await openDB();
    const tx = db.transaction(STORE_NAME, "readwrite");
    tx.objectStore(STORE_NAME).delete(bookId);
    await idbWait(tx);
    db.close();
  } catch {
    // Remoção silenciosa
  }
}

export async function clearAllEpubCache(): Promise<string[]> {
  MEMORY_CACHE.clear();
  const keys: string[] = [];

  try {
    const db = await openDB();
    const tx = db.transaction(STORE_NAME, "readwrite");
    const store = tx.objectStore(STORE_NAME);

    await new Promise<void>((resolve, reject) => {
      const cursor = store.openCursor();
      cursor.onsuccess = () => {
        const result = cursor.result;
        if (result) {
          keys.push(result.key as string);
          result.continue();
        } else {
          resolve();
        }
      };
      cursor.onerror = () => reject(cursor.error);
    });

    for (const key of keys) {
      store.delete(key);
    }

    await idbWait(tx);
    db.close();
  } catch {
    // Limpeza silenciosa
  }

  return keys;
}

export function getCacheStats() {
  const totalBytes = Array.from(MEMORY_CACHE.values()).reduce((s, e) => s + e.size, 0);
  const mb = totalBytes / (1024 * 1024);
  return {
    memoryEntries: MEMORY_CACHE.size,
    memorySize: mb < 1 ? `${(totalBytes / 1024).toFixed(1)} KB` : `${mb.toFixed(1)} MB`,
  };
}

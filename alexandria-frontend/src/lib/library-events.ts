export type LibraryChange = {
  kind: 'added' | 'already_added' | 'updated' | 'removed' | 'invalidated';
  accountId: number | null;
  bookId?: number;
};

const LOCAL_EVENT = 'alexandria:library-changed';
const STORAGE_KEY = 'alexandria-library-invalidation';

export function libraryAccountId(): number | null {
  if (typeof window === 'undefined') return null;
  try {
    const value = JSON.parse(localStorage.getItem('auth-user') || 'null');
    return typeof value?.userId === 'number' ? value.userId : null;
  } catch { return null; }
}

export function emitLibraryChange(change: LibraryChange): void {
  if (typeof window === 'undefined' || change.accountId !== libraryAccountId()) return;
  window.dispatchEvent(new CustomEvent<LibraryChange>(LOCAL_EVENT, { detail: change }));
  // Other tabs receive only an invalidation marker, never an account, book or token.
  try { localStorage.setItem(STORAGE_KEY, `${Date.now()}:${Math.random()}`); } catch { /* Same-tab event still works. */ }
}

export function subscribeLibraryChanges(listener: (change: LibraryChange) => void): () => void {
  const local = (event: Event) => listener((event as CustomEvent<LibraryChange>).detail);
  const external = (event: StorageEvent) => {
    if (event.key === STORAGE_KEY && event.newValue) listener({ kind: 'invalidated', accountId: libraryAccountId() });
  };
  window.addEventListener(LOCAL_EVENT, local);
  window.addEventListener('storage', external);
  return () => {
    window.removeEventListener(LOCAL_EVENT, local);
    window.removeEventListener('storage', external);
  };
}

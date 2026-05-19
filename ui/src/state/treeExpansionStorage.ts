import { EMPTY, ExpansionState, fromPersisted, toPersisted } from './treeExpansion';

export interface StorageLike {
    getItem(key: string): string | null;
    setItem(key: string, value: string): void;
}

export const STORAGE_KEY = 'kensa-tree-collapsed';

function defaultStorage(): StorageLike | null {
    if (typeof window === 'undefined') return null;
    try {
        return window.localStorage;
    } catch {
        return null;
    }
}

export function loadExpansion(storage?: StorageLike): ExpansionState {
    const target = storage ?? defaultStorage();
    if (!target) return EMPTY;
    try {
        const raw = target.getItem(STORAGE_KEY);
        if (!raw) return EMPTY;
        return fromPersisted(JSON.parse(raw));
    } catch {
        return EMPTY;
    }
}

export function saveExpansion(state: ExpansionState, storage?: StorageLike): void {
    const target = storage ?? defaultStorage();
    if (!target) return;
    try {
        target.setItem(STORAGE_KEY, JSON.stringify(toPersisted(state)));
    } catch {
        // ignore quota / private-mode errors
    }
}

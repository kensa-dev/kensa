import { describe, it, expect } from 'vitest';
import { EMPTY, setCollapsed } from './treeExpansion';
import { loadExpansion, saveExpansion, STORAGE_KEY, type StorageLike } from './treeExpansionStorage';

function createStub(initial: Record<string, string> = {}): StorageLike & { store: Record<string, string> } {
    const store = { ...initial };
    return {
        store,
        getItem: (k: string) => (k in store ? store[k] : null),
        setItem: (k: string, v: string) => {
            store[k] = v;
        },
    };
}

describe('loadExpansion', () => {
    it('returns EMPTY when storage is empty', () => {
        const stub = createStub();
        expect([...loadExpansion(stub).collapsed]).toEqual([]);
    });

    it('returns EMPTY when stored JSON is malformed', () => {
        const stub = createStub({ [STORAGE_KEY]: '{not json' });
        expect([...loadExpansion(stub).collapsed]).toEqual([]);
    });

    it('returns EMPTY when stored value has the wrong shape', () => {
        const stub = createStub({ [STORAGE_KEY]: JSON.stringify({ hello: 'world' }) });
        expect([...loadExpansion(stub).collapsed]).toEqual([]);
    });
});

describe('saveExpansion', () => {
    it('writes JSON under STORAGE_KEY', () => {
        const stub = createStub();
        const state = setCollapsed(EMPTY, 'pkg:a', true);
        saveExpansion(state, stub);
        const written = stub.store[STORAGE_KEY];
        expect(JSON.parse(written)).toEqual({ version: 1, collapsed: ['pkg:a'] });
    });

    it('round-trips state via save then load', () => {
        const stub = createStub();
        const state = setCollapsed(setCollapsed(EMPTY, 'pkg:a', true), 'pkg:b', true);
        saveExpansion(state, stub);
        const restored = loadExpansion(stub);
        expect([...restored.collapsed].sort()).toEqual(['pkg:a', 'pkg:b']);
    });

    it('does not throw when the underlying storage throws', () => {
        const throwingStub: StorageLike = {
            getItem: () => null,
            setItem: () => { throw new Error('quota'); },
        };
        expect(() => saveExpansion(EMPTY, throwingStub)).not.toThrow();
    });
});
